package com.example.demo.service;

import com.example.demo.common.Result;
import com.example.demo.dto.UserDTO;

public interface UserService {
    Result<String> register(UserDTO userDTO);
    Result<String> login(UserDTO userDTO);

    Result<String> getUserById(Long id);
}