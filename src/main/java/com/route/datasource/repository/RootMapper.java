package com.route.datasource.repository;

import java.util.List;

import com.route.datasource.model.RouteDatabaseInfo;

public interface RootMapper {
  public List<RouteDatabaseInfo> selectDatabaseInfo();
}
