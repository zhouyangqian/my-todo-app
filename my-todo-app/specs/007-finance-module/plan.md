# Implementation Plan: 财务模块

**Feature Branch**: `007-finance-module`
**Created**: 2026-04-07
**Status**: Draft

## Technical Context

| Aspect | Choice | Notes |
|--------|--------|-------|
| **Language** | Java 17+ (LTS) | 与现有项目一致 |
| **Framework** | Spring Boot 3.0 + Spring Cloud | 微服务架构 |
| **ORM** | MyBatis-Plus 3.5+ | 复杂查询灵活 |
| **Database** | MySQL 8.0+ (主) + Redis 7.0+ (缓存) | 事务支持 |
| **Message Queue** | RocketMQ/Kafka | 异步事件处理 |
| **Workflow** | Flowable | 审批流程 |
| **Report Engine** | Jxls + Apache POI | Excel报表 |
| **Tracing** | SkyWalking/Zipkin | 链路追踪 |

## Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                        API Gateway (004)                         │
└─────────────────────────────────────────────────────────────────┘
                                 │
                                 ▼
┌─────────────────────────────────────────────────────────────────┐
│                     Finance Service                              │
├─────────────────────────────────────────────────────────────────┤
│  ┌─────────────┐ ┌─────────────┐ ┌─────────────┐ ┌───────────┐ │
│  │   Bill      │ │Transaction  │ │  Invoice    │ │  Report   │ │
│  │  Service    │ │  Service    │ │  Service    │ │  Service  │ │
│  └─────────────┘ └─────────────┘ └─────────────┘ └───────────┘ │
│  ┌─────────────┐ ┌─────────────┐ ┌─────────────┐               │
│  │   Cost      │ │   Budget    │ │  Bank Rec.  │               │
│  │  Service    │ │  Service    │ │  Service    │               │
│  └─────────────┘ └─────────────┘ └─────────────┘               │
├─────────────────────────────────────────────────────────────────┤
│                      Event Handlers                              │
│  ┌─────────────────────────────────────────────────────────────┐│
│  │ PurchaseCompletedHandler │ SalesCompletedHandler │ etc.     ││
│  └─────────────────────────────────────────────────────────────┘│
├─────────────────────────────────────────────────────────────────┤
│                      Message Queue Listener                      │
│  ┌─────────────────────────────────────────────────────────────┐│
│  │  erp-events-topic  →  Finance Event Consumers               ││
│  └─────────────────────────────────────────────────────────────┘│
└─────────────────────────────────────────────────────────────────┘
                                 │
        ┌────────────────────────┼────────────────────────┐
        ▼                        ▼                        ▼
┌───────────────┐      ┌───────────────┐      ┌───────────────┐
│    MySQL      │      │    Redis      │      │   RocketMQ    │
│  (主存储)      │      │  (缓存/锁)    │      │  (消息队列)    │
└───────────────┘      └───────────────┘      └───────────────┘
```

## Implementation Phases

### Phase 1: Core Infrastructure (Week 1-2)

#### 1.1 数据库设计
- [ ] 创建数据库表结构 (12张核心表)
- [ ] 配置索引和约束
- [ ] 创建初始化数据脚本 (科目表、枚举值)
- [ ] 设置数据分区策略

**Files**:
- `backend/finance-service/src/main/resources/db/migration/V1__finance_schema.sql`
- `backend/finance-service/src/main/resources/db/migration/V2__finance_indexes.sql`
- `backend/finance-service/src/main/resources/db/migration/V3__finance_init_data.sql`

#### 1.2 基础框架搭建
- [ ] 创建 finance-service 模块
- [ ] 配置 Spring Boot 应用
- [ ] 集成 MyBatis-Plus
- [ ] 配置 Redis 缓存
- [ ] 配置消息队列

**Files**:
- `backend/finance-service/pom.xml`
- `backend/finance-service/src/main/resources/application.yml`
- `backend/finance-service/src/main/java/com/company/finance/FinanceServiceApplication.java`

#### 1.3 通用组件开发
- [ ] 多币种金额处理组件
- [ ] 审计日志切面
- [ ] 数据权限过滤器
- [ ] 统一异常处理
- [ ] 统一响应格式

**Files**:
- `backend/finance-service/src/main/java/com/company/finance/common/Money.java`
- `backend/finance-service/src/main/java/com/company/finance/common/AuditAspect.java`
- `backend/finance-service/src/main/java/com/company/finance/common/GlobalExceptionHandler.java`

---

### Phase 2: 账户与收支管理 (Week 3-4)

#### 2.1 财务账户管理
- [ ] 账户 CRUD API
- [ ] 账户余额计算
- [ ] 多币种账户支持
- [ ] 账户状态管理

**Files**:
- `backend/finance-service/src/main/java/com/company/finance/account/`
  - `controller/AccountController.java`
  - `service/AccountService.java`
  - `service/impl/AccountServiceImpl.java`
  - `mapper/AccountMapper.java`
  - `entity/FinAccount.java`
  - `dto/AccountCreateRequest.java`
  - `dto/AccountResponse.java`

#### 2.2 收支记录管理
- [ ] 收支记录 CRUD API
- [ ] 收支分类管理
- [ ] 账户余额自动更新
- [ ] 转账功能
- [ ] 收支统计查询

**Files**:
- `backend/finance-service/src/main/java/com/company/finance/transaction/`
  - `controller/TransactionController.java`
  - `service/TransactionService.java`
  - `service/impl/TransactionServiceImpl.java`
  - `mapper/TransactionMapper.java`
  - `entity/FinTransaction.java`
  - `dto/TransactionCreateRequest.java`
  - `dto/TransactionResponse.java`

---

### Phase 3: 账单与发票管理 (Week 5-6)

#### 3.1 应收应付账单
- [ ] 账单 CRUD API
- [ ] 账单状态机实现
- [ ] 账单审核流程
- [ ] 收付款记录管理
- [ ] 账单核销逻辑
- [ ] 账单汇总统计

**Files**:
- `backend/finance-service/src/main/java/com/company/finance/bill/`
  - `controller/BillController.java`
  - `service/BillService.java`
  - `service/impl/BillServiceImpl.java`
  - `service/BillStateMachine.java`
  - `mapper/BillMapper.java`
  - `mapper/PaymentRecordMapper.java`
  - `entity/FinBill.java`
  - `entity/FinPaymentRecord.java`

#### 3.2 发票管理
- [ ] 发票 CRUD API
- [ ] 发票与账单关联
- [ ] 发票状态管理
- [ ] 发票统计报表

**Files**:
- `backend/finance-service/src/main/java/com/company/finance/invoice/`
  - `controller/InvoiceController.java`
  - `service/InvoiceService.java`
  - `service/impl/InvoiceServiceImpl.java`
  - `mapper/InvoiceMapper.java`
  - `entity/FinInvoice.java`
  - `entity/FinBillInvoice.java`

---

### Phase 4: 成本核算与集成 (Week 7-8)

#### 4.1 成本核算引擎
- [ ] 成本核算策略接口
- [ ] FIFO 成本计算实现
- [ ] 加权平均成本计算实现
- [ ] 个别计价法实现
- [ ] 成本历史记录
- [ ] 毛利计算服务

**Files**:
- `backend/finance-service/src/main/java/com/company/finance/cost/`
  - `strategy/CostCalculationStrategy.java`
  - `strategy/FifoCostStrategy.java`
  - `strategy/WeightedAverageCostStrategy.java`
  - `strategy/SpecificIdentificationStrategy.java`
  - `service/CostCalculationService.java`
  - `service/impl/CostCalculationServiceImpl.java`
  - `entity/FinCostConfig.java`
  - `entity/FinCostHistory.java`
  - `entity/FinProfitCalc.java`

#### 4.2 进销存集成
- [ ] 事件消息定义
- [ ] 采购入库事件处理
- [ ] 销售出库事件处理
- [ ] 退货事件处理
- [ ] 库存调整事件处理
- [ ] 结算链路追溯

**Files**:
- `backend/finance-service/src/main/java/com/company/finance/integration/`
  - `event/PurchaseCompletedEvent.java`
  - `event/SalesCompletedEvent.java`
  - `listener/PurchaseEventListener.java`
  - `listener/SalesEventListener.java`
  - `service/SettlementService.java`

---

### Phase 5: 财务报表 (Week 9-10)

#### 5.1 报表生成引擎
- [ ] 资产负债表生成
- [ ] 利润表生成
- [ ] 现金流量表生成
- [ ] 毛利分析报表
- [ ] 报表数据缓存
- [ ] 报表锁定机制

**Files**:
- `backend/finance-service/src/main/java/com/company/finance/report/`
  - `controller/ReportController.java`
  - `service/ReportService.java`
  - `service/impl/ReportServiceImpl.java`
  - `generator/BalanceSheetGenerator.java`
  - `generator/IncomeStatementGenerator.java`
  - `generator/CashFlowGenerator.java`
  - `entity/FinReport.java`

#### 5.2 报表导出
- [ ] Excel 报表模板
- [ ] PDF 报表生成
- [ ] 多报表批量导出

**Files**:
- `backend/finance-service/src/main/resources/report-templates/`
  - `balance-sheet-template.xlsx`
  - `income-statement-template.xlsx`
  - `cash-flow-template.xlsx`
- `backend/finance-service/src/main/java/com/company/finance/report/export/`
  - `ExcelExporter.java`
  - `PdfExporter.java`

---

### Phase 6: 高级功能 (Week 11-12)

#### 6.1 银行对账
- [ ] 银行流水导入
- [ ] 自动对账匹配算法
- [ ] 手动对账功能
- [ ] 对账差异报告

**Files**:
- `backend/finance-service/src/main/java/com/company/finance/reconciliation/`
  - `controller/BankReconciliationController.java`
  - `service/BankReconciliationService.java`
  - `service/impl/AutoMatchingService.java`
  - `entity/FinBankRecord.java`

#### 6.2 预算管理
- [ ] 预算编制 API
- [ ] 预算审批流程 (Flowable)
- [ ] 预算执行跟踪
- [ ] 预算预警通知
- [ ] 预算控制拦截

**Files**:
- `backend/finance-service/src/main/java/com/company/finance/budget/`
  - `controller/BudgetController.java`
  - `service/BudgetService.java`
  - `service/impl/BudgetServiceImpl.java`
  - `interceptor/BudgetControlInterceptor.java`
  - `entity/FinBudget.java`

---

## Testing Strategy

### Unit Tests
- [ ] Service 层单元测试 (覆盖率 ≥ 80%)
- [ ] 成本计算策略测试
- [ ] 金额计算精度测试
- [ ] 状态机转换测试

### Integration Tests
- [ ] API 集成测试
- [ ] 事件处理集成测试
- [ ] 数据库事务测试

### Performance Tests
- [ ] 报表生成性能测试 (月度 ≤ 10s)
- [ ] 大数据量查询测试 (10万账单 ≤ 3s)
- [ ] 并发收付款测试

---

## Deployment

### Docker Configuration
```yaml
# docker-compose.yml
services:
  finance-service:
    build: ./backend/finance-service
    ports:
      - "8084:8084"
    environment:
      - SPRING_PROFILES_ACTIVE=prod
      - MYSQL_HOST=mysql
      - REDIS_HOST=redis
    depends_on:
      - mysql
      - redis
```

### Kubernetes
- [ ] Deployment 配置
- [ ] Service 配置
- [ ] ConfigMap/Secret
- [ ] HPA 自动扩缩容

---

## Dependencies

### Internal
- 001-user-module: 用户/租户信息
- 002-permission-module: 权限验证
- 004-gateway: API路由
- 005-erp-inventory: 进销存事件

### External
- MySQL 8.0+
- Redis 7.0+
- RocketMQ 5.0+ / Kafka 3.0+
- Flowable 7.0+

---

## Risk Assessment

| Risk | Probability | Impact | Mitigation |
|------|-------------|--------|------------|
| 金额计算精度问题 | Medium | High | 使用 BigDecimal，统一精度处理 |
| 与进销存集成数据不一致 | Medium | High | 事件幂等处理，定期对账 |
| 报表生成性能 | Medium | Medium | 预聚合数据，增量计算 |
| 多币种汇率波动 | Low | Medium | 汇率缓存，历史汇率存储 |

---

## Timeline

| Phase | Duration | Start | End |
|-------|----------|-------|-----|
| Phase 1: Core Infrastructure | 2 weeks | Week 1 | Week 2 |
| Phase 2: Account & Transaction | 2 weeks | Week 3 | Week 4 |
| Phase 3: Bill & Invoice | 2 weeks | Week 5 | Week 6 |
| Phase 4: Cost & Integration | 2 weeks | Week 7 | Week 8 |
| Phase 5: Reports | 2 weeks | Week 9 | Week 10 |
| Phase 6: Advanced Features | 2 weeks | Week 11 | Week 12 |

**Total Duration**: 12 weeks
