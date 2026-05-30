package com.example.gateway.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 网关熔断配置实体
 * Gateway 使用 WebFlux 响应式栈，无 MyBatis-Plus 依赖
 * 此实体用于配置管理的 JSON 序列化/反序列化
 */
@Data
public class GatewayCircuitBreaker implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    /** 服务ID */
    private String serviceId;

    /** 失败阈值 */
    private Integer failureThreshold;

    /** 冷却时间(秒) */
    private Integer cooldownSeconds;

    /** 半开状态最大调用数 */
    private Integer halfOpenMaxCalls;

    /** 是否启用 */
    private Boolean enabled;

    /** 租户ID */
    private Long tenantId;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime updatedAt;
}
