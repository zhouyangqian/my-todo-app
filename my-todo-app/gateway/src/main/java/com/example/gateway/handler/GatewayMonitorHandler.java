package com.example.gateway.handler;

import com.example.gateway.service.CircuitBreakerService;
import com.example.gateway.service.HealthCheckService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.Map;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

/**
 * 网关监控端点
 * <p>
 * 提供 /gateway/service-statuses 和 /gateway/circuit-breaker-status
 * 两个端点，供前端监控页面调用。
 * 使用 WebFlux 函数式端点（RouterFunction），不使用 @RestController。
 * </p>
 */
@Slf4j
@Component
public class GatewayMonitorHandler {

    private final HealthCheckService healthCheckService;
    private final CircuitBreakerService circuitBreakerService;
    private final ObjectMapper objectMapper;

    public GatewayMonitorHandler(HealthCheckService healthCheckService,
                                  CircuitBreakerService circuitBreakerService,
                                  ObjectMapper objectMapper) {
        this.healthCheckService = healthCheckService;
        this.circuitBreakerService = circuitBreakerService;
        this.objectMapper = objectMapper;
    }

    /**
     * 注册网关监控路由
     */
    @Bean
    public RouterFunction<ServerResponse> gatewayMonitorRoutes() {
        return route(GET("/api/gateway/service-statuses"), this::handleServiceStatuses)
                .andRoute(GET("/api/gateway/circuit-breaker-status"), this::handleCircuitBreakerStatus);
    }

    /**
     * 获取所有服务健康状态
     */
    private Mono<ServerResponse> handleServiceStatuses(org.springframework.web.reactive.function.server.ServerRequest request) {
        try {
            Map<String, Object> result = Map.of(
                    "code", 200,
                    "message", "success",
                    "data", healthCheckService.getServiceStatuses(),
                    "timestamp", System.currentTimeMillis()
            );
            return ServerResponse.ok()
                    .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                    .bodyValue(result);
        } catch (Exception e) {
            log.error("获取服务状态失败", e);
            return ServerResponse.ok()
                    .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                    .bodyValue(Map.of(
                            "code", 500,
                            "message", "获取服务状态失败",
                            "data", null,
                            "timestamp", System.currentTimeMillis()
                    ));
        }
    }

    /**
     * 获取所有断路器状态
     */
    private Mono<ServerResponse> handleCircuitBreakerStatus(org.springframework.web.reactive.function.server.ServerRequest request) {
        try {
            Map<String, Object> result = Map.of(
                    "code", 200,
                    "message", "success",
                    "data", circuitBreakerService.getAllStates(),
                    "timestamp", System.currentTimeMillis()
            );
            return ServerResponse.ok()
                    .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                    .bodyValue(result);
        } catch (Exception e) {
            log.error("获取断路器状态失败", e);
            return ServerResponse.ok()
                    .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                    .bodyValue(Map.of(
                            "code", 500,
                            "message", "获取断路器状态失败",
                            "data", null,
                            "timestamp", System.currentTimeMillis()
                    ));
        }
    }
}
