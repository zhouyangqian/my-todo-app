package com.example.auth.service;

import com.example.auth.entity.TokenBlacklist;
import com.example.auth.mapper.TokenBlacklistMapper;
import com.example.common.security.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * Token黑名单服务实现
 * <p>
 * 使用Redis存储被拉黑的Token，实现快速校验。
 * 数据库同步记录用于审计。
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TokenBlacklistServiceImpl implements TokenBlacklistService {

    private final StringRedisTemplate stringRedisTemplate;
    private final TokenBlacklistMapper tokenBlacklistMapper;
    private final JwtTokenProvider jwtTokenProvider;

    /** Redis Key前缀 */
    private static final String REDIS_KEY_PREFIX = "token:blacklist:";

    @Override
    public void blacklistToken(String token, Long userId, String reason) {
        String tokenPrefix = token.substring(0, Math.min(32, token.length()));

        // 计算Token剩余有效期（秒）
        Date expiryDate = jwtTokenProvider.getExpiration(token);
        long remainingSeconds = (expiryDate.getTime() - System.currentTimeMillis()) / 1000;
        if (remainingSeconds <= 0) {
            // Token已过期，无需拉黑
            return;
        }

        // 写入Redis，TTL为Token剩余有效期
        stringRedisTemplate.opsForValue().set(
                REDIS_KEY_PREFIX + tokenPrefix,
                reason,
                remainingSeconds,
                TimeUnit.SECONDS
        );

        // 写入数据库（审计记录）
        TokenBlacklist record = new TokenBlacklist();
        record.setTokenPrefix(tokenPrefix);
        record.setUserId(userId);
        record.setTenantId(0L); // 由调用方补充，此处为简化
        record.setExpiryTime(LocalDateTime.now().plusSeconds(remainingSeconds));
        record.setReason(reason);
        tokenBlacklistMapper.insert(record);

        log.info("Token已加入黑名单: userId={}, reason={}", userId, reason);
    }

    @Override
    public boolean isTokenBlacklisted(String tokenPrefix) {
        if (tokenPrefix == null) {
            return false;
        }
        return Boolean.TRUE.equals(stringRedisTemplate.hasKey(REDIS_KEY_PREFIX + tokenPrefix));
    }

    @Override
    public void blacklistAllUserTokens(Long userId) {
        String key = "token:blacklist:user:" + userId;
        // 设置30分钟有效期（与账号锁定时间一致）
        stringRedisTemplate.opsForValue().set(key, "FORCED", 30, TimeUnit.MINUTES);
        log.info("已拉黑用户所有Token: userId={}", userId);
    }

    @Override
    public boolean isUserTokensBlacklisted(Long userId) {
        String key = "token:blacklist:user:" + userId;
        return Boolean.TRUE.equals(stringRedisTemplate.hasKey(key));
    }
}
