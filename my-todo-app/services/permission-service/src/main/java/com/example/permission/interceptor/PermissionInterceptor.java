package com.example.permission.interceptor;

import com.example.common.core.annotation.Logical;
import com.example.common.core.annotation.RequiresPermission;
import com.example.permission.service.PermissionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Map;

/**
 * 权限拦截器
 * <p>
 * 实现 HandlerInterceptor，检查目标方法或类上的 @RequiresPermission 注解，
 * 从请求头获取当前用户ID和租户ID，调用 PermissionService 校验用户是否有所需权限。
 * 无权限时返回 403 JSON 响应。
 * </p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PermissionInterceptor implements HandlerInterceptor {

    private final PermissionService permissionService;

    private final ObjectMapper objectMapper;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }

        // 查找方法级别注解（优先），其次查找类级别注解
        RequiresPermission methodAnnotation = handlerMethod.getMethodAnnotation(RequiresPermission.class);
        RequiresPermission classAnnotation = handlerMethod.getBeanType().getAnnotation(RequiresPermission.class);
        RequiresPermission annotation = methodAnnotation != null ? methodAnnotation : classAnnotation;

        if (annotation == null) {
            return true;
        }

        // 从请求头获取用户ID和租户ID
        Long userId = parseLongHeader(request, "X-User-Id");
        Long tenantId = parseLongHeader(request, "X-Tenant-Id");

        if (userId == null) {
            writeForbiddenResponse(response, "缺少用户信息，请重新登录");
            return false;
        }
        if (tenantId == null) {
            tenantId = 1L;
        }

        // 解析需要校验的权限编码
        String[] permissionCodes = resolvePermissionCodes(annotation);
        if (permissionCodes.length == 0) {
            return true;
        }

        // 根据逻辑类型进行校验
        boolean hasPermission;
        if (annotation.logical() == Logical.OR) {
            hasPermission = permissionService.hasAnyPermission(userId, tenantId, permissionCodes);
        } else {
            hasPermission = permissionService.hasAllPermissions(userId, tenantId, permissionCodes);
        }

        if (!hasPermission) {
            String permissionName = annotation.name();
            log.warn("权限校验失败: userId={}, requiredPermissions={}, name={}, logical={}",
                    userId, String.join(",", permissionCodes), permissionName, annotation.logical());
            writeForbiddenResponse(response, "没有权限执行此操作" + (permissionName.isEmpty() ? "" : ": " + permissionName));
            return false;
        }

        log.debug("权限校验通过: userId={}, permissions={}", userId, String.join(",", permissionCodes));
        return true;
    }

    /**
     * 解析注解中的权限编码数组
     * <p>
     * 优先使用 value()，如果为空则使用 code()（兼容旧用法）
     * </p>
     */
    private String[] resolvePermissionCodes(RequiresPermission annotation) {
        String[] values = annotation.value();
        if (values.length > 0) {
            return values;
        }

        String code = annotation.code();
        if (!code.isEmpty()) {
            return new String[]{code};
        }

        return new String[0];
    }

    /**
     * 写入 403 JSON 响应
     */
    private void writeForbiddenResponse(HttpServletResponse response, String message) throws Exception {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json;charset=UTF-8");
        Map<String, Object> result = Map.of(
                "code", 403,
                "message", message,
                "data", "",
                "timestamp", System.currentTimeMillis()
        );
        response.getWriter().write(objectMapper.writeValueAsString(result));
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
