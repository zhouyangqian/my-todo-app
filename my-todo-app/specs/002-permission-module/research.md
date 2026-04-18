# Research: 权限模块

**Feature**: 权限模块
**Date**: 2026-01-10
**Purpose**: 技术选型和最佳实践研究

## 研究主题

基于功能规范中的技术假设，我们需要验证以下技术选型和最佳实践。

### 1. 权限模型：RBAC + ABPC 混合模式

**决策**: 采用基于角色（RBAC）和基于属性的权限控制（ABPC）混合模型

**理由**:
- RBAC 提供简化的权限管理，通过角色批量授予权限
- ABPC 支持细粒度的数据权限控制（如租户、部门维度）
- 混合模型兼顾管理便利性和灵活性

**技术实现**:
- 后端: Spring Security + 自定义 PermissionEvaluator
- 数据模型: Permission（权限）、Role（角色）、UserRole（关联）、UserPermission（直接授权）
- 权限继承: 支持权限的层级结构（父权限包含子权限）

**替代方案**:
- 纯 RBAC: 不支持数据权限过滤
- 纯 ABPC: 管理复杂度高

### 2. 权限注解与 AOP 拦截

**决策**: 使用 Spring AOP + 自定义注解实现权限验证

**理由**:
- 注解方式声明式编程，代码简洁
- AOP 切面统一处理，避免重复代码
- Spring AOP 与 Spring Boot 无缝集成

**技术实现**:
```java
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequiresPermission {
    String[] value();
    LogicalType logical() default LogicalType.AND;
}
```

**替代方案**:
- 手动在每个方法中检查权限: 代码重复，易遗漏
- 拦截器: 无法处理方法级别的细粒度控制

### 3. 数据权限过滤

**决策**: 使用 MyBatis-Plus 拦截器实现数据权限过滤

**理由**:
- MyBatis-Plus 提供 TenantLineInnerInterceptor 租户插件
- 可扩展实现自定义的数据权限拦截器
- 在 SQL 执行前自动添加过滤条件，对业务透明

**技术实现**:
- 自定义 DataPermissionInterceptor 继承 JsqlParserSupport
- 解析 SQL，自动添加 WHERE 条件
- 支持基于租户、部门、用户维度的过滤

**替代方案**:
- 在业务代码中手动添加过滤条件: 易遗漏，维护困难
- 在数据库视图层实现: 性能较差

### 4. 权限缓存与热更新

**决策**: 使用 Redis + Spring Cache 实现权限缓存，通过消息队列刷新缓存

**理由**:
- Redis 高性能读写，支持分布式场景
- Spring Cache 提供统一的缓存抽象
- 消息队列（Redis Pub/Sub）实现缓存实时刷新

**技术实现**:
- 缓存键格式: `permission:user:{userId}:tenant:{tenantId}`
- 缓存过期时间: 30 分钟（主动刷新时立即失效）
- 权限变更时发布消息: `permission:changed:{userId}`

**替代方案**:
- 无缓存: 每次查询数据库，性能差
- 本地缓存: 分布式环境下缓存不一致

### 5. 实时会话管理

**决策**: 使用 WebSocket + Redis 实现会话管理和实时通知

**理由**:
- WebSocket 双向通信，支持服务器主动推送
- Redis 存储会话状态，支持分布式环境
- 支持 WebSocket 不可用时降级为 SSE

**技术实现**:
- 会话存储: Redis Hash，key 为 `session:{sessionId}`
- 挤号登录: 新会话创建时，通知旧会话下线
- 强制下线: 管理员操作后，通过 WebSocket 推送下线消息

**替代方案**:
- 轮询: 延迟高，资源浪费
- 纯前端 Token 验证: 无法主动撤销会话

### 6. WebSocket 长连接降级策略

**决策**: WebSocket → SSE → 轮询（三级降级）

**理由**:
- WebSocket 最优但需要额外端口配置
- SSE（Server-Sent Events）单向推送，兼容性好
- 轮询作为最后保底方案

**技术实现**:
- 前端优先尝试 WebSocket 连接
- 连接失败后尝试 SSE
- 都失败后使用轮询（5 秒间隔）

### 7. 黑名单状态管理

**决策**: 软删除 + 状态字段，保留历史记录

**理由**:
- 软删除保留审计记录
- 状态字段（生效中/已移除）区分当前状态
- 支持统计和分析

**技术实现**:
- Blacklist 实体包含 status 字段
- 登录时检查黑名单状态
- 缓存黑名单状态，变更时主动刷新

## 技术选型总结

| 技术领域 | 选型 | 理由 |
|---------|------|------|
| 权限模型 | RBAC + ABPC | 兼顾管理和灵活性 |
| 权限验证 | Spring AOP + 注解 | 声明式编程，代码简洁 |
| 数据权限 | MyBatis-Plus 拦截器 | 对业务透明 |
| 权限缓存 | Redis + Spring Cache | 高性能，分布式 |
| 实时通信 | WebSocket + SSE | 双向推送，降级策略 |
| 会话存储 | Redis | 分布式共享 |
| 消息通知 | Redis Pub/Sub | 轻量级消息队列 |

## 集成方案

### 权限模块作为独立服务集成

**方式一**: Maven 依赖（推荐）
```xml
<dependency>
    <groupId>com.example</groupId>
    <artifactId>permission-starter</artifactId>
    <version>1.0.0</version>
</dependency>
```

**方式二**: Spring Boot Starter 自动配置
- 提供 @EnablePermission 注解
- 自动配置 PermissionAspect、PermissionFilter 等
- 支持配置外部化

## 需要注意的点

1. **性能**: 权限验证在每次请求时执行，必须保证 <10ms 响应时间
2. **安全**: 权限配置接口需要超级管理员权限，防止权限篡改
3. **一致性**: 分布式环境下权限缓存必须保持一致
4. **可观测性**: 所有权限验证失败必须记录审计日志
5. **租户隔离**: 所有权限数据必须按租户隔离
