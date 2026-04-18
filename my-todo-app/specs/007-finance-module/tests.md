# Tests: 财务模块

**Feature Branch**: `007-finance-module`
**Generated**: 2026-04-07

## 测试策略概览

| 层级 | 框架 | 覆盖率目标 |
|------|------|------------|
| 单元测试 | JUnit 5 + Mockito | ≥ 85% |
| 集成测试 | Spring Boot Test + Testcontainers | ≥ 80% |
| API测试 | MockMvc | 100% 端点 |
| 前端测试 | Vitest + Vue Test Utils | ≥ 75% |

---

## 单元测试

### BillServiceTest
```java
@ExtendWith(MockitoExtension.class)
class BillServiceTest {

    @Mock
    private BillMapper billMapper;

    @Mock
    private BillInvoiceMapper billInvoiceMapper;

    @Mock
    private MqProducer mqProducer;

    @InjectMocks
    private BillServiceImpl billService;

    @Test
    @DisplayName("创建账单 - 应收账单")
    void createBill_receivable() {
        // Given
        BillCreateRequest request = new BillCreateRequest();
        request.setBillType(BillType.RECEIVABLE);
        request.setCustomerId(1L);
        request.setAmount(new BigDecimal("10000.00"));
        request.setDueDate(LocalDate.now().plusDays(30));

        // When
        Long billId = billService.createBill(request);

        // Then
        assertNotNull(billId);
        verify(billMapper).insert(argThat(bill ->
            bill.getBillType() == BillType.RECEIVABLE &&
            bill.getStatus() == BillStatus.PENDING
        ));
    }

    @Test
    @DisplayName("创建账单 - 应付账单")
    void createBill_payable() {
        // Given
        BillCreateRequest request = new BillCreateRequest();
        request.setBillType(BillType.PAYABLE);
        request.setSupplierId(1L);
        request.setAmount(new BigDecimal("5000.00"));

        // When
        Long billId = billService.createBill(request);

        // Then
        assertNotNull(billId);
        verify(billMapper).insert(argThat(bill ->
            bill.getBillType() == BillType.PAYABLE
        ));
    }

    @Test
    @DisplayName("账单核销 - 部分核销")
    void writeOffBill_partial() {
        // Given
        Long billId = 1L;
        BigDecimal writeOffAmount = new BigDecimal("3000.00");

        Bill bill = new Bill();
        bill.setId(billId);
        bill.setAmount(new BigDecimal("10000.00"));
        bill.setPaidAmount(BigDecimal.ZERO);
        bill.setStatus(BillStatus.PENDING);

        when(billMapper.selectById(billId)).thenReturn(bill);

        // When
        billService.writeOff(billId, writeOffAmount);

        // Then
        assertEquals(new BigDecimal("3000.00"), bill.getPaidAmount());
        assertEquals(BillStatus.PARTIAL, bill.getStatus());
    }

    @Test
    @DisplayName("账单核销 - 全额核销")
    void writeOffBill_full() {
        // Given
        Long billId = 1L;
        BigDecimal writeOffAmount = new BigDecimal("10000.00");

        Bill bill = new Bill();
        bill.setId(billId);
        bill.setAmount(new BigDecimal("10000.00"));
        bill.setPaidAmount(BigDecimal.ZERO);
        bill.setStatus(BillStatus.PENDING);

        when(billMapper.selectById(billId)).thenReturn(bill);

        // When
        billService.writeOff(billId, writeOffAmount);

        // Then
        assertEquals(new BigDecimal("10000.00"), bill.getPaidAmount());
        assertEquals(BillStatus.PAID, bill.getStatus());
    }

    @Test
    @DisplayName("账单核销 - 超额核销拒绝")
    void writeOffBill_excess() {
        // Given
        Long billId = 1L;
        BigDecimal writeOffAmount = new BigDecimal("15000.00");

        Bill bill = new Bill();
        bill.setAmount(new BigDecimal("10000.00"));
        bill.setPaidAmount(BigDecimal.ZERO);

        when(billMapper.selectById(billId)).thenReturn(bill);

        // When & Then
        assertThrows(BusinessException.class,
            () -> billService.writeOff(billId, writeOffAmount));
    }

    @Test
    @DisplayName("账单关联合同")
    void linkContract() {
        // Given
        Long billId = 1L;
        Long contractId = 100L;

        Bill bill = new Bill();
        bill.setId(billId);

        when(billMapper.selectById(billId)).thenReturn(bill);

        // When
        billService.linkContract(billId, contractId);

        // Then
        assertEquals(contractId, bill.getContractId());
        verify(billMapper).updateById(bill);
    }
}
```

### TransactionServiceTest
```java
@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionMapper transactionMapper;

    @Mock
    private BillService billService;

    @Mock
    private AccountService accountService;

    @InjectMocks
    private TransactionServiceImpl transactionService;

    @Test
    @DisplayName("创建收支记录 - 收入")
    void createTransaction_income() {
        // Given
        TransactionCreateRequest request = new TransactionCreateRequest();
        request.setType(TransactionType.INCOME);
        request.setAccountId(1L);
        request.setAmount(new BigDecimal("5000.00"));
        request.setBillId(100L);

        Account account = new Account();
        account.setId(1L);
        account.setBalance(new BigDecimal("10000.00"));

        when(accountService.getAccount(1L)).thenReturn(account);

        // When
        Long transactionId = transactionService.createTransaction(request);

        // Then
        assertNotNull(transactionId);
        verify(accountService).updateBalance(1L, new BigDecimal("5000.00"));
        verify(billService).writeOff(100L, new BigDecimal("5000.00"));
    }

    @Test
    @DisplayName("创建收支记录 - 支出")
    void createTransaction_expense() {
        // Given
        TransactionCreateRequest request = new TransactionCreateRequest();
        request.setType(TransactionType.EXPENSE);
        request.setAccountId(1L);
        request.setAmount(new BigDecimal("3000.00"));

        Account account = new Account();
        account.setId(1L);
        account.setBalance(new BigDecimal("10000.00"));

        when(accountService.getAccount(1L)).thenReturn(account);

        // When
        Long transactionId = transactionService.createTransaction(request);

        // Then
        assertNotNull(transactionId);
        verify(accountService).updateBalance(1L, new BigDecimal("-3000.00"));
    }

    @Test
    @DisplayName("收支记录 - 账户余额不足")
    void createTransaction_insufficientBalance() {
        // Given
        TransactionCreateRequest request = new TransactionCreateRequest();
        request.setType(TransactionType.EXPENSE);
        request.setAccountId(1L);
        request.setAmount(new BigDecimal("20000.00"));

        Account account = new Account();
        account.setBalance(new BigDecimal("10000.00"));

        when(accountService.getAccount(1L)).thenReturn(account);

        // When & Then
        assertThrows(InsufficientBalanceException.class,
            () -> transactionService.createTransaction(request));
    }

    @Test
    @DisplayName("多币种收支 - 汇率转换")
    void createTransaction_multiCurrency() {
        // Given
        TransactionCreateRequest request = new TransactionCreateRequest();
        request.setAccountId(1L);
        request.setAmount(new BigDecimal("1000.00"));
        request.setCurrency("USD");

        when(exchangeRateService.getRate("USD", "CNY"))
            .thenReturn(new BigDecimal("7.20"));

        // When
        Long transactionId = transactionService.createTransaction(request);

        // Then
        verify(transactionMapper).insert(argThat(tx ->
            tx.getBaseAmount().equals(new BigDecimal("7200.00"))
        ));
    }
}
```

### InvoiceServiceTest
```java
@ExtendWith(MockitoExtension.class)
class InvoiceServiceTest {

    @Mock
    private InvoiceMapper invoiceMapper;

    @Mock
    private BillInvoiceMapper billInvoiceMapper;

    @Mock
    private SequenceGenerator sequenceGenerator;

    @InjectMocks
    private InvoiceServiceImpl invoiceService;

    @Test
    @DisplayName("开具发票 - 成功")
    void createInvoice_success() {
        // Given
        InvoiceCreateRequest request = new InvoiceCreateRequest();
        request.setInvoiceType(InvoiceType.SALES);
        request.setBuyerName("测试公司");
        request.setBuyerTaxNo("91110000MA00ABCD12");
        request.setAmount(new BigDecimal("11300.00")); // 含税
        request.setTaxRate(new BigDecimal("0.13"));

        when(sequenceGenerator.nextInvoiceNo()).thenReturn("INV20260407001");

        // When
        Long invoiceId = invoiceService.createInvoice(request);

        // Then
        assertNotNull(invoiceId);
        verify(invoiceMapper).insert(argThat(inv ->
            inv.getInvoiceNo().equals("INV20260407001") &&
            inv.getTaxAmount().equals(new BigDecimal("1300.00")) &&
            inv.getExcludeTaxAmount().equals(new BigDecimal("10000.00"))
        ));
    }

    @Test
    @DisplayName("发票验证 - 税号格式")
    void validateInvoice_taxNoFormat() {
        // Given
        InvoiceCreateRequest request = new InvoiceCreateRequest();
        request.setBuyerTaxNo("invalid_tax_no");

        // When & Then
        assertThrows(ValidationException.class,
            () -> invoiceService.createInvoice(request));
    }

    @Test
    @DisplayName("关联合同到发票")
    void linkBillToInvoice() {
        // Given
        Long invoiceId = 1L;
        Long billId = 100L;

        // When
        invoiceService.linkBill(invoiceId, billId);

        // Then
        verify(billInvoiceMapper).insert(new BillInvoice(billId, invoiceId));
    }

    @Test
    @DisplayName("红冲发票")
    void redInvoice() {
        // Given
        Long originalInvoiceId = 1L;

        Invoice original = new Invoice();
        original.setId(originalInvoiceId);
        original.setInvoiceNo("INV20260407001");
        original.setAmount(new BigDecimal("10000.00"));

        when(invoiceMapper.selectById(originalInvoiceId)).thenReturn(original);

        // When
        Long redInvoiceId = invoiceService.createRedInvoice(originalInvoiceId);

        // Then
        verify(invoiceMapper).insert(argThat(inv ->
            inv.getInvoiceType() == InvoiceType.RED &&
            inv.getOriginalInvoiceId().equals(originalInvoiceId) &&
            inv.getAmount().equals(new BigDecimal("-10000.00"))
        ));
    }
}
```

### CostServiceTest
```java
@ExtendWith(MockitoExtension.class)
class CostServiceTest {

    @Mock
    private CostConfigMapper costConfigMapper;

    @Mock
    private CostHistoryMapper costHistoryMapper;

    @Mock
    private InventoryFlowMapper inventoryFlowMapper;

    @InjectMocks
    private CostServiceImpl costService;

    @Test
    @DisplayName("成本计算 - 加权平均法")
    void calculateCost_weightedAverage() {
        // Given
        Long productId = 1L;
        CostConfig config = new CostConfig();
        config.setMethod(CostMethod.WEIGHTED_AVERAGE);

        List<InventoryFlow> flows = List.of(
            new InventoryFlow(100, new BigDecimal("10.00")),
            new InventoryFlow(100, new BigDecimal("12.00")),
            new InventoryFlow(100, new BigDecimal("11.00"))
        );

        when(costConfigMapper.selectByProductId(productId)).thenReturn(config);
        when(inventoryFlowMapper.selectInboundFlows(productId)).thenReturn(flows);

        // When
        BigDecimal cost = costService.calculateCost(productId);

        // Then
        assertEquals(new BigDecimal("11.00"), cost); // (10+12+11)/3
    }

    @Test
    @DisplayName("成本计算 - 先进先出法")
    void calculateCost_fifo() {
        // Given
        Long productId = 1L;
        CostConfig config = new CostConfig();
        config.setMethod(CostMethod.FIFO);

        List<InventoryFlow> flows = List.of(
            new InventoryFlow(50, new BigDecimal("10.00")),
            new InventoryFlow(50, new BigDecimal("12.00"))
        );

        when(costConfigMapper.selectByProductId(productId)).thenReturn(config);
        when(inventoryFlowMapper.selectInboundFlows(productId)).thenReturn(flows);

        // When - 销售60件
        BigDecimal cost = costService.calculateCostForSale(productId, 60);

        // Then - 50件@10 + 10件@12 = 620
        assertEquals(new BigDecimal("620.00"), cost);
    }

    @Test
    @DisplayName("成本调整")
    void adjustCost() {
        // Given
        Long productId = 1L;
        BigDecimal newCost = new BigDecimal("15.00");
        String reason = "市场价格上涨";

        // When
        costService.adjustCost(productId, newCost, reason);

        // Then
        verify(costConfigMapper).updateCost(productId, newCost);
        verify(costHistoryMapper).insert(argThat(history ->
            history.getNewCost().equals(newCost) &&
            history.getReason().equals(reason)
        ));
    }
}
```

### ProfitCalcServiceTest
```java
@ExtendWith(MockitoExtension.class)
class ProfitCalcServiceTest {

    @Mock
    private SalesOrderMapper salesOrderMapper;

    @Mock
    private CostService costService;

    @InjectMocks
    private ProfitCalcServiceImpl profitCalcService;

    @Test
    @DisplayName("毛利计算 - 单笔订单")
    void calculateProfit_singleOrder() {
        // Given
        Long orderId = 1L;

        SalesOrder order = new SalesOrder();
        order.setId(orderId);
        order.setTotalAmount(new BigDecimal("10000.00"));
        order.setItems(List.of(
            new OrderItem(101L, 100, new BigDecimal("100.00"))
        ));

        when(salesOrderMapper.selectById(orderId)).thenReturn(order);
        when(costService.calculateCostForSale(101L, 100))
            .thenReturn(new BigDecimal("6000.00"));

        // When
        ProfitResult result = profitCalcService.calculateOrderProfit(orderId);

        // Then
        assertEquals(new BigDecimal("10000.00"), result.getRevenue());
        assertEquals(new BigDecimal("6000.00"), result.getCost());
        assertEquals(new BigDecimal("4000.00"), result.getGrossProfit());
        assertEquals(new BigDecimal("0.40"), result.getGrossMargin());
    }

    @Test
    @DisplayName("毛利计算 - 月度汇总")
    void calculateProfit_monthly() {
        // Given
        LocalDate month = LocalDate.of(2026, 4, 1);

        when(salesOrderMapper.selectByMonth(month))
            .thenReturn(List.of(
                createOrder(10000, 6000),
                createOrder(20000, 12000),
                createOrder(15000, 9000)
            ));

        // When
        ProfitResult result = profitCalcService.calculateMonthlyProfit(month);

        // Then
        assertEquals(new BigDecimal("45000.00"), result.getRevenue());
        assertEquals(new BigDecimal("27000.00"), result.getCost());
        assertEquals(new BigDecimal("18000.00"), result.getGrossProfit());
    }

    private SalesOrder createOrder(BigDecimal revenue, BigDecimal cost) {
        SalesOrder order = new SalesOrder();
        order.setTotalAmount(revenue);
        // ... setup items
        return order;
    }
}
```

### BankReconciliationTest
```java
@ExtendWith(MockitoExtension.class)
class BankReconciliationTest {

    @Mock
    private BankRecordMapper bankRecordMapper;

    @Mock
    private TransactionMapper transactionMapper;

    @InjectMocks
    private BankReconciliationService reconciliationService;

    @Test
    @DisplayName("银行对账 - 自动匹配")
    void reconcile_autoMatch() {
        // Given
        Long accountId = 1L;
        LocalDate date = LocalDate.of(2026, 4, 7);

        List<BankRecord> bankRecords = List.of(
            new BankRecord("银行收入", new BigDecimal("5000.00"), date)
        );

        List<Transaction> transactions = List.of(
            new Transaction(new BigDecimal("5000.00"), date, "银行收入")
        );

        when(bankRecordMapper.selectByAccountAndDate(accountId, date))
            .thenReturn(bankRecords);
        when(transactionMapper.selectByAccountAndDate(accountId, date))
            .thenReturn(transactions);

        // When
        ReconciliationResult result = reconciliationService.reconcile(accountId, date);

        // Then
        assertEquals(1, result.getMatchedCount());
        assertEquals(0, result.getUnmatchedBankRecords());
        assertEquals(0, result.getUnmatchedTransactions());
    }

    @Test
    @DisplayName("银行对账 - 发现差异")
    void reconcile_discrepancy() {
        // Given
        Long accountId = 1L;
        LocalDate date = LocalDate.of(2026, 4, 7);

        List<BankRecord> bankRecords = List.of(
            new BankRecord("银行收入", new BigDecimal("5000.00"), date)
        );

        List<Transaction> transactions = List.of(
            new Transaction(new BigDecimal("4500.00"), date, "银行收入")
        );

        when(bankRecordMapper.selectByAccountAndDate(accountId, date))
            .thenReturn(bankRecords);
        when(transactionMapper.selectByAccountAndDate(accountId, date))
            .thenReturn(transactions);

        // When
        ReconciliationResult result = reconciliationService.reconcile(accountId, date);

        // Then
        assertEquals(1, result.getDiscrepancies().size());
        assertEquals(new BigDecimal("500.00"), result.getDiscrepancies().get(0).getDifference());
    }
}
```

### BudgetServiceTest
```java
@ExtendWith(MockitoExtension.class)
class BudgetServiceTest {

    @Mock
    private BudgetMapper budgetMapper;

    @Mock
    private TransactionMapper transactionMapper;

    @InjectMocks
    private BudgetServiceImpl budgetService;

    @Test
    @DisplayName("创建预算")
    void createBudget() {
        // Given
        BudgetCreateRequest request = new BudgetCreateRequest();
        request.setYear(2026);
        request.setMonth(4);
        request.setCategory("办公费用");
        request.setAmount(new BigDecimal("10000.00"));

        // When
        Long budgetId = budgetService.createBudget(request);

        // Then
        assertNotNull(budgetId);
        verify(budgetMapper).insert(argThat(budget ->
            budget.getYear() == 2026 &&
            budget.getMonth() == 4 &&
            budget.getUsedAmount().equals(BigDecimal.ZERO)
        ));
    }

    @Test
    @DisplayName("预算执行率计算")
    void calculateExecutionRate() {
        // Given
        Long budgetId = 1L;

        Budget budget = new Budget();
        budget.setAmount(new BigDecimal("10000.00"));
        budget.setUsedAmount(new BigDecimal("7500.00"));

        when(budgetMapper.selectById(budgetId)).thenReturn(budget);

        // When
        BigDecimal rate = budgetService.calculateExecutionRate(budgetId);

        // Then
        assertEquals(new BigDecimal("0.75"), rate);
    }

    @Test
    @DisplayName("预算超支预警")
    void checkBudgetAlert_exceeded() {
        // Given
        Budget budget = new Budget();
        budget.setAmount(new BigDecimal("10000.00"));
        budget.setUsedAmount(new BigDecimal("9500.00"));
        budget.setAlertThreshold(new BigDecimal("0.90"));

        // When
        boolean needAlert = budgetService.checkAlert(budget);

        // Then
        assertTrue(needAlert);
    }
}
```

---

## 集成测试

### BillIntegrationTest
```java
@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class BillIntegrationTest {

    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private BillMapper billMapper;

    @Test
    @DisplayName("账单完整生命周期")
    void billLifecycle() throws Exception {
        // 1. 创建账单
        String createBody = """
            {
                "billType": "RECEIVABLE",
                "customerId": 1,
                "amount": 10000.00,
                "dueDate": "2026-05-07"
            }
            """;

        String response = mockMvc.perform(post("/api/v1/finance/bills")
                .contentType(MediaType.APPLICATION_JSON)
                .content(createBody))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();

        Long billId = JsonPath.parse(response).read("$.data.id", Long.class);

        // 2. 部分核销
        mockMvc.perform(post("/api/v1/finance/bills/{id}/writeoff", billId)
                .param("amount", "5000.00"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.status").value("PARTIAL"));

        // 3. 全额核销
        mockMvc.perform(post("/api/v1/finance/bills/{id}/writeoff", billId)
                .param("amount", "5000.00"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.status").value("PAID"));
    }
}
```

### InvoiceIntegrationTest
```java
@SpringBootTest
@AutoConfigureMockMvc
class InvoiceIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("发票开具和红冲流程")
    void invoiceAndRedInvoice() throws Exception {
        // 1. 开具发票
        String createBody = """
            {
                "invoiceType": "SALES",
                "buyerName": "测试公司",
                "buyerTaxNo": "91110000MA00ABCD12",
                "amount": 11300.00,
                "taxRate": 0.13
            }
            """;

        String response = mockMvc.perform(post("/api/v1/finance/invoices")
                .contentType(MediaType.APPLICATION_JSON)
                .content(createBody))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();

        Long invoiceId = JsonPath.parse(response).read("$.data.id", Long.class);

        // 2. 红冲发票
        mockMvc.perform(post("/api/v1/finance/invoices/{id}/red", invoiceId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.invoiceType").value("RED"));
    }
}
```

### ReportIntegrationTest
```java
@SpringBootTest
@AutoConfigureMockMvc
class ReportIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("生成资产负债表")
    void generateBalanceSheet() throws Exception {
        mockMvc.perform(get("/api/v1/finance/reports/balance-sheet")
                .param("date", "2026-04-07"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.assets").exists())
            .andExpect(jsonPath("$.data.liabilities").exists())
            .andExpect(jsonPath("$.data.equity").exists());
    }

    @Test
    @DisplayName("生成利润表")
    void generateIncomeStatement() throws Exception {
        mockMvc.perform(get("/api/v1/finance/reports/income-statement")
                .param("year", "2026")
                .param("month", "4"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.revenue").exists())
            .andExpect(jsonPath("$.data.costs").exists())
            .andExpect(jsonPath("$.data.netProfit").exists());
    }

    @Test
    @DisplayName("生成现金流量表")
    void generateCashFlowStatement() throws Exception {
        mockMvc.perform(get("/api/v1/finance/reports/cash-flow")
                .param("year", "2026")
                .param("month", "4"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.operatingCashFlow").exists())
            .andExpect(jsonPath("$.data.investingCashFlow").exists())
            .andExpect(jsonPath("$.data.financingCashFlow").exists());
    }
}
```

### ErpIntegrationTest
```java
@SpringBootTest
@AutoConfigureMockMvc
class ErpIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("销售订单触发应收账单创建")
    void salesOrderCreatesBill() throws Exception {
        // 完成销售订单
        Long salesOrderId = completeSalesOrder();

        // 验证应收账单自动创建
        mockMvc.perform(get("/api/v1/finance/bills")
                .param("sourceType", "SALES_ORDER")
                .param("sourceId", salesOrderId.toString()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data[0].billType").value("RECEIVABLE"));
    }

    @Test
    @DisplayName("采购入库触发应付账单创建")
    void purchaseOrderCreatesBill() throws Exception {
        // 完成采购入库
        Long purchaseOrderId = completePurchaseOrder();

        // 验证应付账单自动创建
        mockMvc.perform(get("/api/v1/finance/bills")
                .param("sourceType", "PURCHASE_ORDER")
                .param("sourceId", purchaseOrderId.toString()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data[0].billType").value("PAYABLE"));
    }
}
```

---

## 前端测试

### BillList.spec.ts
```typescript
import { describe, it, expect, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import BillList from '@/views/finance/BillList.vue'
import * as billApi from '@/api/bill'

vi.mock('@/api/bill')

describe('BillList', () => {
  it('渲染账单列表', async () => {
    vi.mocked(billApi.getBills).mockResolvedValue({
      data: {
        records: [
          { id: 1, billType: 'RECEIVABLE', amount: 10000, status: 'PENDING' },
          { id: 2, billType: 'PAYABLE', amount: 5000, status: 'PAID' }
        ],
        total: 2
      }
    })

    const wrapper = mount(BillList)
    await wrapper.vm.$nextTick()

    expect(wrapper.findAll('.bill-row')).toHaveLength(2)
  })

  it('筛选账单类型', async () => {
    const wrapper = mount(BillList)

    await wrapper.find('.type-filter').setValue('RECEIVABLE')
    await wrapper.vm.loadBills()

    expect(billApi.getBills).toHaveBeenCalledWith(
      expect.objectContaining({ billType: 'RECEIVABLE' })
    )
  })

  it('核销账单', async () => {
    vi.mocked(billApi.writeOff).mockResolvedValue({ data: {} })

    const wrapper = mount(BillList, {
      data() {
        return {
          bills: [{ id: 1, amount: 10000, paidAmount: 0 }]
        }
      }
    })

    await wrapper.setData({ writeOffAmount: 5000 })
    await wrapper.find('.writeoff-btn').trigger('click')

    expect(billApi.writeOff).toHaveBeenCalledWith(1, 5000)
  })
})
```

### InvoiceCreate.spec.ts
```typescript
import { describe, it, expect, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import InvoiceCreate from '@/views/finance/InvoiceCreate.vue'
import * as invoiceApi from '@/api/invoice'

vi.mock('@/api/invoice')

describe('InvoiceCreate', () => {
  it('计算税额', async () => {
    const wrapper = mount(InvoiceCreate)

    await wrapper.find('input[name="amount"]').setValue('11300')
    await wrapper.find('input[name="taxRate"]').setValue('0.13')
    await wrapper.vm.calculateTax()

    expect(wrapper.vm.taxAmount).toBe(1300)
    expect(wrapper.vm.excludeTaxAmount).toBe(10000)
  })

  it('验证税号格式', async () => {
    const wrapper = mount(InvoiceCreate)

    await wrapper.find('input[name="buyerTaxNo"]').setValue('invalid')
    await wrapper.find('.submit-btn').trigger('click')

    expect(wrapper.find('.tax-no-error').exists()).toBe(true)
  })

  it('提交发票', async () => {
    vi.mocked(invoiceApi.createInvoice).mockResolvedValue({ data: { id: 1 } })

    const wrapper = mount(InvoiceCreate)

    await fillInvoiceForm(wrapper)
    await wrapper.find('.submit-btn').trigger('click')

    expect(invoiceApi.createInvoice).toHaveBeenCalled()
  })
})
```

### FinancialReport.spec.ts
```typescript
import { describe, it, expect, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import FinancialReport from '@/views/finance/FinancialReport.vue'
import * as reportApi from '@/api/report'

vi.mock('@/api/report')

describe('FinancialReport', () => {
  it('加载报表数据', async () => {
    vi.mocked(reportApi.getBalanceSheet).mockResolvedValue({
      data: {
        assets: { total: 1000000 },
        liabilities: { total: 600000 },
        equity: { total: 400000 }
      }
    })

    const wrapper = mount(FinancialReport)
    await wrapper.vm.loadReport()

    expect(wrapper.vm.reportData).toBeDefined()
  })

  it('导出报表', async () => {
    const wrapper = mount(FinancialReport)

    await wrapper.find('.export-btn').trigger('click')

    // 验证导出逻辑
    expect(wrapper.vm.exporting).toBe(true)
  })

  it('切换报表类型', async () => {
    const wrapper = mount(FinancialReport)

    await wrapper.find('.report-type').setValue('income-statement')
    await wrapper.vm.loadReport()

    expect(reportApi.getIncomeStatement).toHaveBeenCalled()
  })
})
```

---

## 精度测试

### DecimalPrecisionTest
```java
class DecimalPrecisionTest {

    @Test
    @DisplayName("金额计算精度 - BigDecimal")
    void amountPrecision() {
        BigDecimal a = new BigDecimal("100.00");
        BigDecimal b = new BigDecimal("33.33");

        BigDecimal result = a.subtract(b);

        assertEquals(new BigDecimal("66.67"), result);
    }

    @Test
    @DisplayName("税率计算精度")
    void taxCalculation() {
        BigDecimal amount = new BigDecimal("10000.00");
        BigDecimal taxRate = new BigDecimal("0.13");

        BigDecimal tax = amount.multiply(taxRate)
            .setScale(2, RoundingMode.HALF_UP);

        assertEquals(new BigDecimal("1300.00"), tax);
    }

    @Test
    @DisplayName("汇率转换精度")
    void exchangeRateCalculation() {
        BigDecimal usd = new BigDecimal("1000.00");
        BigDecimal rate = new BigDecimal("7.2345");

        BigDecimal cny = usd.multiply(rate)
            .setScale(2, RoundingMode.HALF_UP);

        assertEquals(new BigDecimal("7234.50"), cny);
    }

    @Test
    @DisplayName("毛利计算精度")
    void profitCalculation() {
        BigDecimal revenue = new BigDecimal("10000.00");
        BigDecimal cost = new BigDecimal("6500.00");

        BigDecimal profit = revenue.subtract(cost);
        BigDecimal margin = profit.divide(revenue, 4, RoundingMode.HALF_UP);

        assertEquals(new BigDecimal("3500.00"), profit);
        assertEquals(new BigDecimal("0.3500"), margin);
    }
}
```

---

## 性能测试

### FinancePerformanceTest
```java
@SpringBootTest
@AutoConfigureMockMvc
class FinancePerformanceTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("报表生成性能")
    void reportGenerationPerformance() throws Exception {
        long start = System.nanoTime();

        mockMvc.perform(get("/api/v1/finance/reports/balance-sheet")
                .param("date", "2026-04-07"))
            .andExpect(status().isOk());

        long duration = System.nanoTime() - start;

        // 报表生成 < 3秒
        assertTrue(duration < 3_000_000_000);
    }

    @Test
    @DisplayName("账单查询性能")
    void billQueryPerformance() throws Exception {
        long start = System.nanoTime();

        for (int i = 0; i < 100; i++) {
            mockMvc.perform(get("/api/v1/finance/bills")
                    .param("page", String.valueOf(i))
                    .param("size", "20"))
                .andExpect(status().isOk());
        }

        long duration = System.nanoTime() - start;

        // 平均响应时间 < 100ms
        assertTrue(duration / 100 < 100_000_000);
    }
}
```

### 性能指标

| 接口 | 并发数 | 平均响应时间 | 吞吐量 |
|------|--------|--------------|--------|
| POST /bills | 50 | < 200ms | > 100/s |
| GET /bills | 100 | < 100ms | > 500/s |
| POST /invoices | 30 | < 300ms | > 50/s |
| GET /reports/* | 20 | < 3s | > 10/s |
| POST /transactions | 50 | < 150ms | > 200/s |

---

## 测试清单

- [ ] 单元测试 - BillService
- [ ] 单元测试 - TransactionService
- [ ] 单元测试 - InvoiceService
- [ ] 单元测试 - CostService
- [ ] 单元测试 - ProfitCalcService
- [ ] 单元测试 - BankReconciliationService
- [ ] 单元测试 - BudgetService
- [ ] 单元测试 - ReportService
- [ ] 单元测试 - ExchangeRateService
- [ ] 集成测试 - 账单生命周期
- [ ] 集成测试 - 发票开具和红冲
- [ ] 集成测试 - 财务报表生成
- [ ] 集成测试 - 进销存集成
- [ ] 前端测试 - BillList
- [ ] 前端测试 - InvoiceCreate
- [ ] 前端测试 - FinancialReport
- [ ] 前端测试 - CostConfig
- [ ] 前端测试 - BudgetManage
- [ ] 精度测试 - 金额计算
- [ ] 精度测试 - 税率计算
- [ ] 性能测试 - 报表生成
- [ ] 性能测试 - 账单查询
