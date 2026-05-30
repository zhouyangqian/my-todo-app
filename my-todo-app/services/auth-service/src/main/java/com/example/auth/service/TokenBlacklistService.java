package com.example.auth.service;

/**
 * Token黑名单服务接口
 * <p>
 * 使用Redis存储被拉黑的Token，实现快速校验。
 * </p>
 */
public interface TokenBlacklistService {

    /**
     * 将Token加入黑名单
     *
     * @param token  完整JWT Token
     * @param userId 用户ID
     * @param reason 拉黑原因（LOGOUT/REFRESH/FORCED）
     */
    void blacklistToken(String token, Long userId, String reason);

    /**
     * 检查Token是否在黑名单中
     *
     * @param tokenPrefix Token前缀（前32字符）
     * @return true-已被拉黑
     */
    boolean isTokenBlacklisted(String tokenPrefix);

    /**
     * 拉黑用户所有Token（强制重新验证）
     *
     * @param userId 用户ID
     */
    void blacklistAllUserTokens(Long userId);

    /**
     * 检查用户的Token是否需要重新验证
     *
     * @param userId 用户ID
     * @return true-需要重新验证
     */
    boolean isUserTokensBlacklisted(Long userId);
}
