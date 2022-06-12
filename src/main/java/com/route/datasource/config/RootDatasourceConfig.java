package com.route.datasource.config;

import javax.sql.DataSource;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.lang.Nullable;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@EnableTransactionManagement
@MapperScan(value = "com.route.datasource.repository.root", sqlSessionFactoryRef = "rootSessionFactory")
public class RootDatasourceConfig {
  /* 
      하위 DB들 정보를 가지고 있는 루트 CommonDB의 역할.
      하위 DB와 Tx를 공유해야 한다면 별도로 RouteDataSource에 포함되도록 구성해야한다.
      한개짜리 AbstractRoutingDataSource를 구현하고 새 AbstractRoutingDataSource Bean에 추가하던지? 뭐 방법은 많을 듯
  */
  @Bean(name = "rootDataSource")
  public DataSource rootDataSource() {
    return makeDataSource("common", "root-schema.sql", "root-data.sql");
  }
  
  @Primary
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
  
  @Bean(name = "rootTxManager")
  public DataSourceTransactionManager rootTransactionManager(@Qualifier("rootDataSource") DataSource datasource) {
    DataSourceTransactionManager manager = new DataSourceTransactionManager();
    manager.setDataSource(datasource);
    return manager;
  }
  
  // Datasource 생성 - 공통 디비 사용 용도
  private DataSource makeDataSource(String name, @Nullable String createSqlLoc, @Nullable String addDataSqlLoc) {
    
    log.info("=== DataSource ===");
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
