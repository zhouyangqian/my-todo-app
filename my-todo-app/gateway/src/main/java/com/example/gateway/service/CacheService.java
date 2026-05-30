package com.example.gateway.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Duration;

/**
 * 响应缓存服务
 * <p>
 * 使用 Redis 存储网关层响应缓存，支持按租户+路径+查询参数构建缓存key。
 * 默认TTL 60秒，支持手动清除。
 * 仅在 Redis 可用时生效。
 * </p>
 */
@Slf4j
@Component
public class CacheService {

    private static final String CACHE_KEY_PREFIX = "gateway:cache:";
    private static final long DEFAULT_TTL_SECONDS = 60;

    private final ReactiveRedisTemplate<String, String> redisTemplate;

    /** 缓存命中次数 */
    private volatile long hitCount = 0;
    /** 缓存未命中次数 */
    private volatile long missCount = 0;

    public CacheService(ReactiveRedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * 获取缓存
     *
     * @param key 缓存key
     * @return 缓存值，不存在返回空Mono
     */
    public Mono<String> get(String key) {
        return redisTemplate.opsForValue().get(key)
                .doOnNext(value -> {
                    hitCount++;
                    log.debug("缓存命中: {}", key);
                })
                .switchIfEmpty(Mono.fromRunnable(() -> {
                    missCount++;
                    log.debug("缓存未命中: {}", key);
                }));
    }

    /**
     * 存入缓存
     *
     * @param key        缓存key
     * @param value      缓存值
     * @param ttlSeconds TTL秒数
     */
    public Mono<Boolean> put(String key, String value, long ttlSeconds) {
        return redisTemplate.opsForValue().set(key, value, Duration.ofSeconds(ttlSeconds))
                .doOnSuccess(saved -> log.debug("缓存写入: {}, TTL={}s", key, ttlSeconds));
    }

    /**
     * 删除指定缓存
     *
     * @param key 缓存key
     */
    public Mono<Boolean> evict(String key) {
        return redisTemplate.delete(key)
                .map(count -> count > 0);
    }

    /**
     * 按前缀批量删除缓存
     *
     * @param pattern key模式（如 gateway:cache:*）
     */
    public Mono<Long> evictByPattern(String pattern) {
        return redisTemplate.keys(pattern)
                .collectList()
                .flatMap(keys -> {
                    if (keys.isEmpty()) {
                        return Mono.just(0L);
                    }
                    return redisTemplate.delete(keys.toArray(new String[0]));
                });
    }

    /**
     * 构建缓存key
     * <p>
     * 格式: gateway:cache:{tenantId}:{requestPath}:{queryString}
     * </p>
     *
     * @param request HTTP请求
     * @return 缓存key
     */
    public String buildCacheKey(ServerHttpRequest request) {
        String tenantId = request.getHeaders().getFirst("X-Tenant-Id");
        if (tenantId == null) {
            tenantId = "anonymous";
        }
        String path = request.getPath().value();
        String query = request.getURI().getQuery();
        if (query == null) {
            query = "";
        }
        return CACHE_KEY_PREFIX + tenantId + ":" + path + ":" + query;
    }

    /**
     * 获取缓存统计信息
     *
     * @return 统计Map
     */
    public java.util.Map<String, Object> getStats() {
        long total = hitCount + missCount;
        double hitRate = total > 0 ? (double) hitCount / total : 0.0;
        return java.util.Map.of(
                "hitCount", hitCount,
                "missCount", missCount,
                "totalRequests", total,
                "hitRate", String.format("%.2f%%", hitRate * 100)
        );
    }

    /**
     * 重置统计计数
     */
    public void resetStats() {
        hitCount = 0;
        missCount = 0;
    }

    /**
     * 获取缓存key前缀
     */
    public static String getCacheKeyPrefix() {
        return CACHE_KEY_PREFIX;
    }

    /**
     * 获取默认TTL秒数
     */
    public static long getDefaultTtlSeconds() {
        return DEFAULT_TTL_SECONDS;
    }
}
