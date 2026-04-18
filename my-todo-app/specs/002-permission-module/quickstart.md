# Quick Start: 权限模块集成指南

**Feature**: 权限模块
**Date**: 2026-01-10
**Purpose**: 快速集成和使用权限模块

## 概述

本指南帮助开发者快速集成权限模块到新项目或现有项目。

## 前置条件

- Java 17+
- Spring Boot 3.0+
- MySQL 8.0+
- Redis 7.0+
- 现有用户系统和租户系统

## 快速开始

### 1. 添加依赖

在 `pom.xml` 中添加权限模块依赖：

```xml
<dependency>
    <groupId>com.example</groupId>
    <artifactId>permission-starter</artifactId>
    <version>1.0.0</version>
</dependency>
```

### 2. 配置数据库

执行 SQL 脚本初始化权限表：

```bash
mysql -u root -p your_database < sql/permission-schema.sql
```

### 3. 配置 Redis

在 `application.yml` 中配置 Redis：

```yaml
spring:
  redis:
    host: localhost
    port: 6379
    database: 0
    timeout: 3000ms

permission:
  redis:
    key-prefix: "perm:"           # Redis key 前缀
    cache-ttl: 1800              # 缓存过期时间（秒）
    session-ttl: 86400           # 会话过期时间（秒）
```

### 4. 启用权限模块

在主类上添加 `@EnablePermission` 注解：

```java
@SpringBootApplication
@EnablePermission
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```

### 5. 使用权限注解

在需要保护的方法上添加 `@RequiresPermission` 注解：

```java
@RestController
@RequestMapping("/api/users")
public class UserController {

    @GetMapping
    @RequiresPermission("user:view")
    public Result<List<User>> list() {
        // 业务逻辑
    }

    @PostMapping
    @RequiresPermission("user:create")
    public Result<User> create(@RequestBody User user) {
        // 业务逻辑
    }

    @DeleteMapping("/{id}")
    @RequiresPermission({"user:delete", "admin:all"})
    public Result<Void> delete(@PathVariable Long id) {
        // 业务逻辑
    }
}
```

### 6. 数据权限过滤

使用 `@DataPermission` 注解实现数据权限过滤：

```java
@GetMapping
@DataPermission(type = DataPermissionType.TENANT)
public Result<List<Order>> list() {
    // 自动添加租户过滤条件
}
```

### 7. 前端 WebSocket 连接

前端连接 WebSocket 接收会话通知：

```javascript
import { connectWebSocket } from '@/utils/websocket'

const ws = connectWebSocket({
  url: 'ws://localhost:8080/ws/session',
  token: localStorage.getItem('token'),
  onMessage: (message) => {
    if (message.type === 'session:kicked') {
      // 处理被踢出
      alert('账号已在其他设备登录')
      router.push('/login')
    }
  }
})
```

## 权限代码规范

权限代码采用层级结构，格式为：`模块:功能:操作`

### 常见权限代码

| 权限代码 | 名称 | 说明 |
|---------|------|------|
| user:view | 查看用户 | 允许查看用户列表 |
| user:create | 创建用户 | 允许创建新用户 |
| user:update | 更新用户 | 允许更新用户信息 |
| user:delete | 删除用户 | 允许删除用户 |
| role:view | 查看角色 | 允许查看角色列表 |
| role:create | 创建角色 | 允许创建新角色 |
| role:assign | 分配角色 | 允许为用户分配角色 |
| admin:all | 管理员全部 | 超级管理员权限 |

### 自定义权限

创建自定义权限时遵循以下规范：

1. 使用小写字母和冒号
2. 按模块分组
3. 操作使用动词（view, create, update, delete, approve, export 等）

## API 使用示例

### 创建角色

```bash
POST /api/v1/roles
Authorization: Bearer {token}
Content-Type: application/json

{
  "name": "销售经理",
  "code": "sales_manager",
  "description": "销售部门经理",
  "permissionIds": [1, 2, 3, 10, 11]
}
```

### 为用户分配角色

```bash
POST /api/v1/users/{userId}/roles
Authorization: Bearer {token}
Content-Type: application/json

{
  "roleIds": [1, 2]
}
```

### 强制下线用户

```bash
POST /api/v1/sessions/user/{userId}/kick
Authorization: Bearer {token}

# 响应
{
  "code": 200,
  "message": "success",
  "data": {
    "kickedCount": 3
  }
}
```

### 将用户加入黑名单

```bash
POST /api/v1/blacklist
Authorization: Bearer {token}
Content-Type: application/json

{
  "userId": 123,
  "reason": "严重违规操作"
}
```

## 常见问题

### Q: 权限缓存如何刷新？

A: 权限变更时自动刷新缓存，无需手动操作。缓存 TTL 为 30 分钟。

### Q: 如何测试权限？

A: 使用测试用户登录，尝试访问无权限的接口，应返回 403 错误。

### Q: WebSocket 连接失败怎么办？

A: 前端会自动降级到 SSE 或轮询模式，确保消息送达。

### Q: 多租户如何隔离权限？

A: 所有权限数据自动按租户隔离，Redis 缓存 key 包含租户 ID。

## 下一步

- 查看完整 API 文档: `/contracts/`
- 了解数据模型: `data-model.md`
- 查看技术选型: `research.md`
