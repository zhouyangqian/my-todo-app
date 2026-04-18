# Research: Admin Management Platform

**Feature**: Admin Management Platform
**Date**: 2026-01-28
**Status**: Complete

## 架构决策

### 1. 微服务拆分策略

**决策**：按业务领域拆分为12个独立微服务

**理由**：
- 每个模块职责清晰，符合单一职责原则
- 独立部署和扩展，互不影响
- 技术栈可以根据模块特点灵活选择
- 便于团队并行开发

**服务清单**：
- admin-framework: 后台管理框架服务
- parameter-service: 参数字典服务
- gateway-service: 网关配置服务
- third-party-service: 第三方接口管理服务
- api-market-service: API开放平台服务
- permission-service: 权限路由服务
- billing-service: 套餐活动服务
- tenant-service: 租户管理服务
- error-doc-service: 错误文档服务
- tracing-service: 分布式追踪服务
- codegen-service: 代码生成服务

### 2. 后台管理框架 (M02)

**决策**：使用 Spring Security + JWT 实现统一认证，Vue 3.0 实现动态菜单和布局

**理由**：
- Spring Security 是 Java 生态标准，与 Spring Boot 无缝集成
- JWT 令牌无状态，适合微服务架构
- Vue 3.0 组合式 API 提供更好的类型支持
- 动态菜单支持灵活的权限控制

**关键组件**：
- 统一认证入口：/api/v1/auth/login
- 动态菜单配置：存储在数据库，按角色加载
- 布局框架：Header + Sidebar + Main Content
- 主题切换：支持亮色/暗色主题

### 3. 网关设置管理 (M03)

**决策**：基于 Spring Cloud Gateway Admin 实现网关配置管理

**理由**：
- Spring Cloud Gateway 是现有架构的一部分
- 提供 Actuator 端点用于动态路由配置
- 支持路由、限流、熔断的可视化管理

**关键功能**：
- 路由配置：添加/修改/删除路由规则
- 限流规则：配置 IP 限流、API 限流
- 熔断降级：配置熔断器和降级策略
- 配置热更新：无需重启网关

### 4. 第三方接口管理 (M04)

**决策**：使用 HTTP 客户端池 + 密钥加密存储

**理由**：
- Apache HttpClient 或 OkHttp 作为 HTTP 客户端
- 连接池提高性能，减少资源消耗
- AES 加密存储第三方 API 密钥
- 调用日志记录所有第三方交互

**关键功能**：
- 接口注册：接口名称、端点、认证方式
- 密钥管理：AES 加密存储，权限控制访问
- 调用日志：记录每次调用的请求、响应、耗时
- 健康检查：定期检查第三方接口可用性

### 5. API开放平台 (M05)

**决策**：完整的 API 市场模式，支持 API 订阅和按调用次数计费

**理由**：
- API 市场是 SaaS 平台的标准功能
- 多租户环境下需要租户级别的 API 密钥管理
- 计费功能需要准确记录每个租户的调用次数
- 开发者门户提供良好的用户体验

**关键功能**：
- API 目录：展示所有可订阅的 API
- 应用管理：开发者创建应用、获取 API 密钥
- 订阅管理：订阅/取消订阅 API
- 计费系统：按调用次数计费，支持免费额度
- 使用统计：实时查询 API 调用次数和费用
- 审核流程：新 API 上架需要审核

### 6. 权限路由管理 (M06)

**决策**：基于 RBAC + 动态路由的权限系统

**理由**：
- RBAC（基于角色的访问控制）是成熟的权限模型
- 动态路由支持运行时添加新功能模块
- 菜单权限和操作权限分离，细粒度控制
- 权限配置存储在数据库，支持热更新

**关键实体**：
- Role（角色）：定义角色和权限的集合
- Permission（权限）：具体的操作权限
- Route（路由）：前端路由和权限的映射
- UserRole：用户和角色的多对多关系
- RolePermission：角色和权限的多对多关系

### 7. 套餐管理 (M07)

**决策**：SaaS 套餐模式，支持多种计费方式

**理由**：
- 套餐是 SaaS 平台的标准商业模式
- 支持按月/按年计费，灵活定价
- 套餐与功能模块关联，不同套餐不同功能
- 支持套餐升级和降级

**关键功能**：
- 套餐定义：名称、描述、价格、计费周期
- 功能配置：每个套餐包含的功能模块列表
- 定价策略：基础价格、附加功能价格
- 订阅管理：租户订阅套餐的历史记录

### 8. 活动管理 (M08)

**决策**：营销活动管理系统，与套餐多对多关联

**理由**：
- 活动作为营销手段，可以叠加在套餐上
- 多对多关系支持灵活的营销策略
- 支持折扣、赠送时长、试用等多种活动类型
- 活动有明确的开始和结束时间

**关键功能**：
- 活动创建：定义活动类型、折扣规则、适用范围
- 套餐关联：选择活动适用的套餐
- 活动状态：草稿、进行中、已结束
- 活动效果：统计活动带来的新增订阅

### 9. 租户管理 (M09)

**决策**：完整的租户生命周期管理

**理由**：
- 租户是 SaaS 平台的核心概念
- 需要管理租户从注册到注销的全生命周期
- 资源配额控制防止资源滥用
- 租户状态监控保证平台健康

**关键功能**：
- 租户注册：自助注册或管理员创建
- 租户审核：审核新租户的资质
- 状态管理：正常、欠费、冻结、注销
- 资源配额：存储空间、API 调用次数、用户数限制
- 租户隔离：MyBatis-Plus 租户插件

### 10. 错误文档 (M10)

**决策**：基于 Elasticsearch 的错误日志检索系统

**理由**：
- 微服务环境日志分散，需要集中存储和检索
- Elasticsearch 支持全文搜索和聚合分析
- 错误分类和解决方案库提高问题解决效率
- 支持按租户、服务、时间范围查询

**关键功能**：
- 日志采集：Logback + Logstash 采集日志
- 日志存储：Elasticsearch 集群存储
- 错误分类：自动分类错误类型（数据库、网络、业务等）
- 解决方案：常见错误的解决方案库
- 统计分析：错误趋势、高频错误统计

### 11. 分布式追踪 (M11)

**决策**：集成 SkyWalking 作为 APM 平台

**理由**：
- SkyWalking 是国产开源 APM，社区活跃，文档完善
- 支持多种编程语言和框架
- 提供完整的链路追踪、性能监控、依赖分析
- 可视化界面友好，易于使用

**关键功能**：
- Agent 接入：为每个微服务配置 SkyWalking Agent
- 链路追踪：追踪跨服务的完整调用链
- 性能分析：响应时间、吞吐量、错误率
- 告警配置：响应时间过长、错误率过高等告警
- 仪表盘：自定义监控面板

### 12. 代码生成 (M12)

**决策**：基于数据库表结构生成完整的前后端 CRUD 代码

**理由**：
- 减少重复的 CRUD 开发工作
- 生成的代码包含完整的业务逻辑
- 支持自定义代码模板
- 支持增量生成，不覆盖已有代码

**关键功能**：
- 数据库连接：连接 MySQL 数据库，读取表结构
- 代码模板：后端 Controller/Service/Mapper，前端页面
- 业务逻辑：生成包含验证规则、权限控制、审计日志的完整代码
- 配置验证：验证规则、权限配置
- 代码预览：生成前可预览代码
- 一键下载：打包生成的代码供下载

## 技术栈总结

### 后端技术栈

| 模块 | 框架 | 特殊依赖 |
|------|------|----------|
| admin-framework | Spring Boot 3.0 | Spring Security, JWT |
| parameter-service | Spring Boot 3.0 | MyBatis-Plus |
| gateway-service | Spring Cloud Gateway | Spring Cloud Gateway, Actuator |
| third-party-service | Spring Boot 3.0 | OkHttp, Jasypt（加密） |
| api-market-service | Spring Boot 3.0 | MyBatis-Plus |
| permission-service | Spring Boot 3.0 | Spring Security |
| billing-service | Spring Boot 3.0 | MyBatis-Plus |
| tenant-service | Spring Boot 3.0 | MyBatis-Plus |
| error-doc-service | Spring Boot 3.0 | Elasticsearch, Logstash |
| tracing-service | Spring Boot 3.0 | SkyWalking Agent |
| codegen-service | Spring Boot 3.0 | FreeMarker, MyBatis-Plus Generator |

### 前端技术栈

- 框架：Vue 3.0 组合式 API
- UI 库：Ant Design 6.1.4
- 状态管理：Pinia
- 路由：Vue Router 4.x
- HTTP 客户端：Axios
- 图表：ECharts（用于统计和监控）
- 代码编辑器：Monaco Editor（用于代码生成预览）

## 数据库设计策略

### 多租户隔离

- 使用 MyBatis-Plus 租户插件
- 所有表包含 `tenant_id` 字段
- 查询自动添加租户过滤条件
- Redis 缓存键带租户前缀

### 逻辑删除

- 所有表包含 `deleted` 字段
- MyBatis-Plus 逻辑删除插件
- 查询自动过滤已删除数据

### 乐观锁

- 高频更新的表使用 `@Version` 注解
- 并发冲突时提示用户刷新重试

## 实施风险与缓解

| 风险 | 影响 | 缓解措施 |
|------|------|----------|
| 微服务数量过多导致运维复杂 | 高 | 使用容器化部署，统一配置管理（Nacos） |
| 分布式事务处理困难 | 中 | 使用 Saga 模式或最终一致性，避免强一致性要求 |
| API 开放平台计费准确性 | 高 | 调用日志持久化，定期对账，异常监控告警 |
| 代码生成质量保证 | 中 | 代码模板经过充分测试，生成代码包含单元测试 |
| 链路追踪性能损耗 | 低 | 采样率配置，异步上报追踪数据 |

## 下一步

- [x] Phase 0: 完成技术研究
- [ ] Phase 1: 生成数据模型 (data-model.md)
- [ ] Phase 1: 生成 API 契约 (contracts/)
- [ ] Phase 1: 生成快速入门指南 (quickstart.md)
- [ ] Phase 1: 更新 Agent 上下文
- [ ] Phase 2: 生成任务列表 (tasks.md) - 由 /speckit.tasks 命令完成
