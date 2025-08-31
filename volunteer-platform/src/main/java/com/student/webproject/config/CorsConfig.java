package com.student.webproject.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.List;

/**
 * 全局跨域配置
 * 通过 CorsFilter 实现，确保在 Spring Security 过滤器链之前执行
 */
@Configuration
public class CorsConfig {

    @Bean
    // 使用 @Order(Ordered.HIGHEST_PRECEDENCE) 确保该过滤器在所有过滤器中具有最高优先级
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public CorsFilter corsFilter() {
        // 1. 创建 CorsConfiguration 对象
        CorsConfiguration config = new CorsConfiguration();

        // 2. 配置允许的来源、方法和头信息
        // 允许前端应用访问
        config.setAllowedOrigins(List.of(
                "http://localhost:5173",
                "http://162.14.104.26"
        ));
        // 允许发送 Cookie
        config.setAllowCredentials(true);

        // 允许所有请求方法: "GET", "POST", "PUT", "DELETE", "OPTIONS" 等
        config.addAllowedMethod("*");

        // 允许所有头信息
        config.addAllowedHeader("*");
        // 允许允许携带凭证（cookie）
        config.setAllowCredentials(true);
        // 预检请求缓存时间（秒）
        config.setMaxAge(3600L);

        // 3. 创建 UrlBasedCorsConfigurationSource 对象
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // 4. 为所有接口 ("/**") 应用上述CORS配置
        source.registerCorsConfiguration("/**", config);

        // 5. 返回新的 CorsFilter 实例
        return new CorsFilter(source);
    }
}
