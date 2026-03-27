package com.example.demo.controller;

import com.example.demo.common.Result;       // 导入统一响应类
import com.example.demo.common.ResultCode;   // 导入状态码枚举
import com.example.demo.entity.User;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    // 1. 查：根据ID获取用户信息（GET请求，路径 /api/users/{id}）
    @GetMapping("/{id}")

    public Result<String> getUser(@PathVariable("id") Long id) {
        String data = "查询成功，正在返回 ID 为 " + id + " 的用户信息";
        // 使用统一响应工具类返回成功结果
        return Result.success(data);
    }

    // 2. 增：新增用户（POST请求，路径 /api/users）
    @PostMapping
    // 返回值改为 Result<String> 统一格式
    public Result<String> createUser(@RequestBody User user) {
        // 增加空值校验（可选，提升健壮性）
        if (user == null || user.getName() == null) {
            return Result.error(ResultCode.ERROR);
        }
        String data = "新增成功，接收到用户：" + user.getName() + "，年龄：" + user.getAge();
        return Result.success(data);
    }

    // 3. 改：更新用户信息（PUT请求，路径 /api/users/{id}）
    @PutMapping("/{id}")
    // 返回值改为 Result<String> 统一格式
    public Result<String> updateUser(@PathVariable("id") Long id, @RequestBody User user) {
        String data = "更新成功，ID " + id + " 的用户已改为：" + user.getName();
        return Result.success(data);
    }

    // 4. 删：删除用户（DELETE请求，路径 /api/users/{id}）
    @DeleteMapping("/{id}")
    // 返回值改为 Result<String> 统一格式
    public Result<String> deleteUser(@PathVariable("id") Long id) {
        String data = "删除成功，已移除 ID 为 " + id + " 的用户";
        return Result.success(data);
    }

    // 新增：登录接口（用于拦截器放行测试）
    @GetMapping("/login")
    public Result<String> login() {
        String data = "登录成功，生成Token：abc123456";
        return Result.success(data);
    }
}