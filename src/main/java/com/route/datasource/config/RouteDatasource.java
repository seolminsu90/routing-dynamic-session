package com.route.datasource.config;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.sql.DataSource;

import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

import com.route.datasource.util.ThreadLocalContext;

public class RouteDataSource extends AbstractRoutingDataSource {
  
  @Override
  protected Object determineCurrentLookupKey() {
    return ThreadLocalContext.get();
  }
  
  // AbstractRoutingDataSource 에 등록된 월드를 LookupKey로 변환해서 리턴
  public Set<Integer> getServerLookupWorldIds() {
    Map<Object, DataSource> resolvedDataSources = super.getResolvedDataSources();
    return resolvedDataSources.keySet().stream().map(key -> Integer.valueOf(key.toString())).collect(Collectors.toSet());
  }
}
