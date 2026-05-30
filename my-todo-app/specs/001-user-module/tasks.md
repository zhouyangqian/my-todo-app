# Tasks: 用户模块

**Feature Branch**: `001-user-module`
**Generated**: 2026-04-07
**Total Tasks**: 52

## Overview

| Story | Name | Priority | Tasks | Done | Progress |
|-------|------|----------|-------|------|----------|
| Setup | 项目初始化 | P1 | 6 | 4 | 67% |
| Foundation | 基础设施 | P1 | 7 | 6 | 86% |
| US1 | 租户用户管理 | P1 | 11 | 7 | 64% |
| US2 | 用户权限分配 | P2 | 9 | 5 | 56% |
| US3 | 批量用户操作 | P2 | 6 | 5 | 83% |
| US4 | 用户导入/导出 | P2 | 7 | 3 | 43% |
| Polish | 收尾 | P2 | 6 | 2 | 33% |
| **Total** | | | **52** | **32** | **62%** |

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
- [ ] T005 [P] 配置 MyBatis-Plus 租户插件 `services/user-service/src/main/java/com/example/user/config/MyBatisPlusConfig.java` — 未实现（仅存在 TenantInterceptor 和 WebConfig）
- [ ] T006 [P] 配置 Redis `services/user-service/src/main/java/com/example/user/config/RedisConfig.java` — 未实现

**Checkpoint**: 项目可启动，数据库表创建成功

---

## Phase 2: Foundation (基础设施)

**Duration**: Week 1

- [x] T007 创建 User 实体 `services/user-service/src/main/java/com/example/user/entity/User.java` — 包含完整字段
- [x] T008 [P] 创建 Role 实体 `services/user-service/src/main/java/com/example/user/entity/Role.java` — 包含完整字段
- [x] T009 [P] 创建 UserRole 实体 `services/user-service/src/main/java/com/example/user/entity/UserRole.java`
- [ ] T010 [P] 创建 UserPermission 实体 `services/user-service/src/main/java/com/example/user/entity/UserPermission.java` — 未实现
- [x] T011 [P] 创建 AuditLog 实体 `services/user-service/src/main/java/com/example/user/entity/AuditLog.java` — 包含完整字段
- [x] T012 [P] 创建 UserMapper `services/user-service/src/main/java/com/example/user/mapper/UserMapper.java`
- [x] T013 [P] 创建 RoleMapper `services/user-service/src/main/java/com/example/user/mapper/RoleMapper.java`

**Checkpoint**: 所有实体类编译通过

---

## Phase 3: US1 - 租户用户管理 (P1)

**Duration**: Week 2

### DTO
- [ ] T014 [P] [US1] 创建 UserCreateRequest `services/user-service/src/main/java/com/example/user/dto/UserCreateRequest.java` — 未实现（dto 包不存在）
- [ ] T015 [P] [US1] 创建 UserUpdateRequest `services/user-service/src/main/java/com/example/user/dto/UserUpdateRequest.java` — 未实现（dto 包不存在）
- [ ] T016 [P] [US1] 创建 UserResponse `services/user-service/src/main/java/com/example/user/dto/UserResponse.java` — 未实现（dto 包不存在）
- [ ] T017 [P] [US1] 创建 UserQueryRequest `services/user-service/src/main/java/com/example/user/dto/UserQueryRequest.java` — 未实现（dto 包不存在）

### Service
- [x] T018 [US1] 创建 UserService 接口 `services/user-service/src/main/java/com/example/user/service/UserService.java` — 包含 getUserPage、createUser、updateUser、deleteUser 等方法
- [x] T019 [US1] 实现 UserServiceImpl `services/user-service/src/main/java/com/example/user/service/impl/UserServiceImpl.java` — 功能直接实现在 UserService.java 中（项目使用具体类而非 interface+impl 模式）
- [x] T020 [US1] 创建 AuditLogService `services/user-service/src/main/java/com/example/user/service/AuditLogService.java` — 包含 log() 和 getAuditLogPage()

### Controller
- [x] T021 [US1] 实现 UserController `services/user-service/src/main/java/com/example/user/controller/UserController.java` — 包含 10 个 REST 端点

### Frontend
- [x] T022 [P] [US1] 创建用户列表页面 `frontend/src/views/user/UserList.vue` — 实际位于 `views/system/user/index.vue`
- [ ] T023 [P] [US1] 创建用户表单组件 `frontend/src/views/user/UserForm.vue` — 未作为独立组件实现（内联在用户列表页面中）
- [x] T024 [US1] 创建用户 API 客户端 `frontend/src/api/user.js`

**Checkpoint**: 可完成用户 CRUD 全流程

---

## Phase 4: US2 - 用户权限分配 (P2)

**Duration**: Week 3

### DTO
- [ ] T025 [P] [US2] 创建 RoleCreateRequest `services/user-service/src/main/java/com/example/user/dto/RoleCreateRequest.java` — 未实现（dto 包不存在）
- [ ] T026 [P] [US2] 创建 AssignRoleRequest `services/user-service/src/main/java/com/example/user/dto/AssignRoleRequest.java` — 未实现（dto 包不存在）

### Service
- [x] T027 [US2] 创建 RoleService 接口 `services/user-service/src/main/java/com/example/user/service/RoleService.java` — 包含完整 CRUD + assignRolesToUser
- [x] T028 [US2] 实现 RoleServiceImpl `services/user-service/src/main/java/com/example/user/service/impl/RoleServiceImpl.java` — 功能直接实现在 RoleService.java 中

### Controller
- [x] T029 [US2] 实现 RoleController `services/user-service/src/main/java/com/example/user/controller/RoleController.java` — 包含 12 个端点
- [x] T030 [US2] 实现权限分配 API `services/user-service/src/main/java/com/example/user/controller/UserPermissionController.java` — 功能通过 RoleController.assignRolesToUser 端点实现

### Frontend
- [x] T031 [P] [US2] 创建角色管理页面 `frontend/src/views/user/RoleManage.vue` — 实际位于 `views/system/role-manage/index.vue`
- [ ] T032 [P] [US2] 创建权限选择组件 `frontend/src/components/PermissionSelect.vue` — 未实现
- [ ] T033 [US2] 创建角色 API 客户端 `frontend/src/api/role.js` — 未实现（角色 API 包含在 api/user.js 中）

**Checkpoint**: 可为用户分配角色和权限

---

## Phase 5: US3 - 批量用户操作 (P2)

**Duration**: Week 3

### Service
- [x] T034 [US3] 创建 BatchOperationService `services/user-service/src/main/java/com/example/user/service/BatchOperationService.java` — 包含 batchDisable、batchDelete、assignRoles

### Controller
- [x] T035 [US3] 实现 BatchOperationController `services/user-service/src/main/java/com/example/user/controller/BatchOperationController.java` — 包含 3 个端点

### Frontend
- [ ] T036 [P] [US3] 创建批量操作组件 `frontend/src/components/BatchActions.vue` — 未实现
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
- [ ] T042 [US4] 实现模板下载 API `services/user-service/src/main/java/com/example/user/controller/TemplateController.java` — 未作为独立控制器实现（已合并到 UserImportExportController 中）

### Frontend
- [ ] T043 [P] [US4] 创建导入导出页面 `frontend/src/views/user/UserImport.vue` — 未实现
- [ ] T044 [US4] 实现文件上传组件 `frontend/src/components/FileUpload.vue` — 未实现
- [x] T045 [US4] 实现导出功能 `frontend/src/views/user/UserList.vue` — 实际在 UserImportExportController.exportUsers 中实现

### Templates
- [ ] T046 [US4] 创建用户导入模板 `services/user-service/src/main/resources/templates/user-import-template.xlsx` — 未作为文件存在于磁盘（在代码中动态生成）

**Checkpoint**: 可导入导出用户

---

## Phase 7: Polish (收尾)

**Duration**: Week 4

- [ ] T047 [P] 添加用户缓存 `services/user-service/src/main/java/com/example/user/cache/UserCache.java` — 未实现
- [ ] T048 [P] 添加操作日志切面 `services/user-service/src/main/java/com/example/user/aspect/OperationLogAspect.java` — 未实现
- [x] T049 [P] 创建 Dockerfile `services/user-service/Dockerfile`
- [x] T050 [P] 创建 docker-compose.yml `services/user-service/docker-compose.yml` — 实际位于项目根目录
- [ ] T051 [P] 单元测试 - UserService `services/user-service/src/test/java/com/example/user/service/UserServiceTest.java` — 未实现（无测试目录）
- [ ] T052 [P] 单元测试 - RoleService `services/user-service/src/test/java/com/example/user/service/RoleServiceTest.java` — 未实现

**Checkpoint**: 模块可部署，测试通过

---

## Summary

| Metric | Value |
|--------|-------|
| **Total Tasks** | 52 |
| **Completed** | 32 |
| **Remaining** | 20 |
| **Completion** | 62% |
| **Parallel Tasks** | 28 |
| **Phases** | 7 |
| **Duration** | 4 weeks |

### MVP Scope

- Phase 1-2: Setup + Foundation
- Phase 3: US1 用户管理

**MVP Tasks**: 24 tasks
**MVP Duration**: ~2 weeks
