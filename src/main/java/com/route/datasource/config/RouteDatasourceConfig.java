package com.route.datasource.config;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.sql.DataSource;

import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.transaction.managed.ManagedTransactionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.jta.atomikos.AtomikosDataSourceBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

import com.route.datasource.model.RouteDatabaseInfo;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@MapperScan(value = "com.route.datasource.repository.routing", sqlSessionFactoryRef = "routingSessionFactory") // annotationClass 설정해서 특정 어노테이션만 매퍼로 인식하게 해서 다른 Datasource config랑 분리 가능
public class RouteDatasourceConfig {
  
  @DependsOn({ "rootSessionTemplate" })
  @Bean("routingDataSource")
  public DataSource routingDataSource(@Qualifier("rootSessionTemplate") SqlSessionTemplate sqlSessionTemplate) {
    List<RouteDatabaseInfo> list = sqlSessionTemplate.selectList("selectDatabaseInfo"); // 공통 DB에서 하위 DB 목록을 가져온다.
    
    Map<Object, Object> dataSourceMap = new HashMap<>();
    
    for (RouteDatabaseInfo info : list)
      dataSourceMap.put(info.getId(), makeDataSourceXA(info.getName()));
    
    AbstractRoutingDataSource routingDataSource = new RouteDataSource();
    routingDataSource.setTargetDataSources(dataSourceMap);
    //routingDataSource.setDefaultTargetDataSource(datasource); 룩업 키 설정 안할 경우의 대상. 그냥 에러로 두는게 나을 듯
    return routingDataSource;
  }
  
  @Bean(name = "routingSessionFactory")
  public SqlSessionFactory routingSessionFactory(@Qualifier("routingDataSource") DataSource dataSource, ApplicationContext applicationContext) throws Exception {
    SqlSessionFactoryBean sessionFactory = new SqlSessionFactoryBean();
    sessionFactory.setDataSource(dataSource);
    
    // 기본 Spring Sync session에서만 찾게되는 이슈로 아래 수정 필요하다.
    // ManagedTransactionFactory를 사용하게 하면 되는데, 이상하게 tx를 따로 인식하는지 Connection을 계속 새로 맺고 있었다.
    //sessionFactory.setTransactionFactory(new ManagedTransactionFactory()); 
    
    //그래서 MultiDataSourceTransactionFactory를 새로 구현해서 트랜젝션 내에서 동일 Connection은 재사용 하도록 했다.
    sessionFactory.setTransactionFactory(new MultiDataSourceTransactionFactory()); 
    sessionFactory.setMapperLocations(applicationContext.getResources("classpath:mapper/routing/*.xml"));
    
    return sessionFactory.getObject();
  }
  
  // XA Datasource 생성 - XA 가 적용된 트랜젝션 사용을 위한 하위 디비 사용 용도
  private DataSource makeDataSourceXA(String name) {
    AtomikosDataSourceBean dataSource = new AtomikosDataSourceBean();

    log.info("===XA DataSource ===");
    log.info(name);
    
    Properties properties = new Properties();
    properties.setProperty("user", "sa");
    properties.setProperty("password", "");
    properties.setProperty("url", "jdbc:h2:file:D:/data/" + name);  // 하위 DB의 연결은 파일 h2로 사용했다. (route-scheme.sql 참조)

    //XA 처리를 위한 드라이버 변경: AtomikosDataSourceBean은 XADataSource 인터페이스를 참조하고 있다. 다른 DB도 구현체만 바꿔주면 된다.
    //dataSource.setXaDataSourceClassName("com.mysql.jdbc.jdbc2.optional.MysqlXADataSource");     // MYSQL
    dataSource.setXaDataSourceClassName("org.h2.jdbcx.JdbcDataSource");                           // H2
    dataSource.setXaProperties(properties);
    
    dataSource.setUniqueResourceName("unique_H2_DB_" + name);
    dataSource.setPoolSize(5);                                      // 커넥션 풀 min/max 개수
    dataSource.setBorrowConnectionTimeout(600);                     // 커넥션 풀 대기 타임아웃 시간
    dataSource.setMaxIdleTime(60);                                  // Idle 상태인 커넥션 풀 자동 반환 시간
    
    // 기타 추가 옵션은 com.atomikos.jdbc.AtomikosDataSourceBean::doInit() 확인
    
    return dataSource;
  }

  /*
   Transaction Datasource Connect에 Lazy proxy 적용
   @Bean
    public DataSource lazyRoutingDataSource(
        @Qualifier(value = "routingDataSource") DataSource routingDataSource) {
        return new LazyConnectionDataSourceProxy(routingDataSource);
    }

    @Bean
    public PlatformTransactionManager transactionManager(
        @Qualifier(value = "lazyRoutingDataSource") DataSource lazyRoutingDataSource) {
        DataSourceTransactionManager transactionManager = new DataSourceTransactionManager();
        transactionManager.setDataSource(lazyRoutingDataSource);
        return transactionManager;
    }
    
    일반적인 TxManager 사용 시 ( XA가 적용되지 않으며, ThreadLocal의 Lookup 키관리 확실하게 잘해야 한다.. )
    @Bean(name = "routingTxManager")
    public DataSourceTransactionManager routeTransactionManager(@Qualifier("routingDataSource") DataSource datasource) {
      DataSourceTransactionManager manager = new DataSourceTransactionManager();
      manager.setDataSource(datasource);
      return manager;
    }
   */
}
