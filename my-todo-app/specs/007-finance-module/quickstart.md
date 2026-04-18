# Quick Start: 财务模块

本指南帮助你快速理解和开始开发财务模块。

## 模块概述

财务模块提供完整的财务管理能力，包括：
- 应收应付账单管理
- 收支记录管理
- 发票管理
- 成本核算与毛利计算
- 银行对账
- 预算管理
- 财务报表生成
- 与进销存模块集成

## 核心概念

### 1. 财务账户 (Account)
资金存放的载体，支持多种账户类型：
- 银行账户
- 现金账户
- 支付宝/微信等第三方账户

### 2. 账单 (Bill)
应收/应付的债权债务凭证：
- **应收账单**: 客户欠款
- **应付账单**: 欠供应商款项

### 3. 收支记录 (Transaction)
实际的资金流入流出记录

### 4. 发票 (Invoice)
税务凭证，与账单多对多关联

### 5. 成本核算 (Cost Calculation)
- **FIFO**: 先进先出法
- **加权平均**: 移动加权平均
- **个别计价**: 按批次指定成本

## API 快速参考

### 账户管理
```
GET    /api/v1/finance/accounts          # 账户列表
POST   /api/v1/finance/accounts          # 创建账户
GET    /api/v1/finance/accounts/{id}     # 账户详情
PUT    /api/v1/finance/accounts/{id}     # 更新账户
GET    /api/v1/finance/accounts/{id}/balance  # 账户余额
```

### 账单管理
```
GET    /api/v1/finance/bills             # 账单列表
POST   /api/v1/finance/bills             # 创建账单
GET    /api/v1/finance/bills/{id}        # 账单详情
POST   /api/v1/finance/bills/{id}/approve  # 审核账单
POST   /api/v1/finance/bills/{id}/cancel   # 取消账单
GET    /api/v1/finance/bills/summary     # 账单汇总
```

### 收支记录
```
GET    /api/v1/finance/transactions      # 收支列表
POST   /api/v1/finance/transactions      # 创建收支
PUT    /api/v1/finance/transactions/{id} # 更新收支
DELETE /api/v1/finance/transactions/{id} # 删除收支
POST   /api/v1/finance/transfers         # 账户转账
GET    /api/v1/finance/transactions/summary  # 收支汇总
```

### 发票管理
```
GET    /api/v1/finance/invoices          # 发票列表
POST   /api/v1/finance/invoices          # 创建发票
GET    /api/v1/finance/invoices/{id}     # 发票详情
POST   /api/v1/finance/invoices/{id}/void    # 作废发票
POST   /api/v1/finance/invoices/{id}/bills/link   # 关联账单
```

### 财务报表
```
GET    /api/v1/finance/reports/balance-sheet      # 资产负债表
GET    /api/v1/finance/reports/income-statement   # 利润表
GET    /api/v1/finance/reports/cash-flow-statement # 现金流量表
GET    /api/v1/finance/reports/profit-analysis    # 毛利分析
POST   /api/v1/finance/reports/export             # 导出报表
```

## 开发环境设置

### 前置条件
- JDK 17+
- Maven 3.8+
- MySQL 8.0+
- Redis 7.0+
- IDE (IntelliJ IDEA 推荐)

### 本地启动

```bash
# 1. 克隆项目
cd my-todo-app

# 2. 创建数据库
mysql -u root -p -e "CREATE DATABASE finance_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"

# 3. 执行数据库迁移
mysql -u root -p finance_db < backend/finance-service/src/main/resources/db/migration/V1__finance_schema.sql

# 4. 配置 application.yml
# 修改数据库连接、Redis连接等配置

# 5. 启动服务
cd backend/finance-service
mvn spring-boot:run
```

### 验证安装

```bash
# 健康检查
curl http://localhost:8084/actuator/health

# 创建测试账户
curl -X POST http://localhost:8084/api/v1/finance/accounts \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer {token}" \
  -d '{
    "accountCode": "BANK001",
    "accountName": "基本户",
    "accountType": 1,
    "currency": "CNY"
  }'
```

## 数据模型速览

```
┌──────────────┐     ┌──────────────┐     ┌──────────────┐
│ fin_account  │     │   fin_bill   │     │ fin_invoice  │
├──────────────┤     ├──────────────┤     ├──────────────┤
│ id           │     │ id           │     │ id           │
│ tenant_id    │     │ tenant_id    │     │ tenant_id    │
│ account_code │     │ bill_no      │     │ invoice_no   │
│ account_name │     │ bill_type    │     │ invoice_type │
│ account_type │     │ amount       │     │ amount       │
│ balance      │     │ paid_amount  │     │ tax_amount   │
└──────────────┘     │ status       │     └──────────────┘
                     └──────────────┘
                           │
                           ▼
                     ┌──────────────┐
                     │fin_payment_  │
                     │   record     │
                     ├──────────────┤
                     │ bill_id      │
                     │ amount       │
                     │ payment_date │
                     └──────────────┘
```

## 与进销存集成

### 事件流程

```
销售出库完成 ──────────────► 生成应收账单
     │                           │
     │                           ▼
     │                    计算销售成本
     │                           │
     │                           ▼
     └───────────────────► 记录毛利
```

### 事件定义

```java
// 销售完成事件
public class SalesCompletedEvent {
    private Long tenantId;
    private Long saleId;
    private String saleNo;
    private Long customerId;
    private BigDecimal amount;
    private List<SaleItem> items;
}

// 采购完成事件
public class PurchaseCompletedEvent {
    private Long tenantId;
    private Long purchaseId;
    private String purchaseNo;
    private Long supplierId;
    private BigDecimal amount;
    private List<PurchaseItem> items;
}
```

## 常见开发任务

### 1. 添加新的成本计算方法

```java
@Component
public class MyCostStrategy implements CostCalculationStrategy {

    @Override
    public BigDecimal calculateCost(String productId, BigDecimal quantity) {
        // 实现你的成本计算逻辑
    }

    @Override
    public String getMethod() {
        return "MY_METHOD";
    }
}
```

### 2. 添加新的报表类型

```java
@Component
public class MyReportGenerator implements ReportGenerator {

    @Override
    public ReportData generate(ReportRequest request) {
        // 实现报表生成逻辑
    }

    @Override
    public Integer getReportType() {
        return 5; // 新报表类型
    }
}
```

### 3. 监听新的业务事件

```java
@Component
public class MyEventListener {

    @EventListener
    public void handle(MyBusinessEvent event) {
        // 处理事件，生成财务数据
    }
}
```

## 测试指南

### 单元测试示例

```java
@SpringBootTest
class BillServiceTest {

    @Autowired
    private BillService billService;

    @Test
    void shouldCreateBillSuccessfully() {
        BillCreateRequest request = new BillCreateRequest();
        request.setBillType(1); // 应收
        request.setAmount(new BigDecimal("1000.00"));
        request.setPartnerId(1L);

        BillResponse response = billService.createBill(request);

        assertNotNull(response.getId());
        assertEquals(BillStatus.PENDING_APPROVAL, response.getStatus());
    }
}
```

### 集成测试示例

```java
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class BillControllerIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void shouldReturnBillList() {
        ResponseEntity<BillPageResponse> response =
            restTemplate.exchange(
                "/api/v1/finance/bills",
                HttpMethod.GET,
                null,
                BillPageResponse.class
            );

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }
}
```

## 故障排查

### 常见问题

1. **金额计算精度问题**
   - 确保使用 `BigDecimal`
   - 使用 `RoundingMode.HALF_UP` 舍入

2. **账户余额不一致**
   - 检查事务是否正确提交
   - 使用分布式锁防止并发

3. **报表数据不准确**
   - 检查数据聚合逻辑
   - 验证科目映射配置

## 相关文档

- [数据模型](./data-model.md)
- [API契约](./contracts/)
- [研究文档](./research.md)
- [实现计划](./plan.md)
