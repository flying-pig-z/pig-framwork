package com.flyingpig.jdbc;

import com.flyingpig.jdbc.connection.ConnectionHolder;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

// JdbcTemplate核心类
public class JdbcTemplate {
    private DataSource dataSource;

    public JdbcTemplate(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    // 获取连接的方法
    protected Connection getConnection() throws SQLException {
        Connection conn = ConnectionHolder.getConnection();
        return conn != null ? conn : dataSource.getConnection();
    }

    // 关闭连接的方法
    protected void releaseConnection(Connection conn) throws SQLException {
        if (conn != null && ConnectionHolder.getConnection() != conn) {
            conn.close();
        }
    }

    // 查询单个对象
    public <T> T queryForObject(String sql, RowMapper<T> rowMapper, Object... args) throws SQLException {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = getConnection();
            ps = conn.prepareStatement(sql);
            for (int i = 0; i < args.length; i++) {
                ps.setObject(i + 1, args[i]);
            }
            rs = ps.executeQuery();
            if (rs.next()) {
                return rowMapper.mapRow(rs, 1);
            }
            return null;
        } finally {
            closeResources(conn, ps, rs);
        }
    }

    // 查询单个值
    public <T> T queryForValue(String sql, Class<T> requiredType, Object... args) throws SQLException {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = getConnection();
            ps = conn.prepareStatement(sql);
            for (int i = 0; i < args.length; i++) {
                ps.setObject(i + 1, args[i]);
            }
            rs = ps.executeQuery();
            if (rs.next()) {
                return (T) rs.getObject(1);
            }
            return null;
        } finally {
            closeResources(conn, ps, rs);
        }
    }

    // 查询列表
    public <T> List<T> queryForList(String sql, RowMapper<T> rowMapper, Object... args) throws SQLException {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = getConnection();
            ps = conn.prepareStatement(sql);
            for (int i = 0; i < args.length; i++) {
                ps.setObject(i + 1, args[i]);
            }
            rs = ps.executeQuery();
            List<T> results = new ArrayList<>();
            int rowNum = 0;
            while (rs.next()) {
                results.add(rowMapper.mapRow(rs, ++rowNum));
            }
            return results;
        } finally {
            closeResources(conn, ps, rs);
        }
    }

    // 更新操作
    public int update(String sql, Object... args) throws SQLException {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = getConnection();
            ps = conn.prepareStatement(sql);
            for (int i = 0; i < args.length; i++) {
                ps.setObject(i + 1, args[i]);
            }
            return ps.executeUpdate();
        } finally {
            closeResources(conn, ps, null);
        }
    }

    // 批量更新
    public int[] batchUpdate(String sql, List<Object[]> batchArgs) throws SQLException {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = getConnection();
            ps = conn.prepareStatement(sql);

            for (Object[] args : batchArgs) {
                for (int i = 0; i < args.length; i++) {
                    ps.setObject(i + 1, args[i]);
                }
                ps.addBatch();
            }

            return ps.executeBatch();
        } finally {
            closeResources(conn, ps, null);
        }
    }

    // 关闭资源
    private void closeResources(Connection conn, Statement stmt, ResultSet rs) {
        try {
            if (rs != null) {
                rs.close();
            }
            if (stmt != null) {
                stmt.close();
            }
            // 只在非事务场景下关闭连接
            if (conn != null && ConnectionHolder.getConnection() == null) {
                conn.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}