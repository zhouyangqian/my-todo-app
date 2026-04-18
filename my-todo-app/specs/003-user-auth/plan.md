# Implementation Plan: 用户认证与账号管理 (User Authentication & Account Management)

**Branch**: `003-user-auth` | **Date**: 2026-01-10 | **Spec**: [spec.md](./spec.md)
**Input**: Feature specification from `/specs/003-user-auth/spec.md`

## Summary

本功能实现多租户 SaaS 系统的用户认证与账号管理模块，包括租户注册、用户登录、会话管理、用户自助服务、Token刷新、登录验证码等功能。核心目标是确保系统安全、用户身份验证准确、会话管理可靠。

**技术方案**：
- 后端：Spring Boot 3.0 + Spring Security + JWT Token 实现身份认证
- 前端：Vue 3.0 + Ant Design 6.1.4 实现登录/注册页面
- 密码加密：BCrypt 算法
- 会话管理：JWT Token + Redis 黑名单
- 验证码：EasyCaptcha 或 Kaptcha 图形验证码
- 登录防护：失败次数限制 + 临时锁定机制
- 挤号登录：WebSocket 实时通知

## Technical Context

**Language/Version**: Java 17+, JavaScript ES6+
**Primary Dependencies**: Spring Boot 3.0, Spring Security, JWT, EasyCaptcha/Kaptcha, WebSocket, Vue 3.0, Ant Design 6.1.4
**Storage**: MySQL 8.0+ (用户/租户/日志数据), Redis 7.0+ (会话/黑名单/验证码/失败计数)
**Testing**: JUnit 5, Mockito, WebSocket Client (可选)
**Target Platform**: Linux server (后端), 现代浏览器 (前端)
**Project Type**: web (前端 + 后端微服务)
**Performance Goals**:
- 用户登录响应: <500ms
- Token刷新响应: <200ms
- 验证码生成/验证: <100ms
- 挤号登录通知: <2秒
- 黑名单查询: <10ms
- 租户注册流程: <2分钟
**Constraints**:
- 密码最少8位，必须包含字母和数字
- 连续登录失败5次锁定30分钟
- 验证码有效期5分钟
- Access Token有效期2小时
- Refresh Token有效期7天
- 每个租户默认5个用户上限
**Scale/Scope**: 支持1000并发登录，支持10000+租户

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

**宪章**：多租户 SaaS 系统 (v1.2.2)

### 必需的合规性检查

- [x] **简洁与用户体验**：登录/注册流程简洁，租户注册一次性完成，支持自助密码修改
- [x] **多租户隔离**：所有账号数据和缓存带租户前缀，Token包含租户ID
- [x] **基于权限的访问**：登录/注册API公开，其他API通过JWT Token验证，Token包含权限列表
- [x] **API 优先集成**：前端只通过网关调用认证服务，API契约定义在 contracts/ 目录
- [x] **组件可复用性**：使用 Ant Design Form、Input、Button 组件实现登录/注册表单
- [x] **数据一致性**：租户注册使用事务确保租户和管理员账户同时创建，登出操作记录审计日志
- [x] **可观测性**：日志包含租户 ID、用户 ID、操作类型（登录/登出/注册/修改密码），登录失败记录IP和原因
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
specs/003-user-auth/
├── plan.md              # This file
├── research.md          # Phase 0: 技术研究
├── data-model.md        # Phase 1: 数据模型
├── quickstart.md        # Phase 1: 快速开始指南
├── contracts/           # Phase 1: API 契约
│   ├── tenant-api.yaml         # 租户注册/查询 API
│   ├── auth-api.yaml           # 登录/登出/Token刷新 API
│   ├── session-api.yaml        # 会话管理 API
│   └── captcha-api.yaml        # 验证码 API
└── tasks.md             # Phase 2: 任务分解 (由 /speckit.tasks 生成)
```

### Source Code (repository root)

```text
gateway/                          # Spring Cloud Gateway 网关
├── src/main/resources/
│   └── application.yml          # 路由配置：/api/v1/auth -> auth-service
└── pom.xml

services/
├── auth-service/                 # 认证服务 (本功能核心)
│   ├── src/main/java/com/example/auth/
│   │   ├── controller/
│   │   │   ├── TenantController.java         # 租户注册/查询
│   │   │   ├── AuthController.java           # 登录/登出/Token刷新
│   │   │   ├── SessionController.java        # 会话管理
│   │   │   └── CaptchaController.java        # 验证码生成/验证
│   │   ├── service/
│   │   │   ├── TenantService.java
│   │   │   ├── AuthService.java
│   │   │   ├── TokenService.java
│   │   │   ├── SessionService.java
│   │   │   └── CaptchaService.java
│   │   ├── security/
│   │   │   ├── JwtTokenProvider.java         # JWT Token 生成/验证
│   │   │   ├── PasswordEncoder.java          # 密码加密
│   │   │   └── SecurityFilter.java           # JWT 认证过滤器
│   │   ├── filter/
│   │   │   ├── LoginAttemptFilter.java       # 登录失败限制
│   │   │   └── CaptchaFilter.java            # 验证码过滤器
│   │   ├── messaging/
│   │   │   ├── WebSocketHandler.java         # WebSocket 处理
│   │   │   └── SessionNotifier.java          # 会话通知
│   │   ├── config/
│   │   │   ├── SecurityConfig.java
│   │   │   └── WebSocketConfig.java
│   │   └── entity/
│   │       ├── Tenant.java
│   │       ├── User.java
│   │       ├── LoginLog.java
│   │       ├── TokenBlacklist.java
│   │       └── Captcha.java
│   └── pom.xml
│
frontend/
├── src/
│   ├── views/auth/               # 认证页面
│   │   ├── Login.vue             # 登录页面
│   │   ├── Register.vue          # 租户注册页面
│   │   └── Profile.vue           # 个人资料/密码修改
│   ├── components/
│   │   └── CaptchaInput.vue      # 验证码输入组件
│   └── api/
│       ├── auth.js               # 认证 API
│       └── tenant.js             # 租户 API
└── package.json
```

**架构决策**：auth-service 作为独立微服务，提供身份认证、会话管理、Token刷新、验证码功能，其他服务通过 JWT Token 验证用户身份

## Complexity Tracking

无需复杂度跟踪 - 无宪章违规。

---

## Phase 0: Research & Technology Decisions

### Research Topics

1. **Spring Security + JWT 集成**: 研究 Spring Security 6.0 与 JWT Token 的集成方案
2. **BCrypt 密码加密**: 研究 BCrypt 算法的最佳实践和参数配置
3. **JWT Token 黑名单**: 研究 Redis 黑名单设计和 TTL 管理
4. **EasyCaptcha/Kaptcha**: 研究图形验证码库的选择和配置
5. **登录失败限制**: 研究基于 Redis 的失败计数和锁定机制
6. **WebSocket 挤号通知**: 研究 WebSocket 消息推送和前端集成

---

## Phase 1: Design Artifacts

### Data Model

详见 [data-model.md](./data-model.md)

**核心实体**：
- **Tenant**: 租户表 (tenant_id, name, code, status, user_limit)
- **User**: 用户表 (user_id, username, email, password_hash, tenant_id, status)
- **LoginLog**: 登录日志表 (log_id, user_id, tenant_id, login_time, ip_address, result)
- **TokenBlacklist**: Token黑名单表 (id, token, user_id, expiry_time, reason)
- **Captcha**: 验证码表 (id, code_hash, user_id, expiry_time, used)

### API Contracts

详见 [contracts/](./contracts/) 目录：
- **tenant-api.yaml**: 租户注册/查询 API
- **auth-api.yaml**: 登录/登出/Token刷新 API
- **session-api.yaml**: 会话管理 API
- **captcha-api.yaml**: 验证码生成/验证 API

### Quick Start Guide

详见 [quickstart.md](./quickstart.md)

包含：
- 依赖配置
- JWT Token 配置和使用
- 密码加密配置
- 验证码配置
- 登录失败限制配置
- API 使用示例

---

## Dependencies on Other Features

- **001-user-module**: 提供用户 CRUD、角色分配、批量操作、导入/导出
- **002-permission-module**: 提供权限验证、角色管理、数据权限、会话管理、黑名单

---

## Next Steps

运行 `/speckit.tasks` 生成详细的任务分解清单。
