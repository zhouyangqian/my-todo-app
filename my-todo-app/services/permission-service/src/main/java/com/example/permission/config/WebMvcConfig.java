package com.example.permission.config;

import com.example.permission.interceptor.PermissionInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC 配置
 * <p>
 * 注册拦截器，包括租户拦截器、权限拦截器等。
 * </p>
 */
@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final TenantInterceptor tenantInterceptor;

    private final PermissionInterceptor permissionInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 注册租户拦截器，拦截所有请求（优先执行）
        registry.addInterceptor(tenantInterceptor)
                .addPathPatterns("/**")
                .order(1);

        // 注册权限拦截器，拦截所有 API 请求
        registry.addInterceptor(permissionInterceptor)
                .addPathPatterns("/api/**")
                .order(2);
    }
}
