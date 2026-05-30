# Tasks: API 网关与集成管理

**Feature Branch**: `004-gateway`
**Generated**: 2026-04-07
**Total Tasks**: 50

## Overview

| Story | Name | Priority | Tasks | Done | Parallel |
|-------|------|----------|-------|------|----------|
| US1 | 统一API网关路由 | P1 | 10 | 6 | 5 |
| US2 | 第三方接口集成管理 | P1 | 5 | 0 | 3 |
| US3 | 对外API开放管理 | P2 | 5 | 0 | 3 |
| US4 | 流量控制与限流 | P1 | 9 | 4 | 5 |
| US5 | 健康检查与服务发现 | P1 | 7 | 4 | 4 |
| US6 | 响应缓存策略 | P2 | 6 | 5 | 3 |
| US7 | 灰度发布与流量切换 | P2 | 7 | 4 | 4 |
| US8 | 实时监控与统计 | P2 | 8 | 5 | 4 |
| US9 | 服务熔断与降级 | P2 | 8 | 3 | 4 |

### Dependencies

```
Phase 1 (Setup) ──► Phase 2 (Foundation)
                          │
         ┌────────────────┼────────────────┐
         │                │                │
         ▼                ▼                ▼
       US1              US4              US5
         │                │                │
         └────────────────┼────────────────┘
                          │
         ┌────────────────┴────────────────┐
         │                                 │
         ▼                                 ▼
       US2                              US3
```

---

## Phase 1: Setup (项目初始化)

**Duration**: Week 1

- [x] T001 创建 gateway Maven 模块 `gateway/pom.xml`
- [x] T002 [P] 创建 Spring Cloud Gateway 主类 `gateway/src/main/java/com/example/gateway/GatewayApplication.java`
- [x] T003 [P] 配置 application.yml `gateway/src/main/resources/application.yml`
- [x] T004 [P] 配置 application-dev.yml `gateway/src/main/resources/application-dev.yml` (exists as application-docker.yml)
- [x] T005 [P] 配置 Redis (限流) `gateway/src/main/java/com/example/gateway/config/RedisConfig.java` (exists as ReactiveRedisConfig.java)
- [x] T006 [P] 配置 Nacos/Consul (服务发现) `gateway/src/main/java/com/example/gateway/config/DiscoveryConfig.java` (configured in application.yml)

**Checkpoint**: 网关可启动

---

## Phase 2: Foundation (基础设施)

**Duration**: Week 1

- [ ] T007 创建 RouteConfig 实体 `gateway/src/main/java/com/example/gateway/entity/RouteConfig.java` — NOT implemented (uses YAML-based config, no DB entities)
- [ ] T008 [P] 创建 ExternalApi 实体 `gateway/src/main/java/com/example/gateway/entity/ExternalApi.java` — NOT in gateway (handled by dict-service)
- [ ] T009 [P] 创建 ApiKey 实体 `gateway/src/main/java/com/example/gateway/entity/ApiKey.java` — NOT in gateway (handled by dict-service)
- [ ] T010 [P] 创建 RateLimitConfig 实体 `gateway/src/main/java/com/example/gateway/entity/RateLimitConfig.java` — NOT a DB entity (uses @ConfigurationProperties)
- [x] T011 [P] 创建 ServiceHealth 实体 `gateway/src/main/java/com/example/gateway/entity/ServiceHealth.java` — EXISTS as inner class in HealthCheckService.java
- [ ] T012 [P] 创建 RouteMapper `gateway/src/main/java/com/example/gateway/mapper/RouteMapper.java` — NOT implemented (no DB-driven routes)
- [x] T013 [P] 创建统一响应类 `gateway/src/main/java/com/example/gateway/common/ApiResponse.java` — EXISTS as GlobalExceptionHandler response format
- [x] T014 [P] 创建全局异常处理 `gateway/src/main/java/com/example/gateway/exception/GlobalExceptionHandler.java` — EXISTS as handler/GlobalExceptionHandler.java

**Checkpoint**: 所有实体类编译通过

---

## Phase 3: US1 - 统一API网关路由 (P1)

**Duration**: Week 2

### Filter
- [x] T015 [P] [US1] 创建 TokenValidationFilter `gateway/src/main/java/com/example/gateway/filter/TokenValidationFilter.java` — EXISTS with JWT validation + public endpoint whitelist + header injection
- [ ] T016 [P] [US1] 创建 PermissionFilter `gateway/src/main/java/com/example/gateway/filter/PermissionFilter.java` — NOT implemented (permissions checked downstream)
- [x] T017 [US1] 创建 RequestLogFilter `gateway/src/main/java/com/example/gateway/filter/RequestLogFilter.java` — EXISTS with request ID generation + request/response logging

### Service
- [ ] T018 [US1] 创建 RouteService `gateway/src/main/java/com/example/gateway/service/RouteService.java` — NOT as DB-driven service (routes configured in YAML)
- [x] T019 [US1] 创建 AuthService (调用认证模块) `gateway/src/main/java/com/example/gateway/service/AuthService.java` — TokenValidationFilter handles JWT auth

### Controller
- [x] T020 [US1] 实现 RouteController `gateway/src/main/java/com/example/gateway/controller/RouteController.java` — EXISTS as GatewayConfigHandler with route listing

### Config
- [x] T021 [US1] 配置动态路由 `gateway/src/main/java/com/example/gateway/config/DynamicRouteConfig.java` — EXISTS via Spring Cloud Gateway + Nacos discovery

### Frontend
- [x] T022 [P] [US1] 创建路由配置页面 `frontend/src/views/gateway/RouteConfig.vue` — EXISTS at views/system/gateway/
- [x] T023 [US1] 创建网关 API 客户端 `frontend/src/api/gateway.js` — EXISTS as api/gateway.js
- [ ] T024 [US1] 实现路由监控 `frontend/src/views/gateway/RouteMonitor.vue` — NOT verified

**Checkpoint**: 网关路由正常工作

---

## Phase 4: US4 - 流量控制与限流 (P1)

**Duration**: Week 2

### Filter
- [x] T025 [US4] 创建 RateLimitFilter `gateway/src/main/java/com/example/gateway/filter/RateLimitFilter.java` — EXISTS with Redis sliding window, 3 dimensions (IP/User/Tenant)

### Service
- [x] T026 [US4] 创建 RateLimitService `gateway/src/main/java/com/example/gateway/service/RateLimitService.java` — EXISTS as RateLimitFilter with built-in service logic
- [ ] T027 [US4] 实现令牌桶算法 `gateway/src/main/java/com/example/gateway/limiter/TokenBucketLimiter.java` — NOT implemented (uses Redis sliding window instead)
- [ ] T028 [US4] 实现漏桶算法 `gateway/src/main/java/com/example/gateway/limiter/LeakyBucketLimiter.java` — NOT implemented (uses Redis sliding window instead)

### Controller
- [x] T029 [US4] 实现 RateLimitController `gateway/src/main/java/com/example/gateway/controller/RateLimitController.java` — Rate limit config available via GatewayConfigHandler

### Frontend
- [x] T030 [P] [US4] 创建限流配置页面 `frontend/src/views/gateway/RateLimitConfig.vue` — EXISTS at views/system/gateway/ frontend
- [ ] T031 [US4] 实现限流监控 `frontend/src/views/gateway/RateLimitMonitor.vue` — NOT verified
- [ ] T032 [US4] 创建限流 API 客户端 `frontend/src/api/rateLimit.js` — NOT verified
- [ ] T033 [US4] 实现限流告警 `frontend/src/utils/rateLimitAlert.js` — NOT implemented

**Checkpoint**: 限流功能正常

---

## Phase 5: US5 - 健康检查与服务发现 (P1)

**Duration**: Week 3

### Service
- [x] T034 [US5] 创建 HealthCheckService `gateway/src/main/java/com/example/gateway/service/HealthCheckService.java` — EXISTS with 30s scheduled checks for 7 services
- [x] T035 [US5] 创建 ServiceDiscoveryService `gateway/src/main/java/com/example/gateway/service/ServiceDiscoveryService.java` — EXISTS via Nacos discovery in application.yml

### Task
- [x] T036 [US5] 创建健康检查定时任务 `gateway/src/main/java/com/example/gateway/task/HealthCheckTask.java` — EXISTS as @Scheduled in HealthCheckService

### Controller
- [x] T037 [US5] 实现 HealthController `gateway/src/main/java/com/example/gateway/controller/HealthController.java` — EXISTS with /actuator/gateway-health and /actuator/gateway-status

### Frontend
- [x] T038 [P] [US5] 创建服务健康页面 `frontend/src/views/gateway/ServiceHealth.vue` — EXISTS at views/system/monitor/
- [ ] T039 [US5] 实现服务监控 `frontend/src/views/gateway/ServiceMonitor.vue` — NOT verified
- [ ] T040 [US5] 创建健康检查 API 客户端 `frontend/src/api/health.js` — NOT verified

**Checkpoint**: 健康检查正常

---

## Phase 6: US2 - 第三方接口集成管理 (P1)

**Duration**: Week 3

### Service
- [ ] T041 [US2] 创建 ExternalApiService `gateway/src/main/java/com/example/gateway/service/ExternalApiService.java` — NOT in gateway (in dict-service as ThirdPartyApiService)

### Filter
- [ ] T042 [US2] 创建 ExternalApiFilter `gateway/src/main/java/com/example/gateway/filter/ExternalApiFilter.java` — NOT implemented

### Controller
- [ ] T043 [US2] 实现 ExternalApiController `gateway/src/main/java/com/example/gateway/controller/ExternalApiController.java` — NOT in gateway

### Frontend
- [ ] T044 [P] [US2] 创建第三方接口管理页面 `frontend/src/views/gateway/ExternalApiList.vue` — NOT in gateway frontend
- [ ] T045 [US2] 创建外部 API 客户端 `frontend/src/api/externalApi.js` — NOT in gateway

**Checkpoint**: 第三方接口集成正常

---

## Phase 7: US3 - 对外API开放管理 (P2)

**Duration**: Week 4

### Service
- [ ] T046 [US3] 创建 ApiKeyService `gateway/src/main/java/com/example/gateway/service/ApiKeyService.java` — NOT in gateway (in dict-service ApiMarketService)

### Filter
- [ ] T047 [US3] 创建 ApiKeyAuthFilter `gateway/src/main/java/com/example/gateway/filter/ApiKeyAuthFilter.java` — NOT implemented

### Controller
- [ ] T048 [US3] 实现 ApiKeyController `gateway/src/main/java/com/example/gateway/controller/ApiKeyController.java` — NOT in gateway

### Frontend
- [ ] T049 [P] [US3] 创建 API Key 管理页面 `frontend/src/views/gateway/ApiKeyManage.vue` — NOT in gateway frontend
- [ ] T050 [US3] 创建 API 开放配置 `frontend/src/views/gateway/OpenApiConfig.vue` — NOT implemented

**Checkpoint**: 对外API开放正常

---

## Phase 8: US6 - 响应缓存策略 (P2)

**Duration**: Week 5

### Service
- [x] T051 [US6] 创建 CacheService `gateway/src/main/java/com/example/gateway/service/CacheService.java` — EXISTS with get/put/evict/evictByPattern + stats
- [x] T052 [US6] 实现 CacheKeyGenerator `gateway/src/main/java/com/example/gateway/cache/CacheKeyGenerator.java` — EXISTS as CacheService.buildCacheKey() with tenant-aware keys

### Filter
- [x] T053 [US6] 创建 ResponseCacheFilter `gateway/src/main/java/com/example/gateway/filter/ResponseCacheFilter.java` — EXISTS as CacheFilter (caches GET 200 JSON responses)

### Controller
- [x] T054 [US6] 实现 CacheController `gateway/src/main/java/com/example/gateway/controller/CacheController.java` — EXISTS as CacheHandler with stats/evict endpoints

### Frontend
- [x] T055 [P] [US6] 创建缓存配置页面 `frontend/src/views/gateway/CacheConfig.vue` — EXISTS via views/system/cache/
- [ ] T056 [US6] 创建缓存 API 客户端 `frontend/src/api/cache.js` — NOT verified

**Checkpoint**: 响应缓存可用

---

## Phase 9: US7 - 灰度发布与流量切换 (P2)

**Duration**: Week 5

### Service
- [x] T057 [US7] 创建 CanaryService `gateway/src/main/java/com/example/gateway/service/CanaryService.java` — EXISTS with userId/header/percentage strategies
- [x] T058 [US7] 实现流量分配算法 `gateway/src/main/java/com/example/gateway/router/CanaryRouter.java` — EXISTS as CanaryFilter routing logic

### Filter
- [x] T059 [US7] 创建 CanaryFilter `gateway/src/main/java/com/example/gateway/filter/CanaryFilter.java` — EXISTS with configurable strategies via CanaryConfig

### Controller
- [ ] T060 [US7] 实现 CanaryController `gateway/src/main/java/com/example/gateway/controller/CanaryController.java` — NOT as separate controller (config via application.yml)

### Frontend
- [x] T061 [P] [US7] 创建灰度发布页面 `frontend/src/views/gateway/CanaryDeploy.vue` — EXISTS at views/system/canary/
- [ ] T062 [P] [US7] 创建流量切换组件 `frontend/src/views/gateway/TrafficSwitch.vue` — NOT verified
- [ ] T063 [US7] 创建灰度 API 客户端 `frontend/src/api/canary.js` — NOT verified

**Checkpoint**: 灰度发布可用

---

## Phase 10: US8 - 实时监控与统计 (P2)

**Duration**: Week 6

### Service
- [x] T064 [US8] 创建 MetricsService `gateway/src/main/java/com/example/gateway/service/MetricsService.java` — EXISTS as MetricsCollector with record/getMetrics/getTopSlowApis/getTopErrorApis
- [x] T065 [US8] 创建 AlertService `gateway/src/main/java/com/example/gateway/service/AlertService.java` — EXISTS in MonitorService with getAlertRules() + checkAlerts()

### Task
- [x] T066 [US8] 创建指标收集定时任务 `gateway/src/main/java/com/example/gateway/task/MetricsCollectorTask.java` — EXISTS as MetricsFilter (per-request collection)

### Controller
- [x] T067 [US8] 实现 MetricsController `gateway/src/main/java/com/example/gateway/controller/MetricsController.java` — EXISTS as MonitorHandler with metrics/dashboard/slow-apis/error-apis endpoints
- [ ] T068 [US8] 实现 AlertController `gateway/src/main/java/com/example/gateway/controller/AlertController.java` — NOT separate (alerts via MonitorService)

### Frontend
- [x] T069 [P] [US8] 创建监控面板页面 `frontend/src/views/gateway/MonitorDashboard.vue` — EXISTS at views/system/monitor/
- [ ] T070 [P] [US8] 创建告警配置页面 `frontend/src/views/gateway/AlertConfig.vue` — NOT implemented
- [ ] T071 [US8] 创建监控 API 客户端 `frontend/src/api/metrics.js` — NOT verified

**Checkpoint**: 实时监控可用

---

## Phase 11: US9 - 服务熔断与降级 (P2)

**Duration**: Week 6

### Service
- [x] T072 [US9] 创建 CircuitBreakerService `gateway/src/main/java/com/example/gateway/service/CircuitBreakerService.java` — EXISTS with CLOSED/OPEN/HALF_OPEN states, 5-failure threshold, 60s cooldown
- [x] T073 [US9] 实现熔断状态机 `gateway/src/main/java/com/example/gateway/circuitbreaker/CircuitBreakerStateMachine.java` — EXISTS in CircuitBreakerService with full state transitions
- [ ] T074 [US9] 创建 DegradationService `gateway/src/main/java/com/example/gateway/service/DegradationService.java` — NOT as separate service (degradation handled by CircuitBreakerFilter returning 503)

### Filter
- [x] T075 [US9] 创建 CircuitBreakerFilter `gateway/src/main/java/com/example/gateway/filter/CircuitBreakerFilter.java` — EXISTS with service-level circuit breaking

### Controller
- [ ] T076 [US9] 实现 CircuitBreakerController `gateway/src/main/java/com/example/gateway/controller/CircuitBreakerController.java` — NOT separate (status via GatewayMonitorHandler)

### Frontend
- [ ] T077 [P] [US9] 创建熔断配置页面 `frontend/src/views/gateway/CircuitBreakerConfig.vue` — NOT verified
- [ ] T078 [P] [US9] 创建降级策略页面 `frontend/src/views/gateway/DegradationConfig.vue` — NOT implemented
- [ ] T079 [US9] 创建熔断 API 客户端 `frontend/src/api/circuitBreaker.js` — NOT verified

**Checkpoint**: 服务熔断与降级可用

---

## Summary

| Metric | Value |
|--------|-------|
| **Total Tasks** | 79 |
| **Completed** | 40 |
| **Remaining** | 39 |
| **Parallel Tasks** | 42 |
| **Phases** | 11 |
| **Duration** | 6 weeks |

### Overview

| Story | Name | Priority | Tasks | Done | Parallel |
|-------|------|----------|-------|------|----------|
| US1 | 统一API网关路由 | P1 | 10 | 6 | 5 |
| US2 | 第三方接口集成管理 | P1 | 5 | 0 | 3 |
| US3 | 对外API开放管理 | P2 | 5 | 0 | 3 |
| US4 | 流量控制与限流 | P1 | 9 | 4 | 5 |
| US5 | 健康检查与服务发现 | P1 | 7 | 4 | 4 |
| US6 | 响应缓存策略 | P2 | 6 | 5 | 3 |
| US7 | 灰度发布与流量切换 | P2 | 7 | 4 | 4 |
| US8 | 实时监控与统计 | P2 | 8 | 5 | 4 |
| US9 | 服务熔断与降级 | P2 | 8 | 3 | 4 |

### MVP Scope

- Phase 1-2: Setup + Foundation
- Phase 3: US1 路由
- Phase 4: US4 限流
- Phase 5: US5 健康检查

**MVP Tasks**: 40 tasks
**MVP Duration**: ~3 weeks
