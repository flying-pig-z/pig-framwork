package com.flyingpig.jdbc.transaction;

import com.flyingpig.jdbc.DataSource;

import java.sql.Connection;
import java.sql.SQLException;

// 事务管理器实现
public class DataSourceTransactionManager implements TransactionManager {
    private DataSource dataSource;
    private ThreadLocal<Connection> connectionHolder = new ThreadLocal<>();

    public DataSourceTransactionManager(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void begin() throws SQLException {
        Connection conn = dataSource.getConnection();
        conn.setAutoCommit(false);
        connectionHolder.set(conn);
    }

    @Override
    public void commit() throws SQLException {
        Connection conn = connectionHolder.get();
        if (conn != null) {
            conn.commit();
            conn.close();
            connectionHolder.remove();
        }
    }

    @Override
    public void rollback() throws SQLException {
        Connection conn = connectionHolder.get();
        if (conn != null) {
            conn.rollback();
            conn.close();
            connectionHolder.remove();
        }
    }
}
