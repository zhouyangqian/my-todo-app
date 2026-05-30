package com.example.gateway.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 网关路由配置实体
 * Gateway 使用 WebFlux 响应式栈，无 MyBatis-Plus 依赖
 * 此实体用于配置管理的 JSON 序列化/反序列化
 */
@Data
public class GatewayRoute implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    /** 路由ID */
    private String routeId;

    /** 目标URI */
    private String uri;

    /** 断言配置JSON */
    private String predicates;

    /** 过滤器配置JSON */
    private String filters;

    /** 优先级 */
    private Integer priority;

    /** 是否启用 */
    private Boolean enabled;

    /** 租户ID */
    private Long tenantId;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime updatedAt;
}
