package com.example.common.security.jwt;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * JWT 配置属性类
 * <p>
 * 从 application.yml 中读取 jwt 前缀的配置项，
 * 包括密钥、令牌过期时间、签发者等信息。
 * </p>
 */
@Data
@Component
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {

    /** JWT 签名密钥 */
    private String secret;

    /** 访问令牌过期时间（毫秒），默认2小时 */
    private Long accessTokenExpiration = 7200000L;

    /** 刷新令牌过期时间（毫秒），默认7天 */
    private Long refreshTokenExpiration = 604800000L;

    /** 令牌签发者名称 */
    private String issuer = "my-todo-app";

    /** 令牌受众 */
    private String audience = "my-todo-app-users";
}
