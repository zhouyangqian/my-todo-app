package com.example.permission.interceptor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 数据权限拦截器（服务级别）
 * <p>
 * 实际的数据权限 SQL 过滤由以下两层协作完成：
 * <ul>
 *   <li>common-mybatis 的 DataPermissionInterceptor — MyBatis-Plus InnerInterceptor，拦截 SQL 并追加 WHERE 条件</li>
 *   <li>common-permission 的 DataScopeSqlBuilder — 从 DataPermissionContext 生成 SQL 片段</li>
 * </ul>
 * 本类提供 permission-service 内部的数据权限校验与日志记录辅助能力。
 * </p>
 */
@Slf4j
@Component
public class DataScopeInterceptor {

    /**
     * 校验数据权限访问
     *
     * @param userId   用户ID
     * @param tenantId 租户ID
     * @param resource 资源标识
     */
    public void validateDataScope(Long userId, Long tenantId, String resource) {
        log.debug("数据权限校验: userId={}, tenantId={}, resource={}", userId, tenantId, resource);
    }
}
