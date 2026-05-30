# Tasks: 财务模块

**Feature Branch**: `007-finance-module`
**Generated**: 2026-04-07
**Total Tasks**: 100

## Overview

本文档按用户故事组织任务，支持独立实现和测试。每个阶段完成后可独立验证功能。

### User Stories Priority

| Story | Name | Priority | Tasks | Parallel |
|-------|------|----------|-------|----------|
| US1 | 应收应付管理 | P1 | 12 | 6 |
| US2 | 收支记录管理 | P1 | 10 | 5 |
| US3 | 发票管理 | P1 | 10 | 5 |
| US4 | 成本核算与毛利计算 | P1 | 12 | 6 |
| US5 | 银行对账 | P2 | 9 | 4 |
| US6 | 预算管理 | P2 | 11 | 5 |
| US7 | 财务报表生成 | P1 | 12 | 6 |
| US8 | 进销存结算集成 | P1 | 8 | 3 |

### Dependencies Graph

```
Phase 1 (Setup) ──► Phase 2 (Foundation)
                          │
         ┌────────────────┼────────────────┐
         │                │                │
         ▼                ▼                ▼
       US1              US2              US3
         │                │                │
         └────────────────┼────────────────┘
                          │
         ┌────────────────┼────────────────┐
         │                │                │
         ▼                ▼                ▼
       US4              US7              US8
         │                │                │
         └────────────────┴────────────────┘
                          │
                    ┌─────┴─────┐
                    │           │
                    ▼           ▼
                  US5         US6
```

---

## Phase 1: Setup (项目初始化)

**Goal**: 创建项目骨架和基础设施

**Duration**: Week 1-2

### Tasks

- [x] T001 创建 finance-service Maven 模块 `backend/finance-service/pom.xml` ✅ 实际已存在
- [x] T002 [P] 创建 Spring Boot 主类 `backend/finance-service/src/main/java/com/company/finance/FinanceServiceApplication.java` ✅ 实际已存在
- [x] T003 [P] 配置 application.yml `backend/finance-service/src/main/resources/application.yml` ✅ 实际已存在
- [ ] T004 [P] 配置 application-dev.yml `backend/finance-service/src/main/resources/application-dev.yml`
- [x] T005 创建数据库 schema 脚本 `backend/finance-service/src/main/resources/db/migration/V1__finance_schema.sql` ✅ 实际位于 resources/schema.sql
- [ ] T006 [P] 创建索引脚本 `backend/finance-service/src/main/resources/db/migration/V2__finance_indexes.sql`
- [ ] T007 [P] 创建初始化数据脚本 `backend/finance-service/src/main/resources/db/migration/V3__finance_init_data.sql`
- [ ] T008 [P] 创建多币种金额组件 `backend/finance-service/src/main/java/com/company/finance/common/Money.java`
- [ ] T009 [P] 创建审计日志切面 `backend/finance-service/src/main/java/com/company/finance/common/AuditAspect.java`
- [ ] T010 [P] 创建全局异常处理 `backend/finance-service/src/main/java/com/company/finance/common/GlobalExceptionHandler.java`
- [ ] T011 [P] 创建统一响应类 `backend/finance-service/src/main/java/com/company/finance/common/ApiResponse.java` ✅ 可能位于 common 模块
- [ ] T012 [P] 创建租户上下文 `backend/finance-service/src/main/java/com/company/finance/common/TenantContext.java` ✅ 可能位于 common 模块

**Checkpoint**: 项目可启动，数据库表创建成功

---

## Phase 2: Foundation (基础设施)

**Goal**: 创建共享实体和基础服务

**Duration**: Week 2

### Tasks

- [x] T013 创建 FinAccount 实体 `backend/finance-service/src/main/java/com/company/finance/entity/FinAccount.java` ✅ 实际位于 BankAccount.java
- [x] T014 [P] 创建 FinBill 实体 `backend/finance-service/src/main/java/com/company/finance/entity/FinBill.java` ✅ 实际位于 Bill.java
- [x] T015 [P] 创建 FinTransaction 实体 `backend/finance-service/src/main/java/com/company/finance/entity/FinTransaction.java` ✅ 实际位于 PaymentRecord.java
- [x] T016 [P] 创建 FinInvoice 实体 `backend/finance-service/src/main/java/com/company/finance/entity/FinInvoice.java` ✅ 实际位于 Invoice.java
- [x] T017 [P] 创建 FinPaymentRecord 实体 `backend/finance-service/src/main/java/com/company/finance/entity/FinPaymentRecord.java` ✅ 实际位于 PaymentRecord.java
- [x] T018 [P] 创建 FinBillInvoice 实体 `backend/finance-service/src/main/java/com/company/finance/entity/FinBillInvoice.java` ✅ 实际位于 BillInvoice.java
- [x] T019 [P] 创建 FinCostConfig 实体 `backend/finance-service/src/main/java/com/company/finance/entity/FinCostConfig.java` ✅ 实际已存在
- [x] T020 [P] 创建 FinCostHistory 实体 `backend/finance-service/src/main/java/com/company/finance/entity/FinCostHistory.java` ✅ 实际已存在
- [x] T021 [P] 创建 FinProfitCalc 实体 `backend/finance-service/src/main/java/com/company/finance/entity/FinProfitCalc.java` ✅ 实际已存在
- [x] T022 [P] 创建 FinBankRecord 实体 `backend/finance-service/src/main/java/com/company/finance/entity/FinBankRecord.java` ✅ 实际已存在
- [x] T023 [P] 创建 FinBudget 实体 `backend/finance-service/src/main/java/com/company/finance/entity/FinBudget.java` ✅ 实际已存在
- [x] T024 [P] 创建 FinReport 实体 `backend/finance-service/src/main/java/com/company/finance/entity/FinReport.java` ✅ 实际已存在

**Checkpoint**: 所有实体类编译通过，MyBatis-Plus 映射正常

---

## Phase 3: US1 - 应收应付管理 (P1)

**Goal**: 完整管理应收账款和应付账款

**Independent Test**: 创建账单 → 审核 → 收款/付款 → 核销 → 查看余额

**Duration**: Week 3-4

### Tasks

#### Model & Mapper
- [x] T025 [P] [US1] 创建 BillMapper `backend/finance-service/src/main/java/com/company/finance/mapper/BillMapper.java` ✅ 实际已存在
- [x] T026 [P] [US1] 创建 PaymentRecordMapper `backend/finance-service/src/main/java/com/company/finance/mapper/PaymentRecordMapper.java` ✅ 实际已存在
- [x] T027 [P] [US1] 创建 AccountMapper `backend/finance-service/src/main/java/com/company/finance/mapper/AccountMapper.java` ✅ 实际位于 AccountPayableMapper + AccountReceivableMapper + BankAccountMapper

#### DTO
- [ ] T028 [P] [US1] 创建 BillCreateRequest `backend/finance-service/src/main/java/com/company/finance/bill/dto/BillCreateRequest.java` （无独立DTO，直接使用实体）
- [ ] T029 [P] [US1] 创建 BillResponse `backend/finance-service/src/main/java/com/company/finance/bill/dto/BillResponse.java` （无独立DTO）
- [ ] T030 [P] [US1] 创建 BillDetailResponse `backend/finance-service/src/main/java/com/company/finance/bill/dto/BillDetailResponse.java` （无独立DTO）

#### Service
- [x] T031 [US1] 创建 AccountService 接口 `backend/finance-service/src/main/java/com/company/finance/account/service/AccountService.java` ✅ 实际位于 AccountPayableService + AccountReceivableService + BankAccountService
- [x] T032 [US1] 实现 AccountServiceImpl `backend/finance-service/src/main/java/com/company/finance/account/service/impl/AccountServiceImpl.java` ✅ 功能直接在 Service 类中实现
- [x] T033 [US1] 创建 BillService 接口 `backend/finance-service/src/main/java/com/company/finance/bill/service/BillService.java` ✅ 实际已存在，含完整生命周期（create/update/submit/approve/reject/cancel/summary/createFromPurchase/Sales）
- [x] T034 [US1] 实现 BillServiceImpl `backend/finance-service/src/main/java/com/company/finance/bill/service/impl/BillServiceImpl.java` ✅ 功能直接在 BillService.java 中实现
- [x] T035 [US1] 实现 BillStateMachine `backend/finance-service/src/main/java/com/company/finance/bill/service/BillStateMachine.java` ✅ 状态转换逻辑在 BillService 内部实现

#### Controller
- [x] T036 [US1] 实现 AccountController `backend/finance-service/src/main/java/com/company/finance/account/controller/AccountController.java` ✅ 功能位于 FinanceController
- [x] T037 [US1] 实现 BillController `backend/finance-service/src/main/java/com/company/finance/bill/controller/BillController.java` ✅ 实际已存在，含11个端点（CRUD + workflow + payments + invoices + summary）

**Checkpoint**: 可通过 API 完成账单创建、审核、收付款、核销全流程

---

## Phase 4: US2 - 收支记录管理 (P1)

**Goal**: 记录和管理所有收入和支出

**Independent Test**: 收支 CRUD、转账、统计查询

**Duration**: Week 4

### Tasks

#### Mapper
- [x] T038 [P] [US2] 创建 TransactionMapper `backend/finance-service/src/main/java/com/company/finance/mapper/TransactionMapper.java` ✅ 实际位于 PaymentRecordMapper

#### DTO
- [ ] T039 [P] [US2] 创建 TransactionCreateRequest `backend/finance-service/src/main/java/com/company/finance/transaction/dto/TransactionCreateRequest.java` （无独立DTO）
- [ ] T040 [P] [US2] 创建 TransactionResponse `backend/finance-service/src/main/java/com/company/finance/transaction/dto/TransactionResponse.java` （无独立DTO）
- [ ] T041 [P] [US2] 创建 TransferRequest `backend/finance-service/src/main/java/com/company/finance/transaction/dto/TransferRequest.java` （实际有 TransferVO）

#### Service
- [x] T042 [US2] 创建 TransactionService 接口 `backend/finance-service/src/main/java/com/company/finance/transaction/service/TransactionService.java` ✅ 实际位于 PaymentRecordService（CRUD + approve + cancel）
- [x] T043 [US2] 实现 TransactionServiceImpl `backend/finance-service/src/main/java/com/company/finance/transaction/service/impl/TransactionServiceImpl.java` ✅ 功能直接在 PaymentRecordService 中实现

#### Controller
- [x] T044 [US2] 实现 TransactionController `backend/finance-service/src/main/java/com/company/finance/transaction/controller/TransactionController.java` ✅ 功能位于 FinanceController
- [ ] T045 [US2] 实现收支统计 API `backend/finance-service/src/main/java/com/company/finance/transaction/controller/TransactionSummaryController.java` （非独立控制器）
- [ ] T046 [US2] 实现收支导出 API `backend/finance-service/src/main/java/com/company/finance/transaction/controller/TransactionExportController.java`

**Checkpoint**: 可通过 API 完成收支记录、转账、统计查询、导出

---

## Phase 5: US3 - 发票管理 (P1)

**Goal**: 管理销售和采购发票，支持发票与账单关联

**Independent Test**: 发票 CRUD、关联账单、发票统计

**Duration**: Week 5

### Tasks

#### Mapper
- [x] T047 [P] [US3] 创建 InvoiceMapper `backend/finance-service/src/main/java/com/company/finance/mapper/InvoiceMapper.java` ✅ 实际已存在
- [x] T048 [P] [US3] 创建 BillInvoiceMapper `backend/finance-service/src/main/java/com/company/finance/mapper/BillInvoiceMapper.java` ✅ 实际已存在

#### DTO
- [ ] T049 [P] [US3] 创建 InvoiceCreateRequest `backend/finance-service/src/main/java/com/company/finance/invoice/dto/InvoiceCreateRequest.java` （无独立DTO）
- [ ] T050 [P] [US3] 创建 InvoiceResponse `backend/finance-service/src/main/java/com/company/finance/invoice/dto/InvoiceResponse.java` （无独立DTO）

#### Service
- [x] T051 [US3] 创建 InvoiceService 接口 `backend/finance-service/src/main/java/com/company/finance/invoice/service/InvoiceService.java` ✅ 实际已存在，含 CRUD + void + statistics
- [x] T052 [US3] 实现 InvoiceServiceImpl `backend/finance-service/src/main/java/com/company/finance/invoice/service/impl/InvoiceServiceImpl.java` ✅ 功能直接在 InvoiceService 中实现

#### Controller
- [x] T053 [US3] 实现 InvoiceController `backend/finance-service/src/main/java/com/company/finance/invoice/controller/InvoiceController.java` ✅ 实际已存在，含7个端点
- [x] T054 [US3] 实现发票统计 API `backend/finance-service/src/main/java/com/company/finance/invoice/controller/InvoiceStatisticsController.java` ✅ 实际位于 InvoiceService.getStatistics()
- [x] T055 [US3] 实现发票账单关联 API `backend/finance-service/src/main/java/com/company/finance/invoice/controller/InvoiceBillController.java` ✅ 实际位于 BillService.getBillInvoices()

**Checkpoint**: 可通过 API 完成发票创建、关联账单、作废、统计

---

## Phase 6: US4 - 成本核算与毛利计算 (P1)

**Goal**: 支持多种成本计算方法，自动计算销售毛利

**Independent Test**: 成本方法设置、成本计算、毛利报表

**Duration**: Week 5-6

### Tasks

#### Strategy Pattern
- [x] T056 [P] [US4] 创建 CostCalculationStrategy 接口 `backend/finance-service/src/main/java/com/company/finance/cost/strategy/CostCalculationStrategy.java` ✅ 实际已存在
- [x] T057 [P] [US4] 实现 FifoCostStrategy `backend/finance-service/src/main/java/com/company/finance/cost/strategy/FifoCostStrategy.java` ✅ 实际已存在
- [x] T058 [P] [US4] 实现 WeightedAverageCostStrategy `backend/finance-service/src/main/java/com/company/finance/cost/strategy/WeightedAverageCostStrategy.java` ✅ 实际已存在
- [x] T059 [P] [US4] 实现 SpecificIdentificationStrategy `backend/finance-service/src/main/java/com/company/finance/cost/strategy/SpecificIdentificationStrategy.java` ✅ 实际位于 SpecificIdentificationCostStrategy

#### Service
- [x] T060 [US4] 创建 CostCalculationService 接口 `backend/finance-service/src/main/java/com/company/finance/cost/service/CostCalculationService.java` ✅ 实际已存在，含 setCostMethod/calculateOutboundCost/recordInboundCost
- [x] T061 [US4] 实现 CostCalculationServiceImpl `backend/finance-service/src/main/java/com/company/finance/cost/service/impl/CostCalculationServiceImpl.java` ✅ 功能直接在 CostCalculationService 中实现
- [x] T062 [US4] 创建 ProfitCalculationService `backend/finance-service/src/main/java/com/company/finance/cost/service/ProfitCalculationService.java` ✅ 实际位于 CostCalculationService 内部

#### Controller
- [x] T063 [US4] 实现 CostConfigController `backend/finance-service/src/main/java/com/company/finance/cost/controller/CostConfigController.java` ✅ 实际位于 CostCalculationController
- [x] T064 [US4] 实现 ProfitAnalysisController `backend/finance-service/src/main/java/com/company/finance/cost/controller/ProfitAnalysisController.java` ✅ 实际位于 CostCalculationController

**Checkpoint**: 可设置成本方法、计算成本、查看毛利分析

---

## Phase 7: US7 - 财务报表生成 (P1)

**Goal**: 自动生成资产负债表、利润表、现金流量表

**Independent Test**: 报表生成、导出、锁定

**Duration**: Week 6-7

### Tasks

#### Generator
- [x] T065 [P] [US7] 创建 BalanceSheetGenerator `backend/finance-service/src/main/java/com/company/finance/report/generator/BalanceSheetGenerator.java` ✅ 实际位于 FinanceReportService.generateBalanceSheet()
- [x] T066 [P] [US7] 创建 IncomeStatementGenerator `backend/finance-service/src/main/java/com/company/finance/report/generator/IncomeStatementGenerator.java` ✅ 实际位于 FinanceReportService.generateIncomeStatement()
- [x] T067 [P] [US7] 创建 CashFlowGenerator `backend/finance-service/src/main/java/com/company/finance/report/generator/CashFlowGenerator.java` ✅ 实际位于 FinanceReportService.generateCashFlowStatement()

#### Service
- [x] T068 [US7] 创建 ReportService 接口 `backend/finance-service/src/main/java/com/company/finance/report/service/ReportService.java` ✅ 实际位于 FinanceReportService
- [x] T069 [US7] 实现 ReportServiceImpl `backend/finance-service/src/main/java/com/company/finance/report/service/impl/ReportServiceImpl.java` ✅ 功能直接在 FinanceReportService 中实现

#### Export
- [ ] T070 [P] [US7] 实现 ExcelExporter `backend/finance-service/src/main/java/com/company/finance/report/export/ExcelExporter.java`
- [ ] T071 [P] [US7] 实现 PdfExporter `backend/finance-service/src/main/java/com/company/finance/report/export/PdfExporter.java`

#### Controller
- [x] T072 [US7] 实现 ReportController `backend/finance-service/src/main/java/com/company/finance/report/controller/ReportController.java` ✅ 实际位于 FinanceReportController
- [ ] T073 [US7] 实现 ReportExportController `backend/finance-service/src/main/java/com/company/finance/report/controller/ReportExportController.java`

#### Templates
- [ ] T074 [US7] 创建资产负债表模板 `backend/finance-service/src/main/resources/report-templates/balance-sheet-template.xlsx`
- [ ] T075 [US7] 创建利润表模板 `backend/finance-service/src/main/resources/report-templates/income-statement-template.xlsx`
- [ ] T076 [US7] 创建现金流量表模板 `backend/finance-service/src/main/resources/report-templates/cash-flow-template.xlsx`

**Checkpoint**: 可生成三大报表、导出 Excel/PDF、锁定报表

---

## Phase 8: US8 - 进销存结算集成 (P1)

**Goal**: 与进销存模块深度集成，自动生成财务数据

**Independent Test**: 采购入库→应付→付款；销售出库→应收→收款→毛利

**Duration**: Week 7-8

### Tasks

#### Event Definition
- [x] T077 [P] [US8] 创建 PurchaseCompletedEvent `backend/finance-service/src/main/java/com/company/finance/integration/event/PurchaseCompletedEvent.java` ✅ 实际已存在
- [x] T078 [P] [US8] 创建 SalesCompletedEvent `backend/finance-service/src/main/java/com/company/finance/integration/event/SalesCompletedEvent.java` ✅ 实际已存在

#### Event Listener
- [x] T079 [US8] 实现 PurchaseEventListener `backend/finance-service/src/main/java/com/company/finance/integration/listener/PurchaseEventListener.java` ✅ 实际位于 ErpEventListener.handlePurchaseCompleted()
- [x] T080 [US8] 实现 SalesEventListener `backend/finance-service/src/main/java/com/company/finance/integration/listener/SalesEventListener.java` ✅ 实际位于 ErpEventListener.handleSalesCompleted()

#### Settlement Service
- [x] T081 [US8] 创建 SettlementService `backend/finance-service/src/main/java/com/company/finance/integration/service/SettlementService.java` ✅ 实际已存在
- [x] T082 [US8] 实现结算链路追溯 API `backend/finance-service/src/main/java/com/company/finance/integration/controller/SettlementController.java` ✅ 实际位于 SettlementController

**Checkpoint**: 进销存操作自动触发财务数据生成

---

## Phase 9: US5 - 银行对账 (P2)

**Goal**: 导入银行流水，自动/手动对账

**Independent Test**: 流水导入、自动匹配、手动对账、差异报告

**Duration**: Week 8-9

### Tasks

#### Service
- [x] T083 [US5] 创建 BankReconciliationService `backend/finance-service/src/main/java/com/company/finance/reconciliation/service/BankReconciliationService.java` ✅ 实际已存在，含 import/autoMatch/manualMatch/unmatch
- [x] T084 [US5] 实现 AutoMatchingService `backend/finance-service/src/main/java/com/company/finance/reconciliation/service/impl/AutoMatchingService.java` ✅ 实际位于 BankReconciliationService 内部（基于评分匹配）

#### Controller
- [x] T085 [US5] 实现 BankReconciliationController `backend/finance-service/src/main/java/com/company/finance/reconciliation/controller/BankReconciliationController.java` ✅ 实际已存在，含6个端点
- [x] T086 [US5] 实现银行流水导入 API `backend/finance-service/src/main/java/com/company/finance/reconciliation/controller/BankRecordImportController.java` ✅ 实际位于 BankReconciliationService.importBankStatement()
- [x] T087 [US5] 实现对账差异报告 API `backend/finance-service/src/main/java/com/company/finance/reconciliation/controller/ReconciliationReportController.java` ✅ 实际位于 BankReconciliationService（含未匹配记录 + 统计）

**Checkpoint**: 可导入银行流水、自动匹配、手动对账、查看差异

---

## Phase 10: US6 - 预算管理 (P2)

**Goal**: 预算编制、执行跟踪、预警通知

**Independent Test**: 预算编制、审批、执行跟踪、预警

**Duration**: Week 9-10

### Tasks

#### Service
- [x] T088 [US6] 创建 BudgetService `backend/finance-service/src/main/java/com/company/finance/budget/service/BudgetService.java` ✅ 实际已存在，含 create/approve/update/check/recordUsage/execution tracking
- [x] T089 [US6] 实现 BudgetServiceImpl `backend/finance-service/src/main/java/com/company/finance/budget/service/impl/BudgetServiceImpl.java` ✅ 功能直接在 BudgetService 中实现

#### Controller
- [x] T090 [US6] 实现 BudgetController `backend/finance-service/src/main/java/com/company/finance/budget/controller/BudgetController.java` ✅ 实际已存在，含7个端点
- [x] T091 [US6] 实现预算审批 API `backend/finance-service/src/main/java/com/company/finance/budget/controller/BudgetApprovalController.java` ✅ 实际位于 BudgetController POST /approve/{id}
- [x] T092 [US6] 实现预算执行 API `backend/finance-service/src/main/java/com/company/finance/budget/controller/BudgetExecutionController.java` ✅ 实际位于 BudgetController GET /execution/{id}

#### Interceptor
- [ ] T093 [US6] 实现 BudgetControlInterceptor `backend/finance-service/src/main/java/com/company/finance/budget/interceptor/BudgetControlInterceptor.java` （预算检查逻辑在 BudgetService.checkBudget() 中实现）

**Checkpoint**: 可编制预算、审批、跟踪执行、接收预警

---

## Phase 11: Polish & Cross-Cutting (收尾)

**Goal**: 优化、文档、部署配置

**Duration**: Week 11-12

### Tasks

#### Performance
- [ ] T094 [P] 添加报表数据缓存 `backend/finance-service/src/main/java/com/company/finance/report/cache/ReportCache.java`
- [ ] T095 [P] 优化大数据量查询索引 `backend/finance-service/src/main/resources/db/migration/V4__performance_indexes.sql`

#### Security
- [ ] T096 [P] 实现金额字段加密 `backend/finance-service/src/main/java/com/company/finance/common/encrypt/AmountEncryptor.java`
- [ ] T097 [P] 实现数据权限过滤 `backend/finance-service/src/main/java/com/company/finance/common/permission/DataPermissionFilter.java`

#### Deployment
- [ ] T098 创建 Dockerfile `backend/finance-service/Dockerfile`
- [ ] T099 创建 docker-compose.yml `backend/finance-service/docker-compose.yml`
- [ ] T100 创建 Kubernetes Deployment `backend/finance-service/k8s/deployment.yaml`

**Checkpoint**: 系统可部署，性能达标，安全加固完成

---

## Summary

| Metric | Value |
|--------|-------|
| **Total Tasks** | 100 |
| **Completed** | 68 |
| **Remaining** | 32 |
| **Parallel Tasks** | 52 |
| **Phases** | 11 |
| **Duration** | 12 weeks |

### MVP Scope (Minimum Viable Product)

建议 MVP 包含：
- Phase 1-2: Setup + Foundation
- Phase 3: US1 应收应付管理
- Phase 4: US2 收支记录管理

**MVP Tasks**: 46 tasks
**MVP Duration**: ~4 weeks

### Parallel Execution Examples

```bash
# Phase 1 - 可并行执行的任务
T002, T003, T004, T006, T007, T008, T009, T010, T011, T012

# Phase 2 - 可并行执行的实体创建
T014-T024 (所有实体可并行创建)

# US1 - 可并行的任务
T025, T026, T027 (Mappers)
T028, T029, T030 (DTOs)
```
