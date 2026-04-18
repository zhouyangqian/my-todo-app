# Data Model: API 网关与集成管理

**Feature**: API 网关与集成管理 (004-gateway)
**Date**: 2026-01-10
**Purpose**: 数据库设计和实体关系

## 概述

本文档定义 API 网关与集成管理模块的数据库表结构、实体关系和验证规则。

## 实体关系图 (ERD)

```
┌─────────────────────┐       ┌─────────────────────┐       ┌─────────────────────┐
│    RouteConfig      │       │   RateLimitConfig   │       │  HealthCheckConfig  │
├─────────────────────┤       ├─────────────────────┤       ├─────────────────────┤
│ id (PK)             │       │ id (PK)             │       │ id (PK)             │
│ path                │       │ dimension           │       │ service_id         │
│ service_id          │       │ resource            │       │ interval           │
│ strip_prefix        │       │ limit               │       │ timeout            │
│ timeout             │       │ window_seconds      │       │ failure_threshold  │
│ retry               │       │ burst               │       │ health_path        │
│ status              │       │ whitelist           │       │ status             │
│ created_at          │       │ created_at          │       │ created_at         │
└─────────────────────┘       └─────────────────────┘       └─────────────────────┘
         │                              │                              │
         │                              │                              │
         ▼                              ▼                              ▼
┌─────────────────────┐       ┌─────────────────────┐       ┌─────────────────────┐
│  ApiCallLog         │       │  RateLimitState     │       │ServiceInstanceState │
├─────────────────────┤       ├─────────────────────┤       ├─────────────────────┤
│ id (PK)             │       │ key (PK)            │       │ id (PK)             │
│ request_id          │       │ current_count       │       │ instance_id        │
│ caller_type         │       │ window_start        │       │ service_id         │
│ caller_id           │       │ is_limited          │       │ address            │
│ api_path            │       └─────────────────────┘       │ status             │
│ method              │                                      │ last_check_time     │
│ response_status     │       ┌─────────────────────┐       │ failure_count      │
│ response_time       │       │   CacheConfig       │       └─────────────────────┘
│ created_at          │       ├─────────────────────┤
└─────────────────────┘       │ id (PK)             │                              │
                              │ api_path            │       ┌─────────────────────┐
                              │ ttl                 │       │ CanaryStrategy      │
                              │ max_size            │       ├─────────────────────┤
                              │ key_generator       │       │ id (PK)             │
                              │ tenant_isolated     │       │ service_id         │
                              │ created_at          │       │ target_version     │
                              └─────────────────────┘       │ traffic_percentage │
                                                            │ match_user_ids     │
                                                            │ match_header       │
                                                            │ created_at         │
                                                            └─────────────────────┘
```

## 数据库表

### 1. route_config (路由配置表)

网关路由配置表，定义请求路径到后端服务的映射规则。

| 字段名 | 类型 | 长度 | 允许NULL | 默认值 | 约束 | 说明 |
|--------|------|------|----------|--------|------|------|
| id | BIGINT | - | NO | - | PK, AUTO_INCREMENT | 配置 ID |
| path | VARCHAR | 200 | NO | - | UNIQUE | 路径模式 |
| service_id | VARCHAR | 100 | NO | - | - | 目标服务 ID |
| strip_prefix | INT | - | NO | 0 | - | 去除前缀层级 |
| timeout | INT | - | NO | 5000 | - | 超时时间（毫秒） |
| retry | INT | - | NO | 0 | - | 重试次数 |
| status | TINYINT | - | NO | 1 | - | 状态 (0=禁用, 1=启用) |
| created_at | DATETIME | - | NO | CURRENT_TIMESTAMP | - | 创建时间 |
| updated_at | DATETIME | - | NO | CURRENT_TIMESTAMP | ON UPDATE CURRENT_TIMESTAMP | 更新时间 |

**索引**:
- PRIMARY KEY: `id`
- UNIQUE KEY: `uk_path` (`path`)
- INDEX: `idx_service_id` (`service_id`)
- INDEX: `idx_status` (`status`)

### 2. rate_limit_config (限流配置表)

限流规则配置表，定义多维度限流策略。

| 字段名 | 类型 | 长度 | 允许NULL | 默认值 | 约束 | 说明 |
|--------|------|------|----------|--------|------|------|
| id | BIGINT | - | NO | - | PK, AUTO_INCREMENT | 配置 ID |
| dimension | VARCHAR | 20 | NO | - | - | 限流维度 (USER/TENANT/API/IP) |
| resource | VARCHAR | 100 | NO | - | - | 资源标识 |
| limit | INT | - | NO | - | - | 限额值 |
| window_seconds | INT | - | NO | - | - | 时间窗口（秒） |
| burst | INT | - | YES | NULL | - | 突发缓冲值 |
| whitelist | TEXT | - | YES | NULL | - | 白名单（JSON 数组） |
| status | TINYINT | - | NO | 1 | - | 状态 |
| created_at | DATETIME | - | NO | CURRENT_TIMESTAMP | - | 创建时间 |
| updated_at | DATETIME | - | NO | CURRENT_TIMESTAMP | ON UPDATE CURRENT_TIMESTAMP | 更新时间 |

### 3. health_check_config (健康检查配置表)

后端服务健康检查配置。

| 字段名 | 类型 | 长度 | 允许NULL | 默认值 | 约束 | 说明 |
|--------|------|------|----------|--------|------|------|
| id | BIGINT | - | NO | - | PK, AUTO_INCREMENT | 配置 ID |
| service_id | VARCHAR | 100 | NO | - | - | 服务名称 |
| interval | INT | - | NO | 10 | - | 检查间隔（秒） |
| timeout | INT | - | NO | 5 | - | 超时时间（秒） |
| failure_threshold | INT | - | NO | 3 | - | 失败阈值 |
| health_path | VARCHAR | 200 | NO | /actuator/health | - | 健康检查路径 |
| status | TINYINT | - | NO | 1 | - | 状态 |
| created_at | DATETIME | - | NO | CURRENT_TIMESTAMP | - | 创建时间 |

### 4. cache_config (缓存配置表)

GET 请求缓存策略配置。

| 字段名 | 类型 | 长度 | 允许NULL | 默认值 | 约束 | 说明 |
|--------|------|------|----------|--------|------|------|
| id | BIGINT | - | NO | - | PK, AUTO_INCREMENT | 配置 ID |
| api_path | VARCHAR | 200 | NO | - | - | API 路径模式 |
| ttl | INT | - | NO | - | - | 缓存过期时间（秒） |
| max_size | INT | - | NO | 10000 | - | 最大缓存条目数 |
| key_generator | VARCHAR | 50 | NO | DEFAULT | - | Key 生成策略 |
| tenant_isolated | TINYINT | - | NO | 1 | - | 是否租户隔离 |
| status | TINYINT | - | NO | 1 | - | 状态 |
| created_at | DATETIME | - | NO | CURRENT_TIMESTAMP | - | 创建时间 |

### 5. canary_strategy (灰度策略表)

灰度发布策略配置。

| 字段名 | 类型 | 长度 | 允许NULL | 默认值 | 约束 | 说明 |
|--------|------|------|----------|--------|------|------|
| id | BIGINT | - | NO | - | PK, AUTO_INCREMENT | 策略 ID |
| service_id | VARCHAR | 100 | NO | - | - | 服务名称 |
| target_version | VARCHAR | 50 | NO | - | - | 目标版本 |
| traffic_percentage | INT | - | NO | - | - | 流量比例 (0-100) |
| match_user_ids | TEXT | - | YES | NULL | - | 匹配的用户 ID (JSON) |
| match_header | VARCHAR | 50 | YES | NULL | - | 匹配的请求头 |
| match_header_value | VARCHAR | 200 | YES | NULL | - | 请求头值 |
| status | TINYINT | - | NO | 1 | - | 状态 |
| created_at | DATETIME | - | NO | CURRENT_TIMESTAMP | - | 创建时间 |

### 6. api_call_log (API 调用日志表)

记录所有 API 调用的日志。

| 字段名 | 类型 | 长度 | 允许NULL | 默认值 | 约束 | 说明 |
|--------|------|------|----------|--------|------|------|
| id | BIGINT | - | NO | - | PK, AUTO_INCREMENT | 日志 ID |
| request_id | VARCHAR | 50 | NO | - | INDEX | 请求 ID |
| caller_type | VARCHAR | 20 | NO | - | - | 调用方类型 (USER/API_KEY) |
| caller_id | VARCHAR | 100 | NO | - | - | 调用方 ID |
| tenant_id | BIGINT | - | YES | NULL | - | 租户 ID |
| api_path | VARCHAR | 200 | NO | - | INDEX | API 路径 |
| method | VARCHAR | 10 | NO | - | - | 请求方法 |
| response_status | INT | - | NO | - | - | 响应状态码 |
| response_time | INT | - | NO | - | - | 响应时间（毫秒） |
| created_at | DATETIME | - | NO | CURRENT_TIMESTAMP | INDEX | 创建时间 |

**索引**:
- INDEX: `idx_request_id` (`request_id`)
- INDEX: `idx_api_path` (`api_path`)
- INDEX: `idx_created_at` (`created_at`)
- INDEX: `idx_caller` (`caller_type`, `caller_id`)

### 7. alert_rule (告警规则表)

监控告警规则配置。

| 字段名 | 类型 | 长度 | 允许NULL | 默认值 | 约束 | 说明 |
|--------|------|------|----------|--------|------|------|
| id | BIGINT | - | NO | - | PK, AUTO_INCREMENT | 规则 ID |
| name | VARCHAR | 100 | NO | - | - | 规则名称 |
| metric | VARCHAR | 50 | NO | - | - | 监控指标 |
| threshold | DECIMAL | - | NO | - | - | 阈值 |
| condition | VARCHAR | 20 | NO | - | - | 条件 (GT/LT/GTE/LTE) |
| notify_type | VARCHAR | 20 | NO | - | - | 通知方式 |
| notify_target | VARCHAR | 200 | NO | - | - | 通知目标 |
| status | TINYINT | - | NO | 1 | - | 状态 |
| created_at | DATETIME | - | NO | CURRENT_TIMESTAMP | - | 创建时间 |

## 实体类定义

### RouteConfig.java

```java
@TableName("route_config")
public class RouteConfig {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String path;

    @TableField("service_id")
    private String serviceId;

    @TableField("strip_prefix")
    private Integer stripPrefix;

    private Integer timeout;

    private Integer retry;

    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
```

### RateLimitConfig.java

```java
@TableName("rate_limit_config")
public class RateLimitConfig {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String dimension; // USER, TENANT, API, IP

    private String resource;

    private Integer limit;

    @TableField("window_seconds")
    private Integer windowSeconds;

    private Integer burst;

    private String whitelist; // JSON array

    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
```

## 数据一致性

### 配置变更事务

```java
@Transactional
public void updateRouteConfig(Long id, RouteConfig newConfig) {
    RouteConfig oldConfig = routeConfigMapper.selectById(id);
    routeConfigMapper.updateById(newConfig);

    // 记录审计日志
    auditLogService.log("UPDATE_ROUTE", id, oldConfig, newConfig);

    // 清除相关缓存
    cacheService.evict("route:" + newConfig.getPath());
}
```

### 软删除

部分表使用软删除（如 route_config 的 status 字段）。

### 审计日志

所有配置变更记录到审计日志表。

## 下一步

- 查看技术研究: `research.md`
- 查看快速开始指南: `quickstart.md`
- 查看 API 契约: `contracts/`
