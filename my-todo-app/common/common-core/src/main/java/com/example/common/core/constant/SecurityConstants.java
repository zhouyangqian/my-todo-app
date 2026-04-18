package com.example.common.core.constant;

/**
 * 安全相关常量类
 * <p>
 * 统一管理 HTTP 请求头名称、Cookie 名称、密码策略等安全相关常量。
 * 该类不允许实例化。
 * </p>
 */
public final class SecurityConstants {

    /** 私有构造函数，防止外部实例化 */
    private SecurityConstants() {}

    /** Authorization 请求头名称 */
    public static final String AUTHORIZATION_HEADER = "Authorization";

    /** Bearer 令牌前缀 */
    public static final String BEARER_PREFIX = "Bearer ";

    /** 租户 ID 请求头，用于多租户场景 */
    public static final String TENANT_HEADER = "X-Tenant-Id";

    /** 用户 ID 请求头，由网关解析 Token 后设置 */
    public static final String USER_ID_HEADER = "X-User-Id";

    /** 用户名请求头，由网关解析 Token 后设置 */
    public static final String USERNAME_HEADER = "X-Username";

    /** 用户角色请求头，由网关解析 Token 后设置 */
    public static final String ROLES_HEADER = "X-Roles";

    /** 请求追踪 ID 请求头，用于链路追踪 */
    public static final String REQUEST_ID_HEADER = "X-Request-Id";

    /** 访问令牌 Cookie 名称 */
    public static final String ACCESS_TOKEN_COOKIE = "access_token";

    /** 刷新令牌 Cookie 名称 */
    public static final String REFRESH_TOKEN_COOKIE = "refresh_token";

    /** 默认初始密码（用户首次创建时使用） */
    public static final String DEFAULT_PASSWORD = "123456";

    /** 密码正则：至少8位，包含大小写字母和数字 */
    public static final String PASSWORD_PATTERN = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,}$";

    /** 用户名正则：字母开头，3-20位字母数字下划线 */
    public static final String USERNAME_PATTERN = "^[a-zA-Z][a-zA-Z0-9_]{2,19}$";
}
