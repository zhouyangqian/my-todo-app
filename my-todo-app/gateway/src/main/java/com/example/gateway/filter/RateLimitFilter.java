package com.example.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.Collections;

/**
 * 限流全局过滤器
 * <p>
 * 基于 Redis 令牌桶算法实现的 IP 限流。
 * 默认配置：100 请求/60秒 每个 IP。
 * 超出限制返回 429 Too Many Requests。
 * </p>
 */
@Slf4j
@Component
public class RateLimitFilter implements GlobalFilter, Ordered {

    private final ReactiveRedisTemplate<String, String> redisTemplate;

    @Value("${rate-limit.enabled:true}")
    private boolean enabled;

    @Value("${rate-limit.default-limit:100}")
    private int defaultLimit;

    @Value("${rate-limit.default-period:60}")
    private int defaultPeriod;

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

    public RateLimitFilter(ReactiveRedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        if (!enabled) {
            return chain.filter(exchange);
        }

        String ip = getClientIp(exchange);
        String key = "rate-limit:" + ip;

        RedisScript<Long> script = RedisScript.of(LIMIT_SCRIPT, Long.class);
        long now = System.currentTimeMillis();

        return redisTemplate.execute(script,
                        Collections.singletonList(key),
                        String.valueOf(defaultLimit),
                        String.valueOf(defaultPeriod),
                        String.valueOf(now))
                .next()
                .flatMap(allowed -> {
                    if (allowed != null && allowed == 1L) {
                        return chain.filter(exchange);
                    }
                    log.warn("请求限流: IP={}, path={}", ip, exchange.getRequest().getPath());
                    exchange.getResponse().setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
                    return exchange.getResponse().setComplete();
                })
                .onErrorResume(e -> {
                    log.error("限流检查异常，放行请求: {}", e.getMessage());
                    return chain.filter(exchange);
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
}
