package com.route.datasource.repository;

import java.util.List;

import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import com.route.datasource.model.RouteDatabaseInfo;

@Repository
public class RootMapperImpl implements RootMapper {
  @Autowired
  @Qualifier("rootSessionTemplate")
  SqlSessionTemplate sessionTemplate;
  
  @Override
  public List<RouteDatabaseInfo> selectDatabaseInfo() {
    return sessionTemplate.selectList("selectDatabaseInfo");
  }
  
}
