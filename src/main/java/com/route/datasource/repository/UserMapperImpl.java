package com.route.datasource.repository;

import java.util.List;

import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import com.route.datasource.model.User;
import com.route.datasource.util.ThreadLocalContext;

@Repository
public class UserMapperImpl implements UserMapper {
  @Autowired
  @Qualifier("routingSessionTemplate")
  SqlSessionTemplate sessionTemplate;
  
  @Override
  public List<User> selectUserList(Integer worldId) {
    ThreadLocalContext.set(worldId);
    return sessionTemplate.selectList("selectUserList");
  }
  
}
