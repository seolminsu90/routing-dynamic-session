package com.route.datasource.config;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ibatis.transaction.Transaction;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.jdbc.datasource.DataSourceUtils;

import com.route.datasource.util.ThreadLocalContext;

public class MultiDataSourceTransaction implements Transaction {
  private static final Log LOGGER = LogFactory.getLog(MultiDataSourceTransaction.class);
  
  private final DataSource dataSource;
  private Connection main;
  private Integer currentLookupKey;
  
  private ConcurrentMap<Integer, Connection> other;
  
  public MultiDataSourceTransaction(DataSource dataSource) {
    this.dataSource = dataSource;
    other = new ConcurrentHashMap<>();
    currentLookupKey = ThreadLocalContext.get();
  }
  
  @Override
  public Connection getConnection() throws SQLException {
    Integer nowLookupKey = ThreadLocalContext.get();
    if (nowLookupKey == null) throw new RuntimeException("can't find LookupKey");
    
    LOGGER.debug("getConnection... :: LookupKey" + nowLookupKey);
    if (nowLookupKey.equals(currentLookupKey)) {
      if (main != null) {
        LOGGER.debug("getConnection... :: 기존 연결을 사용합니다.");
        return main;
      } else {
        LOGGER.debug("getConnection... :: 새 연결을 맺습니다.");
        this.main = DataSourceUtils.getConnection(this.dataSource);
        currentLookupKey = nowLookupKey;
        return main;
      }
    } else {
      if (!other.containsKey(nowLookupKey)) {
        LOGGER.debug("getConnection... :: 다른 새 연결을 맺습니다.");
        try {
          Connection conn = dataSource.getConnection();
          other.put(nowLookupKey, conn);
        } catch (SQLException ex) {
          throw new CannotGetJdbcConnectionException("Could not get JDBC Connection", ex);
        }
      } else {
        LOGGER.debug("getConnection... :: 기존 연결을 사용합니다.");
      }
      return other.get(nowLookupKey);
    }
    
  }
  
  @Override
  public void commit() throws SQLException {
    LOGGER.debug("XaDataManager :: XAResource가 대신 관리합니다.");
  }
  
  @Override
  public void rollback() throws SQLException {
    LOGGER.debug("XaDataManager :: XAResource가 대신 관리합니다.");
  }
  
  @Override
  public void close() throws SQLException {
    LOGGER.debug("메인 커넥션을 닫습니다.");
    DataSourceUtils.releaseConnection(this.main, this.dataSource);
    for (Connection connection : other.values()) {
      LOGGER.debug("다른 커넥션을 닫습니다.");
      DataSourceUtils.releaseConnection(connection, this.dataSource);
    }
  }
  
  @Override
  public Integer getTimeout() throws SQLException {
    return null;
  }
}