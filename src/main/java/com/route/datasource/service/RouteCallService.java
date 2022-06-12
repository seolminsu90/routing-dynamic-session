package com.route.datasource.service;

import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.route.datasource.config.RouteDataSource;
import com.route.datasource.model.RouteDatabaseInfo;
import com.route.datasource.model.User;
import com.route.datasource.model.UserDTO;
import com.route.datasource.model.UserRequest;
import com.route.datasource.repository.root.RootMapper;
import com.route.datasource.repository.routing.UserMapper;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@AllArgsConstructor
public class RouteCallService {
  
  private final RootMapper rootMapper;
  private final UserMapper userMapper;
  
  @Autowired
  private RouteDataSource routeDataSource;
  
  @Transactional(value = "rootTxManager")
  public List<RouteDatabaseInfo> getRootAllDatabases() {
    log.debug("Active Tx : {}", String.valueOf(TransactionSynchronizationManager.isActualTransactionActive()));
    log.debug("TxName : {}", TransactionSynchronizationManager.getCurrentTransactionName());
    
    return rootMapper.selectDatabaseInfo();
  }
  
  public List<User> getUser(Integer worldId) {
    // ThreadLocalContext.set(worldId); Aspect 적용으로 필요없어짐
    return userMapper.selectUserList(worldId);
  }
  
  // XA multiTxManager가 XaDatasource를 관리해준다.
  @Transactional(value = "multiTxManager")
  public int createRouteUser(UserRequest request) {
    
    Set<Integer> ids = routeDataSource.getServerLookupWorldIds();
    log.debug(ids.toString());
    
    // 교차 트랜젝션 확인을 위해 For문 -> 교차 월드 작업
    log.debug("Active Tx : {}", String.valueOf(TransactionSynchronizationManager.isActualTransactionActive()));
    log.debug("TxName : {}", TransactionSynchronizationManager.getCurrentTransactionName());
      
    userMapper.createRouteUser(new UserDTO(1, request.getName() + "_1" + 1));
    userMapper.createRouteUser(new UserDTO(2, request.getName() + "_1" + 1));
    if ("Error1".equals(request.getName())) throw new RuntimeException("임의의 1번 월드 트렌젝션 확인용 익셉션");
    userMapper.createRouteUser(new UserDTO(3, request.getName() + "_1" + 1));
    
    userMapper.createRouteUser(new UserDTO(2, request.getName() + "_2" + 2));
    userMapper.createRouteUser(new UserDTO(1, request.getName() + "_2" + 2));
    if ("Error2".equals(request.getName())) throw new RuntimeException("임의의 2번 월드 트렌젝션 확인용 익셉션");
    userMapper.createRouteUser(new UserDTO(3, request.getName() + "_2" + 2));
    
    userMapper.createRouteUser(new UserDTO(3, request.getName() + "_3" + 3));
    if ("Error3".equals(request.getName())) throw new RuntimeException("임의의 3번 월드 트렌젝션 확인용 익셉션");
    userMapper.createRouteUser(new UserDTO(2, request.getName() + "_3" + 3));
    userMapper.createRouteUser(new UserDTO(1, request.getName() + "_3" + 3));
    
    return 0;
  }
  
  // JtaTransactionManager isolation 조절 필요시 allowCustomIsolationLevels 수정. isolation 기본은 DB 디폴트 값
  @Transactional(value = "multiTxManager", isolation = Isolation.READ_COMMITTED)
  public int createRouteUser(UserRequest request, Integer id) {
    // CP 잘 사용하는지 체크해보려는 용도, IsolationLevel 바뀌는지 체크
    log.info("Active Tx : {}", String.valueOf(TransactionSynchronizationManager.isActualTransactionActive()));
    log.info("TxName : {}", TransactionSynchronizationManager.getCurrentTransactionName());
    log.info("IsolationLevel : {}", TransactionSynchronizationManager.getCurrentTransactionIsolationLevel());
    
    userMapper.createRouteUser(new UserDTO(id, request.getName() + "_1" + id));
    userMapper.createRouteUser(new UserDTO(id, request.getName() + "_2" + id));
    userMapper.createRouteUser(new UserDTO(id, request.getName() + "_3" + id));
    try {
      // Pool을 하나로 하여 대기하게 하고 여러개 쓰레드가 동시 실행되면
      // com.atomikos.icatch.default_jta_timeout 가 지날 경우 에러발생 함.
      // 또는 setBorrowConnectionTimeout에 의해 풀을 기다리다 에러 발생하기도 함.
      Thread.sleep(5000);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    log.info("createRouteUser End : {}", id);
    
    return 0;
  }
}
