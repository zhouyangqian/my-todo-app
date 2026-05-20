package com.example.common.permission.aspect;

import com.example.common.core.annotation.DataPermission;
import com.example.common.core.exception.BusinessException;
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
 * 数据权限切面
 * <p>
 * 拦截标注了 @DataPermission 注解的 Controller 方法，
 * 根据注解配置的数据范围，将过滤条件存入 ThreadLocal，
 * 供 MyBatis-Plus 拦截器在 SQL 执行时自动添加 WHERE 条件。
 * </p>
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class DataPermissionAspect {

    /** ThreadLocal 存储当前请求的数据权限上下文 */
    private static final ThreadLocal<DataPermissionContext> CONTEXT = new ThreadLocal<>();

    @Before("@annotation(dataPermission)")
    public void applyDataPermission(JoinPoint joinPoint, DataPermission dataPermission) {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return;
        }

        HttpServletRequest request = attributes.getRequest();
        Long userId = parseLongHeader(request, "X-User-Id");
        Long tenantId = parseLongHeader(request, "X-Tenant-Id");

        if (userId == null) {
            return;
        }

        DataPermissionContext context = new DataPermissionContext();
        context.setUserId(userId);
        context.setTenantId(tenantId != null ? tenantId : 1L);
        context.setDataScope(dataPermission.value());
        context.setDeptField(dataPermission.deptField());
        context.setUserField(dataPermission.userField());

        // 从请求头获取部门ID（如果有的话）
        Long deptId = parseLongHeader(request, "X-Dept-Id");
        context.setDeptId(deptId);

        CONTEXT.set(context);
        log.debug("数据权限上下文已设置: userId={}, scope={}", userId, dataPermission.value());
    }

    /**
     * 获取当前线程的数据权限上下文
     */
    public static DataPermissionContext getContext() {
        return CONTEXT.get();
    }

    /**
     * 清除当前线程的数据权限上下文
     */
    public static void clearContext() {
        CONTEXT.remove();
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

    /**
     * 数据权限上下文
     */
    public static class DataPermissionContext {
        private Long userId;
        private Long tenantId;
        private Long deptId;
        private DataPermission.DataScope dataScope;
        private String deptField;
        private String userField;

        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }

        public Long getTenantId() { return tenantId; }
        public void setTenantId(Long tenantId) { this.tenantId = tenantId; }

        public Long getDeptId() { return deptId; }
        public void setDeptId(Long deptId) { this.deptId = deptId; }

        public DataPermission.DataScope getDataScope() { return dataScope; }
        public void setDataScope(DataPermission.DataScope dataScope) { this.dataScope = dataScope; }

        public String getDeptField() { return deptField; }
        public void setDeptField(String deptField) { this.deptField = deptField; }

        public String getUserField() { return userField; }
        public void setUserField(String userField) { this.userField = userField; }
    }
}
