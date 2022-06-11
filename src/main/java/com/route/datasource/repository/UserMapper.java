package com.route.datasource.repository;

import java.util.List;

import com.route.datasource.model.User;

public interface UserMapper {
  public List<User> selectUserList(Integer worldId);
}
