package com.flyingpig.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;

// RowMapper接口
public interface RowMapper<T> {
    T mapRow(ResultSet rs, int rowNum) throws SQLException;
}
