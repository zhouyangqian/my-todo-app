# Tasks: 权限模块

**Feature Branch**: `002-permission-module`
**Generated**: 2026-04-07
**Total Tasks**: 48

## Overview

| Story | Name | Priority | Tasks | Parallel |
|-------|------|----------|-------|----------|
| US1 | 接口与数据权限控制 | P1 | 16 | 8 |
| US2 | 角色组管理 | P2 | 10 | 5 |
| US3 | 动态权限配置接口 | P3 | 8 | 4 |
| US4 | 实时会话管理 | P4 | 8 | 4 |
| US5 | 黑名单与强制下线 | P5 | 6 | 3 |

### Dependencies

```
Phase 1 (Setup) ──► Phase 2 (Foundation)
                          │
                          ▼
                   US1 (权限控制)
                          │
         ┌────────────────┼────────────────┐
         │                │                │
         ▼                ▼                ▼
       US2              US3              US4
                                           │
                                           ▼
                                         US5
```

---

## Phase 1: Setup (项目初始化)

**Duration**: Week 1

- [ ] T001 创建 permission-service Maven 模块 `services/permission-service/pom.xml`
- [ ] T002 [P] 创建 Spring Boot 主类 `services/permission-service/src/main/java/com/example/permission/PermissionServiceApplication.java`
- [ ] T003 [P] 配置 application.yml `services/permission-service/src/main/resources/application.yml`
- [ ] T004 [P] 创建数据库 schema 脚本 `services/permission-service/src/main/resources/db/migration/V1__permission_schema.sql`
- [ ] T005 [P] 配置 Redis (Token黑名单) `services/permission-service/src/main/java/com/example/permission/config/RedisConfig.java`
- [ ] T006 [P] 配置 WebSocket `services/permission-service/src/main/java/com/example/permission/config/WebSocketConfig.java`

**Checkpoint**: 项目可启动

---

## Phase 2: Foundation (基础设施)

**Duration**: Week 1

- [ ] T007 创建 Permission 实体 `services/permission-service/src/main/java/com/example/permission/entity/Permission.java`
- [ ] T008 [P] 创建 Role 实体 `services/permission-service/src/main/java/com/example/permission/entity/Role.java`
- [ ] T009 [P] 创建 RolePermission 实体 `services/permission-service/src/main/java/com/example/permission/entity/RolePermission.java`
- [ ] T010 [P] 创建 DataPermission 实体 `services/permission-service/src/main/java/com/example/permission/entity/DataPermission.java`
- [ ] T011 [P] 创建 Session 实体 `services/permission-service/src/main/java/com/example/permission/entity/Session.java`
- [ ] T012 [P] 创建 Blacklist 实体 `services/permission-service/src/main/java/com/example/permission/entity/Blacklist.java`
- [ ] T013 [P] 创建 PermissionMapper `services/permission-service/src/main/java/com/example/permission/mapper/PermissionMapper.java`
- [ ] T014 [P] 创建 RoleMapper `services/permission-service/src/main/java/com/example/permission/mapper/RoleMapper.java`

**Checkpoint**: 所有实体类编译通过

---

## Phase 3: US1 - 接口与数据权限控制 (P1)

**Duration**: Week 2

### Annotation
- [ ] T015 [P] [US1] 创建 @RequiresPermission 注解 `services/permission-service/src/main/java/com/example/permission/annotation/RequiresPermission.java`
- [ ] T016 [P] [US1] 创建 @DataScope 注解 `services/permission-service/src/main/java/com/example/permission/annotation/DataScope.java`

### Interceptor
- [ ] T017 [US1] 创建 PermissionInterceptor `services/permission-service/src/main/java/com/example/permission/interceptor/PermissionInterceptor.java`
- [ ] T018 [US1] 创建 DataScopeInterceptor `services/permission-service/src/main/java/com/example/permission/interceptor/DataScopeInterceptor.java`

### Service
- [ ] T019 [US1] 创建 PermissionService 接口 `services/permission-service/src/main/java/com/example/permission/service/PermissionService.java`
- [ ] T020 [US1] 实现 PermissionServiceImpl `services/permission-service/src/main/java/com/example/permission/service/impl/PermissionServiceImpl.java`

### Controller
- [ ] T021 [US1] 实现 PermissionController `services/permission-service/src/main/java/com/example/permission/controller/PermissionController.java`
- [ ] T022 [US1] 实现权限验证 API `services/permission-service/src/main/java/com/example/permission/controller/AuthController.java`

### Frontend
- [ ] T023 [P] [US1] 创建权限管理页面 `frontend/src/views/permission/PermissionList.vue`
- [ ] T024 [US1] 创建权限 API 客户端 `frontend/src/api/permission.js`

**Checkpoint**: 权限注解和拦截器正常工作

---

## Phase 4: US2 - 角色组管理 (P2)

**Duration**: Week 3

### DTO
- [ ] T025 [P] [US2] 创建 RoleCreateRequest `services/permission-service/src/main/java/com/example/permission/dto/RoleCreateRequest.java`
- [ ] T026 [P] [US2] 创建 RoleResponse `services/permission-service/src/main/java/com/example/permission/dto/RoleResponse.java`

### Service
- [ ] T027 [US2] 创建 RoleService 接口 `services/permission-service/src/main/java/com/example/permission/service/RoleService.java`
- [ ] T028 [US2] 实现 RoleServiceImpl `services/permission-service/src/main/java/com/example/permission/service/impl/RoleServiceImpl.java`

### Controller
- [ ] T029 [US2] 实现 RoleController `services/permission-service/src/main/java/com/example/permission/controller/RoleController.java`

### Frontend
- [ ] T030 [P] [US2] 创建角色管理页面 `frontend/src/views/permission/RoleList.vue`
- [ ] T031 [US2] 创建角色 API 客户端 `frontend/src/api/role.js`
- [ ] T032 [US2] 实现角色权限配置 `frontend/src/views/permission/RolePermission.vue`

**Checkpoint**: 可管理角色和权限分配

---

## Phase 5: US3 - 动态权限配置接口 (P3)

**Duration**: Week 3

### Service
- [ ] T033 [US3] 创建 DynamicPermissionService `services/permission-service/src/main/java/com/example/permission/service/DynamicPermissionService.java`

### Controller
- [ ] T034 [US3] 实现动态权限 API `services/permission-service/src/main/java/com/example/permission/controller/DynamicPermissionController.java`

### Cache
- [ ] T035 [US3] 实现权限缓存刷新 `services/permission-service/src/main/java/com/example/permission/cache/PermissionCache.java`

### Frontend
- [ ] T036 [P] [US3] 创建动态权限配置页面 `frontend/src/views/permission/DynamicPermission.vue`
- [ ] T037 [US3] 实现权限热更新提示 `frontend/src/components/PermissionRefresh.vue`
- [ ] T038 [US3] 添加权限变更通知 `frontend/src/utils/permissionNotify.js`

**Checkpoint**: 可动态配置权限

---

## Phase 6: US4 - 实时会话管理 (P4)

**Duration**: Week 4

### Service
- [ ] T039 [US4] 创建 SessionService `services/permission-service/src/main/java/com/example/permission/service/SessionService.java`

### WebSocket
- [ ] T040 [US4] 创建 WebSocket Handler `services/permission-service/src/main/java/com/example/permission/websocket/SessionWebSocketHandler.java`
- [ ] T041 [US4] 实现挤号逻辑 `services/permission-service/src/main/java/com/example/permission/service/impl/KickoutService.java`

### Frontend
- [ ] T042 [P] [US4] 创建 WebSocket 客户端 `frontend/src/utils/websocket.js`
- [ ] T043 [US4] 实现被挤号提示 `frontend/src/components/KickoutNotification.vue`
- [ ] T044 [US4] 实现自动重连 `frontend/src/utils/websocketReconnect.js`

**Checkpoint**: 挤号和实时通知正常

---

## Phase 7: US5 - 黑名单与强制下线 (P5)

**Duration**: Week 4

### Service
- [ ] T045 [US5] 创建 BlacklistService `services/permission-service/src/main/java/com/example/permission/service/BlacklistService.java`

### Controller
- [ ] T046 [US5] 实现 BlacklistController `services/permission-service/src/main/java/com/example/permission/controller/BlacklistController.java`

### Frontend
- [ ] T047 [P] [US5] 创建黑名单管理页面 `frontend/src/views/permission/Blacklist.vue`
- [ ] T048 [US5] 实现强制下线操作 `frontend/src/views/permission/OnlineUsers.vue`

**Checkpoint**: 黑名单和强制下线正常

---

## Phase 8: US6 - 权限模板 (P3)

**Duration**: Week 5

### Service
- [ ] T049 [US6] 创建 PermissionTemplateService `services/permission-service/src/main/java/com/example/permission/service/PermissionTemplateService.java`
- [ ] T050 [US6] 实现 TemplateServiceImpl `services/permission-service/src/main/java/com/example/permission/service/impl/PermissionTemplateServiceImpl.java`

### Controller
- [ ] T051 [US6] 实现 PermissionTemplateController `services/permission-service/src/main/java/com/example/permission/controller/PermissionTemplateController.java`

### Frontend
- [ ] T052 [P] [US6] 创建权限模板列表页面 `frontend/src/views/permission/TemplateList.vue`
- [ ] T053 [P] [US6] 创建模板配置组件 `frontend/src/views/permission/TemplateConfig.vue`
- [ ] T054 [US6] 创建模板 API 客户端 `frontend/src/api/template.js`

**Checkpoint**: 权限模板可用

---

## Phase 9: US7 - 扩展数据权限 (P3)

**Duration**: Week 5

### Entity
- [ ] T055 [US7] 创建 DataPermissionRule 实体 `services/permission-service/src/main/java/com/example/permission/entity/DataPermissionRule.java`
- [ ] T056 [P] [US7] 创建 DataPermissionRuleMapper `services/permission-service/src/main/java/com/example/permission/mapper/DataPermissionRuleMapper.java`

### Service
- [ ] T057 [US7] 创建 DataPermissionRuleService `services/permission-service/src/main/java/com/example/permission/service/DataPermissionRuleService.java`
- [ ] T058 [US7] 实现数据权限 SQL 构建器 `services/permission-service/src/main/java/com/example/permission/builder/DataScopeSqlBuilder.java`

### Controller
- [ ] T059 [US7] 实现 DataPermissionRuleController `services/permission-service/src/main/java/com/example/permission/controller/DataPermissionRuleController.java`

### Frontend
- [ ] T060 [P] [US7] 创建数据权限规则页面 `frontend/src/views/permission/DataPermissionRule.vue`
- [ ] T061 [US7] 创建数据权限 API 客户端 `frontend/src/api/dataPermission.js`

**Checkpoint**: 扩展数据权限可用

---

## Phase 10: US8 - 角色继承 (P4)

**Duration**: Week 6

### Service
- [ ] T062 [US8] 创建 RoleInheritanceService `services/permission-service/src/main/java/com/example/permission/service/RoleInheritanceService.java`
- [ ] T063 [US8] 实现循环继承检测 `services/permission-service/src/main/java/com/example/permission/service/impl/CycleDetectionService.java`
- [ ] T064 [US8] 实现权限继承计算 `services/permission-service/src/main/java/com/example/permission/service/impl/PermissionInheritanceCalculator.java`

### Controller
- [ ] T065 [US8] 实现角色继承 API `services/permission-service/src/main/java/com/example/permission/controller/RoleInheritanceController.java`

### Frontend
- [ ] T066 [P] [US8] 创建角色继承配置页面 `frontend/src/views/permission/RoleInheritance.vue`
- [ ] T067 [P] [US8] 创建继承权限树组件 `frontend/src/components/InheritedPermissionTree.vue`
- [ ] T068 [US8] 创建继承 API 客户端 `frontend/src/api/roleInheritance.js`

**Checkpoint**: 角色继承可用

---

## Summary

| Metric | Value |
|--------|-------|
| **Total Tasks** | 68 |
| **Parallel Tasks** | 36 |
| **Phases** | 10 |
| **Duration** | 6 weeks |

### Overview

| Story | Name | Priority | Tasks | Parallel |
|-------|------|----------|-------|----------|
| US1 | 接口与数据权限控制 | P1 | 10 | 5 |
| US2 | 角色组管理 | P2 | 8 | 4 |
| US3 | 动态权限配置接口 | P3 | 6 | 3 |
| US4 | 实时会话管理 | P4 | 6 | 3 |
| US5 | 黑名单与强制下线 | P5 | 4 | 2 |
| US6 | 权限模板 | P3 | 6 | 3 |
| US7 | 扩展数据权限 | P3 | 7 | 3 |
| US8 | 角色继承 | P4 | 7 | 3 |

### MVP Scope

- Phase 1-2: Setup + Foundation
- Phase 3: US1 权限控制

**MVP Tasks**: 24 tasks
**MVP Duration**: ~2 weeks
