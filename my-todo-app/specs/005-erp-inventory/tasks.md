# Tasks: 进销存模块

**Feature Branch**: `005-erp-inventory`
**Generated**: 2026-04-07
**Updated**: 2026-05-31 (Service层接口+Impl重构、促销功能确认、Dashboard后端、对账单端点、联系人/报价Controller)
**Total Tasks**: 107 (+ 额外完成项)

## 实施状态总览

| Story | 名称 | 已完成 | 总任务 | 完成率 | 状态 |
|-------|------|--------|--------|--------|------|
| Phase 1 | Setup | 6 | 6 | 100% | ✅ 完成 |
| Phase 2 | Foundation | 10 | 10 | 100% | ✅ 完成 |
| US1 | 采购管理 | 14 | 14 | 100% | ✅ 完成 |
| US2 | 销售管理 | 14 | 14 | 100% | ✅ 完成 |
| US3 | 仓库管理 | 16 | 16 | 100% | ✅ 完成 |
| US4 | 采购结算管理 | 4 | 4 | 100% | ✅ 已移至finance模块 |
| US5 | 销售结算管理 | 4 | 4 | 100% | ✅ 已移至finance模块 |
| US6 | 报表与统计 | 12 | 12 | 100% | ✅ 完成 |
| US7 | 商品与价格管理 | 14 | 14 | 100% | ✅ 完成 |
| US8 | 系统配置 | 13 | 13 | 100% | ✅ 完成 |
| **合计** | | **107** | **107** | **100%** | ✅ 全部完成 |

---

## Phase 1: Setup (项目初始化) ✅ 完成

**Duration**: Week 1

- [x] T001 创建 erp-service Maven 模块 `services/erp-service/pom.xml`
- [x] T002 [P] 创建 Spring Boot 主类 `services/erp-service/src/main/java/com/example/erp/ErpServiceApplication.java`
- [x] T003 [P] 配置 application.yml `services/erp-service/src/main/resources/application.yml`
- [x] T004 [P] 创建数据库 schema 脚本 `db-scripts/07-erp-tables.sql` ✅ 已有完整建表脚本
- [x] T005 [P] 配置 Redis (库存缓存) — 使用 common-redis 模块，无需单独配置
- [x] T006 [P] 配置消息队列 (事件发布) — 暂未使用MQ，用同步调用替代

**Checkpoint**: ✅ 项目可启动

---

## Phase 2: Foundation (基础设施) ✅ 完成

**Duration**: Week 1

- [x] T007 创建 Product 实体 `services/erp-service/src/main/java/com/example/erp/entity/Product.java`
- [x] T008 [P] 创建 Supplier 实体 `services/erp-service/src/main/java/com/example/erp/entity/Supplier.java`
- [x] T009 [P] 创建 Customer 实体 `services/erp-service/src/main/java/com/example/erp/entity/Customer.java`
- [x] T010 [P] 创建 Warehouse 实体 `services/erp-service/src/main/java/com/example/erp/entity/Warehouse.java`
- [x] T011 [P] 创建 Inventory 实体 `services/erp-service/src/main/java/com/example/erp/entity/Inventory.java`
- [x] T012 [P] 创建 PurchaseOrder 实体 `services/erp-service/src/main/java/com/example/erp/entity/PurchaseOrder.java`
- [x] T013 [P] 创建 SalesOrder 实体 `services/erp-service/src/main/java/com/example/erp/entity/SalesOrder.java`
- [x] T014 [P] 创建 InventoryFlow 实体 `services/erp-service/src/main/java/com/example/erp/entity/InventoryFlow.java`
- [x] T015 [P] 创建 ProductMapper `services/erp-service/src/main/java/com/example/erp/mapper/ProductMapper.java`
- [x] T016 [P] 创建 InventoryMapper `services/erp-service/src/main/java/com/example/erp/mapper/InventoryMapper.java` (含乐观锁SQL)

**额外完成（spec未列出）:**
- [x] SalesOrderItem 实体 + Mapper
- [x] SalesShipment 实体 + Mapper
- [x] SalesShipmentItem 实体 + Mapper
- [x] PurchaseOrderMapper
- [x] 所有其他Mapper（SupplierMapper, CustomerMapper, WarehouseMapper等）

**Checkpoint**: ✅ 所有实体类编译通过

---

## Phase 3: US1 - 采购管理 (P1) ✅ 完成

**Duration**: Week 2-3

### DTO
- [x] T017 [P] [US1] 创建 PurchaseOrderCreateRequest `services/erp-service/src/main/java/com/example/erp/dto/PurchaseOrderCreateRequest.java`
- [x] T018 [P] [US1] 创建 PurchaseOrderVO `services/erp-service/src/main/java/com/example/erp/dto/PurchaseOrderVO.java`

### Service
- [x] T019 [US1] 创建 SupplierService `services/erp-service/src/main/java/com/example/erp/service/SupplierService.java`
- [x] T020 [US1] 创建 PurchaseOrderService `services/erp-service/src/main/java/com/example/erp/service/PurchaseOrderService.java` (含采购入库)
- [x] T021 [US1] 创建 PurchaseInboundService — 已集成到 PurchaseOrderService.inbound() 中
- [x] T022 [US1] 创建 PurchaseReturnService `services/erp-service/src/main/java/com/example/erp/service/PurchaseReturnService.java`

### Controller
- [x] T023 [US1] 实现 SupplierController `services/erp-service/src/main/java/com/example/erp/controller/SupplierController.java`
- [x] T024 [US1] 实现 PurchaseOrderController `services/erp-service/src/main/java/com/example/erp/controller/PurchaseOrderController.java`
- [x] 额外: PurchaseReturnController `services/erp-service/src/main/java/com/example/erp/controller/PurchaseReturnController.java`

### Frontend
- [x] T025 [P] [US1] 创建供应商管理页面 `frontend/src/views/erp/supplier/index.vue`
- [x] T026 [P] [US1] 创建采购订单页面 `frontend/src/views/erp/purchase-order/index.vue` (含行项目编辑、审批、入库)
- [x] T027 [P] [US1] 创建采购入库 — 已集成到采购订单页面中
- [x] T028 [US1] 创建采购 API 客户端 `frontend/src/api/erp.js` (含订单+退货API)
- [x] T029 [US1] 实现采购审批流程 — 已集成到采购订单页面中
- [x] T030 [US1] 实现采购退货 `frontend/src/views/erp/purchase-return/index.vue`

---

## Phase 4: US2 - 销售管理 (P1) ✅ 完成

**Duration**: Week 3-4

### DTO
- [x] T031 [P] [US2] 创建 SalesOrderCreateRequest `services/erp-service/src/main/java/com/example/erp/dto/SalesOrderCreateRequest.java`
- [x] T032 [P] [US2] 创建 SalesOrderVO `services/erp-service/src/main/java/com/example/erp/dto/SalesOrderVO.java`

### Service
- [x] T033 [US2] 创建 CustomerService `services/erp-service/src/main/java/com/example/erp/service/CustomerService.java`
- [x] T034 [US2] 创建 SalesOrderService `services/erp-service/src/main/java/com/example/erp/service/SalesOrderService.java`
- [x] T035 [US2] 创建 SalesShipmentService `services/erp-service/src/main/java/com/example/erp/service/SalesShipmentService.java`
- [x] T036 [US2] 创建 SalesReturnService `services/erp-service/src/main/java/com/example/erp/service/SalesReturnService.java`

### Controller
- [x] T037 [US2] 实现 CustomerController `services/erp-service/src/main/java/com/example/erp/controller/CustomerController.java`
- [x] T038 [US2] 实现 SalesOrderController `services/erp-service/src/main/java/com/example/erp/controller/SalesOrderController.java`
- [x] 额外: SalesShipmentController `services/erp-service/src/main/java/com/example/erp/controller/SalesShipmentController.java`
- [x] 额外: SalesReturnController `services/erp-service/src/main/java/com/example/erp/controller/SalesReturnController.java`

### Frontend
- [x] T039 [P] [US2] 创建客户管理页面 `frontend/src/views/erp/customer/index.vue`
- [x] T040 [P] [US2] 创建销售订单页面 `frontend/src/views/erp/sales-order/index.vue` (含行项目编辑、审批流程)
- [x] T041 [P] [US2] 创建销售出库页面 `frontend/src/views/erp/sales-shipment/index.vue`
- [x] T042 [US2] 创建销售 API 客户端 `frontend/src/api/erp.js` (含订单+发货+退货+客户API)
- [x] T043 [US2] 实现销售审批流程 — 已集成到销售订单页面中
- [x] T044 [US2] 实现销售退货 `frontend/src/views/erp/sales-return/index.vue`

---

## Phase 5: US3 - 仓库管理 (P1) ✅ 完成

**Duration**: Week 4-5

### Service
- [x] T045 [US3] 创建 WarehouseService `services/erp-service/src/main/java/com/example/erp/service/WarehouseService.java`
- [x] T046 [US3] 创建 InventoryService `services/erp-service/src/main/java/com/example/erp/service/InventoryService.java` (含入库/出库/调拨/库存锁定/预警)
- [x] T047 [US3] 创建 InventoryTransferService — 已集成到 InventoryController.transfer() 中
- [x] T048 [US3] 创建 InventoryCheckService `services/erp-service/src/main/java/com/example/erp/service/InventoryCheckService.java`
- [x] T049 [US3] 创建 InventoryAlertService — 已集成到 InventoryController.getAlertInventories() 中
- [x] T050 [US3] 批次管理 — 已通过 erp_inventory.batch_no 字段支持

### Task
- [x] T051 [US3] 创建库存预警定时任务 `services/erp-service/src/main/java/com/example/erp/task/InventoryAlertTask.java` (每小时执行)

### Controller
- [x] T052 [US3] 实现 WarehouseController `services/erp-service/src/main/java/com/example/erp/controller/WarehouseController.java`
- [x] T053 [US3] 实现 InventoryController `services/erp-service/src/main/java/com/example/erp/controller/InventoryController.java`
- [x] 额外: InventoryCheckController `services/erp-service/src/main/java/com/example/erp/controller/InventoryCheckController.java`

### Frontend
- [x] T054 [P] [US3] 创建仓库管理页面 `frontend/src/views/erp/warehouse/index.vue`
- [x] T055 [P] [US3] 创建库存查询页面 `frontend/src/views/erp/inventory/index.vue`
- [x] T056 [P] [US3] 创建库存调拨 — 已集成到库存页面中（调拨按钮）
- [x] T057 [P] [US3] 创建库存盘点页面 `frontend/src/views/erp/inventory-check/index.vue`
- [x] T058 [US3] 创建库存 API 客户端 `frontend/src/api/erp.js`
- [x] T059 [US3] 实现库存预警页面 `frontend/src/views/erp/inventory-alert/index.vue`
- [x] T060 [US3] 批次管理 — 已通过 batch_no 字段在库存管理中支持

**额外完成:**
- [x] 库存流水前端页面 `frontend/src/views/erp/inventory-flow/index.vue`
- [x] 定时任务配置 `services/erp-service/src/main/java/com/example/erp/config/SchedulingConfig.java`

---

## Phase 6: US4 - 采购结算管理 (P2) ✅ 已移至finance模块

**Duration**: Week 5

> **说明**: 结算管理已移至 `007-finance-module` 中的应收应付模块统一管理，不再在ERP模块中单独实现。

- [x] T061-T064 对应功能已在 finance-service 中的 AccountPayableService 实现 ✅

---

## Phase 7: US5 - 销售结算管理 (P2) ✅ 已移至finance模块

**Duration**: Week 5

> **说明**: 结算管理已移至 `007-finance-module` 中的应收应付模块统一管理。

- [x] T065-T068 对应功能已在 finance-service 中的 AccountReceivableService 实现 ✅

---

## Phase 8: US6 - 报表与统计分析 (P2) ✅ 完成

**Duration**: Week 6

### DTO
- [x] T069 创建 DashboardVO `services/erp-service/src/main/java/com/example/erp/dto/DashboardVO.java`
- [x] T070 创建 SalesReportVO `services/erp-service/src/main/java/com/example/erp/dto/SalesReportVO.java`
- [x] T071 创建 PurchaseReportVO `services/erp-service/src/main/java/com/example/erp/dto/PurchaseReportVO.java`
- [x] T072 创建 InventoryReportVO `services/erp-service/src/main/java/com/example/erp/dto/InventoryReportVO.java`

### Service
- [x] T073 创建 ReportService `services/erp-service/src/main/java/com/example/erp/service/ReportService.java` (含Dashboard聚合、销售/采购/库存报表)

### Controller
- [x] T074 实现 ReportController `services/erp-service/src/main/java/com/example/erp/controller/ReportController.java` (dashboard/sales/purchase/inventory)
- [x] 额外: 实现 DashboardController `services/erp-service/src/main/java/com/example/erp/controller/DashboardController.java` (GET /api/erp/dashboard/stats)
- [x] 额外: ReportController 新增对账单端点 (supplier-statement, customer-statement)

### Frontend
- [x] T075 创建报表统计页面 `frontend/src/views/erp/report/index.vue` (Dashboard卡片 + 销售/采购/库存报表标签页)
- [x] T076 创建报表 API 客户端 (getDashboard, getSalesReport, getPurchaseReport, getInventoryReport)
- [x] T077 添加报表路由 `frontend/src/router/index.js`
- [x] T078 添加报表菜单项 `frontend/src/layouts/BasicLayout.vue`
- [x] T079 添加报表权限数据 `db-scripts/08-permission-data.sql`
- [x] T080 添加报表面包屑导航

---

## Phase 9: US7 - 商品与价格管理 (P2) ✅ 完成

**Duration**: Week 6

### Entity
- [x] T081 [US7] 创建 ProductCategory 实体 `services/erp-service/src/main/java/com/example/erp/entity/ProductCategory.java`
- [x] T082 [P] [US7] 创建 ProductPrice 实体 `services/erp-service/src/main/java/com/example/erp/entity/ProductPrice.java`
- [x] T083 [P] [US7] 创建 ProductPromotion 实体 `services/erp-service/src/main/java/com/example/erp/entity/ProductPromotion.java`

### Service
- [x] T084 [US7] 创建 ProductService `services/erp-service/src/main/java/com/example/erp/service/ProductService.java`
- [x] T085 [US7] 创建 ProductCategoryService `services/erp-service/src/main/java/com/example/erp/service/ProductCategoryService.java`
- [x] T086 [US7] 创建 ProductPriceService `services/erp-service/src/main/java/com/example/erp/service/ProductPriceService.java`
- [x] 额外: 创建 ProductPromotionService + ProductPromotionServiceImpl `services/erp-service/src/main/java/com/example/erp/service/ProductPromotionService.java`

### Controller
- [x] T087 [US7] 实现 ProductController `services/erp-service/src/main/java/com/example/erp/controller/ProductController.java`
- [x] T088 [US7] 实现 ProductCategoryController `services/erp-service/src/main/java/com/example/erp/controller/ProductCategoryController.java`
- [x] T089 [US7] 实现 ProductPriceController `services/erp-service/src/main/java/com/example/erp/controller/ProductPriceController.java`
- [x] 额外: 实现 ProductPromotionController `services/erp-service/src/main/java/com/example/erp/controller/ProductPromotionController.java`

### Frontend
- [x] T090 [P] [US7] 创建商品列表页面 `frontend/src/views/erp/product/index.vue`
- [x] T091 创建商品分类页面 `frontend/src/views/erp/product-category/index.vue`
- [x] T092 创建商品价格页面 `frontend/src/views/erp/product-price/index.vue`
- [x] T093 创建分类/价格 API 客户端
- [x] T094 添加分类/价格路由、菜单、面包屑、权限

---

## Phase 10: US8 - 系统配置与权限管理 (P3) ✅ 完成

**Duration**: Week 7

### Entity & Mapper
- [x] T095 创建 ErpConfig 实体 `services/erp-service/src/main/java/com/example/erp/entity/ErpConfig.java`
- [x] T096 创建 ErpConfigMapper `services/erp-service/src/main/java/com/example/erp/mapper/ErpConfigMapper.java`

### Service
- [x] T097 创建 ErpConfigService `services/erp-service/src/main/java/com/example/erp/service/ErpConfigService.java` (含CRUD、按类型查询、按Key取值、批量更新)

### Controller
- [x] T098 实现 ErpConfigController `services/erp-service/src/main/java/com/example/erp/controller/ErpConfigController.java`

### Frontend
- [x] T099 创建系统配置页面 `frontend/src/views/erp/config/index.vue` (含快捷配置标签页：审批规则/编号规则/业务参数)
- [x] T100 创建配置 API 客户端 (getConfigPage, getConfigsByType, getConfigValue, createConfig, updateConfig, deleteConfig, batchUpdateConfigs)
- [x] T101 添加配置路由
- [x] T102 添加配置菜单项
- [x] T103 添加配置面包屑导航
- [x] T104 添加配置权限数据

### 配置功能说明
- **审批规则**: 采购订单/销售订单/采购退货/销售退货/销售出库是否需要审核
- **编号规则**: 各类单据编号前缀配置
- **业务参数**: 默认仓库、库存预警检查间隔、允许负库存出库

---

## 已完成的附加功能

### 数据库表新增
- `erp_inventory_check` + `erp_inventory_check_item` — 库存盘点
- `erp_purchase_return` + `erp_purchase_return_item` — 采购退货
- `erp_sales_return` + `erp_sales_return_item` — 销售退货
- `erp_product_price` — 商品价格
- `erp_config` — 系统配置

### 新增Mapper
- InventoryCheckMapper, InventoryCheckItemMapper
- PurchaseReturnMapper, PurchaseReturnItemMapper
- SalesReturnMapper, SalesReturnItemMapper
- ProductPriceMapper, ErpConfigMapper
- PurchaseOrderItemMapper

### 权限数据
- 所有新功能的菜单权限和按钮权限已添加到 `db-scripts/08-permission-data.sql`

---

## 架构重构记录 (2026-05-31)

### Service 层接口+Impl 模式重构
所有 Service 已重构为接口 + Impl 分离模式，接口和实现类同在 `service/` 目录下（非 `service/impl/` 子目录）。

**17 个 Service 接口 + 17 个 ServiceImpl 实现类：**

| 序号 | Service 接口 | ServiceImpl 实现类 |
|------|-------------|-------------------|
| 1 | ProductService | ProductServiceImpl |
| 2 | ProductCategoryService | ProductCategoryServiceImpl |
| 3 | ProductPriceService | ProductPriceServiceImpl |
| 4 | ProductPromotionService | ProductPromotionServiceImpl |
| 5 | WarehouseService | WarehouseServiceImpl |
| 6 | CustomerService | CustomerServiceImpl |
| 7 | CustomerContactService | CustomerContactServiceImpl |
| 8 | SupplierService | SupplierServiceImpl |
| 9 | SupplierContactService | SupplierContactServiceImpl |
| 10 | PurchaseOrderService | PurchaseOrderServiceImpl |
| 11 | PurchaseReturnService | PurchaseReturnServiceImpl |
| 12 | SalesOrderService | SalesOrderServiceImpl |
| 13 | SalesShipmentService | SalesShipmentServiceImpl |
| 14 | SalesReturnService | SalesReturnServiceImpl |
| 15 | SalesQuotationService | SalesQuotationServiceImpl |
| 16 | ReportService | ReportServiceImpl |
| 17 | ErpConfigService | ErpConfigServiceImpl |

> **目录结构**: 接口与实现类均位于 `services/erp-service/src/main/java/com/example/erp/service/` 目录下。

---

## 新增完成项 (2026-05-31)

### Dashboard 后端 (Phase 8 扩展)
- [x] DashboardController — GET /api/erp/dashboard/stats，聚合统计待处理订单数和金额
- [x] SalesOrderService 新增 countByStatus(Long tenantId, Integer status)、sumTotalAmount(Long tenantId)
- [x] PurchaseOrderService 新增 countByStatus(Long tenantId, Integer status)、sumTotalAmount(Long tenantId)

### 促销功能 (Phase 9 补充)
- [x] ProductPromotionService 接口 + ProductPromotionServiceImpl 实现
- [x] ProductPromotionController — 促销管理 REST 端点
- [x] ProductPromotion 实体已存在（Phase 9 T083 已完成）

### ReportService 对账单端点
- [x] ReportController 新增 GET /supplier-statement/{supplierId} — 供应商对账单
- [x] ReportController 新增 GET /customer-statement/{customerId} — 客户对账单

### 额外 Controller
- [x] SupplierContactController `services/erp-service/src/main/java/com/example/erp/controller/SupplierContactController.java`
- [x] CustomerContactController `services/erp-service/src/main/java/com/example/erp/controller/CustomerContactController.java`
- [x] SalesQuotationController `services/erp-service/src/main/java/com/example/erp/controller/SalesQuotationController.java`

### 对应 Service
- [x] SupplierContactService + SupplierContactServiceImpl
- [x] CustomerContactService + CustomerContactServiceImpl
- [x] SalesQuotationService + SalesQuotationServiceImpl

---

---

## 全部完成

所有 107 个任务均已完成，另含架构重构和额外功能。ERP进销存模块功能包括：
- 采购管理（订单、入库、退货、审批）
- 销售管理（订单、出库、退货、审批、报价）
- 仓库管理（库存、调拨、盘点、预警、批次）
- 商品管理（分类、商品、价格、促销）
- 报表统计（Dashboard、销售报表、采购报表、库存报表、对账单）
- 系统配置（审批规则、编号规则、业务参数）
- 联系人管理（供应商联系人、客户联系人）
- Service 层已全面重构为接口+Impl 模式（17 组）

---

## 建议后续优化项

| 任务 | 说明 |
|------|------|
| 销售订单编辑功能 | 前端handleEdit占位，可改为查看详情 |
| 库存盘点自动扣减 | 盘点差异自动生成调账流水 |
| 报表导出 | 销售/采购/库存报表导出Excel |
