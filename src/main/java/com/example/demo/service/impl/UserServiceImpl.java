package com.example.demo.service.impl;

import com.example.demo.common.Result;
import com.example.demo.common.ResultCode;
import com.example.demo.dto.UserDTO;
import com.example.demo.entity.UserInfo;
import com.example.demo.mapper.UserInfoMapper;
import com.example.demo.service.UserService;
import com.example.demo.vo.UserDetailVO;
import cn.hutool.json.JSONUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class UserServiceImpl implements UserService {

    private static final String CACHE_KEY_PREFIX = "user:detail:";

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private UserInfoMapper userInfoMapper;

    @Override
    public Result<String> register(UserDTO userDTO) {
        String checkSql = "SELECT COUNT(*) FROM sys_user WHERE username=?";
        Integer count = jdbcTemplate.queryForObject(checkSql, Integer.class, userDTO.getUsername());

        if (count > 0) {
            return Result.error(ResultCode.USER_HAS_EXISTED);
        }

        String insertSql = "INSERT INTO sys_user(username,password) VALUES(?,?)";
        jdbcTemplate.update(insertSql, userDTO.getUsername(), userDTO.getPassword());

        return Result.success("注册成功！");
    }

    @Override
    public Result<String> login(UserDTO userDTO) {
        try {
            String sql = "SELECT password FROM sys_user WHERE username=?";
            String dbPwd = jdbcTemplate.queryForObject(sql, String.class, userDTO.getUsername());

            if (!dbPwd.equals(userDTO.getPassword())) {
                return Result.error(ResultCode.PASSWORD_ERROR);
            }

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
        int offset = (pageNum - 1) * pageSize;
        String dataSql = "SELECT id, username FROM sys_user LIMIT ? OFFSET ?";
        List<Map<String, Object>> records = jdbcTemplate.queryForList(dataSql, pageSize, offset);
        String countSql = "SELECT COUNT(*) FROM sys_user";
        Integer total = jdbcTemplate.queryForObject(countSql, Integer.class);
        int pages = (total + pageSize - 1) / pageSize;
        Map<String, Object> result = Map.of(
            "records", records,
            "total", total,
            "current", pageNum,
            "size", pageSize,
            "pages", pages
        );
        return Result.success(result);
    }

    @Override
    public Result<UserDetailVO> getUserDetail(Long userId) {
        String key = CACHE_KEY_PREFIX + userId;
        
        // 1. 先查缓存
        String json = redisTemplate.opsForValue().get(key);
        if (json != null && !json.isBlank()) {
            try {
                UserDetailVO cacheVO = JSONUtil.toBean(json, UserDetailVO.class);
                return Result.success(cacheVO);
            } catch (Exception e) {
                // 缓存数据异常，删掉脏缓存，继续查数据库
                redisTemplate.delete(key);
            }
        }
        
        // 2. 查数据库
        UserDetailVO detail = userInfoMapper.getUserDetail(userId);
        if (detail == null) {
            return Result.error(ResultCode.USER_NOT_EXIST);
        }
        
        // 3. 写缓存
        redisTemplate.opsForValue().set(
            key,
            JSONUtil.toJsonStr(detail),
            10,
            TimeUnit.MINUTES
        );
        
        return Result.success(detail);
    }

    @Override
    public Result<String> updateUserInfo(UserInfo userInfo) {
        // 参数校验
        if (userInfo == null || userInfo.getUserId() == null) {
            return Result.error(ResultCode.ERROR);
        }

        // 检查用户是否存在
        String checkSql = "SELECT COUNT(*) FROM sys_user WHERE id = ?";
        Integer count = jdbcTemplate.queryForObject(checkSql, Integer.class, userInfo.getUserId());
        if (count == null || count == 0) {
            return Result.error(ResultCode.USER_NOT_EXIST);
        }

        // 更新或插入用户扩展信息
        if (userInfoMapper.userInfoExists(userInfo.getUserId())) {
            userInfoMapper.updateUserInfo(userInfo.getUserId(), userInfo.getRealName(), userInfo.getPhone(), userInfo.getAddress());
        } else {
            userInfoMapper.insertUserInfo(userInfo.getUserId(), userInfo.getRealName(), userInfo.getPhone(), userInfo.getAddress());
        }

        // 删除缓存
        String cacheKey = CACHE_KEY_PREFIX + userInfo.getUserId();
        redisTemplate.delete(cacheKey);

        return Result.success("更新成功！");
    }

    @Override
    public Result<String> deleteUser(Long userId) {
        // 删除用户扩展信息
        userInfoMapper.deleteUserInfo(userId);
        // 删除用户
        userInfoMapper.deleteUser(userId);
        // 删除缓存
        String cacheKey = CACHE_KEY_PREFIX + userId;
        redisTemplate.delete(cacheKey);
        return Result.success("删除成功！");
    }
}
