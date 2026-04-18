# Data Model: 进销存模块 (005-erp-inventory)

**Feature**: ERP 进销存管理系统
**Date**: 2026-01-10
**Purpose**: 数据库设计和实体关系

## 概述

本文档定义进销存模块的数据库表结构、实体关系和验证规则。系统包含 40+ 个核心实体，涵盖采购、销售、仓库、结算、商品等业务领域。

## 实体关系图 (ERD)

```
┌─────────────────────────────────────────────────────────────────────────────────┐
│                              进销存系统实体关系图                                 │
└─────────────────────────────────────────────────────────────────────────────────┘

┌─────────────┐         ┌─────────────┐         ┌─────────────┐
│  Supplier   │────────▶│PurchaseOrder│────────▶│PurchaseRcpt │
│  (供应商)    │         │  (采购订单)  │         │  (采购入库)  │
└─────────────┘         └─────────────┘         └─────────────┘
       │                       │                       │
       │                       │                       ▼
       │                       │              ┌─────────────┐
       │                       │              │  Inventory  │
       │                       │              │   (库存)     │
       │                       │              └─────────────┘
       │                       │                       ▲
       │                       │                       │
       ▼                       ▼                       │
┌─────────────┐         ┌─────────────┐              │
│  Customer   │────────▶│ SalesOrder  │──────────────┘
│  (客户)      │         │  (销售订单)  │
└─────────────┘         └─────────────┘
                              │
                              ▼
                       ┌─────────────┐
                       │SalesDelivery│
                       │  (销售出库)  │
                       └─────────────┘

┌─────────────┐         ┌─────────────┐         ┌─────────────┐
│ ProductCategory│─────▶│  Product   │────────▶│ProductPrice │
│   (商品分类)   │         │   (商品)    │         │  (商品价格)  │
└─────────────┘         └─────────────┘         └─────────────┘
                                                       │
                                                       ▼
                                                ┌─────────────┐
                                                │ProductProm  │
                                                │  (商品促销)  │
                                                └─────────────┘

┌─────────────┐         ┌─────────────┐         ┌─────────────┐
│PaymentAcct  │         │ReceiptAcct  │         │   Invoice   │
│  (应付)      │         │  (应收)      │         │  (发票)      │
└─────────────┘         └─────────────┘         └─────────────┘
```

## 数据库表设计

### 一、供应商相关

#### 1.1 supplier (供应商表)

供应商主数据表，存储供应商基本信息。

| 字段名 | 类型 | 长度 | 允许NULL | 默认值 | 约束 | 说明 |
|--------|------|------|----------|--------|------|------|
| id | BIGINT | - | NO | - | PK, AUTO_INCREMENT | 供应商 ID |
| tenant_id | BIGINT | - | NO | - | INDEX | 租户 ID |
| code | VARCHAR | 50 | NO | - | UNIQUE | 供应商编码 |
| name | VARCHAR | 200 | NO | - | - | 供应商名称 |
| contact | VARCHAR | 50 | YES | NULL | - | 联系人 |
| phone | VARCHAR | 20 | YES | NULL | - | 联系电话 |
| email | VARCHAR | 100 | YES | NULL | - | 邮箱 |
| address | VARCHAR | 500 | YES | NULL | - | 地址 |
| credit_limit | DECIMAL | (15,2) | NO | 0 | - | 信用额度 |
| current_debt | DECIMAL | (15,2) | NO | 0 | - | 当前欠款 |
| status | TINYINT | - | NO | 1 | - | 状态 (0=禁用, 1=启用) |
| remark | TEXT | - | YES | NULL | - | 备注 |
| created_at | DATETIME | - | NO | CURRENT_TIMESTAMP | - | 创建时间 |
| updated_at | DATETIME | - | NO | CURRENT_TIMESTAMP | ON UPDATE CURRENT_TIMESTAMP | 更新时间 |
| created_by | BIGINT | - | YES | NULL | - | 创建人 ID |

**索引**:
- PRIMARY KEY: `id`
- UNIQUE KEY: `uk_tenant_code` (`tenant_id`, `code`)
- INDEX: `idx_tenant_status` (`tenant_id`, `status`)

#### 1.2 supplier_contact (供应商联系人表)

供应商的多个联系人。

| 字段名 | 类型 | 长度 | 允许NULL | 默认值 | 约束 | 说明 |
|--------|------|------|----------|--------|------|------|
| id | BIGINT | - | NO | - | PK, AUTO_INCREMENT | 联系人 ID |
| tenant_id | BIGINT | - | NO | - | INDEX | 租户 ID |
| supplier_id | BIGINT | - | NO | - | INDEX | 供应商 ID |
| name | VARCHAR | 50 | NO | - | - | 姓名 |
| position | VARCHAR | 50 | YES | NULL | - | 职位 |
| phone | VARCHAR | 20 | YES | NULL | - | 电话 |
| email | VARCHAR | 100 | YES | NULL | - | 邮箱 |
| remark | VARCHAR | 200 | YES | NULL | - | 备注 |
| created_at | DATETIME | - | NO | CURRENT_TIMESTAMP | - | 创建时间 |

**索引**:
- PRIMARY KEY: `id`
- INDEX: `idx_supplier_id` (`supplier_id`)

---

### 二、客户相关

#### 2.1 customer (客户表)

客户主数据表。

| 字段名 | 类型 | 长度 | 允许NULL | 默认值 | 约束 | 说明 |
|--------|------|------|----------|--------|------|------|
| id | BIGINT | - | NO | - | PK, AUTO_INCREMENT | 客户 ID |
| tenant_id | BIGINT | - | NO | - | INDEX | 租户 ID |
| code | VARCHAR | 50 | NO | - | UNIQUE | 客户编码 |
| name | VARCHAR | 200 | NO | - | - | 客户名称 |
| type | VARCHAR | 20 | NO | - | - | 客户类型 |
| contact | VARCHAR | 50 | YES | NULL | - | 联系人 |
| phone | VARCHAR | 20 | YES | NULL | - | 联系电话 |
| email | VARCHAR | 100 | YES | NULL | - | 邮箱 |
| address | VARCHAR | 500 | YES | NULL | - | 地址 |
| credit_limit | DECIMAL | (15,2) | NO | 0 | - | 信用额度 |
| current_debt | DECIMAL | (15,2) | NO | 0 | - | 当前欠款 |
| status | TINYINT | - | NO | 1 | - | 状态 |
| remark | TEXT | - | YES | NULL | - | 备注 |
| created_at | DATETIME | - | NO | CURRENT_TIMESTAMP | - | 创建时间 |
| updated_at | DATETIME | - | NO | CURRENT_TIMESTAMP | ON UPDATE CURRENT_TIMESTAMP | 更新时间 |
| created_by | BIGINT | - | YES | NULL | - | 创建人 ID |

**索引**:
- PRIMARY KEY: `id`
- UNIQUE KEY: `uk_tenant_code` (`tenant_id`, `code`)
- INDEX: `idx_tenant_status` (`tenant_id`, `status`)

#### 2.2 customer_contact (客户联系人表)

同供应商联系人结构。

---

### 三、商品相关

#### 3.1 product_category (商品分类表)

商品分类，支持多级分类。

| 字段名 | 类型 | 长度 | 允许NULL | 默认值 | 约束 | 说明 |
|--------|------|------|----------|--------|------|------|
| id | BIGINT | - | NO | - | PK, AUTO_INCREMENT | 分类 ID |
| tenant_id | BIGINT | - | NO | - | INDEX | 租户 ID |
| parent_id | BIGINT | - | YES | NULL | INDEX | 上级分类 ID |
| name | VARCHAR | 100 | NO | - | - | 分类名称 |
| sort_order | INT | - | NO | 0 | - | 排序 |
| status | TINYINT | - | NO | 1 | - | 状态 |
| created_at | DATETIME | - | NO | CURRENT_TIMESTAMP | - | 创建时间 |

**索引**:
- PRIMARY KEY: `id`
- INDEX: `idx_parent_id` (`parent_id`)

#### 3.2 product (商品表)

商品主数据表。

| 字段名 | 类型 | 长度 | 允许NULL | 默认值 | 约束 | 说明 |
|--------|------|------|----------|--------|------|------|
| id | BIGINT | - | NO | - | PK, AUTO_INCREMENT | 商品 ID |
| tenant_id | BIGINT | - | NO | - | INDEX | 租户 ID |
| code | VARCHAR | 50 | NO | - | UNIQUE | 商品编码 |
| name | VARCHAR | 200 | NO | - | - | 商品名称 |
| category_id | BIGINT | - | YES | NULL | INDEX | 分类 ID |
| specification | VARCHAR | 200 | YES | NULL | - | 规格 |
| unit | VARCHAR | 20 | NO | - | - | 单位 |
| reference_price | DECIMAL | (15,2) | YES | NULL | - | 参考价格 |
| status | TINYINT | - | NO | 1 | - | 状态 |
| created_at | DATETIME | - | NO | CURRENT_TIMESTAMP | - | 创建时间 |
| updated_at | DATETIME | - | NO | CURRENT_TIMESTAMP | ON UPDATE CURRENT_TIMESTAMP | 更新时间 |
| created_by | BIGINT | - | YES | NULL | - | 创建人 ID |

**索引**:
- PRIMARY KEY: `id`
- UNIQUE KEY: `uk_tenant_code` (`tenant_id`, `code`)
- INDEX: `idx_category` (`category_id`)

#### 3.3 product_price (商品价格表)

商品价格体系。

| 字段名 | 类型 | 长度 | 允许NULL | 默认值 | 约束 | 说明 |
|--------|------|------|----------|--------|------|------|
| id | BIGINT | - | NO | - | PK, AUTO_INCREMENT | 价格 ID |
| tenant_id | BIGINT | - | NO | - | INDEX | 租户 ID |
| product_id | BIGINT | - | NO | - | INDEX | 商品 ID |
| price_type | VARCHAR | 20 | NO | - | - | 价格类型 (PURCHASE/WHOLESALE/RETAIL) |
| price | DECIMAL | (15,2) | NO | - | - | 价格 |
| effective_date | DATE | - | NO | - | - | 生效日期 |
| created_at | DATETIME | - | NO | CURRENT_TIMESTAMP | - | 创建时间 |

**索引**:
- PRIMARY KEY: `id`
- INDEX: `idx_product_type_date` (`product_id`, `price_type`, `effective_date`)

#### 3.4 product_promotion (商品促销表)

商品促销价格。

| 字段名 | 类型 | 长度 | 允许NULL | 默认值 | 约束 | 说明 |
|--------|------|------|----------|--------|------|------|
| id | BIGINT | - | NO | - | PK, AUTO_INCREMENT | 促销 ID |
| tenant_id | BIGINT | - | NO | - | INDEX | 租户 ID |
| product_id | BIGINT | - | NO | - | INDEX | 商品 ID |
| promotion_price | DECIMAL | (15,2) | NO | - | - | 促销价 |
| start_time | DATETIME | - | NO | - | - | 开始时间 |
| end_time | DATETIME | - | NO | - | - | 结束时间 |
| status | TINYINT | - | NO | 1 | - | 状态 |
| created_at | DATETIME | - | NO | CURRENT_TIMESTAMP | - | 创建时间 |

**索引**:
- PRIMARY KEY: `id`
- INDEX: `idx_product_time` (`product_id`, `start_time`, `end_time`)

---

### 四、采购相关

#### 4.1 purchase_order (采购订单表)

采购主单据。

| 字段名 | 类型 | 长度 | 允许NULL | 默认值 | 约束 | 说明 |
|--------|------|------|----------|--------|------|------|
| id | BIGINT | - | NO | - | PK, AUTO_INCREMENT | 订单 ID |
| tenant_id | BIGINT | - | NO | - | INDEX | 租户 ID |
| order_no | VARCHAR | 50 | NO | - | UNIQUE | 订单编号 |
| supplier_id | BIGINT | - | NO | - | INDEX | 供应商 ID |
| order_date | DATE | - | NO | - | - | 订单日期 |
| delivery_date | DATE | - | YES | NULL | - | 交货日期 |
| total_amount | DECIMAL | (15,2) | NO | 0 | - | 总金额 |
| status | VARCHAR | 20 | NO | - | - | 状态 (PENDING/CONFIRMED/PARTIAL_RECEIVED/COMPLETED/CANCELLED) |
| remark | TEXT | - | YES | NULL | - | 备注 |
| created_at | DATETIME | - | NO | CURRENT_TIMESTAMP | - | 创建时间 |
| updated_at | DATETIME | - | NO | CURRENT_TIMESTAMP | ON UPDATE CURRENT_TIMESTAMP | 更新时间 |
| created_by | BIGINT | - | YES | NULL | - | 创建人 ID |

**索引**:
- PRIMARY KEY: `id`
- UNIQUE KEY: `uk_tenant_order_no` (`tenant_id`, `order_no`)
- INDEX: `idx_supplier` (`supplier_id`)
- INDEX: `idx_status` (`status`)
- INDEX: `idx_order_date` (`order_date`)

#### 4.2 purchase_order_item (采购订单明细表)

| 字段名 | 类型 | 长度 | 允许NULL | 默认值 | 约束 | 说明 |
|--------|------|------|----------|--------|------|------|
| id | BIGINT | - | NO | - | PK, AUTO_INCREMENT | 明细 ID |
| order_id | BIGINT | - | NO | - | INDEX | 订单 ID |
| product_id | BIGINT | - | NO | - | INDEX | 商品 ID |
| quantity | INT | - | NO | - | - | 数量 |
| unit_price | DECIMAL | (15,2) | NO | - | - | 单价 |
| amount | DECIMAL | (15,2) | NO | - | - | 金额 |
| received_quantity | INT | - | NO | 0 | - | 已入库数量 |
| remark | VARCHAR | 500 | YES | NULL | - | 备注 |

**索引**:
- PRIMARY KEY: `id`
- INDEX: `idx_order_id` (`order_id`)
- INDEX: `idx_product_id` (`product_id`)

#### 4.3 purchase_receipt (采购入库单表)

| 字段名 | 类型 | 长度 | 允许NULL | 默认值 | 约束 | 说明 |
|--------|------|------|----------|--------|------|------|
| id | BIGINT | - | NO | - | PK, AUTO_INCREMENT | 入库单 ID |
| tenant_id | BIGINT | - | NO | - | INDEX | 租户 ID |
| receipt_no | VARCHAR | 50 | NO | - | UNIQUE | 入库单号 |
| order_id | BIGINT | - | NO | - | INDEX | 采购订单 ID |
| supplier_id | BIGINT | - | NO | - | INDEX | 供应商 ID |
| receipt_date | DATE | - | NO | - | - | 入库日期 |
| warehouse_id | BIGINT | - | NO | - | INDEX | 仓库 ID |
| total_amount | DECIMAL | (15,2) | NO | 0 | - | 总金额 |
| status | VARCHAR | 20 | NO | - | - | 状态 |
| remark | TEXT | - | YES | NULL | - | 备注 |
| created_at | DATETIME | - | NO | CURRENT_TIMESTAMP | - | 创建时间 |
| created_by | BIGINT | - | YES | NULL | - | 创建人 ID |

**索引**:
- PRIMARY KEY: `id`
- UNIQUE KEY: `uk_tenant_receipt_no` (`tenant_id`, `receipt_no`)
- INDEX: `idx_order_id` (`order_id`)

#### 4.4 purchase_receipt_item (采购入库明细表)

| 字段名 | 类型 | 长度 | 允许NULL | 默认值 | 约束 | 说明 |
|--------|------|------|----------|--------|------|------|
| id | BIGINT | - | NO | - | PK, AUTO_INCREMENT | 明细 ID |
| receipt_id | BIGINT | - | NO | - | INDEX | 入库单 ID |
| order_item_id | BIGINT | - | YES | NULL | INDEX | 订单明细 ID |
| product_id | BIGINT | - | NO | - | INDEX | 商品 ID |
| order_quantity | INT | - | NO | - | - | 订单数量 |
| received_quantity | INT | - | NO | - | - | 实收数量 |
| unit_price | DECIMAL | (15,2) | NO | - | - | 单价 |
| amount | DECIMAL | (15,2) | NO | - | - | 金额 |
| batch_no | VARCHAR | 50 | YES | NULL | - | 批次号 |
| production_date | DATE | - | YES | NULL | - | 生产日期 |
| expiry_date | DATE | - | YES | NULL | - | 有效期 |

**索引**:
- PRIMARY KEY: `id`
- INDEX: `idx_receipt_id` (`receipt_id`)

#### 4.5 purchase_return (采购退货单表)

| 字段名 | 类型 | 长度 | 允许NULL | 默认值 | 约束 | 说明 |
|--------|------|------|----------|--------|------|------|
| id | BIGINT | - | NO | - | PK, AUTO_INCREMENT | 退货单 ID |
| tenant_id | BIGINT | - | NO | - | INDEX | 租户 ID |
| return_no | VARCHAR | 50 | NO | - | UNIQUE | 退货单号 |
| order_id | BIGINT | - | YES | NULL | INDEX | 采购订单 ID |
| receipt_id | BIGINT | - | YES | NULL | INDEX | 入库单 ID |
| supplier_id | BIGINT | - | NO | - | INDEX | 供应商 ID |
| return_date | DATE | - | NO | - | - | 退货日期 |
| total_amount | DECIMAL | (15,2) | NO | 0 | - | 总金额 |
| return_reason | TEXT | - | YES | NULL | - | 退货原因 |
| status | VARCHAR | 20 | NO | - | - | 状态 |
| created_at | DATETIME | - | NO | CURRENT_TIMESTAMP | - | 创建时间 |
| created_by | BIGINT | - | YES | NULL | - | 创建人 ID |

**索引**:
- PRIMARY KEY: `id`
- UNIQUE KEY: `uk_tenant_return_no` (`tenant_id`, `return_no`)

#### 4.6 purchase_return_item (采购退货明细表)

同入库明细结构。

---

### 五、销售相关

#### 5.1 sales_quotation (销售报价单表)

| 字段名 | 类型 | 长度 | 允许NULL | 默认值 | 约束 | 说明 |
|--------|------|------|----------|--------|------|------|
| id | BIGINT | - | NO | - | PK, AUTO_INCREMENT | 报价单 ID |
| tenant_id | BIGINT | - | NO | - | INDEX | 租户 ID |
| quotation_no | VARCHAR | 50 | NO | - | UNIQUE | 报价单号 |
| customer_id | BIGINT | - | NO | - | INDEX | 客户 ID |
| quotation_date | DATE | - | NO | - | - | 报价日期 |
| valid_until | DATE | - | YES | NULL | - | 有效期至 |
| total_amount | DECIMAL | (15,2) | NO | 0 | - | 总金额 |
| status | VARCHAR | 20 | NO | - | - | 状态 (DRAFT/SENT/ACCEPTED/EXPIRED/CONVERTED) |
| remark | TEXT | - | YES | NULL | - | 备注 |
| created_at | DATETIME | - | NO | CURRENT_TIMESTAMP | - | 创建时间 |
| created_by | BIGINT | - | YES | NULL | - | 创建人 ID |

**索引**:
- PRIMARY KEY: `id`
- UNIQUE KEY: `uk_tenant_quotation_no` (`tenant_id`, `quotation_no`)

#### 5.2 sales_quotation_item (销售报价明细表)

| 字段名 | 类型 | 长度 | 允许NULL | 默认值 | 约束 | 说明 |
|--------|------|------|----------|--------|------|------|
| id | BIGINT | - | NO | - | PK, AUTO_INCREMENT | 明细 ID |
| quotation_id | BIGINT | - | NO | - | INDEX | 报价单 ID |
| product_id | BIGINT | - | NO | - | INDEX | 商品 ID |
| quantity | INT | - | NO | - | - | 数量 |
| unit_price | DECIMAL | (15,2) | NO | - | - | 单价 |
| amount | DECIMAL | (15,2) | NO | - | - | 金额 |
| remark | VARCHAR | 500 | YES | NULL | - | 备注 |

#### 5.3 sales_order (销售订单表)

| 字段名 | 类型 | 长度 | 允许NULL | 默认值 | 约束 | 说明 |
|--------|------|------|----------|--------|------|------|
| id | BIGINT | - | NO | - | PK, AUTO_INCREMENT | 订单 ID |
| tenant_id | BIGINT | - | NO | - | INDEX | 租户 ID |
| order_no | VARCHAR | 50 | NO | - | UNIQUE | 订单编号 |
| customer_id | BIGINT | - | NO | - | INDEX | 客户 ID |
| order_date | DATE | - | NO | - | - | 订单日期 |
| delivery_date | DATE | - | YES | NULL | - | 交货日期 |
| total_amount | DECIMAL | (15,2) | NO | 0 | - | 总金额 |
| status | VARCHAR | 20 | NO | - | - | 状态 (PENDING/CONFIRMED/PARTIAL_DELIVERED/COMPLETED/CANCELLED) |
| is_shortage | TINYINT | - | NO | 0 | - | 是否缺货 |
| remark | TEXT | - | YES | NULL | - | 备注 |
| created_at | DATETIME | - | NO | CURRENT_TIMESTAMP | - | 创建时间 |
| updated_at | DATETIME | - | NO | CURRENT_TIMESTAMP | ON UPDATE CURRENT_TIMESTAMP | 更新时间 |
| created_by | BIGINT | - | YES | NULL | - | 创建人 ID |

**索引**:
- PRIMARY KEY: `id`
- UNIQUE KEY: `uk_tenant_order_no` (`tenant_id`, `order_no`)
- INDEX: `idx_customer` (`customer_id`)
- INDEX: `idx_status` (`status`)

#### 5.4 sales_order_item (销售订单明细表)

| 字段名 | 类型 | 长度 | 允许NULL | 默认值 | 约束 | 说明 |
|--------|------|------|----------|--------|------|------|
| id | BIGINT | - | NO | - | PK, AUTO_INCREMENT | 明细 ID |
| order_id | BIGINT | - | NO | - | INDEX | 订单 ID |
| product_id | BIGINT | - | NO | - | INDEX | 商品 ID |
| quantity | INT | - | NO | - | - | 数量 |
| unit_price | DECIMAL | (15,2) | NO | - | - | 单价 |
| amount | DECIMAL | (15,2) | NO | - | - | 金额 |
| delivered_quantity | INT | - | NO | 0 | - | 已出库数量 |
| remark | VARCHAR | 500 | YES | NULL | - | 备注 |

**索引**:
- PRIMARY KEY: `id`
- INDEX: `idx_order_id` (`order_id`)

#### 5.5 sales_delivery (销售出库单表)

| 字段名 | 类型 | 长度 | 允许NULL | 默认值 | 约束 | 说明 |
|--------|------|------|----------|--------|------|------|
| id | BIGINT | - | NO | - | PK, AUTO_INCREMENT | 出库单 ID |
| tenant_id | BIGINT | - | NO | - | INDEX | 租户 ID |
| delivery_no | VARCHAR | 50 | NO | - | UNIQUE | 出库单号 |
| order_id | BIGINT | - | NO | - | INDEX | 销售订单 ID |
| customer_id | BIGINT | - | NO | - | INDEX | 客户 ID |
| delivery_date | DATE | - | NO | - | - | 出库日期 |
| warehouse_id | BIGINT | - | NO | - | INDEX | 仓库 ID |
| total_amount | DECIMAL | (15,2) | NO | 0 | - | 总金额 |
| status | VARCHAR | 20 | NO | - | - | 状态 |
| remark | TEXT | - | YES | NULL | - | 备注 |
| created_at | DATETIME | - | NO | CURRENT_TIMESTAMP | - | 创建时间 |
| created_by | BIGINT | - | YES | NULL | - | 创建人 ID |

**索引**:
- PRIMARY KEY: `id`
- UNIQUE KEY: `uk_tenant_delivery_no` (`tenant_id`, `delivery_no`)

#### 5.6 sales_delivery_item (销售出库明细表)

| 字段名 | 类型 | 长度 | 允许NULL | 默认值 | 约束 | 说明 |
|--------|------|------|----------|--------|------|------|
| id | BIGINT | - | NO | - | PK, AUTO_INCREMENT | 明细 ID |
| delivery_id | BIGINT | - | NO | - | INDEX | 出库单 ID |
| order_item_id | BIGINT | - | YES | NULL | INDEX | 订单明细 ID |
| product_id | BIGINT | - | NO | - | INDEX | 商品 ID |
| order_quantity | INT | - | NO | - | - | 订单数量 |
| delivery_quantity | INT | - | NO | - | - | 出库数量 |
| unit_price | DECIMAL | (15,2) | NO | - | - | 单价 |
| amount | DECIMAL | (15,2) | NO | - | - | 金额 |
| batch_id | BIGINT | - | YES | NULL | INDEX | 批次 ID |

**索引**:
- PRIMARY KEY: `id`
- INDEX: `idx_delivery_id` (`delivery_id`)

#### 5.7 sales_return (销售退货单表)

| 字段名 | 类型 | 长度 | 允许NULL | 默认值 | 约束 | 说明 |
|--------|------|------|----------|--------|------|------|
| id | BIGINT | - | NO | - | PK, AUTO_INCREMENT | 退货单 ID |
| tenant_id | BIGINT | - | NO | - | INDEX | 租户 ID |
| return_no | VARCHAR | 50 | NO | - | UNIQUE | 退货单号 |
| order_id | BIGINT | - | YES | NULL | INDEX | 销售订单 ID |
| delivery_id | BIGINT | - | YES | NULL | INDEX | 出库单 ID |
| customer_id | BIGINT | - | NO | - | INDEX | 客户 ID |
| return_date | DATE | - | NO | - | - | 退货日期 |
| total_amount | DECIMAL | (15,2) | NO | 0 | - | 总金额 |
| return_reason | TEXT | - | YES | NULL | - | 退货原因 |
| status | VARCHAR | 20 | NO | - | - | 状态 |
| created_at | DATETIME | - | NO | CURRENT_TIMESTAMP | - | 创建时间 |
| created_by | BIGINT | - | YES | NULL | - | 创建人 ID |

**索引**:
- PRIMARY KEY: `id`
- UNIQUE KEY: `uk_tenant_return_no` (`tenant_id`, `return_no`)

#### 5.8 sales_return_item (销售退货明细表)

同出库明细结构。

---

### 六、仓库与库存相关

#### 6.1 warehouse (仓库表)

| 字段名 | 类型 | 长度 | 允许NULL | 默认值 | 约束 | 说明 |
|--------|------|------|----------|--------|------|------|
| id | BIGINT | - | NO | - | PK, AUTO_INCREMENT | 仓库 ID |
| tenant_id | BIGINT | - | NO | - | INDEX | 租户 ID |
| code | VARCHAR | 50 | NO | - | UNIQUE | 仓库编码 |
| name | VARCHAR | 100 | NO | - | - | 仓库名称 |
| address | VARCHAR | 500 | YES | NULL | - | 地址 |
| contact | VARCHAR | 50 | YES | NULL | - | 联系人 |
| phone | VARCHAR | 20 | YES | NULL | - | 电话 |
| status | TINYINT | - | NO | 1 | - | 状态 |
| created_at | DATETIME | - | NO | CURRENT_TIMESTAMP | - | 创建时间 |

**索引**:
- PRIMARY KEY: `id`
- UNIQUE KEY: `uk_tenant_code` (`tenant_id`, `code`)

#### 6.2 inventory (库存表)

| 字段名 | 类型 | 长度 | 允许NULL | 默认值 | 约束 | 说明 |
|--------|------|------|----------|--------|------|------|
| id | BIGINT | - | NO | - | PK, AUTO_INCREMENT | 库存 ID |
| tenant_id | BIGINT | - | NO | - | INDEX | 租户 ID |
| product_id | BIGINT | - | NO | - | INDEX | 商品 ID |
| warehouse_id | BIGINT | - | NO | - | INDEX | 仓库 ID |
| quantity | INT | - | NO | 0 | - | 当前库存数量 |
| locked_quantity | INT | - | NO | 0 | - | 锁定库存数量 |
| unit_cost | DECIMAL | (15,4) | YES | NULL | - | 单位成本 |
| version | INT | - | NO | 0 | - | 乐观锁版本号 |
| updated_at | DATETIME | - | NO | CURRENT_TIMESTAMP | ON UPDATE CURRENT_TIMESTAMP | 更新时间 |

**索引**:
- PRIMARY KEY: `id`
- UNIQUE KEY: `uk_product_warehouse` (`tenant_id`, `product_id`, `warehouse_id`)
- INDEX: `idx_product` (`product_id`)
- INDEX: `idx_warehouse` (`warehouse_id`)

#### 6.3 inventory_batch (库存批次表)

| 字段名 | 类型 | 长度 | 允许NULL | 默认值 | 约束 | 说明 |
|--------|------|------|----------|--------|------|------|
| id | BIGINT | - | NO | - | PK, AUTO_INCREMENT | 批次 ID |
| tenant_id | BIGINT | - | NO | - | INDEX | 租户 ID |
| product_id | BIGINT | - | NO | - | INDEX | 商品 ID |
| warehouse_id | BIGINT | - | NO | - | INDEX | 仓库 ID |
| batch_no | VARCHAR | 50 | NO | - | - | 批次号 |
| production_date | DATE | - | YES | NULL | - | 生产日期 |
| expiry_date | DATE | - | YES | NULL | INDEX | 有效期 |
| quantity | INT | - | NO | 0 | - | 数量 |
| created_at | DATETIME | - | NO | CURRENT_TIMESTAMP | - | 创建时间 |

**索引**:
- PRIMARY KEY: `id`
- INDEX: `idx_product_warehouse_fifo` (`product_id`, `warehouse_id`, `production_date`)
- INDEX: `idx_expiry` (`expiry_date`)

#### 6.4 inventory_transaction (库存流水表)

| 字段名 | 类型 | 长度 | 允许NULL | 默认值 | 约束 | 说明 |
|--------|------|------|----------|--------|------|------|
| id | BIGINT | - | NO | - | PK, AUTO_INCREMENT | 流水 ID |
| tenant_id | BIGINT | - | NO | - | INDEX | 租户 ID |
| product_id | BIGINT | - | NO | - | INDEX | 商品 ID |
| warehouse_id | BIGINT | - | NO | - | INDEX | 仓库 ID |
| batch_id | BIGINT | - | YES | NULL | INDEX | 批次 ID |
| transaction_type | VARCHAR | 20 | NO | - | - | 流水类型 (PURCHASE_IN/SALES_OUT/TRANSFER_IN/TRANSFER_OUT/CHECK_GAIN/CHECK_LOSS) |
| quantity | INT | - | NO | - | - | 变动数量 |
| before_quantity | INT | - | NO | - | - | 变动前数量 |
| after_quantity | INT | - | NO | - | - | 变动后数量 |
| document_type | VARCHAR | 20 | YES | NULL | - | 关联单据类型 |
| document_id | BIGINT | - | YES | NULL | - | 关联单据 ID |
| remark | VARCHAR | 500 | YES | NULL | - | 备注 |
| created_at | DATETIME | - | NO | CURRENT_TIMESTAMP | INDEX | 创建时间 |
| created_by | BIGINT | - | YES | NULL | - | 创建人 ID |

**索引**:
- PRIMARY KEY: `id`
- INDEX: `idx_product_warehouse` (`product_id`, `warehouse_id`)
- INDEX: `idx_document` (`document_type`, `document_id`)
- INDEX: `idx_created_at` (`created_at`)

#### 6.5 stock_transfer (库存调拨单表)

| 字段名 | 类型 | 长度 | 允许NULL | 默认值 | 约束 | 说明 |
|--------|------|------|----------|--------|------|------|
| id | BIGINT | - | NO | - | PK, AUTO_INCREMENT | 调拨单 ID |
| tenant_id | BIGINT | - | NO | - | INDEX | 租户 ID |
| transfer_no | VARCHAR | 50 | NO | - | UNIQUE | 调拨单号 |
| from_warehouse_id | BIGINT | - | NO | - | INDEX | 调出仓库 ID |
| to_warehouse_id | BIGINT | - | NO | - | INDEX | 调入仓库 ID |
| transfer_date | DATE | - | NO | - | - | 调拨日期 |
| status | VARCHAR | 20 | NO | - | - | 状态 (PENDING/IN_TRANSIT/COMPLETED/CANCELLED) |
| remark | TEXT | - | YES | NULL | - | 备注 |
| created_at | DATETIME | - | NO | CURRENT_TIMESTAMP | - | 创建时间 |
| created_by | BIGINT | - | YES | NULL | - | 创建人 ID |

**索引**:
- PRIMARY KEY: `id`
- UNIQUE KEY: `uk_tenant_transfer_no` (`tenant_id`, `transfer_no`)

#### 6.6 stock_transfer_item (库存调拨明细表)

| 字段名 | 类型 | 长度 | 允许NULL | 默认值 | 约束 | 说明 |
|--------|------|------|----------|--------|------|------|
| id | BIGINT | - | NO | - | PK, AUTO_INCREMENT | 明细 ID |
| transfer_id | BIGINT | - | NO | - | INDEX | 调拨单 ID |
| product_id | BIGINT | - | NO | - | INDEX | 商品 ID |
| quantity | INT | - | NO | - | - | 调拨数量 |
| remark | VARCHAR | 500 | YES | NULL | - | 备注 |

**索引**:
- PRIMARY KEY: `id`
- INDEX: `idx_transfer_id` (`transfer_id`)

#### 6.7 stock_check (库存盘点单表)

| 字段名 | 类型 | 长度 | 允许NULL | 默认值 | 约束 | 说明 |
|--------|------|------|----------|--------|------|------|
| id | BIGINT | - | NO | - | PK, AUTO_INCREMENT | 盘点单 ID |
| tenant_id | BIGINT | - | NO | - | INDEX | 租户 ID |
| check_no | VARCHAR | 50 | NO | - | UNIQUE | 盘点单号 |
| warehouse_id | BIGINT | - | NO | - | INDEX | 仓库 ID |
| check_date | DATE | - | NO | - | - | 盘点日期 |
| status | VARCHAR | 20 | NO | - | - | 状态 (DRAFT/CONFIRMED/CANCELLED) |
| remark | TEXT | - | YES | NULL | - | 备注 |
| created_at | DATETIME | - | NO | CURRENT_TIMESTAMP | - | 创建时间 |
| created_by | BIGINT | - | YES | NULL | - | 创建人 ID |

**索引**:
- PRIMARY KEY: `id`
- UNIQUE KEY: `uk_tenant_check_no` (`tenant_id`, `check_no`)

#### 6.8 stock_check_item (库存盘点明细表)

| 字段名 | 类型 | 长度 | 允许NULL | 默认值 | 约束 | 说明 |
|--------|------|------|----------|--------|------|------|
| id | BIGINT | - | NO | - | PK, AUTO_INCREMENT | 明细 ID |
| check_id | BIGINT | - | NO | - | INDEX | 盘点单 ID |
| product_id | BIGINT | - | NO | - | INDEX | 商品 ID |
| warehouse_id | BIGINT | - | NO | - | - | 仓库 ID |
| batch_id | BIGINT | - | YES | NULL | - | 批次 ID |
| book_quantity | INT | - | NO | - | - | 账面数量 |
| actual_quantity | INT | - | NO | - | - | 实盘数量 |
| diff_quantity | INT | - | NO | - | - | 差异数量 |
| check_reason | VARCHAR | 200 | YES | NULL | - | 盘点原因 |

**索引**:
- PRIMARY KEY: `id`
- INDEX: `idx_check_id` (`check_id`)

---

### 七、结算相关

#### 7.1 payment_account (应付记录表)

| 字段名 | 类型 | 长度 | 允许NULL | 默认值 | 约束 | 说明 |
|--------|------|------|----------|--------|------|------|
| id | BIGINT | - | NO | - | PK, AUTO_INCREMENT | 应付 ID |
| tenant_id | BIGINT | - | NO | - | INDEX | 租户 ID |
| payment_no | VARCHAR | 50 | NO | - | UNIQUE | 应付单号 |
| supplier_id | BIGINT | - | NO | - | INDEX | 供应商 ID |
| purchase_order_id | BIGINT | - | NO | - | INDEX | 采购订单 ID |
| receipt_id | BIGINT | - | NO | - | INDEX | 入库单 ID |
| total_amount | DECIMAL | (15,2) | NO | - | - | 应付金额 |
| paid_amount | DECIMAL | (15,2) | NO | 0 | - | 已付金额 |
| remaining_amount | DECIMAL | (15,2) | NO | - | - | 欠款金额 |
| status | VARCHAR | 20 | NO | - | - | 状态 (UNPAID/PARTIAL_PAID/PAID) |
| created_at | DATETIME | - | NO | CURRENT_TIMESTAMP | - | 创建时间 |

**索引**:
- PRIMARY KEY: `id`
- UNIQUE KEY: `uk_tenant_payment_no` (`tenant_id`, `payment_no`)
- INDEX: `idx_supplier` (`supplier_id`)

#### 7.2 payment_application (付款申请表)

| 字段名 | 类型 | 长度 | 允许NULL | 默认值 | 约束 | 说明 |
|--------|------|------|----------|--------|------|------|
| id | BIGINT | - | NO | - | PK, AUTO_INCREMENT | 申请 ID |
| tenant_id | BIGINT | - | NO | - | INDEX | 租户 ID |
| application_no | VARCHAR | 50 | NO | - | UNIQUE | 申请单号 |
| payment_account_id | BIGINT | - | NO | - | INDEX | 应付记录 ID |
| supplier_id | BIGINT | - | NO | - | - | 供应商 ID |
| application_amount | DECIMAL | (15,2) | NO | - | - | 申请金额 |
| payment_method | VARCHAR | 20 | NO | - | - | 付款方式 |
| expected_payment_date | DATE | - | YES | NULL | - | 期望付款日期 |
| status | VARCHAR | 20 | NO | - | - | 状态 (PENDING/APPROVED/REJECTED/PAID) |
| created_at | DATETIME | - | NO | CURRENT_TIMESTAMP | - | 创建时间 |
| created_by | BIGINT | - | YES | NULL | - | 申请人 ID |

**索引**:
- PRIMARY KEY: `id`
- UNIQUE KEY: `uk_tenant_app_no` (`tenant_id`, `application_no`)

#### 7.3 receipt_account (应收记录表)

| 字段名 | 类型 | 长度 | 允许NULL | 默认值 | 约束 | 说明 |
|--------|------|------|----------|--------|------|------|
| id | BIGINT | - | NO | - | PK, AUTO_INCREMENT | 应收 ID |
| tenant_id | BIGINT | - | NO | - | INDEX | 租户 ID |
| receipt_no | VARCHAR | 50 | NO | - | UNIQUE | 应收单号 |
| customer_id | BIGINT | - | NO | - | INDEX | 客户 ID |
| sales_order_id | BIGINT | - | NO | - | INDEX | 销售订单 ID |
| delivery_id | BIGINT | - | NO | - | INDEX | 出库单 ID |
| total_amount | DECIMAL | (15,2) | NO | - | - | 应收金额 |
| received_amount | DECIMAL | (15,2) | NO | 0 | - | 已收金额 |
| remaining_amount | DECIMAL | (15,2) | NO | - | - | 欠款金额 |
| status | VARCHAR | 20 | NO | - | - | 状态 (UNRECEIVED/PARTIAL_RECEIVED/RECEIVED) |
| created_at | DATETIME | - | NO | CURRENT_TIMESTAMP | - | 创建时间 |

**索引**:
- PRIMARY KEY: `id`
- UNIQUE KEY: `uk_tenant_receipt_no` (`tenant_id`, `receipt_no`)
- INDEX: `idx_customer` (`customer_id`)

#### 7.4 receipt_registration (收款登记表)

| 字段名 | 类型 | 长度 | 允许NULL | 默认值 | 约束 | 说明 |
|--------|------|------|----------|--------|------|------|
| id | BIGINT | - | NO | - | PK, AUTO_INCREMENT | 收款 ID |
| tenant_id | BIGINT | - | NO | - | INDEX | 租户 ID |
| registration_no | VARCHAR | 50 | NO | - | UNIQUE | 收款单号 |
| receipt_account_id | BIGINT | - | NO | - | INDEX | 应收记录 ID |
| customer_id | BIGINT | - | NO | - | - | 客户 ID |
| received_amount | DECIMAL | (15,2) | NO | - | - | 收款金额 |
| payment_method | VARCHAR | 20 | NO | - | - | 收款方式 |
| received_date | DATE | - | NO | - | - | 收款日期 |
| created_at | DATETIME | - | NO | CURRENT_TIMESTAMP | - | 创建时间 |
| created_by | BIGINT | - | YES | NULL | - | 登记人 ID |

**索引**:
- PRIMARY KEY: `id`
- UNIQUE KEY: `uk_tenant_reg_no` (`tenant_id`, `registration_no`)

#### 7.5 invoice (发票表)

| 字段名 | 类型 | 长度 | 允许NULL | 默认值 | 约束 | 说明 |
|--------|------|------|----------|--------|------|------|
| id | BIGINT | - | NO | - | PK, AUTO_INCREMENT | 发票 ID |
| tenant_id | BIGINT | - | NO | - | INDEX | 租户 ID |
| invoice_no | VARCHAR | 50 | NO | - | UNIQUE | 发票单号 |
| invoice_type | VARCHAR | 20 | NO | - | - | 发票类型 (PURCHASE/SALES) |
| document_type | VARCHAR | 20 | NO | - | - | 关联单据类型 |
| document_id | BIGINT | - | NO | - | - | 关联单据 ID |
| invoice_date | DATE | - | NO | - | - | 发票日期 |
| invoice_amount | DECIMAL | (15,2) | NO | - | - | 发票金额 |
| tax_number | VARCHAR | 50 | YES | NULL | - | 税号 |
| status | VARCHAR | 20 | NO | - | - | 状态 |
| created_at | DATETIME | - | NO | CURRENT_TIMESTAMP | - | 创建时间 |

**索引**:
- PRIMARY KEY: `id`
- UNIQUE KEY: `uk_tenant_invoice_no` (`tenant_id`, `invoice_no`)

---

### 八、系统配置相关

#### 8.1 system_config (系统配置表)

| 字段名 | 类型 | 长度 | 允许NULL | 默认值 | 约束 | 说明 |
|--------|------|------|----------|--------|------|------|
| id | BIGINT | - | NO | - | PK, AUTO_INCREMENT | 配置 ID |
| tenant_id | BIGINT | - | YES | NULL | INDEX | 租户 ID (NULL 表示全局配置) |
| config_key | VARCHAR | 100 | NO | - | - | 配置键 |
| config_value | TEXT | - | YES | NULL | - | 配置值 |
| description | VARCHAR | 200 | YES | NULL | - | 描述 |
| created_at | DATETIME | - | NO | CURRENT_TIMESTAMP | - | 创建时间 |
| updated_at | DATETIME | - | NO | CURRENT_TIMESTAMP | ON UPDATE CURRENT_TIMESTAMP | 更新时间 |

**索引**:
- PRIMARY KEY: `id`
- UNIQUE KEY: `uk_tenant_key` (`tenant_id`, `config_key`)

#### 8.2 approval_rule (审批规则表)

| 字段名 | 类型 | 长度 | 允许NULL | 默认值 | 约束 | 说明 |
|--------|------|------|----------|--------|------|------|
| id | BIGINT | - | NO | - | PK, AUTO_INCREMENT | 规则 ID |
| tenant_id | BIGINT | - | NO | - | INDEX | 租户 ID |
| name | VARCHAR | 100 | NO | - | - | 规则名称 |
| document_type | VARCHAR | 20 | NO | - | - | 单据类型 |
| amount_threshold | DECIMAL | (15,2) | YES | NULL | - | 金额阈值 |
| approval_type | VARCHAR | 20 | NO | - | - | 审批类型 |
| approvers | TEXT | - | YES | NULL | - | 审批人 (JSON) |
| status | TINYINT | - | NO | 1 | - | 状态 |
| created_at | DATETIME | - | NO | CURRENT_TIMESTAMP | - | 创建时间 |

**索引**:
- PRIMARY KEY: `id`
- INDEX: `idx_document_type` (`document_type`)

#### 8.3 number_rule (单据编号规则表)

| 字段名 | 类型 | 长度 | 允许NULL | 默认值 | 约束 | 说明 |
|--------|------|------|----------|--------|------|------|
| id | BIGINT | - | NO | - | PK, AUTO_INCREMENT | 规则 ID |
| tenant_id | BIGINT | - | NO | - | INDEX | 租户 ID |
| name | VARCHAR | 100 | NO | - | - | 规则名称 |
| document_type | VARCHAR | 20 | NO | - | UNIQUE | 单据类型 |
| prefix | VARCHAR | 20 | NO | - | - | 前缀 |
| date_format | VARCHAR | 20 | NO | - | - | 日期格式 (yyyyMMdd) |
| sequence_length | INT | - | NO | 4 | - | 流水号位数 |
| status | TINYINT | - | NO | 1 | - | 状态 |
| created_at | DATETIME | - | NO | CURRENT_TIMESTAMP | - | 创建时间 |

**索引**:
- PRIMARY KEY: `id`
- UNIQUE KEY: `uk_tenant_type` (`tenant_id`, `document_type`)

#### 8.4 role (角色表)

| 字段名 | 类型 | 长度 | 允许NULL | 默认值 | 约束 | 说明 |
|--------|------|------|----------|--------|------|------|
| id | BIGINT | - | NO | - | PK, AUTO_INCREMENT | 角色 ID |
| tenant_id | BIGINT | - | NO | - | INDEX | 租户 ID |
| role_code | VARCHAR | 50 | NO | - | - | 角色编码 |
| role_name | VARCHAR | 100 | NO | - | - | 角色名称 |
| description | VARCHAR | 200 | YES | NULL | - | 描述 |
| status | TINYINT | - | NO | 1 | - | 状态 |
| created_at | DATETIME | - | NO | CURRENT_TIMESTAMP | - | 创建时间 |

**索引**:
- PRIMARY KEY: `id`
- UNIQUE KEY: `uk_tenant_code` (`tenant_id`, `role_code`)

#### 8.5 permission (权限表)

| 字段名 | 类型 | 长度 | 允许NULL | 默认值 | 约束 | 说明 |
|--------|------|------|----------|--------|------|------|
| id | BIGINT | - | NO | - | PK, AUTO_INCREMENT | 权限 ID |
| permission_code | VARCHAR | 100 | NO | - | UNIQUE | 权限编码 |
| permission_name | VARCHAR | 100 | NO | - | - | 权限名称 |
| permission_type | VARCHAR | 20 | NO | - | - | 权限类型 (MENU/BUTTON/API) |
| resource_path | VARCHAR | 200 | YES | NULL | - | 资源路径 |
| parent_id | BIGINT | - | YES | NULL | INDEX | 父权限 ID |
| sort_order | INT | - | NO | 0 | - | 排序 |
| status | TINYINT | - | NO | 1 | - | 状态 |
| created_at | DATETIME | - | NO | CURRENT_TIMESTAMP | - | 创建时间 |

**索引**:
- PRIMARY KEY: `id`
- INDEX: `idx_parent_id` (`parent_id`)

#### 8.6 role_permission (角色权限关联表)

| 字段名 | 类型 | 长度 | 允许NULL | 默认值 | 约束 | 说明 |
|--------|------|------|----------|--------|------|------|
| id | BIGINT | - | NO | - | PK, AUTO_INCREMENT | 关联 ID |
| role_id | BIGINT | - | NO | - | INDEX | 角色 ID |
| permission_id | BIGINT | - | NO | - | INDEX | 权限 ID |
| created_at | DATETIME | - | NO | CURRENT_TIMESTAMP | - | 创建时间 |

**索引**:
- PRIMARY KEY: `id`
- UNIQUE KEY: `uk_role_permission` (`role_id`, `permission_id`)

#### 8.7 user (用户表)

| 字段名 | 类型 | 长度 | 允许NULL | 默认值 | 约束 | 说明 |
|--------|------|------|----------|--------|------|------|
| id | BIGINT | - | NO | - | PK, AUTO_INCREMENT | 用户 ID |
| tenant_id | BIGINT | - | NO | - | INDEX | 租户 ID |
| username | VARCHAR | 50 | NO | - | - | 用户名 |
| real_name | VARCHAR | 50 | NO | - | - | 真实姓名 |
| password | VARCHAR | 100 | NO | - | - | 密码 (加密) |
| email | VARCHAR | 100 | YES | NULL | - | 邮箱 |
| phone | VARCHAR | 20 | YES | NULL | - | 手机号 |
| role_id | BIGINT | - | NO | - | INDEX | 角色 ID |
| status | TINYINT | - | NO | 1 | - | 状态 |
| created_at | DATETIME | - | NO | CURRENT_TIMESTAMP | - | 创建时间 |
| updated_at | DATETIME | - | NO | CURRENT_TIMESTAMP | ON UPDATE CURRENT_TIMESTAMP | 更新时间 |

**索引**:
- PRIMARY KEY: `id`
- UNIQUE KEY: `uk_tenant_username` (`tenant_id`, `username`)

---

## 实体类定义示例

### Product.java

```java
package com.example.erp.entity.product;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

@Data
@TableName("product")
public class Product extends TenantBaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String code;

    private String name;

    private Long categoryId;

    private String specification;

    private String unit;

    private java.math.BigDecimal referencePrice;

    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    private Long createdBy;
}
```

### PurchaseOrder.java

```java
package com.example.erp.entity.purchase;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@TableName("purchase_order")
public class PurchaseOrder extends TenantBaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String orderNo;

    private Long supplierId;

    private LocalDate orderDate;

    private LocalDate deliveryDate;

    private BigDecimal totalAmount;

    private String status;

    private String remark;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    private Long createdBy;
}
```

## 数据一致性

### 库存并发控制

```java
// 乐观锁配置
@TableName("inventory")
public class Inventory {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long productId;
    private Long warehouseId;
    private Integer quantity;

    @Version  // MyBatis-Plus 乐观锁
    private Integer version;
}

// 库存扣减（带版本检查）
<update id="deductInventory">
    UPDATE inventory
    SET quantity = quantity - #{quantity},
        version = version + 1
    WHERE product_id = #{productId}
      AND warehouse_id = #{warehouseId}
      AND quantity >= #{quantity}
      AND version = #{version}
</update>
```

## 下一步

- 查看技术研究: `research.md`
- 查看快速开始指南: `quickstart.md`
- 查看 API 契约: `contracts/`
