package com.route.datasource.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.route.datasource.model.RouteDatabaseInfo;
import com.route.datasource.model.User;
import com.route.datasource.repository.RootMapper;
import com.route.datasource.repository.UserMapper;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class RouteCallService {
  
  private final RootMapper rootMapper;
  private final UserMapper userMapper;
  
  public List<RouteDatabaseInfo> getRootAllDatabases() {
    return rootMapper.selectDatabaseInfo();
  }
  
  public List<User> getUser(Integer worldId) {
    return userMapper.selectUserList(worldId);
  }
}
