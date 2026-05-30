package com.example.gateway.handler;

import com.example.gateway.service.MetricsCollector;
import com.example.gateway.service.MonitorService;
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
 * 监控指标端点
 * <p>
 * 提供 /api/gateway/metrics、/api/gateway/dashboard、
 * /api/gateway/slow-apis、/api/gateway/error-apis 四个端点。
 * 使用 WebFlux 函数式端点（RouterFunction）。
 * </p>
 */
@Slf4j
@Component
public class MonitorHandler {

    private final MetricsCollector metricsCollector;
    private final MonitorService monitorService;

    public MonitorHandler(MetricsCollector metricsCollector, MonitorService monitorService) {
        this.metricsCollector = metricsCollector;
        this.monitorService = monitorService;
    }

    /**
     * 注册监控路由
     */
    @Bean
    public RouterFunction<ServerResponse> monitorRoutes() {
        return route(GET("/api/gateway/metrics"), this::handleMetrics)
                .andRoute(GET("/api/gateway/dashboard"), this::handleDashboard)
                .andRoute(GET("/api/gateway/slow-apis"), this::handleSlowApis)
                .andRoute(GET("/api/gateway/error-apis"), this::handleErrorApis);
    }

    /**
     * 获取所有API指标
     */
    private Mono<ServerResponse> handleMetrics(org.springframework.web.reactive.function.server.ServerRequest request) {
        try {
            return ServerResponse.ok()
                    .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                    .bodyValue(Map.of(
                            "code", 200,
                            "message", "success",
                            "data", Map.of(
                                    "metrics", metricsCollector.getMetrics(),
                                    "summary", metricsCollector.getSummary()
                            ),
                            "timestamp", System.currentTimeMillis()
                    ));
        } catch (Exception e) {
            log.error("获取指标数据失败", e);
            return ServerResponse.ok()
                    .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                    .bodyValue(Map.of(
                            "code", 500,
                            "message", "获取指标数据失败",
                            "data", null,
                            "timestamp", System.currentTimeMillis()
                    ));
        }
    }

    /**
     * 获取监控仪表盘数据
     */
    private Mono<ServerResponse> handleDashboard(org.springframework.web.reactive.function.server.ServerRequest request) {
        try {
            Map<String, Object> dashboard = monitorService.getDashboard();
            return ServerResponse.ok()
                    .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                    .bodyValue(Map.of(
                            "code", 200,
                            "message", "success",
                            "data", dashboard,
                            "timestamp", System.currentTimeMillis()
                    ));
        } catch (Exception e) {
            log.error("获取仪表盘数据失败", e);
            return ServerResponse.ok()
                    .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                    .bodyValue(Map.of(
                            "code", 500,
                            "message", "获取仪表盘数据失败",
                            "data", null,
                            "timestamp", System.currentTimeMillis()
                    ));
        }
    }

    /**
     * 获取最慢API
     */
    private Mono<ServerResponse> handleSlowApis(org.springframework.web.reactive.function.server.ServerRequest request) {
        try {
            int count = Integer.parseInt(request.queryParam("count").orElse("10"));
            return ServerResponse.ok()
                    .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                    .bodyValue(Map.of(
                            "code", 200,
                            "message", "success",
                            "data", metricsCollector.getTopSlowApis(count),
                            "timestamp", System.currentTimeMillis()
                    ));
        } catch (Exception e) {
            log.error("获取最慢API失败", e);
            return ServerResponse.ok()
                    .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                    .bodyValue(Map.of(
                            "code", 500,
                            "message", "获取最慢API失败",
                            "data", null,
                            "timestamp", System.currentTimeMillis()
                    ));
        }
    }

    /**
     * 获取错误率最高API
     */
    private Mono<ServerResponse> handleErrorApis(org.springframework.web.reactive.function.server.ServerRequest request) {
        try {
            int count = Integer.parseInt(request.queryParam("count").orElse("10"));
            return ServerResponse.ok()
                    .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                    .bodyValue(Map.of(
                            "code", 200,
                            "message", "success",
                            "data", metricsCollector.getTopErrorApis(count),
                            "timestamp", System.currentTimeMillis()
                    ));
        } catch (Exception e) {
            log.error("获取错误API失败", e);
            return ServerResponse.ok()
                    .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                    .bodyValue(Map.of(
                            "code", 500,
                            "message", "获取错误API失败",
                            "data", null,
                            "timestamp", System.currentTimeMillis()
                    ));
        }
    }
}
