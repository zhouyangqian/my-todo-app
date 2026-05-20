package com.example.common.permission.aspect;

import com.example.common.core.annotation.RequiresPermission;
import com.example.common.core.exception.BusinessException;
import com.example.common.permission.checker.PermissionChecker;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * 权限校验切面
 * <p>
 * 拦截所有标注了 {@link RequiresPermission} 注解的 Controller 方法，
 * 通过调用 permission-service 的接口校验当前用户是否拥有指定权限。
 * </p>
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class PermissionAspect {

    private final PermissionChecker permissionChecker;

    /**
     * 前置通知：在目标方法执行前进行权限校验
     */
    @Before("@annotation(requiresPermission)")
    public void checkPermission(JoinPoint joinPoint, RequiresPermission requiresPermission) {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            throw new BusinessException(500, "无法获取请求上下文");
        }
        HttpServletRequest request = attributes.getRequest();

        Long userId = parseLongHeader(request, "X-User-Id");
        Long tenantId = parseLongHeader(request, "X-Tenant-Id");

        if (userId == null) {
            throw new BusinessException(401, "缺少用户信息，请重新登录");
        }
        if (tenantId == null) {
            tenantId = 1L;
        }

        boolean hasPermission = permissionChecker.checkPermission(userId, tenantId, requiresPermission.code());

        if (!hasPermission) {
            log.warn("权限校验失败: userId={}, requiredPermission={}, name={}",
                    userId, requiresPermission.code(), requiresPermission.name());
            throw new BusinessException(403, "没有权限执行此操作: " + requiresPermission.name());
        }

        log.debug("权限校验通过: userId={}, permission={}", userId, requiresPermission.code());
    }

    private Long parseLongHeader(HttpServletRequest request, String headerName) {
        String value = request.getHeader(headerName);
        if (value == null || value.isEmpty()) {
            return null;
        }
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
