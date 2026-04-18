# Implementation Plan: 进销存模块

**Branch**: `005-erp-inventory` | **Date**: 2026-01-10 | **Spec**: [spec.md](./spec.md)
**Input**: Feature specification from `/specs/005-erp-inventory/spec.md`

## Summary

实现一个完整的企业级进销存（ERP）管理系统，包含采购管理、销售管理、仓库管理三大核心功能模块。系统采用多租户 SaaS 架构，支持多仓库、多批次、多价格体系的管理，并提供采购结算、销售结算、报表统计等扩展功能。

技术方案采用 Spring Boot 3.0 微服务架构后端，Vue 3.0 + Ant Design 前端，通过 Spring Cloud Gateway 统一网关进行 API 路由和安全控制。使用 MyBatis-Plus 实现多租户数据隔离，Redis 缓存提升性能，MySQL 8.0+ 持久化业务数据。

## Technical Context

**Language/Version**: Java 17+, JavaScript ES6+
**Primary Dependencies**: Spring Boot 3.0, Spring Security, MyBatis-Plus 3.5.5, Vue 3.0, Ant Design 6.1.4, Spring Cloud Gateway
**Storage**:
- MySQL 8.0+ (业务数据、主数据、流水数据)
- Redis 7.0+ (缓存、会话、分布式锁、限流)

**Testing**: JUnit 5, Mockito, Jest (可选)
**Target Platform**: Linux server (后端), 现代浏览器 (前端)
**Project Type**: web (前后端分离的多租户 SaaS)
**Performance Goals**:
- 查询操作响应 < 2秒
- 单据保存 < 3秒
- 报表生成 < 5秒
- 库存实时更新 < 1秒
- 支持 50-100 并发用户
- 库存查询支持 10万+ 记录

**Constraints**:
- 多租户数据隔离（必须）
- 库存并发安全（乐观锁/悲观锁）
- 99.5% 系统可用性
- 移动端响应式支持

**Scale/Scope**:
- 支持 10000 个商品
- 支持 1000 个供应商
- 支持 1000 个客户
- 支持 100 个并发用户
- 单表数据量 < 100万条

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

**宪章**：多租户 SaaS 系统 (v1.2.2)

### 必需的合规性检查

- [x] **简洁与用户体验**：
  - 用户故事定义了清晰的业务流程（采购、销售、仓库）
  - 界面使用 Ant Design 组件保证一致性
  - 性能目标确保用户体验（<2秒查询，<3秒保存）
  - 移动端响应式支持（FR-075）

- [x] **多租户隔离**：
  - FR-068 明确要求多租户数据隔离
  - 所有实体需要包含 tenant_id 字段
  - API 请求需要验证租户上下文
  - Redis 缓存键需要租户前缀
  - US-08 场景5验证租户隔离

- [x] **基于权限的访问**：
  - US-08 定义了角色权限管理（采购员、销售员、仓库员、财务、管理员）
  - FR-066, FR-067 定义了功能权限和数据权限
  - 所有 API 端点需要权限保护

- [x] **API 优先集成**：
  - 所有前端通信通过网关
  - contracts/ 目录将定义所有 API 契约（OpenAPI 3.0）
  - 集成假设提到与 003-user-auth 和 002-permission-module 集成

- [x] **组件可复用性**：
  - 前端使用 Ant Design 6.1.4 组件
  - 技术假设明确使用 Vue 3.0 + Ant Design

- [x] **数据一致性**：
  - FR-069 要求库存并发安全
  - FR-070 要求审计日志
  - FR-071 要求数据备份
  - Edge Cases 考虑了并发、退货、撤销等场景

- [x] **可观测性**：
  - FR-070 定义了审计日志（操作人、时间、内容、IP）
  - 需要实现租户感知的日志记录

- [x] **代码风格一致性**：
  - 技术假设明确使用 Vue 3.0 + JavaScript
  - 需要配置 ESLint + Prettier + lint-staged + husky

### 技术栈验证

- [x] 前端：Vue 3.0 + JavaScript + Ant Design 6.1.4
- [x] Node.js：Node.js 18+ LTS（宪章要求）
- [x] 前端工具：ESLint + Prettier + lint-staged + husky
- [x] 后端：Spring Boot 3.0 + Java 17+
- [x] ORM：MyBatis-Plus
- [x] 数据库：MySQL 8.0+
- [x] 缓存：Redis 7.0+
- [x] 网关：Spring Cloud Gateway

### 复杂度理由

所有合规性检查均通过，无需复杂度理由。

## Project Structure

### Documentation (this feature)

```text
specs/005-erp-inventory/
├── plan.md              # This file
├── research.md          # Phase 0 output
├── data-model.md        # Phase 1 output
├── quickstart.md        # Phase 1 output
├── contracts/           # Phase 1 output (API definitions)
│   ├── purchase-api.yaml
│   ├── sales-api.yaml
│   ├── warehouse-api.yaml
│   ├── settlement-api.yaml
│   ├── report-api.yaml
│   └── product-api.yaml
├── tasks.md             # Phase 2 output (created by /speckit.tasks)
└── checklists/
    └── requirements.md
```

### Source Code (repository root)

```text
gateway/                          # Spring Cloud Gateway 网关
├── src/main/java/com/example/gateway/
│   ├── filter/
│   │   ├── TenantFilter.java     # 租户识别过滤器
│   │   └── LoggingFilter.java    # 日志过滤器
│   └── config/
│       └── RouteConfig.java      # 路由配置
└── pom.xml

services/erp-service/             # ERP 进销存服务
├── src/main/java/com/example/erp/
│   ├── controller/
│   │   ├── purchase/             # 采购管理
│   │   │   ├── SupplierController.java
│   │   │   ├── PurchaseOrderController.java
│   │   │   ├── PurchaseReceiptController.java
│   │   │   └── PurchaseReturnController.java
│   │   ├── sales/                # 销售管理
│   │   │   ├── CustomerController.java
│   │   │   ├── SalesOrderController.java
│   │   │   ├── SalesDeliveryController.java
│   │   │   └── SalesReturnController.java
│   │   ├── warehouse/            # 仓库管理
│   │   │   ├── WarehouseController.java
│   │   │   ├── InventoryController.java
│   │   │   ├── StockTransferController.java
│   │   │   └── StockCheckController.java
│   │   ├── settlement/           # 结算管理
│   │   │   ├── PaymentController.java
│   │   │   └── ReceiptController.java
│   │   ├── product/              # 商品管理
│   │   │   ├── ProductController.java
│   │   │   └── CategoryController.java
│   │   └── report/               # 报表管理
│   │       └── ReportController.java
│   ├── service/
│   │   ├── purchase/
│   │   ├── sales/
│   │   ├── warehouse/
│   │   ├── settlement/
│   │   └── product/
│   ├── entity/
│   │   ├── purchase/
│   │   ├── sales/
│   │   ├── warehouse/
│   │   ├── settlement/
│   │   └── product/
│   ├── mapper/
│   │   ├── purchase/
│   │   ├── sales/
│   │   ├── warehouse/
│   │   ├── settlement/
│   │   └── product/
│   └── config/
│       ├── MybatisPlusConfig.java      # MyBatis-Plus 配置
│       ├── RedisConfig.java            # Redis 配置
│       └── TenantConfig.java           # 租户配置
├── src/main/resources/
│   ├── mapper/                         # MyBatis XML
│   └── application.yml
└── pom.xml

frontend/                         # Vue 3.0 + Ant Design 前端
├── src/
│   ├── views/
│   │   ├── purchase/             # 采购管理页面
│   │   │   ├── SupplierList.vue
│   │   │   ├── PurchaseOrderList.vue
│   │   │   └── PurchaseReceipt.vue
│   │   ├── sales/                # 销售管理页面
│   │   │   ├── CustomerList.vue
│   │   │   ├── SalesOrderList.vue
│   │   │   └── SalesDelivery.vue
│   │   ├── warehouse/            # 仓库管理页面
│   │   │   ├── InventoryList.vue
│   │   │   ├── StockTransfer.vue
│   │   │   └── StockCheck.vue
│   │   ├── settlement/           # 结算管理页面
│   │   │   ├── PaymentList.vue
│   │   │   └── ReceiptList.vue
│   │   ├── product/              # 商品管理页面
│   │   │   ├── ProductList.vue
│   │   │   └── CategoryManage.vue
│   │   └── report/               # 报表页面
│   │       ├── PurchaseReport.vue
│   │       ├── SalesReport.vue
│   │       └── InventoryReport.vue
│   ├── components/               # 可复用组件
│   │   ├── ProductSelector.vue   # 商品选择器
│   │   ├── SupplierSelector.vue  # 供应商选择器
│   │   ├── CustomerSelector.vue  # 客户选择器
│   │   └── BatchSelector.vue     # 批次选择器
│   ├── api/                      # API 客户端
│   │   ├── purchase.js
│   │   ├── sales.js
│   │   ├── warehouse.js
│   │   ├── settlement.js
│   │   └── product.js
│   ├── router/
│   ├── store/
│   └── utils/
├── public/
├── .eslintrc.js                  # ESLint 配置
├── .prettierrc                   # Prettier 配置
├── package.json
└── vite.config.js
```

**架构决策**：多租户 SaaS 架构，采用独立的 ERP 微服务处理所有进销存业务，通过网关统一路由和鉴权。

## Complexity Tracking

> **仅当宪章检查有必须证明的违规时填写**

无违规，本表格不适用。

## Phase 0: Research Topics

### 待研究的技术问题

1. **库存并发控制策略**
   - 问题：多个用户同时操作同一商品库存时如何保证一致性？
   - 目标：选择合适的并发控制机制（乐观锁 vs 悲观锁 vs 分布式锁）
   - 影响：架构设计、性能、数据一致性

2. **多租户数据隔离实现**
   - 问题：如何在 MyBatis-Plus 中实现透明的多租户数据隔离？
   - 目标：确定租户拦截器实现方案
   - 影响：数据层设计、查询性能

3. **批次管理 FIFO 实现**
   - 问题：如何高效实现批次先进先出推荐？
   - 目标：设计批次管理和推荐算法
   - 影响：出库流程、库存查询

4. **报表性能优化**
   - 问题：如何支持百万级数据的快速统计分析？
   - 目标：选择合适的报表技术方案
   - 影响：报表性能、用户体验

5. **分布式事务处理**
   - 问题：采购入库涉及库存更新和应付生成，如何保证事务性？
   - 目标：确定分布式事务方案（Saga vs 本地消息表）
   - 影响：数据一致性、系统复杂度

### 待集成的依赖模块

1. **003-user-auth 集成**
   - 研究点：用户认证 API、JWT Token 验证
   - 目标：定义集成契约

2. **002-permission-module 集成**
   - 研究点：权限验证 API、角色管理
   - 目标：定义权限检查机制

### 最佳实践研究

1. **ERP 单据编号生成策略**
   - 研究：分布式环境下的单据编号唯一性保证
   - 参考：数据库序列、Redis INCR、雪花算法

2. **审批流程引擎选择**
   - 研究：轻量级审批流程实现
   - 参考：Flowable vs 自研状态机

## Phase 1: Design Artifacts

**待生成文件**（执行 Phase 0 后生成）：

1. `research.md` - 技术研究决策文档
2. `data-model.md` - 完整数据模型设计（40+ 实体）
3. `contracts/` - API 契约定义（OpenAPI 3.0 格式）
4. `quickstart.md` - 开发者快速开始指南

## Next Steps

1. **运行 `/speckit.plan`** - 完成此文件的创建（当前步骤）
2. **查看 `research.md`** - 阅读技术研究结果
3. **查看 `data-model.md`** - 了解数据库设计
4. **查看 `contracts/`** - 了解 API 契约
5. **运行 `/speckit.tasks`** - 生成任务分解（当准备好开始实施时）
