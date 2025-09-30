package com.student.webproject.user.Service;


import com.student.webproject.user.Entity.User;
import com.student.webproject.user.dto.UserLoginDTO;
import com.student.webproject.user.dto.UserRegisterDTO;

public interface AuthService {

    /**
     * 处理用户注册的业务逻辑
     * @param userRegisterDTO 包含用户注册信息的数据传输对象
     * @return 返回创建成功后的用户信息实体 (包含数据库生成的ID)
     */
    User register(UserRegisterDTO userRegisterDTO);

    /**
     * 处理用户登录的业务逻辑
     * @param userLoginDTO 包含用户登录信息的数据传输对象
     * @return 登录成功后生成的 JWT
     */
//    String login(UserLoginDTO userLoginDTO);
    String adminLogin(UserLoginDTO userLoginDTO);
}