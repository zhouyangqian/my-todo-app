package com.example.auth.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

/**
 * 认证服务安全配置
 * <p>
 * 覆盖默认安全配置，将登录、注册、刷新令牌、健康检查等端点设为公开访问，
 * 其他端点仍需认证。
 * </p>
 */
@Configuration
public class AuthSecurityConfig {

    /**
     * 认证服务安全过滤器链
     * <p>
     * 公开端点：登录、注册、刷新令牌、健康检查、API 文档
     * 其他请求需要认证
     * </p>
     */
    @Bean
    public SecurityFilterChain authSecurityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)  // 禁用 CSRF
            .authorizeHttpRequests(auth -> auth
                // 公开端点：无需认证即可访问
                .requestMatchers(
                    "/api/auth/login",
                    "/api/auth/register",
                    "/api/auth/refresh",
                    "/api/auth/health",
                    "/api/auth/captcha",
                    "/api/auth/captcha/**",
                    "/api/auth/sse/**",
                    "/api/auth/tenant/register",
                    "/actuator/**",
                    "/doc.html",
                    "/webjars/**",
                    "/swagger-resources/**",
                    "/v3/api-docs/**"
                ).permitAll()
                // 其他所有请求需要认证
                .anyRequest().authenticated()
            );

        return http.build();
    }
}
