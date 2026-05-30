package com.example.user.aspect;

import com.example.user.service.AuditLogService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Arrays;

/**
 * 操作日志切面
 * <p>
 * 拦截 Controller 层方法，记录操作人、操作类型、请求参数、返回结果、耗时，
 * 并通过 AuditLogService.log() 保存审计日志。
 * </p>
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class OperationLogAspect {

    private final AuditLogService auditLogService;

    /**
     * 拦截 user-service 下所有 Controller 的公共方法
     */
    @Around("execution(public * com.example.user.controller..*.*(..))")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String className = signature.getDeclaringTypeName();
        String methodName = signature.getName();
        String operationType = deriveOperationType(signature.getMethod().getName());

        // 提取请求头中的租户ID和操作人ID
        Long tenantId = null;
        Long operatorId = null;
        String ipAddress = null;
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            tenantId = parseLong(request.getHeader("X-Tenant-Id"));
            operatorId = parseLong(request.getHeader("X-User-Id"));
            ipAddress = getClientIp(request);
        }

        // 记录请求参数
        String params = truncateParams(Arrays.toString(joinPoint.getArgs()));

        Object result = null;
        boolean success = true;
        try {
            result = joinPoint.proceed();
            return result;
        } catch (Throwable e) {
            success = false;
            throw e;
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            String detail = buildDetail(className, methodName, params, duration, success);

            try {
                auditLogService.log(
                        tenantId,
                        operatorId,
                        operationType,
                        "USER",
                        null,
                        detail,
                        ipAddress
                );
            } catch (Exception e) {
                log.warn("记录操作日志失败: {}", e.getMessage());
            }
        }
    }

    /**
     * 根据方法名推导操作类型
     */
    private String deriveOperationType(String methodName) {
        if (methodName.startsWith("create") || methodName.startsWith("add")) {
            return "CREATE";
        } else if (methodName.startsWith("update") || methodName.startsWith("edit")) {
            return "UPDATE";
        } else if (methodName.startsWith("delete") || methodName.startsWith("remove")) {
            return "DELETE";
        } else if (methodName.startsWith("enable")) {
            return "ENABLE";
        } else if (methodName.startsWith("disable")) {
            return "DISABLE";
        } else if (methodName.startsWith("assign")) {
            return "ASSIGN";
        } else if (methodName.startsWith("import")) {
            return "IMPORT";
        } else if (methodName.startsWith("export") || methodName.startsWith("download")) {
            return "EXPORT";
        } else {
            return "QUERY";
        }
    }

    /**
     * 构建日志详情
     */
    private String buildDetail(String className, String methodName,
                                String params, long duration, boolean success) {
        return className + "." + methodName
                + " | params=" + params
                + " | duration=" + duration + "ms"
                + " | success=" + success;
    }

    /**
     * 截断过长的参数字符串，防止日志过大
     */
    private String truncateParams(String params) {
        if (params != null && params.length() > 500) {
            return params.substring(0, 500) + "...(truncated)";
        }
        return params;
    }

    /**
     * 获取客户端IP
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }

    /**
     * 安全解析 Long 值
     */
    private Long parseLong(String value) {
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
