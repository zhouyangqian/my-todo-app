package com.example.gateway.handler;

import com.example.gateway.service.CacheService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.Map;

import static org.springframework.web.reactive.function.server.RequestPredicates.DELETE;
import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

/**
 * 缓存管理端点
 * <p>
 * 提供 /api/gateway/cache/stats、/api/gateway/cache/evict、/api/gateway/cache/evict/{key}
 * 三个端点，供前端缓存管理页面调用。
 * 使用 WebFlux 函数式端点（RouterFunction）。
 * </p>
 */
@Slf4j
@Component
public class CacheHandler {

    private final CacheService cacheService;

    public CacheHandler(CacheService cacheService) {
        this.cacheService = cacheService;
    }

    /**
     * 注册缓存管理路由
     */
    @Bean
    public RouterFunction<ServerResponse> cacheRoutes() {
        return route(GET("/api/gateway/cache/stats"), this::handleCacheStats)
                .andRoute(DELETE("/api/gateway/cache/evict"), this::handleEvictAll)
                .andRoute(DELETE("/api/gateway/cache/evict/{key}"), this::handleEvictByKey);
    }

    /**
     * 获取缓存统计信息
     */
    private Mono<ServerResponse> handleCacheStats(org.springframework.web.reactive.function.server.ServerRequest request) {
        try {
            Map<String, Object> stats = cacheService.getStats();
            return ServerResponse.ok()
                    .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                    .bodyValue(Map.of(
                            "code", 200,
                            "message", "success",
                            "data", stats,
                            "timestamp", System.currentTimeMillis()
                    ));
        } catch (Exception e) {
            log.error("获取缓存统计失败", e);
            return ServerResponse.ok()
                    .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                    .bodyValue(Map.of(
                            "code", 500,
                            "message", "获取缓存统计失败",
                            "data", null,
                            "timestamp", System.currentTimeMillis()
                    ));
        }
    }

    /**
     * 清除所有网关缓存
     */
    private Mono<ServerResponse> handleEvictAll(org.springframework.web.reactive.function.server.ServerRequest request) {
        return cacheService.evictByPattern(CacheService.getCacheKeyPrefix() + "*")
                .map(count -> {
                    log.info("清除所有缓存，共{}条", count);
                    return Map.of(
                            "code", 200,
                            "message", "success",
                            "data", Map.of("evictedCount", count),
                            "timestamp", System.currentTimeMillis()
                    );
                })
                .onErrorResume(e -> {
                    log.error("清除缓存失败", e);
                    return Mono.just(Map.of(
                            "code", 500,
                            "message", "清除缓存失败: " + e.getMessage(),
                            "data", null,
                            "timestamp", System.currentTimeMillis()
                    ));
                })
                .flatMap(result -> ServerResponse.ok()
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .bodyValue(result));
    }

    /**
     * 清除指定缓存
     */
    private Mono<ServerResponse> handleEvictByKey(org.springframework.web.reactive.function.server.ServerRequest request) {
        String key = request.pathVariable("key");
        // 实际key需要加前缀
        String fullKey = CacheService.getCacheKeyPrefix() + key;

        return cacheService.evict(fullKey)
                .map(success -> {
                    log.info("清除缓存: {}, 结果: {}", fullKey, success);
                    return Map.of(
                            "code", 200,
                            "message", success ? "缓存已清除" : "缓存不存在",
                            "data", Map.of("key", fullKey, "success", success),
                            "timestamp", System.currentTimeMillis()
                    );
                })
                .onErrorResume(e -> {
                    log.error("清除缓存失败: {}", fullKey, e);
                    return Mono.just(Map.of(
                            "code", 500,
                            "message", "清除缓存失败: " + e.getMessage(),
                            "data", null,
                            "timestamp", System.currentTimeMillis()
                    ));
                })
                .flatMap(result -> ServerResponse.ok()
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .bodyValue(result));
    }
}
