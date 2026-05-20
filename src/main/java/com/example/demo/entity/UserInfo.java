package com.example.demo.entity;

import lombok.Data;

@Data
public class UserInfo {
    private Long userId;
    private String realName;
    private String phone;
    private String address;
}
