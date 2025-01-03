package com.flyingpig.jdbc.transaction;

import java.sql.SQLException;

// 事务管理器接口
public interface TransactionManager {
    void begin() throws SQLException;

    void commit() throws SQLException;

    void rollback() throws SQLException;
}
