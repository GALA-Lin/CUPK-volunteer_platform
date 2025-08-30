package com.student.webproject.user.controller;

import com.student.webproject.common.response.Result;
import com.student.webproject.user.dto.UserLoginDTO;
import com.student.webproject.user.dto.UserRegisterDTO;
import com.student.webproject.user.Service.AuthService;
import com.student.webproject.user.util.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService; // 注册服务依然保留

    // --- 新增注入 ---
    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtils jwtUtils;
    // --- 新增结束 ---


    @PostMapping("/register")
    public Result<?> register(@RequestBody UserRegisterDTO userRegisterDTO) {
        // 注册逻辑不变
        return Result.created(authService.register(userRegisterDTO));
    }


    /**
     * 用户登录接口
     */
    @PostMapping("/login")
    public Result<?> login(@RequestBody UserLoginDTO userLoginDTO) {
        // 1. 创建一个 Spring Security 的认证凭证
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(userLoginDTO.getUsername(), userLoginDTO.getPassword());

        // 2. 调用 AuthenticationManager 进行认证，它会自动调用我们写的 UserDetailsServiceImpl 和 PasswordEncoder
        // 如果认证失败，这里会自动抛出异常，并被我们的 GlobalExceptionHandler 捕获
        Authentication authentication = authenticationManager.authenticate(authenticationToken);

        // 3. 认证成功，生成JWT
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String token = jwtUtils.generateTokenByUserDetails(userDetails); // 假设JwtUtils有个新方法

        // 4. 封装成功的响应
        Map<String, String> tokenMap = Collections.singletonMap("token", token);
        return Result.success(tokenMap, "登录成功");
    }

}