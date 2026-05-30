package com.example.erp.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.erp.api.dto.DashboardDTO;
import com.example.erp.api.dto.SalesReportDTO;
import com.example.erp.api.dto.PurchaseReportDTO;
import com.example.erp.api.dto.InventoryReportDTO;
import com.example.erp.api.dto.ProfitReportDTO;
import com.example.erp.api.dto.SupplierStatementDTO;
import com.example.erp.api.dto.CustomerStatementDTO;
import com.example.erp.api.feign.InventoryFeignClient;
import com.example.common.core.result.ApiResponse;
import com.example.common.core.result.PageResult;
import com.example.inventory.api.dto.InventoryDTO;
import com.example.erp.entity.*;
import com.example.erp.mapper.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 报表统计服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final SalesOrderMapper salesOrderMapper;
    private final SalesOrderItemMapper salesOrderItemMapper;
    private final PurchaseOrderMapper purchaseOrderMapper;
    private final PurchaseOrderItemMapper purchaseOrderItemMapper;
    private final InventoryFeignClient inventoryFeignClient;
    private final CustomerMapper customerMapper;
    private final SupplierMapper supplierMapper;
    private final ProductMapper productMapper;
    private final WarehouseMapper warehouseMapper;

    @Override
    public DashboardDTO getDashboardData(Long tenantId) {
        DashboardDTO vo = new DashboardDTO();

        LocalDate today = LocalDate.now();
        LocalDateTime todayStart = today.atStartOfDay();
        LocalDateTime todayEnd = today.atTime(LocalTime.MAX);
        LocalDateTime monthStart = today.withDayOfMonth(1).atStartOfDay();

        List<SalesOrder> todaySales = salesOrderMapper.selectList(
            new LambdaQueryWrapper<SalesOrder>()
                .eq(SalesOrder::getTenantId, tenantId)
                .ge(SalesOrder::getCreatedAt, todayStart)
                .le(SalesOrder::getCreatedAt, todayEnd)
                .ne(SalesOrder::getOrderStatus, 5)
        );
        vo.setTodaySales(todaySales.stream().map(SalesOrder::getTotalAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add));

        List<SalesOrder> monthSales = salesOrderMapper.selectList(
            new LambdaQueryWrapper<SalesOrder>()
                .eq(SalesOrder::getTenantId, tenantId)
                .ge(SalesOrder::getCreatedAt, monthStart)
                .ne(SalesOrder::getOrderStatus, 5)
        );
        vo.setMonthSales(monthSales.stream().map(SalesOrder::getTotalAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add));

        List<PurchaseOrder> todayPurchases = purchaseOrderMapper.selectList(
            new LambdaQueryWrapper<PurchaseOrder>()
                .eq(PurchaseOrder::getTenantId, tenantId)
                .ge(PurchaseOrder::getCreatedAt, todayStart)
                .le(PurchaseOrder::getCreatedAt, todayEnd)
                .ne(PurchaseOrder::getOrderStatus, 5)
        );
        vo.setTodayPurchases(todayPurchases.stream().map(PurchaseOrder::getTotalAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add));

        List<PurchaseOrder> monthPurchases = purchaseOrderMapper.selectList(
            new LambdaQueryWrapper<PurchaseOrder>()
                .eq(PurchaseOrder::getTenantId, tenantId)
                .ge(PurchaseOrder::getCreatedAt, monthStart)
                .ne(PurchaseOrder::getOrderStatus, 5)
        );
        vo.setMonthPurchases(monthPurchases.stream().map(PurchaseOrder::getTotalAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add));

        ApiResponse<PageResult<InventoryDTO>> inventoryResp = inventoryFeignClient.getInventoryPage(tenantId, 1, 1000, null, null);
        List<InventoryDTO> allInventory = (inventoryResp.getData() != null && inventoryResp.getData().getRecords() != null)
            ? inventoryResp.getData().getRecords() : Collections.emptyList();
        vo.setInventoryProductCount((int) allInventory.stream().filter(i -> i.getQuantity() != null && i.getQuantity().compareTo(BigDecimal.ZERO) > 0).count());

        ApiResponse<List<InventoryDTO>> alertResp = inventoryFeignClient.getAlertInventories(tenantId, null);
        vo.setAlertProductCount(alertResp.getData() != null ? alertResp.getData().size() : 0);

        Long pendingPO = purchaseOrderMapper.selectCount(
            new LambdaQueryWrapper<PurchaseOrder>()
                .eq(PurchaseOrder::getTenantId, tenantId)
                .eq(PurchaseOrder::getOrderStatus, 1)
        );
        vo.setPendingPurchaseOrders(pendingPO.intValue());

        Long pendingSO = salesOrderMapper.selectCount(
            new LambdaQueryWrapper<SalesOrder>()
                .eq(SalesOrder::getTenantId, tenantId)
                .eq(SalesOrder::getOrderStatus, 1)
        );
        vo.setPendingSalesOrders(pendingSO.intValue());

        vo.setSalesTrend(getSalesTrend(tenantId, 7));
        vo.setPurchaseTrend(getPurchaseTrend(tenantId, 7));
        vo.setTopProducts(getTopProducts(tenantId, 10));
        vo.setTopCustomers(getTopCustomers(tenantId, 10));

        return vo;
    }

    @Override
    public SalesReportDTO getSalesReport(Long tenantId, String startDate, String endDate) {
        SalesReportDTO vo = new SalesReportDTO();
        LocalDateTime start = LocalDate.parse(startDate).atStartOfDay();
        LocalDateTime end = LocalDate.parse(endDate).atTime(LocalTime.MAX);

        List<SalesOrder> orders = salesOrderMapper.selectList(
            new LambdaQueryWrapper<SalesOrder>()
                .eq(SalesOrder::getTenantId, tenantId)
                .ge(SalesOrder::getCreatedAt, start)
                .le(SalesOrder::getCreatedAt, end)
                .ne(SalesOrder::getOrderStatus, 5)
        );

        SalesReportDTO.Summary summary = new SalesReportDTO.Summary();
        summary.setTotalAmount(orders.stream().map(SalesOrder::getTotalAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add));
        summary.setTotalOrders(orders.size());
        vo.setSummary(summary);

        if (orders.isEmpty()) {
            vo.setProductStats(Collections.emptyList());
            vo.setCustomerStats(Collections.emptyList());
            vo.setDailyStats(Collections.emptyList());
            return vo;
        }

        Set<Long> orderIds = orders.stream().map(SalesOrder::getId).collect(Collectors.toSet());

        List<SalesOrderItem> items = salesOrderItemMapper.selectList(
            new LambdaQueryWrapper<SalesOrderItem>()
                .in(SalesOrderItem::getOrderId, orderIds)
        );
        Map<Long, List<SalesOrderItem>> productGroups = items.stream()
            .collect(Collectors.groupingBy(SalesOrderItem::getProductId));
        List<SalesReportDTO.ProductStat> productStats = new ArrayList<>();
        int totalQty = 0;
        for (Map.Entry<Long, List<SalesOrderItem>> entry : productGroups.entrySet()) {
            SalesReportDTO.ProductStat stat = new SalesReportDTO.ProductStat();
            stat.setProductId(entry.getKey());
            SalesOrderItem first = entry.getValue().get(0);
            stat.setProductName(first.getProductName());
            stat.setProductCode(first.getProductCode());
            int qty = entry.getValue().stream().mapToInt(i -> i.getQuantity().intValue()).sum();
            BigDecimal amt = entry.getValue().stream().map(SalesOrderItem::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            stat.setQuantity(qty);
            stat.setAmount(amt);
            productStats.add(stat);
            totalQty += qty;
        }
        productStats.sort((a, b) -> b.getAmount().compareTo(a.getAmount()));
        vo.setProductStats(productStats);
        summary.setTotalQuantity(totalQty);
        summary.setAvgOrderAmount(summary.getTotalAmount().divide(
            BigDecimal.valueOf(summary.getTotalOrders()), 2, RoundingMode.HALF_UP));

        Map<Long, List<SalesOrder>> customerGroups = orders.stream()
            .collect(Collectors.groupingBy(SalesOrder::getCustomerId));
        Set<Long> customerIds = customerGroups.keySet();
        Map<Long, Customer> customerMap = customerMapper.selectBatchIds(customerIds).stream()
            .collect(Collectors.toMap(Customer::getId, c -> c));
        List<SalesReportDTO.CustomerStat> customerStats = new ArrayList<>();
        for (Map.Entry<Long, List<SalesOrder>> entry : customerGroups.entrySet()) {
            SalesReportDTO.CustomerStat stat = new SalesReportDTO.CustomerStat();
            stat.setCustomerId(entry.getKey());
            Customer c = customerMap.get(entry.getKey());
            stat.setCustomerName(c != null ? c.getCustomerName() : "未知");
            stat.setOrderCount(entry.getValue().size());
            stat.setAmount(entry.getValue().stream().map(SalesOrder::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add));
            customerStats.add(stat);
        }
        customerStats.sort((a, b) -> b.getAmount().compareTo(a.getAmount()));
        vo.setCustomerStats(customerStats);

        Map<String, List<SalesOrder>> dateGroups = orders.stream()
            .collect(Collectors.groupingBy(o -> o.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))));
        List<SalesReportDTO.DailyStat> dailyStats = new ArrayList<>();
        for (Map.Entry<String, List<SalesOrder>> entry : dateGroups.entrySet()) {
            SalesReportDTO.DailyStat stat = new SalesReportDTO.DailyStat();
            stat.setDate(entry.getKey());
            stat.setOrderCount(entry.getValue().size());
            stat.setAmount(entry.getValue().stream().map(SalesOrder::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add));
            dailyStats.add(stat);
        }
        dailyStats.sort(Comparator.comparing(SalesReportDTO.DailyStat::getDate));
        vo.setDailyStats(dailyStats);

        return vo;
    }

    @Override
    public PurchaseReportDTO getPurchaseReport(Long tenantId, String startDate, String endDate) {
        PurchaseReportDTO vo = new PurchaseReportDTO();
        LocalDateTime start = LocalDate.parse(startDate).atStartOfDay();
        LocalDateTime end = LocalDate.parse(endDate).atTime(LocalTime.MAX);

        List<PurchaseOrder> orders = purchaseOrderMapper.selectList(
            new LambdaQueryWrapper<PurchaseOrder>()
                .eq(PurchaseOrder::getTenantId, tenantId)
                .ge(PurchaseOrder::getCreatedAt, start)
                .le(PurchaseOrder::getCreatedAt, end)
                .ne(PurchaseOrder::getOrderStatus, 5)
        );

        PurchaseReportDTO.Summary summary = new PurchaseReportDTO.Summary();
        summary.setTotalAmount(orders.stream().map(PurchaseOrder::getTotalAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add));
        summary.setTotalOrders(orders.size());
        vo.setSummary(summary);

        if (orders.isEmpty()) {
            vo.setProductStats(Collections.emptyList());
            vo.setSupplierStats(Collections.emptyList());
            vo.setDailyStats(Collections.emptyList());
            return vo;
        }

        Set<Long> orderIds = orders.stream().map(PurchaseOrder::getId).collect(Collectors.toSet());

        List<PurchaseOrderItem> items = purchaseOrderItemMapper.selectList(
            new LambdaQueryWrapper<PurchaseOrderItem>()
                .in(PurchaseOrderItem::getOrderId, orderIds)
        );
        Map<Long, List<PurchaseOrderItem>> productGroups = items.stream()
            .collect(Collectors.groupingBy(PurchaseOrderItem::getProductId));
        List<PurchaseReportDTO.ProductStat> productStats = new ArrayList<>();
        int totalQty = 0;
        for (Map.Entry<Long, List<PurchaseOrderItem>> entry : productGroups.entrySet()) {
            PurchaseReportDTO.ProductStat stat = new PurchaseReportDTO.ProductStat();
            stat.setProductId(entry.getKey());
            PurchaseOrderItem first = entry.getValue().get(0);
            stat.setProductName(first.getProductName());
            stat.setProductCode(first.getProductCode());
            int qty = entry.getValue().stream().mapToInt(i -> i.getQuantity().intValue()).sum();
            BigDecimal amt = entry.getValue().stream().map(PurchaseOrderItem::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            stat.setQuantity(qty);
            stat.setAmount(amt);
            productStats.add(stat);
            totalQty += qty;
        }
        productStats.sort((a, b) -> b.getAmount().compareTo(a.getAmount()));
        vo.setProductStats(productStats);
        summary.setTotalQuantity(totalQty);
        summary.setAvgOrderAmount(summary.getTotalAmount().divide(
            BigDecimal.valueOf(summary.getTotalOrders()), 2, RoundingMode.HALF_UP));

        Map<Long, List<PurchaseOrder>> supplierGroups = orders.stream()
            .collect(Collectors.groupingBy(PurchaseOrder::getSupplierId));
        Set<Long> supplierIds = supplierGroups.keySet();
        Map<Long, Supplier> supplierMap = supplierMapper.selectBatchIds(supplierIds).stream()
            .collect(Collectors.toMap(Supplier::getId, s -> s));
        List<PurchaseReportDTO.SupplierStat> supplierStats = new ArrayList<>();
        for (Map.Entry<Long, List<PurchaseOrder>> entry : supplierGroups.entrySet()) {
            PurchaseReportDTO.SupplierStat stat = new PurchaseReportDTO.SupplierStat();
            stat.setSupplierId(entry.getKey());
            Supplier s = supplierMap.get(entry.getKey());
            stat.setSupplierName(s != null ? s.getSupplierName() : "未知");
            stat.setOrderCount(entry.getValue().size());
            stat.setAmount(entry.getValue().stream().map(PurchaseOrder::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add));
            supplierStats.add(stat);
        }
        supplierStats.sort((a, b) -> b.getAmount().compareTo(a.getAmount()));
        vo.setSupplierStats(supplierStats);

        Map<String, List<PurchaseOrder>> dateGroups = orders.stream()
            .collect(Collectors.groupingBy(o -> o.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))));
        List<PurchaseReportDTO.DailyStat> dailyStats = new ArrayList<>();
        for (Map.Entry<String, List<PurchaseOrder>> entry : dateGroups.entrySet()) {
            PurchaseReportDTO.DailyStat stat = new PurchaseReportDTO.DailyStat();
            stat.setDate(entry.getKey());
            stat.setOrderCount(entry.getValue().size());
            stat.setAmount(entry.getValue().stream().map(PurchaseOrder::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add));
            dailyStats.add(stat);
        }
        dailyStats.sort(Comparator.comparing(PurchaseReportDTO.DailyStat::getDate));
        vo.setDailyStats(dailyStats);

        return vo;
    }

    @Override
    public InventoryReportDTO getInventoryReport(Long tenantId, Long warehouseId) {
        InventoryReportDTO vo = new InventoryReportDTO();

        ApiResponse<PageResult<InventoryDTO>> resp = inventoryFeignClient.getInventoryPage(tenantId, 1, 1000, warehouseId, null);
        List<InventoryDTO> inventories = (resp.getData() != null && resp.getData().getRecords() != null)
            ? resp.getData().getRecords() : Collections.emptyList();

        InventoryReportDTO.Summary summary = new InventoryReportDTO.Summary();
        summary.setTotalProducts(inventories.size());
        summary.setTotalQuantity(inventories.stream().map(InventoryDTO::getQuantity)
            .reduce(BigDecimal.ZERO, BigDecimal::add));
        summary.setTotalValue(inventories.stream()
            .map(i -> i.getQuantity().multiply(i.getCostPrice() != null ? i.getCostPrice() : BigDecimal.ZERO))
            .reduce(BigDecimal.ZERO, BigDecimal::add));
        summary.setAlertCount((int) inventories.stream()
            .filter(i -> i.getStockMin() != null && i.getStockMin().compareTo(BigDecimal.ZERO) > 0
                && i.getQuantity().compareTo(i.getStockMin()) < 0)
            .count());
        vo.setSummary(summary);

        List<InventoryReportDTO.InventoryDetail> details = new ArrayList<>();
        for (InventoryDTO inv : inventories) {
            InventoryReportDTO.InventoryDetail detail = new InventoryReportDTO.InventoryDetail();
            detail.setWarehouseId(inv.getWarehouseId());
            detail.setProductId(inv.getProductId());
            detail.setWarehouseName(inv.getWarehouseName());
            detail.setProductName(inv.getProductName());
            detail.setProductCode(inv.getProductSku());
            detail.setQuantity(inv.getQuantity());
            detail.setCostPrice(inv.getCostPrice());
            detail.setTotalValue(inv.getQuantity().multiply(
                inv.getCostPrice() != null ? inv.getCostPrice() : BigDecimal.ZERO));
            detail.setStockMin(inv.getStockMin());
            detail.setStockMax(inv.getStockMax());
            details.add(detail);
        }
        vo.setDetails(details);

        List<InventoryReportDTO.AgingStat> agingStats = new ArrayList<>();
        InventoryReportDTO.AgingStat stat1 = new InventoryReportDTO.AgingStat();
        stat1.setAgingRange("0-30天");
        stat1.setProductCount(inventories.size());
        stat1.setQuantity(summary.getTotalQuantity());
        stat1.setValue(summary.getTotalValue());
        agingStats.add(stat1);
        vo.setAgingStats(agingStats);

        return vo;
    }

    @Override
    public ProfitReportDTO getProfitReport(Long tenantId, String startDate, String endDate) {
        ProfitReportDTO report = new ProfitReportDTO();
        LocalDateTime start = LocalDate.parse(startDate).atStartOfDay();
        LocalDateTime end = LocalDate.parse(endDate).atTime(LocalTime.MAX);

        List<SalesOrder> salesOrders = salesOrderMapper.selectList(
            new LambdaQueryWrapper<SalesOrder>()
                .eq(SalesOrder::getTenantId, tenantId)
                .ge(SalesOrder::getCreatedAt, start)
                .le(SalesOrder::getCreatedAt, end)
                .ne(SalesOrder::getOrderStatus, 5)
        );

        List<PurchaseOrder> purchaseOrders = purchaseOrderMapper.selectList(
            new LambdaQueryWrapper<PurchaseOrder>()
                .eq(PurchaseOrder::getTenantId, tenantId)
                .ge(PurchaseOrder::getCreatedAt, start)
                .le(PurchaseOrder::getCreatedAt, end)
                .ne(PurchaseOrder::getOrderStatus, 5)
        );

        BigDecimal totalRevenue = salesOrders.stream()
            .map(SalesOrder::getTotalAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<Long, List<SalesOrderItem>> salesItemsByProduct = new LinkedHashMap<>();
        Map<String, BigDecimal> revenueByDate = new LinkedHashMap<>();
        if (!salesOrders.isEmpty()) {
            Set<Long> salesOrderIds = salesOrders.stream().map(SalesOrder::getId).collect(Collectors.toSet());
            List<SalesOrderItem> salesItems = salesOrderItemMapper.selectList(
                new LambdaQueryWrapper<SalesOrderItem>()
                    .in(SalesOrderItem::getOrderId, salesOrderIds)
            );
            salesItemsByProduct = salesItems.stream()
                .collect(Collectors.groupingBy(SalesOrderItem::getProductId));

            Map<Long, SalesOrder> orderMap = salesOrders.stream()
                .collect(Collectors.toMap(SalesOrder::getId, o -> o));
            for (SalesOrderItem item : salesItems) {
                SalesOrder order = orderMap.get(item.getOrderId());
                if (order != null) {
                    String dateKey = order.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                    revenueByDate.merge(dateKey, item.getAmount(), BigDecimal::add);
                }
            }
        }

        Map<Long, List<PurchaseOrderItem>> purchaseItemsByProduct = new LinkedHashMap<>();
        Map<String, BigDecimal> costByDate = new LinkedHashMap<>();
        if (!purchaseOrders.isEmpty()) {
            Set<Long> purchaseOrderIds = purchaseOrders.stream().map(PurchaseOrder::getId).collect(Collectors.toSet());
            List<PurchaseOrderItem> purchaseItems = purchaseOrderItemMapper.selectList(
                new LambdaQueryWrapper<PurchaseOrderItem>()
                    .in(PurchaseOrderItem::getOrderId, purchaseOrderIds)
            );
            purchaseItemsByProduct = purchaseItems.stream()
                .collect(Collectors.groupingBy(PurchaseOrderItem::getProductId));

            Map<Long, PurchaseOrder> poMap = purchaseOrders.stream()
                .collect(Collectors.toMap(PurchaseOrder::getId, o -> o));
            for (PurchaseOrderItem item : purchaseItems) {
                PurchaseOrder order = poMap.get(item.getOrderId());
                if (order != null) {
                    String dateKey = order.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                    costByDate.merge(dateKey, item.getAmount(), BigDecimal::add);
                }
            }
        }

        BigDecimal totalCost = purchaseOrders.stream()
            .map(PurchaseOrder::getTotalAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalProfit = totalRevenue.subtract(totalCost);
        BigDecimal profitMargin = totalRevenue.compareTo(BigDecimal.ZERO) > 0
            ? totalProfit.divide(totalRevenue, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100))
            : BigDecimal.ZERO;

        ProfitReportDTO.Summary summary = new ProfitReportDTO.Summary();
        summary.setTotalRevenue(totalRevenue);
        summary.setTotalCost(totalCost);
        summary.setTotalProfit(totalProfit);
        summary.setProfitMargin(profitMargin);
        report.setSummary(summary);

        Set<Long> allProductIds = new LinkedHashSet<>();
        allProductIds.addAll(salesItemsByProduct.keySet());
        allProductIds.addAll(purchaseItemsByProduct.keySet());
        Map<Long, Product> productMap = allProductIds.isEmpty() ? Collections.emptyMap()
            : productMapper.selectBatchIds(allProductIds).stream()
                .collect(Collectors.toMap(Product::getId, p -> p));

        List<ProfitReportDTO.ProductProfit> productProfits = new ArrayList<>();
        for (Long productId : allProductIds) {
            ProfitReportDTO.ProductProfit pp = new ProfitReportDTO.ProductProfit();
            pp.setProductId(productId);
            Product product = productMap.get(productId);
            pp.setProductName(product != null ? product.getProductName() : "未知");
            pp.setProductCode(product != null ? product.getProductCode() : "");

            List<SalesOrderItem> salesItems = salesItemsByProduct.getOrDefault(productId, Collections.emptyList());
            BigDecimal revenue = salesItems.stream().map(SalesOrderItem::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal qty = salesItems.stream().map(SalesOrderItem::getQuantity)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            pp.setQuantity(qty);
            pp.setRevenue(revenue);

            List<PurchaseOrderItem> purchaseItems = purchaseItemsByProduct.getOrDefault(productId, Collections.emptyList());
            BigDecimal cost = purchaseItems.stream().map(PurchaseOrderItem::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            if (cost.compareTo(BigDecimal.ZERO) == 0 && product != null && product.getCostPrice() != null) {
                cost = product.getCostPrice().multiply(qty);
            }
            pp.setCost(cost);

            BigDecimal profit = revenue.subtract(cost);
            pp.setProfit(profit);
            pp.setProfitMargin(revenue.compareTo(BigDecimal.ZERO) > 0
                ? profit.divide(revenue, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100))
                : BigDecimal.ZERO);
            productProfits.add(pp);
        }
        productProfits.sort((a, b) -> b.getProfit().compareTo(a.getProfit()));
        report.setProductProfits(productProfits);

        Set<String> allDates = new LinkedHashSet<>();
        allDates.addAll(revenueByDate.keySet());
        allDates.addAll(costByDate.keySet());
        List<ProfitReportDTO.DailyProfit> dailyProfits = new ArrayList<>();
        for (String date : allDates) {
            ProfitReportDTO.DailyProfit dp = new ProfitReportDTO.DailyProfit();
            dp.setDate(date);
            BigDecimal dayRevenue = revenueByDate.getOrDefault(date, BigDecimal.ZERO);
            BigDecimal dayCost = costByDate.getOrDefault(date, BigDecimal.ZERO);
            dp.setRevenue(dayRevenue);
            dp.setCost(dayCost);
            dp.setProfit(dayRevenue.subtract(dayCost));
            dailyProfits.add(dp);
        }
        dailyProfits.sort(Comparator.comparing(ProfitReportDTO.DailyProfit::getDate));
        report.setDailyProfits(dailyProfits);

        return report;
    }

    @Override
    public SupplierStatementDTO getSupplierStatement(Long tenantId, Long supplierId,
                                                      String startDate, String endDate) {
        SupplierStatementDTO statement = new SupplierStatementDTO();

        Supplier supplier = supplierMapper.selectById(supplierId);
        statement.setSupplierId(supplierId);
        statement.setSupplierName(supplier != null ? supplier.getSupplierName() : "未知");

        LocalDateTime start = LocalDate.parse(startDate).atStartOfDay();
        LocalDateTime end = LocalDate.parse(endDate).atTime(LocalTime.MAX);

        List<PurchaseOrder> beforeOrders = purchaseOrderMapper.selectList(
            new LambdaQueryWrapper<PurchaseOrder>()
                .eq(PurchaseOrder::getTenantId, tenantId)
                .eq(PurchaseOrder::getSupplierId, supplierId)
                .lt(PurchaseOrder::getCreatedAt, start)
                .ne(PurchaseOrder::getOrderStatus, 5)
        );
        BigDecimal openingBalance = beforeOrders.stream()
            .map(PurchaseOrder::getTotalAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        statement.setOpeningBalance(openingBalance);

        List<PurchaseOrder> periodOrders = purchaseOrderMapper.selectList(
            new LambdaQueryWrapper<PurchaseOrder>()
                .eq(PurchaseOrder::getTenantId, tenantId)
                .eq(PurchaseOrder::getSupplierId, supplierId)
                .ge(PurchaseOrder::getCreatedAt, start)
                .le(PurchaseOrder::getCreatedAt, end)
                .ne(PurchaseOrder::getOrderStatus, 5)
                .orderByAsc(PurchaseOrder::getCreatedAt)
        );
        BigDecimal purchaseAmount = periodOrders.stream()
            .map(PurchaseOrder::getTotalAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        statement.setPurchaseAmount(purchaseAmount);

        BigDecimal paymentAmount = periodOrders.stream()
            .filter(o -> o.getOrderStatus() != null && o.getOrderStatus() >= 2)
            .map(o -> o.getPaidAmount() != null ? o.getPaidAmount() : BigDecimal.ZERO)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        statement.setPaymentAmount(paymentAmount);

        BigDecimal closingBalance = openingBalance.add(purchaseAmount).subtract(paymentAmount);
        statement.setClosingBalance(closingBalance);

        List<SupplierStatementDTO.StatementItem> items = new ArrayList<>();
        BigDecimal runningBalance = openingBalance;

        if (openingBalance.compareTo(BigDecimal.ZERO) != 0) {
            SupplierStatementDTO.StatementItem opening = new SupplierStatementDTO.StatementItem();
            opening.setDate(startDate);
            opening.setDocumentNo("-");
            opening.setType("opening");
            opening.setDescription("期初余额");
            opening.setDebit(BigDecimal.ZERO);
            opening.setCredit(BigDecimal.ZERO);
            opening.setBalance(openingBalance);
            items.add(opening);
        }

        for (PurchaseOrder order : periodOrders) {
            SupplierStatementDTO.StatementItem item = new SupplierStatementDTO.StatementItem();
            item.setDate(order.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
            item.setDocumentNo(order.getOrderNo());
            item.setType("purchase");
            item.setDescription("采购订单");
            item.setDebit(order.getTotalAmount());
            item.setCredit(BigDecimal.ZERO);
            runningBalance = runningBalance.add(order.getTotalAmount());
            item.setBalance(runningBalance);
            items.add(item);
        }

        for (PurchaseOrder order : periodOrders) {
            if (order.getOrderStatus() != null && order.getOrderStatus() >= 2
                && order.getPaidAmount() != null && order.getPaidAmount().compareTo(BigDecimal.ZERO) > 0) {
                SupplierStatementDTO.StatementItem item = new SupplierStatementDTO.StatementItem();
                item.setDate(order.getApprovedAt() != null
                    ? order.getApprovedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                    : order.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
                item.setDocumentNo(order.getOrderNo());
                item.setType("payment");
                item.setDescription("付款（采购单核销）");
                item.setDebit(BigDecimal.ZERO);
                item.setCredit(order.getPaidAmount());
                runningBalance = runningBalance.subtract(order.getPaidAmount());
                item.setBalance(runningBalance);
                items.add(item);
            }
        }

        statement.setItems(items);
        return statement;
    }

    @Override
    public CustomerStatementDTO getCustomerStatement(Long tenantId, Long customerId,
                                                      String startDate, String endDate) {
        CustomerStatementDTO statement = new CustomerStatementDTO();

        Customer customer = customerMapper.selectById(customerId);
        statement.setCustomerId(customerId);
        statement.setCustomerName(customer != null ? customer.getCustomerName() : "未知");

        LocalDateTime start = LocalDate.parse(startDate).atStartOfDay();
        LocalDateTime end = LocalDate.parse(endDate).atTime(LocalTime.MAX);

        List<SalesOrder> beforeOrders = salesOrderMapper.selectList(
            new LambdaQueryWrapper<SalesOrder>()
                .eq(SalesOrder::getTenantId, tenantId)
                .eq(SalesOrder::getCustomerId, customerId)
                .lt(SalesOrder::getCreatedAt, start)
                .ne(SalesOrder::getOrderStatus, 5)
        );
        BigDecimal openingBalance = beforeOrders.stream()
            .map(SalesOrder::getTotalAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        statement.setOpeningBalance(openingBalance);

        List<SalesOrder> periodOrders = salesOrderMapper.selectList(
            new LambdaQueryWrapper<SalesOrder>()
                .eq(SalesOrder::getTenantId, tenantId)
                .eq(SalesOrder::getCustomerId, customerId)
                .ge(SalesOrder::getCreatedAt, start)
                .le(SalesOrder::getCreatedAt, end)
                .ne(SalesOrder::getOrderStatus, 5)
                .orderByAsc(SalesOrder::getCreatedAt)
        );
        BigDecimal salesAmount = periodOrders.stream()
            .map(SalesOrder::getTotalAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        statement.setSalesAmount(salesAmount);

        BigDecimal receiptAmount = periodOrders.stream()
            .filter(o -> o.getOrderStatus() != null && o.getOrderStatus() >= 2)
            .map(o -> o.getReceivedAmount() != null ? o.getReceivedAmount() : BigDecimal.ZERO)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        statement.setReceiptAmount(receiptAmount);

        BigDecimal closingBalance = openingBalance.add(salesAmount).subtract(receiptAmount);
        statement.setClosingBalance(closingBalance);

        List<CustomerStatementDTO.StatementItem> items = new ArrayList<>();
        BigDecimal runningBalance = openingBalance;

        if (openingBalance.compareTo(BigDecimal.ZERO) != 0) {
            CustomerStatementDTO.StatementItem opening = new CustomerStatementDTO.StatementItem();
            opening.setDate(startDate);
            opening.setDocumentNo("-");
            opening.setType("opening");
            opening.setDescription("期初余额");
            opening.setDebit(BigDecimal.ZERO);
            opening.setCredit(BigDecimal.ZERO);
            opening.setBalance(openingBalance);
            items.add(opening);
        }

        for (SalesOrder order : periodOrders) {
            CustomerStatementDTO.StatementItem item = new CustomerStatementDTO.StatementItem();
            item.setDate(order.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
            item.setDocumentNo(order.getOrderNo());
            item.setType("sales");
            item.setDescription("销售订单");
            item.setDebit(order.getTotalAmount());
            item.setCredit(BigDecimal.ZERO);
            runningBalance = runningBalance.add(order.getTotalAmount());
            item.setBalance(runningBalance);
            items.add(item);
        }

        for (SalesOrder order : periodOrders) {
            if (order.getOrderStatus() != null && order.getOrderStatus() >= 2
                && order.getReceivedAmount() != null && order.getReceivedAmount().compareTo(BigDecimal.ZERO) > 0) {
                CustomerStatementDTO.StatementItem item = new CustomerStatementDTO.StatementItem();
                item.setDate(order.getApprovedAt() != null
                    ? order.getApprovedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                    : order.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
                item.setDocumentNo(order.getOrderNo());
                item.setType("receipt");
                item.setDescription("收款（销售单核销）");
                item.setDebit(BigDecimal.ZERO);
                item.setCredit(order.getReceivedAmount());
                runningBalance = runningBalance.subtract(order.getReceivedAmount());
                item.setBalance(runningBalance);
                items.add(item);
            }
        }

        statement.setItems(items);
        return statement;
    }

    private List<DashboardDTO.TrendData> getSalesTrend(Long tenantId, int days) {
        List<DashboardDTO.TrendData> trend = new ArrayList<>();
        LocalDate date = LocalDate.now().minusDays(days - 1);
        for (int i = 0; i < days; i++) {
            LocalDateTime dayStart = date.atStartOfDay();
            LocalDateTime dayEnd = date.atTime(LocalTime.MAX);
            List<SalesOrder> orders = salesOrderMapper.selectList(
                new LambdaQueryWrapper<SalesOrder>()
                    .eq(SalesOrder::getTenantId, tenantId)
                    .ge(SalesOrder::getCreatedAt, dayStart)
                    .le(SalesOrder::getCreatedAt, dayEnd)
                    .ne(SalesOrder::getOrderStatus, 5)
            );
            DashboardDTO.TrendData data = new DashboardDTO.TrendData();
            data.setDate(date.format(DateTimeFormatter.ofPattern("MM-dd")));
            data.setAmount(orders.stream().map(SalesOrder::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add));
            trend.add(data);
            date = date.plusDays(1);
        }
        return trend;
    }

    private List<DashboardDTO.TrendData> getPurchaseTrend(Long tenantId, int days) {
        List<DashboardDTO.TrendData> trend = new ArrayList<>();
        LocalDate date = LocalDate.now().minusDays(days - 1);
        for (int i = 0; i < days; i++) {
            LocalDateTime dayStart = date.atStartOfDay();
            LocalDateTime dayEnd = date.atTime(LocalTime.MAX);
            List<PurchaseOrder> orders = purchaseOrderMapper.selectList(
                new LambdaQueryWrapper<PurchaseOrder>()
                    .eq(PurchaseOrder::getTenantId, tenantId)
                    .ge(PurchaseOrder::getCreatedAt, dayStart)
                    .le(PurchaseOrder::getCreatedAt, dayEnd)
                    .ne(PurchaseOrder::getOrderStatus, 5)
            );
            DashboardDTO.TrendData data = new DashboardDTO.TrendData();
            data.setDate(date.format(DateTimeFormatter.ofPattern("MM-dd")));
            data.setAmount(orders.stream().map(PurchaseOrder::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add));
            trend.add(data);
            date = date.plusDays(1);
        }
        return trend;
    }

    private List<DashboardDTO.RankingData> getTopProducts(Long tenantId, int limit) {
        LocalDateTime start = LocalDate.now().minusDays(30).atStartOfDay();
        List<SalesOrder> orders = salesOrderMapper.selectList(
            new LambdaQueryWrapper<SalesOrder>()
                .eq(SalesOrder::getTenantId, tenantId)
                .ge(SalesOrder::getCreatedAt, start)
                .ne(SalesOrder::getOrderStatus, 5)
        );
        if (orders.isEmpty()) return Collections.emptyList();

        Set<Long> orderIds = orders.stream().map(SalesOrder::getId).collect(Collectors.toSet());
        List<SalesOrderItem> items = salesOrderItemMapper.selectList(
            new LambdaQueryWrapper<SalesOrderItem>()
                .in(SalesOrderItem::getOrderId, orderIds)
        );

        Map<Long, List<SalesOrderItem>> groups = items.stream()
            .collect(Collectors.groupingBy(SalesOrderItem::getProductId));
        List<DashboardDTO.RankingData> rankings = new ArrayList<>();
        for (Map.Entry<Long, List<SalesOrderItem>> entry : groups.entrySet()) {
            DashboardDTO.RankingData data = new DashboardDTO.RankingData();
            SalesOrderItem first = entry.getValue().get(0);
            data.setName(first.getProductName());
            data.setQuantity(entry.getValue().stream().mapToInt(i -> i.getQuantity().intValue()).sum());
            data.setAmount(entry.getValue().stream().map(SalesOrderItem::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add));
            rankings.add(data);
        }
        rankings.sort((a, b) -> b.getAmount().compareTo(a.getAmount()));
        return rankings.stream().limit(limit).collect(Collectors.toList());
    }

    private List<DashboardDTO.RankingData> getTopCustomers(Long tenantId, int limit) {
        LocalDateTime start = LocalDate.now().minusDays(30).atStartOfDay();
        List<SalesOrder> orders = salesOrderMapper.selectList(
            new LambdaQueryWrapper<SalesOrder>()
                .eq(SalesOrder::getTenantId, tenantId)
                .ge(SalesOrder::getCreatedAt, start)
                .ne(SalesOrder::getOrderStatus, 5)
        );
        if (orders.isEmpty()) return Collections.emptyList();

        Map<Long, List<SalesOrder>> groups = orders.stream()
            .collect(Collectors.groupingBy(SalesOrder::getCustomerId));
        Set<Long> customerIds = groups.keySet();
        Map<Long, Customer> customerMap = customerMapper.selectBatchIds(customerIds).stream()
            .collect(Collectors.toMap(Customer::getId, c -> c));

        List<DashboardDTO.RankingData> rankings = new ArrayList<>();
        for (Map.Entry<Long, List<SalesOrder>> entry : groups.entrySet()) {
            DashboardDTO.RankingData data = new DashboardDTO.RankingData();
            Customer c = customerMap.get(entry.getKey());
            data.setName(c != null ? c.getCustomerName() : "未知");
            data.setQuantity(entry.getValue().size());
            data.setAmount(entry.getValue().stream().map(SalesOrder::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add));
            rankings.add(data);
        }
        rankings.sort((a, b) -> b.getAmount().compareTo(a.getAmount()));
        return rankings.stream().limit(limit).collect(Collectors.toList());
    }
}
