# Implementation Plan: 权限模块 (Permission Module)

**Branch**: `002-permission-module` | **Date**: 2026-01-10 | **Spec**: [spec.md](./spec.md)
**Input**: Feature specification from `/specs/002-permission-module/spec.md`

## Summary

本功能实现多租户 SaaS 系统的权限控制模块，包括接口权限、数据权限、角色管理、权限模板、角色继承、实时会话管理和黑名单功能。核心目标是确保系统安全、数据隔离和灵活的权限配置。

**技术方案**：
- 后端：Spring Boot 3.0 + MyBatis-Plus + Spring AOP 实现权限控制
- 前端：Vue 3.0 + Ant Design 6.1.4 实现权限管理界面
- 权限验证：基于注解的 AOP 拦截器
- 数据权限：MyBatis-Plus 拦截器实现自动 SQL 过滤
- 会话管理：WebSocket + Redis 实现实时通知
- 黑名单：Redis 缓存实现快速验证

## Technical Context

**Language/Version**: Java 17+, JavaScript ES6+
**Primary Dependencies**: Spring Boot 3.0, MyBatis-Plus 3.5+, Spring AOP, WebSocket, Vue 3.0, Ant Design 6.1.4
**Storage**: MySQL 8.0+ (权限数据), Redis 7.0+ (会话/缓存/黑名单)
**Testing**: JUnit 5, Mockito, WebSocket Client (可选)
**Target Platform**: Linux server (后端), 现代浏览器 (前端)
**Project Type**: web (前端 + 后端微服务)
**Performance Goals**:
- 权限验证响应: <10ms
- 数据权限过滤: <50ms
- 角色继承同步: <5秒
- 挤号登录通知: <2秒
- 黑名单验证: <5ms
**Constraints**:
- 角色继承最多 5 级
- 批量操作最多 100 个权限
- 权限代码必须唯一
- 循环继承必须被检测和阻止
**Scale/Scope**: 支持 10,000 并发权限验证，支持 1000+ 权限定义

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

**宪章**：多租户 SaaS 系统 (v1.2.2)

### 必需的合规性检查

- [x] **简洁与用户体验**：使用权限模板简化角色创建，数据权限自动过滤无需手动配置
- [x] **多租户隔离**：所有权限数据和缓存带租户前缀，数据权限自动添加租户/部门过滤
- [x] **基于权限的访问**：所有 API 通过 @RequiresPermission 注解保护，权限变更实时生效
- [x] **API 优先集成**：前端只通过网关调用权限服务，API 契约定义在 contracts/ 目录
- [x] **组件可复用性**：使用 Ant Design Tree、Transfer、Select 组件实现权限选择和配置
- [x] **数据一致性**：权限变更使用事务，角色继承变更同步更新，黑名单操作记录审计日志
- [x] **可观测性**：日志包含租户 ID、用户 ID、权限验证结果，支持按租户过滤和查询
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
specs/002-permission-module/
├── plan.md              # This file
├── research.md          # Phase 0: 技术研究
├── data-model.md        # Phase 1: 数据模型
├── quickstart.md        # Phase 1: 快速开始指南
├── contracts/           # Phase 1: API 契约
│   ├── permission-api.yaml    # 权限管理 API
│   ├── role-api.yaml          # 角色管理 API
│   ├── session-api.yaml       # 会话管理 API
│   └── blacklist-api.yaml     # 黑名单 API
└── tasks.md             # Phase 2: 任务分解 (由 /speckit.tasks 生成)
```

### Source Code (repository root)

```text
gateway/                          # Spring Cloud Gateway 网关
├── src/main/resources/
│   └── application.yml          # 路由配置：/api/v1/permissions -> permission-service
└── pom.xml

services/
├── permission-service/           # 权限服务 (本功能核心)
│   ├── src/main/java/com/example/permission/
│   │   ├── controller/
│   │   │   ├── PermissionController.java      # 权限 CRUD
│   │   │   ├── RoleController.java            # 角色管理
│   │   │   ├── SessionController.java         # 会话管理
│   │   │   └── BlacklistController.java       # 黑名单管理
│   │   ├── service/
│   │   │   ├── PermissionService.java
│   │   │   ├── RoleService.java
│   │   │   ├── PermissionTemplateService.java
│   │   │   ├── SessionService.java
│   │   │   └── BlacklistService.java
│   │   ├── aspect/
│   │   │   ├── PermissionAspect.java           # 权限验证 AOP
│   │   │   └── DataPermissionAspect.java       # 数据权限 AOP
│   │   ├── interceptor/
│   │   │   └── DataPermissionInterceptor.java # MyBatis-Plus 拦截器
│   │   ├── annotation/
│   │   │   ├── RequiresPermission.java         # 权限注解
│   │   │   └── DataPermission.java             # 数据权限注解
│   │   ├── messaging/
│   │   │   ├── WebSocketHandler.java           # WebSocket 处理
│   │   │   └── SessionNotifier.java             # 会话通知
│   │   ├── config/
│   │   │   ├── PermissionConfig.java
│   │   │   └── WebSocketConfig.java
│   │   └── entity/
│   │       ├── Permission.java
│   │       ├── Role.java
│   │       ├── PermissionTemplate.java
│   │       ├── Session.java
│   │       └── Blacklist.java
│   └── pom.xml
│
frontend/
├── src/
│   ├── views/permission/       # 权限管理页面
│   │   ├── PermissionList.vue   # 权限列表
│   │   ├── RoleList.vue        # 角色列表
│   │   ├── RoleForm.vue        # 角色表单
│   │   ├── SessionList.vue     # 会话列表
│   │   └── Blacklist.vue       # 黑名单管理
│   ├── components/
│   │   ├── PermissionTree.vue   # 权限树组件
│   │   ├── RolePermissionSelector.vue # 角色-权限选择器
│   │   └── DataPermissionConfig.vue  # 数据权限配置
│   └── api/
│       ├── permission.js
│       └── role.js
└── package.json
```

**架构决策**：permission-service 作为独立微服务，提供权限验证注解和 API，其他服务通过依赖调用权限验证

## Complexity Tracking

无需复杂度跟踪 - 无宪章违规。

---

## Phase 0: Research & Technology Decisions

### Research Topics

1. **Spring AOP 权限验证**: 研究如何实现基于注解的权限拦截器
2. **MyBatis-Plus 数据权限**: 研究如何实现 SQL 拦截器自动添加数据过滤条件
3. **WebSocket 会话通知**: 研究 WebSocket 消息推送和前端集成
4. **Redis 黑名单缓存**: 研究黑名单缓存设计和性能优化
5. **角色继承算法**: 研究循环继承检测和权限合并策略

---

## Phase 1: Design Artifacts

### Data Model

详见 [data-model.md](./data-model.md)

**核心实体**：
- **Permission**: 权限表 (permission_id, code, name, parent_id)
- **Role**: 角色表 (role_id, name, code, parent_id, tenant_id)
- **PermissionTemplate**: 权限模板表 (template_id, name, tenant_id, is_system)
- **DataPermissionRule**: 数据权限规则表 (rule_id, user_id, type, target_id)
- **Session**: 会话表 (session_id, user_id, tenant_id, device_info, status)
- **Blacklist**: 黑名单表 (id, user_id, tenant_id, reason, status)

### API Contracts

详见 [contracts/](./contracts/) 目录：
- **permission-api.yaml**: 权限 CRUD API
- **role-api.yaml**: 角色 CRUD、权限分配、角色继承 API
- **session-api.yaml**: 会话查询、强制下线 API
- **blacklist-api.yaml**: 黑名单添加/移除 API

### Quick Start Guide

详见 [quickstart.md](./quickstart.md)

包含：
- 依赖配置
- 权限注解使用示例
- 数据权限配置示例
- WebSocket 集成示例
- API 使用示例

---

## Dependencies on Other Features

- **003-user-auth**: 提供用户身份认证、JWT Token 验证、登录/登出
- **001-user-module**: 提供用户管理、角色分配

---

## Next Steps

运行 `/speckit.tasks` 生成详细的任务分解清单。
