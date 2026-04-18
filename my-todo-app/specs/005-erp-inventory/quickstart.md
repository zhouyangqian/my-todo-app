# Quick Start: 进销存模块 (005-erp-inventory)

**Feature**: ERP 进销存管理系统
**Date**: 2026-01-10
**Purpose**: 开发者快速集成和使用指南

## 概述

本文档帮助开发者快速集成和使用进销存模块的功能。

## 前置条件

- Java 17+
- Spring Boot 3.0
- MySQL 8.0+
- Redis 7.0+
- Vue 3.0 + Ant Design 6.1.4
- Node.js 18+ LTS

## 快速开始

### 1. 添加依赖

在 `erp-service/pom.xml` 中添加：

```xml
<dependencies>
    <!-- Spring Boot Web -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>

    <!-- MyBatis-Plus -->
    <dependency>
        <groupId>com.baomidou</groupId>
        <artifactId>mybatis-plus-boot-starter</artifactId>
        <version>3.5.5</version>
    </dependency>

    <!-- MySQL Driver -->
    <dependency>
        <groupId>com.mysql</groupId>
        <artifactId>mysql-connector-j</artifactId>
        <runtime>true</runtime>
    </dependency>

    <!-- Redis -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-redis</artifactId>
    </dependency>

    <!-- Validation -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-validation</artifactId>
    </dependency>
</dependencies>
```

### 2. 配置应用

在 `application.yml` 中配置：

```yaml
spring:
  application:
    name: erp-service

  datasource:
    url: jdbc:mysql://localhost:3306/saas_db?useSSL=false
    username: root
    password: your_password
    driver-class-name: com.mysql.cj.jdbc.Driver

  redis:
    host: localhost
    port: 6379
    database: 0

mybatis-plus:
  configuration:
    map-underscore-to-camel-case: true
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl
  global-config:
    db-config:
      logic-delete-field: deleted
      logic-delete-value: 1
      logic-not-delete-value: 0

server:
  port: 8081
```

### 3. 配置多租户

```java
@Configuration
public class MybatisPlusConfig {

    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();

        // 租户插件
        TenantLineInnerInterceptor tenantInterceptor = new TenantLineInnerInterceptor();
        tenantInterceptor.setTenantLineHandler(new TenantLineHandler() {
            @Override
            public Expression getTenantId() {
                Long tenantId = TenantContext.getTenantId();
                if (tenantId == null) {
                    throw new RuntimeException("租户上下文缺失");
                }
                return new LongValue(tenantId);
            }

            @Override
            public boolean ignoreTable(String tableName) {
                return "system_config".equals(tableName);
            }
        });

        interceptor.addInnerInterceptor(tenantInterceptor);
        return interceptor;
    }
}
```

### 4. 创建采购订单

```java
@RestController
@RequestMapping("/api/v1/purchase-orders")
public class PurchaseOrderController {

    @Autowired
    private PurchaseOrderService purchaseOrderService;

    @PostMapping
    @RequirePermission("purchase:order:create")
    public Result<PurchaseOrder> create(@RequestBody PurchaseOrderDTO dto) {
        PurchaseOrder order = purchaseOrderService.createOrder(dto);
        return Result.success(order);
    }

    @GetMapping
    @RequirePermission("purchase:order:query")
    public Result<Page<PurchaseOrder>> list(
            @RequestParam(required = false) Long supplierId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {
        return Result.success(purchaseOrderService.queryPage(supplierId, status, page, size));
    }
}
```

### 5. 库存并发安全

```java
@Service
public class InventoryService {

    @Autowired
    private InventoryMapper inventoryMapper;

    @Transactional
    public void deductInventory(Long productId, Long warehouseId, Integer quantity) {
        // 乐观锁扣减
        int updated = inventoryMapper.deductInventory(productId, warehouseId, quantity);
        if (updated == 0) {
            throw new BusinessException("库存不足或数据已被修改，请重试");
        }

        // 记录流水
        InventoryTransaction transaction = new InventoryTransaction();
        transaction.setProductId(productId);
        transaction.setWarehouseId(warehouseId);
        transaction.setTransactionType("SALES_OUT");
        transaction.setQuantity(-quantity);
        inventoryTransactionMapper.insert(transaction);
    }
}
```

### 6. 前端 API 客户端

```javascript
// frontend/src/api/purchase.js
import request from '@/utils/request'

export function getSuppliers(params) {
  return request({
    url: '/api/v1/suppliers',
    method: 'get',
    params
  })
}

export function createSupplier(data) {
  return request({
    url: '/api/v1/suppliers',
    method: 'post',
    data
  })
}

export function getPurchaseOrders(params) {
  return request({
    url: '/api/v1/purchase-orders',
    method: 'get',
    params
  })
}

export function createPurchaseOrder(data) {
  return request({
    url: '/api/v1/purchase-orders',
    method: 'post',
    data
  })
}

export function approveOrder(id, data) {
  return request({
    url: `/api/v1/purchase-orders/${id}/approve`,
    method: 'post',
    data
  })
}
```

### 7. 前端采购订单页面

```vue
<template>
  <a-card title="采购订单">
    <template #extra>
      <a-button type="primary" @click="showCreateModal">
        创建订单
      </a-button>
    </template>

    <a-table
      :columns="columns"
      :data-source="orders"
      :loading="loading"
      :pagination="pagination"
      @change="handleTableChange"
    >
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'status'">
          <a-tag :color="getStatusColor(record.status)">
            {{ getStatusText(record.status) }}
          </a-tag>
        </template>
        <template v-if="column.key === 'action'">
          <a-space>
            <a-button size="small" @click="viewOrder(record)">查看</a-button>
            <a-button
              v-if="record.status === 'PENDING'"
              size="small"
              type="primary"
              @click="approveOrder(record)"
            >
              审批
            </a-button>
          </a-space>
        </template>
      </template>
    </a-table>
  </a-card>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getPurchaseOrders, approveOrder } from '@/api/purchase'

const orders = ref([])
const loading = ref(false)

const columns = [
  { title: '订单号', dataIndex: 'orderNo', key: 'orderNo' },
  { title: '供应商', dataIndex: 'supplierName', key: 'supplierName' },
  { title: '订单日期', dataIndex: 'orderDate', key: 'orderDate' },
  { title: '总金额', dataIndex: 'totalAmount', key: 'totalAmount' },
  { title: '状态', dataIndex: 'status', key: 'status' },
  { title: '操作', key: 'action' }
]

const fetchOrders = async () => {
  loading.value = true
  try {
    const { data } = await getPurchaseOrders({ page: 1, size: 20 })
    orders.value = data.records
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchOrders()
})
</script>
```

## 常见问题

### Q: 如何实现库存并发安全？

A: 使用 MyBatis-Plus 的 `@Version` 注解实现乐观锁：

```java
@TableName("inventory")
public class Inventory {
    @Version
    private Integer version;
}
```

更新时自动检查版本号，防止并发冲突。

### Q: 如何实现租户数据隔离？

A: 使用 MyBatis-Plus 的租户插件，自动在 SQL 中添加租户条件。参考 `research.md` 中的详细实现。

### Q: 如何生成单据编号？

A: 使用数据库序列 + Redis INCR 的混合策略：

```java
public String generateNumber(String documentType) {
    String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
    Long sequence = redisTemplate.opsForValue().increment("doc:seq:" + documentType + ":" + dateStr);
    return "PO" + dateStr + String.format("%04d", sequence);
}
```

### Q: 如何实现批次 FIFO？

A: 按生产日期排序查询可用批次：

```java
List<InventoryBatch> batches = batchMapper.selectList(
    new LambdaQueryWrapper<InventoryBatch>()
        .eq(InventoryBatch::getProductId, productId)
        .gt(InventoryBatch::getQuantity, 0)
        .orderByAsc(InventoryBatch::getProductionDate)
);
```

## 下一步

- 查看完整 API 文档: `contracts/`
- 了解数据模型: `data-model.md`
- 查看技术研究: `research.md`
