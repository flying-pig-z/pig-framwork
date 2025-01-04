package com.flyingpig.demo.service;

import com.flyingpig.demo.entity.User;
import com.flyingpig.jdbc.JdbcTemplate;
import com.flyingpig.jdbc.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {
    @Autowired
    private JdbcTemplate jdbcTemplate;


    public void createUser(User user) {
        String sql = "INSERT INTO user (id, username, email) VALUES (?, ?, ?)";
        try {
            jdbcTemplate.update(sql, user.getId(), user.getUsername(), user.getEmail());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // 删除用户
    public void deleteUser(String userId) {
        String sql = "DELETE FROM user WHERE id = ?";
        try {
            jdbcTemplate.update(sql, userId);
        } catch (SQLException e) {
            throw new RuntimeException("删除用户失败: " + e.getMessage(), e);
        }
    }

    // 修改用户名称
    public void updateUsername(String userId, String newUsername) {
        String sql = "UPDATE user SET username = ? WHERE id = ?";
        try {
            jdbcTemplate.update(sql, newUsername, userId);
        } catch (SQLException e) {
            throw new RuntimeException("更新用户名称失败: " + e.getMessage(), e);
        }
    }

    public User getUserById(Long id) {
        String sql = "SELECT * FROM user WHERE id = ?";
        try {
            return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> {
                return new User(rs.getString("id"), rs.getString("email"), rs.getString("username"));
            }, id);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Transactional
    public void batchCreateUsers(List<User> users) {
        String sql = "INSERT INTO user (id, username, email) VALUES (?, ?, ?)";

        try {
            List<Object[]> batchArgs = users.stream()
                    .map(user -> new Object[]{
                            user.getId(),
                            user.getUsername(),
                            user.getEmail()
                    })
                    .collect(Collectors.toList());

            jdbcTemplate.batchUpdate(sql, batchArgs);
        } catch (Exception e) {
            throw new RuntimeException("批量创建用户失败", e);
        }
    }
}