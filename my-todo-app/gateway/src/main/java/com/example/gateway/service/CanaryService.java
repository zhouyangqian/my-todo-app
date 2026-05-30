package com.example.gateway.service;

import com.example.gateway.config.CanaryConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 金丝雀发布服务
 * <p>
 * 根据配置的金丝雀策略，判断请求是否应路由到金丝雀版本。
 * 支持三种匹配策略（优先级从高到低）：
 * <ol>
 *   <li>用户ID列表 — 指定用户始终路由到金丝雀版本</li>
 *   <li>请求头匹配 — 包含指定请求头值则路由</li>
 *   <li>流量百分比 — 基于 userId 哈希值进行流量分配</li>
 * </ol>
 * </p>
 */
@Slf4j
@Component
public class CanaryService {

    private final CanaryConfig canaryConfig;

    public CanaryService(CanaryConfig canaryConfig) {
        this.canaryConfig = canaryConfig;
    }

    /**
     * 判断请求是否应路由到金丝雀版本
     *
     * @param serviceId 服务ID
     * @param userId    用户ID（从 X-User-Id 请求头获取）
     * @param header    请求中匹配的header值（用于 matchHeader 匹配）
     * @return true 表示路由到金丝雀版本
     */
    public boolean shouldRouteToCanary(String serviceId, String userId, String header) {
        if (!canaryConfig.isEnabled()) {
            return false;
        }

        CanaryConfig.RouteCanary strategy = findStrategy(serviceId);
        if (strategy == null) {
            return false;
        }

        // 策略1：用户ID列表匹配
        if (strategy.getMatchUserIds() != null && userId != null
                && strategy.getMatchUserIds().contains(userId)) {
            log.debug("金丝雀路由(用户ID匹配): serviceId={}, userId={}", serviceId, userId);
            return true;
        }

        // 策略2：请求头匹配
        if (strategy.getMatchHeader() != null && header != null
                && !header.isEmpty()) {
            String expectedValue = strategy.getMatchHeaderValue();
            if (expectedValue == null || header.equals(expectedValue)) {
                log.debug("金丝雀路由(请求头匹配): serviceId={}, header={}", serviceId, header);
                return true;
            }
        }

        // 策略3：流量百分比
        if (strategy.getTrafficPercentage() > 0 && userId != null) {
            int hash = Math.abs(userId.hashCode()) % 100;
            if (hash < strategy.getTrafficPercentage()) {
                log.debug("金丝雀路由(流量百分比): serviceId={}, userId={}, hash={}", serviceId, userId, hash);
                return true;
            }
        }

        return false;
    }

    /**
     * 获取所有金丝雀策略
     *
     * @return 策略列表
     */
    public List<CanaryConfig.RouteCanary> getStrategies() {
        return canaryConfig.getRoutes();
    }

    /**
     * 查找指定服务的金丝雀策略
     */
    private CanaryConfig.RouteCanary findStrategy(String serviceId) {
        return canaryConfig.getRoutes().stream()
                .filter(r -> r.getServiceId().equals(serviceId))
                .findFirst()
                .orElse(null);
    }
}
