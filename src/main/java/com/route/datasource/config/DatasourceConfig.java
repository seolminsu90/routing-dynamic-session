package com.route.datasource.config;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;
import org.springframework.lang.Nullable;

import com.route.datasource.model.RouteDatabaseInfo;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
public class DatasourceConfig {
  @Bean(name = "rootDataSource")
  public EmbeddedDatabase rootDataSource() {
    return makeDataSource("common", "root-schema.sql", "root-data.sql");
  }
  
  @Bean(name = "rootSessionFactory")
  public SqlSessionFactory rootSessionFactory(@Qualifier("rootDataSource") DataSource dataSource, ApplicationContext applicationContext) throws Exception {
    SqlSessionFactoryBean sessionFactory = new SqlSessionFactoryBean();
    sessionFactory.setDataSource(dataSource);
    sessionFactory.setMapperLocations(applicationContext.getResources("classpath:mapper/root/*.xml"));
    
    return sessionFactory.getObject();
  }
  
  @Bean(name = "rootSessionTemplate")
  public SqlSessionTemplate rootSqlSessionTemplate(@Qualifier("rootSessionFactory") SqlSessionFactory sqlSessionFactory) {
    return new SqlSessionTemplate(sqlSessionFactory);
  }
  
  @Bean("routingDataSource")
  public DataSource routingDataSource(@Qualifier("rootSessionTemplate") SqlSessionTemplate sqlSessionTemplate) {
    List<RouteDatabaseInfo> list = sqlSessionTemplate.selectList("selectDatabaseInfo");
    
    Map<Object, Object> dataSourceMap = new HashMap<>();
    
    for (RouteDatabaseInfo info : list)
      dataSourceMap.put(info.getId(), makeDataSource(info.getName(), "route-schema.sql", "route-" + info.getName() + ".sql"));
    
    AbstractRoutingDataSource routingDataSource = new RouteDatasource();
    routingDataSource.setTargetDataSources(dataSourceMap);
    // routingDataSource.setDefaultTargetDataSource(world1Datasource);
    return routingDataSource;
  }
  
  @Bean(name = "routingSessionFactory")
  public SqlSessionFactory routingSessionFactory(@Qualifier("routingDataSource") DataSource dataSource, ApplicationContext applicationContext) throws Exception {
    SqlSessionFactoryBean sessionFactory = new SqlSessionFactoryBean();
    sessionFactory.setDataSource(dataSource);
    sessionFactory.setMapperLocations(applicationContext.getResources("classpath:mapper/routing/*.xml"));
    
    return sessionFactory.getObject();
  }
  
  @Bean(name = "routingSessionTemplate")
  public SqlSessionTemplate routingSqlSessionTemplate(@Qualifier("routingSessionFactory") SqlSessionFactory sqlSessionFactory) {
    return new SqlSessionTemplate(sqlSessionFactory);
  }
  
  /*
   * -- master/slave 방식의 TransactionSynchronizationManager.isCurrentTransactionReadOnly 사용 시 필요
   * 
   * @Bean("routingLazyDataSource") public DataSource routingLazyDataSource(@Qualifier("RoutingDataSource") DataSource dataSource) { return new LazyConnectionDataSourceProxy(dataSource); }
   */
  
  private EmbeddedDatabase makeDataSource(String name, @Nullable String createSqlLoc, @Nullable String addDataSqlLoc) {
    
    log.info(name);
    log.info(createSqlLoc);
    log.info(addDataSqlLoc);
    
    EmbeddedDatabaseBuilder builder = new EmbeddedDatabaseBuilder();
    builder.setType(EmbeddedDatabaseType.H2).setName(name);
    if (createSqlLoc != null) builder.addScript(createSqlLoc);
    if (addDataSqlLoc != null) builder.addScript(addDataSqlLoc);
    
    return builder.build();
  }
}
