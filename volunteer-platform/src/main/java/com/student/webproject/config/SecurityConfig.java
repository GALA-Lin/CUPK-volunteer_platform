package com.student.webproject.config;

import com.student.webproject.security.JwtAuthenticationTokenFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Autowired
    private JwtAuthenticationTokenFilter jwtAuthenticationTokenFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(withDefaults())
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authz -> authz
                        // 【关键修改】第一条规则：无条件放行所有OPTIONS预检请求
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // 第二部分：配置无需认证即可访问的公开API
                        // 登录、注册、AI聊天等POST请求
                        .requestMatchers(
                                "/api/auth/login",
                                "/api/auth/register",
                                "/api/admin/auth/login", // 管理员登录
                                "/api/chat/**"
                        ).permitAll()
                        // 所有公开的GET请求
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/activities/**",
                                "/api/news/**",
                                "/api/organizations/**",
                                "/api/dashboard/**"
                        ).permitAll()

                        // 第三部分：配置需要特定权限的路径
                        // 所有/api/admin/下的请求都需要 'super_admin' 或 'admin' 角色
                        .requestMatchers("/api/admin/**").hasAnyAuthority("super_admin", "admin")

                        // 第四部分【必须是最后一条规则】：其他所有未明确匹配的请求，都必须经过认证
                        .anyRequest().authenticated()
                )
                // 在UsernamePasswordAuthenticationFilter之前添加JWT过滤器
                .addFilterBefore(jwtAuthenticationTokenFilter, UsernamePasswordAuthenticationFilter.class)
                // 配置退出登录
                .logout(logout -> logout
                        .logoutUrl("/api/auth/logout")
                        .logoutSuccessHandler(new HttpStatusReturningLogoutSuccessHandler(HttpStatus.OK))
                );

        return http.build();
    }
}
