# Data Model: 用户模块

**Feature**: 001-user-module
**Date**: 2026-01-10
**Purpose**: 定义用户管理模块的数据库表结构和实体关系

## Entity Relationship Diagram

```
┌─────────────┐         ┌─────────────┐         ┌─────────────┐
│   Tenant    │         │    User     │         │    Role     │
├─────────────┤         ├─────────────┤         ├─────────────┤
│ tenant_id   │───1:N───│ user_id     │───N:M───│ role_id     │
│ name        │         │ username    │         │ name        │
│ status      │         │ email       │         │ description │
│ user_limit  │         │ tenant_id   │         │ tenant_id   │
└─────────────┘         │ status      │         └─────────────┘
                       │ deleted     │              ▲
                       └─────────────┘              │
                                                  │
                       ┌─────────────┐              │
                       │ UserRole    │──────────────┘
                       ├─────────────┤
                       │ user_id     │
                       │ role_id     │
                       │ tenant_id   │
                       └─────────────┘

┌─────────────┐         ┌─────────────┐
│ AuditLog    │         │ UserPermission│
├─────────────┤         ├─────────────┤
│ log_id      │         │ id          │
│ operator_id │         │ user_id     │
│ target_id   │         │ permission_id│
│ operation   │         │ tenant_id   │
│ tenant_id   │         └─────────────┘
└─────────────┘
```

## Database Tables

### 1. user (用户表)

| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| user_id | BIGINT | PK, AUTO | 主键 |
| username | VARCHAR(50) | NOT NULL, UNIQUE | 用户名（租户内唯一） |
| email | VARCHAR(100) | NOT NULL, UNIQUE | 邮箱（租户内唯一） |
| phone | VARCHAR(20) | NULL | 手机号 |
| password_hash | VARCHAR(255) | NOT NULL | 密码哈希（bcrypt） |
| tenant_id | BIGINT | NOT NULL, FK | 所属租户 |
| status | TINYINT | NOT NULL, DEFAULT 1 | 状态：1=正常, 0=禁用, 2=锁定 |
| deleted | TINYINT | NOT NULL, DEFAULT 0 | 软删除：0=正常, 1=已删除 |
| created_at | DATETIME | NOT NULL | 创建时间 |
| updated_at | DATETIME | NOT NULL | 更新时间 |
| last_login_at | DATETIME | NULL | 最后登录时间 |

**Indexes**:
- `idx_tenant_username`: (tenant_id, username) - 租户用户名唯一索引
- `idx_tenant_email`: (tenant_id, email) - 租户邮箱唯一索引
- `idx_tenant_status`: (tenant_id, status, deleted) - 租户用户查询

**Validation Rules**:
- username: 3-50 字符，只允许字母、数字、下划线
- email: 标准邮箱格式
- password_hash: bcrypt 加密，长度 60 字符

### 2. role (角色表)

| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| role_id | BIGINT | PK, AUTO | 主键 |
| role_name | VARCHAR(50) | NOT NULL | 角色名称 |
| role_code | VARCHAR(50) | NOT NULL, UNIQUE | 角色代码（租户内唯一） |
| description | VARCHAR(255) | NULL | 角色描述 |
| tenant_id | BIGINT | NOT NULL, FK | 所属租户 |
| status | TINYINT | NOT NULL, DEFAULT 1 | 状态：1=启用, 0=禁用 |
| is_system | TINYINT | NOT NULL, DEFAULT 0 | 是否系统角色：0=自定义, 1=系统预定义 |
| created_at | DATETIME | NOT NULL | 创建时间 |
| updated_at | DATETIME | NOT NULL | 更新时间 |

**Indexes**:
- `idx_tenant_code`: (tenant_id, role_code) - 租户角色代码唯一索引

### 3. user_role (用户-角色关联表)

| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| id | BIGINT | PK, AUTO | 主键 |
| user_id | BIGINT | NOT NULL, FK → user.user_id | 用户 ID |
| role_id | BIGINT | NOT NULL, FK → role.role_id | 角色 ID |
| tenant_id | BIGINT | NOT NULL, FK | 租户 ID |
| created_at | DATETIME | NOT NULL | 分配时间 |

**Indexes**:
- `idx_user_tenant`: (user_id, tenant_id) - 用户角色查询
- `idx_role_tenant`: (role_id, tenant_id) - 角色用户查询
- `uk_user_role_tenant`: (user_id, role_id, tenant_id) - 唯一约束

### 4. user_permission (用户-权限直接关联表)

| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| id | BIGINT | PK, AUTO | 主键 |
| user_id | BIGINT | NOT NULL, FK → user.user_id | 用户 ID |
| permission_id | BIGINT | NOT NULL, FK → permission.id | 权限 ID |
| tenant_id | BIGINT | NOT NULL, FK | 租户 ID |
| created_at | DATETIME | NOT NULL | 授予时间 |

**Note**: 此表用于直接授予用户额外权限（不通过角色）

### 5. audit_log (审计日志表)

| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| log_id | BIGINT | PK, AUTO | 主键 |
| tenant_id | BIGINT | NOT NULL, FK | 租户 ID |
| operator_id | BIGINT | NOT NULL, FK → user.user_id | 操作人 ID |
| operation_type | VARCHAR(50) | NOT NULL | 操作类型：CREATE/UPDATE/DELETE/ASSIGN_ROLE等 |
| target_type | VARCHAR(50) | NOT NULL | 目标类型：USER/ROLE/PERMISSION |
| target_id | BIGINT | NOT NULL | 目标 ID |
| operation_detail | TEXT | NULL | 操作详情（JSON 格式） |
| result | VARCHAR(20) | NOT NULL | 操作结果：SUCCESS/FAILED |
| error_message | VARCHAR(500) | NULL | 错误信息 |
| ip_address | VARCHAR(50) | NULL | 操作 IP |
| created_at | DATETIME | NOT NULL | 操作时间 |

**Indexes**:
- `idx_tenant_created`: (tenant_id, created_at) - 租户审计日志查询
- `idx_operator_created`: (operator_id, created_at) - 操作人日志查询

## Entity States

### User Status

| 状态码 | 名称 | 说明 |
|--------|------|------|
| 1 | NORMAL | 正常，可以登录 |
| 0 | DISABLED | 禁用，无法登录 |
| 2 | LOCKED | 锁定，无法登录（连续登录失败） |

### Deleted Flag

| 值 | 名称 | 说明 |
|----|------|------|
| 0 | ACTIVE | 正常 |
| 1 | DELETED | 已软删除 |

## Data Validation

### Username (用户名)

- 格式：`^[a-zA-Z0-9_]{3,50}$`
- 规则：
  - 长度：3-50 字符
  - 只允许字母、数字、下划线
  - 租户内唯一

### Email (邮箱)

- 格式：标准邮箱正则
- 规则：
  - 租户内唯一
  - 最大 100 字符

### Password (密码)

- 存储：bcrypt 哈希
- 规则（由 003-user-auth 模块验证）：
  - 最少 8 位
  - 必须包含字母和数字

## Migration Scripts

### 创建表

```sql
-- 用户表
CREATE TABLE user (
    user_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL,
    email VARCHAR(100) NOT NULL,
    phone VARCHAR(20),
    password_hash VARCHAR(255) NOT NULL,
    tenant_id BIGINT NOT NULL,
    status TINYINT NOT NULL DEFAULT 1 COMMENT '1=正常, 0=禁用, 2=锁定',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '0=正常, 1=已删除',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    last_login_at DATETIME,
    UNIQUE KEY uk_tenant_username (tenant_id, username),
    UNIQUE KEY uk_tenant_email (tenant_id, email),
    KEY idx_tenant_status (tenant_id, status, deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 角色表
CREATE TABLE role (
    role_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    role_name VARCHAR(50) NOT NULL,
    role_code VARCHAR(50) NOT NULL,
    description VARCHAR(255),
    tenant_id BIGINT NOT NULL,
    status TINYINT NOT NULL DEFAULT 1 COMMENT '1=启用, 0=禁用',
    is_system TINYINT NOT NULL DEFAULT 0 COMMENT '0=自定义, 1=系统预定义',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_tenant_code (tenant_id, role_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

-- 用户-角色关联表
CREATE TABLE user_role (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    tenant_id BIGINT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES user(user_id),
    FOREIGN KEY (role_id) REFERENCES role(role_id),
    UNIQUE KEY uk_user_role_tenant (user_id, role_id, tenant_id),
    KEY idx_user_tenant (user_id, tenant_id),
    KEY idx_role_tenant (role_id, tenant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户-角色关联表';

-- 用户-权限直接关联表
CREATE TABLE user_permission (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    permission_id BIGINT NOT NULL,
    tenant_id BIGINT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES user(user_id),
    FOREIGN KEY (permission_id) REFERENCES permission(id),
    KEY idx_user_tenant (user_id, tenant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户-权限关联表';

-- 审计日志表
CREATE TABLE audit_log (
    log_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    operator_id BIGINT NOT NULL,
    operation_type VARCHAR(50) NOT NULL,
    target_type VARCHAR(50) NOT NULL,
    target_id BIGINT NOT NULL,
    operation_detail TEXT,
    result VARCHAR(20) NOT NULL,
    error_message VARCHAR(500),
    ip_address VARCHAR(50),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    KEY idx_tenant_created (tenant_id, created_at),
    KEY idx_operator_created (operator_id, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='审计日志表';
```

## Data Access Patterns

### MyBatis-Plus Entity Examples

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

    @TableLogic
    @TableField("deleted")
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    private LocalDateTime lastLoginAt;
}
```

### Tenant Isolation Strategy

所有查询自动添加 `WHERE tenant_id = ?` 和 `WHERE deleted = 0`：

```java
// MyBatis-Plus 自动生成的 SQL
SELECT * FROM user
WHERE tenant_id = 123
  AND deleted = 0
  AND status = 1
ORDER BY created_at DESC
LIMIT 10 OFFSET 0;
```
