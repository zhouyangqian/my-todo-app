package com.example.gateway.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

/**
 * 金丝雀发布配置
 * <p>
 * 通过 canary 前缀在 application.yml 中配置金丝雀路由策略。
 * 支持按流量百分比、请求头匹配、用户ID列表进行灰度路由。
 * </p>
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "canary")
public class CanaryConfig {

    /** 是否启用金丝雀发布 */
    private boolean enabled = false;

    /** 金丝雀路由策略列表 */
    private List<RouteCanary> routes = new ArrayList<>();

    /**
     * 单个服务的金丝雀路由配置
     */
    @Data
    public static class RouteCanary {
        /** 目标服务ID（对应 Spring Cloud Gateway route id） */
        private String serviceId;
        /** 金丝雀版本标识 */
        private String targetVersion;
        /** 流量分配百分比（0-100） */
        private int trafficPercentage = 0;
        /** 匹配的请求头名称 */
        private String matchHeader;
        /** 匹配的请求头值 */
        private String matchHeaderValue;
        /** 指定用户ID列表，这些用户始终路由到金丝雀版本 */
        private List<String> matchUserIds;
    }
}
