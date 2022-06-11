package com.route.datasource.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.route.datasource.model.RestResponse;
import com.route.datasource.model.RouteDatabaseInfo;
import com.route.datasource.model.User;
import com.route.datasource.service.RouteCallService;

@RestController
public class RouteCallController {
  
  @Autowired
  private RouteCallService routeCallService;
  
  @GetMapping("/api/root/databases")
  public RestResponse<List<RouteDatabaseInfo>> getRootAllDatabases() {
    return new RestResponse<List<RouteDatabaseInfo>>(routeCallService.getRootAllDatabases(), HttpStatus.OK);
  }
  
  @GetMapping("/api/users")
  public RestResponse<List<User>> getRouteUsers(@RequestParam(name = "worldId", defaultValue = "1") Integer worldId) {
    return new RestResponse<List<User>>(routeCallService.getUser(worldId), HttpStatus.OK);
  }
}
