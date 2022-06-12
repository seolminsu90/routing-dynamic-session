package com.route.datasource.config;

import javax.transaction.TransactionManager;
import javax.transaction.UserTransaction;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.jta.JtaTransactionManager;

import com.atomikos.icatch.jta.UserTransactionImp;
import com.atomikos.icatch.jta.UserTransactionManager;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
public class XaDataManagerConfig {
  /*
   * JtaTransactionManager를 빈으로 등록한다. JtaTransactionManager는 여타 트랜잭션 매니저와는 다르게 프로퍼티로 DataSource나 SessionFactory 등의 빈을 참조하지 않는다. 대신 서버에 등록된 트랜잭션 매니저를 가져와 JTA를 이용해서 트랜잭션을 관리해줄 뿐이다.
   */
  
  @Bean(name = "userTransaction")
  public UserTransaction userTransaction() throws Throwable {
    log.info("========= userTransaction =========");
    UserTransactionImp userTransactionImp = new UserTransactionImp();
    userTransactionImp.setTransactionTimeout(10000);
    
    return userTransactionImp;
  }
  
  @Bean(name = "atomikosTransactionManager", initMethod = "init", destroyMethod = "close")
  public TransactionManager atomikosTransactionManager() throws Throwable {
    log.info("========= atomikosTransactionManager =========");
    UserTransactionManager userTransactionManager = new UserTransactionManager();
    userTransactionManager.setForceShutdown(false);
    
    return userTransactionManager;
  }
  
  @Bean(name = "multiTxManager")
  @DependsOn({ "userTransaction", "atomikosTransactionManager" })
  public PlatformTransactionManager transactionManager(UserTransaction userTransaction, TransactionManager atomikosTransactionManager) throws Throwable {
    log.info("========= transactionManager =========");
    
    JtaTransactionManager manager = new JtaTransactionManager(userTransaction, atomikosTransactionManager);
    
    return manager;
  }
}