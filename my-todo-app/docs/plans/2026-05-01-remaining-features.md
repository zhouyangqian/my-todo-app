# 剩余功能实施计划 ✅ 已全部完成

> **状态更新（2026-06-01）**：本计划中的 9 个任务已全部实施完毕。详细验证报告见 `docs/plans/2026-06-01-implementation-status.md`。
>
> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 完成项目剩余的全部功能，包括采购订单、发票管理、Dashboard统计、库存流水页面，以及修复前端占位操作。✅ 已达成

**Architecture:** 遵循现有项目模式——后端使用 MyBatis-Plus ServiceImpl + Controller 分层，前端使用 Vue 3 + Element Plus + Axios。采购订单参照销售订单模式（含明细），发票管理为独立CRUD模块，Dashboard 通过聚合各模块统计API实现。

**Tech Stack:** Java 17, Spring Boot 3.2, MyBatis-Plus 3.5.5, Vue 3.4, Element Plus 2.4, Axios, Pinia

---

## 文件结构总览

### 后端新增/修改文件

**Task 1 - 采购订单:**
- Create: `services/erp-service/src/main/java/com/example/erp/entity/PurchaseOrderItem.java`
- Create: `services/erp-service/src/main/java/com/example/erp/mapper/PurchaseOrderItemMapper.java`
- Create: `services/erp-service/src/main/java/com/example/erp/dto/PurchaseOrderCreateRequest.java`
- Create: `services/erp-service/src/main/java/com/example/erp/dto/PurchaseOrderVO.java`
- Create: `services/erp-service/src/main/java/com/example/erp/service/PurchaseOrderService.java`
- Create: `services/erp-service/src/main/java/com/example/erp/controller/PurchaseOrderController.java`
- Create: `db-scripts/10-purchase-order-item-table.sql`

**Task 2 - 发票管理:**
- Create: `services/finance-service/src/main/java/com/example/finance/service/InvoiceService.java`
- Create: `services/finance-service/src/main/java/com/example/finance/controller/InvoiceController.java`
- Modify: `services/finance-service/src/main/java/com/example/finance/controller/FinanceController.java` (可选，或独立Controller)

**Task 3 - Dashboard统计:**
- Create: `services/erp-service/src/main/java/com/example/erp/controller/DashboardController.java` (或在各service中加统计方法)
- Modify: `services/finance-service/src/main/java/com/example/finance/service/AccountReceivableService.java` (加统计方法)
- Modify: `services/finance-service/src/main/java/com/example/finance/service/AccountPayableService.java` (加统计方法)

**Task 4 - 修复前端占位操作:**
- Modify: `frontend/src/views/finance/receivable/index.vue`
- Modify: `frontend/src/views/finance/payable/index.vue`
- Modify: `frontend/src/views/finance/bank-account/index.vue`

### 前端新增/修改文件

**Task 1 - 采购订单前端:**
- Create: `frontend/src/views/erp/purchase-order/index.vue`
- Modify: `frontend/src/api/erp.js` (添加采购订单API)
- Modify: `frontend/src/router/index.js` (添加路由)

**Task 2 - 发票管理前端:**
- Create: `frontend/src/views/finance/invoice/index.vue`
- Modify: `frontend/src/api/finance.js` (添加发票API)
- Modify: `frontend/src/router/index.js` (添加路由)

**Task 3 - Dashboard前端:**
- Modify: `frontend/src/views/dashboard/index.vue` (重写)
- Modify: `frontend/src/api/erp.js` (添加统计API)

**Task 5 - 库存流水前端:**
- Create: `frontend/src/views/erp/inventory-flow/index.vue`
- Modify: `frontend/src/router/index.js` (添加路由)

---

## Task 1: 采购订单管理（后端）

**Files:**
- Create: `services/erp-service/src/main/java/com/example/erp/entity/PurchaseOrderItem.java`
- Create: `services/erp-service/src/main/java/com/example/erp/mapper/PurchaseOrderItemMapper.java`
- Create: `services/erp-service/src/main/java/com/example/erp/dto/PurchaseOrderCreateRequest.java`
- Create: `services/erp-service/src/main/java/com/example/erp/dto/PurchaseOrderVO.java`
- Create: `services/erp-service/src/main/java/com/example/erp/service/PurchaseOrderService.java`
- Create: `services/erp-service/src/main/java/com/example/erp/controller/PurchaseOrderController.java`
- Create: `db-scripts/10-purchase-order-item-table.sql`

### Step 1: 创建采购订单明细表SQL

创建 `db-scripts/10-purchase-order-item-table.sql`:

```sql
-- 采购订单明细表
CREATE TABLE IF NOT EXISTS `erp_purchase_order_item` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '明细ID',
    `tenant_id` BIGINT NOT NULL DEFAULT 0 COMMENT '租户ID',
    `order_id` BIGINT NOT NULL COMMENT '订单ID',
    `product_id` BIGINT NOT NULL COMMENT '商品ID',
    `product_code` VARCHAR(50) NOT NULL COMMENT '商品编码',
    `product_name` VARCHAR(200) NOT NULL COMMENT '商品名称',
    `specification` VARCHAR(100) DEFAULT NULL COMMENT '规格型号',
    `unit` VARCHAR(20) DEFAULT NULL COMMENT '单位',
    `quantity` DECIMAL(18,4) NOT NULL COMMENT '采购数量',
    `price` DECIMAL(18,4) NOT NULL COMMENT '单价',
    `discount_amount` DECIMAL(18,2) DEFAULT 0 COMMENT '行折扣金额',
    `amount` DECIMAL(18,2) NOT NULL COMMENT '行金额',
    `received_quantity` DECIMAL(18,4) DEFAULT 0 COMMENT '已入库数量',
    `received_amount` DECIMAL(18,2) DEFAULT 0 COMMENT '已入库金额',
    `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '软删除',
    `created_by` BIGINT DEFAULT NULL COMMENT '创建人',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_by` BIGINT DEFAULT NULL COMMENT '更新人',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_tenant_id` (`tenant_id`),
    KEY `idx_order_id` (`order_id`),
    KEY `idx_product_id` (`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='采购订单明细表';
```

### Step 2: 创建 PurchaseOrderItem 实体

创建 `services/erp-service/src/main/java/com/example/erp/entity/PurchaseOrderItem.java`:

```java
package com.example.erp.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("erp_purchase_order_item")
public class PurchaseOrderItem implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField(fill = FieldFill.INSERT)
    private Long tenantId;

    private Long orderId;
    private Long productId;
    private String productCode;
    private String productName;
    private String specification;
    private String unit;
    private BigDecimal quantity;
    private BigDecimal price;
    private BigDecimal discountAmount;
    private BigDecimal amount;
    private BigDecimal receivedQuantity;
    private BigDecimal receivedAmount;
    private String remark;

    @TableLogic
    @TableField(fill = FieldFill.INSERT)
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    private Long createdBy;

    @TableField(fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.UPDATE)
    private Long updatedBy;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private LocalDateTime updatedAt;
}
```

### Step 3: 创建 PurchaseOrderItemMapper

创建 `services/erp-service/src/main/java/com/example/erp/mapper/PurchaseOrderItemMapper.java`:

```java
package com.example.erp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.erp.entity.PurchaseOrderItem;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PurchaseOrderItemMapper extends BaseMapper<PurchaseOrderItem> {
}
```

### Step 4: 创建 PurchaseOrderCreateRequest DTO

创建 `services/erp-service/src/main/java/com/example/erp/dto/PurchaseOrderCreateRequest.java`:

```java
package com.example.erp.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class PurchaseOrderCreateRequest {

    @NotNull(message = "供应商ID不能为空")
    private Long supplierId;

    @NotNull(message = "仓库ID不能为空")
    private Long warehouseId;

    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private LocalDateTime expectedDate;

    private Long handlerId;
    private String remark;

    @NotNull(message = "订单明细不能为空")
    private List<OrderItemRequest> items;

    @Data
    public static class OrderItemRequest {
        @NotNull(message = "商品ID不能为空")
        private Long productId;

        @NotNull(message = "数量不能为空")
        @Positive(message = "数量必须大于0")
        private BigDecimal quantity;

        @NotNull(message = "单价不能为空")
        @Positive(message = "单价必须大于0")
        private BigDecimal price;

        private BigDecimal discountAmount;
        private String remark;
    }
}
```

### Step 5: 创建 PurchaseOrderVO DTO

创建 `services/erp-service/src/main/java/com/example/erp/dto/PurchaseOrderVO.java`:

```java
package com.example.erp.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class PurchaseOrderVO {
    private Long id;
    private String orderNo;
    private Long supplierId;
    private String supplierName;
    private Long warehouseId;
    private String warehouseName;

    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private LocalDateTime orderDate;

    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private LocalDateTime expectedDate;

    private BigDecimal totalAmount;
    private BigDecimal discountAmount;
    private BigDecimal paidAmount;
    private Integer orderStatus;
    private String orderStatusText;
    private Long approvedBy;
    private String approvedByName;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "GMT+8")
    private LocalDateTime approvedAt;

    private Long handlerId;
    private String handlerName;
    private String remark;
    private List<PurchaseOrderItemVO> items;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "GMT+8")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "GMT+8")
    private LocalDateTime updatedAt;

    @Data
    public static class PurchaseOrderItemVO {
        private Long id;
        private Long productId;
        private String productCode;
        private String productName;
        private String specification;
        private String unit;
        private BigDecimal quantity;
        private BigDecimal price;
        private BigDecimal discountAmount;
        private BigDecimal amount;
        private BigDecimal receivedQuantity;
        private BigDecimal receivedAmount;
        private String remark;
        private BigDecimal receivableQuantity; // 可入库数量 = 数量 - 已入库数量
    }
}
```

### Step 6: 创建 PurchaseOrderService

创建 `services/erp-service/src/main/java/com/example/erp/service/PurchaseOrderService.java`:

参照 `SalesOrderService` 的完整模式，实现以下方法：
- `getOrderPage(tenantId, page, size, orderNo, supplierId, status)` — 分页查询
- `getOrderDetail(orderId)` — 获取详情含明细
- `createOrder(request, userId)` — 创建订单含明细
- `updateOrder(orderId, request, userId)` — 更新订单（仅草稿）
- `submitForApproval(orderId)` — 提交审核
- `approveOrder(orderId, userId)` — 审核通过
- `cancelOrder(orderId, userId)` — 取消订单
- `generateOrderNo(tenantId)` — 生成编号（格式: PO + yyyyMMdd + 4位序号）

关键代码结构参照 SalesOrderService，将 `customerId` 换为 `supplierId`，将 `SalesOrder` 换为 `PurchaseOrder`，将 `SalesOrderItem` 换为 `PurchaseOrderItem`。

### Step 7: 创建 PurchaseOrderController

创建 `services/erp-service/src/main/java/com/example/erp/controller/PurchaseOrderController.java`:

```java
package com.example.erp.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.common.core.result.ApiResponse;
import com.example.common.core.result.PageResult;
import com.example.erp.dto.PurchaseOrderCreateRequest;
import com.example.erp.dto.PurchaseOrderVO;
import com.example.erp.entity.PurchaseOrder;
import com.example.erp.service.PurchaseOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "采购订单管理", description = "采购订单增删改查及审核API")
@RestController
@RequestMapping("/api/erp/purchase-orders")
@RequiredArgsConstructor
public class PurchaseOrderController {

    private final PurchaseOrderService purchaseOrderService;

    @Operation(summary = "分页查询采购订单")
    @GetMapping("/get-purchase-order-page")
    public ApiResponse<PageResult<PurchaseOrderVO>> getOrderPage(
            @RequestHeader("X-Tenant-Id") Long tenantId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String orderNo,
            @RequestParam(required = false) Long supplierId,
            @RequestParam(required = false) Integer status) {
        Page<PurchaseOrderVO> result = purchaseOrderService.getOrderPage(tenantId, page, size, orderNo, supplierId, status);
        PageResult<PurchaseOrderVO> pageResult = PageResult.of(result.getRecords(), result.getTotal(), result.getCurrent(), result.getSize());
        return ApiResponse.success(pageResult);
    }

    @Operation(summary = "获取采购订单详情")
    @GetMapping("/get-purchase-order/{id}")
    public ApiResponse<PurchaseOrderVO> getOrderDetail(@PathVariable Long id) {
        return ApiResponse.success(purchaseOrderService.getOrderDetail(id));
    }

    @Operation(summary = "创建采购订单")
    @PostMapping("/create-purchase-order")
    public ApiResponse<PurchaseOrder> createOrder(
            @Valid @RequestBody PurchaseOrderCreateRequest request,
            @RequestHeader("X-Tenant-Id") Long tenantId,
            @RequestHeader("X-User-Id") Long userId) {
        return ApiResponse.success(purchaseOrderService.createOrder(request, userId));
    }

    @Operation(summary = "更新采购订单")
    @PutMapping("/update-purchase-order/{id}")
    public ApiResponse<PurchaseOrder> updateOrder(
            @PathVariable Long id,
            @Valid @RequestBody PurchaseOrderCreateRequest request,
            @RequestHeader("X-User-Id") Long userId) {
        return ApiResponse.success(purchaseOrderService.updateOrder(id, request, userId));
    }

    @Operation(summary = "提交采购订单审核")
    @PostMapping("/submit-for-approval/{id}")
    public ApiResponse<Void> submitOrder(@PathVariable Long id) {
        purchaseOrderService.submitForApproval(id);
        return ApiResponse.success();
    }

    @Operation(summary = "审核采购订单")
    @PostMapping("/approve-order/{id}")
    public ApiResponse<Void> approveOrder(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long userId) {
        purchaseOrderService.approveOrder(id, userId);
        return ApiResponse.success();
    }

    @Operation(summary = "取消采购订单")
    @PostMapping("/cancel-order/{id}")
    public ApiResponse<Void> cancelOrder(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long userId) {
        purchaseOrderService.cancelOrder(id, userId);
        return ApiResponse.success();
    }
}
```

### Step 8: 编译验证

```bash
cd services/erp-service && mvn compile
```

Expected: BUILD SUCCESS

### Step 9: Commit

```bash
git add services/erp-service/src/main/java/com/example/erp/entity/PurchaseOrderItem.java \
        services/erp-service/src/main/java/com/example/erp/mapper/PurchaseOrderItemMapper.java \
        services/erp-service/src/main/java/com/example/erp/dto/PurchaseOrderCreateRequest.java \
        services/erp-service/src/main/java/com/example/erp/dto/PurchaseOrderVO.java \
        services/erp-service/src/main/java/com/example/erp/service/PurchaseOrderService.java \
        services/erp-service/src/main/java/com/example/erp/controller/PurchaseOrderController.java \
        db-scripts/10-purchase-order-item-table.sql
git commit -m "feat(erp): 添加采购订单管理后端API（含明细）"
```

---

## Task 2: 采购订单管理（前端）

**Files:**
- Create: `frontend/src/views/erp/purchase-order/index.vue`
- Modify: `frontend/src/api/erp.js`
- Modify: `frontend/src/router/index.js`

### Step 1: 添加采购订单API函数

修改 `frontend/src/api/erp.js`，在文件末尾添加：

```javascript
// ==================== 采购订单 ====================

/**
 * 分页查询采购订单
 */
export function getPurchaseOrderPage(params) {
  return get('/erp/purchase-orders/get-purchase-order-page', params)
}

/**
 * 获取采购订单详情
 */
export function getPurchaseOrderDetail(id) {
  return get(`/erp/purchase-orders/get-purchase-order/${id}`)
}

/**
 * 创建采购订单
 */
export function createPurchaseOrder(data) {
  return post('/erp/purchase-orders/create-purchase-order', data)
}

/**
 * 更新采购订单
 */
export function updatePurchaseOrder(id, data) {
  return put(`/erp/purchase-orders/update-purchase-order/${id}`, data)
}

/**
 * 提交采购订单审核
 */
export function submitPurchaseOrder(id) {
  return post(`/erp/purchase-orders/submit-for-approval/${id}`)
}

/**
 * 审核采购订单
 */
export function approvePurchaseOrder(id) {
  return post(`/erp/purchase-orders/approve-order/${id}`)
}

/**
 * 取消采购订单
 */
export function cancelPurchaseOrder(id) {
  return post(`/erp/purchase-orders/cancel-order/${id}`)
}
```

### Step 2: 添加路由

修改 `frontend/src/router/index.js`，在 erp 路由分组中添加：

```javascript
{
  path: 'erp/purchase-order',
  name: 'PurchaseOrder',
  component: () => import('@/views/erp/purchase-order/index.vue'),
  meta: { title: '采购订单', icon: 'ShoppingCart' }
}
```

### Step 3: 创建采购订单页面

创建 `frontend/src/views/erp/purchase-order/index.vue`，参照 `sales-order/index.vue` 的完整结构，主要区别：
- 搜索条件：订单编号、供应商（下拉）、订单状态
- 表格列：订单编号、供应商名称、仓库名称、订单金额、实付金额、状态、创建时间
- 新建/编辑对话框：供应商选择、仓库选择、预计到货日期、经手人、备注、订单明细（商品选择、数量、单价、折扣）
- 详情对话框：展示订单完整信息和明细列表
- 操作按钮：编辑（草稿）、提交审核（草稿）、审核（待审核）、取消（非已完成/已取消）

### Step 4: 前端编译验证

```bash
cd frontend && pnpm run build
```

Expected: build success

### Step 5: Commit

```bash
git add frontend/src/api/erp.js \
        frontend/src/router/index.js \
        frontend/src/views/erp/purchase-order/index.vue
git commit -m "feat(erp): 添加采购订单管理前端页面"
```

---

## Task 3: 发票管理（后端）

**Files:**
- Create: `services/finance-service/src/main/java/com/example/finance/service/InvoiceService.java`
- Create: `services/finance-service/src/main/java/com/example/finance/controller/InvoiceController.java`

### Step 1: 创建 InvoiceService

创建 `services/finance-service/src/main/java/com/example/finance/service/InvoiceService.java`：

```java
package com.example.finance.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.common.core.exception.BusinessException;
import com.example.finance.entity.Invoice;
import com.example.finance.mapper.InvoiceMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@Service
@RequiredArgsConstructor
public class InvoiceService extends ServiceImpl<InvoiceMapper, Invoice> {

    public Page<Invoice> getPage(Long tenantId, int page, int size,
                                  Integer invoiceType, Integer invoiceDirection, Integer status) {
        LambdaQueryWrapper<Invoice> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Invoice::getTenantId, tenantId)
               .eq(Invoice::getDeleted, 0);
        if (invoiceType != null) {
            wrapper.eq(Invoice::getInvoiceType, invoiceType);
        }
        if (invoiceDirection != null) {
            wrapper.eq(Invoice::getInvoiceDirection, invoiceDirection);
        }
        if (status != null) {
            wrapper.eq(Invoice::getStatus, status);
        }
        wrapper.orderByDesc(Invoice::getCreatedAt);
        return page(new Page<>(page, size), wrapper);
    }

    @Transactional
    public Invoice create(Invoice invoice) {
        invoice.setInvoiceNo(generateInvoiceNo(invoice.getTenantId()));
        invoice.setStatus(0); // 待开票
        save(invoice);
        log.info("创建发票: {}", invoice.getInvoiceNo());
        return invoice;
    }

    @Transactional
    public void issue(Long id) {
        Invoice invoice = getById(id);
        if (invoice == null) {
            throw new BusinessException("发票不存在");
        }
        if (invoice.getStatus() != 0) {
            throw new BusinessException("只有待开票状态的发票可以开具");
        }
        invoice.setStatus(1); // 已开票
        updateById(invoice);
        log.info("开具发票: {}", invoice.getInvoiceNo());
    }

    @Transactional
    public void voidInvoice(Long id) {
        Invoice invoice = getById(id);
        if (invoice == null) {
            throw new BusinessException("发票不存在");
        }
        if (invoice.getStatus() == 2) {
            throw new BusinessException("发票已作废");
        }
        invoice.setStatus(2); // 已作废
        updateById(invoice);
        log.info("作废发票: {}", invoice.getInvoiceNo());
    }

    private String generateInvoiceNo(Long tenantId) {
        String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String prefix = "FP" + dateStr;
        Long count = lambdaQuery()
            .eq(Invoice::getTenantId, tenantId)
            .likeRight(Invoice::getInvoiceNo, prefix)
            .count();
        return prefix + String.format("%04d", count + 1);
    }
}
```

### Step 2: 创建 InvoiceController

创建 `services/finance-service/src/main/java/com/example/finance/controller/InvoiceController.java`：

```java
package com.example.finance.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.common.core.result.ApiResponse;
import com.example.common.core.result.PageResult;
import com.example.finance.entity.Invoice;
import com.example.finance.service.InvoiceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "发票管理", description = "发票增删改查及开票/作废API")
@RestController
@RequestMapping("/api/finance/invoices")
@RequiredArgsConstructor
public class InvoiceController {

    private final InvoiceService invoiceService;

    @Operation(summary = "分页查询发票")
    @GetMapping("/get-invoice-page")
    public ApiResponse<PageResult<Invoice>> getInvoicePage(
            @RequestHeader("X-Tenant-Id") Long tenantId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Integer invoiceType,
            @RequestParam(required = false) Integer invoiceDirection,
            @RequestParam(required = false) Integer status) {
        Page<Invoice> result = invoiceService.getPage(tenantId, page, size, invoiceType, invoiceDirection, status);
        PageResult<Invoice> pageResult = PageResult.of(result.getRecords(), result.getTotal(), result.getCurrent(), result.getSize());
        return ApiResponse.success(pageResult);
    }

    @Operation(summary = "获取发票详情")
    @GetMapping("/get-invoice/{id}")
    public ApiResponse<Invoice> getInvoice(@PathVariable Long id) {
        return ApiResponse.success(invoiceService.getById(id));
    }

    @Operation(summary = "创建发票")
    @PostMapping("/create-invoice")
    public ApiResponse<Invoice> createInvoice(
            @RequestBody Invoice invoice,
            @RequestHeader("X-Tenant-Id") Long tenantId,
            @RequestHeader("X-User-Id") Long userId) {
        invoice.setTenantId(tenantId);
        invoice.setCreatedBy(userId);
        return ApiResponse.success(invoiceService.create(invoice));
    }

    @Operation(summary = "开具发票")
    @PostMapping("/issue-invoice/{id}")
    public ApiResponse<Void> issueInvoice(@PathVariable Long id) {
        invoiceService.issue(id);
        return ApiResponse.success();
    }

    @Operation(summary = "作废发票")
    @PostMapping("/void-invoice/{id}")
    public ApiResponse<Void> voidInvoice(@PathVariable Long id) {
        invoiceService.voidInvoice(id);
        return ApiResponse.success();
    }

    @Operation(summary = "删除发票")
    @DeleteMapping("/delete-invoice/{id}")
    public ApiResponse<Void> deleteInvoice(@PathVariable Long id) {
        invoiceService.removeById(id);
        return ApiResponse.success();
    }
}
```

### Step 3: 编译验证

```bash
cd services/finance-service && mvn compile
```

### Step 4: Commit

```bash
git add services/finance-service/src/main/java/com/example/finance/service/InvoiceService.java \
        services/finance-service/src/main/java/com/example/finance/controller/InvoiceController.java
git commit -m "feat(finance): 添加发票管理后端API"
```

---

## Task 4: 发票管理（前端）

**Files:**
- Create: `frontend/src/views/finance/invoice/index.vue`
- Modify: `frontend/src/api/finance.js`
- Modify: `frontend/src/router/index.js`

### Step 1: 添加发票API函数

修改 `frontend/src/api/finance.js`，添加：

```javascript
// ==================== 发票管理 ====================

export function getInvoicePage(params) {
  return get('/finance/invoices/get-invoice-page', params)
}

export function getInvoice(id) {
  return get(`/finance/invoices/get-invoice/${id}`)
}

export function createInvoice(data) {
  return post('/finance/invoices/create-invoice', data)
}

export function issueInvoice(id) {
  return post(`/finance/invoices/issue-invoice/${id}`)
}

export function voidInvoice(id) {
  return post(`/finance/invoices/void-invoice/${id}`)
}

export function deleteInvoice(id) {
  return del(`/finance/invoices/delete-invoice/${id}`)
}
```

### Step 2: 添加路由

修改 `frontend/src/router/index.js`，在 finance 路由分组中添加：

```javascript
{
  path: 'finance/invoice',
  name: 'Invoice',
  component: () => import('@/views/finance/invoice/index.vue'),
  meta: { title: '发票管理', icon: 'Tickets' }
}
```

### Step 3: 创建发票管理页面

创建 `frontend/src/views/finance/invoice/index.vue`，参照 `receivable/index.vue` 的CRUD模式：
- 搜索条件：发票类型（下拉：增值税专用/普通/电子）、发票方向（下拉：开票/收票）、状态（下拉）
- 表格列：发票编号、发票类型、发票方向、价税合计、税率、税额、状态、开票日期
- 新建对话框：发票类型、发票方向、关联业务ID、客户/供应商、开票日期、不含税金额、税率、税额（自动计算）、价税合计（自动计算）、备注
- 操作按钮：开具（待开票）、作废（已开票）、删除

### Step 4: 编译验证

```bash
cd frontend && pnpm run build
```

### Step 5: Commit

```bash
git add frontend/src/api/finance.js \
        frontend/src/router/index.js \
        frontend/src/views/finance/invoice/index.vue
git commit -m "feat(finance): 添加发票管理前端页面"
```

---

## Task 5: Dashboard 工作台（后端）

**Files:**
- Create: `services/erp-service/src/main/java/com/example/erp/controller/DashboardController.java`
- Modify: `services/erp-service/src/main/java/com/example/erp/service/SalesOrderService.java` (添加统计方法)
- Modify: `services/erp-service/src/main/java/com/example/erp/service/PurchaseOrderService.java` (添加统计方法)
- Modify: `services/erp-service/src/main/java/com/example/erp/service/ProductService.java` (添加统计方法)
- Modify: `services/erp-service/src/main/java/com/example/erp/service/InventoryService.java` (添加统计方法)
- Modify: `services/finance-service/src/main/java/com/example/finance/service/AccountReceivableService.java` (添加统计方法)
- Modify: `services/finance-service/src/main/java/com/example/finance/service/AccountPayableService.java` (添加统计方法)

### Step 1: 向各Service添加统计方法

**SalesOrderService** 添加：
```java
public long countByStatus(Long tenantId, Integer status) {
    return lambdaQuery()
        .eq(SalesOrder::getTenantId, tenantId)
        .eq(SalesOrder::getOrderStatus, status)
        .eq(SalesOrder::getDeleted, 0)
        .count();
}

public BigDecimal sumTotalAmount(Long tenantId) {
    List<SalesOrder> list = lambdaQuery()
        .eq(SalesOrder::getTenantId, tenantId)
        .ne(SalesOrder::getOrderStatus, 5) // 排除已取消
        .eq(SalesOrder::getDeleted, 0)
        .list();
    return list.stream()
        .map(SalesOrder::getTotalAmount)
        .reduce(BigDecimal.ZERO, BigDecimal::add);
}
```

**PurchaseOrderService** 添加类似方法。

**AccountReceivableService** 添加：
```java
public BigDecimal sumUnreceivedAmount(Long tenantId) {
    List<AccountReceivable> list = lambdaQuery()
        .eq(AccountReceivable::getTenantId, tenantId)
        .in(AccountReceivable::getStatus, 0, 1)
        .eq(AccountReceivable::getDeleted, 0)
        .list();
    return list.stream()
        .map(AccountReceivable::getUnreceivedAmount)
        .reduce(BigDecimal.ZERO, BigDecimal::add);
}
```

**AccountPayableService** 添加类似方法。

### Step 2: 创建 DashboardController

创建 `services/erp-service/src/main/java/com/example/erp/controller/DashboardController.java`：

```java
package com.example.erp.controller;

import com.example.common.core.result.ApiResponse;
import com.example.erp.service.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Tag(name = "工作台统计", description = "Dashboard统计数据API")
@RestController
@RequestMapping("/api/erp/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final SalesOrderService salesOrderService;
    private final PurchaseOrderService purchaseOrderService;
    private final ProductService productService;
    private final InventoryService inventoryService;

    @Operation(summary = "获取工作台统计数据")
    @GetMapping("/stats")
    public ApiResponse<Map<String, Object>> getDashboardStats(
            @RequestHeader("X-Tenant-Id") Long tenantId) {
        Map<String, Object> stats = new HashMap<>();

        // 商品总数
        stats.put("productCount", productService.lambdaQuery()
            .eq(com.example.erp.entity.Product::getTenantId, tenantId)
            .eq(com.example.erp.entity.Product::getDeleted, 0)
            .count());

        // 销售订单统计
        stats.put("salesOrderCount", salesOrderService.lambdaQuery()
            .eq(com.example.erp.entity.SalesOrder::getTenantId, tenantId)
            .eq(com.example.erp.entity.SalesOrder::getDeleted, 0)
            .count());
        stats.put("pendingSalesOrders", salesOrderService.countByStatus(tenantId, 1));
        stats.put("salesOrderAmount", salesOrderService.sumTotalAmount(tenantId));

        // 采购订单统计
        stats.put("purchaseOrderCount", purchaseOrderService.lambdaQuery()
            .eq(com.example.erp.entity.PurchaseOrder::getTenantId, tenantId)
            .eq(com.example.erp.entity.PurchaseOrder::getDeleted, 0)
            .count());
        stats.put("pendingPurchaseOrders", purchaseOrderService.countByStatus(tenantId, 1));
        stats.put("purchaseOrderAmount", purchaseOrderService.sumTotalAmount(tenantId));

        // 库存预警
        stats.put("lowStockCount", inventoryService.getAlertInventories(tenantId).size());

        return ApiResponse.success(stats);
    }
}
```

注意：需要在 InventoryService 中确认 `getAlertInventories` 方法存在（根据之前的探索，该方法已在 InventoryController 中使用）。

### Step 3: 编译验证

```bash
cd services/erp-service && mvn compile
cd services/finance-service && mvn compile
```

### Step 4: Commit

```bash
git add services/erp-service/src/main/java/com/example/erp/controller/DashboardController.java \
        services/erp-service/src/main/java/com/example/erp/service/SalesOrderService.java \
        services/erp-service/src/main/java/com/example/erp/service/PurchaseOrderService.java
git commit -m "feat(erp): 添加Dashboard统计后端API"
```

---

## Task 6: Dashboard 工作台（前端）

**Files:**
- Modify: `frontend/src/views/dashboard/index.vue`
- Modify: `frontend/src/api/erp.js`

### Step 1: 添加Dashboard API

修改 `frontend/src/api/erp.js`，添加：

```javascript
// ==================== 工作台统计 ====================

export function getDashboardStats() {
  return get('/erp/dashboard/stats')
}
```

### Step 2: 重写Dashboard页面

重写 `frontend/src/views/dashboard/index.vue`，替换硬编码数据为真实API调用：

- 统计卡片：商品总数、销售订单数、采购订单数、库存预警数
- 财务概览：应收账款总额、应付账款总额（从 finance API 获取）
- 待办事项：待审核销售订单、待审核采购订单
- 快捷操作：链接到各功能页面

使用 `onMounted` 调用 `getDashboardStats()` 获取数据，用 `ref` 绑定到模板。

### Step 3: 编译验证

```bash
cd frontend && pnpm run build
```

### Step 4: Commit

```bash
git add frontend/src/views/dashboard/index.vue \
        frontend/src/api/erp.js
git commit -m "feat(dashboard): 重写工作台页面，接入真实统计数据"
```

---

## Task 7: 修复前端占位操作

**Files:**
- Modify: `frontend/src/views/finance/receivable/index.vue`
- Modify: `frontend/src/views/finance/payable/index.vue`
- Modify: `frontend/src/views/finance/bank-account/index.vue`

### Step 1: 修复应收账款删除

修改 `frontend/src/views/finance/receivable/index.vue`，找到 `handleDelete` 方法，取消API调用的注释：

```javascript
const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确认删除该应收账款记录？', '提示', { type: 'warning' })
    await deleteReceivable(row.id)  // 取消注释此行
    ElMessage.success('删除成功')
    loadData()
  } catch (e) {
    if (e !== 'cancel') console.error(e)
  }
}
```

同时在 `frontend/src/api/finance.js` 中确认 `deleteReceivable` 函数存在，如不存在则添加。

### Step 2: 修复应付账款删除

同上，修改 `frontend/src/views/finance/payable/index.vue` 的 `handleDelete` 方法。

### Step 3: 修复银行账户余额调整

修改 `frontend/src/views/finance/bank-account/index.vue`，找到 `handleAdjustSubmit` 方法，取消API调用的注释。

需要在后端 `BankAccountService` 中确认余额调整接口存在。如不存在，需要添加：

```java
@Transactional
public void adjustBalance(Long id, BigDecimal amount) {
    BankAccount account = getById(id);
    if (account == null) {
        throw new BusinessException("银行账户不存在");
    }
    account.setBalance(account.getBalance().add(amount));
    updateById(account);
    log.info("调整银行账户余额: ID={}, 调整金额={}", id, amount);
}
```

并在 `FinanceController` 中添加对应接口。

### Step 4: 修复发货编辑占位

修改 `frontend/src/views/erp/sales-shipment/index.vue`，找到 `handleEdit` 方法，实现编辑功能（或改为查看详情并提示"编辑功能暂不支持已审核的发货单"）。

### Step 5: 编译验证

```bash
cd frontend && pnpm run build
```

### Step 6: Commit

```bash
git add frontend/src/views/finance/receivable/index.vue \
        frontend/src/views/finance/payable/index.vue \
        frontend/src/views/finance/bank-account/index.vue \
        frontend/src/views/erp/sales-shipment/index.vue
git commit -m "fix(frontend): 修复各模块占位操作，启用删除和余额调整功能"
```

---

## Task 8: 库存流水页面（前端）

**Files:**
- Create: `frontend/src/views/erp/inventory-flow/index.vue`
- Modify: `frontend/src/router/index.js`

### Step 1: 添加路由

修改 `frontend/src/router/index.js`，在 erp 路由分组中添加：

```javascript
{
  path: 'erp/inventory-flow',
  name: 'InventoryFlow',
  component: () => import('@/views/erp/inventory-flow/index.vue'),
  meta: { title: '库存流水', icon: 'List' }
}
```

### Step 2: 创建库存流水页面

创建 `frontend/src/views/erp/inventory-flow/index.vue`：

- 搜索条件：仓库（下拉）、商品名称、业务类型（下拉：采购入库/销售出库/调拨入库/调拨出库/盘盈/盘亏/退货入库/退货出库）
- 表格列：业务类型、业务单号、商品名称、变动数量（正数绿色/负数红色）、变动前数量、变动后数量、成本价、操作人、创建时间
- 仅查看，无CRUD操作

使用已有的 `getInventoryFlowPage` API（在 `frontend/src/api/erp.js` 中已定义）。

### Step 3: 编译验证

```bash
cd frontend && pnpm run build
```

### Step 4: Commit

```bash
git add frontend/src/views/erp/inventory-flow/index.vue \
        frontend/src/router/index.js
git commit -m "feat(erp): 添加库存流水查看页面"
```

---

## Task 9: 侧边栏菜单更新

**Files:**
- Modify: `frontend/src/layouts/BasicLayout.vue`

### Step 1: 更新侧边栏菜单

修改 `frontend/src/layouts/BasicLayout.vue`，在侧边栏菜单中添加：
- ERP模块下添加：采购订单、库存流水
- 财务模块下添加：发票管理

### Step 2: Commit

```bash
git add frontend/src/layouts/BasicLayout.vue
git commit -m "feat(layout): 更新侧边栏菜单，添加采购订单、库存流水、发票管理"
```

---

## 实施顺序建议

| 优先级 | Task | 说明 | 预估工作量 |
|--------|------|------|-----------|
| P0 | Task 7 | 修复占位操作（最小改动，立即可用） | 小 |
| P0 | Task 1+2 | 采购订单（核心业务缺失） | 中 |
| P1 | Task 3+4 | 发票管理（核心业务缺失） | 中 |
| P1 | Task 5+6 | Dashboard工作台（提升用户体验） | 小 |
| P2 | Task 8 | 库存流水页面（已有API，仅缺页面） | 小 |
| P2 | Task 9 | 侧边栏菜单更新 | 小 |

**建议执行方式：** 使用 subagent-driven-development，每个Task由独立子代理执行，Task间做review检查点。

---

## 完成确认（2026-06-01）

| Task | 描述 | 验证结果 |
|------|------|---------|
| Task 1 | 采购订单管理（后端） | `PurchaseOrderController` + `PurchaseOrderService` + Entity + Mapper 全部存在 |
| Task 2 | 采购订单管理（前端） | `erp/purchase-order/index.vue` 存在，路由 `/erp/purchase-order` 已注册 |
| Task 3 | 发票管理（后端） | `InvoiceController` + `InvoiceService` 全部存在 |
| Task 4 | 发票管理（前端） | `finance/invoice/index.vue` 存在，路由 `/finance/invoice` 已注册 |
| Task 5 | Dashboard 统计（后端） | `DashboardController` + 各 Service 统计方法已实现 |
| Task 6 | Dashboard 统计（前端） | `dashboard/index.vue` 已接入真实 API |
| Task 7 | 修复前端占位操作 | receivable/payable 删除已接入 `deleteReceivable`/`deletePayable` API，bank-account 调账已接入 `adjustBalance` API |
| Task 8 | 库存流水页面 | `erp/inventory-flow/index.vue` 存在，路由 `/erp/inventory-flow` 已注册 |
| Task 9 | 侧边栏菜单更新 | `BasicLayout.vue` 侧边栏已更新 |

**结论：本计划 9 项任务全部完成。** 后续未完成项（销售报价单前端页面、dict-service DB 脚本、侧边栏入口补充等）请参见 `docs/plans/2026-06-01-implementation-status.md`。
