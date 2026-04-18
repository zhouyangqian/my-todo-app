# Tasks: 用户认证与账号管理

**Feature Branch**: `003-user-auth`
**Generated**: 2026-04-07
**Total Tasks**: 45

## Overview

| Story | Name | Priority | Tasks | Parallel |
|-------|------|----------|-------|----------|
| US1 | 租户注册 | P1 | 10 | 5 |
| US2 | 用户登录 | P1 | 12 | 6 |
| US3 | 查看租户信息 | P2 | 6 | 3 |
| US4 | 用户自助服务 | P2 | 6 | 3 |
| US5 | 登出与Token刷新 | P1 | 8 | 4 |

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

- [ ] T001 创建 auth-service Maven 模块 `services/auth-service/pom.xml`
- [ ] T002 [P] 创建 Spring Boot 主类 `services/auth-service/src/main/java/com/example/auth/AuthServiceApplication.java`
- [ ] T003 [P] 配置 application.yml `services/auth-service/src/main/resources/application.yml`
- [ ] T004 [P] 创建数据库 schema 脚本 `services/auth-service/src/main/resources/db/migration/V1__auth_schema.sql`
- [ ] T005 [P] 配置 JWT `services/auth-service/src/main/java/com/example/auth/config/JwtConfig.java`
- [ ] T006 [P] 配置 Redis (Token存储) `services/auth-service/src/main/java/com/example/auth/config/RedisConfig.java`

**Checkpoint**: 项目可启动

---

## Phase 2: Foundation (基础设施)

**Duration**: Week 1

- [ ] T007 创建 Tenant 实体 `services/auth-service/src/main/java/com/example/auth/entity/Tenant.java`
- [ ] T008 [P] 创建 User 实体 `services/auth-service/src/main/java/com/example/auth/entity/User.java`
- [ ] T009 [P] 创建 Token 实体 `services/auth-service/src/main/java/com/example/auth/entity/Token.java`
- [ ] T010 [P] 创建 TenantMapper `services/auth-service/src/main/java/com/example/auth/mapper/TenantMapper.java`
- [ ] T011 [P] 创建 UserMapper `services/auth-service/src/main/java/com/example/auth/mapper/UserMapper.java`
- [ ] T012 [P] 创建 JWT 工具类 `services/auth-service/src/main/java/com/example/auth/util/JwtUtil.java`

**Checkpoint**: 所有实体类编译通过

---

## Phase 3: US1 - 租户注册 (P1)

**Duration**: Week 2

### DTO
- [ ] T013 [P] [US1] 创建 TenantRegisterRequest `services/auth-service/src/main/java/com/example/auth/dto/TenantRegisterRequest.java`
- [ ] T014 [P] [US1] 创建 TenantResponse `services/auth-service/src/main/java/com/example/auth/dto/TenantResponse.java`

### Service
- [ ] T015 [US1] 创建 TenantService `services/auth-service/src/main/java/com/example/auth/service/TenantService.java`
- [ ] T016 [US1] 实现 TenantServiceImpl `services/auth-service/src/main/java/com/example/auth/service/impl/TenantServiceImpl.java`

### Controller
- [ ] T017 [US1] 实现 TenantController `services/auth-service/src/main/java/com/example/auth/controller/TenantController.java`

### Frontend
- [ ] T018 [P] [US1] 创建注册页面 `frontend/src/views/auth/Register.vue`
- [ ] T019 [US1] 创建注册 API 客户端 `frontend/src/api/auth.js`

**Checkpoint**: 可完成租户注册

---

## Phase 4: US2 - 用户登录 (P1)

**Duration**: Week 2

### DTO
- [ ] T020 [P] [US2] 创建 LoginRequest `services/auth-service/src/main/java/com/example/auth/dto/LoginRequest.java`
- [ ] T021 [P] [US2] 创建 LoginResponse `services/auth-service/src/main/java/com/example/auth/dto/LoginResponse.java`

### Service
- [ ] T022 [US2] 创建 AuthService 接口 `services/auth-service/src/main/java/com/example/auth/service/AuthService.java`
- [ ] T023 [US2] 实现 AuthServiceImpl `services/auth-service/src/main/java/com/example/auth/service/impl/AuthServiceImpl.java`
- [ ] T024 [US2] 创建 TokenService `services/auth-service/src/main/java/com/example/auth/service/TokenService.java`

### Controller
- [ ] T025 [US2] 实现 AuthController `services/auth-service/src/main/java/com/example/auth/controller/AuthController.java`

### Frontend
- [ ] T026 [P] [US2] 创建登录页面 `frontend/src/views/auth/Login.vue`
- [ ] T027 [US2] 实现登录状态管理 `frontend/src/store/auth.js`
- [ ] T028 [US2] 实现路由守卫 `frontend/src/router/guards.js`

**Checkpoint**: 可完成用户登录

---

## Phase 5: US5 - 登出与Token刷新 (P1)

**Duration**: Week 3

### Service
- [ ] T029 [US5] 实现登出逻辑 `services/auth-service/src/main/java/com/example/auth/service/impl/LogoutServiceImpl.java`
- [ ] T030 [US5] 实现Token刷新 `services/auth-service/src/main/java/com/example/auth/service/impl/TokenRefreshServiceImpl.java`

### Controller
- [ ] T031 [US5] 实现登出 API `services/auth-service/src/main/java/com/example/auth/controller/LogoutController.java`
- [ ] T032 [US5] 实现Token刷新 API `services/auth-service/src/main/java/com/example/auth/controller/TokenController.java`

### Frontend
- [ ] T033 [P] [US5] 实现登出功能 `frontend/src/views/layout/Header.vue`
- [ ] T034 [US5] 实现Token自动刷新 `frontend/src/utils/tokenRefresh.js`
- [ ] T035 [US5] 实现Token过期处理 `frontend/src/utils/tokenExpire.js`
- [ ] T036 [US5] 实现全局登出 `frontend/src/utils/globalLogout.js`

**Checkpoint**: 登出和Token刷新正常

---

## Phase 6: US3 - 查看租户信息 (P2)

**Duration**: Week 3

### DTO
- [ ] T037 [P] [US3] 创建 TenantDetailResponse `services/auth-service/src/main/java/com/example/auth/dto/TenantDetailResponse.java`

### Service
- [ ] T038 [US3] 实现租户信息查询 `services/auth-service/src/main/java/com/example/auth/service/impl/TenantQueryServiceImpl.java`

### Controller
- [ ] T039 [US3] 实现租户信息 API `services/auth-service/src/main/java/com/example/auth/controller/TenantInfoController.java`

**Checkpoint**: 可查看租户信息

---

## Phase 7: US4 - 用户自助服务 (P2)

**Duration**: Week 3

### DTO
- [ ] T040 [P] [US4] 创建 ChangePasswordRequest `services/auth-service/src/main/java/com/example/auth/dto/ChangePasswordRequest.java`

### Service
- [ ] T041 [US4] 创建 UserProfileService `services/auth-service/src/main/java/com/example/auth/service/UserProfileService.java`

### Controller
- [ ] T042 [US4] 实现 UserProfileController `services/auth-service/src/main/java/com/example/auth/controller/UserProfileController.java`

### Frontend
- [ ] T043 [P] [US4] 创建个人资料页面 `frontend/src/views/profile/UserProfile.vue`
- [ ] T044 [US4] 实现修改密码 `frontend/src/views/profile/ChangePassword.vue`
- [ ] T045 [US4] 创建用户 API 客户端 `frontend/src/api/profile.js`

**Checkpoint**: 用户可自助修改信息

---

## Phase 8: US6 - 登录验证码 (P2)

**Duration**: Week 4

### Service
- [ ] T046 [US6] 创建 CaptchaService `services/auth-service/src/main/java/com/example/auth/service/CaptchaService.java`
- [ ] T047 [US6] 实现图形验证码生成 `services/auth-service/src/main/java/com/example/auth/service/impl/CaptchaServiceImpl.java`
- [ ] T048 [US6] 实现验证码存储与验证 `services/auth-service/src/main/java/com/example/auth/service/impl/CaptchaValidator.java`

### Controller
- [ ] T049 [US6] 实现 CaptchaController `services/auth-service/src/main/java/com/example/auth/controller/CaptchaController.java`
- [ ] T050 [US6] 集成验证码到登录流程 `services/auth-service/src/main/java/com/example/auth/controller/AuthController.java` (更新)

### Frontend
- [ ] T051 [P] [US6] 创建验证码组件 `frontend/src/components/Captcha.vue`
- [ ] T052 [US6] 集成验证码到登录页 `frontend/src/views/auth/Login.vue` (更新)
- [ ] T053 [US6] 实现验证码刷新 `frontend/src/utils/captchaRefresh.js`

**Checkpoint**: 登录验证码可用

---

## Summary

| Metric | Value |
|--------|-------|
| **Total Tasks** | 53 |
| **Parallel Tasks** | 27 |
| **Phases** | 8 |
| **Duration** | 4 weeks |

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
