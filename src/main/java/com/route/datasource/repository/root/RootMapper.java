package com.route.datasource.repository.root;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.route.datasource.model.RouteDatabaseInfo;

@Mapper
public interface RootMapper {
  public List<RouteDatabaseInfo> selectDatabaseInfo();
}
