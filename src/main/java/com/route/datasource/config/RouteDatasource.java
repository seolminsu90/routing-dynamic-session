package com.route.datasource.config;

import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

import com.route.datasource.util.ThreadLocalContext;

public class RouteDatasource extends AbstractRoutingDataSource {
  
  @Override
  protected Object determineCurrentLookupKey() {
    return ThreadLocalContext.get();
  }
  
}
