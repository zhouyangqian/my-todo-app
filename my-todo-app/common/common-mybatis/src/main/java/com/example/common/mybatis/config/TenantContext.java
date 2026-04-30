package com.example.common.mybatis.config;

/**
 * 租户上下文
 * <p>
 * 使用 ThreadLocal 存储当前线程的租户ID，方便在任意位置获取。
 * </p>
 * <p>
 * 使用场景：
 * <ul>
 *   <li>在拦截器中从 JWT Token 解析出租户ID并设置到上下文</li>
 *   <li>在多租户拦截器中从上下文获取租户ID</li>
 *   <li>在业务代码中获取当前租户ID进行权限判断</li>
 * </ul>
 * </p>
 */
public class TenantContext {

    private static final ThreadLocal<Long> TENANT_ID = new ThreadLocal<>();

    /**
     * 设置当前租户ID
     *
     * @param tenantId 租户ID
     */
    public static void setTenantId(Long tenantId) {
        TENANT_ID.set(tenantId);
    }

    /**
     * 获取当前租户ID
     *
     * @return 租户ID，如果未设置则返回 null
     */
    public static Long getTenantId() {
        return TENANT_ID.get();
    }

    /**
     * 清除当前租户ID
     * <p>
     * 在请求结束时调用，避免 ThreadLocal 内存泄漏。
     * </p>
     */
    public static void clear() {
        TENANT_ID.remove();
    }
}
