# Tasks: 进销存模块

**Feature Branch**: `005-erp-inventory`
**Generated**: 2026-04-07
**Total Tasks**: 68

## Overview

| Story | Name | Priority | Tasks | Parallel |
|-------|------|----------|-------|----------|
| US1 | 采购管理 | P1 | 16 | 8 |
| US2 | 销售管理 | P1 | 16 | 8 |
| US3 | 仓库管理 | P1 | 18 | 9 |
| US4 | 采购结算管理 | P2 | 8 | 4 |
| US5 | 销售结算管理 | P2 | 8 | 4 |

### Dependencies

```
Phase 1 (Setup) ──► Phase 2 (Foundation)
                          │
         ┌────────────────┼────────────────┐
         │                │                │
         ▼                ▼                ▼
       US1              US2              US3
    (采购管理)        (销售管理)       (仓库管理)
         │                │                │
         └────────────────┼────────────────┘
                          │
         ┌────────────────┴────────────────┐
         │                                 │
         ▼                                 ▼
       US4                              US5
    (采购结算)                        (销售结算)
```

---

## Phase 1: Setup (项目初始化)

**Duration**: Week 1

- [ ] T001 创建 erp-service Maven 模块 `services/erp-service/pom.xml`
- [ ] T002 [P] 创建 Spring Boot 主类 `services/erp-service/src/main/java/com/example/erp/ErpServiceApplication.java`
- [ ] T003 [P] 配置 application.yml `services/erp-service/src/main/resources/application.yml`
- [ ] T004 [P] 创建数据库 schema 脚本 `services/erp-service/src/main/resources/db/migration/V1__erp_schema.sql`
- [ ] T005 [P] 配置 Redis (库存缓存) `services/erp-service/src/main/java/com/example/erp/config/RedisConfig.java`
- [ ] T006 [P] 配置消息队列 (事件发布) `services/erp-service/src/main/java/com/example/erp/config/MqConfig.java`

**Checkpoint**: 项目可启动

---

## Phase 2: Foundation (基础设施)

**Duration**: Week 1

- [ ] T007 创建 Product 实体 `services/erp-service/src/main/java/com/example/erp/entity/Product.java`
- [ ] T008 [P] 创建 Supplier 实体 `services/erp-service/src/main/java/com/example/erp/entity/Supplier.java`
- [ ] T009 [P] 创建 Customer 实体 `services/erp-service/src/main/java/com/example/erp/entity/Customer.java`
- [ ] T010 [P] 创建 Warehouse 实体 `services/erp-service/src/main/java/com/example/erp/entity/Warehouse.java`
- [ ] T011 [P] 创建 Inventory 实体 `services/erp-service/src/main/java/com/example/erp/entity/Inventory.java`
- [ ] T012 [P] 创建 PurchaseOrder 实体 `services/erp-service/src/main/java/com/example/erp/entity/PurchaseOrder.java`
- [ ] T013 [P] 创建 SalesOrder 实体 `services/erp-service/src/main/java/com/example/erp/entity/SalesOrder.java`
- [ ] T014 [P] 创建 InventoryFlow 实体 `services/erp-service/src/main/java/com/example/erp/entity/InventoryFlow.java`
- [ ] T015 [P] 创建 ProductMapper `services/erp-service/src/main/java/com/example/erp/mapper/ProductMapper.java`
- [ ] T016 [P] 创建 InventoryMapper `services/erp-service/src/main/java/com/example/erp/mapper/InventoryMapper.java`

**Checkpoint**: 所有实体类编译通过

---

## Phase 3: US1 - 采购管理 (P1)

**Duration**: Week 2-3

### DTO
- [ ] T017 [P] [US1] 创建 PurchaseOrderCreateRequest `services/erp-service/src/main/java/com/example/erp/dto/PurchaseOrderCreateRequest.java`
- [ ] T018 [P] [US1] 创建 PurchaseOrderResponse `services/erp-service/src/main/java/com/example/erp/dto/PurchaseOrderResponse.java`

### Service
- [ ] T019 [US1] 创建 SupplierService `services/erp-service/src/main/java/com/example/erp/service/SupplierService.java`
- [ ] T020 [US1] 创建 PurchaseOrderService `services/erp-service/src/main/java/com/example/erp/service/PurchaseOrderService.java`
- [ ] T021 [US1] 创建 PurchaseInboundService `services/erp-service/src/main/java/com/example/erp/service/PurchaseInboundService.java`
- [ ] T022 [US1] 创建 PurchaseReturnService `services/erp-service/src/main/java/com/example/erp/service/PurchaseReturnService.java`

### Controller
- [ ] T023 [US1] 实现 SupplierController `services/erp-service/src/main/java/com/example/erp/controller/SupplierController.java`
- [ ] T024 [US1] 实现 PurchaseOrderController `services/erp-service/src/main/java/com/example/erp/controller/PurchaseOrderController.java`

### Frontend
- [ ] T025 [P] [US1] 创建供应商管理页面 `frontend/src/views/erp/SupplierList.vue`
- [ ] T026 [P] [US1] 创建采购订单页面 `frontend/src/views/erp/PurchaseOrderList.vue`
- [ ] T027 [P] [US1] 创建采购入库页面 `frontend/src/views/erp/PurchaseInbound.vue`
- [ ] T028 [US1] 创建采购 API 客户端 `frontend/src/api/purchase.js`
- [ ] T029 [US1] 实现采购审批流程 `frontend/src/views/erp/PurchaseApproval.vue`
- [ ] T030 [US1] 实现采购退货 `frontend/src/views/erp/PurchaseReturn.vue`

**Checkpoint**: 采购流程完整可用

---

## Phase 4: US2 - 销售管理 (P1)

**Duration**: Week 3-4

### DTO
- [ ] T031 [P] [US2] 创建 SalesOrderCreateRequest `services/erp-service/src/main/java/com/example/erp/dto/SalesOrderCreateRequest.java`
- [ ] T032 [P] [US2] 创建 SalesOrderResponse `services/erp-service/src/main/java/com/example/erp/dto/SalesOrderResponse.java`

### Service
- [ ] T033 [US2] 创建 CustomerService `services/erp-service/src/main/java/com/example/erp/service/CustomerService.java`
- [ ] T034 [US2] 创建 SalesOrderService `services/erp-service/src/main/java/com/example/erp/service/SalesOrderService.java`
- [ ] T035 [US2] 创建 SalesOutboundService `services/erp-service/src/main/java/com/example/erp/service/SalesOutboundService.java`
- [ ] T036 [US2] 创建 SalesReturnService `services/erp-service/src/main/java/com/example/erp/service/SalesReturnService.java`

### Controller
- [ ] T037 [US2] 实现 CustomerController `services/erp-service/src/main/java/com/example/erp/controller/CustomerController.java`
- [ ] T038 [US2] 实现 SalesOrderController `services/erp-service/src/main/java/com/example/erp/controller/SalesOrderController.java`

### Frontend
- [ ] T039 [P] [US2] 创建客户管理页面 `frontend/src/views/erp/CustomerList.vue`
- [ ] T040 [P] [US2] 创建销售订单页面 `frontend/src/views/erp/SalesOrderList.vue`
- [ ] T041 [P] [US2] 创建销售出库页面 `frontend/src/views/erp/SalesOutbound.vue`
- [ ] T042 [US2] 创建销售 API 客户端 `frontend/src/api/sales.js`
- [ ] T043 [US2] 实现销售审批流程 `frontend/src/views/erp/SalesApproval.vue`
- [ ] T044 [US2] 实现销售退货 `frontend/src/views/erp/SalesReturn.vue`

**Checkpoint**: 销售流程完整可用

---

## Phase 5: US3 - 仓库管理 (P1)

**Duration**: Week 4-5

### Service
- [ ] T045 [US3] 创建 WarehouseService `services/erp-service/src/main/java/com/example/erp/service/WarehouseService.java`
- [ ] T046 [US3] 创建 InventoryService `services/erp-service/src/main/java/com/example/erp/service/InventoryService.java`
- [ ] T047 [US3] 创建 InventoryTransferService `services/erp-service/src/main/java/com/example/erp/service/InventoryTransferService.java`
- [ ] T048 [US3] 创建 InventoryCheckService `services/erp-service/src/main/java/com/example/erp/service/InventoryCheckService.java`
- [ ] T049 [US3] 创建 InventoryAlertService `services/erp-service/src/main/java/com/example/erp/service/InventoryAlertService.java`
- [ ] T050 [US3] 创建 BatchManageService `services/erp-service/src/main/java/com/example/erp/service/BatchManageService.java`

### Task
- [ ] T051 [US3] 创建库存预警定时任务 `services/erp-service/src/main/java/com/example/erp/task/InventoryAlertTask.java`

### Controller
- [ ] T052 [US3] 实现 WarehouseController `services/erp-service/src/main/java/com/example/erp/controller/WarehouseController.java`
- [ ] T053 [US3] 实现 InventoryController `services/erp-service/src/main/java/com/example/erp/controller/InventoryController.java`

### Frontend
- [ ] T054 [P] [US3] 创建仓库管理页面 `frontend/src/views/erp/WarehouseList.vue`
- [ ] T055 [P] [US3] 创建库存查询页面 `frontend/src/views/erp/InventoryList.vue`
- [ ] T056 [P] [US3] 创建库存调拨页面 `frontend/src/views/erp/InventoryTransfer.vue`
- [ ] T057 [P] [US3] 创建库存盘点页面 `frontend/src/views/erp/InventoryCheck.vue`
- [ ] T058 [US3] 创建库存 API 客户端 `frontend/src/api/inventory.js`
- [ ] T059 [US3] 实现库存预警 `frontend/src/views/erp/InventoryAlert.vue`
- [ ] T060 [US3] 实现批次管理 `frontend/src/views/erp/BatchManage.vue`

**Checkpoint**: 仓库管理完整可用

---

## Phase 6: US4 - 采购结算管理 (P2)

**Duration**: Week 5

### Service
- [ ] T061 [US4] 创建 PurchaseSettlementService `services/erp-service/src/main/java/com/example/erp/service/PurchaseSettlementService.java`

### Controller
- [ ] T062 [US4] 实现 PurchaseSettlementController `services/erp-service/src/main/java/com/example/erp/controller/PurchaseSettlementController.java`

### Frontend
- [ ] T063 [P] [US4] 创建采购结算页面 `frontend/src/views/erp/PurchaseSettlement.vue`
- [ ] T064 [US4] 创建结算 API 客户端 `frontend/src/api/settlement.js`

**Checkpoint**: 采购结算可用

---

## Phase 7: US5 - 销售结算管理 (P2)

**Duration**: Week 5

### Service
- [ ] T065 [US5] 创建 SalesSettlementService `services/erp-service/src/main/java/com/example/erp/service/SalesSettlementService.java`

### Controller
- [ ] T066 [US5] 实现 SalesSettlementController `services/erp-service/src/main/java/com/example/erp/controller/SalesSettlementController.java`

### Frontend
- [ ] T067 [P] [US5] 创建销售结算页面 `frontend/src/views/erp/SalesSettlement.vue`
- [ ] T068 [US5] 实现收款管理 `frontend/src/views/erp/PaymentReceive.vue`

**Checkpoint**: 销售结算可用

---

## Phase 8: US6 - 报表与统计分析 (P2)

**Duration**: Week 6

### Service
- [ ] T069 [US6] 创建 ReportService `services/erp-service/src/main/java/com/example/erp/service/ReportService.java`
- [ ] T070 [US6] 创建 PurchaseReportService `services/erp-service/src/main/java/com/example/erp/service/report/PurchaseReportService.java`
- [ ] T071 [US6] 创建 SalesReportService `services/erp-service/src/main/java/com/example/erp/service/report/SalesReportService.java`
- [ ] T072 [US6] 创建 InventoryReportService `services/erp-service/src/main/java/com/example/erp/service/report/InventoryReportService.java`
- [ ] T073 [US6] 创建 ProfitReportService `services/erp-service/src/main/java/com/example/erp/service/report/ProfitReportService.java`

### Controller
- [ ] T074 [US6] 实现 ReportController `services/erp-service/src/main/java/com/example/erp/controller/ReportController.java`
- [ ] T075 [US6] 实现报表导出 API `services/erp-service/src/main/java/com/example/erp/controller/ReportExportController.java`

### Frontend
- [ ] T076 [P] [US6] 创建采购报表页面 `frontend/src/views/erp/report/PurchaseReport.vue`
- [ ] T077 [P] [US6] 创建销售报表页面 `frontend/src/views/erp/report/SalesReport.vue`
- [ ] T078 [P] [US6] 创建库存报表页面 `frontend/src/views/erp/report/InventoryReport.vue`
- [ ] T079 [P] [US6] 创建利润分析页面 `frontend/src/views/erp/report/ProfitAnalysis.vue`
- [ ] T080 [US6] 创建报表 API 客户端 `frontend/src/api/report.js`

**Checkpoint**: 报表统计可用

---

## Phase 9: US7 - 商品与价格管理 (P2)

**Duration**: Week 6

### Entity
- [ ] T081 [US7] 创建 ProductCategory 实体 `services/erp-service/src/main/java/com/example/erp/entity/ProductCategory.java`
- [ ] T082 [P] [US7] 创建 ProductPrice 实体 `services/erp-service/src/main/java/com/example/erp/entity/ProductPrice.java`
- [ ] T083 [P] [US7] 创建 ProductPromotion 实体 `services/erp-service/src/main/java/com/example/erp/entity/ProductPromotion.java`

### Service
- [ ] T084 [US7] 创建 ProductService `services/erp-service/src/main/java/com/example/erp/service/ProductService.java`
- [ ] T085 [US7] 创建 ProductCategoryService `services/erp-service/src/main/java/com/example/erp/service/ProductCategoryService.java`
- [ ] T086 [US7] 创建 ProductPriceService `services/erp-service/src/main/java/com/example/erp/service/ProductPriceService.java`

### Controller
- [ ] T087 [US7] 实现 ProductController `services/erp-service/src/main/java/com/example/erp/controller/ProductController.java`
- [ ] T088 [US7] 实现 ProductCategoryController `services/erp-service/src/main/java/com/example/erp/controller/ProductCategoryController.java`
- [ ] T089 [US7] 实现 ProductPriceController `services/erp-service/src/main/java/com/example/erp/controller/ProductPriceController.java`

### Frontend
- [ ] T090 [P] [US7] 创建商品列表页面 `frontend/src/views/erp/product/ProductList.vue`
- [ ] T091 [P] [US7] 创建商品分类页面 `frontend/src/views/erp/product/CategoryList.vue`
- [ ] T092 [P] [US7] 创建价格管理页面 `frontend/src/views/erp/product/PriceManage.vue`
- [ ] T093 [P] [US7] 创建促销价格页面 `frontend/src/views/erp/product/PromotionList.vue`
- [ ] T094 [US7] 创建商品 API 客户端 `frontend/src/api/product.js`

**Checkpoint**: 商品价格管理可用

---

## Phase 10: US8 - 系统配置与权限管理 (P3)

**Duration**: Week 7

### Entity
- [ ] T095 [US8] 创建 SystemConfig 实体 `services/erp-service/src/main/java/com/example/erp/entity/SystemConfig.java`
- [ ] T096 [P] [US8] 创建 ApprovalRule 实体 `services/erp-service/src/main/java/com/example/erp/entity/ApprovalRule.java`
- [ ] T097 [P] [US8] 创建 NumberRule 实体 `services/erp-service/src/main/java/com/example/erp/entity/NumberRule.java`

### Service
- [ ] T098 [US8] 创建 SystemConfigService `services/erp-service/src/main/java/com/example/erp/service/SystemConfigService.java`
- [ ] T099 [US8] 创建 ApprovalRuleService `services/erp-service/src/main/java/com/example/erp/service/ApprovalRuleService.java`
- [ ] T100 [US8] 创建 NumberRuleService `services/erp-service/src/main/java/com/example/erp/service/NumberRuleService.java`

### Controller
- [ ] T101 [US8] 实现 SystemConfigController `services/erp-service/src/main/java/com/example/erp/controller/SystemConfigController.java`
- [ ] T102 [US8] 实现 ApprovalRuleController `services/erp-service/src/main/java/com/example/erp/controller/ApprovalRuleController.java`
- [ ] T103 [US8] 实现 NumberRuleController `services/erp-service/src/main/java/com/example/erp/controller/NumberRuleController.java`

### Frontend
- [ ] T104 [P] [US8] 创建系统配置页面 `frontend/src/views/erp/config/SystemConfig.vue`
- [ ] T105 [P] [US8] 创建审批规则页面 `frontend/src/views/erp/config/ApprovalRule.vue`
- [ ] T106 [P] [US8] 创建编号规则页面 `frontend/src/views/erp/config/NumberRule.vue`
- [ ] T107 [US8] 创建配置 API 客户端 `frontend/src/api/config.js`

**Checkpoint**: 系统配置可用

---

## Summary

| Metric | Value |
|--------|-------|
| **Total Tasks** | 107 |
| **Parallel Tasks** | 56 |
| **Phases** | 10 |
| **Duration** | 7 weeks |

### Overview

| Story | Name | Priority | Tasks | Parallel |
|-------|------|----------|-------|----------|
| US1 | 采购管理 | P1 | 14 | 7 |
| US2 | 销售管理 | P1 | 14 | 7 |
| US3 | 仓库管理 | P1 | 16 | 8 |
| US4 | 采购结算管理 | P2 | 4 | 2 |
| US5 | 销售结算管理 | P2 | 4 | 2 |
| US6 | 报表与统计分析 | P2 | 12 | 6 |
| US7 | 商品与价格管理 | P2 | 14 | 7 |
| US8 | 系统配置与权限管理 | P3 | 13 | 6 |

### MVP Scope

- Phase 1-2: Setup + Foundation
- Phase 3: US1 采购管理
- Phase 4: US2 销售管理
- Phase 5: US3 仓库管理

**MVP Tasks**: 60 tasks
**MVP Duration**: ~4 weeks
