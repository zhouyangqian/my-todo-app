# Tests: 进销存模块

**Feature Branch**: `005-erp-inventory`
**Generated**: 2026-04-07

## 测试策略概览

| 层级 | 框架 | 覆盖率目标 |
|------|------|------------|
| 单元测试 | JUnit 5 + Mockito | ≥ 80% |
| 集成测试 | Spring Boot Test + Testcontainers | ≥ 75% |
| API测试 | MockMvc | 100% 端点 |
| 前端测试 | Vitest + Vue Test Utils | ≥ 70% |

---

## 单元测试

### PurchaseOrderServiceTest
```java
@ExtendWith(MockitoExtension.class)
class PurchaseOrderServiceTest {

    @Mock
    private PurchaseOrderMapper orderMapper;

    @Mock
    private ProductMapper productMapper;

    @Mock
    private SupplierMapper supplierMapper;

    @Mock
    private MqProducer mqProducer;

    @InjectMocks
    private PurchaseOrderService orderService;

    @Test
    @DisplayName("创建采购订单 - 成功")
    void createOrder_success() {
        // Given
        PurchaseOrderCreateRequest request = new PurchaseOrderCreateRequest();
        request.setSupplierId(1L);
        request.setItems(List.of(
            new OrderItem(101L, 100, new BigDecimal("10.00")),
            new OrderItem(102L, 50, new BigDecimal("20.00"))
        ));

        when(supplierMapper.selectById(1L)).thenReturn(new Supplier());
        when(productMapper.selectById(101L)).thenReturn(new Product());
        when(productMapper.selectById(102L)).thenReturn(new Product());

        // When
        Long orderId = orderService.createOrder(request);

        // Then
        assertNotNull(orderId);
        verify(orderMapper).insert(any(PurchaseOrder.class));
    }

    @Test
    @DisplayName("创建采购订单 - 供应商不存在")
    void createOrder_supplierNotFound() {
        // Given
        PurchaseOrderCreateRequest request = new PurchaseOrderCreateRequest();
        request.setSupplierId(999L);

        when(supplierMapper.selectById(999L)).thenReturn(null);

        // When & Then
        assertThrows(BusinessException.class,
            () -> orderService.createOrder(request));
    }

    @Test
    @DisplayName("审批采购订单 - 通过")
    void approveOrder_pass() {
        // Given
        Long orderId = 1L;
        PurchaseOrder order = new PurchaseOrder();
        order.setId(orderId);
        order.setStatus(OrderStatus.PENDING);

        when(orderMapper.selectById(orderId)).thenReturn(order);

        // When
        orderService.approveOrder(orderId, true);

        // Then
        assertEquals(OrderStatus.APPROVED, order.getStatus());
        verify(mqProducer).sendOrderApprovedEvent(orderId);
    }

    @Test
    @DisplayName("审批采购订单 - 拒绝")
    void approveOrder_reject() {
        // Given
        Long orderId = 1L;
        PurchaseOrder order = new PurchaseOrder();
        order.setId(orderId);
        order.setStatus(OrderStatus.PENDING);

        when(orderMapper.selectById(orderId)).thenReturn(order);

        // When
        orderService.approveOrder(orderId, false, "供应商资质不符");

        // Then
        assertEquals(OrderStatus.REJECTED, order.getStatus());
        assertEquals("供应商资质不符", order.getRejectReason());
    }

    @Test
    @DisplayName("审批采购订单 - 状态错误")
    void approveOrder_invalidStatus() {
        // Given
        Long orderId = 1L;
        PurchaseOrder order = new PurchaseOrder();
        order.setStatus(OrderStatus.COMPLETED);

        when(orderMapper.selectById(orderId)).thenReturn(order);

        // When & Then
        assertThrows(BusinessException.class,
            () -> orderService.approveOrder(orderId, true));
    }
}
```

### SalesOrderServiceTest
```java
@ExtendWith(MockitoExtension.class)
class SalesOrderServiceTest {

    @Mock
    private SalesOrderMapper orderMapper;

    @Mock
    private InventoryService inventoryService;

    @Mock
    private CustomerMapper customerMapper;

    @InjectMocks
    private SalesOrderService orderService;

    @Test
    @DisplayName("创建销售订单 - 成功")
    void createOrder_success() {
        // Given
        SalesOrderCreateRequest request = new SalesOrderCreateRequest();
        request.setCustomerId(1L);
        request.setItems(List.of(
            new OrderItem(101L, 10, new BigDecimal("100.00"))
        ));

        when(customerMapper.selectById(1L)).thenReturn(new Customer());
        when(inventoryService.checkStock(101L, 10)).thenReturn(true);

        // When
        Long orderId = orderService.createOrder(request);

        // Then
        assertNotNull(orderId);
    }

    @Test
    @DisplayName("创建销售订单 - 库存不足")
    void createOrder_insufficientStock() {
        // Given
        SalesOrderCreateRequest request = new SalesOrderCreateRequest();
        request.setItems(List.of(new OrderItem(101L, 1000, BigDecimal.ONE)));

        when(inventoryService.checkStock(101L, 1000)).thenReturn(false);

        // When & Then
        assertThrows(InsufficientStockException.class,
            () -> orderService.createOrder(request));
    }

    @Test
    @DisplayName("计算订单金额")
    void calculateTotalAmount() {
        // Given
        List<OrderItem> items = List.of(
            new OrderItem(1L, 10, new BigDecimal("100.00")),  // 1000
            new OrderItem(2L, 5, new BigDecimal("200.00"))    // 1000
        );

        // When
        BigDecimal total = orderService.calculateTotalAmount(items);

        // Then
        assertEquals(new BigDecimal("2000.00"), total);
    }
}
```

### InventoryServiceTest
```java
@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {

    @Mock
    private InventoryMapper inventoryMapper;

    @Mock
    private InventoryFlowMapper flowMapper;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @InjectMocks
    private InventoryService inventoryService;

    @Test
    @DisplayName("检查库存 - 充足")
    void checkStock_sufficient() {
        // Given
        Long productId = 1L;
        Integer quantity = 100;

        Inventory inventory = new Inventory();
        inventory.setQuantity(200);

        when(inventoryMapper.selectByProductId(productId)).thenReturn(inventory);

        // When
        boolean result = inventoryService.checkStock(productId, quantity);

        // Then
        assertTrue(result);
    }

    @Test
    @DisplayName("检查库存 - 不足")
    void checkStock_insufficient() {
        // Given
        Long productId = 1L;
        Integer quantity = 300;

        Inventory inventory = new Inventory();
        inventory.setQuantity(200);

        when(inventoryMapper.selectByProductId(productId)).thenReturn(inventory);

        // When
        boolean result = inventoryService.checkStock(productId, quantity);

        // Then
        assertFalse(result);
    }

    @Test
    @DisplayName("入库 - 增加库存")
    void inbound_increaseStock() {
        // Given
        Long productId = 1L;
        Long warehouseId = 1L;
        Integer quantity = 100;
        String batchNo = "B20260407001";

        // When
        inventoryService.inbound(productId, warehouseId, quantity, batchNo);

        // Then
        verify(inventoryMapper).increaseQuantity(productId, warehouseId, quantity);
        verify(flowMapper).insert(argThat(flow ->
            flow.getType() == FlowType.INBOUND &&
            flow.getQuantity().equals(quantity)
        ));
    }

    @Test
    @DisplayName("出库 - 扣减库存")
    void outbound_decreaseStock() {
        // Given
        Long productId = 1L;
        Long warehouseId = 1L;
        Integer quantity = 50;

        Inventory inventory = new Inventory();
        inventory.setQuantity(100);

        when(inventoryMapper.selectByProductIdAndWarehouse(productId, warehouseId))
            .thenReturn(inventory);

        // When
        inventoryService.outbound(productId, warehouseId, quantity, "销售出库");

        // Then
        verify(inventoryMapper).decreaseQuantity(productId, warehouseId, quantity);
    }

    @Test
    @DisplayName("出库 - 库存不足抛异常")
    void outbound_insufficientStock() {
        // Given
        Long productId = 1L;
        Long warehouseId = 1L;
        Integer quantity = 200;

        Inventory inventory = new Inventory();
        inventory.setQuantity(100);

        when(inventoryMapper.selectByProductIdAndWarehouse(productId, warehouseId))
            .thenReturn(inventory);

        // When & Then
        assertThrows(InsufficientStockException.class,
            () -> inventoryService.outbound(productId, warehouseId, quantity, "销售出库"));
    }

    @Test
    @DisplayName("库存预警检查")
    void checkAlert() {
        // Given
        Inventory inventory = new Inventory();
        inventory.setProductId(1L);
        inventory.setQuantity(10);
        inventory.setAlertQuantity(20);

        // When
        boolean needAlert = inventoryService.checkAlert(inventory);

        // Then
        assertTrue(needAlert);
    }
}
```

### InventoryTransferServiceTest
```java
@ExtendWith(MockitoExtension.class)
class InventoryTransferServiceTest {

    @Mock
    private InventoryMapper inventoryMapper;

    @Mock
    private InventoryFlowMapper flowMapper;

    @InjectMocks
    private InventoryTransferService transferService;

    @Test
    @DisplayName("库存调拨 - 成功")
    void transfer_success() {
        // Given
        Long productId = 1L;
        Long fromWarehouse = 1L;
        Long toWarehouse = 2L;
        Integer quantity = 50;

        Inventory fromInventory = new Inventory();
        fromInventory.setQuantity(100);

        when(inventoryMapper.selectByProductIdAndWarehouse(productId, fromWarehouse))
            .thenReturn(fromInventory);

        // When
        transferService.transfer(productId, fromWarehouse, toWarehouse, quantity);

        // Then
        verify(inventoryMapper).decreaseQuantity(productId, fromWarehouse, quantity);
        verify(inventoryMapper).increaseQuantity(productId, toWarehouse, quantity);
    }

    @Test
    @DisplayName("库存调拨 - 源仓库库存不足")
    void transfer_insufficientStock() {
        // Given
        Long productId = 1L;
        Long fromWarehouse = 1L;
        Long toWarehouse = 2L;
        Integer quantity = 200;

        Inventory fromInventory = new Inventory();
        fromInventory.setQuantity(100);

        when(inventoryMapper.selectByProductIdAndWarehouse(productId, fromWarehouse))
            .thenReturn(fromInventory);

        // When & Then
        assertThrows(InsufficientStockException.class,
            () -> transferService.transfer(productId, fromWarehouse, toWarehouse, quantity));
    }
}
```

### InventoryCheckServiceTest
```java
@ExtendWith(MockitoExtension.class)
class InventoryCheckServiceTest {

    @Mock
    private InventoryMapper inventoryMapper;

    @Mock
    private InventoryCheckMapper checkMapper;

    @InjectMocks
    private InventoryCheckService checkService;

    @Test
    @DisplayName("库存盘点 - 创建盘点单")
    void createCheck() {
        // Given
        Long warehouseId = 1L;

        // When
        Long checkId = checkService.createCheck(warehouseId);

        // Then
        assertNotNull(checkId);
        verify(checkMapper).insert(any(InventoryCheck.class));
    }

    @Test
    @DisplayName("库存盘点 - 录入实盘数量")
    void recordActualQuantity() {
        // Given
        Long checkId = 1L;
        Long productId = 101L;
        Integer actualQuantity = 95;

        InventoryCheck check = new InventoryCheck();
        check.setStatus(CheckStatus.IN_PROGRESS);

        when(checkMapper.selectById(checkId)).thenReturn(check);

        // When
        checkService.recordActualQuantity(checkId, productId, actualQuantity);

        // Then
        verify(checkMapper).updateCheckItem(checkId, productId, actualQuantity);
    }

    @Test
    @DisplayName("库存盘点 - 完成盘点调整库存")
    void completeCheck() {
        // Given
        Long checkId = 1L;

        InventoryCheck check = new InventoryCheck();
        check.setStatus(CheckStatus.IN_PROGRESS);

        List<CheckItem> items = List.of(
            new CheckItem(101L, 100, 95),  // 盘亏5
            new CheckItem(102L, 50, 55)    // 盘盈5
        );

        when(checkMapper.selectById(checkId)).thenReturn(check);
        when(checkMapper.selectCheckItems(checkId)).thenReturn(items);

        // When
        checkService.completeCheck(checkId);

        // Then
        verify(inventoryMapper).updateQuantity(101L, 95);
        verify(inventoryMapper).updateQuantity(102L, 55);
    }
}
```

---

## 集成测试

### PurchaseOrderIntegrationTest
```java
@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class PurchaseOrderIntegrationTest {

    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0");

    @Container
    static GenericContainer<?> redis = new GenericContainer<>("redis:7.0");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PurchaseOrderMapper orderMapper;

    @Test
    @DisplayName("采购订单完整流程")
    void purchaseOrderFullFlow() throws Exception {
        // 1. 创建订单
        String createBody = """
            {
                "supplierId": 1,
                "items": [
                    {"productId": 101, "quantity": 100, "price": 10.00}
                ]
            }
            """;

        String response = mockMvc.perform(post("/api/v1/purchase/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(createBody))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();

        Long orderId = JsonPath.parse(response).read("$.data.id", Long.class);

        // 2. 审批订单
        mockMvc.perform(post("/api/v1/purchase/orders/{id}/approve", orderId)
                .param("approved", "true"))
            .andExpect(status().isOk());

        // 3. 入库
        String inboundBody = """
            {
                "orderId": %d,
                "warehouseId": 1,
                "batchNo": "B20260407001"
            }
            """.formatted(orderId);

        mockMvc.perform(post("/api/v1/purchase/inbound")
                .contentType(MediaType.APPLICATION_JSON)
                .content(inboundBody))
            .andExpect(status().isOk());

        // 4. 验证库存
        mockMvc.perform(get("/api/v1/inventory")
                .param("productId", "101"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.quantity").value(100));
    }
}
```

### SalesOrderIntegrationTest
```java
@SpringBootTest
@AutoConfigureMockMvc
class SalesOrderIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("销售订单完整流程")
    void salesOrderFullFlow() throws Exception {
        // 准备库存
        prepareInventory(101L, 100);

        // 1. 创建订单
        String createBody = """
            {
                "customerId": 1,
                "items": [
                    {"productId": 101, "quantity": 10, "price": 100.00}
                ]
            }
            """;

        String response = mockMvc.perform(post("/api/v1/sales/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(createBody))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();

        Long orderId = JsonPath.parse(response).read("$.data.id", Long.class);

        // 2. 审批订单
        mockMvc.perform(post("/api/v1/sales/orders/{id}/approve", orderId)
                .param("approved", "true"))
            .andExpect(status().isOk());

        // 3. 出库
        mockMvc.perform(post("/api/v1/sales/orders/{id}/outbound", orderId))
            .andExpect(status().isOk());

        // 4. 验证库存扣减
        mockMvc.perform(get("/api/v1/inventory")
                .param("productId", "101"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.quantity").value(90));
    }

    @Test
    @DisplayName("销售退货流程")
    void salesReturnFlow() throws Exception {
        // 准备已完成的销售订单
        Long orderId = createCompletedSalesOrder();

        // 1. 申请退货
        String returnBody = """
            {
                "orderId": %d,
                "items": [
                    {"productId": 101, "quantity": 5, "reason": "质量问题"}
                ]
            }
            """.formatted(orderId);

        mockMvc.perform(post("/api/v1/sales/returns")
                .contentType(MediaType.APPLICATION_JSON)
                .content(returnBody))
            .andExpect(status().isOk());

        // 2. 审批退货
        mockMvc.perform(post("/api/v1/sales/returns/{id}/approve", 1L)
                .param("approved", "true"))
            .andExpect(status().isOk());

        // 3. 验证库存增加
        mockMvc.perform(get("/api/v1/inventory")
                .param("productId", "101"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.quantity").value(95)); // 100 - 10 + 5
    }
}
```

### InventoryIntegrationTest
```java
@SpringBootTest
@AutoConfigureMockMvc
class InventoryIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private InventoryMapper inventoryMapper;

    @Test
    @DisplayName("库存调拨")
    void inventoryTransfer() throws Exception {
        // 准备库存
        inventoryMapper.insert(new Inventory(1L, 101L, 1L, 100));
        inventoryMapper.insert(new Inventory(2L, 101L, 2L, 0));

        String body = """
            {
                "productId": 101,
                "fromWarehouseId": 1,
                "toWarehouseId": 2,
                "quantity": 30
            }
            """;

        mockMvc.perform(post("/api/v1/inventory/transfer")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isOk());

        // 验证调拨结果
        Inventory from = inventoryMapper.selectByProductIdAndWarehouse(101L, 1L);
        Inventory to = inventoryMapper.selectByProductIdAndWarehouse(101L, 2L);

        assertEquals(70, from.getQuantity());
        assertEquals(30, to.getQuantity());
    }

    @Test
    @DisplayName("库存盘点")
    void inventoryCheck() throws Exception {
        // 准备库存
        inventoryMapper.insert(new Inventory(1L, 101L, 1L, 100));

        // 1. 创建盘点单
        String response = mockMvc.perform(post("/api/v1/inventory/check")
                .param("warehouseId", "1"))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();

        Long checkId = JsonPath.parse(response).read("$.data.id", Long.class);

        // 2. 录入实盘数量
        mockMvc.perform(post("/api/v1/inventory/check/{id}/record", checkId)
                .param("productId", "101")
                .param("actualQuantity", "95"))
            .andExpect(status().isOk());

        // 3. 完成盘点
        mockMvc.perform(post("/api/v1/inventory/check/{id}/complete", checkId))
            .andExpect(status().isOk());

        // 4. 验证库存调整
        Inventory inventory = inventoryMapper.selectByProductIdAndWarehouse(101L, 1L);
        assertEquals(95, inventory.getQuantity());
    }
}
```

---

## 前端测试

### PurchaseOrderList.spec.ts
```typescript
import { describe, it, expect, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import PurchaseOrderList from '@/views/erp/PurchaseOrderList.vue'
import * as purchaseApi from '@/api/purchase'

vi.mock('@/api/purchase')

describe('PurchaseOrderList', () => {
  it('渲染采购订单列表', async () => {
    vi.mocked(purchaseApi.getOrders).mockResolvedValue({
      data: {
        records: [
          { id: 1, orderNo: 'PO001', supplierName: '供应商A', status: 'PENDING' },
          { id: 2, orderNo: 'PO002', supplierName: '供应商B', status: 'APPROVED' }
        ],
        total: 2
      }
    })

    const wrapper = mount(PurchaseOrderList)
    await wrapper.vm.$nextTick()

    expect(wrapper.findAll('.order-row')).toHaveLength(2)
  })

  it('创建采购订单', async () => {
    vi.mocked(purchaseApi.createOrder).mockResolvedValue({ data: { id: 1 } })

    const wrapper = mount(PurchaseOrderList)

    await wrapper.find('.create-btn').trigger('click')
    await wrapper.vm.$nextTick()

    expect(wrapper.find('.order-dialog').isVisible()).toBe(true)
  })

  it('审批订单', async () => {
    vi.mocked(purchaseApi.approveOrder).mockResolvedValue({ data: {} })

    const wrapper = mount(PurchaseOrderList, {
      data() {
        return {
          orders: [{ id: 1, status: 'PENDING' }]
        }
      }
    })

    await wrapper.find('.approve-btn').trigger('click')

    expect(purchaseApi.approveOrder).toHaveBeenCalledWith(1, true)
  })
})
```

### InventoryList.spec.ts
```typescript
import { describe, it, expect, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import InventoryList from '@/views/erp/InventoryList.vue'
import * as inventoryApi from '@/api/inventory'

vi.mock('@/api/inventory')

describe('InventoryList', () => {
  it('渲染库存列表', async () => {
    vi.mocked(inventoryApi.getInventory).mockResolvedValue({
      data: [
        { productId: 1, productName: '商品A', quantity: 100, alertQuantity: 20 },
        { productId: 2, productName: '商品B', quantity: 10, alertQuantity: 20 }
      ]
    })

    const wrapper = mount(InventoryList)
    await wrapper.vm.$nextTick()

    expect(wrapper.findAll('.inventory-row')).toHaveLength(2)
  })

  it('显示库存预警', async () => {
    const wrapper = mount(InventoryList, {
      data() {
        return {
          inventory: [
            { productId: 1, quantity: 10, alertQuantity: 20 }
          ]
        }
      }
    })

    expect(wrapper.find('.alert-warning').exists()).toBe(true)
  })

  it('搜索商品', async () => {
    const wrapper = mount(InventoryList)

    await wrapper.find('.search-input').setValue('商品A')
    await wrapper.find('.search-btn').trigger('click')

    expect(inventoryApi.getInventory).toHaveBeenCalledWith(
      expect.objectContaining({ keyword: '商品A' })
    )
  })
})
```

### InventoryTransfer.spec.ts
```typescript
import { describe, it, expect, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import InventoryTransfer from '@/views/erp/InventoryTransfer.vue'
import * as inventoryApi from '@/api/inventory'

vi.mock('@/api/inventory')

describe('InventoryTransfer', () => {
  it('提交调拨申请', async () => {
    vi.mocked(inventoryApi.transfer).mockResolvedValue({ data: {} })

    const wrapper = mount(InventoryTransfer)

    await wrapper.setData({
      form: {
        productId: 1,
        fromWarehouseId: 1,
        toWarehouseId: 2,
        quantity: 50
      }
    })
    await wrapper.find('.submit-btn').trigger('click')

    expect(inventoryApi.transfer).toHaveBeenCalledWith({
      productId: 1,
      fromWarehouseId: 1,
      toWarehouseId: 2,
      quantity: 50
    })
  })

  it('验证调拨数量', async () => {
    const wrapper = mount(InventoryTransfer)

    await wrapper.setData({
      form: { quantity: -10 }
    })
    await wrapper.find('.submit-btn').trigger('click')

    expect(wrapper.find('.quantity-error').exists()).toBe(true)
  })
})
```

---

## 并发测试

### InventoryConcurrencyTest
```java
@SpringBootTest
class InventoryConcurrencyTest {

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private InventoryMapper inventoryMapper;

    @BeforeEach
    void setup() {
        inventoryMapper.insert(new Inventory(1L, 101L, 1L, 100));
    }

    @Test
    @DisplayName("并发出库 - 库存正确")
    void concurrentOutbound() throws InterruptedException {
        int threadCount = 10;
        int quantityPerThread = 10;
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    inventoryService.outbound(101L, 1L, quantityPerThread, "测试");
                    successCount.incrementAndGet();
                } catch (InsufficientStockException e) {
                    // 库存不足
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(10, TimeUnit.SECONDS);
        executor.shutdown();

        // 验证成功次数
        assertEquals(10, successCount.get());

        // 验证最终库存
        Inventory inventory = inventoryMapper.selectByProductIdAndWarehouse(101L, 1L);
        assertEquals(0, inventory.getQuantity());
    }

    @Test
    @DisplayName("并发出库 - 库存不足时正确拒绝")
    void concurrentOutbound_insufficientStock() throws InterruptedException {
        int threadCount = 20;
        int quantityPerThread = 10; // 总需求200，但只有100
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger failCount = new AtomicInteger(0);

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    inventoryService.outbound(101L, 1L, quantityPerThread, "测试");
                } catch (InsufficientStockException e) {
                    failCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(10, TimeUnit.SECONDS);
        executor.shutdown();

        // 应该有10个请求被拒绝
        assertEquals(10, failCount.get());
    }
}
```

---

## 性能测试

### ErpPerformanceTest
```java
@SpringBootTest
@AutoConfigureMockMvc
class ErpPerformanceTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("订单创建性能")
    void orderCreatePerformance() throws Exception {
        String body = """
            {
                "supplierId": 1,
                "items": [{"productId": 101, "quantity": 10, "price": 100.00}]
            }
            """;

        long start = System.nanoTime();
        for (int i = 0; i < 100; i++) {
            mockMvc.perform(post("/api/v1/purchase/orders")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body))
                .andExpect(status().isOk());
        }
        long duration = System.nanoTime() - start;

        // 平均响应时间 < 200ms
        assertTrue(duration / 100 < 200_000_000);
    }
}
```

### 性能指标

| 接口 | 并发数 | 平均响应时间 | 吞吐量 |
|------|--------|--------------|--------|
| POST /purchase/orders | 50 | < 200ms | > 100/s |
| POST /sales/orders | 50 | < 200ms | > 100/s |
| GET /inventory | 100 | < 100ms | > 500/s |
| POST /inventory/transfer | 30 | < 300ms | > 50/s |

---

## 测试清单

- [ ] 单元测试 - PurchaseOrderService
- [ ] 单元测试 - SalesOrderService
- [ ] 单元测试 - InventoryService
- [ ] 单元测试 - InventoryTransferService
- [ ] 单元测试 - InventoryCheckService
- [ ] 单元测试 - SupplierService
- [ ] 单元测试 - CustomerService
- [ ] 单元测试 - WarehouseService
- [ ] 单元测试 - InventoryAlertService
- [ ] 单元测试 - BatchManageService
- [ ] 集成测试 - 采购订单流程
- [ ] 集成测试 - 销售订单流程
- [ ] 集成测试 - 库存调拨
- [ ] 集成测试 - 库存盘点
- [ ] 集成测试 - 采购退货
- [ ] 集成测试 - 销售退货
- [ ] 前端测试 - PurchaseOrderList
- [ ] 前端测试 - SalesOrderList
- [ ] 前端测试 - InventoryList
- [ ] 前端测试 - InventoryTransfer
- [ ] 前端测试 - InventoryCheck
- [ ] 并发测试 - 库存操作
- [ ] 性能测试 - 订单创建
- [ ] 性能测试 - 库存查询
