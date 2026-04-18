# Data Model: 用户认证与账号管理

**Feature**: 用户认证与账号管理 (003-user-auth)
**Date**: 2026-01-10
**Purpose**: 数据库设计和实体关系

## 概述

本文档定义用户认证与账号管理模块的数据库表结构、实体关系和验证规则。

## 实体关系图 (ERD)

```
┌─────────────────────┐       ┌─────────────────────┐
│      Tenant         │       │       User          │
├─────────────────────┤       ├─────────────────────┤
│ tenant_id (PK)      │──1──N─│ user_id (PK)        │
│ name                │       │ username            │
│ code                │       │ email               │
│ status              │       │ password_hash       │
│ user_limit          │       │ tenant_id (FK)      │
│ contact_email       │       │ status              │
│ created_at          │       │ last_login_at       │
│ updated_at          │       │ created_at          │
└─────────────────────┘       │ updated_at          │
                              └─────────┬───────────┘
                                        │
                                        │ 1
                                        │
                                        │ N
┌─────────────────────┐       ┌─────────▼───────────┐
│     LoginLog        │       │  TokenBlacklist     │
├─────────────────────┤       ├─────────────────────┤
│ log_id (PK)         │       │ id (PK)             │
│ user_id (FK)        │       │ token (INDEX)       │
│ tenant_id           │       │ user_id (INDEX)     │
│ login_time (INDEX)  │       │ expiry_time (INDEX) │
│ ip_address          │       │ reason              │
│ device_info         │       │ created_at          │
│ result              │       └─────────────────────┘
│ failure_reason      │
└─────────────────────┘

┌─────────────────────┐
│      Captcha        │
├─────────────────────┤
│ id (PK)             │
│ code_hash           │
│ user_id             │
│ created_at (INDEX)  │
│ expiry_time         │
│ used                │
└─────────────────────┘
```

## 数据库表

### 1. tenant (租户表)

租户表存储 SaaS 系统中的组织单位信息。

| 字段名 | 类型 | 长度 | 允许NULL | 默认值 | 约束 | 说明 |
|--------|------|------|----------|--------|------|------|
| tenant_id | BIGINT | - | NO | - | PK, AUTO_INCREMENT | 租户 ID |
| name | VARCHAR | 100 | NO | - | - | 租户名称 |
| code | VARCHAR | 50 | NO | - | UNIQUE | 租户代码 (唯一标识) |
| status | TINYINT | - | NO | 1 | - | 状态 (0=禁用, 1=正常, 2=过期) |
| user_limit | INT | - | NO | 5 | - | 用户数量上限 |
| contact_email | VARCHAR | 100 | NO | - | - | 联系邮箱 |
| contact_phone | VARCHAR | 20 | YES | NULL | - | 联系电话 |
| created_at | DATETIME | - | NO | CURRENT_TIMESTAMP | - | 创建时间 |
| updated_at | DATETIME | - | NO | CURRENT_TIMESTAMP | ON UPDATE CURRENT_TIMESTAMP | 更新时间 |

**索引**:
- PRIMARY KEY: `tenant_id`
- UNIQUE KEY: `uk_code` (`code`)
- INDEX: `idx_status` (`status`)

**验证规则**:
- `name`: 长度 2-100 字符，不能为空
- `code`: 长度 2-50 字符，只能包含小写字母、数字、下划线，全局唯一
- `status`: 只能是 0、1、2
- `user_limit`: 必须 >= 1
- `contact_email`: 必须是有效邮箱格式

**Migration SQL**:

```sql
CREATE TABLE `tenant` (
  `tenant_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '租户 ID',
  `name` VARCHAR(100) NOT NULL COMMENT '租户名称',
  `code` VARCHAR(50) NOT NULL COMMENT '租户代码',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态 (0=禁用, 1=正常, 2=过期)',
  `user_limit` INT NOT NULL DEFAULT 5 COMMENT '用户数量上限',
  `contact_email` VARCHAR(100) NOT NULL COMMENT '联系邮箱',
  `contact_phone` VARCHAR(20) DEFAULT NULL COMMENT '联系电话',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`tenant_id`),
  UNIQUE KEY `uk_code` (`code`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='租户表';
```

### 2. user (用户表)

用户表存储系统用户信息，包括租户管理员和子账号。

| 字段名 | 类型 | 长度 | 允许NULL | 默认值 | 约束 | 说明 |
|--------|------|------|----------|--------|------|------|
| user_id | BIGINT | - | NO | - | PK, AUTO_INCREMENT | 用户 ID |
| username | VARCHAR | 50 | NO | - | - | 用户名 (租户内唯一) |
| email | VARCHAR | 100 | NO | - | UNIQUE | 邮箱 (全局唯一) |
| phone | VARCHAR | 20 | YES | NULL | - | 手机号 |
| password_hash | VARCHAR | 255 | NO | - | - | 密码哈希 (BCrypt) |
| tenant_id | BIGINT | - | NO | - | FK | 所属租户 ID |
| status | TINYINT | - | NO | 1 | - | 状态 (0=禁用, 1=正常, 2=锁定) |
| last_login_at | DATETIME | - | YES | NULL | - | 最后登录时间 |
| last_login_ip | VARCHAR | 50 | YES | NULL | - | 最后登录 IP |
| created_at | DATETIME | - | NO | CURRENT_TIMESTAMP | - | 创建时间 |
| updated_at | DATETIME | - | NO | CURRENT_TIMESTAMP | ON UPDATE CURRENT_TIMESTAMP | 更新时间 |
| deleted | TINYINT | - | NO | 0 | - | 软删除标记 (0=正常, 1=已删除) |

**索引**:
- PRIMARY KEY: `user_id`
- UNIQUE KEY: `uk_email` (`email`)
- UNIQUE KEY: `uk_tenant_username` (`tenant_id`, `username`)
- INDEX: `idx_tenant_id` (`tenant_id`)
- INDEX: `idx_status` (`status`)

**验证规则**:
- `username`: 长度 3-50 字符，只能包含字母、数字、下划线，租户内唯一
- `email`: 必须是有效邮箱格式，全局唯一
- `phone`: 手机号格式（可选）
- `password_hash`: BCrypt 哈希，长度 60 字符
- `status`: 只能是 0、1、2

**Migration SQL**:

```sql
CREATE TABLE `user` (
  `user_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户 ID',
  `username` VARCHAR(50) NOT NULL COMMENT '用户名',
  `email` VARCHAR(100) NOT NULL COMMENT '邮箱',
  `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号',
  `password_hash` VARCHAR(255) NOT NULL COMMENT '密码哈希',
  `tenant_id` BIGINT NOT NULL COMMENT '所属租户 ID',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态 (0=禁用, 1=正常, 2=锁定)',
  `last_login_at` DATETIME DEFAULT NULL COMMENT '最后登录时间',
  `last_login_ip` VARCHAR(50) DEFAULT NULL COMMENT '最后登录 IP',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '软删除标记',
  PRIMARY KEY (`user_id`),
  UNIQUE KEY `uk_email` (`email`),
  UNIQUE KEY `uk_tenant_username` (`tenant_id`, `username`),
  KEY `idx_tenant_id` (`tenant_id`),
  KEY `idx_status` (`status`),
  KEY `idx_deleted` (`deleted`),
  CONSTRAINT `fk_user_tenant` FOREIGN KEY (`tenant_id`) REFERENCES `tenant` (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';
```

### 3. login_log (登录日志表)

登录日志表记录用户登录尝试信息，用于安全审计。

| 字段名 | 类型 | 长度 | 允许NULL | 默认值 | 约束 | 说明 |
|--------|------|------|----------|--------|------|------|
| log_id | BIGINT | - | NO | - | PK, AUTO_INCREMENT | 日志 ID |
| user_id | BIGINT | - | YES | NULL | FK | 用户 ID (失败时可能为 NULL) |
| tenant_id | BIGINT | - | NO | - | - | 租户 ID |
| login_time | DATETIME | - | NO | CURRENT_TIMESTAMP | INDEX | 登录时间 |
| ip_address | VARCHAR | 50 | NO | - | - | IP 地址 |
| device_info | VARCHAR | 200 | YES | NULL | - | 设备信息 (User-Agent) |
| result | TINYINT | - | NO | - | - | 结果 (0=失败, 1=成功) |
| failure_reason | VARCHAR | 100 | YES | NULL | - | 失败原因 |

**索引**:
- PRIMARY KEY: `log_id`
- INDEX: `idx_user_id` (`user_id`)
- INDEX: `idx_tenant_id` (`tenant_id`)
- INDEX: `idx_login_time` (`login_time`)
- INDEX: `idx_result` (`result`)

**验证规则**:
- `result`: 只能是 0 (失败) 或 1 (成功)
- `failure_reason`: 仅当 result=0 时有值

**Migration SQL**:

```sql
CREATE TABLE `login_log` (
  `log_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '日志 ID',
  `user_id` BIGINT DEFAULT NULL COMMENT '用户 ID',
  `tenant_id` BIGINT NOT NULL COMMENT '租户 ID',
  `login_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '登录时间',
  `ip_address` VARCHAR(50) NOT NULL COMMENT 'IP 地址',
  `device_info` VARCHAR(200) DEFAULT NULL COMMENT '设备信息',
  `result` TINYINT NOT NULL COMMENT '结果 (0=失败, 1=成功)',
  `failure_reason` VARCHAR(100) DEFAULT NULL COMMENT '失败原因',
  PRIMARY KEY (`log_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_tenant_id` (`tenant_id`),
  KEY `idx_login_time` (`login_time`),
  KEY `idx_result` (`result`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='登录日志表';
```

### 4. token_blacklist (Token 黑名单表)

Token 黑名单表记录已失效的 Token，用于拒绝已登出或已刷新的 Token。

| 字段名 | 类型 | 长度 | 允许NULL | 默认值 | 约束 | 说明 |
|--------|------|------|----------|--------|------|------|
| id | BIGINT | - | NO | - | PK, AUTO_INCREMENT | 记录 ID |
| token | VARCHAR | 500 | NO | - | INDEX | Token 字符串 (JWT) |
| user_id | BIGINT | - | NO | - | INDEX | 用户 ID |
| expiry_time | DATETIME | - | NO | - | INDEX | Token 过期时间 (TTL) |
| reason | VARCHAR | 50 | NO | - | - | 失效原因 (LOGOUT/REFRESH/FORCED) |
| created_at | DATETIME | - | NO | CURRENT_TIMESTAMP | - | 创建时间 |

**索引**:
- PRIMARY KEY: `id`
- INDEX: `idx_token` (`token`(191)) - 前缀索引，避免索引长度超限
- INDEX: `idx_user_id` (`user_id`)
- INDEX: `idx_expiry_time` (`expiry_time`)

**验证规则**:
- `reason`: 只能是 LOGOUT (登出)、REFRESH (刷新)、FORCED (强制下线)

**Migration SQL**:

```sql
CREATE TABLE `token_blacklist` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '记录 ID',
  `token` VARCHAR(500) NOT NULL COMMENT 'Token 字符串',
  `user_id` BIGINT NOT NULL COMMENT '用户 ID',
  `expiry_time` DATETIME NOT NULL COMMENT 'Token 过期时间',
  `reason` VARCHAR(50) NOT NULL COMMENT '失效原因',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_token` (`token`(191)),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_expiry_time` (`expiry_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Token 黑名单表';

-- 清理过期 Token 的定时任务 (每天执行一次)
DELETE FROM `token_blacklist` WHERE `expiry_time` < NOW();
```

### 5. captcha (验证码表)

验证码表存储登录验证码信息。

| 字段名 | 类型 | 长度 | 允许NULL | 默认值 | 约束 | 说明 |
|--------|------|------|----------|--------|------|------|
| id | BIGINT | - | NO | - | PK, AUTO_INCREMENT | 验证码 ID |
| code_hash | VARCHAR | 64 | NO | - | - | 验证码哈希 (SHA-256) |
| user_id | BIGINT | - | YES | NULL | - | 用户 ID (可选，用于关联) |
| created_at | DATETIME | - | NO | CURRENT_TIMESTAMP | INDEX | 创建时间 |
| expiry_time | DATETIME | - | NO | - | - | 过期时间 |
| used | TINYINT | - | NO | 0 | - | 是否已使用 (0=未使用, 1=已使用) |

**索引**:
- PRIMARY KEY: `id`
- INDEX: `idx_created_at` (`created_at`)
- INDEX: `idx_user_id` (`user_id`)

**验证规则**:
- `code_hash`: SHA-256 哈希，固定 64 字符
- `used`: 只能是 0 (未使用) 或 1 (已使用)

**Migration SQL**:

```sql
CREATE TABLE `captcha` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '验证码 ID',
  `code_hash` VARCHAR(64) NOT NULL COMMENT '验证码哈希',
  `user_id` BIGINT DEFAULT NULL COMMENT '用户 ID',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `expiry_time` DATETIME NOT NULL COMMENT '过期时间',
  `used` TINYINT NOT NULL DEFAULT 0 COMMENT '是否已使用',
  PRIMARY KEY (`id`),
  KEY `idx_created_at` (`created_at`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='验证码表';

-- 清理过期验证码的定时任务 (每小时执行一次)
DELETE FROM `captcha` WHERE `expiry_time` < NOW();
```

## 实体类定义

### Tenant.java

```java
@TableName("tenant")
public class Tenant {

    @TableId(type = IdType.AUTO)
    private Long tenantId;

    private String name;

    private String code;

    private Integer status;

    private Integer userLimit;

    private String contactEmail;

    private String contactPhone;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
```

### User.java

```java
@TableName("user")
public class User {

    @TableId(type = IdType.AUTO)
    private Long userId;

    private String username;

    private String email;

    private String phone;

    private String passwordHash;

    @TableField("tenant_id")
    private Long tenantId;

    private Integer status;

    @TableField("last_login_at")
    private LocalDateTime lastLoginAt;

    @TableField("last_login_ip")
    private String lastLoginIp;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableLogic
    @TableField("deleted")
    private Integer deleted;
}
```

### LoginLog.java

```java
@TableName("login_log")
public class LoginLog {

    @TableId(type = IdType.AUTO)
    private Long logId;

    @TableField("user_id")
    private Long userId;

    @TableField("tenant_id")
    private Long tenantId;

    @TableField("login_time")
    private LocalDateTime loginTime;

    @TableField("ip_address")
    private String ipAddress;

    @TableField("device_info")
    private String deviceInfo;

    private Integer result;

    @TableField("failure_reason")
    private String failureReason;
}
```

### TokenBlacklist.java

```java
@TableName("token_blacklist")
public class TokenBlacklist {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String token;

    @TableField("user_id")
    private Long userId;

    @TableField("expiry_time")
    private LocalDateTime expiryTime;

    private String reason;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
```

### Captcha.java

```java
@TableName("captcha")
public class Captcha {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String codeHash;

    @TableField("user_id")
    private Long userId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField("expiry_time")
    private LocalDateTime expiryTime;

    private Integer used;
}
```

## 数据一致性

### 1. 租户注册事务

```java
@Transactional
public void registerTenant(Tenant tenant, User admin) {
    // 1. 创建租户
    tenantMapper.insert(tenant);

    // 2. 创建管理员账户
    admin.setTenantId(tenant.getTenantId());
    userMapper.insert(admin);

    // 3. 分配默认角色 (调用 002-permission-module)
    roleService.assignDefaultRole(admin.getUserId(), tenant.getTenantId());
}
```

### 2. 软删除

用户表使用软删除 (`deleted` 字段)，所有查询需要过滤已删除记录：

```java
@TableName("user")
public class User {
    @TableLogic
    @TableField("deleted")
    private Integer deleted;
}
```

### 3. 审计日志

所有账号操作记录到 `login_log` 表，用于安全审计：

```java
@Aspect
@Component
public class AuditLogAspect {

    @AfterReturning(pointcut = "@annotation(AuditLog)")
    public void logAfterReturning(JoinPoint joinPoint) {
        // 记录操作日志
    }
}
```

## 下一步

- 查看技术研究: `research.md`
- 查看快速开始指南: `quickstart.md`
- 查看 API 契约: `contracts/`
