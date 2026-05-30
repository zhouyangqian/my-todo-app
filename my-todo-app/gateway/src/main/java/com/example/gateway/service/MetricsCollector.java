package com.example.gateway.service;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * API 指标收集器
 * <p>
 * 使用 ConcurrentHashMap 在内存中存储各 API 的调用指标。
 * 记录请求数、成功数、错误数、响应时间等，支持按服务查询和 Top-N 排序。
 * </p>
 */
@Slf4j
@Component
public class MetricsCollector {

    private final ConcurrentHashMap<String, ApiMetrics> metricsMap = new ConcurrentHashMap<>();

    /**
     * API 指标数据
     */
    @Data
    public static class ApiMetrics {
        /** API 路径 */
        private String apiPath;
        /** HTTP 方法 */
        private String method;
        /** 总请求数 */
        private long totalRequests;
        /** 成功请求数 */
        private long successCount;
        /** 错误请求数 */
        private long errorCount;
        /** 总响应时间（用于计算平均值） */
        private long totalResponseTime;
        /** 最大响应时间 */
        private long maxResponseTime;
        /** 最后访问时间 */
        private String lastAccessTime;
    }

    /**
     * 记录一次请求的指标
     *
     * @param apiPath      API路径
     * @param method       HTTP方法
     * @param responseTime 响应时间（毫秒）
     * @param statusCode   HTTP状态码
     */
    public void record(String apiPath, String method, long responseTime, int statusCode) {
        String key = method + ":" + apiPath;
        ApiMetrics metrics = metricsMap.computeIfAbsent(key, k -> {
            ApiMetrics m = new ApiMetrics();
            m.setApiPath(apiPath);
            m.setMethod(method);
            return m;
        });

        synchronized (metrics) {
            metrics.setTotalRequests(metrics.getTotalRequests() + 1);
            metrics.setTotalResponseTime(metrics.getTotalResponseTime() + responseTime);
            if (responseTime > metrics.getMaxResponseTime()) {
                metrics.setMaxResponseTime(responseTime);
            }
            if (statusCode >= 200 && statusCode < 400) {
                metrics.setSuccessCount(metrics.getSuccessCount() + 1);
            } else {
                metrics.setErrorCount(metrics.getErrorCount() + 1);
            }
            metrics.setLastAccessTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        }
    }

    /**
     * 获取所有API指标
     *
     * @return 指标列表
     */
    public List<ApiMetrics> getMetrics() {
        return new ArrayList<>(metricsMap.values());
    }

    /**
     * 按服务ID前缀获取指标
     *
     * @param serviceId 服务ID（如 erp-service）
     * @return 该服务相关的指标列表
     */
    public List<ApiMetrics> getMetricsForService(String serviceId) {
        return metricsMap.values().stream()
                .filter(m -> m.getApiPath().contains(serviceId))
                .collect(Collectors.toList());
    }

    /**
     * 获取最慢的N个API（按平均响应时间降序）
     *
     * @param count 数量
     * @return 最慢API列表
     */
    public List<ApiMetrics> getTopSlowApis(int count) {
        return metricsMap.values().stream()
                .filter(m -> m.getTotalRequests() > 0)
                .sorted(Comparator.comparingLong(
                        (ApiMetrics m) -> m.getTotalResponseTime() / m.getTotalRequests()
                ).reversed())
                .limit(count)
                .collect(Collectors.toList());
    }

    /**
     * 获取错误率最高的N个API
     *
     * @param count 数量
     * @return 错误率最高API列表
     */
    public List<ApiMetrics> getTopErrorApis(int count) {
        return metricsMap.values().stream()
                .filter(m -> m.getTotalRequests() > 0)
                .sorted(Comparator.comparingDouble(
                        (ApiMetrics m) -> (double) m.getErrorCount() / m.getTotalRequests()
                ).reversed())
                .limit(count)
                .collect(Collectors.toList());
    }

    /**
     * 获取汇总统计
     *
     * @return 汇总数据
     */
    public java.util.Map<String, Object> getSummary() {
        long totalRequests = metricsMap.values().stream()
                .mapToLong(ApiMetrics::getTotalRequests).sum();
        long totalErrors = metricsMap.values().stream()
                .mapToLong(ApiMetrics::getErrorCount).sum();
        long totalResponseTime = metricsMap.values().stream()
                .mapToLong(ApiMetrics::getTotalResponseTime).sum();
        long activeApis = metricsMap.size();

        double avgResponseTime = totalRequests > 0 ? (double) totalResponseTime / totalRequests : 0;
        double errorRate = totalRequests > 0 ? (double) totalErrors / totalRequests * 100 : 0;

        return java.util.Map.of(
                "totalRequests", totalRequests,
                "totalErrors", totalErrors,
                "avgResponseTime", String.format("%.1f", avgResponseTime),
                "errorRate", String.format("%.2f%%", errorRate),
                "activeApis", activeApis
        );
    }
}
