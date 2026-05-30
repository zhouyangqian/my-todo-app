package com.example.gateway.service;

import java.util.Map;

/**
 * 降级服务接口
 * <p>
 * 为下游服务提供降级配置管理，当服务不可用时返回预设的降级响应。
 * </p>
 */
public interface DegradationService {

    /**
     * 获取服务的降级响应配置
     *
     * @param serviceId 服务标识
     * @return 降级响应配置，不存在返回 null
     */
    Map<String, Object> getConfiguredFallback(String serviceId);

    /**
     * 更新服务的降级响应配置
     *
     * @param serviceId        服务标识
     * @param fallbackResponse 降级响应体
     */
    void updateFallbackConfig(String serviceId, Map<String, Object> fallbackResponse);

    /**
     * 获取所有服务的降级配置
     *
     * @return 所有降级配置
     */
    Map<String, Map<String, Object>> getAllConfigs();

    /**
     * 删除服务的降级配置
     *
     * @param serviceId 服务标识
     */
    void removeFallbackConfig(String serviceId);
}
