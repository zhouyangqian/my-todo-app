# Data Model: 财务模块

**Feature Branch**: `007-finance-module`
**Created**: 2026-04-07

## Entity Relationship Diagram

```
┌─────────────────┐     ┌─────────────────┐     ┌─────────────────┐
│   fin_account   │     │   fin_bill      │     │   fin_invoice   │
│   (财务账户)     │     │   (账单)        │     │   (发票)        │
├─────────────────┤     ├─────────────────┤     ├─────────────────┤
│ id              │     │ id              │     │ id              │
│ tenant_id       │     │ tenant_id       │     │ tenant_id       │
│ account_code    │     │ bill_no         │     │ invoice_no      │
│ account_name    │     │ bill_type       │     │ invoice_type    │
│ account_type    │◄────│ account_id      │────►│ invoice_status  │
│ currency        │     │ direction       │     │ invoice_date    │
│ balance         │     │ amount          │     │ amount          │
│ status          │     │ paid_amount     │     │ tax_rate        │
└─────────────────┘     │ status          │     │ tax_amount      │
                        │ source_type     │     │ source_bill_id  │
                        │ source_id       │     └────────┬────────┘
                        └────────┬────────┘              │
                                 │                       │
                                 │    ┌──────────────────┘
                                 │    │
                                 ▼    ▼
                        ┌─────────────────┐
                        │ fin_bill_invoice│
                        │ (账单发票关联)   │
                        ├─────────────────┤
                        │ id              │
                        │ bill_id         │
                        │ invoice_id      │
                        │ related_amount  │
                        └─────────────────┘

┌─────────────────┐     ┌─────────────────┐     ┌─────────────────┐
│ fin_transaction │     │ fin_cost_config │     │   fin_budget    │
│   (收支记录)     │     │  (成本配置)     │     │    (预算)       │
├─────────────────┤     ├─────────────────┤     ├─────────────────┤
│ id              │     │ id              │     │ id              │
│ tenant_id       │     │ tenant_id       │     │ tenant_id       │
│ trans_no        │     │ category_id     │     │ budget_name     │
│ trans_type      │     │ cost_method     │     │ budget_type     │
│ direction       │     │ category_code   │     │ department_id   │
│ account_id      │     └─────────────────┘     │ subject_code    │
│ amount          │                             │ budget_amount   │
│ currency        │     ┌─────────────────┐     │ used_amount     │
│ exchange_rate   │     │ fin_bank_record │     │ start_date      │
│ base_amount     │     │  (银行流水)      │     │ end_date        │
│ trans_date      │     ├─────────────────┤     │ control_level   │
│ source_type     │     │ id              │     │ status          │
│ source_id       │     │ tenant_id       │     └─────────────────┘
│ status          │     │ account_id      │
└─────────────────┘     │ statement_date  │
                        │ trans_date      │
┌─────────────────┐     │ amount          │     ┌─────────────────┐
│fin_payment_record│    │ balance         │     │  fin_report     │
│  (收付款记录)    │     │ description     │     │   (财务报表)    │
├─────────────────┤     │ matched         │     ├─────────────────┤
│ id              │     │ matched_trans_id│     │ id              │
│ tenant_id       │     └─────────────────┘     │ tenant_id       │
│ bill_id         │                             │ report_type     │
│ payment_type    │     ┌─────────────────┐     │ report_date     │
│ amount          │     │ fin_profit_calc │     │ period_type     │
│ payment_method  │     │  (毛利计算)     │     │ status          │
│ payment_account │     ├─────────────────┤     │ locked          │
│ payment_date    │     │ id              │     │ report_data     │
│ status          │     │ tenant_id       │     └─────────────────┘
└─────────────────┘     │ product_id      │
                        │ sale_id         │
┌─────────────────┐     │ quantity        │
│fin_cost_history │     │ unit_cost       │
│  (成本历史)     │     │ total_cost      │
├─────────────────┤     │ sale_amount     │
│ id              │     │ gross_profit    │
│ tenant_id       │     │ profit_rate     │
│ product_id      │     └─────────────────┘
│ cost_method     │
│ unit_cost       │
│ quantity        │
│ effective_date  │
│ source_type     │
│ source_id       │
└─────────────────┘
```

## Entity Definitions

### 1. fin_account (财务账户)

| Field | Type | Nullable | Description | Constraints |
|-------|------|----------|-------------|-------------|
| id | BIGINT | NO | 主键 | PK, Auto-increment |
| tenant_id | BIGINT | NO | 租户ID | FK → tenant.id |
| account_code | VARCHAR(32) | NO | 账户编码 | Unique per tenant |
| account_name | VARCHAR(100) | NO | 账户名称 | |
| account_type | TINYINT | NO | 账户类型 | 1=银行账户, 2=现金账户, 3=支付宝, 4=微信, 5=其他 |
| currency | VARCHAR(10) | NO | 币种 | Default: CNY |
| balance | DECIMAL(18,2) | NO | 余额 | Default: 0 |
| status | TINYINT | NO | 状态 | 0=禁用, 1=启用 |
| created_by | BIGINT | NO | 创建人 | |
| created_at | DATETIME | NO | 创建时间 | |
| updated_by | BIGINT | YES | 更新人 | |
| updated_at | DATETIME | YES | 更新时间 | |
| deleted | TINYINT | NO | 删除标记 | 0=未删除, 1=已删除 |

**Indexes**:
- `idx_tenant_id` (tenant_id)
- `uk_tenant_code` (tenant_id, account_code)

---

### 2. fin_bill (账单)

| Field | Type | Nullable | Description | Constraints |
|-------|------|----------|-------------|-------------|
| id | BIGINT | NO | 主键 | PK |
| tenant_id | BIGINT | NO | 租户ID | FK → tenant.id |
| bill_no | VARCHAR(32) | NO | 账单编号 | Unique per tenant |
| bill_type | TINYINT | NO | 账单类型 | 1=应收, 2=应付 |
| direction | TINYINT | NO | 方向 | 1=正常, -1=红字(冲销) |
| partner_type | TINYINT | NO | 往来方类型 | 1=客户, 2=供应商 |
| partner_id | BIGINT | NO | 往来方ID | |
| partner_name | VARCHAR(100) | NO | 往来方名称 | 冗余存储 |
| account_id | BIGINT | YES | 关联账户 | FK → fin_account.id |
| amount | DECIMAL(18,2) | NO | 账单金额 | > 0 |
| paid_amount | DECIMAL(18,2) | NO | 已收/付金额 | Default: 0 |
| currency | VARCHAR(10) | NO | 币种 | |
| exchange_rate | DECIMAL(10,6) | NO | 汇率 | Default: 1 |
| base_amount | DECIMAL(18,2) | NO | 本位币金额 | |
| bill_date | DATE | NO | 账单日期 | |
| due_date | DATE | YES | 到期日 | |
| status | TINYINT | NO | 状态 | 见状态枚举 |
| source_type | VARCHAR(50) | YES | 来源类型 | PURCHASE/SALE/MANUAL |
| source_id | BIGINT | YES | 来源ID | |
| source_no | VARCHAR(32) | YES | 来源单号 | |
| remark | VARCHAR(500) | YES | 备注 | |
| audit_status | TINYINT | NO | 审核状态 | 0=待审核, 1=已通过, 2=已拒绝 |
| audit_by | BIGINT | YES | 审核人 | |
| audit_at | DATETIME | YES | 审核时间 | |
| created_by | BIGINT | NO | 创建人 | |
| created_at | DATETIME | NO | 创建时间 | |
| updated_at | DATETIME | YES | 更新时间 | |
| deleted | TINYINT | NO | 删除标记 | |

**Status Enum**:
- 0: DRAFT (草稿)
- 1: PENDING_APPROVAL (待审核)
- 2: APPROVED (已审核)
- 3: PARTIAL_PAID (部分收付)
- 4: COMPLETED (已完成)
- 5: CANCELLED (已取消)

**Indexes**:
- `idx_tenant_id` (tenant_id)
- `idx_bill_date` (bill_date)
- `idx_partner` (partner_type, partner_id)
- `idx_status` (status)
- `idx_source` (source_type, source_id)

---

### 3. fin_payment_record (收付款记录)

| Field | Type | Nullable | Description | Constraints |
|-------|------|----------|-------------|-------------|
| id | BIGINT | NO | 主键 | PK |
| tenant_id | BIGINT | NO | 租户ID | |
| bill_id | BIGINT | NO | 关联账单 | FK → fin_bill.id |
| payment_no | VARCHAR(32) | NO | 收付款单号 | |
| payment_type | TINYINT | NO | 类型 | 1=收款, 2=付款 |
| amount | DECIMAL(18,2) | NO | 金额 | > 0 |
| currency | VARCHAR(10) | NO | 币种 | |
| payment_method | TINYINT | NO | 支付方式 | 1=银行转账, 2=现金, 3=支票, 4=支付宝, 5=微信 |
| payment_account | VARCHAR(100) | YES | 支付账户 | |
| from_account_id | BIGINT | YES | 付款账户 | FK → fin_account.id |
| to_account_id | BIGINT | YES | 收款账户 | FK → fin_account.id |
| payment_date | DATE | NO | 收付款日期 | |
| voucher_no | VARCHAR(50) | YES | 凭证号 | |
| status | TINYINT | NO | 状态 | 0=待确认, 1=已确认, 2=已取消 |
| remark | VARCHAR(500) | YES | 备注 | |
| created_by | BIGINT | NO | 创建人 | |
| created_at | DATETIME | NO | 创建时间 | |
| deleted | TINYINT | NO | 删除标记 | |

**Indexes**:
- `idx_tenant_bill` (tenant_id, bill_id)
- `idx_payment_date` (payment_date)

---

### 4. fin_transaction (收支记录)

| Field | Type | Nullable | Description | Constraints |
|-------|------|----------|-------------|-------------|
| id | BIGINT | NO | 主键 | PK |
| tenant_id | BIGINT | NO | 租户ID | |
| trans_no | VARCHAR(32) | NO | 交易编号 | Unique per tenant |
| trans_type | VARCHAR(50) | NO | 交易类型 | INCOME/EXPENSE/TRANSFER |
| category_code | VARCHAR(50) | NO | 科目代码 | |
| category_name | VARCHAR(100) | NO | 科目名称 | |
| direction | TINYINT | NO | 方向 | 1=收入, -1=支出 |
| account_id | BIGINT | NO | 账户 | FK → fin_account.id |
| amount | DECIMAL(18,2) | NO | 金额 | > 0 |
| currency | VARCHAR(10) | NO | 币种 | |
| exchange_rate | DECIMAL(10,6) | NO | 汇率 | |
| base_amount | DECIMAL(18,2) | NO | 本位币金额 | |
| trans_date | DATE | NO | 交易日期 | |
| source_type | VARCHAR(50) | YES | 来源类型 | BILL/MANUAL/ADJUSTMENT |
| source_id | BIGINT | YES | 来源ID | |
| related_trans_id | BIGINT | YES | 关联交易ID | 用于转账关联 |
| status | TINYINT | NO | 状态 | 0=待确认, 1=已确认, 2=已取消 |
| remark | VARCHAR(500) | YES | 备注 | |
| created_by | BIGINT | NO | 创建人 | |
| created_at | DATETIME | NO | 创建时间 | |
| deleted | TINYINT | NO | 删除标记 | |

**Indexes**:
- `idx_tenant_date` (tenant_id, trans_date)
- `idx_account` (account_id)
- `idx_category` (category_code)

---

### 5. fin_invoice (发票)

| Field | Type | Nullable | Description | Constraints |
|-------|------|----------|-------------|-------------|
| id | BIGINT | NO | 主键 | PK |
| tenant_id | BIGINT | NO | 租户ID | |
| invoice_no | VARCHAR(50) | NO | 发票号码 | Unique per tenant |
| invoice_type | TINYINT | NO | 发票类型 | 1=销售发票, 2=采购发票 |
| invoice_code | VARCHAR(20) | YES | 发票代码 | |
| invoice_status | TINYINT | NO | 状态 | 0=待开票, 1=已开票, 2=已作废 |
| invoice_date | DATE | NO | 开票日期 | |
| partner_type | TINYINT | NO | 往来方类型 | 1=客户, 2=供应商 |
| partner_id | BIGINT | NO | 往来方ID | |
| partner_name | VARCHAR(100) | NO | 往来方名称 | |
| amount | DECIMAL(18,2) | NO | 金额(不含税) | |
| tax_rate | DECIMAL(5,2) | NO | 税率(%) | |
| tax_amount | DECIMAL(18,2) | NO | 税额 | |
| total_amount | DECIMAL(18,2) | NO | 价税合计 | |
| currency | VARCHAR(10) | NO | 币种 | |
| remark | VARCHAR(500) | YES | 备注 | |
| void_reason | VARCHAR(200) | YES | 作废原因 | |
| void_by | BIGINT | YES | 作废人 | |
| void_at | DATETIME | YES | 作废时间 | |
| created_by | BIGINT | NO | 创建人 | |
| created_at | DATETIME | NO | 创建时间 | |
| deleted | TINYINT | NO | 删除标记 | |

**Indexes**:
- `idx_tenant_no` (tenant_id, invoice_no)
- `idx_invoice_date` (invoice_date)
- `idx_partner` (partner_type, partner_id)

---

### 6. fin_bill_invoice (账单发票关联)

| Field | Type | Nullable | Description | Constraints |
|-------|------|----------|-------------|-------------|
| id | BIGINT | NO | 主键 | PK |
| tenant_id | BIGINT | NO | 租户ID | |
| bill_id | BIGINT | NO | 账单ID | FK → fin_bill.id |
| invoice_id | BIGINT | NO | 发票ID | FK → fin_invoice.id |
| related_amount | DECIMAL(18,2) | NO | 关联金额 | |
| created_at | DATETIME | NO | 创建时间 | |

**Indexes**:
- `uk_bill_invoice` (bill_id, invoice_id)

---

### 7. fin_cost_config (成本核算配置)

| Field | Type | Nullable | Description | Constraints |
|-------|------|----------|-------------|-------------|
| id | BIGINT | NO | 主键 | PK |
| tenant_id | BIGINT | NO | 租户ID | |
| category_id | BIGINT | NO | 商品类别ID | |
| category_code | VARCHAR(50) | NO | 类别编码 | |
| cost_method | TINYINT | NO | 成本方法 | 1=FIFO, 2=加权平均, 3=个别计价 |
| status | TINYINT | NO | 状态 | 0=禁用, 1=启用 |
| created_by | BIGINT | NO | 创建人 | |
| created_at | DATETIME | NO | 创建时间 | |
| updated_at | DATETIME | YES | 更新时间 | |

---

### 8. fin_cost_history (成本历史)

| Field | Type | Nullable | Description | Constraints |
|-------|------|----------|-------------|-------------|
| id | BIGINT | NO | 主键 | PK |
| tenant_id | BIGINT | NO | 租户ID | |
| product_id | BIGINT | NO | 商品ID | |
| product_code | VARCHAR(50) | NO | 商品编码 | |
| warehouse_id | BIGINT | NO | 仓库ID | |
| cost_method | TINYINT | NO | 成本方法 | |
| unit_cost | DECIMAL(18,4) | NO | 单位成本 | |
| quantity | DECIMAL(18,4) | NO | 数量 | |
| total_cost | DECIMAL(18,2) | NO | 总成本 | |
| effective_date | DATETIME | NO | 生效时间 | |
| source_type | VARCHAR(50) | NO | 来源类型 | PURCHASE/ADJUSTMENT/RETURN |
| source_id | BIGINT | NO | 来源ID | |
| created_at | DATETIME | NO | 创建时间 | |

**Indexes**:
- `idx_product_date` (product_id, effective_date)
- `idx_source` (source_type, source_id)

---

### 9. fin_profit_calc (毛利计算记录)

| Field | Type | Nullable | Description | Constraints |
|-------|------|----------|-------------|-------------|
| id | BIGINT | NO | 主键 | PK |
| tenant_id | BIGINT | NO | 租户ID | |
| sale_id | BIGINT | NO | 销售单ID | |
| sale_no | VARCHAR(32) | NO | 销售单号 | |
| product_id | BIGINT | NO | 商品ID | |
| product_code | VARCHAR(50) | NO | 商品编码 | |
| product_name | VARCHAR(200) | NO | 商品名称 | |
| quantity | DECIMAL(18,4) | NO | 销售数量 | |
| unit_price | DECIMAL(18,4) | NO | 销售单价 | |
| sale_amount | DECIMAL(18,2) | NO | 销售金额 | |
| unit_cost | DECIMAL(18,4) | NO | 单位成本 | |
| total_cost | DECIMAL(18,2) | NO | 销售成本 | |
| gross_profit | DECIMAL(18,2) | NO | 毛利 | |
| profit_rate | DECIMAL(10,4) | NO | 毛利率(%) | |
| cost_method | TINYINT | NO | 成本方法 | |
| calc_date | DATE | NO | 计算日期 | |
| created_at | DATETIME | NO | 创建时间 | |

**Indexes**:
- `idx_tenant_date` (tenant_id, calc_date)
- `idx_sale` (sale_id)
- `idx_product` (product_id)

---

### 10. fin_bank_record (银行流水)

| Field | Type | Nullable | Description | Constraints |
|-------|------|----------|-------------|-------------|
| id | BIGINT | NO | 主键 | PK |
| tenant_id | BIGINT | NO | 租户ID | |
| account_id | BIGINT | NO | 银行账户ID | FK → fin_account.id |
| statement_no | VARCHAR(50) | NO | 流水号 | |
| statement_date | DATE | NO | 对账单日期 | |
| trans_date | DATE | NO | 交易日期 | |
| trans_time | TIME | YES | 交易时间 | |
| amount | DECIMAL(18,2) | NO | 交易金额 | 正=收入, 负=支出 |
| balance | DECIMAL(18,2) | NO | 账户余额 | |
| direction | TINYINT | NO | 方向 | 1=收入, -1=支出 |
| description | VARCHAR(500) | YES | 摘要 | |
| counterparty | VARCHAR(200) | YES | 对方户名 | |
| counterparty_account | VARCHAR(50) | YES | 对方账号 | |
| matched | TINYINT | NO | 匹配状态 | 0=未匹配, 1=已匹配 |
| matched_trans_id | BIGINT | YES | 匹配的交易ID | FK → fin_transaction.id |
| match_type | TINYINT | YES | 匹配类型 | 1=自动, 2=手动 |
| match_by | BIGINT | YES | 匹配人 | |
| match_at | DATETIME | YES | 匹配时间 | |
| exception_flag | TINYINT | NO | 异常标记 | 0=正常, 1=异常 |
| exception_reason | VARCHAR(200) | YES | 异常原因 | |
| created_at | DATETIME | NO | 创建时间 | |

**Indexes**:
- `idx_tenant_account_date` (tenant_id, account_id, statement_date)
- `idx_matched` (matched)

---

### 11. fin_budget (预算)

| Field | Type | Nullable | Description | Constraints |
|-------|------|----------|-------------|-------------|
| id | BIGINT | NO | 主键 | PK |
| tenant_id | BIGINT | NO | 租户ID | |
| budget_no | VARCHAR(32) | NO | 预算编号 | |
| budget_name | VARCHAR(100) | NO | 预算名称 | |
| budget_type | TINYINT | NO | 预算类型 | 1=收入预算, 2=支出预算 |
| department_id | BIGINT | YES | 部门ID | |
| department_name | VARCHAR(100) | YES | 部门名称 | |
| subject_code | VARCHAR(50) | NO | 科目代码 | |
| subject_name | VARCHAR(100) | NO | 科目名称 | |
| budget_amount | DECIMAL(18,2) | NO | 预算金额 | |
| used_amount | DECIMAL(18,2) | NO | 已使用金额 | Default: 0 |
| frozen_amount | DECIMAL(18,2) | NO | 冻结金额 | Default: 0 |
| start_date | DATE | NO | 开始日期 | |
| end_date | DATE | NO | 结束日期 | |
| control_level | TINYINT | NO | 控制级别 | 1=强制, 2=提醒, 3=记录 |
| warn_threshold | DECIMAL(5,2) | NO | 预警阈值(%) | Default: 80 |
| status | TINYINT | NO | 状态 | 0=草稿, 1=待审批, 2=执行中, 3=已结束 |
| parent_id | BIGINT | YES | 父预算ID | 用于预算层级 |
| created_by | BIGINT | NO | 创建人 | |
| created_at | DATETIME | NO | 创建时间 | |
| approved_by | BIGINT | YES | 审批人 | |
| approved_at | DATETIME | YES | 审批时间 | |

**Indexes**:
- `idx_tenant_date` (tenant_id, start_date, end_date)
- `idx_department` (department_id)

---

### 12. fin_report (财务报表)

| Field | Type | Nullable | Description | Constraints |
|-------|------|----------|-------------|-------------|
| id | BIGINT | NO | 主键 | PK |
| tenant_id | BIGINT | NO | 租户ID | |
| report_type | TINYINT | NO | 报表类型 | 1=资产负债表, 2=利润表, 3=现金流量表 |
| report_name | VARCHAR(100) | NO | 报表名称 | |
| report_date | DATE | NO | 报表日期 | |
| period_type | TINYINT | NO | 周期类型 | 1=月, 2=季, 3=年 |
| period_start | DATE | NO | 周期开始 | |
| period_end | DATE | NO | 周期结束 | |
| currency | VARCHAR(10) | NO | 币种 | |
| status | TINYINT | NO | 状态 | 0=生成中, 1=已完成, 2=失败 |
| locked | TINYINT | NO | 锁定标记 | 0=未锁定, 1=已锁定 |
| report_data | JSON | NO | 报表数据(JSON) | |
| version | INT | NO | 版本号 | Default: 1 |
| created_by | BIGINT | NO | 创建人 | |
| created_at | DATETIME | NO | 创建时间 | |
| locked_by | BIGINT | YES | 锁定人 | |
| locked_at | DATETIME | YES | 锁定时间 | |

**Indexes**:
- `idx_tenant_type_date` (tenant_id, report_type, report_date)
- `idx_period` (period_start, period_end)

---

## Data Migration Notes

1. **初始化财务账户**: 每个租户需要至少创建一个默认财务账户
2. **成本配置初始化**: 需要为现有商品类别设置默认成本核算方法
3. **汇率数据**: 需要导入历史汇率数据用于多币种转换
4. **科目体系**: 需要预置标准会计科目表

## Data Volume Estimates

| Entity | Est. Records/Tenant/Month | Growth Rate |
|--------|---------------------------|-------------|
| fin_bill | 500-2000 | Linear |
| fin_transaction | 1000-5000 | Linear |
| fin_payment_record | 300-1000 | Linear |
| fin_invoice | 200-800 | Linear |
| fin_profit_calc | 1000-3000 | Linear |
| fin_bank_record | 500-2000 | Linear |
| fin_cost_history | 500-2000 | Linear |
