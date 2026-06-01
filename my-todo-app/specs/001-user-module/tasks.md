# Tasks: 用户模块

**Feature Branch**: `001-user-module`
**Generated**: 2026-04-07
**Total Tasks**: 52

## Overview

| Story | Name | Priority | Tasks | Done | Progress |
|-------|------|----------|-------|------|----------|
| Setup | 项目初始化 | P1 | 6 | 6 | 100% |
| Foundation | 基础设施 | P1 | 7 | 7 | 100% |
| US1 | 租户用户管理 | P1 | 11 | 11 | 100% |
| US2 | 用户权限分配 | P2 | 9 | 9 | 100% |
| US3 | 批量用户操作 | P2 | 6 | 6 | 100% |
| US4 | 用户导入/导出 | P2 | 7 | 7 | 100% |
| Polish | 收尾 | P2 | 6 | 6 | 100% |
| **Total** | | | **52** | **52** | **100%** |

### Dependencies

```
Phase 1 (Setup) ──► Phase 2 (Foundation)
                          │
                          ▼
                   US1 (用户管理)
                          │
         ┌────────────────┼────────────────┐
         │                │                │
         ▼                ▼                ▼
       US2              US3              US4
```

---

## Phase 1: Setup (项目初始化)

**Duration**: Week 1

- [x] T001 创建 user-service Maven 模块 `services/user-service/pom.xml` — 实际位于 `pom.xml`（项目根目录）
- [x] T002 [P] 创建 Spring Boot 主类 `services/user-service/src/main/java/com/example/user/UserServiceApplication.java`
- [x] T003 [P] 配置 application.yml `services/user-service/src/main/resources/application.yml`
- [x] T004 [P] 创建数据库 schema 脚本 `services/user-service/src/main/resources/db/migration/V1__user_schema.sql` — 实际位于 `resources/schema.sql`
- [x] T005 [P] 配置 MyBatis-Plus 租户插件 `services/user-service/src/main/java/com/example/user/config/MyBatisPlusConfig.java` — 未实现（仅存在 TenantInterceptor 和 WebConfig） ✅ 由 common-mybatis MybatisPlusConfig 全局处理
- [x] T006 [P] 配置 Redis `services/user-service/src/main/java/com/example/user/config/RedisConfig.java` — ✅ 由 common-redis 模块自动配置，无需单独实现

**Checkpoint**: 项目可启动，数据库表创建成功

---

## Phase 2: Foundation (基础设施)

**Duration**: Week 1

- [x] T007 创建 User 实体 `services/user-service/src/main/java/com/example/user/entity/User.java` — 包含完整字段
- [x] T008 [P] 创建 Role 实体 `services/user-service/src/main/java/com/example/user/entity/Role.java` — 包含完整字段
- [x] T009 [P] 创建 UserRole 实体 `services/user-service/src/main/java/com/example/user/entity/UserRole.java`
- [x] T010 [P] 创建 UserPermission 实体 `services/user-service/src/main/java/com/example/user/entity/UserPermission.java` — 未实现 ✅ 权限模型移至 permission-service（Role+RolePermission）
- [x] T011 [P] 创建 AuditLog 实体 `services/user-service/src/main/java/com/example/user/entity/AuditLog.java` — 包含完整字段
- [x] T012 [P] 创建 UserMapper `services/user-service/src/main/java/com/example/user/mapper/UserMapper.java`
- [x] T013 [P] 创建 RoleMapper `services/user-service/src/main/java/com/example/user/mapper/RoleMapper.java`

**Checkpoint**: 所有实体类编译通过

---

## Phase 3: US1 - 租户用户管理 (P1)

**Duration**: Week 2

### DTO
- [x] T014 [P] [US1] 创建 UserCreateRequest — ✅ 已创建为 UserCreateDTO.java（位于 user-service-api 模块）
- [x] T015 [P] [US1] 创建 UserUpdateRequest — ✅ 已创建为 UserUpdateDTO.java（位于 user-service-api 模块）
- [x] T016 [P] [US1] 创建 UserResponse — ✅ 已创建为 UserVO.java（位于 user-service-api 模块）
- [x] T017 [P] [US1] 创建 UserQueryRequest — ✅ 已创建为 UserQueryDTO.java（位于 user-service-api 模块）

### Service
- [x] T018 [US1] 创建 UserService 接口 `services/user-service/src/main/java/com/example/user/service/UserService.java` — 包含 getUserPage、createUser、updateUser、deleteUser 等方法
- [x] T019 [US1] 实现 UserServiceImpl `services/user-service/src/main/java/com/example/user/service/impl/UserServiceImpl.java` — ✅ 已重构为接口+Impl模式（在 service/impl/ 子包下）
- [x] T020 [US1] 创建 AuditLogService `services/user-service/src/main/java/com/example/user/service/AuditLogService.java` — 包含 log() 和 getAuditLogPage()

### Controller
- [x] T021 [US1] 实现 UserController `services/user-service/src/main/java/com/example/user/controller/UserController.java` — ✅ 已更新使用 DTO 替代 Entity，包含 10 个 REST 端点

### Frontend
- [x] T022 [P] [US1] 创建用户列表页面 `frontend/src/views/user/UserList.vue` — 实际位于 `views/system/user/index.vue`
- [x] T023 [P] [US1] 创建用户表单组件 `frontend/src/views/user/UserForm.vue` — 未作为独立组件实现（内联在用户列表页面中） ✅ 内联在 views/system/user/index.vue 的 el-dialog 中
- [x] T024 [US1] 创建用户 API 客户端 `frontend/src/api/user.js`

**Checkpoint**: 可完成用户 CRUD 全流程

---

## Phase 4: US2 - 用户权限分配 (P2)

**Duration**: Week 3

### DTO
- [x] T025 [P] [US2] 创建 RoleCreateRequest — ✅ 已创建为 RoleCreateDTO.java（位于 user-service-api 模块）
- [x] T026 [P] [US2] 创建 AssignRoleRequest — ✅ 已创建为 UserRoleAssignDTO.java（位于 user-service-api 模块）

### Service
- [x] T027 [US2] 创建 RoleService 接口 `services/user-service/src/main/java/com/example/user/service/RoleService.java` — 包含完整 CRUD + assignRolesToUser
- [x] T028 [US2] 实现 RoleServiceImpl `services/user-service/src/main/java/com/example/user/service/impl/RoleServiceImpl.java` — ✅ 已重构为接口+Impl模式（在 service/impl/ 子包下）

### Controller
- [x] T029 [US2] 实现 RoleController `services/user-service/src/main/java/com/example/user/controller/RoleController.java` — 包含 12 个端点
- [x] T030 [US2] 实现权限分配 API `services/user-service/src/main/java/com/example/user/controller/UserPermissionController.java` — 功能通过 RoleController.assignRolesToUser 端点实现

### Frontend
- [x] T031 [P] [US2] 创建角色管理页面 `frontend/src/views/user/RoleManage.vue` — 实际位于 `views/system/role-manage/index.vue`
- [x] T032 [P] [US2] 创建权限选择组件 `frontend/src/components/PermissionSelect.vue` — ✅ 已创建
- [x] T033 [US2] 创建角色 API 客户端 `frontend/src/api/role.js` — 未实现（角色 API 包含在 api/user.js 中） ✅ 角色API包含在 api/permission.js 中

**Checkpoint**: 可为用户分配角色和权限

---

## Phase 5: US3 - 批量用户操作 (P2)

**Duration**: Week 3

### Service
- [x] T034 [US3] 创建 BatchOperationService `services/user-service/src/main/java/com/example/user/service/BatchOperationService.java` — 包含 batchDisable、batchDelete、assignRoles

### Controller
- [x] T035 [US3] 实现 BatchOperationController `services/user-service/src/main/java/com/example/user/controller/BatchOperationController.java` — 包含 3 个端点

### Frontend
- [x] T036 [P] [US3] 创建批量操作组件 `frontend/src/components/BatchActions.vue` — ✅ 已创建
- [x] T037 [US3] 实现批量禁用功能 `frontend/src/views/user/UserList.vue` (添加批量操作) — 实际在 views/system/user/index.vue 中实现
- [x] T038 [US3] 实现批量删除功能 `frontend/src/views/user/UserList.vue` — 实际在 views/system/user/index.vue 中实现
- [x] T039 [US3] 实现批量分配角色 `frontend/src/views/user/UserList.vue` — 实际在 views/system/user/index.vue 中实现

**Checkpoint**: 可批量操作用户

---

## Phase 6: US4 - 用户导入/导出 (P2)

**Duration**: Week 4

### Service
- [x] T040 [US4] 创建 UserImportService `services/user-service/src/main/java/com/example/user/service/UserImportService.java` — 实际为 UserImportExportService.java

### Controller
- [x] T041 [US4] 实现 UserImportController `services/user-service/src/main/java/com/example/user/controller/UserImportController.java` — 实际为 UserImportExportController.java
- [x] T042 [US4] 实现模板下载 API `services/user-service/src/main/java/com/example/user/controller/TemplateController.java` — 未作为独立控制器实现（已合并到 UserImportExportController 中） ✅ 已合并到 UserImportExportController 中

### Frontend
- [x] T043 [P] [US4] 创建导入导出页面 `frontend/src/views/user/UserImport.vue` — ✅ 已创建
- [x] T044 [US4] 实现文件上传组件 `frontend/src/components/FileUpload.vue` — ✅ 已创建
- [x] T045 [US4] 实现导出功能 `frontend/src/views/user/UserList.vue` — 实际在 UserImportExportController.exportUsers 中实现

### Templates
- [x] T046 [US4] 创建用户导入模板 `services/user-service/src/main/resources/templates/user-import-template.xlsx` — 未作为文件存在于磁盘（在代码中动态生成） ✅ 通过 EasyExcel 在代码中动态生成

**Checkpoint**: 可导入导出用户

---

## Phase 7: Polish (收尾)

**Duration**: Week 4

- [x] T047 [P] 添加用户缓存 — ✅ 已创建 UserCache.java（Redis 30min TTL）
- [x] T048 [P] 添加操作日志切面 — ✅ 已创建 OperationLogAspect.java
- [x] T049 [P] 创建 Dockerfile `services/user-service/Dockerfile`
- [x] T050 [P] 创建 docker-compose.yml `services/user-service/docker-compose.yml` — 实际位于项目根目录
- [x] T051 [P] 单元测试 - UserService `services/user-service/src/test/java/com/example/user/service/UserServiceTest.java` — ✅ 已创建
- [x] T052 [P] 单元测试 - RoleService `services/user-service/src/test/java/com/example/user/service/RoleServiceTest.java` — ✅ 已创建

**Checkpoint**: 模块可部署，测试通过

---

## Summary

| Metric | Value |
|--------|-------|
| **Total Tasks** | 52 |
| **Completed** | 52 |
| **Remaining** | 0 |
| **Completion** | 100% |
| **Parallel Tasks** | 28 |
| **Phases** | 7 |
| **Duration** | 4 weeks |

### Service 层重构说明

所有 Service 已重构为接口+Impl模式（在 service/impl/ 子包下）。

### MVP Scope

- Phase 1-2: Setup + Foundation
- Phase 3: US1 用户管理

**MVP Tasks**: 24 tasks
**MVP Duration**: ~2 weeks
