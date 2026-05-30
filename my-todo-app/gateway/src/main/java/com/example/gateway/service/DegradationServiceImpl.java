package com.example.gateway.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 降级服务实现
 * <p>
 * 使用内存 ConcurrentHashMap 存储各服务的降级响应配置。
 * 提供默认降级响应（503 Service Unavailable），支持自定义降级响应体。
 * </p>
 */
@Slf4j
@Service
public class DegradationServiceImpl implements DegradationService {

    private final ConcurrentHashMap<String, Map<String, Object>> fallbackConfigMap = new ConcurrentHashMap<>();

    @Override
    public Map<String, Object> getConfiguredFallback(String serviceId) {
        return fallbackConfigMap.get(serviceId);
    }

    @Override
    public void updateFallbackConfig(String serviceId, Map<String, Object> fallbackResponse) {
        if (serviceId == null || serviceId.isEmpty()) {
            throw new IllegalArgumentException("serviceId 不能为空");
        }
        fallbackConfigMap.put(serviceId, fallbackResponse);
        log.info("更新降级配置: serviceId={}, config={}", serviceId, fallbackResponse);
    }

    @Override
    public Map<String, Map<String, Object>> getAllConfigs() {
        return new LinkedHashMap<>(fallbackConfigMap);
    }

    @Override
    public void removeFallbackConfig(String serviceId) {
        fallbackConfigMap.remove(serviceId);
        log.info("删除降级配置: serviceId={}", serviceId);
    }

    /**
     * 获取服务的降级响应。
     * 如果有自定义配置则使用自定义配置，否则返回默认 503 降级响应。
     *
     * @param serviceId 服务标识
     * @return 降级响应体
     */
    public Map<String, Object> getFallbackResponse(String serviceId) {
        Map<String, Object> configured = getConfiguredFallback(serviceId);
        if (configured != null) {
            return configured;
        }
        return buildDefaultFallback(serviceId);
    }

    /**
     * 构建默认降级响应
     *
     * @param serviceId 服务标识
     * @return 默认降级响应体
     */
    private Map<String, Object> buildDefaultFallback(String serviceId) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("code", 503);
        response.put("message", "服务[" + serviceId + "]暂时不可用，请稍后重试");
        response.put("data", null);
        response.put("timestamp", System.currentTimeMillis());
        return response;
    }
}
