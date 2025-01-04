package com.flyingpig.jdbc.transaction;

import com.flyingpig.jdbc.DataSource;
import com.flyingpig.jdbc.connection.ConnectionHolder;

import java.sql.Connection;
import java.sql.SQLException;

// 事务管理器实现
public class DataSourceTransactionManager implements TransactionManager {
    private DataSource dataSource;

    public DataSourceTransactionManager(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void begin() throws SQLException {
        Connection conn = dataSource.getConnection();
        conn.setAutoCommit(false);
        ConnectionHolder.setConnection(conn);
    }

    public void commit() throws SQLException {
        Connection conn = ConnectionHolder.getConnection();
        if (conn != null) {
            conn.commit();
            conn.setAutoCommit(true);
            conn.close();
            ConnectionHolder.removeConnection();
        }
    }

    public void rollback() throws SQLException {
        Connection conn = ConnectionHolder.getConnection();
        if (conn != null) {
            conn.rollback();
            conn.setAutoCommit(true);
            conn.close();
            ConnectionHolder.removeConnection();
        }
    }
}
