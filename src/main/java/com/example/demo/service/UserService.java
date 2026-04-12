package com.example.demo.service;

import com.example.demo.common.Result;
import com.example.demo.dto.UserDTO;

public interface UserService {
    Result<String> register(UserDTO userDTO);
    Result<String> login(UserDTO userDTO);

    Result<String> getUserById(Long id);
    
    // 获取用户分页数据
    Result<Object> getUserPage(Integer pageNum, Integer pageSize);
}