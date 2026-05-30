package com.example.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.ConcurrentHashMap;

/**
 * API Key 认证过滤器
 * <p>
 * 对 /api/open/** 路径进行 API Key 认证。
 * 从请求头 X-API-Key 获取 key，验证其是否有效。
 * 优先使用 Redis 存储，Redis 不可用时回退到内存缓存。
 * 无效 key 返回 401 Unauthorized。
 * </p>
 */
@Slf4j
@Component
public class ApiKeyAuthFilter implements GlobalFilter, Ordered {

    private static final String OPEN_PATH_PREFIX = "/api/open/";
    private static final String API_KEY_HEADER = "X-API-Key";
    private static final String API_KEY_REDIS_PREFIX = "gateway:api-key:";

    private final ReactiveRedisTemplate<String, String> redisTemplate;

    /** 内存缓存，Redis 不可用时使用 */
    private final ConcurrentHashMap<String, String> memoryApiKeyStore = new ConcurrentHashMap<>();

    @Value("${gateway.api-key.enabled:true}")
    private boolean enabled;

    /** 预配置的默认 API Key（用于开发/测试） */
    @Value("${gateway.api-key.default-key:}")
    private String defaultApiKey;

    public ApiKeyAuthFilter(ReactiveRedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
        // 初始化内存缓存中的默认 key
    }

    @jakarta.annotation.PostConstruct
    public void init() {
        if (defaultApiKey != null && !defaultApiKey.isEmpty()) {
            memoryApiKeyStore.put(defaultApiKey, "default");
            log.info("默认 API Key 已加载");
        }
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        if (!enabled) {
            return chain.filter(exchange);
        }

        String path = exchange.getRequest().getPath().value();

        if (!path.startsWith(OPEN_PATH_PREFIX)) {
            return chain.filter(exchange);
        }

        String apiKey = exchange.getRequest().getHeaders().getFirst(API_KEY_HEADER);

        if (apiKey == null || apiKey.isEmpty()) {
            log.warn("API Key 缺失: path={}", path);
            return unauthorized(exchange, "缺少 X-API-Key 请求头");
        }

        return validateApiKey(apiKey)
                .flatMap(valid -> {
                    if (valid) {
                        log.debug("API Key 验证通过: path={}", path);
                        return chain.filter(exchange);
                    }
                    log.warn("API Key 无效: path={}", path);
                    return unauthorized(exchange, "无效的 API Key");
                });
    }

    /**
     * 验证 API Key
     * <p>
     * 先查内存缓存，再查 Redis。
     * </p>
     *
     * @param apiKey API Key 值
     * @return true 表示有效
     */
    private Mono<Boolean> validateApiKey(String apiKey) {
        // 先查内存缓存
        if (memoryApiKeyStore.containsKey(apiKey)) {
            return Mono.just(true);
        }

        // 再查 Redis
        return redisTemplate.opsForValue().get(API_KEY_REDIS_PREFIX + apiKey)
                .map(value -> true)
                .defaultIfEmpty(false)
                .onErrorResume(e -> {
                    log.warn("Redis 查询失败，使用内存缓存: {}", e.getMessage());
                    return Mono.just(memoryApiKeyStore.containsKey(apiKey));
                });
    }

    /**
     * 返回 401 未授权响应
     */
    private Mono<Void> unauthorized(ServerWebExchange exchange, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        String body = String.format(
                "{\"code\":401,\"message\":\"%s\",\"data\":null,\"timestamp\":%d}",
                message, System.currentTimeMillis()
        );

        DataBuffer buffer = response.bufferFactory().wrap(body.getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Mono.just(buffer));
    }

    @Override
    public int getOrder() {
        // 在 TokenValidationFilter 之前执行，order 更小优先级更高
        return -110;
    }
}
