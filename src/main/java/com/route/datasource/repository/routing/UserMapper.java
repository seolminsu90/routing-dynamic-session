package com.route.datasource.repository.routing;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.route.datasource.annotation.LookupKey;
import com.route.datasource.annotation.RoutingMapper;
import com.route.datasource.model.User;
import com.route.datasource.model.UserDTO;

@Mapper
@RoutingMapper
public interface UserMapper {
  public List<User> selectUserList(@LookupKey Integer worldId);
  
  public int createRouteUser(@LookupKey UserDTO dto);
}
