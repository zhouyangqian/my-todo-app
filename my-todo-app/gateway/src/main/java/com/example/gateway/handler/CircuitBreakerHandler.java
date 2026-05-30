package com.example.gateway.handler;

import com.example.gateway.service.CircuitBreakerService;
import com.example.gateway.service.DegradationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.springframework.web.reactive.function.server.RequestPredicates.*;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

/**
 * 熔断器与降级配置管理端点
 * <p>
 * 提供以下接口：
 * <ul>
 *   <li>GET /api/gateway/circuit-breakers/status - 获取所有服务熔断状态</li>
 *   <li>POST /api/gateway/circuit-breakers/{serviceId}/reset - 重置指定服务的熔断状态</li>
 *   <li>GET /api/gateway/degradation/config - 获取降级配置</li>
 *   <li>PUT /api/gateway/degradation/config - 更新降级配置</li>
 * </ul>
 * </p>
 */
@Slf4j
@Component
public class CircuitBreakerHandler {

    private final CircuitBreakerService circuitBreakerService;
    private final DegradationService degradationService;

    public CircuitBreakerHandler(CircuitBreakerService circuitBreakerService,
                                 DegradationService degradationService) {
        this.circuitBreakerService = circuitBreakerService;
        this.degradationService = degradationService;
    }

    @Bean
    public RouterFunction<ServerResponse> circuitBreakerRoutes() {
        return route(GET("/api/gateway/circuit-breakers/status"), this::handleCircuitBreakerStatus)
                .andRoute(POST("/api/gateway/circuit-breakers/{serviceId}/reset"), this::handleResetCircuitBreaker)
                .andRoute(GET("/api/gateway/degradation/config"), this::handleGetDegradationConfig)
                .andRoute(PUT("/api/gateway/degradation/config"), this::handleUpdateDegradationConfig);
    }

    /**
     * 获取所有服务熔断状态
     */
    private Mono<ServerResponse> handleCircuitBreakerStatus(ServerRequest request) {
        try {
            return ServerResponse.ok()
                    .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                    .bodyValue(Map.of(
                            "code", 200,
                            "message", "success",
                            "data", circuitBreakerService.getAllStates(),
                            "timestamp", System.currentTimeMillis()
                    ));
        } catch (Exception e) {
            log.error("获取熔断状态失败", e);
            return ServerResponse.ok()
                    .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                    .bodyValue(Map.of(
                            "code", 500,
                            "message", "获取熔断状态失败",
                            "data", null,
                            "timestamp", System.currentTimeMillis()
                    ));
        }
    }

    /**
     * 重置指定服务的熔断状态
     */
    private Mono<ServerResponse> handleResetCircuitBreaker(ServerRequest request) {
        try {
            String serviceId = request.pathVariable("serviceId");
            circuitBreakerService.reset(serviceId);
            log.info("熔断器已重置: serviceId={}", serviceId);
            return ServerResponse.ok()
                    .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                    .bodyValue(Map.of(
                            "code", 200,
                            "message", "熔断器已重置",
                            "data", Map.of("serviceId", serviceId),
                            "timestamp", System.currentTimeMillis()
                    ));
        } catch (Exception e) {
            log.error("重置熔断器失败", e);
            return ServerResponse.ok()
                    .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                    .bodyValue(Map.of(
                            "code", 500,
                            "message", "重置熔断器失败: " + e.getMessage(),
                            "data", null,
                            "timestamp", System.currentTimeMillis()
                    ));
        }
    }

    /**
     * 获取降级配置
     */
    private Mono<ServerResponse> handleGetDegradationConfig(ServerRequest request) {
        try {
            Map<String, Map<String, Object>> configs = degradationService.getAllConfigs();
            return ServerResponse.ok()
                    .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                    .bodyValue(Map.of(
                            "code", 200,
                            "message", "success",
                            "data", configs,
                            "timestamp", System.currentTimeMillis()
                    ));
        } catch (Exception e) {
            log.error("获取降级配置失败", e);
            return ServerResponse.ok()
                    .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                    .bodyValue(Map.of(
                            "code", 500,
                            "message", "获取降级配置失败",
                            "data", null,
                            "timestamp", System.currentTimeMillis()
                    ));
        }
    }

    /**
     * 更新降级配置
     */
    @SuppressWarnings("unchecked")
    private Mono<ServerResponse> handleUpdateDegradationConfig(ServerRequest request) {
        return request.bodyToMono(Map.class)
                .flatMap(body -> {
                    try {
                        String serviceId = (String) body.get("serviceId");
                        Map<String, Object> fallbackResponse = (Map<String, Object>) body.get("fallbackResponse");

                        if (serviceId == null || serviceId.isEmpty()) {
                            return ServerResponse.ok()
                                    .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                                    .bodyValue(Map.of(
                                            "code", 400,
                                            "message", "serviceId 不能为空",
                                            "data", null,
                                            "timestamp", System.currentTimeMillis()
                                    ));
                        }

                        degradationService.updateFallbackConfig(serviceId, fallbackResponse != null ? fallbackResponse : new LinkedHashMap<>());
                        log.info("降级配置已更新: serviceId={}", serviceId);

                        return ServerResponse.ok()
                                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                                .bodyValue(Map.of(
                                        "code", 200,
                                        "message", "降级配置已更新",
                                        "data", Map.of("serviceId", serviceId),
                                        "timestamp", System.currentTimeMillis()
                                ));
                    } catch (Exception e) {
                        log.error("更新降级配置失败", e);
                        return ServerResponse.ok()
                                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                                .bodyValue(Map.of(
                                        "code", 500,
                                        "message", "更新降级配置失败: " + e.getMessage(),
                                        "data", null,
                                        "timestamp", System.currentTimeMillis()
                                ));
                    }
                });
    }
}
