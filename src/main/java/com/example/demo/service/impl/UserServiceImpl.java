package com.example.demo.service.impl;

import com.example.demo.common.Result;
import com.example.demo.common.ResultCode;
import com.example.demo.dto.UserDTO;
import com.example.demo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import java.util.UUID;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public Result<String> register(UserDTO userDTO) {
        // 1. 检查用户名是否存在
        String checkSql = "SELECT COUNT(*) FROM sys_user WHERE username=?";
        Integer count = jdbcTemplate.queryForObject(checkSql, Integer.class, userDTO.getUsername());

        if (count > 0) {
            return Result.error(ResultCode.USER_HAS_EXISTED);
        }

        // 2. 插入数据库
        String insertSql = "INSERT INTO sys_user(username,password) VALUES(?,?)";
        jdbcTemplate.update(insertSql, userDTO.getUsername(), userDTO.getPassword());

        return Result.success("注册成功！");
    }

    @Override
    public Result<String> login(UserDTO userDTO) {
        try {
            // 1. 查询密码
            String sql = "SELECT password FROM sys_user WHERE username=?";
            String dbPwd = jdbcTemplate.queryForObject(sql, String.class, userDTO.getUsername());

            // 2. 校验密码
            if (!dbPwd.equals(userDTO.getPassword())) {
                return Result.error(ResultCode.PASSWORD_ERROR);
            }

            // 3. 生成token
            String token = "Bearer_" + UUID.randomUUID().toString().replace("-", "");
            return Result.success(token);

        } catch (EmptyResultDataAccessException e) {
            return Result.error(ResultCode.USER_NOT_EXIST);
        }
    }
}