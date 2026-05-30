package com.example.gateway.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 网关限流配置实体
 * Gateway 使用 WebFlux 响应式栈，无 MyBatis-Plus 依赖
 * 此实体用于配置管理的 JSON 序列化/反序列化
 */
@Data
public class GatewayRateLimit implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    /** 路由ID(为空表示全局) */
    private String routeId;

    /** 路径匹配模式 */
    private String pathPattern;

    /** 最大请求数 */
    private Integer maxRequests;

    /** 时间窗口(秒) */
    private Integer timeWindowSeconds;

    /** 限流维度: IP/USER/TENANT */
    private String dimension;

    /** 是否启用 */
    private Boolean enabled;

    /** 租户ID */
    private Long tenantId;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime updatedAt;
}
