-- ============================================================
-- Patch 01: 补充缺失的 Gateway 模块 3 张表
-- 来源: gateway/src/main/resources/schema_gateway.sql
-- 问题: sql/my-todo-app-dev.sql 中缺少这 3 张表
-- ============================================================

USE `my-todo-app-dev`;

-- 1. 网关路由配置表
CREATE TABLE IF NOT EXISTS `gateway_route` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `tenant_id` BIGINT NOT NULL DEFAULT 0 COMMENT '租户ID',
    `route_id` VARCHAR(100) NOT NULL COMMENT '路由ID',
    `uri` VARCHAR(255) NOT NULL COMMENT '目标URI',
    `predicates` TEXT COMMENT '断言配置(JSON)',
    `filters` TEXT COMMENT '过滤器配置(JSON)',
    `order_num` INT DEFAULT 0 COMMENT '排序',
    `enabled` TINYINT NOT NULL DEFAULT 1 COMMENT '0=禁用 1=启用',
    `remark` VARCHAR(500) COMMENT '备注',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '软删除',
    `created_by` BIGINT COMMENT '创建人',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_by` BIGINT COMMENT '更新人',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_route_id` (`route_id`),
    KEY `idx_tenant_id` (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='网关路由配置表';

-- 2. 网关限流配置表
CREATE TABLE IF NOT EXISTS `gateway_rate_limit` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `tenant_id` BIGINT NOT NULL DEFAULT 0 COMMENT '租户ID',
    `route_id` VARCHAR(100) NOT NULL COMMENT '路由ID',
    `limit_dimension` VARCHAR(20) NOT NULL DEFAULT 'IP' COMMENT '限流维度(IP/USER/TENANT)',
    `max_requests` INT NOT NULL DEFAULT 100 COMMENT '最大请求数',
    `window_seconds` INT NOT NULL DEFAULT 60 COMMENT '时间窗口(秒)',
    `enabled` TINYINT NOT NULL DEFAULT 1 COMMENT '0=禁用 1=启用',
    `remark` VARCHAR(500) COMMENT '备注',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '软删除',
    `created_by` BIGINT COMMENT '创建人',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_by` BIGINT COMMENT '更新人',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_tenant_route` (`tenant_id`, `route_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='网关限流配置表';

-- 3. 网关熔断配置表
CREATE TABLE IF NOT EXISTS `gateway_circuit_breaker` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `tenant_id` BIGINT NOT NULL DEFAULT 0 COMMENT '租户ID',
    `route_id` VARCHAR(100) NOT NULL COMMENT '路由ID',
    `failure_threshold` INT NOT NULL DEFAULT 5 COMMENT '失败阈值',
    `cooldown_seconds` INT NOT NULL DEFAULT 60 COMMENT '冷却时间(秒)',
    `half_open_max` INT DEFAULT 3 COMMENT '半开状态最大请求数',
    `state` VARCHAR(20) NOT NULL DEFAULT 'CLOSED' COMMENT '状态(CLOSED/OPEN/HALF_OPEN)',
    `enabled` TINYINT NOT NULL DEFAULT 1 COMMENT '0=禁用 1=启用',
    `remark` VARCHAR(500) COMMENT '备注',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '软删除',
    `created_by` BIGINT COMMENT '创建人',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_by` BIGINT COMMENT '更新人',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_tenant_route` (`tenant_id`, `route_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='网关熔断配置表';
