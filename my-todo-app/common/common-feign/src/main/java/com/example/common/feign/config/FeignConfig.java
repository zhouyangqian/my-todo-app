package com.example.common.feign.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Feign 配置类
 * <p>
 * 配置 Feign 客户端拦截器，实现以下功能：
 * <ul>
 *   <li>传递认证令牌 - 服务间调用时自动传递 JWT Token</li>
 *   <li>传递租户ID - 服务间调用时自动传递租户ID，确保数据隔离</li>
 *   <li>传递追踪ID - 服务间调用时传递 TraceId，便于链路追踪</li>
 * </ul>
 * </p>
 */
@Configuration
public class FeignConfig {

    /**
     * Feign 请求拦截器
     * <p>
     * 在每次 Feign 调用前，将当前请求的上下文信息（Token、租户ID、追踪ID）
     * 自动添加到 Feign 请求头中，实现上下文传递。
     * </p>
     */
    @Bean
    public RequestInterceptor requestInterceptor() {
        return new RequestInterceptor() {
            @Override
            public void apply(RequestTemplate template) {
                ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
                if (attributes == null) {
                    return;
                }

                HttpServletRequest request = attributes.getRequest();

                // 1. 传递认证令牌
                String token = request.getHeader("Authorization");
                if (token != null && !token.isEmpty()) {
                    template.header("Authorization", token);
                }

                // 2. 传递租户ID
                String tenantId = request.getHeader("X-Tenant-Id");
                if (tenantId != null && !tenantId.isEmpty()) {
                    template.header("X-Tenant-Id", tenantId);
                }

                // 3. 传递追踪ID（用于链路追踪）
                String traceId = request.getHeader("X-Trace-Id");
                if (traceId != null && !traceId.isEmpty()) {
                    template.header("X-Trace-Id", traceId);
                }

                // 4. 传递其他自定义请求头（可选）
                String userAgent = request.getHeader("User-Agent");
                if (userAgent != null && !userAgent.isEmpty()) {
                    template.header("X-Original-User-Agent", userAgent);
                }
            }
        };
    }
}
