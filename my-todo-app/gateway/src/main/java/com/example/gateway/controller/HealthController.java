package com.example.gateway.controller;

import com.example.gateway.service.CircuitBreakerService;
import com.example.gateway.service.HealthCheckService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 网关健康监控控制器（WebFlux 响应式）
 * <p>
 * 提供后端服务健康状态和断路器状态查询接口。
 * 使用 Mono 返回值，兼容 Spring Cloud Gateway 的响应式编程模型。
 * </p>
 */
@RestController
@RequestMapping("/actuator")
public class HealthController {

    private final HealthCheckService healthCheckService;
    private final CircuitBreakerService circuitBreakerService;

    public HealthController(HealthCheckService healthCheckService,
                            CircuitBreakerService circuitBreakerService) {
        this.healthCheckService = healthCheckService;
        this.circuitBreakerService = circuitBreakerService;
    }

    /**
     * GET /actuator/gateway-health
     * 返回所有后端服务的健康状态和断路器状态
     */
    @GetMapping(value = "/gateway-health", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<Map<String, Object>> gatewayHealth() {
        return Mono.fromCallable(() -> {
                    // 获取健康检查状态（同步方法，包装为 Mono）
                    List<HealthCheckService.ServiceHealth> services = healthCheckService.getServiceStatuses();
                    // 获取断路器状态（同步方法）
                    List<Map<String, Object>> circuitBreakers = circuitBreakerService.getAllStates();

                    Map<String, Object> data = new HashMap<>();
                    data.put("services", services);
                    data.put("circuitBreakers", circuitBreakers);

                    // 组装外层 ApiResponse 格式
                    Map<String, Object> response = new HashMap<>();
                    response.put("code", 200);
                    response.put("message", "success");
                    response.put("data", data);
                    response.put("timestamp", System.currentTimeMillis());
                    return response;
                })
                .subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * GET /actuator/gateway-status
     * 仅返回断路器状态（轻量版）
     */
    @GetMapping(value = "/gateway-status", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<Map<String, Object>> gatewayStatus() {
        return Mono.fromCallable(() -> {
                    List<Map<String, Object>> circuitBreakers = circuitBreakerService.getAllStates();

                    Map<String, Object> response = new HashMap<>();
                    response.put("code", 200);
                    response.put("message", "success");
                    response.put("data", circuitBreakers);
                    response.put("timestamp", System.currentTimeMillis());
                    return response;
                })
                .subscribeOn(Schedulers.boundedElastic());
    }
}
