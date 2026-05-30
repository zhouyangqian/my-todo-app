# Tasks: 权限模块

**Feature Branch**: `002-permission-module`
**Generated**: 2026-04-07
**Total Tasks**: 48

## Overview

| Story | Name | Priority | Tasks | Done | Parallel |
|-------|------|----------|-------|------|----------|
| US1 | 接口与数据权限控制 | P1 | 10 | 7 | 5 |
| US2 | 角色组管理 | P2 | 8 | 6 | 4 |
| US3 | 动态权限配置接口 | P3 | 6 | 4 | 3 |
| US4 | 实时会话管理 | P4 | 6 | 2 | 3 |
| US5 | 黑名单与强制下线 | P5 | 4 | 4 | 2 |
| US6 | 权限模板 | P3 | 6 | 4 | 3 |
| US7 | 扩展数据权限 | P3 | 7 | 5 | 3 |
| US8 | 角色继承 | P4 | 7 | 5 | 3 |

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

- [x] T001 创建 permission-service Maven 模块 `services/permission-service/pom.xml`
- [x] T002 [P] 创建 Spring Boot 主类 `services/permission-service/src/main/java/com/example/permission/PermissionServiceApplication.java`
- [x] T003 [P] 配置 application.yml `services/permission-service/src/main/resources/application.yml`
- [x] T004 [P] 创建数据库 schema 脚本 `services/permission-service/src/main/resources/db/migration/V1__permission_schema.sql`
- [ ] T005 [P] 配置 Redis (Token黑名单) `services/permission-service/src/main/java/com/example/permission/config/RedisConfig.java` — **NOT implemented as explicit config class; Redis used via Spring auto-config**
- [ ] T006 [P] 配置 WebSocket `services/permission-service/src/main/java/com/example/permission/config/WebSocketConfig.java` — **NOT implemented; uses SSE via auth-service (SseService) instead**

**Checkpoint**: 项目可启动

---

## Phase 2: Foundation (基础设施)

**Duration**: Week 1

- [x] T007 创建 Permission 实体 `services/permission-service/src/main/java/com/example/permission/entity/Permission.java`
- [x] T008 [P] 创建 Role 实体 `services/permission-service/src/main/java/com/example/permission/entity/Role.java`
- [x] T009 [P] 创建 RolePermission 实体 `services/permission-service/src/main/java/com/example/permission/entity/RolePermission.java`
- [ ] T010 [P] 创建 DataPermission 实体 `services/permission-service/src/main/java/com/example/permission/entity/DataPermission.java` — **NOT implemented; DataPermissionRule entity exists instead**
- [x] T011 [P] 创建 Session 实体 `services/permission-service/src/main/java/com/example/permission/entity/Session.java`
- [x] T012 [P] 创建 Blacklist 实体 `services/permission-service/src/main/java/com/example/permission/entity/Blacklist.java`
- [x] T013 [P] 创建 PermissionMapper `services/permission-service/src/main/java/com/example/permission/mapper/PermissionMapper.java`
- [x] T014 [P] 创建 RoleMapper `services/permission-service/src/main/java/com/example/permission/mapper/RoleMapper.java`

**Checkpoint**: 所有实体类编译通过

---

## Phase 3: US1 - 接口与数据权限控制 (P1)

**Duration**: Week 2

### Annotation
- [ ] T015 [P] [US1] 创建 @RequiresPermission 注解 `services/permission-service/src/main/java/com/example/permission/annotation/RequiresPermission.java` — **NOT found in permission-service; may be in common module; controllers reference it**
- [ ] T016 [P] [US1] 创建 @DataScope 注解 `services/permission-service/src/main/java/com/example/permission/annotation/DataScope.java` — **NOT implemented**

### Interceptor
- [ ] T017 [US1] 创建 PermissionInterceptor `services/permission-service/src/main/java/com/example/permission/interceptor/PermissionInterceptor.java` — **NOT implemented**
- [ ] T018 [US1] 创建 DataScopeInterceptor `services/permission-service/src/main/java/com/example/permission/interceptor/DataScopeInterceptor.java` — **NOT implemented**

### Service
- [x] T019 [US1] 创建 PermissionService 接口 `services/permission-service/src/main/java/com/example/permission/service/PermissionService.java` — **Exists as concrete class with getUserPermissions, getPermissionTree, hasPermission, etc.**
- [x] T020 [US1] 实现 PermissionServiceImpl `services/permission-service/src/main/java/com/example/permission/service/impl/PermissionServiceImpl.java` — **Functionality in PermissionService.java (concrete class, no separate impl)**

### Controller
- [x] T021 [US1] 实现 PermissionController `services/permission-service/src/main/java/com/example/permission/controller/PermissionController.java` — **EXISTS with 8 endpoints**
- [x] T022 [US1] 实现权限验证 API `services/permission-service/src/main/java/com/example/permission/controller/AuthController.java` — **Functionality in PermissionController.checkPermission()**

### Frontend
- [x] T023 [P] [US1] 创建权限管理页面 `frontend/src/views/permission/PermissionList.vue` — **EXISTS at views/system/permission/index.vue**
- [x] T024 [US1] 创建权限 API 客户端 `frontend/src/api/permission.js`

**Checkpoint**: 权限注解和拦截器正常工作

---

## Phase 4: US2 - 角色组管理 (P2)

**Duration**: Week 3

### DTO
- [ ] T025 [P] [US2] 创建 RoleCreateRequest `services/permission-service/src/main/java/com/example/permission/dto/RoleCreateRequest.java` — **NOT separate DTO; uses entity directly**
- [ ] T026 [P] [US2] 创建 RoleResponse `services/permission-service/src/main/java/com/example/permission/dto/RoleResponse.java` — **NOT separate DTO; uses entity directly**

### Service
- [x] T027 [US2] 创建 RoleService 接口 `services/permission-service/src/main/java/com/example/permission/service/RoleService.java` — **EXISTS with CRUD + assignRolesToUser + assignPermissionsToRole**
- [x] T028 [US2] 实现 RoleServiceImpl `services/permission-service/src/main/java/com/example/permission/service/impl/RoleServiceImpl.java` — **Functionality in RoleService.java (concrete class)**

### Controller
- [x] T029 [US2] 实现 RoleController `services/permission-service/src/main/java/com/example/permission/controller/RoleController.java` — **EXISTS with 12 endpoints**

### Frontend
- [x] T030 [P] [US2] 创建角色管理页面 `frontend/src/views/permission/RoleList.vue` — **EXISTS at views/system/role/index.vue and views/system/role-manage/index.vue**
- [ ] T031 [US2] 创建角色 API 客户端 `frontend/src/api/role.js` — **NOT separate; role APIs in api/permission.js**
- [x] T032 [US2] 实现角色权限配置 `frontend/src/views/permission/RolePermission.vue` — **EXISTS at views/system/role/index.vue**

**Checkpoint**: 可管理角色和权限分配

---

## Phase 5: US3 - 动态权限配置接口 (P3)

**Duration**: Week 3

### Service
- [x] T033 [US3] 创建 DynamicPermissionService `services/permission-service/src/main/java/com/example/permission/service/DynamicPermissionService.java` — **EXISTS with Redis cache + CRUD + tree**

### Controller
- [x] T034 [US3] 实现动态权限 API `services/permission-service/src/main/java/com/example/permission/controller/DynamicPermissionController.java` — **EXISTS with 5 endpoints**

### Cache
- [x] T035 [US3] 实现权限缓存刷新 `services/permission-service/src/main/java/com/example/permission/cache/PermissionCache.java` — **IMPLEMENTED in DynamicPermissionService.invalidateCache()**

### Frontend
- [x] T036 [P] [US3] 创建动态权限配置页面 `frontend/src/views/permission/DynamicPermission.vue` — **EXISTS at views/system/permission-dynamic/index.vue**
- [ ] T037 [US3] 实现权限热更新提示 `frontend/src/components/PermissionRefresh.vue` — **NOT implemented**
- [ ] T038 [US3] 添加权限变更通知 `frontend/src/utils/permissionNotify.js` — **NOT implemented**

**Checkpoint**: 可动态配置权限

---

## Phase 6: US4 - 实时会话管理 (P4)

**Duration**: Week 4

### Service
- [x] T039 [US4] 创建 SessionService `services/permission-service/src/main/java/com/example/permission/service/SessionService.java` — **EXISTS with Redis+DB dual storage, enforceSingleSession, kickSession**

### WebSocket
- [ ] T040 [US4] 创建 WebSocket Handler `services/permission-service/src/main/java/com/example/permission/websocket/SessionWebSocketHandler.java` — **NOT implemented; uses SSE via SseService in auth-service instead**
- [x] T041 [US4] 实现挤号逻辑 `services/permission-service/src/main/java/com/example/permission/service/impl/KickoutService.java` — **EXISTS in SessionService.kickSession() and SessionService.enforceSingleSession()**

### Frontend
- [ ] T042 [P] [US4] 创建 WebSocket 客户端 `frontend/src/utils/websocket.js` — **NOT implemented; uses SSE**
- [ ] T043 [US4] 实现被挤号提示 `frontend/src/components/KickoutNotification.vue` — **NOT implemented; uses SSE in auth-service**
- [ ] T044 [US4] 实现自动重连 `frontend/src/utils/websocketReconnect.js` — **NOT implemented**

**Checkpoint**: 挤号和实时通知正常

---

## Phase 7: US5 - 黑名单与强制下线 (P5)

**Duration**: Week 4

### Service
- [x] T045 [US5] 创建 BlacklistService `services/permission-service/src/main/java/com/example/permission/service/BlacklistService.java` — **EXISTS with add/remove/check/pagination + auto-kick**

### Controller
- [x] T046 [US5] 实现 BlacklistController `services/permission-service/src/main/java/com/example/permission/controller/BlacklistController.java` — **EXISTS with 4 endpoints**

### Frontend
- [x] T047 [P] [US5] 创建黑名单管理页面 `frontend/src/views/permission/Blacklist.vue` — **EXISTS at views/system/blacklist/index.vue**
- [x] T048 [US5] 实现强制下线操作 `frontend/src/views/permission/OnlineUsers.vue` — **EXISTS at views/system/session/index.vue**

**Checkpoint**: 黑名单和强制下线正常

---

## Phase 8: US6 - 权限模板 (P3)

**Duration**: Week 5

### Service
- [x] T049 [US6] 创建 PermissionTemplateService `services/permission-service/src/main/java/com/example/permission/service/PermissionTemplateService.java` — **EXISTS with CRUD + apply to role + init system templates**
- [x] T050 [US6] 实现 TemplateServiceImpl `services/permission-service/src/main/java/com/example/permission/service/impl/PermissionTemplateServiceImpl.java` — **Functionality in PermissionTemplateService.java (concrete class)**

### Controller
- [x] T051 [US6] 实现 PermissionTemplateController `services/permission-service/src/main/java/com/example/permission/controller/PermissionTemplateController.java` — **EXISTS with 6 endpoints**

### Frontend
- [x] T052 [P] [US6] 创建权限模板列表页面 `frontend/src/views/permission/TemplateList.vue` — **EXISTS at views/system/permission-template/index.vue**
- [ ] T053 [P] [US6] 创建模板配置组件 `frontend/src/views/permission/TemplateConfig.vue` — **NOT verified as separate component**
- [ ] T054 [US6] 创建模板 API 客户端 `frontend/src/api/template.js` — **NOT verified**

**Checkpoint**: 权限模板可用

---

## Phase 9: US7 - 扩展数据权限 (P3)

**Duration**: Week 5

### Entity
- [x] T055 [US7] 创建 DataPermissionRule 实体 `services/permission-service/src/main/java/com/example/permission/entity/DataPermissionRule.java`
- [x] T056 [P] [US7] 创建 DataPermissionRuleMapper `services/permission-service/src/main/java/com/example/permission/mapper/DataPermissionRuleMapper.java`

### Service
- [x] T057 [US7] 创建 DataPermissionRuleService `services/permission-service/src/main/java/com/example/permission/service/DataPermissionRuleService.java` — **EXISTS with CRUD + getRulesByRoleIds**
- [ ] T058 [US7] 实现数据权限 SQL 构建器 `services/permission-service/src/main/java/com/example/permission/builder/DataScopeSqlBuilder.java` — **NOT implemented**

### Controller
- [x] T059 [US7] 实现 DataPermissionRuleController `services/permission-service/src/main/java/com/example/permission/controller/DataPermissionRuleController.java` — **EXISTS with 4 endpoints**

### Frontend
- [x] T060 [P] [US7] 创建数据权限规则页面 `frontend/src/views/permission/DataPermissionRule.vue` — **EXISTS at views/system/data-rule/index.vue**
- [ ] T061 [US7] 创建数据权限 API 客户端 `frontend/src/api/dataPermission.js` — **NOT verified**

**Checkpoint**: 扩展数据权限可用

---

## Phase 10: US8 - 角色继承 (P4)

**Duration**: Week 6

### Service
- [x] T062 [US8] 创建 RoleInheritanceService `services/permission-service/src/main/java/com/example/permission/service/RoleInheritanceService.java` — **EXISTS with setParent, removeParent, inheritance chain, effective permissions**
- [x] T063 [US8] 实现循环继承检测 `services/permission-service/src/main/java/com/example/permission/service/impl/CycleDetectionService.java` — **EXISTS in RoleService.hasCircularInheritance()**
- [x] T064 [US8] 实现权限继承计算 `services/permission-service/src/main/java/com/example/permission/service/impl/PermissionInheritanceCalculator.java` — **EXISTS in RoleInheritanceService.getInheritedPermissions() and getAllEffectivePermissions()**

### Controller
- [x] T065 [US8] 实现角色继承 API `services/permission-service/src/main/java/com/example/permission/controller/RoleInheritanceController.java` — **EXISTS with 6 endpoints**

### Frontend
- [x] T066 [P] [US8] 创建角色继承配置页面 `frontend/src/views/permission/RoleInheritance.vue` — **EXISTS at views/system/role-inheritance/index.vue**
- [ ] T067 [P] [US8] 创建继承权限树组件 `frontend/src/components/InheritedPermissionTree.vue` — **NOT verified as separate component**
- [ ] T068 [US8] 创建继承 API 客户端 `frontend/src/api/roleInheritance.js` — **NOT verified**

**Checkpoint**: 角色继承可用

---

## Summary

| Metric | Value |
|--------|-------|
| **Total Tasks** | 68 |
| **Completed** | 43 |
| **Remaining** | 25 |
| **Parallel Tasks** | 36 |
| **Phases** | 10 |
| **Duration** | 6 weeks |

### Overview

| Story | Name | Priority | Tasks | Done | Parallel |
|-------|------|----------|-------|------|----------|
| US1 | 接口与数据权限控制 | P1 | 10 | 7 | 5 |
| US2 | 角色组管理 | P2 | 8 | 6 | 4 |
| US3 | 动态权限配置接口 | P3 | 6 | 4 | 3 |
| US4 | 实时会话管理 | P4 | 6 | 2 | 3 |
| US5 | 黑名单与强制下线 | P5 | 4 | 4 | 2 |
| US6 | 权限模板 | P3 | 6 | 4 | 3 |
| US7 | 扩展数据权限 | P3 | 7 | 5 | 3 |
| US8 | 角色继承 | P4 | 7 | 5 | 3 |

### MVP Scope

- Phase 1-2: Setup + Foundation
- Phase 3: US1 权限控制

**MVP Tasks**: 24 tasks
**MVP Duration**: ~2 weeks
