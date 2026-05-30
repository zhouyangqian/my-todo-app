package com.example.gateway.filter;

import com.example.gateway.config.RateLimitConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.InetSocketAddress;
import java.util.Collections;
import java.util.List;

/**
 * 多维度限流全局过滤器
 * <p>
 * 基于 Redis 滑动窗口算法，支持三个维度的限流：
 * <ul>
 *   <li>IP 维度 — 根据 X-Forwarded-For / X-Real-IP / remoteAddress</li>
 *   <li>用户维度 — 根据 X-User-Id 请求头</li>
 *   <li>租户维度 — 根据 X-Tenant-Id 请求头</li>
 * </ul>
 * 每个维度独立计数，任一维度超限即返回 429。
 * 仅在 Redis 可用时生效。
 * </p>
 */
@Slf4j
@Component
@ConditionalOnBean(ReactiveRedisConnectionFactory.class)
public class RateLimitFilter implements GlobalFilter, Ordered {

    private final ReactiveRedisTemplate<String, String> redisTemplate;
    private final RateLimitConfig rateLimitConfig;

    /** Lua 脚本：滑动窗口计数限流 */
    private static final String LIMIT_SCRIPT =
            "local key = KEYS[1] " +
            "local limit = tonumber(ARGV[1]) " +
            "local period = tonumber(ARGV[2]) " +
            "local now = tonumber(ARGV[3]) " +
            "redis.call('zremrangebyscore', key, 0, now - period * 1000) " +
            "local count = redis.call('zcard', key) " +
            "if count < limit then " +
            "  redis.call('zadd', key, now, now .. '-' .. math.random(1, 1000000)) " +
            "  redis.call('expire', key, period) " +
            "  return 1 " +
            "end " +
            "return 0";

    public RateLimitFilter(ReactiveRedisTemplate<String, String> redisTemplate,
                           RateLimitConfig rateLimitConfig) {
        this.redisTemplate = redisTemplate;
        this.rateLimitConfig = rateLimitConfig;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        if (!rateLimitConfig.isEnabled()) {
            return chain.filter(exchange);
        }

        String ip = getClientIp(exchange);
        String path = exchange.getRequest().getPath().value();

        // 按维度依次检查限流，任一维度超限即拦截
        return checkDimension("ip", ip, rateLimitConfig.getIp())
                .then(checkDimension("user",
                        exchange.getRequest().getHeaders().getFirst("X-User-Id"),
                        rateLimitConfig.getUser()))
                .then(checkDimension("tenant",
                        exchange.getRequest().getHeaders().getFirst("X-Tenant-Id"),
                        rateLimitConfig.getTenant()))
                .then(chain.filter(exchange))
                .onErrorResume(RateLimitExceededException.class, e -> {
                    log.warn("请求限流: dimension={}, key={}, path={}",
                            e.getDimension(), e.getKey(), path);
                    exchange.getResponse().setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
                    return exchange.getResponse().setComplete();
                })
                .onErrorResume(e -> {
                    if (!(e instanceof RateLimitExceededException)) {
                        log.error("限流检查异常，放行请求: {}", e.getMessage());
                    }
                    return chain.filter(exchange);
                });
    }

    /**
     * 检查单个维度的限流
     *
     * @param dimension 维度名称（ip/user/tenant）
     * @param key       限流标识（IP地址、用户ID、租户ID）
     * @param config    该维度的限流配置
     */
    private Mono<Void> checkDimension(String dimension, String key,
                                       RateLimitConfig.DimensionConfig config) {
        // limit 为 0 或 key 为空表示不限制该维度
        if (config.getLimit() <= 0 || key == null || key.isEmpty()) {
            return Mono.empty();
        }

        String redisKey = "rate_limit:" + dimension + ":" + key + ":" + config.getPeriod();
        RedisScript<Long> script = RedisScript.of(LIMIT_SCRIPT, Long.class);
        long now = System.currentTimeMillis();

        return redisTemplate.execute(script,
                        Collections.singletonList(redisKey),
                        List.of(String.valueOf(config.getLimit()),
                                String.valueOf(config.getPeriod()),
                                String.valueOf(now)))
                .next()
                .flatMap(allowed -> {
                    if (allowed != null && allowed == 1L) {
                        return Mono.empty();
                    }
                    return Mono.error(new RateLimitExceededException(dimension, key));
                });
    }

    private String getClientIp(ServerWebExchange exchange) {
        String ip = exchange.getRequest().getHeaders().getFirst("X-Forwarded-For");
        if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
            return ip.split(",")[0].trim();
        }
        String realIp = exchange.getRequest().getHeaders().getFirst("X-Real-IP");
        if (realIp != null && !realIp.isEmpty() && !"unknown".equalsIgnoreCase(realIp)) {
            return realIp.trim();
        }
        InetSocketAddress remoteAddress = exchange.getRequest().getRemoteAddress();
        return remoteAddress != null ? remoteAddress.getAddress().getHostAddress() : "unknown";
    }

    @Override
    public int getOrder() {
        return -150;
    }

    /**
     * 限流超限异常（内部信号，不对外暴露）
     */
    private static class RateLimitExceededException extends RuntimeException {
        private final String dimension;
        private final String key;

        RateLimitExceededException(String dimension, String key) {
            super("Rate limit exceeded: " + dimension + "=" + key);
            this.dimension = dimension;
            this.key = key;
        }

        String getDimension() { return dimension; }
        String getKey() { return key; }
    }
}
