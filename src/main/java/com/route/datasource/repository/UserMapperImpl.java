package com.route.datasource.repository;

import java.util.List;
import java.util.Set;

import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import com.route.datasource.config.RouteDataSource;
import com.route.datasource.model.User;
import com.route.datasource.util.ThreadLocalContext;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Repository
public class UserMapperImpl implements UserMapper {
  
  @Autowired
  @Qualifier("routingSessionTemplate")
  SqlSessionTemplate sessionTemplate;
  
  @Override
  public List<User> selectUserList() {
    return sessionTemplate.selectList("selectUserList");
  }
  
  @Override
  public int createRouteUser(String name) {
    return sessionTemplate.insert("createRouteUser", name);
  }
  
}
