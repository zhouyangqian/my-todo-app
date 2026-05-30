package com.example.gateway.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 多维度限流配置
 * <p>
 * 支持按 IP、用户、租户三个维度配置不同的限流参数。
 * 通过 rate-limit 前缀在 application.yml 中配置。
 * </p>
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "rate-limit")
public class RateLimitConfig {

    /** 是否启用限流 */
    private boolean enabled = true;

    /** IP 维度限流配置 */
    private DimensionConfig ip = new DimensionConfig(100, 60);

    /** 用户维度限流配置 */
    private DimensionConfig user = new DimensionConfig(200, 60);

    /** 租户维度限流配置 */
    private DimensionConfig tenant = new DimensionConfig(500, 60);

    /**
     * 单个维度的限流配置
     */
    @Data
    public static class DimensionConfig {
        /** 时间窗口内允许的最大请求数 */
        private int limit;

        /** 时间窗口（秒） */
        private int period;

        public DimensionConfig() {
        }

        public DimensionConfig(int limit, int period) {
            this.limit = limit;
            this.period = period;
        }
    }
}
