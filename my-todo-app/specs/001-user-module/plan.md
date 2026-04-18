# Implementation Plan: 用户模块 (User Module)

**Branch**: `001-user-module` | **Date**: 2026-01-10 | **Spec**: [spec.md](./spec.md)
**Input**: Feature specification from `/specs/001-user-module/spec.md`

## Summary

本功能实现多租户 SaaS 系统中的用户管理模块，包括用户 CRUD、角色分配、权限管理、批量操作和 Excel 导入/导出功能。核心目标是确保租户数据隔离、权限控制和操作审计。

**技术方案**：
- 后端：Spring Boot 3.0 + MyBatis-Plus 实现用户管理和权限控制
- 前端：Vue 3.0 + Ant Design 6.1.4 实现用户管理界面
- 数据隔离：基于 MyBatis-Plus 租户插件实现自动租户过滤
- 权限验证：集成 002-permission-module 提供的权限注解和接口

## Technical Context

**Language/Version**: Java 17+, JavaScript ES6+
**Primary Dependencies**: Spring Boot 3.0, MyBatis-Plus 3.5+, Vue 3.0, Ant Design 6.1.4, Apache POI 5.2+
**Storage**: MySQL 8.0+ (用户数据), Redis 7.0+ (缓存/会话)
**Testing**: JUnit 5, Mockito, Vue Test Utils (可选)
**Target Platform**: Linux server (后端), 现代浏览器 (前端)
**Project Type**: web (前端 + 后端微服务)
**Performance Goals**:
- 用户列表查询: <500ms (1000 用户规模)
- 权限变更生效: <1秒
- 批量操作: 100 个用户 <10秒
- 用户导入: 1000 条记录 <30秒
- 用户导出: 10000 条记录 <15秒
**Constraints**:
- 租户用户上限: 默认 5 个用户，可升级
- 批量操作限制: 最多 100 个用户
- 导入记录限制: 最多 1000 条记录
- 软删除: 用户删除仅标记为已删除
**Scale/Scope**: 支持 10,000 用户/租户 (性能基准)，默认 5 用户/租户

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

**宪章**：多租户 SaaS 系统 (v1.2.2)

### 必需的合规性检查

- [x] **简洁与用户体验**：用户界面使用 Ant Design 组件，提供直观的 CRUD 操作和批量操作界面
- [x] **多租户隔离**：所有数据库查询通过 MyBatis-Plus 租户插件自动添加租户过滤，Redis 缓存带租户前缀
- [x] **基于权限的访问**：所有 API 端点通过 002-permission-module 的权限注解保护
- [x] **API 优先集成**：前端只通过 Spring Cloud Gateway 网关调用后端服务，API 契约定义在 contracts/ 目录
- [x] **组件可复用性**：使用 Ant Design Table、Form、Modal 等组件实现用户列表、编辑、批量操作
- [x] **数据一致性**：用户创建、角色分配、权限变更使用事务处理，所有操作记录审计日志
- [x] **可观测性**：日志包含租户 ID、用户 ID、操作类型、结果，支持按租户过滤和查询
- [x] **代码风格一致性**：前端配置 ESLint + Prettier + lint-staged + husky

### 技术栈验证

- [x] 前端：Vue 3.0 + JavaScript + Ant Design 6.1.4
- [x] Node.js：Node.js 18+ LTS
- [x] 前端工具：ESLint + Prettier + lint-staged + husky
- [x] 后端：Spring Boot 3.0 + Java 17+
- [x] ORM：MyBatis-Plus
- [x] 数据库：MySQL 8.0+
- [x] 缓存：Redis 7.0+
- [x] 网关：Spring Cloud Gateway

### 复杂度理由

无需复杂度理由 - 所有合规性检查通过。

## Project Structure

### Documentation (this feature)

```text
specs/001-user-module/
├── plan.md              # This file
├── research.md          # Phase 0: 技术研究
├── data-model.md        # Phase 1: 数据模型
├── quickstart.md        # Phase 1: 快速开始指南
├── contracts/           # Phase 1: API 契约
│   ├── user-api.yaml    # 用户管理 API
│   ├── role-api.yaml    # 角色管理 API
│   └── permission-api.yaml # 权限管理 API
└── tasks.md             # Phase 2: 任务分解 (由 /speckit.tasks 生成)
```

### Source Code (repository root)

```text
gateway/                          # Spring Cloud Gateway 网关
├── src/main/resources/
│   └── application.yml          # 路由配置：/api/v1/users -> user-service
└── pom.xml

services/
├── user-service/                 # 用户管理服务 (本功能核心)
│   ├── src/main/java/com/example/user/
│   │   ├── controller/
│   │   │   ├── UserController.java          # 用户 CRUD
│   │   │   ├── RoleController.java          # 角色管理
│   │   │   ├── UserImportController.java    # 导入/导出
│   │   │   └── BatchOperationController.java # 批量操作
│   │   ├── service/
│   │   │   ├── UserService.java
│   │   │   ├── RoleService.java
│   │   │   ├── UserImportService.java
│   │   │   └── AuditLogService.java
│   │   ├── repository/
│   │   │   ├── UserMapper.java
│   │   │   ├── RoleMapper.java
│   │   │   └── AuditLogMapper.java
│   │   ├── entity/
│   │   │   ├── User.java
│   │   │   ├── Role.java
│   │   │   ├── UserRole.java
│   │   │   ├── UserPermission.java
│   │   │   └── AuditLog.java
│   │   └── config/
│   │       ├── MyBatisPlusConfig.java       # 租户插件配置
│   │       └── RedisConfig.java
│   ├── src/main/resources/
│   │   ├── mapper/
│   │   │   ├── UserMapper.xml
│   │   │   └── RoleMapper.xml
│   │   └── application.yml
│   └── pom.xml
│
├── permission-service/           # 权限服务 (由 002-permission-module 提供)
│   └── (权限验证、角色定义、权限注解)
│
frontend/
├── src/
│   ├── views/user/               # 用户管理页面
│   │   ├── UserList.vue          # 用户列表
│   │   ├── UserForm.vue          # 用户表单
│   │   ├── RoleManage.vue        # 角色管理
│   │   └── UserImport.vue        # 导入/导出
│   ├── components/
│   │   ├── UserTable.vue         # 用户表格组件
│   │   ├── BatchActions.vue      # 批量操作组件
│   │   └── PermissionSelect.vue  # 权限选择组件
│   ├── api/
│   │   ├── user.js               # 用户 API 客户端
│   │   └── role.js               # 角色 API 客户端
│   └── router/
│       └── index.js              # 路由配置
├── package.json
├── .eslintrc.js
├── .prettierrc
└── vite.config.js
```

**架构决策**：多租户 SaaS 架构，user-service 作为独立微服务，通过网关暴露 API，前端 Vue 3 应用调用网关

## Complexity Tracking

无需复杂度跟踪 - 无宪章违规。

---

## Phase 0: Research & Technology Decisions

### Research Topics

1. **MyBatis-Plus 租户插件**: 研究如何配置租户拦截器实现自动租户过滤
2. **Apache POI Excel 处理**: 研究 Java 中 Excel 导入/导出的最佳实践
3. **Vue 3 + Ant Design 批量操作**: 研究如何实现表格批量选择和操作
4. **异步任务处理**: 研究大批量导入的异步处理方案
5. **软删除实现**: 研究 MyBatis-Plus 软删除配置和查询过滤

---

## Phase 1: Design Artifacts

### Data Model

详见 [data-model.md](./data-model.md)

**核心实体**：
- **User**: 用户表 (user_id, username, email, phone, password_hash, tenant_id, status, deleted)
- **Role**: 角色表 (role_id, role_name, description, tenant_id)
- **UserRole**: 用户-角色关联表 (user_id, role_id, tenant_id)
- **UserPermission**: 用户-权限关联表 (user_id, permission_id, tenant_id)
- **AuditLog**: 审计日志表 (log_id, operator_id, target_id, operation_type, tenant_id)

### API Contracts

详见 [contracts/](./contracts/) 目录：
- **user-api.yaml**: 用户 CRUD、批量操作、导入/导出 API
- **role-api.yaml**: 角色 CRUD、权限分配 API
- **permission-api.yaml**: 用户权限查询 API

### Quick Start Guide

详见 [quickstart.md](./quickstart.md)

包含：
- 依赖配置
- 数据库初始化脚本
- API 使用示例
- 前端组件集成示例

---

## Dependencies on Other Features

- **003-user-auth**: 提供身份认证 (JWT Token 验证)、租户注册、用户登录
- **002-permission-module**: 提供权限定义、权限验证注解 (@RequiresPermission)、角色管理

---

## Next Steps

运行 `/speckit.tasks` 生成详细的任务分解清单。
