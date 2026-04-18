# Research: 财务模块

**Feature Branch**: `007-finance-module`
**Research Date**: 2026-04-07

## Technology Stack

### Core Technologies

| Component | Decision | Rationale | Alternatives Considered |
|-----------|----------|-----------|-------------------------|
| **Framework** | Spring Boot 3.0 + Spring Cloud | 与现有项目保持一致，微服务架构支持 | Quarkus, Micronaut |
| **ORM** | MyBatis-Plus | 与现有项目保持一致，复杂查询灵活 | JPA/Hibernate, JOOQ |
| **Database** | MySQL 8.0+ | 与现有项目保持一致，ACID事务支持 | PostgreSQL, Oracle |
| **Cache** | Redis 7.0+ | 高性能缓存，分布式锁支持 | Ehcache, Hazelcast |
| **Message Queue** | RocketMQ/Kafka | 异步事件处理，与进销存模块解耦 | RabbitMQ, ActiveMQ |

### Financial-Specific Technologies

| Component | Decision | Rationale | Alternatives Considered |
|-----------|----------|-----------|-------------------------|
| **报表引擎** | Jxls + Apache POI | Excel模板报表生成，符合财务习惯 | JasperReports, BIRT |
| **PDF生成** | iText / OpenPDF | 财务报表PDF导出 | Flying Saucer, PDFBox |
| **工作流引擎** | Flowable | 预算审批、账单审核流程 | Activiti, Camunda |
| **规则引擎** | Easy Rules / Drools | 成本核算规则、预算控制规则 | 自定义实现 |

## Design Patterns

### 1. 多币种处理

**Decision**: 使用本位币 + 原币双记录模式

**Rationale**:
- 支持多币种记账的同时保证报表统一
- 汇率变动时自动计算汇兑损益
- 符合会计准则要求

**Implementation**:
```java
public class MultiCurrencyAmount {
    private BigDecimal originalAmount;  // 原币金额
    private String originalCurrency;    // 原币代码
    private BigDecimal exchangeRate;    // 汇率
    private BigDecimal baseAmount;      // 本位币金额
}
```

### 2. 成本核算方法

**Decision**: 策略模式实现多种成本计算方法

**Rationale**:
- 支持FIFO、加权平均、个别计价法
- 按商品类别配置不同方法
- 易于扩展新的成本方法

**Implementation**:
```java
public interface CostCalculationStrategy {
    BigDecimal calculateCost(String productId, BigDecimal quantity);
}

public class FifoCostStrategy implements CostCalculationStrategy { ... }
public class WeightedAverageCostStrategy implements CostCalculationStrategy { ... }
public class SpecificIdentificationCostStrategy implements CostCalculationStrategy { ... }
```

### 3. 账单状态机

**Decision**: 状态机模式管理账单生命周期

**State Transitions**:
```
[创建] → 待审核 → 待收款/待付款 → 部分收款/部分付款 → 已完成
           ↓              ↓
        已拒绝          已取消
```

**Implementation**:
```java
public enum BillStatus {
    DRAFT,           // 草稿
    PENDING_APPROVAL,// 待审核
    APPROVED,        // 已审核/待收付
    PARTIAL_PAID,    // 部分收付
    COMPLETED,       // 已完成
    REJECTED,        // 已拒绝
    CANCELLED        // 已取消
}
```

### 4. 事件驱动集成

**Decision**: 使用事件驱动架构与进销存模块集成

**Rationale**:
- 松耦合设计，模块独立部署
- 异步处理提高性能
- 支持事件回放和审计

**Events**:
```java
// 进销存模块发布
PurchaseOrderCompletedEvent
SalesOrderCompletedEvent
PurchaseReturnCompletedEvent
SalesReturnCompletedEvent
InventoryAdjustmentEvent

// 财务模块监听并处理
@EventListener
public void handlePurchaseOrderCompleted(PurchaseOrderCompletedEvent event) {
    // 自动生成应付账单
}
```

## Security Considerations

### 1. 金额数据安全

**Decision**: 使用 BigDecimal 存储金额，加密存储敏感金额

**Rationale**:
- BigDecimal 避免浮点精度问题
- 敏感字段加密防止数据泄露

### 2. 审计日志

**Decision**: 使用 AOP + 数据库触发器双重审计

**Audit Fields**:
- 操作人、操作时间、操作类型
- 变更前值、变更后值
- IP地址、会话信息

### 3. 权限控制

**Decision**: 基于角色的数据权限控制

**Permission Levels**:
- 超级管理员: 全部财务数据
- 财务经理: 本租户财务数据
- 财务专员: 指定科目/账户数据
- 普通用户: 只读查看权限

## Performance Optimization

### 1. 报表生成优化

**Decision**: 预聚合 + 增量计算

**Strategy**:
- 日/月/年维度预聚合汇总表
- 增量更新而非全量计算
- 报表缓存机制

### 2. 大数据量查询

**Decision**: 分区表 + 索引优化

**Strategy**:
- 按租户ID分区
- 按时间范围索引
- 分页查询强制条件

## Integration Points

### 1. 与进销存模块集成

| 触发事件 | 财务动作 | 数据流向 |
|---------|---------|---------|
| 采购入库完成 | 生成应付账单 | ERP → Finance |
| 销售出库完成 | 生成应收账单 + 收入 + 成本 | ERP → Finance |
| 采购退货完成 | 生成红字应付 | ERP → Finance |
| 销售退货完成 | 生成红字应收 + 红字收入 | ERP → Finance |
| 库存盘点确认 | 生成盘盈盘亏记录 | ERP → Finance |

### 2. 汇率API集成

**Decision**: 缓存 + 定时更新

**Strategy**:
- 每日定时获取最新汇率
- Redis缓存当日汇率
- 历史汇率持久化存储

**Recommended APIs** (优先级排序):

| API | 免费额度 | 更新频率 | 备注 |
|-----|----------|----------|------|
| 中国银行官网汇率 | 免费 | 每日 | 国内首选，权威数据源 |
| ExchangeRate-API | 1500次/月 | 每日 | 备选方案，支持多币种 |
| Fixer.io | 100次/月 | 每日 | 欧洲汇率，备选 |

**Implementation**:
```java
@Service
public class ExchangeRateService {

    @Cacheable(value = "exchange-rates", key = "#date + '-' + #fromCurrency + '-' + #toCurrency")
    public BigDecimal getExchangeRate(LocalDate date, String fromCurrency, String toCurrency) {
        // 1. 先查本地历史汇率表
        // 2. 本地没有则调用外部API
        // 3. 缓存结果
    }

    @Scheduled(cron = "0 0 8 * * ?")  // 每日8点更新
    public void refreshDailyRates() {
        // 批量获取当日汇率并缓存
    }
}
```

**Fallback Strategy**:
- 如果外部API不可用，使用最近一日缓存汇率
- 支持手动录入汇率（财务人员操作）
- 记录汇率来源和获取时间

## Outstanding Questions

| # | Question | Status | Resolution |
|---|----------|--------|------------|
| 1 | 是否需要支持多会计准则（如IFRS、GAAP）？ | RESOLVED | 默认支持中国会计准则 |
| 2 | 银行对账单自动获取接口的具体银行？ | RESOLVED | 先支持手动导入，后续扩展API对接 |
| 3 | 汇率API具体选择哪个？ | RESOLVED | 优先中国银行官网，备选ExchangeRate-API |
| 3 | 财务报表是否需要CPA签名功能？ | NEEDS CLARIFICATION | 暂不实现，作为企业版功能 |

## References

- [Spring Boot 3.0 Documentation](https://spring.io/projects/spring-boot)
- [MyBatis-Plus Documentation](https://baomidou.com/)
- [Flowable Documentation](https://www.flowable.com/)
- [Jxls Documentation](http://jxls.sourceforge.net/)
