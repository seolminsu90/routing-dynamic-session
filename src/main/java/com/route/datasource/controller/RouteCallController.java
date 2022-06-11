package com.route.datasource.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.route.datasource.model.RestResponse;
import com.route.datasource.model.RouteDatabaseInfo;
import com.route.datasource.model.User;
import com.route.datasource.model.UserRequest;
import com.route.datasource.service.RouteCallService;

@RestController
public class RouteCallController {
  
  @Autowired
  private RouteCallService routeCallService;
  
  // 공통데이터베이스 - 모든 하위 데이터베이스 조회
  @GetMapping("/api/root/databases")
  public RestResponse<List<RouteDatabaseInfo>> getRootAllDatabases() {
    return new RestResponse<List<RouteDatabaseInfo>>(routeCallService.getRootAllDatabases(), HttpStatus.OK);
  }
  
  // 하위데이터베이스 - 월드 별 유저 목록 조회
  @GetMapping("/api/users")
  public RestResponse<List<User>> getRouteUsers(@RequestParam(name = "worldId", defaultValue = "1") Integer worldId) {
    return new RestResponse<List<User>>(routeCallService.getUser(worldId), HttpStatus.OK);
  }
  
  // 하위데이터베이스 - 특정 이름의 유저 1,2,3을 모든 월드에 생성한다.
  @PostMapping("/api/users/{name}")
  public RestResponse<Integer> createRouteUser(@PathVariable String name) {
    UserRequest request = new UserRequest(name);
    return new RestResponse<Integer>(routeCallService.createRouteUser(request), HttpStatus.CREATED);
  }
}
