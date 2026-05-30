package com.example.gateway.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 监控仪表盘服务
 * <p>
 * 聚合 MetricsCollector、HealthCheckService、CircuitBreakerService 的数据，
 * 提供监控仪表盘和告警检查能力。
 * </p>
 */
@Slf4j
@Component
public class MonitorService {

    private final MetricsCollector metricsCollector;
    private final HealthCheckService healthCheckService;
    private final CircuitBreakerService circuitBreakerService;

    public MonitorService(MetricsCollector metricsCollector,
                          HealthCheckService healthCheckService,
                          CircuitBreakerService circuitBreakerService) {
        this.metricsCollector = metricsCollector;
        this.healthCheckService = healthCheckService;
        this.circuitBreakerService = circuitBreakerService;
    }

    /**
     * 获取监控仪表盘数据
     *
     * @return 仪表盘数据
     */
    public Map<String, Object> getDashboard() {
        Map<String, Object> dashboard = new HashMap<>();

        // 服务健康状态
        dashboard.put("services", healthCheckService.getServiceStatuses());

        // 断路器状态
        dashboard.put("circuitBreakers", circuitBreakerService.getAllStates());

        // API 汇总指标
        dashboard.put("metricsSummary", metricsCollector.getSummary());

        // 最慢API（Top 5）
        dashboard.put("topSlowApis", metricsCollector.getTopSlowApis(5));

        // 错误率最高API（Top 5）
        dashboard.put("topErrorApis", metricsCollector.getTopErrorApis(5));

        return dashboard;
    }

    /**
     * 获取告警规则定义
     *
     * @return 告警规则列表
     */
    public List<Map<String, Object>> getAlertRules() {
        List<Map<String, Object>> rules = new ArrayList<>();

        rules.add(Map.of(
                "name", "高错误率",
                "description", "API错误率超过10%",
                "threshold", "10%",
                "metric", "errorRate"
        ));

        rules.add(Map.of(
                "name", "慢响应",
                "description", "平均响应时间超过2000ms",
                "threshold", "2000ms",
                "metric", "avgResponseTime"
        ));

        rules.add(Map.of(
                "name", "服务不可用",
                "description", "服务健康检查状态为DOWN",
                "threshold", "DOWN",
                "metric", "serviceHealth"
        ));

        rules.add(Map.of(
                "name", "断路器打开",
                "description", "服务断路器处于OPEN状态",
                "threshold", "OPEN",
                "metric", "circuitBreakerState"
        ));

        return rules;
    }

    /**
     * 检查是否触发告警
     *
     * @return 告警列表（空列表表示无告警）
     */
    public List<Map<String, Object>> checkAlerts() {
        List<Map<String, Object>> alerts = new ArrayList<>();

        // 检查服务健康状态
        for (HealthCheckService.ServiceHealth health : healthCheckService.getServiceStatuses()) {
            if ("DOWN".equals(health.getStatus())) {
                alerts.add(Map.of(
                        "level", "CRITICAL",
                        "rule", "服务不可用",
                        "message", String.format("服务 %s 状态为 DOWN: %s",
                                health.getServiceName(),
                                health.getErrorMessage() != null ? health.getErrorMessage() : "未知错误"),
                        "timestamp", System.currentTimeMillis()
                ));
            }
        }

        // 检查断路器状态
        for (Map<String, Object> cb : circuitBreakerService.getAllStates()) {
            if ("OPEN".equals(cb.get("state"))) {
                alerts.add(Map.of(
                        "level", "WARNING",
                        "rule", "断路器打开",
                        "message", String.format("服务 %s 断路器处于OPEN状态", cb.get("serviceId")),
                        "timestamp", System.currentTimeMillis()
                ));
            }
        }

        // 检查高错误率API
        for (MetricsCollector.ApiMetrics metrics : metricsCollector.getTopErrorApis(10)) {
            if (metrics.getTotalRequests() > 0) {
                double errorRate = (double) metrics.getErrorCount() / metrics.getTotalRequests() * 100;
                if (errorRate > 10 && metrics.getTotalRequests() > 5) {
                    alerts.add(Map.of(
                            "level", "WARNING",
                            "rule", "高错误率",
                            "message", String.format("API %s 错误率 %.1f%% (总请求%d, 错误%d)",
                                    metrics.getApiPath(), errorRate,
                                    metrics.getTotalRequests(), metrics.getErrorCount()),
                            "timestamp", System.currentTimeMillis()
                    ));
                }
            }
        }

        if (!alerts.isEmpty()) {
            log.warn("当前告警数量: {}", alerts.size());
        }

        return alerts;
    }
}
