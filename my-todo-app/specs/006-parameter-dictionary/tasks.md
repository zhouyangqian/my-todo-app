# Tasks: Admin Management Platform

**Input**: Design documents from `/specs/006-parameter-dictionary/`
**Prerequisites**: plan.md, spec.md, research.md, data-model.md, contracts/

**Tests**: Tests are NOT included in this specification (per project constitution, testing is optional unless explicitly required)

**Organization**: Tasks are grouped by module priority to enable independent implementation and testing.

## Format: `[ID] [P?] [Module] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Mxx]**: Which module this task belongs to (M01-M12)
- Include exact file paths in descriptions

## Path Conventions

- **Backend Services**: `services/{service-name}/src/main/java/com/example/{service}/`
- **Frontend**: `frontend/src/`
- **Gateway**: `gateway/src/main/resources/`

---

## Phase 1: Project Setup (Shared Infrastructure)

**Purpose**: Multi-service project initialization and basic structure

- [ ] T001 Create services directory structure for 12 microservices
- [ ] T002 Initialize frontend project with Vue 3.0 + Vite in frontend/
- [ ] T003 [P] Configure ESLint and Prettier in frontend/.eslintrc.js and frontend/.prettierrc
- [ ] T004 [P] Setup lint-staged and husky Git hooks in frontend/package.json
- [ ] T005 [P] Create shared common module for utilities in services/common/src/main/java/com/example/common/
- [ ] T006 [P] Setup Nacos configuration center connection in services/common/src/main/resources/application.yml
- [ ] T007 [P] Create gateway service directory structure in services/gateway-service/
- [ ] T008 Initialize Spring Cloud Gateway in services/gateway-service/pom.xml

---

## Phase 2: Foundational (M02 - Admin Framework) 🎯 P0 CRITICAL

**Purpose**: Core authentication, user management, role management, menu system - ALL other modules depend on this

**Goal**: 实现统一的后台管理框架，包括用户认证、RBAC权限、动态菜单、审计日志等核心功能

**Independent Test**: 用户登录、角色分配、菜单加载、权限验证的完整流程

### Database Schema

- [ ] T009 [P] [M02] Create tenant table SQL in services/admin-framework/src/main/resources/db/migration/V1__init_tenant.sql
- [ ] T010 [P] [M02] Create sys_user table SQL in services/admin-framework/src/main/resources/db/migration/V2__init_sys_user.sql
- [ ] T011 [P] [M02] Create sys_role table SQL in services/admin-framework/src/main/resources/db/migration/V3__init_sys_role.sql
- [ ] T012 [P] [M02] Create sys_menu table SQL in services/admin-framework/src/main/resources/db/migration/V4__init_sys_menu.sql
- [ ] T013 [P] [M02] Create sys_user_role table SQL in services/admin-framework/src/main/resources/db/migration/V5__init_sys_user_role.sql
- [ ] T014 [P] [M02] Create sys_role_menu table SQL in services/admin-framework/src/main/resources/db/migration/V6__init_sys_role_menu.sql
- [ ] T015 [P] [M02] Create sys_permission table SQL in services/admin-framework/src/main/resources/db/migration/V7__init_sys_permission.sql
- [ ] T016 [P] [M02] Create role_permission table SQL in services/admin-framework/src/main/resources/db/migration/V8__init_role_permission.sql
- [ ] T017 [P] [M02] Create audit_log table SQL in services/admin-framework/src/main/resources/db/migration/V9__init_audit_log.sql

### Entities & Mappers

- [ ] T018 [P] [M02] Create Tenant entity in services/admin-framework/src/main/java/com/example/admin/entity/Tenant.java
- [ ] T019 [P] [M02] Create SysUser entity in services/admin-framework/src/main/java/com/example/admin/entity/SysUser.java
- [ ] T020 [P] [M02] Create SysRole entity in services/admin-framework/src/main/java/com/example/admin/entity/SysRole.java
- [ ] T021 [P] [M02] Create SysMenu entity in services/admin-framework/src/main/java/com/example/admin/entity/SysMenu.java
- [ ] T022 [P] [M02] Create SysPermission entity in services/admin-framework/src/main/java/com/example/admin/entity/SysPermission.java
- [ ] T023 [P] [M02] Create AuditLog entity in services/admin-framework/src/main/java/com/example/admin/entity/AuditLog.java
- [ ] T024 [P] [M02] Create all Mapper interfaces in services/admin-framework/src/main/java/com/example/admin/mapper/

### Authentication & Security

- [ ] T025 [M02] Setup Spring Security with JWT in services/admin-framework/src/main/java/com/example/admin/config/SecurityConfig.java
- [ ] T026 [M02] Create JWT utility class in services/admin-framework/src/main/java/com/example/admin/util/JwtUtil.java
- [ ] T027 [M02] Create UserDetailsServiceImpl in services/admin-framework/src/main/java/com/example/admin/service/impl/UserDetailsServiceImpl.java
- [ ] T028 [M02] Create LoginController in services/admin-framework/src/main/java/com/example/admin/controller/LoginController.java
- [ ] T029 [M02] Implement POST /auth/login endpoint (depends on T025, T026, T027, T028)
- [ ] T030 [M02] Implement POST /auth/logout endpoint
- [ ] T031 [M02] Implement POST /auth/refresh endpoint

### User Management

- [ ] T032 [M02] Create SysUserService interface in services/admin-framework/src/main/java/com/example/admin/service/SysUserService.java
- [ ] T033 [M02] Create SysUserServiceImpl in services/admin-framework/src/main/java/com/example/admin/service/impl/SysUserServiceImpl.java
- [ ] T034 [M02] Create SysUserController in services/admin-framework/src/main/java/com/example/admin/controller/SysUserController.java
- [ ] T035 [M02] Implement GET /users endpoint with pagination (depends on T033, T034)
- [ ] T036 [M02] Implement POST /users endpoint
- [ ] T037 [M02] Implement GET /users/{id} endpoint
- [ ] T038 [M02] Implement PUT /users/{id} endpoint
- [ ] T039 [M02] Implement DELETE /users/{id} endpoint
- [ ] T040 [M02] Implement GET /users/{id}/roles endpoint
- [ ] T041 [M02] Implement PUT /users/{id}/roles endpoint

### Role & Permission Management

- [ ] T042 [M02] Create SysRoleService interface in services/admin-framework/src/main/java/com/example/admin/service/SysRoleService.java
- [ ] T043 [M02] Create SysRoleServiceImpl in services/admin-framework/src/main/java/com/example/admin/service/impl/SysRoleServiceImpl.java
- [ ] T044 [M02] Create SysRoleController in services/admin-framework/src/main/java/com/example/admin/controller/SysRoleController.java
- [ ] T045 [M02] Implement GET /roles endpoint
- [ ] T046 [M02] Implement POST /roles endpoint
- [ ] T047 [M02] Implement PUT /roles/{id} endpoint
- [ ] T048 [M02] Implement DELETE /roles/{id} endpoint
- [ ] T049 [M02] Implement GET /roles/{id}/menus endpoint
- [ ] T050 [M02] Implement PUT /roles/{id}/menus endpoint

### Menu Management

- [ ] T051 [M02] Create SysMenuService interface in services/admin-framework/src/main/java/com/example/admin/service/SysMenuService.java
- [ ] T052 [M02] Create SysMenuServiceImpl in services/admin-framework/src/main/java/com/example/admin/service/impl/SysMenuServiceImpl.java
- [ ] T053 [M02] Create SysMenuController in services/admin-framework/src/main/java/com/example/admin/controller/SysMenuController.java
- [ ] T054 [M02] Implement GET /menus/tree endpoint (depends on T052, T053)
- [ ] T055 [M02] Implement POST /menus endpoint
- [ ] T056 [M02] Implement PUT /menus/{id} endpoint
- [ ] T057 [M02] Implement DELETE /menus/{id} endpoint

### Audit Logging

- [ ] T058 [M02] Create AuditLogAspect in services/admin-framework/src/main/java/com/example/admin/aspect/AuditLogAspect.java
- [ ] T059 [M02] Create AuditLogController in services/admin-framework/src/main/java/com/example/admin/controller/AuditLogController.java
- [ ] T060 [M02] Implement GET /audit/logs endpoint

### Frontend - Admin Framework

- [ ] T061 [P] [M02] Create frontend main layout in frontend/src/layouts/AdminLayout.vue
- [ ] T062 [P] [M02] Create Login.vue in frontend/src/views/Login.vue
- [ ] T063 [P] [M02] Create UserManagement.vue in frontend/src/views/admin/UserManagement.vue
- [ ] T064 [P] [M02] Create RoleManagement.vue in frontend/src/views/admin/RoleManagement.vue
- [ ] T065 [P] [M02] Create MenuManagement.vue in frontend/src/views/admin/MenuManagement.vue
- [ ] T066 [P] [M02] Create auth store in frontend/src/stores/auth.js
- [ ] T067 [P] [M02] Create permission store in frontend/src/stores/permission.js
- [ ] T068 [M02] Setup Vue Router with dynamic routes in frontend/src/router/index.js
- [ ] T069 [M02] Integrate Ant Design components in frontend/src/main.js
- [ ] T070 [M02] Setup Axios interceptors for JWT in frontend/src/utils/request.js

### Gateway Integration

- [ ] T071 [M02] Add admin-framework route configuration in gateway/src/main/resources/application.yml

**Checkpoint**: Admin Framework complete - all other modules can now be developed in parallel

---

## Phase 3: M09 - Tenant Management (P1)

**Goal**: 实现租户全生命周期管理，包括注册审核、状态管理、资源配额

**Independent Test**: 租户注册、审核、配额设置、状态监控的完整流程

### Database & Entities

- [ ] T072 [P] [M09] Create tenant table SQL in services/tenant-service/src/main/resources/db/migration/V1__init_tenant.sql
- [ ] T073 [P] [M09] Create tenant_quota table SQL in services/tenant-service/src/main/resources/db/migration/V2__init_tenant_quota.sql
- [ ] T074 [P] [M09] Create tenant_resource_usage table SQL in services/tenant-service/src/main/resources/db/migration/V3__init_tenant_resource_usage.sql
- [ ] T075 [P] [M09] Create Tenant entity in services/tenant-service/src/main/java/com/example/tenant/entity/Tenant.java
- [ ] T076 [P] [M09] Create TenantQuota entity in services/tenant-service/src/main/java/com/example/tenant/entity/TenantQuota.java
- [ ] T077 [P] [M09] Create TenantResourceUsage entity in services/tenant-service/src/main/java/com/example/tenant/entity/TenantResourceUsage.java

### Service & Controller

- [ ] T078 [M09] Create TenantService in services/tenant-service/src/main/java/com/example/tenant/service/TenantService.java
- [ ] T079 [M09] Create TenantController in services/tenant-service/src/main/java/com/example/tenant/controller/TenantController.java
- [ ] T080 [M09] Implement GET /tenants endpoint with filters
- [ ] T081 [M09] Implement POST /tenants endpoint
- [ ] T082 [M09] Implement GET /tenants/{id} endpoint
- [ ] T083 [M09] Implement PUT /tenants/{id} endpoint
- [ ] T084 [M09] Implement DELETE /tenants/{id} endpoint
- [ ] T085 [M09] Implement POST /tenants/{id}/approve endpoint
- [ ] T086 [M09] Implement PUT /tenants/{id}/status endpoint
- [ ] T087 [M09] Implement GET /tenants/{id}/quota endpoint
- [ ] T088 [M09] Implement PUT /tenants/{id}/quota endpoint
- [ ] T089 [M09] Implement GET /tenants/{id}/usage endpoint
- [ ] T090 [M09] Implement GET /tenants/{id}/usage/summary endpoint
- [ ] T091 [M09] Implement GET /tenants/{id}/usage/realtime endpoint

### Frontend

- [ ] T092 [P] [M09] Create TenantList.vue in frontend/src/views/tenant/TenantList.vue
- [ ] T093 [P] [M09] Create TenantForm.vue in frontend/src/views/tenant/TenantForm.vue
- [ ] T094 [P] [M09] Create TenantApproval.vue in frontend/src/views/tenant/TenantApproval.vue
- [ ] T095 [P] [M09] Create TenantQuota.vue in frontend/src/views/tenant/TenantQuota.vue
- [ ] T096 [P] [M09] Create TenantUsage.vue in frontend/src/views/tenant/TenantUsage.vue

### Gateway

- [ ] T097 [M09] Add tenant-service route configuration in gateway/src/main/resources/application.yml

---

## Phase 4: M01 - Parameter Dictionary Management (P1)

**Goal**: 管理参数字典和参数项，支持系统参数和业务参数的分类管理

**Independent Test**: 创建参数字典、添加参数项、查询、编辑、删除的完整CRUD流程

### Database & Entities

- [ ] T098 [P] [M01] Create parameter_category table SQL in services/parameter-service/src/main/resources/db/migration/V1__init_parameter_category.sql
- [ ] T099 [P] [M01] Create parameter_dictionary table SQL in services/parameter-service/src/main/resources/db/migration/V2__init_parameter_dictionary.sql
- [ ] T100 [P] [M01] Create parameter_item table SQL in services/parameter-service/src/main/resources/db/migration/V3__init_parameter_item.sql
- [ ] T101 [P] [M01] Create ParameterCategory entity in services/parameter-service/src/main/java/com/example/parameter/entity/ParameterCategory.java
- [ ] T102 [P] [M01] Create ParameterDictionary entity in services/parameter-service/src/main/java/com/example/parameter/entity/ParameterDictionary.java
- [ ] T103 [P] [M01] Create ParameterItem entity in services/parameter-service/src/main/java/com/example/parameter/entity/ParameterItem.java

### Service & Controller

- [ ] T104 [M01] Create ParameterDictionaryService in services/parameter-service/src/main/java/com/example/parameter/service/ParameterDictionaryService.java
- [ ] T105 [M01] Create ParameterDictionaryController in services/parameter-service/src/main/java/com/example/parameter/controller/ParameterDictionaryController.java
- [ ] T106 [M01] Implement GET /categories endpoint
- [ ] T107 [M01] Implement POST /categories endpoint
- [ ] T108 [M01] Implement PUT /categories/{id} endpoint
- [ ] T109 [M01] Implement DELETE /categories/{id} endpoint
- [ ] T110 [M01] Implement GET /dictionaries endpoint with pagination
- [ ] T111 [M01] Implement POST /dictionaries endpoint
- [ ] T112 [M01] Implement GET /dictionaries/{id} endpoint
- [ ] T113 [M01] Implement PUT /dictionaries/{id} endpoint
- [ ] T114 [M01] Implement DELETE /dictionaries/{id} endpoint
- [ ] T115 [M01] Implement GET /dictionaries/{id}/items endpoint
- [ ] T116 [M01] Implement POST /dictionaries/{id}/items endpoint
- [ ] T117 [M01] Implement PUT /items/{id} endpoint with version check
- [ ] T118 [M01] Implement DELETE /items/{id} endpoint
- [ ] T119 [M01] Implement PATCH /dictionaries/{id}/status endpoint
- [ ] T120 [M01] Implement PATCH /items/{id}/status endpoint

### Frontend

- [ ] T121 [P] [M01] Create CategoryList.vue in frontend/src/views/parameter/CategoryList.vue
- [ ] T122 [P] [M01] Create DictionaryList.vue in frontend/src/views/parameter/DictionaryList.vue
- [ ] T123 [P] [M01] Create DictionaryForm.vue in frontend/src/views/parameter/DictionaryForm.vue
- [ ] T124 [P] [M01] Create ItemList.vue in frontend/src/views/parameter/ItemList.vue
- [ ] T125 [P] [M01] Create ItemForm.vue in frontend/src/views/parameter/ItemForm.vue

### Gateway

- [ ] T126 [M01] Add parameter-service route configuration in gateway/src/main/resources/application.yml

---

## Phase 5: M03 - Gateway Configuration (P1)

**Goal**: 管理Spring Cloud Gateway的路由、限流、熔断配置

**Independent Test**: 创建路由、配置限流、设置熔断器的完整流程

### Database & Entities

- [ ] T127 [P] [M03] Create gateway_route table SQL in services/gateway-service/src/main/resources/db/migration/V1__init_gateway_route.sql
- [ ] T128 [P] [M03] Create gateway_rate_limit table SQL in services/gateway-service/src/main/resources/db/migration/V2__init_gateway_rate_limit.sql
- [ ] T129 [P] [M03] Create gateway_circuit_breaker table SQL in services/gateway-service/src/main/resources/db/migration/V3__init_gateway_circuit_breaker.sql
- [ ] T130 [P] [M03] Create GatewayRoute entity in services/gateway-service/src/main/java/com/example/gateway/entity/GatewayRoute.java
- [ ] T131 [P] [M03] Create GatewayRateLimit entity in services/gateway-service/src/main/java/com/example/gateway/entity/GatewayRateLimit.java
- [ ] T132 [P] [M03] Create GatewayCircuitBreaker entity in services/gateway-service/src/main/java/com/example/gateway/entity/GatewayCircuitBreaker.java

### Service & Controller

- [ ] T133 [M03] Create GatewayRouteService in services/gateway-service/src/main/java/com/example/gateway/service/GatewayRouteService.java
- [ ] T134 [M03] Create GatewayConfigController in services/gateway-service/src/main/java/com/example/gateway/controller/GatewayConfigController.java
- [ ] T135 [M03] Implement GET /routes endpoint
- [ ] T136 [M03] Implement POST /routes endpoint
- [ ] T137 [M03] Implement PUT /routes/{id} endpoint
- [ ] T138 [M03] Implement DELETE /routes/{id} endpoint
- [ ] T139 [M03] Implement POST /routes/{id}/refresh endpoint
- [ ] T140 [M03] Implement GET /rate-limits endpoint
- [ ] T141 [M03] Implement POST /rate-limits endpoint
- [ ] T142 [M03] Implement PUT /rate-limits/{id} endpoint
- [ ] T143 [M03] Implement DELETE /rate-limits/{id} endpoint
- [ ] T144 [M03] Implement GET /circuit-breakers endpoint
- [ ] T145 [M03] Implement POST /circuit-breakers endpoint
- [ ] T146 [M03] Implement PUT /circuit-breakers/{id} endpoint
- [ ] T147 [M03] Implement DELETE /circuit-breakers/{id} endpoint

### Frontend

- [ ] T148 [P] [M03] Create RouteList.vue in frontend/src/views/gateway/RouteList.vue
- [ ] T149 [P] [M03] Create RouteForm.vue in frontend/src/views/gateway/RouteForm.vue
- [ ] T150 [P] [M03] Create RateLimitList.vue in frontend/src/views/gateway/RateLimitList.vue
- [ ] T151 [P] [M03] Create CircuitBreakerList.vue in frontend/src/views/gateway/CircuitBreakerList.vue

---

## Phase 6: M06 - Permission & Route Management (P1)

**Goal**: RBAC权限配置、动态路由管理

**Independent Test**: 角色权限分配、动态菜单加载的完整流程

### Service & Controller (uses M02 entities)

- [ ] T152 [M06] Create PermissionService in services/admin-framework/src/main/java/com/example/admin/service/PermissionService.java
- [ ] T153 [M06] Create PermissionController in services/admin-framework/src/main/java/com/example/admin/controller/PermissionController.java
- [ ] T154 [M06] Implement GET /permissions endpoint
- [ ] T155 [M06] Implement POST /permissions endpoint
- [ ] T156 [M06] Implement PUT /permissions/{id} endpoint
- [ ] T157 [M06] Implement DELETE /permissions/{id} endpoint
- [ ] T158 [M06] Implement PUT /roles/{roleId}/permissions endpoint
- [ ] T159 [M06] Implement GET /users/{userId}/permissions endpoint
- [ ] T160 [M06] Implement GET /routes/user endpoint (returns dynamic routes for frontend)
- [ ] T161 [M06] Implement POST /routes/refresh endpoint
- [ ] T162 [M06] Implement POST /check-permission endpoint

### Frontend

- [ ] T163 [P] [M06] Create PermissionList.vue in frontend/src/views/permission/PermissionList.vue
- [ ] T164 [P] [M06] Create PermissionForm.vue in frontend/src/views/permission/PermissionForm.vue
- [ ] T165 [P] [M06] Create dynamic route loader in frontend/src/router/dynamicRoutes.js

---

## Phase 7: M05 - API Marketplace (P1)

**Goal**: 完整的API市场功能，包括API定义、订阅、计费、开发者门户

**Independent Test**: API发布、应用创建、订阅API、查看计费的完整流程

### Database & Entities

- [ ] T166 [P] [M05] Create api_definition table SQL in services/api-market-service/src/main/resources/db/migration/V1__init_api_definition.sql
- [ ] T167 [P] [M05] Create developer_application table SQL in services/api-market-service/src/main/resources/db/migration/V2__init_developer_application.sql
- [ ] T168 [P] [M05] Create api_subscription table SQL in services/api-market-service/src/main/resources/db/migration/V3__init_api_subscription.sql
- [ ] T169 [P] [M05] Create api_usage_record table SQL in services/api-market-service/src/main/resources/db/migration/V4__init_api_usage_record.sql
- [ ] T170 [P] [M05] Create api_call_billing table SQL in services/api-market-service/src/main/resources/db/migration/V5__init_api_call_billing.sql
- [ ] T171 [P] [M05] Create ApiDefinition entity in services/api-market-service/src/main/java/com/example/apimarket/entity/ApiDefinition.java
- [ ] T172 [P] [M05] Create DeveloperApplication entity in services/api-market-service/src/main/java/com/example/apimarket/entity/DeveloperApplication.java
- [ ] T173 [P] [M05] Create ApiSubscription entity in services/api-market-service/src/main/java/com/example/apimarket/entity/ApiSubscription.java
- [ ] T174 [P] [M05] Create ApiUsageRecord entity in services/api-market-service/src/main/java/com/example/apimarket/entity/ApiUsageRecord.java
- [ ] T175 [P] [M05] Create ApiCallBilling entity in services/api-market-service/src/main/java/com/example/apimarket/entity/ApiCallBilling.java

### Service & Controller

- [ ] T176 [M05] Create ApiDefinitionService in services/api-market-service/src/main/java/com/example/apimarket/service/ApiDefinitionService.java
- [ ] T177 [M05] Create ApiMarketController in services/api-market-service/src/main/java/com/example/apimarket/controller/ApiMarketController.java
- [ ] T178 [M05] Implement GET /apis endpoint
- [ ] T179 [M05] Implement POST /apis endpoint
- [ ] T180 [M05] Implement GET /apis/{id} endpoint
- [ ] T181 [M05] Implement PUT /apis/{id} endpoint
- [ ] T182 [M05] Implement DELETE /apis/{id} endpoint
- [ ] T183 [M05] Implement POST /apis/{id}/publish endpoint
- [ ] T184 [M05] Implement POST /apis/{id}/audit endpoint
- [ ] T185 [M05] Implement GET /applications endpoint
- [ ] T186 [M05] Implement POST /applications endpoint
- [ ] T187 [M05] Implement GET /applications/{id}/api-keys endpoint
- [ ] T188 [M05] Implement POST /applications/{id}/api-keys endpoint
- [ ] T189 [M05] Implement GET /subscriptions endpoint
- [ ] T190 [M05] Implement POST /subscriptions endpoint
- [ ] T191 [M05] Implement DELETE /subscriptions/{id} endpoint
- [ ] T192 [M05] Implement POST /subscriptions/{id}/suspend endpoint
- [ ] T193 [M05] Implement POST /subscriptions/{id}/activate endpoint
- [ ] T194 [M05] Implement GET /usage endpoint
- [ ] T195 [M05] Implement GET /usage/summary endpoint
- [ ] T196 [M05] Implement GET /billing endpoint
- [ ] T197 [M05] Implement GET /billing/summary endpoint

### Frontend

- [ ] T198 [P] [M05] Create ApiMarketList.vue in frontend/src/views/apimarket/ApiMarketList.vue
- [ ] T199 [P] [M05] Create ApiDefinitionForm.vue in frontend/src/views/apimarket/ApiDefinitionForm.vue
- [ ] T200 [P] [M05] Create ApplicationList.vue in frontend/src/views/apimarket/ApplicationList.vue
- [ ] T201 [P] [M05] Create ApplicationForm.vue in frontend/src/views/apimarket/ApplicationForm.vue
- [ ] T202 [P] [M05] Create SubscriptionList.vue in frontend/src/views/apimarket/SubscriptionList.vue
- [ ] T203 [P] [M05] Create ApiBilling.vue in frontend/src/views/apimarket/ApiBilling.vue

### Gateway

- [ ] T204 [M05] Add api-market-service route configuration in gateway/src/main/resources/application.yml

---

## Phase 8: M11 - Distributed Tracing (P1)

**Goal**: 集成SkyWalking/Zipkin，实现链路追踪、性能监控、告警配置

**Independent Test**: 服务接入、链路查询、告警配置的完整流程

### Database & Entities

- [ ] T205 [P] [M11] Create trace_config table SQL in services/tracing-service/src/main/resources/db/migration/V1__init_trace_config.sql
- [ ] T206 [P] [M11] Create trace_alert table SQL in services/tracing-service/src/main/resources/db/migration/V2__init_trace_alert.sql
- [ ] T207 [P] [M11] Create trace_dashboard table SQL in services/tracing-service/src/main/resources/db/migration/V3__init_trace_dashboard.sql
- [ ] T208 [P] [M11] Create TraceConfig entity in services/tracing-service/src/main/java/com/example/tracing/entity/TraceConfig.java
- [ ] T209 [P] [M11] Create TraceAlert entity in services/tracing-service/src/main/java/com/example/tracing/entity/TraceAlert.java
- [ ] T210 [P] [M11] Create TraceDashboard entity in services/tracing-service/src/main/java/com/example/tracing/entity/TraceDashboard.java

### Service & Controller

- [ ] T211 [M11] Create TracingConfigService in services/tracing-service/src/main/java/com/example/tracing/service/TracingConfigService.java
- [ ] T212 [M11] Create TracingController in services/tracing-service/src/main/java/com/example/tracing/controller/TracingController.java
- [ ] T213 [M11] Implement GET /configs endpoint
- [ ] T214 [M11] Implement POST /configs endpoint
- [ ] T215 [M11] Implement PUT /configs/{id} endpoint
- [ ] T216 [M11] Implement DELETE /configs/{id} endpoint
- [ ] T217 [M11] Implement GET /alerts endpoint
- [ ] T218 [M11] Implement POST /alerts endpoint
- [ ] T219 [M11] Implement PUT /alerts/{id} endpoint
- [ ] T220 [M11] Implement DELETE /alerts/{id} endpoint
- [ ] T221 [M11] Implement POST /alerts/{id}/toggle endpoint
- [ ] T222 [M11] Implement GET /dashboards endpoint
- [ ] T223 [M11] Implement POST /dashboards endpoint
- [ ] T224 [M11] Implement GET /traces/query endpoint
- [ ] T225 [M11] Implement GET /traces/{traceId} endpoint
- [ ] T226 [M11] Implement GET /metrics/services endpoint
- [ ] T227 [M11] Implement GET /metrics/top endpoint

### Frontend

- [ ] T228 [P] [M11] Create TraceConfigList.vue in frontend/src/views/tracing/TraceConfigList.vue
- [ ] T229 [P] [M11] Create TraceAlertList.vue in frontend/src/views/tracing/TraceAlertList.vue
- [ ] T230 [P] [M11] Create TraceQuery.vue in frontend/src/views/tracing/TraceQuery.vue
- [ ] T231 [P] [M11] Create TraceDashboard.vue in frontend/src/views/tracing/TraceDashboard.vue

---

## Phase 9: M04 - Third-party API Management (P2)

**Goal**: 接入第三方API，配置密钥，记录调用日志，健康检查

**Independent Test**: 注册第三方API、配置密钥、查看调用日志的完整流程

### Database & Entities

- [ ] T232 [P] [M04] Create third_party_api table SQL in services/third-party-service/src/main/resources/db/migration/V1__init_third_party_api.sql
- [ ] T233 [P] [M04] Create api_key table SQL in services/third-party-service/src/main/resources/db/migration/V2__init_api_key.sql
- [ ] T234 [P] [M04] Create api_call_log table SQL in services/third-party-service/src/main/resources/db/migration/V3__init_api_call_log.sql
- [ ] T235 [P] [M04] Create api_health_check table SQL in services/third-party-service/src/main/resources/db/migration/V4__init_api_health_check.sql
- [ ] T236 [P] [M04] Create ThirdPartyApi entity in services/third-party-service/src/main/java/com/example/thirdparty/entity/ThirdPartyApi.java
- [ ] T237 [P] [M04] Create ApiKey entity in services/third-party-service/src/main/java/com/example/thirdparty/entity/ApiKey.java
- [ ] T238 [P] [M04] Create ApiCallLog entity in services/third-party-service/src/main/java/com/example/thirdparty/entity/ApiCallLog.java
- [ ] T239 [P] [M04] Create ApiHealthCheck entity in services/third-party-service/src/main/java/com/example/thirdparty/entity/ApiHealthCheck.java

### Service & Controller

- [ ] T240 [M04] Create ThirdPartyApiService in services/third-party-service/src/main/java/com/example/thirdparty/service/ThirdPartyApiService.java
- [ ] T241 [M04] Create ThirdPartyApiController in services/third-party-service/src/main/java/com/example/thirdparty/controller/ThirdPartyApiController.java
- [ ] T242 [M04] Implement GET /apis endpoint
- [ ] T243 [M04] Implement POST /apis endpoint
- [ ] T244 [M04] Implement PUT /apis/{id} endpoint
- [ ] T245 [M04] Implement DELETE /apis/{id} endpoint
- [ ] T246 [M04] Implement GET /apis/{id}/keys endpoint
- [ ] T247 [M04] Implement POST /apis/{id}/keys endpoint
- [ ] T248 [M04] Implement PUT /keys/{id} endpoint
- [ ] T249 [M04] Implement DELETE /keys/{id} endpoint
- [ ] T250 [M04] Implement GET /apis/{id}/logs endpoint
- [ ] T251 [M04] Implement GET /apis/{id}/health endpoint
- [ ] T252 [M04] Implement POST /apis/{id}/health/check endpoint

### Frontend

- [ ] T253 [P] [M04] Create ThirdPartyApiList.vue in frontend/src/views/thirdparty/ApiList.vue
- [ ] T254 [P] [M04] Create ThirdPartyApiForm.vue in frontend/src/views/thirdparty/ApiForm.vue
- [ ] T255 [P] [M04] Create ApiKeyManagement.vue in frontend/src/views/thirdparty/ApiKeyManagement.vue
- [ ] T256 [P] [M04] Create ApiCallLog.vue in frontend/src/views/thirdparty/ApiCallLog.vue

---

## Phase 10: M07 - Package Management (P2)

**Goal**: SaaS套餐定义、定价、功能配置、订阅管理

**Independent Test**: 创建套餐、配置功能、租户订阅的完整流程

### Database & Entities

- [ ] T257 [P] [M07] Create saas_package table SQL in services/billing-service/src/main/resources/db/migration/V1__init_saas_package.sql
- [ ] T258 [P] [M07] Create package_feature table SQL in services/billing-service/src/main/resources/db/migration/V2__init_package_feature.sql
- [ ] T259 [P] [M07] Create tenant_subscription table SQL in services/billing-service/src/main/resources/db/migration/V3__init_tenant_subscription.sql
- [ ] T260 [P] [M07] Create SaasPackage entity in services/billing-service/src/main/java/com/example/billing/entity/SaasPackage.java
- [ ] T261 [P] [M07] Create PackageFeature entity in services/billing-service/src/main/java/com/example/billing/entity/PackageFeature.java
- [ ] T262 [P] [M07] Create TenantSubscription entity in services/billing-service/src/main/java/com/example/billing/entity/TenantSubscription.java

### Service & Controller

- [ ] T263 [M07] Create PackageService in services/billing-service/src/main/java/com/example/billing/service/PackageService.java
- [ ] T264 [M07] Create PackageController in services/billing-service/src/main/java/com/example/billing/controller/PackageController.java
- [ ] T265 [M07] Implement GET /packages endpoint
- [ ] T266 [M07] Implement POST /packages endpoint
- [ ] T267 [M07] Implement PUT /packages/{id} endpoint
- [ ] T268 [M07] Implement DELETE /packages/{id} endpoint
- [ ] T269 [M07] Implement GET /packages/{id}/features endpoint
- [ ] T270 [M07] Implement POST /packages/{id}/features endpoint
- [ ] T271 [M07] Implement PUT /features/{featureId} endpoint
- [ ] T272 [M07] Implement DELETE /features/{featureId} endpoint
- [ ] T273 [M07] Implement GET /subscriptions endpoint
- [ ] T274 [M07] Implement POST /subscriptions endpoint
- [ ] T275 [M07] Implement PUT /subscriptions/{id} endpoint
- [ ] T276 [M07] Implement DELETE /subscriptions/{id} endpoint
- [ ] T277 [M07] Implement POST /subscriptions/{id}/renew endpoint
- [ ] T278 [M07] Implement POST /subscriptions/{id}/suspend endpoint
- [ ] T279 [M07] Implement POST /subscriptions/{id}/activate endpoint
- [ ] T280 [M07] Implement GET /tenants/{tenantId}/subscription endpoint

### Frontend

- [ ] T281 [P] [M07] Create PackageList.vue in frontend/src/views/package/PackageList.vue
- [ ] T282 [P] [M07] Create PackageForm.vue in frontend/src/views/package/PackageForm.vue
- [ ] T283 [P] [M07] Create SubscriptionList.vue in frontend/src/views/package/SubscriptionList.vue

---

## Phase 11: M08 - Activity Management (P2)

**Goal**: 营销活动管理，与套餐多对多关联，活动参与记录

**Independent Test**: 创建活动、关联套餐、租户参与的完整流程

### Database & Entities

- [ ] T284 [P] [M08] Create marketing_activity table SQL in services/billing-service/src/main/resources/db/migration/V4__init_marketing_activity.sql
- [ ] T285 [P] [M08] Create package_activity table SQL in services/billing-service/src/main/resources/db/migration/V5__init_package_activity.sql
- [ ] T286 [P] [M08] Create activity_subscription table SQL in services/billing-service/src/main/resources/db/migration/V6__init_activity_subscription.sql
- [ ] T287 [P] [M08] Create MarketingActivity entity in services/billing-service/src/main/java/com/example/billing/entity/MarketingActivity.java
- [ ] T288 [P] [M08] Create PackageActivity entity in services/billing-service/src/main/java/com/example/billing/entity/PackageActivity.java
- [ ] T289 [P] [M08] Create ActivitySubscription entity in services/billing-service/src/main/java/com/example/billing/entity/ActivitySubscription.java

### Service & Controller

- [ ] T290 [M08] Create ActivityService in services/billing-service/src/main/java/com/example/billing/service/ActivityService.java
- [ ] T291 [M08] Create ActivityController in services/billing-service/src/main/java/com/example/billing/controller/ActivityController.java
- [ ] T292 [M08] Implement GET /activities endpoint
- [ ] T293 [M08] Implement POST /activities endpoint
- [ ] T294 [M08] Implement PUT /activities/{id} endpoint
- [ ] T295 [M08] Implement DELETE /activities/{id} endpoint
- [ ] T296 [M08] Implement POST /activities/{id}/start endpoint
- [ ] T297 [M08] Implement POST /activities/{id}/pause endpoint
- [ ] T298 [M08] Implement POST /activities/{id}/end endpoint
- [ ] T299 [M08] Implement GET /activities/{id}/packages endpoint
- [ ] T300 [M08] Implement POST /activities/{id}/packages endpoint (link packages)
- [ ] T301 [M08] Implement DELETE /activities/{activityId}/packages/{packageId} endpoint
- [ ] T302 [M08] Implement GET /activities/{id}/participations endpoint
- [ ] T303 [M08] Implement POST /activities/{id}/join endpoint
- [ ] T304 [M08] Implement DELETE /participations/{id}/cancel endpoint

### Frontend

- [ ] T305 [P] [M08] Create ActivityList.vue in frontend/src/views/activity/ActivityList.vue
- [ ] T306 [P] [M08] Create ActivityForm.vue in frontend/src/views/activity/ActivityForm.vue
- [ ] T307 [P] [M08] Create ParticipationList.vue in frontend/src/views/activity/ParticipationList.vue

---

## Phase 12: M12 - Code Generation (P2)

**Goal**: 基于数据库表结构自动生成前后端CRUD代码

**Independent Test**: 导入表结构、预览代码、下载生成代码的完整流程

### Database & Entities

- [ ] T308 [P] [M12] Create code_template table SQL in services/codegen-service/src/main/resources/db/migration/V1__init_code_template.sql
- [ ] T309 [P] [M12] Create gen_history table SQL in services/codegen-service/src/main/resources/db/migration/V2__init_gen_history.sql
- [ ] T310 [P] [M12] Create CodeTemplate entity in services/codegen-service/src/main/java/com/example/codegen/entity/CodeTemplate.java
- [ ] T311 [P] [M12] Create GenHistory entity in services/codegen-service/src/main/java/com/example/codegen/entity/GenHistory.java

### Service & Controller

- [ ] T312 [M12] Create CodeTemplateService in services/codegen-service/src/main/java/com/example/codegen/service/CodeTemplateService.java
- [ ] T313 [M12] Create CodegenService in services/codegen-service/src/main/java/com/example/codegen/service/CodegenService.java
- [ ] T314 [M12] Create CodeTemplateController in services/codegen-service/src/main/java/com/example/codegen/controller/CodeTemplateController.java
- [ ] T315 [M12] Create CodegenController in services/codegen-service/src/main/java/com/example/codegen/controller/CodegenController.java
- [ ] T316 [M12] Implement GET /templates endpoint
- [ ] T317 [M12] Implement POST /templates endpoint
- [ ] T318 [M12] Implement PUT /templates/{id} endpoint
- [ ] T319 [M12] Implement DELETE /templates/{id} endpoint
- [ ] T320 [M12] Implement GET /database/tables endpoint
- [ ] T321 [M12] Implement GET /database/tables/{tableName} endpoint
- [ ] T322 [M12] Implement POST /database/import endpoint
- [ ] T323 [M12] Implement POST /generation/preview endpoint
- [ ] T324 [M12] Implement POST /generation/generate endpoint
- [ ] T325 [M12] Implement GET /generation/download/{historyId} endpoint
- [ ] T326 [M12] Implement GET /history endpoint
- [ ] T327 [M12] Implement GET /history/{id} endpoint
- [ ] T328 [M12] Implement DELETE /history/{id} endpoint
- [ ] T329 [M12] Implement GET /history/{id}/files endpoint
- [ ] T330 [M12] Implement POST /history/{id}/regenerate endpoint

### Frontend

- [ ] T331 [P] [M12] Create TemplateList.vue in frontend/src/views/codegen/TemplateList.vue
- [ ] T332 [P] [M12] Create TableSelector.vue in frontend/src/views/codegen/TableSelector.vue
- [ ] T333 [P] [M12] Create CodePreview.vue in frontend/src/views/codegen/CodePreview.vue
- [ ] T334 [P] [M12] Create GenHistory.vue in frontend/src/views/codegen/GenHistory.vue

---

## Phase 13: M10 - Error Documentation (P3)

**Goal**: 错误日志查询（Elasticsearch）、错误分类、解决方案库

**Independent Test**: 搜索错误日志、查看解决方案、标记有用的完整流程

### Database & Entities

- [ ] T335 [P] [M10] Create error_category table SQL in services/error-doc-service/src/main/resources/db/migration/V1__init_error_category.sql
- [ ] T336 [P] [M10] Create error_solution table SQL in services/error-doc-service/src/main/resources/db/migration/V2__init_error_solution.sql
- [ ] T337 [P] [M10] Create ErrorCategory entity in services/error-doc-service/src/main/java/com/example/errordoc/entity/ErrorCategory.java
- [ ] T338 [P] [M10] Create ErrorSolution entity in services/error-doc-service/src/main/java/com/example/errordoc/entity/ErrorSolution.java

### Elasticsearch Index

- [ ] T339 [M10] Create error-log index mapping in services/error-doc-service/src/main/resources/elasticsearch/error-log-mapping.json

### Service & Controller

- [ ] T340 [M10] Create ErrorCategoryService in services/error-doc-service/src/main/java/com/example/errordoc/service/ErrorCategoryService.java
- [ ] T341 [M10] Create ErrorSolutionService in services/error-doc-service/src/main/java/com/example/errordoc/service/ErrorSolutionService.java
- [ ] T342 [M10] Create ErrorSearchService in services/error-doc-service/src/main/java/com/example/errordoc/service/ErrorSearchService.java
- [ ] T343 [M10] Create ErrorDocController in services/error-doc-service/src/main/java/com/example/errordoc/controller/ErrorDocController.java
- [ ] T344 [M10] Implement GET /categories endpoint
- [ ] T345 [M10] Implement POST /categories endpoint
- [ ] T346 [M10] Implement GET /categories/tree endpoint
- [ ] T347 [M10] Implement PUT /categories/{id} endpoint
- [ ] T348 [M10] Implement DELETE /categories/{id} endpoint
- [ ] T349 [M10] Implement GET /solutions endpoint
- [ ] T350 [M10] Implement POST /solutions endpoint
- [ ] T351 [M10] Implement POST /solutions/search endpoint
- [ ] T352 [M10] Implement PUT /solutions/{id} endpoint
- [ ] T353 [M10] Implement DELETE /solutions/{id} endpoint
- [ ] T354 [M10] Implement POST /solutions/{id}/helpful endpoint
- [ ] T355 [M10] Implement POST /logs/search endpoint (Elasticsearch)
- [ ] T356 [M10] Implement POST /logs/aggregations endpoint

### Frontend

- [ ] T357 [P] [M10] Create ErrorCategoryList.vue in frontend/src/views/errordoc/ErrorCategoryList.vue
- [ ] T358 [P] [M10] Create ErrorSolutionList.vue in frontend/src/views/errordoc/ErrorSolutionList.vue
- [ ] T359 [P] [M10] Create ErrorLogSearch.vue in frontend/src/views/errordoc/ErrorLogSearch.vue
- [ ] T360 [P] [M10] Create ErrorLogDetail.vue in frontend/src/views/errordoc/ErrorLogDetail.vue

---

## Phase 14: Polish & Cross-Cutting Concerns

**Purpose**: System-wide improvements and optimizations

### Multi-Tenant Configuration

- [ ] T361 [P] Configure MyBatis-Plus tenant plugin for all services in services/*/src/main/java/com/example/*/config/MyBatisPlusConfig.java
- [ ] T362 [P] Configure tenant line handler in services/*/src/main/java/com/example/*/config/TenantLineHandler.java
- [ ] T363 [P] Configure logical delete plugin in services/*/src/main/java/com/example/*/config/MyBatisPlusConfig.java

### Caching & Performance

- [ ] T364 [P] Configure Redis for all services in services/*/src/main/resources/application.yml
- [ ] T365 [P] Implement Redis cache with tenant prefix in services/*/src/main/java/com/example/*/config/RedisConfig.java
- [ ] T366 [P] Add database indexes for common queries in services/*/src/main/resources/db/migration/

### Security & RBAC

- [ ] T367 [P] Add @PreAuthorize annotations to all controllers in services/*/src/main/java/com/example/*/controller/
- [ ] T368 [P] Configure method security in services/admin-framework/src/main/java/com/example/admin/config/SecurityConfig.java

### Error Handling

- [ ] T369 [P] Create global exception handler in services/*/src/main/java/com/example/*/exception/GlobalExceptionHandler.java
- [ ] T370 [P] Add user-friendly error messages in services/*/src/main/java/com/example/*/exception/

### Frontend Polish

- [ ] T371 [P] Add loading states to all frontend components in frontend/src/views/
- [ ] T372 [P] Add error handling to all frontend API calls in frontend/src/utils/request.js
- [ ] T373 [P] Add success notifications for CRUD operations in frontend/src/components/
- [ ] T374 [P] Optimize bundle size in frontend/vite.config.js

### Documentation

- [ ] T375 [P] Update API documentation with OpenAPI specs in services/*/src/main/resources/openapi/
- [ ] T376 [P] Add README for each service in services/*/README.md

### Final Validation

- [ ] T377 Run all scenarios from quickstart.md and verify functionality
- [ ] T378 Verify all constitution compliance checks pass
- [ ] T379 Test multi-tenant isolation with multiple tenants
- [ ] T380 Test concurrent edit conflict detection (optimistic locking)
- [ ] T381 Test audit logging for all operations
- [ ] T382 Test API gateway routing for all services
- [ ] T383 Test distributed tracing end-to-end

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies - can start immediately
- **Foundational - M02 (Phase 2)**: BLOCKS all other phases - must complete first
- **User Stories (Phase 3-13)**: All depend on M02 completion, can run in parallel after:
  - Phase 3: M09 (Tenant) - P1
  - Phase 4: M01 (Parameter) - P1
  - Phase 5: M03 (Gateway) - P1
  - Phase 6: M06 (Permission) - P1 (uses M02 entities)
  - Phase 7: M05 (API Marketplace) - P1
  - Phase 8: M11 (Tracing) - P1
  - Phase 9: M04 (Third-party) - P2
  - Phase 10: M07 (Package) - P2
  - Phase 11: M08 (Activity) - P2 (depends on M07 for package-activity junction)
  - Phase 12: M12 (Code Generation) - P2
  - Phase 13: M10 (Error Documentation) - P3
- **Polish (Phase 14)**: Depends on all desired modules being complete

### Module Dependencies

- **M02 (Admin Framework)**: Foundation - ALL other modules depend on this
- **M01 (Parameter)**: Independent after M02
- **M03 (Gateway)**: Independent after M02 (but configures routing for others)
- **M04 (Third-party)**: Independent after M02
- **M05 (API Marketplace)**: Independent after M02, M09
- **M06 (Permission)**: Uses M02 entities, extends with route management
- **M07 (Package)**: Independent after M02
- **M08 (Activity)**: Depends on M07 (many-to-many junction table)
- **M09 (Tenant)**: Foundation for multi-tenant, all modules use tenant context
- **M10 (Error Documentation)**: Independent after M02
- **M11 (Tracing)**: Independent after M02 (but traces all services)
- **M12 (Code Generation)**: Independent after M02

### Parallel Opportunities

After M02 (Admin Framework) is complete:
- **P1 modules can run in parallel**: M01, M03, M05, M06, M09, M11
- **P2 modules can run in parallel**: M04, M07, M12
- **M08** must wait for **M07** (package-activity junction)
- **M10** (P3) can run independently at any time

### Parallel Example: P1 Modules After M02

```bash
# After M02 is complete, these can all run in parallel:
Task: "Phase 3: M09 - Tenant Management"
Task: "Phase 4: M01 - Parameter Dictionary"
Task: "Phase 5: M03 - Gateway Configuration"
Task: "Phase 6: M06 - Permission Management"
Task: "Phase 7: M05 - API Marketplace"
Task: "Phase 8: M11 - Distributed Tracing"
```

### Parallel Example: P2 Modules After M02

```bash
# After M02 is complete, these can all run in parallel:
Task: "Phase 9: M04 - Third-party API Management"
Task: "Phase 10: M07 - Package Management"
Task: "Phase 12: M12 - Code Generation"

# Then M08 after M07:
Task: "Phase 11: M08 - Activity Management"
```

---

## Implementation Strategy

### MVP First (M02 Only - Critical Foundation)

1. Complete Phase 1: Setup (T001-T008)
2. Complete Phase 2: M02 Admin Framework (T009-T071) - CRITICAL
3. **STOP and VALIDATE**: Test login, user management, roles, permissions, menus independently
4. Deploy/demo Foundation

### Incremental Delivery

1. **Wave 1 (Foundation)**: Setup + M02 → Foundation ready
2. **Wave 2 (P1 Modules)**: M01 + M03 + M05 + M06 + M09 + M11 → Core platform features
3. **Wave 3 (P2 Modules)**: M04 + M07 + M12 → Extended features
4. **Wave 4 (M08)**: Activity management (after M07)
5. **Wave 5 (M10)**: Error documentation (P3, can defer)
6. **Wave 6 (Polish)**: Phase 14 → Final deployment

### Parallel Team Strategy

With multiple developers after M02 is complete:

1. **Team A (P1 Core)**: M01 (Parameter) + M09 (Tenant)
2. **Team B (P1 Core)**: M03 (Gateway) + M11 (Tracing)
3. **Team C (P1 Core)**: M05 (API Marketplace) + M06 (Permission)
4. **Team D (P2 Extended)**: M04 (Third-party) + M12 (Code Gen)
5. **Team E (P2 Extended)**: M07 (Package) → followed by M08 (Activity)
6. **Team F (P3 Optional)**: M10 (Error Documentation)

---

## Task Count Summary

| Phase | Task Count | Module | Priority |
|-------|-----------|--------|----------|
| Phase 1: Setup | 8 | Shared | - |
| Phase 2: M02 Foundation | 63 | Admin Framework | P0 |
| Phase 3: M09 | 26 | Tenant Management | P1 |
| Phase 4: M01 | 29 | Parameter Dictionary | P1 |
| Phase 5: M03 | 25 | Gateway Configuration | P1 |
| Phase 6: M06 | 14 | Permission Management | P1 |
| Phase 7: M05 | 39 | API Marketplace | P1 |
| Phase 8: M11 | 27 | Distributed Tracing | P1 |
| Phase 9: M04 | 25 | Third-party API | P2 |
| Phase 10: M07 | 27 | Package Management | P2 |
| Phase 11: M08 | 25 | Activity Management | P2 |
| Phase 12: M12 | 27 | Code Generation | P2 |
| Phase 13: M10 | 26 | Error Documentation | P3 |
| Phase 14: Polish | 23 | Cross-cutting | - |
| **Total** | **384** | All 12 Modules | - |

### By Priority

| Priority | Module Count | Task Count | Modules |
|----------|-------------|-----------|---------|
| P0 (Critical) | 1 | 71 | M02 |
| P1 (High) | 6 | 160 | M01, M03, M05, M06, M09, M11 |
| P2 (Medium) | 4 | 104 | M04, M07, M08, M12 |
| P3 (Low) | 1 | 26 | M10 |

### By Module

| Module | ID | Task Count | Parallel Opportunities |
|--------|-----|-----------|----------------------|
| M02: Admin Framework | P0 | 71 | 26 parallel |
| M09: Tenant Management | P1 | 26 | 9 parallel |
| M01: Parameter Dictionary | P1 | 29 | 10 parallel |
| M03: Gateway Configuration | P1 | 25 | 8 parallel |
| M06: Permission Management | P1 | 14 | 5 parallel |
| M05: API Marketplace | P1 | 39 | 14 parallel |
| M11: Distributed Tracing | P1 | 27 | 10 parallel |
| M04: Third-party API | P2 | 25 | 9 parallel |
| M07: Package Management | P2 | 27 | 10 parallel |
| M08: Activity Management | P2 | 25 | 9 parallel |
| M12: Code Generation | P2 | 27 | 10 parallel |
| M10: Error Documentation | P3 | 26 | 9 parallel |
| Polish | - | 23 | 20 parallel |

---

## Notes

- [P] tasks = different files, no dependencies within phase
- [Mxx] label maps task to specific module for traceability
- Each module should be independently completable and testable after M02 foundation
- Commit after each task or logical group
- Stop at any checkpoint to validate module independently
- MVP scope = Phase 1 + Phase 2 (Setup + M02 Foundation) = 79 tasks

---

## Quick Reference

### Module ID Mapping

- **M01**: Parameter Dictionary (参数字典)
- **M02**: Admin Framework (后台管理框架)
- **M03**: Gateway Configuration (网关设置)
- **M04**: Third-party API (第三方接口)
- **M05**: API Marketplace (API开放平台)
- **M06**: Permission Management (权限路由)
- **M07**: Package Management (套餐管理)
- **M08**: Activity Management (活动管理)
- **M09**: Tenant Management (租户管理)
- **M10**: Error Documentation (错误文档)
- **M11**: Distributed Tracing (链路追踪)
- **M12**: Code Generation (代码生成)

### Service Names

- `services/admin-framework/` - M02
- `services/parameter-service/` - M01
- `services/gateway-service/` - M03
- `services/third-party-service/` - M04
- `services/api-market-service/` - M05
- `services/permission-service/` - M06 (merged with admin-framework)
- `services/billing-service/` - M07 + M08
- `services/tenant-service/` - M09
- `services/error-doc-service/` - M10
- `services/tracing-service/` - M11
- `services/codegen-service/` - M12
- `services/gateway/` - Spring Cloud Gateway (infrastructure)
