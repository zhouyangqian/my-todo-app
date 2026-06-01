package com.example.finance.cache;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReportCache {

    private final RedisTemplate<String, Object> redisTemplate;
    private static final String KEY_PREFIX = "finance:report:";
    private static final long DEFAULT_TTL_HOURS = 1;
    private static final long LOCKED_TTL_HOURS = 24;

    public void put(Long tenantId, String reportType, String period, Object data) {
        String key = buildKey(tenantId, reportType, period);
        redisTemplate.opsForValue().set(key, data, DEFAULT_TTL_HOURS, TimeUnit.HOURS);
        log.debug("缓存报表数据: {}", key);
    }

    public void putLocked(Long tenantId, String reportType, String period, Object data) {
        String key = buildKey(tenantId, reportType, period);
        redisTemplate.opsForValue().set(key, data, LOCKED_TTL_HOURS, TimeUnit.HOURS);
        log.debug("缓存已锁定报表数据: {}", key);
    }

    @SuppressWarnings("unchecked")
    public <T> T get(Long tenantId, String reportType, String period, Class<T> clazz) {
        String key = buildKey(tenantId, reportType, period);
        Object data = redisTemplate.opsForValue().get(key);
        return data != null ? (T) data : null;
    }

    public void evict(Long tenantId, String reportType, String period) {
        String key = buildKey(tenantId, reportType, period);
        redisTemplate.delete(key);
        log.debug("清除报表缓存: {}", key);
    }

    public void evictByTenant(Long tenantId) {
        String pattern = KEY_PREFIX + tenantId + ":*";
        redisTemplate.delete(redisTemplate.keys(pattern));
        log.debug("清除租户所有报表缓存: tenantId={}", tenantId);
    }

    private String buildKey(Long tenantId, String reportType, String period) {
        return KEY_PREFIX + tenantId + ":" + reportType + ":" + period;
    }
}
