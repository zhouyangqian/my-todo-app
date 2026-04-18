package com.example.common.security.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Spring Security 默认安全配置
 * <p>
 * 配置密码编码器（BCrypt）、CSRF 禁用（无状态 API）、Session 策略（无状态）、
 * 公开访问端点（健康检查、API文档）和认证要求。
 * </p>
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    /**
     * 密码编码器 Bean，使用 BCrypt 算法
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 安全过滤器链配置
     * <p>
     * - 禁用 CSRF（无状态 API 不需要）
     * - 使用无状态 Session（不创建 HttpSession）
     * - 公开端点：健康检查、API 文档等
     * - 其他所有请求需要认证
     * </p>
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // 禁用 CSRF，因为是前后端分离的无状态 API
            .csrf(AbstractHttpConfigurer::disable)
            // 使用无状态 Session，不创建 HttpSession
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            // 配置请求授权规则
            .authorizeHttpRequests(auth -> auth
                // 公开端点：健康检查、Swagger 文档等无需认证
                .requestMatchers(
                    "/actuator/health",
                    "/actuator/info",
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
