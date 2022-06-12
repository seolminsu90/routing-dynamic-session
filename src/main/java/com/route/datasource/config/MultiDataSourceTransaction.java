package com.route.datasource.config;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.sql.DataSource;

import org.apache.ibatis.transaction.Transaction;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.jdbc.datasource.DataSourceUtils;

import com.route.datasource.util.ThreadLocalContext;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MultiDataSourceTransaction implements Transaction {
  private final DataSource dataSource;
  
  private ConcurrentMap<Integer, Connection> connections;
  
  public MultiDataSourceTransaction(DataSource dataSource) {
    this.dataSource = dataSource;
    connections = new ConcurrentHashMap<>();
  }
  
  @Override
  public Connection getConnection() throws SQLException {
    Integer nowLookupKey = ThreadLocalContext.get();
    if (nowLookupKey == null) throw new RuntimeException("getConnection... :: LookupKey가 Context에 설정되지 않았습니다.");
    
    if (connections.isEmpty()) {
      log.info("getConnection... :: 연결 하나를 가져옵니다. [LookUp : {}]", nowLookupKey);
      connections.put(nowLookupKey, DataSourceUtils.getConnection(this.dataSource));
    } else {
      if (!connections.containsKey(nowLookupKey)) {
        log.info("getConnection... :: 다른 연결 하나를 가져옵니다. [LookUp : {}]", nowLookupKey);
        try {
          Connection conn = this.dataSource.getConnection();
          connections.put(nowLookupKey, conn);
        } catch (SQLException ex) {
          throw new CannotGetJdbcConnectionException("getConnection... :: LookupKey에 해당하는 DB가 없거나 가져오기에 실패했습니다.", ex);
        }
      } else {
        log.info("getConnection... :: 기존 연결을 사용합니다. [LookUp : {}]", nowLookupKey);
      }
    }
    return connections.get(nowLookupKey);
  }
  
  @Override
  public void commit() throws SQLException {
    log.info("XaDataManager :: XAResource가 대신 관리합니다.");
  }
  
  @Override
  public void rollback() throws SQLException {
    log.info("XaDataManager :: XAResource가 대신 관리합니다.");
  }
  
  @Override
  public void close() throws SQLException {
    for (Connection connection : connections.values()) {
      log.info("열린 연결을 반환합니다.");
      DataSourceUtils.releaseConnection(connection, this.dataSource);
    }
  }
  
  @Override
  public Integer getTimeout() throws SQLException {
    return null;
  }
}