package com.example.user.cache;

import com.example.user.dto.UserVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;

/**
 * 用户缓存组件
 * <p>
 * 使用 Redis 缓存用户信息，减少数据库查询压力。
 * 缓存 key: user:info:{tenantId}:{userId}
 * TTL: 30 分钟
 * 在用户创建/更新/删除时清除缓存。
 * </p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UserCache {

    private static final String KEY_PREFIX = "user:info:";
    private static final Duration TTL = Duration.ofMinutes(30);

    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * 构建缓存 key
     */
    private String buildKey(Long tenantId, Long userId) {
        return KEY_PREFIX + tenantId + ":" + userId;
    }

    /**
     * 获取缓存的用户信息
     *
     * @param tenantId 租户ID
     * @param userId   用户ID
     * @return 缓存的 UserVO，不存在返回 null
     */
    public UserVO get(Long tenantId, Long userId) {
        if (tenantId == null || userId == null) {
            return null;
        }
        try {
            String key = buildKey(tenantId, userId);
            Object value = redisTemplate.opsForValue().get(key);
            if (value instanceof UserVO) {
                return (UserVO) value;
            }
            return null;
        } catch (Exception e) {
            log.warn("获取用户缓存失败, tenantId={}, userId={}: {}", tenantId, userId, e.getMessage());
            return null;
        }
    }

    /**
     * 缓存用户信息
     *
     * @param tenantId 租户ID
     * @param userVO   用户信息
     */
    public void put(Long tenantId, UserVO userVO) {
        if (tenantId == null || userVO == null || userVO.getId() == null) {
            return;
        }
        try {
            String key = buildKey(tenantId, userVO.getId());
            redisTemplate.opsForValue().set(key, userVO, TTL);
            log.debug("缓存用户信息: tenantId={}, userId={}", tenantId, userVO.getId());
        } catch (Exception e) {
            log.warn("缓存用户信息失败, tenantId={}, userId={}: {}", tenantId, userVO.getId(), e.getMessage());
        }
    }

    /**
     * 删除用户缓存
     *
     * @param tenantId 租户ID
     * @param userId   用户ID
     */
    public void evict(Long tenantId, Long userId) {
        if (tenantId == null || userId == null) {
            return;
        }
        try {
            String key = buildKey(tenantId, userId);
            redisTemplate.delete(key);
            log.debug("清除用户缓存: tenantId={}, userId={}", tenantId, userId);
        } catch (Exception e) {
            log.warn("清除用户缓存失败, tenantId={}, userId={}: {}", tenantId, userId, e.getMessage());
        }
    }

    /**
     * 批量删除用户缓存
     *
     * @param tenantId 租户ID
     * @param userIds  用户ID列表
     */
    public void evictBatch(Long tenantId, List<Long> userIds) {
        if (tenantId == null || userIds == null || userIds.isEmpty()) {
            return;
        }
        try {
            List<String> keys = userIds.stream()
                    .map(userId -> buildKey(tenantId, userId))
                    .toList();
            redisTemplate.delete(keys);
            log.debug("批量清除用户缓存: tenantId={}, userIds={}", tenantId, userIds);
        } catch (Exception e) {
            log.warn("批量清除用户缓存失败, tenantId={}: {}", tenantId, e.getMessage());
        }
    }
}
