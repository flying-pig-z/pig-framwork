package com.flyingpig.jdbc;

import java.sql.Connection;
import java.sql.SQLException;

// 数据源配置接口
public interface DataSource {
    Connection getConnection() throws SQLException;
}
