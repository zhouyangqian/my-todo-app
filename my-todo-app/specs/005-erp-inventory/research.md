# Technical Research: 进销存模块 (005-erp-inventory)

**Feature**: ERP 进销存管理系统
**Date**: 2026-01-10
**Purpose**: 技术选型与最佳实践研究

## 研究概述

本文档记录了进销存模块的技术研究和决策，包括并发控制、多租户隔离、批次管理、报表优化、分布式事务等关键技术问题的解决方案。

## 技术决策

### 1. 库存并发控制策略

**决策**: 混合策略 - 乐观锁 + Redis 分布式锁

**理由**:
- **乐观锁**（基于 MyBatis-Plus `@Version`）：适用于低冲突场景，性能好，实现简单
- **分布式锁**（Redis SET NX + Lua）：适用于高冲突场景（如秒杀、大促），保证绝对一致性
- 采用分层策略：常规操作用乐观锁，关键操作用分布式锁

**实现方案**:

```java
// 实体类 - 乐观锁
@TableName("inventory")
public class Inventory {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long productId;
    private Long warehouseId;
    private Integer quantity;

    @Version  // MyBatis-Plus 乐观锁注解
    private Integer version;

    // 锁定库存（用于订单锁定）
    private Integer lockedQuantity;
}

// Service - 库存扣减（乐观锁）
public boolean deductInventory(Long productId, Long warehouseId, Integer quantity) {
    return inventoryMapper.deductInventory(productId, warehouseId, quantity) > 0;
}

// Mapper XML
<update id="deductInventory">
    UPDATE inventory
    SET quantity = quantity - #{quantity},
        version = version + 1
    WHERE product_id = #{productId}
      AND warehouse_id = #{warehouseId}
      AND quantity >= #{quantity}
      AND version = #{version}
</update>

// Service - 分布式锁（关键操作）
public boolean criticalDeductInventory(Long productId, Integer quantity) {
    String lockKey = "inventory:lock:" + productId;
    String lockValue = UUID.randomUUID().toString();

    try {
        // 获取锁（30秒过期）
        Boolean locked = redisTemplate.opsForValue()
            .setIfAbsent(lockKey, lockValue, 30, TimeUnit.SECONDS);

        if (Boolean.FALSE.equals(locked)) {
            throw new BusinessException("操作频繁，请稍后重试");
        }

        // 执行库存操作
        return deductInventory(productId, quantity);

    } finally {
        // 释放锁（Lua 脚本保证原子性）
        String script = "if redis.call('get', KEYS[1]) == ARGV[1] then " +
                        "return redis.call('del', KEYS[1]) else return 0 end";
        redisTemplate.execute(new DefaultRedisScript<>(script, Long.class),
                             Collections.singletonList(lockKey), lockValue);
    }
}
```

**替代方案**:
- **悲观锁**（SELECT FOR UPDATE）：实现简单但性能差，高并发下数据库压力大
- **纯分布式锁**：所有操作都用锁，性能损失大

---

### 2. 多租户数据隔离实现

**决策**: MyBatis-Plus 租户插件 + ThreadLocal 租户上下文

**理由**:
- MyBatis-Plus 内置租户插件，自动在 SQL 中添加租户过滤
- 透明化业务代码，无需手动添加租户条件
- 支持租户字段自动填充

**实现方案**:

```java
// 1. 租户上下文 - ThreadLocal
public class TenantContext {
    private static final ThreadLocal<Long> TENANT_ID = new ThreadLocal<>();

    public static void setTenantId(Long tenantId) {
        TENANT_ID.set(tenantId);
    }

    public static Long getTenantId() {
        return TENANT_ID.get();
    }

    public static void clear() {
        TENANT_ID.remove();
    }
}

// 2. 基础实体 - 所有表继承
@TableName("product")
public class Product extends TenantBaseEntity {
    private String code;
    private String name;
    // ...
}

// 租户基类
public abstract class TenantBaseEntity {
    @TableField(fill = FieldFill.INSERT)
    private Long tenantId;  // 租户 ID
}

// 3. MyBatis-Plus 配置 - 租户插件
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
                // 忽略不需要租户隔离的表（如系统配置表）
                return "system_config".equals(tableName);
            }
        });

        interceptor.addInnerInterceptor(tenantInterceptor);
        return interceptor;
    }
}

// 4. 网关过滤器 - 提取租户 ID
@Component
public class TenantFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String tenantId = exchange.getRequest().getHeaders().getFirst("X-Tenant-Id");

        if (tenantId != null) {
            // 添加到下游请求
            ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                .header("X-Tenant-Id", tenantId)
                .build();

            return chain.filter(exchange.mutate().request(mutatedRequest).build());
        }

        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return -200; // 最高优先级
    }
}

// 5. 拦截器 - 设置租户上下文
@Component
public class TenantInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request,
                            HttpServletResponse response,
                            Object handler) {
        String tenantId = request.getHeader("X-Tenant-Id");
        if (tenantId != null) {
            TenantContext.setTenantId(Long.parseLong(tenantId));
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request,
                               HttpServletResponse response,
                               Object handler,
                               Exception ex) {
        TenantContext.clear();
    }
}
```

**Redis 租户隔离**:

```java
// Redis Key 带租户前缀
public class TenantRedisTemplate {

    public String getKey(String key) {
        Long tenantId = TenantContext.getTenantId();
        return "tenant:" + tenantId + ":" + key;
    }

    public void set(String key, Object value) {
        redisTemplate.opsForValue().set(getKey(key), value);
    }

    public Object get(String key) {
        return redisTemplate.opsForValue().get(getKey(key));
    }
}
```

---

### 3. 批次管理 FIFO 实现

**决策**: 数据库索引排序 + 内存缓存辅助

**理由**:
- 批次数量相对有限（一般 < 100/商品），数据库排序足够
- 用生产日期排序即可实现 FIFO
- 批次信息可缓存减少查询

**数据模型**:

```sql
CREATE TABLE inventory_batch (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    product_id BIGINT NOT NULL,
    warehouse_id BIGINT NOT NULL,
    batch_no VARCHAR(50) NOT NULL,
    production_date DATE NOT NULL,
    expiry_date DATE,
    quantity INT NOT NULL,
    tenant_id BIGINT NOT NULL,
    INDEX idx_product_fifo (product_id, warehouse_id, production_date ASC)
);
```

**实现方案**:

```java
// Service - 批次推荐
public List<InventoryBatch> recommendBatches(Long productId,
                                            Long warehouseId,
                                            Integer requiredQuantity) {
    // 按生产日期排序查询可用批次
    List<InventoryBatch> batches = inventoryBatchMapper.selectList(
        new LambdaQueryWrapper<InventoryBatch>()
            .eq(InventoryBatch::getProductId, productId)
            .eq(InventoryBatch::getWarehouseId, warehouseId)
            .gt(InventoryBatch::getQuantity, 0)
            .orderByAsc(InventoryBatch::getProductionDate)  // FIFO
    );

    // 过滤过期批次
    batches = batches.stream()
        .filter(b -> b.getExpiryDate() == null ||
                       b.getExpiryDate().isAfter(LocalDate.now()))
        .collect(Collectors.toList());

    return batches;
}

// 出库批次数组计算
public Map<Long, Integer> allocateBatches(Long productId,
                                         Long warehouseId,
                                         Integer requiredQuantity) {
    List<InventoryBatch> batches = recommendBatches(productId, warehouseId, requiredQuantity);
    Map<Long, Integer> allocation = new LinkedHashMap<>();
    int remaining = requiredQuantity;

    for (InventoryBatch batch : batches) {
        if (remaining <= 0) break;

        int takeQuantity = Math.min(batch.getQuantity(), remaining);
        allocation.put(batch.getId(), takeQuantity);
        remaining -= takeQuantity;
    }

    if (remaining > 0) {
        throw new BusinessException("库存不足，还差 " + remaining);
    }

    return allocation;
}

// 批次出库（事务）
@Transactional
public void deductBatchInventory(Map<Long, Integer> allocation) {
    allocation.forEach((batchId, quantity) -> {
        int updated = inventoryBatchMapper.deductQuantity(batchId, quantity);
        if (updated == 0) {
            throw new BusinessException("批次 " + batchId + " 库存不足");
        }
    });
}

// Mapper
<update id="deductQuantity">
    UPDATE inventory_batch
    SET quantity = quantity - #{quantity}
    WHERE id = #{batchId}
      AND quantity >= #{quantity}
</update>
```

---

### 4. 报表性能优化

**决策**: 分层缓存 + 异步计算 + 数据库优化

**理由**:
- 实时报表：查询优化 + 索引
- 定时报表：异步预计算 + 结果缓存
- 大数据报表：分页 + 游标查询

**实现方案**:

```java
// 1. 数据库优化 - 索引 + 物化视图
// 销售报表汇总表（按日预计算）
CREATE TABLE sales_summary_daily (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tenant_id BIGINT NOT NULL,
    summary_date DATE NOT NULL,
    product_id BIGINT,
    customer_id BIGINT,
    sales_quantity INT DEFAULT 0,
    sales_amount DECIMAL(15,2) DEFAULT 0,
    cost_amount DECIMAL(15,2) DEFAULT 0,
    profit_amount DECIMAL(15,2) DEFAULT 0,
    INDEX idx_tenant_date (tenant_id, summary_date),
    INDEX idx_product (tenant_id, product_id, summary_date),
    UNIQUE KEY uk_summary (tenant_id, summary_date, product_id, customer_id)
);

// 2. 异步预计算（定时任务）
@Scheduled(cron = "0 0 1 * * ?")  // 每天凌晨1点
public void calculateDailySummary() {
    List<Long> tenantIds = tenantService.getAllActiveTenantIds();

    for (Long tenantId : tenantIds) {
        TenantContext.setTenantId(tenantId);
        try {
            calculateYesterdaySummary(tenantId);
        } finally {
            TenantContext.clear();
        }
    }
}

// 3. 报表查询服务
@Service
public class ReportService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    // 实时报表（查询优化）
    public SalesReportDTO getRealtimeReport(ReportQueryDTO query) {
        // 直接查询汇总表
        return salesSummaryMapper.selectByQuery(query);
    }

    // 复杂报表（缓存）
    @Cacheable(value = "report:complex", key = "#query.hashCode()")
    public ComplexReportDTO getComplexReport(ReportQueryDTO query) {
        // 复杂计算逻辑
        return calculateComplexReport(query);
    }

    // 大数据报表（分页 + 游标）
    public Page<ReportRowDTO> getLargeReport(ReportQueryDTO query,
                                             int pageSize,
                                             Long lastId) {
        return reportMapper.selectPageByCursor(query, pageSize, lastId);
    }
}

// 4. 缓存策略
@Configuration
public class CacheConfig {

    @Bean
    public CacheManager cacheManager() {
        Map<String, RedisCacheConfiguration> configMap = new HashMap<>();

        // 报表缓存 - 1小时过期
        configMap.put("report:complex",
            RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofHours(1))
                .serializeValuesWith(
                    RedisSerializationContext.SerializationPair
                        .fromSerializer(new GenericJackson2JsonSerializer())
                ));

        return RedisCacheManager.builder(redisConnectionFactory)
            .cacheDefaults(config)
            .withInitialCacheConfigurations(configMap)
            .build();
    }
}
```

**查询优化**:

```sql
-- 分组查询优化（使用索引）
SELECT
    DATE(so.created_at) AS order_date,
    p.name AS product_name,
    SUM(sod.quantity) AS total_quantity,
    SUM(sod.quantity * sod.price) AS total_amount
FROM sales_order so
INNER JOIN sales_order_item sod ON so.id = sod.order_id
INNER JOIN product p ON sod.product_id = p.id
WHERE so.tenant_id = ?
  AND so.created_at >= ? AND so.created_at < ?
  AND so.status IN ('COMPLETED', 'PARTIAL_DELIVERED')
GROUP BY DATE(so.created_at), p.id
ORDER BY order_date, total_amount DESC;
```

---

### 5. 分布式事务处理

**决策**: 本地事务 + 最大努力 + 补偿机制

**理由**:
- ERP 场景下，跨服务的分布式事务相对较少
- 采购入库等关键操作在单一服务内，用本地事务
- 需要跨服务的场景用最终一致性 + 补偿

**实现方案**:

```java
// 1. 本地事务 - 单一服务内
@Service
public class PurchaseReceiptService {

    @Transactional(rollbackFor = Exception.class)
    public void completeReceipt(Long receiptId) {
        PurchaseReceipt receipt = receiptMapper.selectById(receiptId);

        // 1. 更新库存
        inventoryService.addInventory(receipt.getItems());

        // 2. 生成应付记录
        paymentAccountService.generatePaymentAccount(receipt);

        // 3. 更新订单状态
        purchaseOrderService.updateReceiptStatus(receipt.getOrderId());

        // 4. 记录流水
        auditLogService.log("PURCHASE_RECEIPT_COMPLETE", receiptId);
    }
}

// 2. 最大努力通知 - 跨服务
@Service
public class SalesDeliveryService {

    @Autowired
    private NotificationService notificationService;

    @Transactional(rollbackFor = Exception.class)
    public void completeDelivery(Long deliveryId) {
        SalesDelivery delivery = deliveryMapper.selectById(deliveryId);

        // 1. 扣减库存（本地事务）
        inventoryService.deductInventory(delivery.getItems());

        // 2. 生成应收（本地事务）
        receiptAccountService.generateReceiptAccount(delivery);

        // 3. 发送通知（最大努力）
        try {
            notificationService.sendDeliveryNotification(delivery);
        } catch (Exception e) {
            // 记录失败，异步重试
            log.error("发货通知发送失败", e);
            notificationService.retryAsync(delivery.getId(), "DELIVERY_NOTIFY");
        }
    }
}

// 3. 补偿机制 - 定时任务
@Component
public class CompensationJob {

    @Autowired
    private SalesOrderMapper orderMapper;

    @Autowired
    private InventoryService inventoryService;

    // 每小时执行
    @Scheduled(cron = "0 0 * * * ?")
    public void compensateFailedOrders() {
        // 查询超时未处理的订单
        List<SalesOrder> timeoutOrders = orderMapper.selectTimeoutOrders();

        for (SalesOrder order : timeoutOrders) {
            try {
                // 自动取消订单，释放锁定库存
                salesOrderService.cancelOrder(order.getId(), "SYSTEM_TIMEOUT");
            } catch (Exception e) {
                log.error("订单补偿失败: {}", order.getId(), e);
            }
        }
    }
}
```

---

### 6. 单据编号生成策略

**决策**: 数据库序列 + Redis INCR 备份

**理由**:
- 数据库序列保证唯一性和连续性
- Redis INCR 作为备份提升性能
- 单据规则：前缀 + 日期 + 流水号（如 PO20260110001）

**实现方案**:

```java
@Service
public class NumberGeneratorService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private NumberRuleMapper numberRuleMapper;

    public String generateNumber(String documentType) {
        // 1. 获取编号规则
        NumberRule rule = numberRuleMapper.selectByType(documentType);

        // 2. 生成编号
        LocalDate now = LocalDate.now();
        String prefix = rule.getPrefix();
        String dateStr = now.format(DateTimeFormatter.ofPattern(rule.getDateFormat()));

        // 3. 获取流水号（Redis + 数据库双保险）
        Long sequence = getNextSequence(documentType, now);

        // 4. 格式化编号
        String format = prefix + dateStr + "%0" + rule.getSequenceLength() + "d";
        return String.format(format, sequence);
    }

    private Long getNextSequence(String documentType, LocalDate date) {
        String redisKey = "doc:sequence:" + documentType + ":" + date;

        // Redis INCR
        Long sequence = redisTemplate.opsForValue().increment(redisKey);

        if (sequence == 1) {
            // 首次生成，从数据库初始化
            sequence = initSequenceFromDB(redisKey, documentType, date);
        }

        // 设置过期（第二天清零）
        Duration ttl = Duration.between(LocalDateTime.now(), LocalDate.now().plusDays(1).atStartOfDay());
        redisTemplate.expire(redisKey, ttl.getSeconds(), TimeUnit.SECONDS);

        return sequence;
    }

    private Long initSequenceFromDB(String redisKey, String documentType, LocalDate date) {
        // 从数据库查询当日最大流水号
        Long maxSeq = numberRuleMapper.selectMaxSequence(documentType, date);
        long initValue = (maxSeq == null ? 0 : maxSeq) + 1;

        // 重置 Redis
        redisTemplate.opsForValue().set(redisKey, initValue);

        return initValue;
    }
}
```

---

### 7. 审批流程引擎选择

**决策**: 自研状态机 + 规则引擎

**理由**:
- ERP 审批流程相对简单（通常 1-3 级）
- 自研轻量级，集成简单，维护成本低
- 避免引入 Flowable 等重型框架

**实现方案**:

```java
// 1. 审批规则配置
@TableName("approval_rule")
public class ApprovalRule {
    private Long id;
    private String name;
    private String documentType;  // 单据类型
    private BigDecimal amountThreshold;  // 金额阈值
    private String approvalType;  // 审批类型：SINGLE, MULTI, SEQUENTIAL
    private String approvers;     // 审批人（JSON 数组）
}

// 2. 审批状态机
public enum ApprovalStatus {
    PENDING,    // 待审批
    APPROVED,   // 已批准
    REJECTED,   // 已拒绝
    CANCELLED   // 已取消
}

// 3. 审批服务
@Service
public class ApprovalService {

    @Autowired
    private ApprovalRuleMapper ruleMapper;

    @Autowired
    private ApprovalRecordMapper recordMapper;

    public void submitForApproval(String documentType, Long documentId, BigDecimal amount) {
        // 1. 查找审批规则
        List<ApprovalRule> rules = ruleMapper.selectByTypeAndAmount(documentType, amount);

        if (rules.isEmpty()) {
            // 无需审批，直接通过
            autoApprove(documentType, documentId);
            return;
        }

        // 2. 创建审批记录
        ApprovalRecord record = new ApprovalRecord();
        record.setDocumentType(documentType);
        record.setDocumentId(documentId);
        record.setAmount(amount);
        record.setStatus(ApprovalStatus.PENDING);
        record.setCurrentStep(0);
        record.setTotalSteps(rules.size());
        recordMapper.insert(record);

        // 3. 通知第一个审批人
        notifyApprover(rules.get(0), record);
    }

    public void approve(Long recordId, Long approverId, String comment) {
        ApprovalRecord record = recordMapper.selectById(recordId);

        // 1. 验证权限
        if (!isValidApprover(record, approverId)) {
            throw new BusinessException("无权限审批");
        }

        // 2. 更新审批进度
        int nextStep = record.getCurrentStep() + 1;

        if (nextStep >= record.getTotalSteps()) {
            // 全部审批完成
            record.setStatus(ApprovalStatus.APPROVED);
            recordMapper.updateById(record);

            // 触发业务逻辑（如解锁订单）
            onApprovalComplete(record);
        } else {
            // 进入下一步
            record.setCurrentStep(nextStep);
            recordMapper.updateById(record);

            // 通知下一审批人
            ApprovalRule nextRule = getApprovalRule(record.getDocumentType(), nextStep);
            notifyApprover(nextRule, record);
        }
    }

    public void reject(Long recordId, Long approverId, String reason) {
        ApprovalRecord record = recordMapper.selectById(recordId);

        if (!isValidApprover(record, approverId)) {
            throw new BusinessException("无权限审批");
        }

        record.setStatus(ApprovalStatus.REJECTED);
        record.setRejectReason(reason);
        recordMapper.updateById(record);

        // 触发拒绝逻辑
        onApprovalRejected(record);
    }
}
```

---

### 8. 与 003-user-auth 集成

**集成方式**: JWT Token + 网关验证

```java
// 网关 - JWT 验证
@Component
public class JwtAuthenticationFilter implements GlobalFilter {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String token = exchange.getRequest().getHeaders().getFirst("Authorization");

        if (token == null || !token.startsWith("Bearer ")) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        // 调用认证服务验证 Token
        return authClient.validateToken(token.substring(7))
            .flatMap(response -> {
                if (response.isValid()) {
                    // 添加用户信息到请求头
                    ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                        .header("X-User-Id", String.valueOf(response.getUserId()))
                        .header("X-Tenant-Id", String.valueOf(response.getTenantId()))
                        .header("X-User-Roles", String.join(",", response.getRoles()))
                        .build();
                    return chain.filter(exchange.mutate().request(mutatedRequest).build());
                } else {
                    exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                    return exchange.getResponse().setComplete();
                }
            });
    }
}
```

---

### 9. 与 002-permission-module 集成

**集成方式**: 注解 + 拦截器

```java
// 权限注解
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequirePermission {
    String value();
    String logical() default "AND";  // AND / OR
}

// Controller 使用
@RestController
@RequestMapping("/api/v1/purchase/orders")
public class PurchaseOrderController {

    @GetMapping
    @RequirePermission("purchase:order:query")
    public Result<List<PurchaseOrder>> list() {
        // ...
    }

    @PostMapping
    @RequirePermission("purchase:order:create")
    public Result<PurchaseOrder> create(@RequestBody PurchaseOrderDTO dto) {
        // ...
    }
}

// 权限拦截器
@Component
public class PermissionInterceptor implements HandlerInterceptor {

    @Autowired
    private PermissionClient permissionClient;

    @Override
    public boolean preHandle(HttpServletRequest request,
                            HttpServletResponse response,
                            Object handler) {
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        HandlerMethod method = (HandlerMethod) handler;
        RequirePermission annotation = method.getMethodAnnotation(RequirePermission.class);

        if (annotation == null) {
            return true;
        }

        // 获取用户权限
        Long userId = Long.parseLong(request.getHeader("X-User-Id"));
        List<String> userPermissions = permissionClient.getUserPermissions(userId);

        // 检查权限
        boolean hasPermission = checkPermission(userPermissions, annotation.value());

        if (!hasPermission) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return false;
        }

        return true;
    }
}
```

---

## 总结

本模块技术选型遵循以下原则：

1. **成熟稳定优先**：选择经过验证的技术方案，降低风险
2. **性能与一致性平衡**：根据场景选择合适的并发控制策略
3. **透明化多租户**：通过框架特性实现租户隔离，减少业务代码侵入
4. **渐进式优化**：先实现功能，再优化性能
5. **避免过度设计**：审批、报表等功能采用轻量级方案

## 下一步

- 查看 `data-model.md` 了解数据库设计
- 查看 `contracts/` 了解 API 契约
- 查看 `quickstart.md` 了解快速开始指南
