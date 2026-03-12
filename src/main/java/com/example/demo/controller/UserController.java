package com.example.demo.controller;

import com.example.demo.entity.User;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    // 1. 查：根据ID获取用户信息（GET请求，路径 /api/users/{id}）
    // @GetMapping 匹配GET请求，{id}是路径变量
    @GetMapping("/{id}")
    // @PathVariable 提取路径中的id值
    public String getUser(@PathVariable("id") Long id) {
        return "查询成功，正在返回 ID 为 " + id + " 的用户信息";
    }

    // 2. 增：新增用户（POST请求，路径 /api/users）
    // @PostMapping 匹配POST请求
    @PostMapping
    // @RequestBody 将请求体中的JSON数据转换为User对象
    public String createUser(@RequestBody User user) {
        return "新增成功，接收到用户：" + user.getName() + "，年龄：" + user.getAge();
    }

    // 3. 改：更新用户信息（PUT请求，路径 /api/users/{id}）
    // @PutMapping 匹配PUT请求
    @PutMapping("/{id}")
    public String updateUser(@PathVariable("id") Long id, @RequestBody User user) {
        return "更新成功，ID " + id + " 的用户已改为：" + user.getName();
    }

    // 4. 删：删除用户（DELETE请求，路径 /api/users/{id}）
    // @DeleteMapping 匹配DELETE请求
    @DeleteMapping("/{id}")
    public String deleteUser(@PathVariable("id") Long id) {
        return "删除成功，已移除 ID 为 " + id + " 的用户";
    }
}
