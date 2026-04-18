# Data Model: Admin Management Platform

**Feature**: Admin Management Platform (12 Modules)
**Date**: 2026-01-28
**Database**: MySQL 8.0+
**Architecture**: Multi-tenant SaaS with microservices

## 实体关系概览

```text
┌─────────────────────────────────────────────────────────────────────────────────────┐
│                        Admin Management Platform - Data Model                        │
├─────────────────────────────────────────────────────────────────────────────────────┤
│                                                                                     │
│  ┌──────────────┐     ┌──────────────┐     ┌──────────────┐     ┌──────────────┐  │
│  │   Tenant     │────<│   User       │────<│   Role       │     │   Package    │  │
│  │   (M09)      │     │   (M02)      │     │   (M06)      │     │   (M07)      │  │
│  └──────────────┘     └──────────────┘     └──────────────┘     └──────────────┘  │
│         │                    │                    │                    │            │
│         └────────────────────┴────────────────────┴────────────────────┘            │
│                              │                                                    │
│                              v                                                    │
│  ┌─────────────────────────────────────────────────────────────────────────────┐  │
│  │                    All tables have: tenant_id, deleted                       │  │
│  └─────────────────────────────────────────────────────────────────────────────┘  │
│                                                                                     │
│  M01: Parameter Dictionary      M02: Admin Framework                               │
│    - parameter_category           - sys_user                                       │
│    - parameter_dictionary         - sys_role                                       │
│    - parameter_item               - sys_menu (动态菜单)                             │
│                                    - sys_user_role                                 │
│  M03: Gateway Config              - sys_role_menu                                 │
│    - gateway_route                - audit_log                                      │
│    - gateway_rate_limit                                                           │
│    - gateway_circuit_breaker     M04: Third Party API                              │
│                                    - third_party_api                                │
│  M05: API Marketplace             - api_call_log                                   │
│    - api_definition               - api_health_check                               │
│    - api_subscription             - api_key (encrypted)                            │
│    - developer_application                                                         │
│    - api_usage_record                                                           │
│    - api_call_billing                                                              │
│                                                                                    │
│  M06: Permission Route          M07: Package Management                            │
│    - sys_role (shared)             - saas_package                                  │
│    - sys_menu (shared)             - package_feature                               │
│    - sys_permission               - package_activity (M08 junction)                │
│    - role_permission                                                             │
│                                    M08: Activity Management                        │
│  M09: Tenant Management            - marketing_activity                            │
│    - tenant                        - package_activity                              │
│    - tenant_quota                  - activity_subscription                         │
│    - tenant_resource_usage                                                        │
│                                                                    M11: Tracing    │
│  M10: Error Documentation            - trace_config (配置)                          │
│    - error_log (ES)                 - trace_alert (告警)                            │
│    - error_category                 - trace_dashboard (配置)                        │
│    - error_solution                                                               │
│                                                                 M12: Code Gen    │
│                                                                  - code_template  │
│                                                                  - gen_history    │
└─────────────────────────────────────────────────────────────────────────────────────┘
```

## 通用表设计模式

### 所有业务表通用字段

| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| id | BIGINT | PK, AUTO_INCREMENT | 主键 |
| tenant_id | VARCHAR(64) | FK, NOT NULL, INDEX | 租户ID |
| deleted | TINYINT | DEFAULT 0 | 逻辑删除标记 |
| created_at | DATETIME | NOT NULL | 创建时间 |
| updated_at | DATETIME | NOT NULL | 更新时间 |
| created_by | VARCHAR(64) | | 创建人ID |
| updated_by | VARCHAR(64) | | 更新人ID |
| version | INT | DEFAULT 0 | 乐观锁版本号（高频更新表） |

### 索引标准

```sql
-- 所有表必备索引
PRIMARY KEY (id)
INDEX idx_tenant_deleted (tenant_id, deleted)

-- 外键索引
INDEX idx_tenant_xxx (tenant_id, xxx_id)
```

---

## M01: 参数字典管理

### 1. parameter_category（参数分类表）

| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| id | BIGINT | PK, AUTO_INCREMENT | 主键 |
| tenant_id | VARCHAR(64) | FK, NOT NULL | 租户ID |
| code | VARCHAR(50) | NOT NULL, UNIQUE(tenant_id, code) | 分类编码 |
| name | VARCHAR(100) | NOT NULL | 分类名称 |
| description | VARCHAR(500) | | 分类描述 |
| sort_order | INT | DEFAULT 0 | 排序序号 |
| deleted | TINYINT | DEFAULT 0 | 逻辑删除标记 |
| created_at | DATETIME | NOT NULL | 创建时间 |
| updated_at | DATETIME | NOT NULL | 更新时间 |
| created_by | VARCHAR(64) | | 创建人ID |
| updated_by | VARCHAR(64) | | 更新人ID |

### 2. parameter_dictionary（参数字典表）

| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| id | BIGINT | PK, AUTO_INCREMENT | 主键 |
| tenant_id | VARCHAR(64) | FK, NOT NULL | 租户ID |
| code | VARCHAR(50) | NOT NULL, UNIQUE(tenant_id, type, code) | 字典编码 |
| name | VARCHAR(100) | NOT NULL | 字典名称 |
| description | VARCHAR(500) | | 字典描述 |
| type | VARCHAR(20) | NOT NULL, CHECK(type IN ('SYSTEM', 'BUSINESS')) | 字典类型 |
| category_id | BIGINT | FK, NULL | 所属分类ID |
| status | VARCHAR(20) | NOT NULL, DEFAULT 'ENABLED' | 状态（ENABLED/DISABLED） |
| deleted | TINYINT | DEFAULT 0 | 逻辑删除标记 |
| created_at | DATETIME | NOT NULL | 创建时间 |
| updated_at | DATETIME | NOT NULL | 更新时间 |
| created_by | VARCHAR(64) | | 创建人ID |
| updated_by | VARCHAR(64) | | 更新人ID |
| version | INT | NOT NULL DEFAULT 0 | 乐观锁版本号 |

### 3. parameter_item（参数项表）

| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| id | BIGINT | PK, AUTO_INCREMENT | 主键 |
| tenant_id | VARCHAR(64) | FK, NOT NULL | 租户ID |
| dictionary_id | BIGINT | FK, NOT NULL | 所属字典ID |
| key | VARCHAR(50) | NOT NULL, UNIQUE(dictionary_id, key) | 参数键 |
| value | JSON | NOT NULL | 参数值（支持复杂类型） |
| description | VARCHAR(500) | | 参数描述 |
| data_type | VARCHAR(50) | NOT NULL | 数据类型 |
| validation_rule | JSON | | 验证规则（JSON格式） |
| status | VARCHAR(20) | NOT NULL, DEFAULT 'ENABLED' | 状态 |
| sort_order | INT | DEFAULT 0 | 排序序号 |
| deleted | TINYINT | DEFAULT 0 | 逻辑删除标记 |
| created_at | DATETIME | NOT NULL | 创建时间 |
| updated_at | DATETIME | NOT NULL | 更新时间 |
| created_by | VARCHAR(64) | | 创建人ID |
| updated_by | VARCHAR(64) | | 更新人ID |
| version | INT | NOT NULL DEFAULT 0 | 乐观锁版本号 |

---

## M02: 后台管理框架

### 1. sys_user（系统用户表）

| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| id | VARCHAR(64) | PK, NOT NULL | 用户ID（UUID） |
| tenant_id | VARCHAR(64) | FK, NOT NULL | 租户ID |
| username | VARCHAR(50) | NOT NULL, UNIQUE(tenant_id, username) | 用户名 |
| password | VARCHAR(255) | NOT NULL | 密码（BCrypt加密） |
| real_name | VARCHAR(100) | | 真实姓名 |
| email | VARCHAR(100) | UNIQUE | 邮箱 |
| phone | VARCHAR(20) | | 手机号 |
| avatar | VARCHAR(255) | | 头像URL |
| status | VARCHAR(20) | NOT NULL, DEFAULT 'ACTIVE' | 状态（ACTIVE/LOCKED/DISABLED） |
| user_type | VARCHAR(20) | NOT NULL | 用户类型（SUPER_ADMIN/ADMIN/USER/DEVELOPER） |
| deleted | TINYINT | DEFAULT 0 | 逻辑删除标记 |
| last_login_at | DATETIME | | 最后登录时间 |
| last_login_ip | VARCHAR(50) | | 最后登录IP |
| created_at | DATETIME | NOT NULL | 创建时间 |
| updated_at | DATETIME | NOT NULL | 更新时间 |
| created_by | VARCHAR(64) | | 创建人ID |
| updated_by | VARCHAR(64) | | 更新人ID |

### 2. sys_role（系统角色表）

| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| id | BIGINT | PK, AUTO_INCREMENT | 主键 |
| tenant_id | VARCHAR(64) | FK, NOT NULL | 租户ID |
| role_code | VARCHAR(50) | NOT NULL, UNIQUE(tenant_id, role_code) | 角色编码 |
| role_name | VARCHAR(100) | NOT NULL | 角色名称 |
| description | VARCHAR(500) | | 角色描述 |
| sort_order | INT | DEFAULT 0 | 排序序号 |
| status | VARCHAR(20) | NOT NULL, DEFAULT 'ENABLED' | 状态 |
| deleted | TINYINT | DEFAULT 0 | 逻辑删除标记 |
| created_at | DATETIME | NOT NULL | 创建时间 |
| updated_at | DATETIME | NOT NULL | 更新时间 |
| created_by | VARCHAR(64) | | 创建人ID |
| updated_by | VARCHAR(64) | | 更新人ID |

### 3. sys_menu（系统菜单表，动态路由）

| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| id | BIGINT | PK, AUTO_INCREMENT | 主键 |
| tenant_id | VARCHAR(64) | FK, NOT NULL | 租户ID |
| parent_id | BIGINT | DEFAULT 0 | 父菜单ID（0表示根菜单） |
| menu_name | VARCHAR(50) | NOT NULL | 菜单名称 |
| menu_type | VARCHAR(20) | NOT NULL | 菜单类型（DIRECTORY/MENU/BUTTON） |
| route_path | VARCHAR(200) | | 路由路径 |
| component | VARCHAR(200) | | 组件路径 |
| icon | VARCHAR(100) | | 图标 |
| permission | VARCHAR(100) | | 权限标识 |
| sort_order | INT | DEFAULT 0 | 排序序号 |
| visible | TINYINT | DEFAULT 1 | 是否可见 |
| status | VARCHAR(20) | NOT NULL, DEFAULT 'ENABLED' | 状态 |
| deleted | TINYINT | DEFAULT 0 | 逻辑删除标记 |
| created_at | DATETIME | NOT NULL | 创建时间 |
| updated_at | DATETIME | NOT NULL | 更新时间 |
| created_by | VARCHAR(64) | | 创建人ID |
| updated_by | VARCHAR(64) | | 更新人ID |

### 4. sys_user_role（用户角色关联表）

| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| id | BIGINT | PK, AUTO_INCREMENT | 主键 |
| user_id | VARCHAR(64) | FK, NOT NULL | 用户ID |
| role_id | BIGINT | FK, NOT NULL | 角色ID |
| created_at | DATETIME | NOT NULL | 创建时间 |

**索引**：
- UNIQUE INDEX uk_user_role (user_id, role_id)

### 5. sys_role_menu（角色菜单关联表）

| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| id | BIGINT | PK, AUTO_INCREMENT | 主键 |
| role_id | BIGINT | FK, NOT NULL | 角色ID |
| menu_id | BIGINT | FK, NOT NULL | 菜单ID |
| created_at | DATETIME | NOT NULL | 创建时间 |

**索引**：
- UNIQUE INDEX uk_role_menu (role_id, menu_id)

### 6. sys_permission（权限标识表）

| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| id | BIGINT | PK, AUTO_INCREMENT | 主键 |
| tenant_id | VARCHAR(64) | FK, NOT NULL | 租户ID |
| permission_code | VARCHAR(100) | NOT NULL, UNIQUE(tenant_id, permission_code) | 权限编码 |
| permission_name | VARCHAR(100) | NOT NULL | 权限名称 |
| resource_type | VARCHAR(20) | NOT NULL | 资源类型（API/MENU/BUTTON） |
| resource_path | VARCHAR(200) | | 资源路径 |
| http_method | VARCHAR(20) | | HTTP方法（GET/POST/PUT/DELETE） |
| description | VARCHAR(500) | | 描述 |
| deleted | TINYINT | DEFAULT 0 | 逻辑删除标记 |
| created_at | DATETIME | NOT NULL | 创建时间 |
| updated_at | DATETIME | NOT NULL | 更新时间 |

### 7. role_permission（角色权限关联表）

| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| id | BIGINT | PK, AUTO_INCREMENT | 主键 |
| role_id | BIGINT | FK, NOT NULL | 角色ID |
| permission_id | BIGINT | FK, NOT NULL | 权限ID |
| created_at | DATETIME | NOT NULL | 创建时间 |

**索引**：
- UNIQUE INDEX uk_role_permission (role_id, permission_id)

### 8. audit_log（审计日志表）

| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| id | BIGINT | PK, AUTO_INCREMENT | 主键 |
| tenant_id | VARCHAR(64) | FK, NOT NULL, INDEX | 租户ID |
| user_id | VARCHAR(64) | FK, NOT NULL | 操作人ID |
| operation | VARCHAR(50) | NOT NULL | 操作类型 |
| target_type | VARCHAR(50) | NOT NULL | 目标类型 |
| target_id | BIGINT | NOT NULL | 目标ID |
| before_value | JSON | | 变更前值 |
| after_value | JSON | | 变更后值 |
| ip_address | VARCHAR(50) | | IP地址 |
| user_agent | VARCHAR(500) | | 用户代理 |
| request_context | JSON | | 请求上下文 |
| created_at | DATETIME | NOT NULL, INDEX | 创建时间 |

---

## M03: 网关设置管理

### 1. gateway_route（网关路由配置表）

| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| id | BIGINT | PK, AUTO_INCREMENT | 主键 |
| route_id | VARCHAR(100) | NOT NULL, UNIQUE | 路由ID |
| route_name | VARCHAR(100) | NOT NULL | 路由名称 |
| uri | VARCHAR(500) | NOT NULL | 目标URI |
| predicates | JSON | NOT NULL | 断言配置 |
| filters | JSON | | 过滤器配置 |
| order_num | INT | DEFAULT 0 | 路由顺序 |
| status | VARCHAR(20) | NOT NULL, DEFAULT 'ENABLED' | 状态 |
| description | VARCHAR(500) | | 描述 |
| created_at | DATETIME | NOT NULL | 创建时间 |
| updated_at | DATETIME | NOT NULL | 更新时间 |
| created_by | VARCHAR(64) | | 创建人ID |
| updated_by | VARCHAR(64) | | 更新人ID |
| version | INT | NOT NULL DEFAULT 0 | 乐观锁版本号 |

### 2. gateway_rate_limit（网关限流配置表）

| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| id | BIGINT | PK, AUTO_INCREMENT | 主键 |
| limit_name | VARCHAR(100) | NOT NULL | 限流名称 |
| limit_type | VARCHAR(20) | NOT NULL | 限流类型（IP/API/SERVICE） |
| target_key | VARCHAR(200) | NOT NULL | 限流目标（IP地址/API路径/服务名） |
| limit_count | INT | NOT NULL | 限流次数 |
| time_window_seconds | INT | NOT NULL | 时间窗口（秒） |
| status | VARCHAR(20) | NOT NULL, DEFAULT 'ENABLED' | 状态 |
| created_at | DATETIME | NOT NULL | 创建时间 |
| updated_at | DATETIME | NOT NULL | 更新时间 |
| created_by | VARCHAR(64) | | 创建人ID |
| updated_by | VARCHAR(64) | | 更新人ID |

### 3. gateway_circuit_breaker（熔断器配置表）

| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| id | BIGINT | PK, AUTO_INCREMENT | 主键 |
| breaker_name | VARCHAR(100) | NOT NULL, UNIQUE | 熔断器名称 |
| service_id | VARCHAR(100) | NOT NULL | 服务ID |
| failure_threshold | INT | NOT NULL | 失败阈值 |
| timeout_seconds | INT | NOT NULL | 超时时间 |
| half_open_calls | INT | | 半开状态调用数 |
| fallback_uri | VARCHAR(500) | | 降级URI |
| status | VARCHAR(20) | NOT NULL, DEFAULT 'ENABLED' | 状态 |
| created_at | DATETIME | NOT NULL | 创建时间 |
| updated_at | DATETIME | NOT NULL | 更新时间 |
| created_by | VARCHAR(64) | | 创建人ID |
| updated_by | VARCHAR(64) | | 更新人ID |

---

## M04: 第三方接口管理

### 1. third_party_api（第三方接口表）

| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| id | BIGINT | PK, AUTO_INCREMENT | 主键 |
| tenant_id | VARCHAR(64) | FK, NOT NULL | 租户ID |
| api_name | VARCHAR(100) | NOT NULL | 接口名称 |
| api_code | VARCHAR(50) | NOT NULL, UNIQUE(tenant_id, api_code) | 接口编码 |
| endpoint_url | VARCHAR(500) | NOT NULL | 接口端点 |
| auth_type | VARCHAR(20) | NOT NULL | 认证类型（API_KEY/OAUTH2/BASIC/BEARER） |
| auth_config | JSON | | 认证配置（加密存储） |
| timeout_seconds | INT | DEFAULT 30 | 超时时间 |
| retry_count | INT | DEFAULT 3 | 重试次数 |
| status | VARCHAR(20) | NOT NULL, DEFAULT 'ENABLED' | 状态 |
| health_check_url | VARCHAR(500) | | 健康检查URL |
| health_check_interval | INT | DEFAULT 300 | 健康检查间隔（秒） |
| description | VARCHAR(500) | | 描述 |
| deleted | TINYINT | DEFAULT 0 | 逻辑删除标记 |
| created_at | DATETIME | NOT NULL | 创建时间 |
| updated_at | DATETIME | NOT NULL | 更新时间 |
| created_by | VARCHAR(64) | | 创建人ID |
| updated_by | VARCHAR(64) | | 更新人ID |

### 2. api_key（API密钥表，加密存储）

| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| id | BIGINT | PK, AUTO_INCREMENT | 主键 |
| tenant_id | VARCHAR(64) | FK, NOT NULL | 租户ID |
| api_id | BIGINT | FK, NOT NULL | 关联的第三方API ID |
| key_name | VARCHAR(100) | NOT NULL | 密钥名称 |
| key_value_encrypted | TEXT | NOT NULL | 加密后的密钥值 |
| key_type | VARCHAR(20) | NOT NULL | 密钥类型（PUBLIC/PRIVATE/SECRET） |
| status | VARCHAR(20) | NOT NULL, DEFAULT 'ACTIVE' | 状态 |
| expires_at | DATETIME | | 过期时间 |
| deleted | TINYINT | DEFAULT 0 | 逻辑删除标记 |
| created_at | DATETIME | NOT NULL | 创建时间 |
| updated_at | DATETIME | NOT NULL | 更新时间 |
| created_by | VARCHAR(64) | | 创建人ID |

### 3. api_call_log（API调用日志表）

| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| id | BIGINT | PK, AUTO_INCREMENT | 主键 |
| tenant_id | VARCHAR(64) | FK, NOT NULL, INDEX | 租户ID |
| api_id | BIGINT | FK, NOT NULL, INDEX | 第三方API ID |
| request_id | VARCHAR(64) | NOT NULL, INDEX | 请求ID（UUID） |
| request_method | VARCHAR(10) | NOT NULL | 请求方法（GET/POST等） |
| request_url | VARCHAR(500) | NOT NULL | 请求URL |
| request_headers | JSON | | 请求头 |
| request_body | JSON | | 请求体 |
| response_status | INT | | 响应状态码 |
| response_headers | JSON | | 响应头 |
| response_body | TEXT | | 响应体 |
| duration_ms | BIGINT | | 耗时（毫秒） |
| success | TINYINT | NOT NULL | 是否成功 |
| error_message | TEXT | | 错误信息 |
| created_at | DATETIME | NOT NULL, INDEX | 调用时间 |

### 4. api_health_check（API健康检查记录表）

| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| id | BIGINT | PK, AUTO_INCREMENT | 主键 |
| tenant_id | VARCHAR(64) | FK, NOT NULL | 租户ID |
| api_id | BIGINT | FK, NOT NULL, INDEX | 第三方API ID |
| check_time | DATETIME | NOT NULL, INDEX | 检查时间 |
| status | VARCHAR(20) | NOT NULL | 健康状态（HEALTHY/UNHEALTHY/TIMEOUT） |
| response_time_ms | BIGINT | | 响应时间（毫秒） |
| error_message | TEXT | | 错误信息 |

---

## M05: API开放平台（完整API市场）

### 1. api_definition（API定义表）

| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| id | BIGINT | PK, AUTO_INCREMENT | 主键 |
| tenant_id | VARCHAR(64) | FK, NOT NULL | 租户ID（API提供方） |
| api_name | VARCHAR(100) | NOT NULL | API名称 |
| api_code | VARCHAR(50) | NOT NULL, UNIQUE | API编码 |
| api_version | VARCHAR(20) | NOT NULL | API版本 |
| description | TEXT | | API描述 |
| category | VARCHAR(50) | | API分类 |
| endpoint_url | VARCHAR(500) | NOT NULL | API端点 |
| http_method | VARCHAR(10) | NOT NULL | HTTP方法 |
| request_format | VARCHAR(20) | | 请求格式（JSON/XML/FORM） |
| response_format | VARCHAR(20) | | 响应格式（JSON/XML） |
| price_per_call | DECIMAL(10,4) | DEFAULT 0 | 每次调用价格 |
| free_quota_per_day | INT | DEFAULT 0 | 每日免费额度 |
| rate_limit_per_minute | INT | DEFAULT 60 | 每分钟限流 |
| status | VARCHAR(20) | NOT NULL, DEFAULT 'DRAFT' | 状态（DRAFT/PUBLISHED/DEPRECATED） |
| audit_status | VARCHAR(20) | NOT NULL, DEFAULT 'PENDING' | 审核状态（PENDING/APPROVED/REJECTED） |
| audit_remark | TEXT | | 审核备注 |
| deleted | TINYINT | DEFAULT 0 | 逻辑删除标记 |
| created_at | DATETIME | NOT NULL | 创建时间 |
| updated_at | DATETIME | NOT NULL | 更新时间 |
| created_by | VARCHAR(64) | | 创建人ID |
| updated_by | VARCHAR(64) | | 更新人ID |
| version | INT | NOT NULL DEFAULT 0 | 乐观锁版本号 |

### 2. developer_application（开发者应用表）

| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| id | BIGINT | PK, AUTO_INCREMENT | 主键 |
| tenant_id | VARCHAR(64) | FK, NOT NULL | 租户ID（开发者） |
| app_name | VARCHAR(100) | NOT NULL | 应用名称 |
| app_code | VARCHAR(50) | NOT NULL, UNIQUE(tenant_id, app_code) | 应用编码 |
| app_description | TEXT | | 应用描述 |
| callback_url | VARCHAR(500) | | 回调URL |
| status | VARCHAR(20) | NOT NULL, DEFAULT 'ACTIVE' | 状态（ACTIVE/SUSPENDED） |
| deleted | TINYINT | DEFAULT 0 | 逻辑删除标记 |
| created_at | DATETIME | NOT NULL | 创建时间 |
| updated_at | DATETIME | NOT NULL | 更新时间 |
| created_by | VARCHAR(64) | | 创建人ID |
| updated_by | VARCHAR(64) | | 更新人ID |

### 3. api_subscription（API订阅表）

| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| id | BIGINT | PK, AUTO_INCREMENT | 主键 |
| tenant_id | VARCHAR(64) | FK, NOT NULL, INDEX | 租户ID（订阅方） |
| api_id | BIGINT | FK, NOT NULL, INDEX | API ID |
| app_id | BIGINT | FK, NOT NULL | 应用ID |
| subscription_status | VARCHAR(20) | NOT NULL, DEFAULT 'ACTIVE' | 订阅状态（ACTIVE/SUSPENDED/CANCELLED） |
| subscribe_time | DATETIME | NOT NULL | 订阅时间 |
| unsubscribe_time | DATETIME | | 取消订阅时间 |
| deleted | TINYINT | DEFAULT 0 | 逻辑删除标记 |
| created_at | DATETIME | NOT NULL | 创建时间 |
| updated_at | DATETIME | NOT NULL | 更新时间 |

**索引**：
- UNIQUE INDEX uk_tenant_api_app (tenant_id, api_id, app_id)

### 4. api_usage_record（API使用记录表）

| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| id | BIGINT | PK, AUTO_INCREMENT | 主键 |
| tenant_id | VARCHAR(64) | FK, NOT NULL, INDEX | 租户ID（调用方） |
| api_id | BIGINT | FK, NOT NULL, INDEX | API ID |
| subscription_id | BIGINT | FK, NOT NULL | 订阅ID |
| app_id | BIGINT | FK, NOT NULL | 应用ID |
| call_time | DATETIME | NOT NULL, INDEX | 调用时间 |
| success | TINYINT | NOT NULL | 是否成功 |
| response_time_ms | BIGINT | | 响应时间（毫秒） |
| error_code | VARCHAR(50) | | 错误码 |
| error_message | TEXT | | 错误信息 |

### 5. api_call_billing（API调用计费表）

| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| id | BIGINT | PK, AUTO_INCREMENT | 主键 |
| tenant_id | VARCHAR(64) | FK, NOT NULL, INDEX | 租户ID |
| api_id | BIGINT | FK, NOT NULL, INDEX | API ID |
| billing_date | DATE | NOT NULL, INDEX | 计费日期 |
| total_calls | BIGINT | NOT NULL | 总调用次数 |
| free_calls | BIGINT | DEFAULT 0 | 免费调用次数 |
| paid_calls | BIGINT | DEFAULT 0 | 付费调用次数 |
| unit_price | DECIMAL(10,4) | NOT NULL | 单价 |
| total_amount | DECIMAL(12,2) | NOT NULL | 总金额 |
| billed | TINYINT | DEFAULT 0 | 是否已出账 |
| created_at | DATETIME | NOT NULL | 创建时间 |
| updated_at | DATETIME | NOT NULL | 更新时间 |

**索引**：
- UNIQUE INDEX uk_tenant_api_date (tenant_id, api_id, billing_date)

---

## M06: 权限路由管理

> 注：M06 复用 M02 的 sys_user, sys_role, sys_menu 表，核心是 RBAC 实现

### role_permission（已在M02定义）

复用 M02 的 role_permission 表实现角色与权限的关联。

### 动态路由配置存储

动态路由通过 sys_menu 表实现：
- menu_type = 'DIRECTORY': 目录
- menu_type = 'MENU': 菜单
- menu_type = 'BUTTON': 按钮

前端根据用户的 role_id 加载对应的菜单：
1. 查询用户的所有角色
2. 查询角色关联的菜单（sys_role_menu）
3. 递归构建菜单树

---

## M07: 套餐管理

### 1. saas_package（SaaS套餐表）

| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| id | BIGINT | PK, AUTO_INCREMENT | 主键 |
| package_code | VARCHAR(50) | NOT NULL, UNIQUE | 套餐编码 |
| package_name | VARCHAR(100) | NOT NULL | 套餐名称 |
| description | TEXT | | 套餐描述 |
| billing_cycle | VARCHAR(20) | NOT NULL | 计费周期（MONTHLY/YEARLY） |
| base_price | DECIMAL(12,2) | NOT NULL | 基础价格 |
| currency | VARCHAR(10) | DEFAULT 'CNY' | 货币 |
| max_users | INT | | 最大用户数 |
| max_storage_gb | INT | | 最大存储空间（GB） |
| max_api_calls_per_day | INT | | 每日最大API调用次数 |
| features | JSON | | 套餐功能列表 |
| status | VARCHAR(20) | NOT NULL, DEFAULT 'ACTIVE' | 状态（ACTIVE/INACTIVE） |
| sort_order | INT | DEFAULT 0 | 排序序号 |
| deleted | TINYINT | DEFAULT 0 | 逻辑删除标记 |
| created_at | DATETIME | NOT NULL | 创建时间 |
| updated_at | DATETIME | NOT NULL | 更新时间 |
| created_by | VARCHAR(64) | | 创建人ID |
| updated_by | VARCHAR(64) | | 更新人ID |
| version | INT | NOT NULL DEFAULT 0 | 乐观锁版本号 |

### 2. package_feature（套餐功能明细表）

| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| id | BIGINT | PK, AUTO_INCREMENT | 主键 |
| package_id | BIGINT | FK, NOT NULL | 套餐ID |
| feature_code | VARCHAR(50) | NOT NULL | 功能编码 |
| feature_name | VARCHAR(100) | NOT NULL | 功能名称 |
| feature_value | VARCHAR(200) | | 功能值（如"100GB"） |
| is_included | TINYINT | NOT NULL | 是否包含 |
| created_at | DATETIME | NOT NULL | 创建时间 |
| updated_at | DATETIME | NOT NULL | 更新时间 |

### 3. tenant_subscription（租户订阅表）

| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| id | BIGINT | PK, AUTO_INCREMENT | 主键 |
| tenant_id | VARCHAR(64) | FK, NOT NULL, UNIQUE | 租户ID |
| package_id | BIGINT | FK, NOT NULL | 套餐ID |
| subscription_status | VARCHAR(20) | NOT NULL | 订阅状态（ACTIVE/SUSPENDED/CANCELLED） |
| subscribe_time | DATETIME | NOT NULL | 订阅时间 |
| expire_time | DATETIME | | 到期时间 |
| auto_renew | TINYINT | DEFAULT 0 | 是否自动续费 |
| created_at | DATETIME | NOT NULL | 创建时间 |
| updated_at | DATETIME | NOT NULL | 更新时间 |

---

## M08: 活动管理

### 1. marketing_activity（营销活动表）

| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| id | BIGINT | PK, AUTO_INCREMENT | 主键 |
| activity_code | VARCHAR(50) | NOT NULL, UNIQUE | 活动编码 |
| activity_name | VARCHAR(100) | NOT NULL | 活动名称 |
| activity_type | VARCHAR(20) | NOT NULL | 活动类型（DISCOUNT/TRIAL/GIFT） |
| description | TEXT | | 活动描述 |
| discount_type | VARCHAR(20) | | 折扣类型（PERCENTAGE/FIXED） |
| discount_value | DECIMAL(10,2) | | 折扣值 |
| free_trial_days | INT | | 免费试用天数 |
| start_time | DATETIME | NOT NULL | 开始时间 |
| end_time | DATETIME | NOT NULL | 结束时间 |
| status | VARCHAR(20) | NOT NULL, DEFAULT 'DRAFT' | 状态（DRAFT/ACTIVE/PAUSED/ENDED） |
| max_participants | INT | | 最大参与人数 |
| current_participants | INT | DEFAULT 0 | 当前参与人数 |
| deleted | TINYINT | DEFAULT 0 | 逻辑删除标记 |
| created_at | DATETIME | NOT NULL | 创建时间 |
| updated_at | DATETIME | NOT NULL | 更新时间 |
| created_by | VARCHAR(64) | | 创建人ID |
| updated_by | VARCHAR(64) | | 更新人ID |
| version | INT | NOT NULL DEFAULT 0 | 乐观锁版本号 |

### 2. package_activity（套餐活动关联表，多对多）

| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| id | BIGINT | PK, AUTO_INCREMENT | 主键 |
| package_id | BIGINT | FK, NOT NULL | 套餐ID |
| activity_id | BIGINT | FK, NOT NULL | 活动ID |
| created_at | DATETIME | NOT NULL | 创建时间 |

**索引**：
- UNIQUE INDEX uk_package_activity (package_id, activity_id)

### 3. activity_subscription（活动参与记录表）

| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| id | BIGINT | PK, AUTO_INCREMENT | 主键 |
| tenant_id | VARCHAR(64) | FK, NOT NULL | 租户ID |
| activity_id | BIGINT | FK, NOT NULL | 活动ID |
| subscription_id | BIGINT | FK | 订阅ID |
| join_time | DATETIME | NOT NULL | 参与时间 |
| benefit_received | JSON | | 获得的优惠详情 |
| status | VARCHAR(20) | NOT NULL | 状态（ACTIVE/EXPIRED/USED） |
| expire_time | DATETIME | | 到期时间 |
| created_at | DATETIME | NOT NULL | 创建时间 |

**索引**：
- UNIQUE INDEX uk_tenant_activity (tenant_id, activity_id)

---

## M09: 租户管理

### 1. tenant（租户表）

| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| id | VARCHAR(64) | PK, NOT NULL | 租户ID（UUID） |
| tenant_code | VARCHAR(50) | NOT NULL, UNIQUE | 租户编码 |
| tenant_name | VARCHAR(100) | NOT NULL | 租户名称 |
| contact_name | VARCHAR(100) | | 联系人姓名 |
| contact_email | VARCHAR(100) | | 联系人邮箱 |
| contact_phone | VARCHAR(20) | | 联系人电话 |
| tenant_status | VARCHAR(20) | NOT NULL, DEFAULT 'PENDING' | 租户状态（PENDING/ACTIVE/SUSPENDED/ARREARS/CLOSED） |
| industry | VARCHAR(50) | | 行业 |
| company_size | VARCHAR(20) | | 公司规模 |
| registration_date | DATETIME | NOT NULL | 注册时间 |
| approval_date | DATETIME | | 审核通过日期 |
| approver_id | VARCHAR(64) | | 审核人ID |
| reject_reason | TEXT | | 拒绝原因 |
| deleted | TINYINT | DEFAULT 0 | 逻辑删除标记 |
| created_at | DATETIME | NOT NULL | 创建时间 |
| updated_at | DATETIME | NOT NULL | 更新时间 |

### 2. tenant_quota（租户资源配额表）

| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| id | BIGINT | PK, AUTO_INCREMENT | 主键 |
| tenant_id | VARCHAR(64) | FK, NOT NULL, UNIQUE | 租户ID |
| max_users | INT | DEFAULT 10 | 最大用户数 |
| max_storage_gb | INT | DEFAULT 100 | 最大存储空间（GB） |
| max_api_calls_per_day | BIGINT | DEFAULT 10000 | 每日最大API调用次数 |
| max_concurrent_requests | INT | DEFAULT 100 | 最大并发请求数 |
| created_at | DATETIME | NOT NULL | 创建时间 |
| updated_at | DATETIME | NOT NULL | 更新时间 |

### 3. tenant_resource_usage（租户资源使用记录表）

| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| id | BIGINT | PK, AUTO_INCREMENT | 主键 |
| tenant_id | VARCHAR(64) | FK, NOT NULL, INDEX | 租户ID |
| record_date | DATE | NOT NULL, INDEX | 记录日期 |
| user_count | INT | DEFAULT 0 | 用户数量 |
| storage_used_gb | DECIMAL(10,2) | DEFAULT 0 | 已用存储（GB） |
| api_calls_count | BIGINT | DEFAULT 0 | API调用次数 |
| concurrent_requests_avg | INT | DEFAULT 0 | 平均并发请求数 |
| created_at | DATETIME | NOT NULL | 创建时间 |
| updated_at | DATETIME | NOT NULL | 更新时间 |

**索引**：
- UNIQUE INDEX uk_tenant_date (tenant_id, record_date)

---

## M10: 错误文档查看

> 注：错误日志存储在 Elasticsearch 中，MySQL 仅存储错误分类和解决方案

### 1. error_category（错误分类表）

| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| id | BIGINT | PK, AUTO_INCREMENT | 主键 |
| category_code | VARCHAR(50) | NOT NULL, UNIQUE | 分类编码 |
| category_name | VARCHAR(100) | NOT NULL | 分类名称 |
| parent_id | BIGINT | DEFAULT 0 | 父分类ID |
| description | VARCHAR(500) | | 描述 |
| sort_order | INT | DEFAULT 0 | 排序序号 |
| deleted | TINYINT | DEFAULT 0 | 逻辑删除标记 |
| created_at | DATETIME | NOT NULL | 创建时间 |
| updated_at | DATETIME | NOT NULL | 更新时间 |

### 2. error_solution（错误解决方案表）

| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| id | BIGINT | PK, AUTO_INCREMENT | 主键 |
| error_code | VARCHAR(50) | NOT NULL, INDEX | 错误码 |
| error_message_pattern | VARCHAR(500) | | 错误信息模式（正则） |
| category_id | BIGINT | FK | 分类ID |
| title | VARCHAR(200) | NOT NULL | 问题标题 |
| description | TEXT | | 问题描述 |
| solution | TEXT | NOT NULL | 解决方案 |
| related_service | VARCHAR(100) | | 相关服务 |
| priority | VARCHAR(20) | DEFAULT 'MEDIUM' | 优先级（HIGH/MEDIUM/LOW） |
| status | VARCHAR(20) | NOT NULL, DEFAULT 'PUBLISHED' | 状态（DRAFT/PUBLISHED） |
| view_count | BIGINT | DEFAULT 0 | 查看次数 |
| helpful_count | BIGINT | DEFAULT 0 | 有用次数 |
| deleted | TINYINT | DEFAULT 0 | 逻辑删除标记 |
| created_at | DATETIME | NOT NULL | 创建时间 |
| updated_at | DATETIME | NOT NULL | 更新时间 |
| created_by | VARCHAR(64) | | 创建人ID |
| updated_by | VARCHAR(64) | | 更新人ID |

### Elasticsearch 错误日志索引设计

```json
{
  "index": "error-log-*",
  "mappings": {
    "properties": {
      "tenant_id": { "type": "keyword" },
      "service_name": { "type": "keyword" },
      "error_code": { "type": "keyword" },
      "error_message": { "type": "text" },
      "error_type": { "type": "keyword" },
      "stack_trace": { "type": "text" },
      "request_id": { "type": "keyword" },
      "user_id": { "type": "keyword" },
      "uri": { "type": "keyword" },
      "method": { "type": "keyword" },
      "ip_address": { "type": "ip" },
      "user_agent": { "type": "text" },
      "occurred_at": { "type": "date" },
      "created_at": { "type": "date" }
    }
  }
}
```

---

## M11: 分布式链路追踪

> 注：SkyWalking/Zipkin 数据存储在各自的后端（Elasticsearch/H2），MySQL 存储配置

### 1. trace_config（链路追踪配置表）

| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| id | BIGINT | PK, AUTO_INCREMENT | 主键 |
| service_name | VARCHAR(100) | NOT NULL, UNIQUE | 服务名称 |
| tracing_enabled | TINYINT | NOT NULL, DEFAULT 1 | 是否启用追踪 |
| sampling_rate | DECIMAL(3,2) | DEFAULT 1.00 | 采样率（0.00-1.00） |
| agent_config | JSON | | Agent 配置 |
| created_at | DATETIME | NOT NULL | 创建时间 |
| updated_at | DATETIME | NOT NULL | 更新时间 |

### 2. trace_alert（链路追踪告警配置表）

| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| id | BIGINT | PK, AUTO_INCREMENT | 主键 |
| alert_name | VARCHAR(100) | NOT NULL | 告警名称 |
| service_name | VARCHAR(100) | NOT NULL | 服务名称 |
| metric_type | VARCHAR(20) | NOT NULL | 指标类型（RESPONSE_TIME/ERROR_RATE/THROUGHPUT） |
| threshold_value | DECIMAL(10,2) | NOT NULL | 阈值 |
| comparison_operator | VARCHAR(10) | NOT NULL | 比较操作符（GT/LT/GTE/LTE） |
| time_window_minutes | INT | NOT NULL | 时间窗口（分钟） |
| notification_channels | JSON | | 通知渠道（EMAIL/WEBHOOK/SMS） |
| status | VARCHAR(20) | NOT NULL, DEFAULT 'ENABLED' | 状态 |
| created_at | DATETIME | NOT NULL | 创建时间 |
| updated_at | DATETIME | NOT NULL | 更新时间 |
| created_by | VARCHAR(64) | | 创建人ID |

### 3. trace_dashboard（链路追踪仪表盘配置表）

| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| id | BIGINT | PK, AUTO_INCREMENT | 主键 |
| dashboard_name | VARCHAR(100) | NOT NULL | 仪表盘名称 |
| description | VARCHAR(500) | | 描述 |
| config | JSON | NOT NULL | 仪表盘配置（面板布局、查询等） |
| is_public | TINYINT | DEFAULT 0 | 是否公开 |
| created_at | DATETIME | NOT NULL | 创建时间 |
| updated_at | DATETIME | NOT NULL | 更新时间 |
| created_by | VARCHAR(64) | | 创建人ID |
| updated_by | VARCHAR(64) | | 更新人ID |

---

## M12: 代码生成

### 1. code_template（代码模板表）

| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| id | BIGINT | PK, AUTO_INCREMENT | 主键 |
| template_name | VARCHAR(100) | NOT NULL | 模板名称 |
| template_code | VARCHAR(50) | NOT NULL, UNIQUE | 模板编码 |
| template_type | VARCHAR(20) | NOT NULL | 模板类型（CONTROLLER/SERVICE/MAPPER/VUE） |
| template_content | TEXT | NOT NULL | 模板内容（FreeMarker格式） |
| description | VARCHAR(500) | | 描述 |
| file_path | VARCHAR(200) | | 生成文件路径 |
| status | VARCHAR(20) | NOT NULL, DEFAULT 'ACTIVE' | 状态 |
| version | VARCHAR(20) | DEFAULT '1.0' | 模板版本 |
| deleted | TINYINT | DEFAULT 0 | 逻辑删除标记 |
| created_at | DATETIME | NOT NULL | 创建时间 |
| updated_at | DATETIME | NOT NULL | 更新时间 |
| created_by | VARCHAR(64) | | 创建人ID |
| updated_by | VARCHAR(64) | | 更新人ID |

### 2. gen_history（代码生成历史表）

| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| id | BIGINT | PK, AUTO_INCREMENT | 主键 |
| tenant_id | VARCHAR(64) | FK, NOT NULL | 租户ID |
| table_name | VARCHAR(100) | NOT NULL | 表名 |
| table_comment | VARCHAR(200) | | 表注释 |
| module_name | VARCHAR(50) | | 模块名称 |
| package_name | VARCHAR(100) | | 包名 |
| function_name | VARCHAR(100) | | 功能名称 |
| gen_type | VARCHAR(20) | NOT NULL | 生成类型（CRUD/TREE/MASTER_DETAIL） |
| generated_files | JSON | | 生成的文件列表 |
| download_count | INT | DEFAULT 0 | 下载次数 |
| created_at | DATETIME | NOT NULL, INDEX | 创建时间 |
| created_by | VARCHAR(64) | | 创建人ID |

---

## 通用枚举定义

### 状态枚举

```java
// 通用启用/禁用状态
public enum EnabledStatus {
    ENABLED("启用"),
    DISABLED("禁用");
}

// 用户状态
public enum UserStatus {
    ACTIVE("正常"),
    LOCKED("锁定"),
    DISABLED("禁用");
}

// 用户类型
public enum UserType {
    SUPER_ADMIN("超级管理员"),
    ADMIN("业务管理员"),
    USER("普通用户"),
    DEVELOPER("开发人员");
}

// 租户状态
public enum TenantStatus {
    PENDING("待审核"),
    ACTIVE("正常"),
    SUSPENDED("暂停"),
    ARREARS("欠费"),
    CLOSED("已关闭");
}

// API状态
public enum ApiStatus {
    DRAFT("草稿"),
    PUBLISHED("已发布"),
    DEPRECATED("已废弃");
}

// 活动状态
public enum ActivityStatus {
    DRAFT("草稿"),
    ACTIVE("进行中"),
    PAUSED("暂停"),
    ENDED("已结束");
}
```

---

## 数据完整性约束总结

### 多租户隔离

- 所有业务表包含 `tenant_id` 字段
- MyBatis-Plus 租户插件自动过滤
- Redis 缓存键带租户前缀：`tenant:{tenantId}:*`

### 逻辑删除

- `deleted` 字段：0=正常，1=已删除
- MyBatis-Plus 逻辑删除插件自动过滤

### 乐观锁

- 高频更新表使用 `version` 字段
- 使用 `@Version` 注解自动处理

### 唯一性约束

- 分类编码：同一租户下唯一
- 字典编码：同一租户、同一类型下唯一
- 参数键：同一字典下唯一
- 用户名：同一租户下唯一
- 角色编码：同一租户下唯一
- API编码：全局唯一
- 套餐编码：全局唯一
- 活动编码：全局唯一

---

## Redis 缓存设计

### 缓存键格式（带租户前缀）

```
# 参数字典
tenant:{tenantId}:parameter:dictionary:{id}
tenant:{tenantId}:parameter:dictionary:code:{code}
tenant:{tenantId}:parameter:items:{dictionaryId}
tenant:{tenantId}:parameter:categories

# 用户权限
tenant:{tenantId}:user:{userId}:roles
tenant:{tenantId}:user:{userId}:permissions
tenant:{tenantId}:user:{userId}:menus

# 网关配置
gateway:routes
gateway:rate_limits
gateway:circuit_breakers

# API订阅
tenant:{tenantId}:api:subscriptions
tenant:{tenantId}:api:usage:{date}

# 租户配额
tenant:{tenantId}:quota
tenant:{tenantId}:resource:usage:{date}
```

### 缓存过期时间

| 类型 | 过期时间 |
|------|----------|
| 参数字典缓存 | 1小时 |
| 用户权限缓存 | 30分钟 |
| 网关配置缓存 | 永久（手动失效） |
| API使用统计 | 24小时 |
| 租户配额 | 1小时 |

---

## 数据库分库策略

### 微服务独立数据库

```text
db_admin_framework       - M02后台管理框架
db_parameter_service     - M01参数字典
db_gateway_config        - M03网关设置
db_third_party           - M04第三方接口
db_api_marketplace       - M05 API开放平台
db_permission            - M06权限路由（与admin_framework合并）
db_billing               - M07套餐 + M08活动
db_tenant                - M09租户管理
db_error_doc             - M10错误文档（MySQL部分）
db_codegen               - M12代码生成
```

### 共享表策略

- `tenant` 表：作为中心表，所有服务的数据库都需要此表
- `sys_user`, `sys_role`, `sys_menu`：在 `db_admin_framework` 中管理，其他服务通过 Feign 调用

---

## 完整初始化 SQL 脚本

> 见各模块的 SQL 脚本文件：`scripts/init-{module}-database.sql`
