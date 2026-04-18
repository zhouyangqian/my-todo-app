# Data Model: 权限模块

**Feature**: 权限模块
**Date**: 2026-01-10
**Purpose**: 定义权限模块的核心数据实体和关系

## 实体关系图 (ERD)

```
┌─────────────┐         ┌─────────────┐         ┌─────────────┐
│   Tenant    │         │   User      │         │ Permission  │
├─────────────┤         ├─────────────┤         ├─────────────┤
│ id (PK)     │<--------│ id (PK)     │         │ id (PK)     │
│ name        │ 1     N │ username    │         │ code        │
│ status      │         │ email       │         │ name        │
└─────────────┘         │ tenantId(FK)│         │ description │
                        └─────────────┘         │ parentId    │
                                                 └─────────────┘
                                                       │
                                                      │ 1     N
                       ┌─────────────┐                │
                       │    Role     │<───────────────┘
                       ├─────────────┤
                       │ id (PK)     │         ┌─────────────┐
                       │ name        │<--------│ UserRole    │
                       │ tenantId(FK)│ 1     N ├─────────────┤
                       │ status      │         │ userId (FK) │
                       └─────────────┘         │ roleId (FK) │
                                               │ assignedAt  │
                                               └─────────────┘

┌─────────────┐         ┌─────────────┐
│  Session    │         │ Blacklist   │
├─────────────┤         ├─────────────┤
│ id (PK)     │         │ id (PK)     │
│ userId (FK) │         │ userId (FK) │
│ tenantId(FK)│         │ tenantId(FK)│
│ loginTime   │         │ addedAt     │
│ lastActive  │         │ removedAt   │
│ deviceInfo  │         │ reason      │
│ ipAddress   │         │ operatorId  │
│ status      │         │ status      │
└─────────────┘         └─────────────┘
```

## 核心实体定义

### 1. Permission (权限)

系统中可执行的操作或可访问的资源，支持层级结构。

| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| id | BIGINT | PK, AUTO | 主键 |
| code | VARCHAR(100) | NOT NULL, UNIQUE | 权限代码（如 "user:create"） |
| name | VARCHAR(100) | NOT NULL | 权限名称 |
| description | VARCHAR(500) | | 权限描述 |
| parentId | BIGINT | FK → Permission.id | 父权限 ID（支持层级） |
| level | INT | NOT NULL | 层级深度（0 为顶级） |
| createdAt | DATETIME | NOT NULL | 创建时间 |
| updatedAt | DATETIME | NOT NULL | 更新时间 |

**索引**:
- idx_code: code (UNIQUE)
- idx_parentId: parentId

### 2. Role (角色)

权限的集合，由租户管理员创建和管理。

| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| id | BIGINT | PK, AUTO | 主键 |
| name | VARCHAR(100) | NOT NULL | 角色名称 |
| code | VARCHAR(100) | NOT NULL | 角色代码 |
| description | VARCHAR(500) | | 角色描述 |
| tenantId | BIGINT | NOT NULL, FK → Tenant.id | 所属租户 |
| status | TINYINT | NOT NULL, DEFAULT 1 | 状态（1=启用, 0=禁用） |
| isSystem | TINYINT | NOT NULL, DEFAULT 0 | 是否系统角色 |
| createdAt | DATETIME | NOT NULL | 创建时间 |
| updatedAt | DATETIME | NOT NULL | 更新时间 |

**索引**:
- idx_tenant_code: tenantId, code (UNIQUE)
- idx_tenantId: tenantId

### 3. UserRole (用户-角色关联)

用户和角色之间的多对多关系。

| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| id | BIGINT | PK, AUTO | 主键 |
| userId | BIGINT | NOT NULL, FK → User.id | 用户 ID |
| roleId | BIGINT | NOT NULL, FK → Role.id | 角色 ID |
| tenantId | BIGINT | NOT NULL, FK → Tenant.id | 租户 ID |
| assignedAt | DATETIME | NOT NULL | 分配时间 |
| assignedBy | BIGINT | FK → User.id | 分配人 ID |

**索引**:
- idx_user_role: userId, roleId (UNIQUE)
- idx_tenantId: tenantId

### 4. UserPermission (用户-权限直接关联)

用户和权限之间的直接关联，用于直接授予权限（不通过角色）。

| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| id | BIGINT | PK, AUTO | 主键 |
| userId | BIGINT | NOT NULL, FK → User.id | 用户 ID |
| permissionId | BIGINT | NOT NULL, FK → Permission.id | 权限 ID |
| tenantId | BIGINT | NOT NULL, FK → Tenant.id | 租户 ID |
| grantedAt | DATETIME | NOT NULL | 授予时间 |
| grantedBy | BIGINT | FK → User.id | 授予人 ID |

**索引**:
- idx_user_permission: userId, permissionId (UNIQUE)
- idx_tenantId: tenantId

### 5. Session (会话)

用户的登录会话信息。

| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| id | VARCHAR(64) | PK, NOT NULL | 会话 ID（UUID） |
| userId | BIGINT | NOT NULL, FK → User.id | 用户 ID |
| tenantId | BIGINT | NOT NULL, FK → Tenant.id | 租户 ID |
| loginTime | DATETIME | NOT NULL | 登录时间 |
| lastActive | DATETIME | NOT NULL | 最后活跃时间 |
| deviceInfo | VARCHAR(200) | | 设备信息 |
| ipAddress | VARCHAR(50) | | IP 地址 |
| userAgent | VARCHAR(500) | | User-Agent |
| status | TINYINT | NOT NULL, DEFAULT 1 | 状态（1=活跃, 0=已下线） |
| createdAt | DATETIME | NOT NULL | 创建时间 |
| updatedAt | DATETIME | NOT NULL | 更新时间 |

**索引**:
- idx_user_tenant: userId, tenantId
- idx_status: status
- idx_lastActive: lastActive

**TTL**: Redis 中设置 24 小时过期，自动清理

### 6. Blacklist (黑名单)

被禁用的用户列表。

| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| id | BIGINT | PK, AUTO | 主键 |
| userId | BIGINT | NOT NULL, FK → User.id | 用户 ID |
| tenantId | BIGINT | NOT NULL, FK → Tenant.id | 租户 ID |
| addedAt | DATETIME | NOT NULL | 加入时间 |
| removedAt | DATETIME | | 移除时间 |
| reason | VARCHAR(500) | | 加入原因 |
| operatorId | BIGINT | NOT NULL, FK → User.id | 操作人 ID |
| operatorType | TINYINT | NOT NULL | 操作人类型（1=系统管理员, 2=租户管理员） |
| status | TINYINT | NOT NULL, DEFAULT 1 | 状态（1=生效中, 0=已移除） |
| createdAt | DATETIME | NOT NULL | 创建时间 |
| updatedAt | DATETIME | NOT NULL | 更新时间 |

**索引**:
- idx_user_tenant: userId, tenantId, status (UNIQUE)
- idx_tenantId: tenantId

### 7. PermissionLog (权限验证日志)

记录所有权限验证操作，用于审计。

| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| id | BIGINT | PK, AUTO | 主键 |
| userId | BIGINT | NOT NULL, FK → User.id | 用户 ID |
| tenantId | BIGINT | NOT NULL, FK → Tenant.id | 租户 ID |
| resource | VARCHAR(200) | NOT NULL | 资源（如 API 路径） |
| action | VARCHAR(50) | NOT NULL | 操作（如 GET, POST） |
| permission | VARCHAR(100) | | 所需权限 |
| result | TINYINT | NOT NULL | 结果（1=通过, 0=拒绝） |
| reason | VARCHAR(500) | | 拒绝原因 |
| ipAddress | VARCHAR(50) | | IP 地址 |
| createdAt | DATETIME | NOT NULL | 创建时间 |

**索引**:
- idx_user_tenant: userId, tenantId
- idx_createdAt: createdAt
- idx_result: result

**分区**: 按月分区，保留最近 6 个月数据

## 多租户隔离策略

### 数据隔离
- 所有核心表（Role, UserRole, UserPermission, Session, Blacklist, PermissionLog）包含 `tenantId` 字段
- 查询时自动添加租户过滤条件

### 缓存隔离
- Redis key 格式: `{tenantId}:{entityType}:{entityId}`
- 示例: `1:permission:123`, `1:user:session:abc`

### 会话隔离
- 会话包含租户上下文
- Token 中包含租户 ID
- 切换租户需要重新登录

## 数据权限过滤维度

支持的数据权限过滤维度：

1. **租户级别**: 只能看到本租户数据
2. **部门级别**: 只能看到本部门及子部门数据
3. **用户级别**: 只能看到自己的数据

扩展点：可通过 `DataPermissionProvider` 接口扩展自定义过滤维度
