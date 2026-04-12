package com.example.demo.service.impl;

import com.example.demo.common.Result;
import com.example.demo.common.ResultCode;
import com.example.demo.dto.UserDTO;
import com.example.demo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;
import java.util.UUID;

//
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

    @Override
    public Result<String> getUserById(Long id) {
        String sql = "SELECT username FROM sys_user WHERE id = ?";
        try {
            String username = jdbcTemplate.queryForObject(sql, String.class, id);
            return Result.success("查询成功，用户名为：" + username);
        } catch (EmptyResultDataAccessException e) {
            return Result.error(ResultCode.USER_NOT_EXIST);
        }
    }

    @Override
    public Result<Object> getUserPage(Integer pageNum, Integer pageSize) {
        // 1. 计算偏移量
        int offset = (pageNum - 1) * pageSize;
        
        // 2. 查询数据
        String dataSql = "SELECT id, username FROM sys_user LIMIT ? OFFSET ?";
        List<Map<String, Object>> records = jdbcTemplate.queryForList(dataSql, pageSize, offset);
        
        // 3. 查询总数
        String countSql = "SELECT COUNT(*) FROM sys_user";
        Integer total = jdbcTemplate.queryForObject(countSql, Integer.class);
        
        // 4. 计算总页数
        int pages = (total + pageSize - 1) / pageSize;
        
        // 5. 构建返回结果
        Map<String, Object> result = Map.of(
            "records", records,
            "total", total,
            "current", pageNum,
            "size", pageSize,
            "pages", pages
        );
        
        return Result.success(result);
    }
}