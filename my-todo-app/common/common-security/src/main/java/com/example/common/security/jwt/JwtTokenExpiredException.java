package com.example.common.security.jwt;

/**
 * JWT 令牌过期异常
 * <p>
 * 当 JWT 令牌已超过有效期时抛出此异常。
 * </p>
 */
public class JwtTokenExpiredException extends RuntimeException {

    public JwtTokenExpiredException(String message) {
        super(message);
    }

    public JwtTokenExpiredException(String message, Throwable cause) {
        super(message, cause);
    }
}
