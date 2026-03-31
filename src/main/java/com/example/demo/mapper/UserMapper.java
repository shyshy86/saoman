package com.example.demo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.demo.entity.User;
import org.apache.ibatis.annotations.Mapper;

// 继承BaseMapper获得MyBatis-Plus的CRUD能力
@Mapper // 标记为MyBatis的Mapper接口
public interface UserMapper extends BaseMapper<User> {
    // 无需手写SQL，BaseMapper已包含单表增删改查
}