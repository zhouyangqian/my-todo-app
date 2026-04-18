package com.example.common.core.constant;

/**
 * 系统错误码常量类
 * <p>
 * 统一管理系统中所有业务错误码，按模块分段编号：
 * <ul>
 *   <li>通用错误码：1000-1999</li>
 *   <li>认证错误码：2000-2999</li>
 *   <li>用户错误码：3000-3999</li>
 *   <li>权限错误码：4000-4999</li>
 *   <li>租户错误码：5000-5999</li>
 * </ul>
 * </p>
 * <p>该类为工具常量类，不允许实例化。</p>
 */
public final class ErrorCodes {

    /**
     * 私有构造函数，防止外部实例化
     */
    private ErrorCodes() {}

    // ==================== 通用错误码（1000-1999）====================

    /** 请求成功 */
    public static final int SUCCESS = 200;
    public static final int BAD_REQUEST = 400;
    public static final int UNAUTHORIZED = 401;
    public static final int FORBIDDEN = 403;
    public static final int NOT_FOUND = 404;
    public static final int INTERNAL_ERROR = 500;

    // Auth errors (2000-2999)
    public static final int AUTH_INVALID_CREDENTIALS = 2001;
    public static final int AUTH_TOKEN_EXPIRED = 2002;
    public static final int AUTH_TOKEN_INVALID = 2003;
    public static final int AUTH_ACCOUNT_LOCKED = 2004;
    public static final int AUTH_ACCOUNT_DISABLED = 2005;
    public static final int AUTH_CAPTCHA_ERROR = 2006;
    public static final int AUTH_PASSWORD_EXPIRED = 2007;
    public static final int AUTH_PASSWORD_WEAK = 2008;
    public static final int AUTH_REFRESH_TOKEN_INVALID = 2009;
    public static final int AUTH_LOGIN_LIMIT_EXCEEDED = 2010;

    // User errors (3000-3999)
    public static final int USER_NOT_FOUND = 3001;
    public static final int USER_ALREADY_EXISTS = 3002;
    public static final int USER_EMAIL_EXISTS = 3003;
    public static final int USER_PHONE_EXISTS = 3004;

    // Permission errors (4000-4999)
    public static final int PERMISSION_DENIED = 4001;
    public static final int ROLE_NOT_FOUND = 4002;
    public static final int ROLE_ALREADY_EXISTS = 4003;

    // Tenant errors (5000-5999)
    public static final int TENANT_NOT_FOUND = 5001;
    public static final int TENANT_DISABLED = 5002;
    public static final int TENANT_EXPIRED = 5003;
}
