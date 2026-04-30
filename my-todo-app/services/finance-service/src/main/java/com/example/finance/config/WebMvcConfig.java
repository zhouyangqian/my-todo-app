package com.example.finance.config;

import com.example.common.mybatis.config.TenantContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC 配置
 * <p>
 * 配置租户拦截器，从请求头中提取租户ID并设置到 TenantContext。
 * </p>
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    /**
     * 租户拦截器
     */
    private static class TenantInterceptor implements HandlerInterceptor {

        @Override
        public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
            // 从请求头获取租户ID
            String tenantIdHeader = request.getHeader("X-Tenant-Id");
            if (tenantIdHeader != null && !tenantIdHeader.isEmpty()) {
                try {
                    Long tenantId = Long.parseLong(tenantIdHeader);
                    TenantContext.setTenantId(tenantId);
                } catch (NumberFormatException e) {
                    // 租户ID格式无效，使用默认值
                    TenantContext.setTenantId(1L);
                }
            } else {
                // 请求头中没有租户ID，使用默认值
                TenantContext.setTenantId(1L);
            }
            return true;
        }

        @Override
        public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
            // 请求完成后清除 TenantContext，避免 ThreadLocal 内存泄漏
            TenantContext.clear();
        }
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new TenantInterceptor()).addPathPatterns("/**");
    }
}
