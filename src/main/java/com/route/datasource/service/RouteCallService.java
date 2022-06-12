package com.route.datasource.service;

import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
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
    log.info("Active Tx : {}", String.valueOf(TransactionSynchronizationManager.isActualTransactionActive()));
    log.info("TxName : {}", TransactionSynchronizationManager.getCurrentTransactionName());
    
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
    log.info(ids.toString());
    
    // 교차 트랜젝션 확인을 위해 For문 -> 교차 월드 작업
    log.info("Active Tx : {}", String.valueOf(TransactionSynchronizationManager.isActualTransactionActive()));
    log.info("TxName : {}", TransactionSynchronizationManager.getCurrentTransactionName());
      
    userMapper.createRouteUser(new UserDTO(1, request.getName() + "_1" + 1));
    userMapper.createRouteUser(new UserDTO(2, request.getName() + "_2" + 2));
    if ("TestError".equals(request.getName())) throw new RuntimeException("임의의 1번 월드 트렌젝션 확인용 익셉션");
    userMapper.createRouteUser(new UserDTO(1, request.getName() + "_3" + 1));
    
    userMapper.createRouteUser(new UserDTO(2, request.getName() + "_1" + 2));
    userMapper.createRouteUser(new UserDTO(1, request.getName() + "_2" + 1));
    if ("ErrorTest".equals(request.getName())) throw new RuntimeException("임의의 2번 월드 트렌젝션 확인용 익셉션");
    userMapper.createRouteUser(new UserDTO(2, request.getName() + "_3" + 2));
    
    return 0;
  }
  
  @Transactional(value = "multiTxManager")
  public int createRouteUser(UserRequest request, Integer id) {
    
    log.info("Active Tx : {}", String.valueOf(TransactionSynchronizationManager.isActualTransactionActive()));
    log.info("TxName : {}", TransactionSynchronizationManager.getCurrentTransactionName());
    
    // null로 이름을 보내면 아예 커넥션을 맺지 않아본다.
    if (!"null".equals(request.getName())) userMapper.createRouteUser(new UserDTO(id, request.getName() + "_1" + id));
    
    return 0;
  }
}
