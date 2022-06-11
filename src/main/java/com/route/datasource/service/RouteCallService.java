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
import com.route.datasource.model.UserRequest;
import com.route.datasource.repository.RootMapper;
import com.route.datasource.repository.UserMapper;
import com.route.datasource.util.ThreadLocalContext;

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
  
  public List<RouteDatabaseInfo> getRootAllDatabases() {
    return rootMapper.selectDatabaseInfo();
  }
  
  public List<User> getUser(Integer worldId) {
    ThreadLocalContext.set(worldId);
    return userMapper.selectUserList();
  }
  
  /*
   * XA 트랜젝션 Manager 사용
   * 시나리오
   * - 월드 목록 가져온다. (현재 1,2)
   * - 월드 별로 입력받은 이름으로 각기 다른 3개의 유저를 생성한다.
   * - ErrorTest로 넣을 경우 2번 월드에 유저를 생성 할 때 에러가 발생하고, 1번 월드까지 롤백되는 것을 확인 할 수 있다.
   * - 일반 routeTxManager로 설정 시 월드별 작업을 따로 Transaction으로 묶어야 한다. (한개만 적용되는 이슈로 인해)
   * - *참조 : 트렌젝션은 프록시로 동작하기에 private나 동일클래스 다른 메소드 호출 등에 동작하지 않음.
   */
  @Transactional(value = "multiTxManager")
  public int createRouteUser(UserRequest request) {
    
    Set<Integer> ids = routeDataSource.getServerIds();
    log.info(ids.toString());
    
    for (Integer id : ids) {
      ThreadLocalContext.set(id);
      log.info("Active Tx : {}", String.valueOf(TransactionSynchronizationManager.isActualTransactionActive()));
      log.info("TxName : {}", TransactionSynchronizationManager.getCurrentTransactionName());
      log.info("ThreadLocal {}", ThreadLocalContext.get());
      
      userMapper.createRouteUser(request.getName() + "_1");
      userMapper.createRouteUser(request.getName() + "_2");
      if ("ErrorTest".equals(request.getName()) && id == 2) throw new RuntimeException("임의의 2번 월드 트렌젝션 확인용 익셉션");
      userMapper.createRouteUser(request.getName() + "_3");
    }
    
    return 0;
  }
}
