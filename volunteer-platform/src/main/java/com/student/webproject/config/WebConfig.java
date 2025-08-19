package com.student.webproject.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // 允许所有路径进行跨域
                // 同时允许本地开发环境和服务器环境的前端访问
                .allowedOrigins(
                        "http://localhost:5173",  // 本地开发环境
                        "http://162.14.104.26"    // 服务器前端地址（你的公网IP）
                )
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // 允许的请求方法
                .allowedHeaders("*") // 允许所有的请求头
                .allowCredentials(true) // 允许发送Cookie
                .maxAge(3600); // 预检请求的有效期，单位为秒
    }
}
