package com.example.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Instant;

/**
 * 外部接口集成过滤器
 * <p>
 * 对标记为外部的 API 路由进行特殊处理：
 * <ul>
 *   <li>请求/响应日志记录</li>
 *   <li>超时控制（通过响应式 timeout）</li>
 *   <li>错误重试（可配置次数）</li>
 * </ul>
 * 外部路由通过路径前缀 /api/external/** 识别。
 * </p>
 */
@Slf4j
@Component
public class ExternalApiFilter implements GlobalFilter, Ordered {

    /** 外部 API 路径前缀 */
    private static final String EXTERNAL_PATH_PREFIX = "/api/external/";

    /** 默认超时时间（毫秒） */
    private static final long DEFAULT_TIMEOUT_MS = 10_000;

    /** 默认重试次数 */
    private static final int DEFAULT_RETRY_COUNT = 2;

    /** 重试的 HTTP 状态码阈值（5xx） */
    private static final int RETRY_STATUS_THRESHOLD = 500;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getPath().value();

        if (!path.startsWith(EXTERNAL_PATH_PREFIX)) {
            return chain.filter(exchange);
        }

        String method = exchange.getRequest().getMethod().name();
        String clientIp = getClientIp(exchange);

        // 记录请求日志
        log.info("[ExternalAPI] 请求开始: method={}, path={}, clientIp={}, timestamp={}",
                method, path, clientIp, Instant.now());

        long startTime = System.currentTimeMillis();

        return executeWithRetry(exchange, chain, DEFAULT_RETRY_COUNT)
                .timeout(java.time.Duration.ofMillis(DEFAULT_TIMEOUT_MS))
                .doOnSuccess(v -> {
                    long elapsed = System.currentTimeMillis() - startTime;
                    log.info("[ExternalAPI] 请求完成: method={}, path={}, elapsed={}ms", method, path, elapsed);
                })
                .doOnError(e -> {
                    long elapsed = System.currentTimeMillis() - startTime;
                    log.error("[ExternalAPI] 请求异常: method={}, path={}, elapsed={}ms, error={}",
                            method, path, elapsed, e.getMessage());
                });
    }

    /**
     * 带重试的请求执行
     *
     * @param exchange   请求上下文
     * @param chain      过滤器链
     * @param retryCount 重试次数
     * @return Mono<Void>
     */
    private Mono<Void> executeWithRetry(ServerWebExchange exchange, GatewayFilterChain chain, int retryCount) {
        return chain.filter(exchange)
                .onErrorResume(Exception.class, e -> {
                    if (retryCount > 0) {
                        log.warn("[ExternalAPI] 请求失败，剩余重试{}次: path={}, error={}",
                                retryCount, exchange.getRequest().getPath().value(), e.getMessage());
                        return executeWithRetry(exchange, chain, retryCount - 1);
                    }
                    log.error("[ExternalAPI] 重试耗尽: path={}", exchange.getRequest().getPath().value(), e);
                    return Mono.error(e);
                });
    }

    private String getClientIp(ServerWebExchange exchange) {
        ServerHttpRequest request = exchange.getRequest();
        String ip = request.getHeaders().getFirst("X-Forwarded-For");
        if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
            return ip.split(",")[0].trim();
        }
        String realIp = request.getHeaders().getFirst("X-Real-IP");
        if (realIp != null && !realIp.isEmpty() && !"unknown".equalsIgnoreCase(realIp)) {
            return realIp.trim();
        }
        return request.getRemoteAddress() != null
                ? request.getRemoteAddress().getAddress().getHostAddress()
                : "unknown";
    }

    @Override
    public int getOrder() {
        return -85;
    }
}
