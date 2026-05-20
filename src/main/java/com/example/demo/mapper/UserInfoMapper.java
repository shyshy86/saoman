package com.example.demo.mapper;

import com.example.demo.vo.UserDetailVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public class UserInfoMapper {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public UserDetailVO getUserDetail(Long userId) {
        String sql = """
            SELECT 
                u.id AS userId, 
                u.username,
                i.real_name AS realName, 
                i.phone, 
                i.address 
            FROM sys_user u LEFT JOIN user_info i ON u.id = i.user_id 
            WHERE u.id = ?
            """;
        
        List<Map<String, Object>> results = jdbcTemplate.queryForList(sql, userId);
        
        if (results.isEmpty()) {
            return null;
        }
        
        Map<String, Object> row = results.get(0);
        UserDetailVO vo = new UserDetailVO();
        vo.setUserId(((Number) row.get("userId")).longValue());
        vo.setUsername((String) row.get("username"));
        vo.setRealName((String) row.get("realName"));
        vo.setPhone((String) row.get("phone"));
        vo.setAddress((String) row.get("address"));
        
        return vo;
    }

    public int updateUserInfo(Long userId, String realName, String phone, String address) {
        String sql = """
            UPDATE user_info 
            SET real_name = ?, phone = ?, address = ? 
            WHERE user_id = ?
            """;
        return jdbcTemplate.update(sql, realName, phone, address, userId);
    }

    public int insertUserInfo(Long userId, String realName, String phone, String address) {
        String sql = """
            INSERT INTO user_info (real_name, phone, address, user_id) 
            VALUES (?, ?, ?, ?)
            """;
        return jdbcTemplate.update(sql, realName, phone, address, userId);
    }

    public int deleteUserInfo(Long userId) {
        String sql = "DELETE FROM user_info WHERE user_id = ?";
        return jdbcTemplate.update(sql, userId);
    }

    public int deleteUser(Long userId) {
        String sql = "DELETE FROM sys_user WHERE id = ?";
        return jdbcTemplate.update(sql, userId);
    }

    public boolean userInfoExists(Long userId) {
        String sql = "SELECT COUNT(*) FROM user_info WHERE user_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, userId);
        return count != null && count > 0;
    }
}
