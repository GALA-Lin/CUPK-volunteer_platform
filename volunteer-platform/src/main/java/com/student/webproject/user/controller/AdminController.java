package com.student.webproject.user.controller;

import com.student.webproject.common.response.Result;
import com.student.webproject.user.Service.AuthService;
import com.student.webproject.user.dto.UserLoginDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.Map;

@RestController
// 【关键】这个Controller处理所有 /api/admin/ 开头的请求
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private AuthService authService; // 复用之前创建的AuthService

    /**
     * 管理员登录接口
     */
    @PostMapping("/auth/login") // 最终路径是 /api/admin/auth/login
    public Result<?> adminLogin(@RequestBody UserLoginDTO userLoginDTO) {
        String token = authService.adminLogin(userLoginDTO);

        Map<String, String> tokenMap = Collections.singletonMap("token", token);
        return Result.success(tokenMap, "管理员登录成功");
    }
}