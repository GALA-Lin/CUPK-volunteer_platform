package com.student.webproject.user.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.student.webproject.user.Entity.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import java.util.Date;

@Component
public class JwtUtils {

    // 从配置文件注入密钥
    @Value("${jwt.secret-key}")
    private String secretKey;

    // 从配置文件注入过期时间
    @Value("${jwt.expire-time}")
    private long expireTime;

    /**
     * 根据UserDetails对象生成JWT Token
     */
    public String generateTokenByUserDetails(UserDetails userDetails) {
        Date expirationDate = new Date(System.currentTimeMillis() + expireTime);

        return JWT.create()
                .withClaim("username", userDetails.getUsername())
                .withExpiresAt(expirationDate)
                .sign(Algorithm.HMAC256(secretKey));
    }

    /**
     * 从Token中提取用户名
     */
    public String extractUsername(String token) {
        try {
            return getDecodedJWT(token).getClaim("username").asString();
        } catch (JWTVerificationException e) {
            return null;
        }
    }

    /**
     * 验证Token是否有效
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        try {
            final String username = extractUsername(token);
            return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
        } catch (JWTVerificationException e) {
            return false;
        }
    }

    /**
     * 检查Token是否已过期
     */
    private boolean isTokenExpired(String token) {
        Date expiration = getDecodedJWT(token).getExpiresAt();
        return expiration.before(new Date());
    }

    /**
     * 私有辅助方法：用于验证并解析Token
     */
    private DecodedJWT getDecodedJWT(String token) {
        JWTVerifier verifier = JWT.require(Algorithm.HMAC256(secretKey)).build();
        return verifier.verify(token);
    }

    /**
     * 根据User对象生成Token的方法
     */
    public String generateToken(User user) {
        Date expirationDate = new Date(System.currentTimeMillis() + expireTime);

        return JWT.create()
                .withClaim("userId", user.getId())
                .withClaim("username", user.getUsername())
                .withClaim("avatarUrl", user.getAvatarUrl())
                .withExpiresAt(expirationDate)
                .sign(Algorithm.HMAC256(secretKey));
    }
}
