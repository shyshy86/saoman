package com.example.demo.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.PrintWriter;

public class AuthInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 1. 获取请求方法和路径
        String method = request.getMethod();
        String uri = request.getRequestURI();

        // 2. 仅放行2类合法请求：
        // - POST /api/users（新增用户）
        boolean isCreateUser = "POST".equalsIgnoreCase(method) && "/api/users".equals(uri);
        // - GET /api/users/xxx（查询用户，如/api/users/1）
        boolean isGetUser = "GET".equalsIgnoreCase(method) && uri.startsWith("/api/users/");

        // 满足上述2类，直接放行（无需Token）
        if (isCreateUser || isGetUser) {
            return true;
        }

        // 3. 其余请求（DELETE/PUT等）必须校验Token
        String token = request.getHeader("Authorization");
        if (token == null || token.isEmpty()) {

            response.setContentType("application/json;charset=UTF-8");
            String errorJson = "{\"code\":401,\"msg\":\"敏感操作["+method+"]需携带Token！\"}";
            PrintWriter writer = response.getWriter();
            writer.write(errorJson);
            writer.flush();
            writer.close();
            return false; // 拦截，不放行
        }


        return true;
    }
}