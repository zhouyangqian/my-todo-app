package com.example.gateway.filter;

import com.example.gateway.config.CanaryConfig;
import com.example.gateway.service.CanaryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * 金丝雀发布全局过滤器
 * <p>
 * 检查目标服务是否有金丝雀策略，如果有且匹配条件满足，
 * 添加 X-Canary-Version 和 X-Canary 请求头，让下游路由使用。
 * </p>
 */
@Slf4j
@Component
public class CanaryFilter implements GlobalFilter, Ordered {

    private final CanaryService canaryService;
    private final CanaryConfig canaryConfig;

    public CanaryFilter(CanaryService canaryService, CanaryConfig canaryConfig) {
        this.canaryService = canaryService;
        this.canaryConfig = canaryConfig;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        if (!canaryConfig.isEnabled()) {
            return chain.filter(exchange);
        }

        Route route = exchange.getAttribute("org.springframework.cloud.gateway.support.ServerWebExchangeUtils.gatewayRoute");
        if (route == null) {
            return chain.filter(exchange);
        }

        String serviceId = route.getId();
        String userId = exchange.getRequest().getHeaders().getFirst("X-User-Id");

        // 检查是否有匹配的金丝雀策略中定义的 header
        String headerValue = null;
        CanaryConfig.RouteCanary strategy = canaryConfig.getRoutes().stream()
                .filter(r -> r.getServiceId().equals(serviceId))
                .findFirst()
                .orElse(null);

        if (strategy != null && strategy.getMatchHeader() != null) {
            headerValue = exchange.getRequest().getHeaders().getFirst(strategy.getMatchHeader());
        }

        boolean isCanary = canaryService.shouldRouteToCanary(serviceId, userId, headerValue);

        if (isCanary) {
            String targetVersion = strategy != null ? strategy.getTargetVersion() : "canary";
            log.info("金丝雀路由: serviceId={}, targetVersion={}, userId={}", serviceId, targetVersion, userId);

            ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                    .header("X-Canary-Version", targetVersion)
                    .header("X-Canary", "true")
                    .build();

            return chain.filter(exchange.mutate().request(mutatedRequest).build());
        }

        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return -70;
    }
}
