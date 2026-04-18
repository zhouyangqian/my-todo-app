package com.example.common.security.jwt;

/**
 * JWT 令牌无效异常
 * <p>
 * 当 JWT 令牌格式错误、签名不匹配或无法解析时抛出此异常。
 * </p>
 */
public class JwtTokenInvalidException extends RuntimeException {

    public JwtTokenInvalidException(String message) {
        super(message);
    }

    public JwtTokenInvalidException(String message, Throwable cause) {
        super(message, cause);
    }
}
