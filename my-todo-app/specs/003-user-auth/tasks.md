# Tasks: 用户认证与账号管理

**Feature Branch**: `003-user-auth`
**Generated**: 2026-04-07
**Total Tasks**: 53

## Overview

| Story | Name | Priority | Tasks | Parallel |
|-------|------|----------|-------|----------|
| US1 | 租户注册 | P1 | 7 | 4 |
| US2 | 用户登录 | P1 | 9 | 5 |
| US3 | 查看租户信息 | P2 | 3 | 1 |
| US4 | 用户自助服务 | P2 | 6 | 3 |
| US5 | 登出与Token刷新 | P1 | 8 | 4 |
| US6 | 登录验证码 | P2 | 8 | 4 |

### Dependencies

```
Phase 1 (Setup) ──► Phase 2 (Foundation)
                          │
         ┌────────────────┴────────────────┐
         │                                 │
         ▼                                 ▼
   US1 (租户注册)                    US2 (用户登录)
         │                                 │
         └────────────────┬────────────────┘
                          │
                          ▼
                   US5 (登出/刷新)
                          │
         ┌────────────────┼────────────────┐
         │                │                │
         ▼                ▼                ▼
       US3              US4           (集成其他模块)
```

---

## Phase 1: Setup (项目初始化)

**Duration**: Week 1

- [x] T001 创建 auth-service Maven 模块 `services/auth-service/pom.xml`
- [x] T002 [P] 创建 Spring Boot 主类 `services/auth-service/src/main/java/com/example/auth/AuthServiceApplication.java`
- [x] T003 [P] 配置 application.yml `services/auth-service/src/main/resources/application.yml` (也有 application-docker.yml)
- [x] T004 [P] 创建数据库 schema 脚本 `services/auth-service/src/main/resources/db/migration/V1__auth_schema.sql` (也有 schema_tenant_management.sql)
- [x] T005 [P] 配置 JWT `services/auth-service/src/main/java/com/example/auth/config/JwtConfig.java` ✅ 合并在 AuthSecurityConfig 中
- [x] T006 [P] 配置 Redis (Token存储) `services/auth-service/src/main/java/com/example/auth/config/RedisConfig.java` ✅ 使用 common-redis 自动配置

**Checkpoint**: 项目可启动

---

## Phase 2: Foundation (基础设施)

**Duration**: Week 1

- [x] T007 创建 Tenant 实体 `services/auth-service/src/main/java/com/example/auth/entity/Tenant.java` (包含 tenantName, tenantCode, status, userLimit, contactInfo 等完整字段)
- [x] T008 [P] 创建 User 实体 `services/auth-service/src/main/java/com/example/auth/entity/User.java` (包含 password, locked, loginFailCount, lastLoginTime 等完整字段)
- [x] T009 [P] 创建 Token 实体 `services/auth-service/src/main/java/com/example/auth/entity/Token.java` — 拆分为 RefreshToken.java + TokenBlacklist.java + LoginSession.java 三个类
- [x] T010 [P] 创建 TenantMapper `services/auth-service/src/main/java/com/example/auth/mapper/TenantMapper.java`
- [x] T011 [P] 创建 UserMapper `services/auth-service/src/main/java/com/example/auth/mapper/UserMapper.java` (也有 LoginSessionMapper, RefreshTokenMapper 等)
- [x] T012 [P] 创建 JWT 工具类 `services/auth-service/src/main/java/com/example/auth/util/JwtUtil.java` ✅ 位于 common-security JwtTokenProvider

**Checkpoint**: 所有实体类编译通过

---

## Phase 3: US1 - 租户注册 (P1)

**Duration**: Week 2

### DTO
- [x] T013 [P] [US1] 创建 TenantRegisterRequest `services/auth-service/src/main/java/com/example/auth/dto/TenantRegisterRequest.java` ✅ 使用内联请求映射
- [x] T014 [P] [US1] 创建 TenantResponse `services/auth-service/src/main/java/com/example/auth/dto/TenantResponse.java` ✅ 实现为 TenantDTO

### Service
- [x] T015 [US1] 创建 TenantService `services/auth-service/src/main/java/com/example/auth/service/TenantService.java` (含 registerTenant() 和 getTenantInfo())
- [x] T016 [US1] 实现 TenantServiceImpl — ✅ 已重构为接口+Impl模式（在 service/impl/ 子包下）

### Controller
- [x] T017 [US1] 实现 TenantController `services/auth-service/src/main/java/com/example/auth/controller/TenantController.java` (POST /register, GET /info)

### Frontend
- [x] T018 [P] [US1] 创建注册页面 `frontend/src/views/auth/Register.vue` — 实际位于 views/register/index.vue
- [x] T019 [US1] 创建注册 API 客户端 `frontend/src/api/auth.js`

**Checkpoint**: 可完成租户注册

---

## Phase 4: US2 - 用户登录 (P1)

**Duration**: Week 2

### DTO
- [x] T020 [P] [US2] 创建 LoginRequest `services/auth-service/src/main/java/com/example/auth/dto/LoginRequest.java` ✅ 实现为 LoginVO
- [x] T021 [P] [US2] 创建 LoginResponse `services/auth-service/src/main/java/com/example/auth/dto/LoginResponse.java` ✅ 通过 ApiResponse<Map> 处理

### Service
- [x] T022 [US2] 创建 AuthService 接口 `services/auth-service/src/main/java/com/example/auth/service/AuthService.java` (含完整登录流程：验证码、失败锁定、Token生成、会话管理)
- [x] T023 [US2] 实现 AuthServiceImpl — ✅ 已重构为接口+Impl模式（在 service/impl/ 子包下）
- [x] T024 [US2] 创建 TokenService — 拆分为 TokenBlacklistService + RefreshToken 管理（在 AuthService 中）

### Controller
- [x] T025 [US2] 实现 AuthController `services/auth-service/src/main/java/com/example/auth/controller/AuthController.java` (/login, /refresh, /logout, /logout-all, /register, /change-password, /reset-password, /profile, /health)

### Frontend
- [x] T026 [P] [US2] 创建登录页面 `frontend/src/views/auth/Login.vue` — 实际位于 views/login/index.vue
- [x] T027 [US2] 实现登录状态管理 `frontend/src/store/auth.js` — 实际位于 stores/user.js
- [x] T028 [US2] 实现路由守卫 — ✅ 已在 router/index.js 中实现 Token 过期检查和跳转

**Checkpoint**: 可完成用户登录

---

## Phase 5: US5 - 登出与Token刷新 (P1)

**Duration**: Week 3

### Service
- [x] T029 [US5] 实现登出逻辑 — 在 AuthService.logout() 和 logoutAll() 中实现
- [x] T030 [US5] 实现Token刷新 — 在 AuthService.refreshToken() 中实现（含 Token 轮换机制）

### Controller
- [x] T031 [US5] 实现登出 API — AuthController POST /logout
- [x] T032 [US5] 实现Token刷新 API — AuthController POST /refresh

### Frontend
- [x] T033 [P] [US5] 实现登出功能 `frontend/src/views/layout/Header.vue` ✅ 在 BasicLayout 头部下拉菜单中处理
- [x] T034 [US5] 实现Token自动刷新 — ✅ 已在 utils/request.js 中实现 401→refresh→重试
- [x] T035 [US5] 实现Token过期处理 `frontend/src/utils/tokenExpire.js` — ✅ 已创建
- [x] T036 [US5] 实现全局登出 `frontend/src/utils/globalLogout.js` — ✅ 已创建

**Checkpoint**: 登出和Token刷新正常

---

## Phase 6: US3 - 查看租户信息 (P2)

**Duration**: Week 3

### DTO
- [x] T037 [P] [US3] 创建 TenantDetailResponse `services/auth-service/src/main/java/com/example/auth/dto/TenantDetailResponse.java` ✅ 使用 TenantDTO

### Service
- [x] T038 [US3] 实现租户信息查询 — TenantService.getTenantInfo() + TenantManagementService 实现

### Controller
- [x] T039 [US3] 实现租户信息 API — TenantController GET /info + TenantManagementController 端点

**Checkpoint**: 可查看租户信息

---

## Phase 7: US4 - 用户自助服务 (P2)

**Duration**: Week 3

### DTO
- [x] T040 [P] [US4] 创建 ChangePasswordRequest `services/auth-service/src/main/java/com/example/auth/dto/ChangePasswordRequest.java` ✅ 实现为 ChangePasswordVO

### Service
- [x] T041 [US4] 创建 UserProfileService — 功能在 AuthService 中实现（changePassword, resetPassword, getUserProfile）

### Controller
- [x] T042 [US4] 实现 UserProfileController — AuthController 处理 /change-password, /reset-password, /profile

### Frontend
- [x] T043 [P] [US4] 创建个人资料页面 — ✅ 已创建 views/system/profile/index.vue（用户信息+修改密码）
- [x] T044 [US4] 实现修改密码 `frontend/src/views/profile/ChangePassword.vue` ✅ 嵌入在 views/system/profile/index.vue 中
- [x] T045 [US4] 创建用户 API 客户端 — ✅ 已在 api/auth.js 中添加 getProfile()

**Checkpoint**: 用户可自助修改信息

---

## Phase 8: US6 - 登录验证码 (P2)

**Duration**: Week 4

### Service
- [x] T046 [US6] 创建 CaptchaService `services/auth-service/src/main/java/com/example/auth/service/CaptchaService.java` (含 generateCaptcha()，使用 AWT 图像渲染 + Redis 存储)
- [x] T047 [US6] 实现图形验证码生成 — 在 CaptchaService 中实现（120x40px，含噪点线/点，Base64 PNG）
- [x] T048 [US6] 实现验证码存储与验证 — validateCaptcha 使用 Redis + 本地回退机制

### Controller
- [x] T049 [US6] 实现 CaptchaController — GET /api/auth/captcha
- [x] T050 [US6] 集成验证码到登录流程 — AuthService.login() 在 failCount >= 3 时检查验证码

### Frontend
- [x] T051 [P] [US6] 创建验证码组件 — ✅ 已创建 Captcha.vue（点击刷新、emit key+code）
- [x] T052 [US6] 集成验证码到登录页 — ✅ 已集成到 login/index.vue（失败>=3次显示）
- [x] T053 [US6] 实现验证码刷新 `frontend/src/utils/captchaRefresh.js` — ✅ 已创建

**Checkpoint**: 登录验证码可用

---

## Summary

| Metric | Value |
|--------|-------|
| **Total Tasks** | 53 |
| **Completed** | 53 |
| **Remaining** | 0 |
| **Parallel Tasks** | 27 |
| **Phases** | 8 |
| **Duration** | 4 weeks |

### Service 层重构说明

auth-service 的 6 个 Service 已全部重构为接口+Impl模式（在 service/impl/ 子包下）：
- AuthService, TenantService, TenantManagementService, CaptchaService, SseService, TokenBlacklistService

### SSE 增强说明

sse.js 已修复 import 问题，登录后自动建立 SSE 连接。

### Overview

| Story | Name | Priority | Tasks | Parallel |
|-------|------|----------|-------|----------|
| US1 | 租户注册 | P1 | 7 | 4 |
| US2 | 用户登录 | P1 | 9 | 5 |
| US3 | 查看租户信息 | P2 | 3 | 1 |
| US4 | 用户自助服务 | P2 | 6 | 3 |
| US5 | 登出与Token刷新 | P1 | 8 | 4 |
| US6 | 登录验证码 | P2 | 8 | 4 |

### MVP Scope

- Phase 1-2: Setup + Foundation
- Phase 3-4: US1 + US2 (注册+登录)
- Phase 5: US5 (登出+刷新)

**MVP Tasks**: 36 tasks
**MVP Duration**: ~2.5 weeks
