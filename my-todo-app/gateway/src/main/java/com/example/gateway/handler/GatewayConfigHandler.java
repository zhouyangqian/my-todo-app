package com.example.gateway.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.*;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

/**
 * 网关配置查看端点
 * 提供路由配置、限流配置、断路器配置的只读查看
 */
@Slf4j
@Component
public class GatewayConfigHandler {

    private final RouteLocator routeLocator;
    private final ObjectMapper objectMapper;

    @Value("${rate-limit.enabled:true}")
    private boolean rateLimitEnabled;

    @Value("${rate-limit.ip.limit:100}")
    private int ipLimit;

    @Value("${rate-limit.ip.period:60}")
    private int ipPeriod;

    @Value("${rate-limit.user.limit:200}")
    private int userLimit;

    @Value("${rate-limit.user.period:60}")
    private int userPeriod;

    @Value("${rate-limit.tenant.limit:500}")
    private int tenantLimit;

    @Value("${rate-limit.tenant.period:60}")
    private int tenantPeriod;

    @Value("${circuit-breaker.enabled:true}")
    private boolean circuitBreakerEnabled;

    @Value("${circuit-breaker.failure-threshold:5}")
    private int failureThreshold;

    @Value("${circuit-breaker.open-duration:60000}")
    private long openDuration;

    public GatewayConfigHandler(RouteLocator routeLocator, ObjectMapper objectMapper) {
        this.routeLocator = routeLocator;
        this.objectMapper = objectMapper;
    }

    @Bean
    public RouterFunction<ServerResponse> gatewayConfigRoutes() {
        return route(GET("/api/gateway/routes"), this::handleRoutes)
                .andRoute(GET("/api/gateway/rate-limits"), this::handleRateLimits)
                .andRoute(GET("/api/gateway/circuit-breakers"), this::handleCircuitBreakers)
                .andRoute(GET("/api/gateway/refresh"), this::handleRefresh);
    }

    private Mono<ServerResponse> handleRoutes(org.springframework.web.reactive.function.server.ServerRequest request) {
        try {
            List<Map<String, String>> routes = new ArrayList<>();
            routes.add(Map.of("id", "auth-service", "path", "/api/auth/**", "uri", "lb://auth-service"));
            routes.add(Map.of("id", "user-service", "path", "/api/users/**", "uri", "lb://user-service"));
            routes.add(Map.of("id", "permission-service", "path", "/api/permissions/**,/api/roles/**,/api/system/**", "uri", "lb://permission-service"));
            routes.add(Map.of("id", "inventory-service", "path", "/api/erp/inventory/**", "uri", "lb://inventory-service"));
            routes.add(Map.of("id", "erp-service", "path", "/api/erp/**", "uri", "lb://erp-service"));
            routes.add(Map.of("id", "finance-service", "path", "/api/finance/**", "uri", "lb://finance-service"));
            routes.add(Map.of("id", "dict-service", "path", "/api/dict/**,/api/config/**,/api/api-market/**,/api/tracing/**,/api/third-party/**,/api/packages/**,/api/activities/**,/api/codegen/**,/api/error-doc/**", "uri", "lb://dict-service"));

            Map<String, Object> result = Map.of("code", 200, "message", "success", "data", routes);
            return ServerResponse.ok()
                    .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                    .bodyValue(result);
        } catch (Exception e) {
            log.error("获取路由配置失败", e);
            return ServerResponse.ok()
                    .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                    .bodyValue(Map.of("code", 500, "message", "获取路由配置失败", "data", null));
        }
    }

    private Mono<ServerResponse> handleRateLimits(org.springframework.web.reactive.function.server.ServerRequest request) {
        Map<String, Object> config = new LinkedHashMap<>();
        config.put("enabled", rateLimitEnabled);
        Map<String, Object> ip = Map.of("limit", ipLimit, "period", ipPeriod, "description", "每个IP限制");
        Map<String, Object> user = Map.of("limit", userLimit, "period", userPeriod, "description", "每个用户限制");
        Map<String, Object> tenant = Map.of("limit", tenantLimit, "period", tenantPeriod, "description", "每个租户限制");
        config.put("ip", ip);
        config.put("user", user);
        config.put("tenant", tenant);

        Map<String, Object> result = Map.of("code", 200, "message", "success", "data", config);
        return ServerResponse.ok()
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .bodyValue(result);
    }

    private Mono<ServerResponse> handleCircuitBreakers(org.springframework.web.reactive.function.server.ServerRequest request) {
        Map<String, Object> config = new LinkedHashMap<>();
        config.put("enabled", circuitBreakerEnabled);
        config.put("failureThreshold", failureThreshold);
        config.put("openDuration", openDuration);
        config.put("description", "断路器配置");

        Map<String, Object> result = Map.of("code", 200, "message", "success", "data", config);
        return ServerResponse.ok()
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .bodyValue(result);
    }

    private Mono<ServerResponse> handleRefresh(org.springframework.web.reactive.function.server.ServerRequest request) {
        log.info("收到路由刷新请求");
        Map<String, Object> result = Map.of("code", 200, "message", "路由刷新请求已接收，配置将自动更新", "data", null);
        return ServerResponse.ok()
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .bodyValue(result);
    }
}
