package com.example.demo.entity;

// 用户实体类
public class User {
    // 主键ID
    private Long id;
    // 用户名
    private String name;
    // 年龄
    private Integer age;

    // 无参构造（Spring 解析JSON需要）
    public User() {
    }

    // 全参构造
    public User(Long id, String name, Integer age) {
        this.id = id;
        this.name = name;
        this.age = age;
    }

    // Getter和Setter方法（必须，否则JSON解析不到属性）
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }
}