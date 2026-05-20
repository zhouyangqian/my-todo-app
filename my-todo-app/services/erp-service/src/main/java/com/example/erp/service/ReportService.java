package com.example.erp.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.erp.api.dto.DashboardDTO;
import com.example.erp.api.dto.SalesReportDTO;
import com.example.erp.api.dto.PurchaseReportDTO;
import com.example.erp.api.dto.InventoryReportDTO;
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
 * 报表统计服务类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReportService {

    private final SalesOrderMapper salesOrderMapper;
    private final SalesOrderItemMapper salesOrderItemMapper;
    private final PurchaseOrderMapper purchaseOrderMapper;
    private final PurchaseOrderItemMapper purchaseOrderItemMapper;
    private final InventoryFeignClient inventoryFeignClient;
    private final CustomerMapper customerMapper;
    private final SupplierMapper supplierMapper;
    private final ProductMapper productMapper;
    private final WarehouseMapper warehouseMapper;

    /**
     * 获取Dashboard统计数据
     */
    public DashboardDTO getDashboardData(Long tenantId) {
        DashboardDTO vo = new DashboardDTO();

        // 今日和本月时间范围
        LocalDate today = LocalDate.now();
        LocalDateTime todayStart = today.atStartOfDay();
        LocalDateTime todayEnd = today.atTime(LocalTime.MAX);
        LocalDateTime monthStart = today.withDayOfMonth(1).atStartOfDay();

        // 今日销售额
        List<SalesOrder> todaySales = salesOrderMapper.selectList(
            new LambdaQueryWrapper<SalesOrder>()
                .eq(SalesOrder::getTenantId, tenantId)
                .ge(SalesOrder::getCreatedAt, todayStart)
                .le(SalesOrder::getCreatedAt, todayEnd)
                .ne(SalesOrder::getOrderStatus, 5)
        );
        vo.setTodaySales(todaySales.stream().map(SalesOrder::getTotalAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add));

        // 本月销售额
        List<SalesOrder> monthSales = salesOrderMapper.selectList(
            new LambdaQueryWrapper<SalesOrder>()
                .eq(SalesOrder::getTenantId, tenantId)
                .ge(SalesOrder::getCreatedAt, monthStart)
                .ne(SalesOrder::getOrderStatus, 5)
        );
        vo.setMonthSales(monthSales.stream().map(SalesOrder::getTotalAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add));

        // 今日采购额
        List<PurchaseOrder> todayPurchases = purchaseOrderMapper.selectList(
            new LambdaQueryWrapper<PurchaseOrder>()
                .eq(PurchaseOrder::getTenantId, tenantId)
                .ge(PurchaseOrder::getCreatedAt, todayStart)
                .le(PurchaseOrder::getCreatedAt, todayEnd)
                .ne(PurchaseOrder::getOrderStatus, 5)
        );
        vo.setTodayPurchases(todayPurchases.stream().map(PurchaseOrder::getTotalAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add));

        // 本月采购额
        List<PurchaseOrder> monthPurchases = purchaseOrderMapper.selectList(
            new LambdaQueryWrapper<PurchaseOrder>()
                .eq(PurchaseOrder::getTenantId, tenantId)
                .ge(PurchaseOrder::getCreatedAt, monthStart)
                .ne(PurchaseOrder::getOrderStatus, 5)
        );
        vo.setMonthPurchases(monthPurchases.stream().map(PurchaseOrder::getTotalAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add));

        // 库存商品种类数
        ApiResponse<PageResult<InventoryDTO>> inventoryResp = inventoryFeignClient.getInventoryPage(tenantId, 1, 1000, null, null);
        List<InventoryDTO> allInventory = (inventoryResp.getData() != null && inventoryResp.getData().getRecords() != null)
            ? inventoryResp.getData().getRecords() : Collections.emptyList();
        vo.setInventoryProductCount((int) allInventory.stream().filter(i -> i.getQuantity() != null && i.getQuantity().compareTo(BigDecimal.ZERO) > 0).count());

        // 库存预警商品数
        ApiResponse<List<InventoryDTO>> alertResp = inventoryFeignClient.getAlertInventories(tenantId, null);
        vo.setAlertProductCount(alertResp.getData() != null ? alertResp.getData().size() : 0);

        // 待审核采购订单数
        Long pendingPO = purchaseOrderMapper.selectCount(
            new LambdaQueryWrapper<PurchaseOrder>()
                .eq(PurchaseOrder::getTenantId, tenantId)
                .eq(PurchaseOrder::getOrderStatus, 1)
        );
        vo.setPendingPurchaseOrders(pendingPO.intValue());

        // 待审核销售订单数
        Long pendingSO = salesOrderMapper.selectCount(
            new LambdaQueryWrapper<SalesOrder>()
                .eq(SalesOrder::getTenantId, tenantId)
                .eq(SalesOrder::getOrderStatus, 1)
        );
        vo.setPendingSalesOrders(pendingSO.intValue());

        // 销售趋势（近7天）
        vo.setSalesTrend(getSalesTrend(tenantId, 7));

        // 采购趋势（近7天）
        vo.setPurchaseTrend(getPurchaseTrend(tenantId, 7));

        // 销售排行（前10商品）
        vo.setTopProducts(getTopProducts(tenantId, 10));

        // 销售排行（前10客户）
        vo.setTopCustomers(getTopCustomers(tenantId, 10));

        return vo;
    }

    /**
     * 获取销售报表
     */
    public SalesReportDTO getSalesReport(Long tenantId, String startDate, String endDate) {
        SalesReportDTO vo = new SalesReportDTO();
        LocalDateTime start = LocalDate.parse(startDate).atStartOfDay();
        LocalDateTime end = LocalDate.parse(endDate).atTime(LocalTime.MAX);

        // 查询时间范围内的销售订单
        List<SalesOrder> orders = salesOrderMapper.selectList(
            new LambdaQueryWrapper<SalesOrder>()
                .eq(SalesOrder::getTenantId, tenantId)
                .ge(SalesOrder::getCreatedAt, start)
                .le(SalesOrder::getCreatedAt, end)
                .ne(SalesOrder::getOrderStatus, 5)
        );

        // 汇总
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

        // 按商品统计
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

        // 按客户统计
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

        // 按日期统计
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

    /**
     * 获取采购报表
     */
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

        // 按商品统计
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

        // 按供应商统计
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

        // 按日期统计
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

    /**
     * 获取库存报表
     */
    public InventoryReportDTO getInventoryReport(Long tenantId, Long warehouseId) {
        InventoryReportDTO vo = new InventoryReportDTO();

        ApiResponse<PageResult<InventoryDTO>> resp = inventoryFeignClient.getInventoryPage(tenantId, 1, 1000, warehouseId, null);
        List<InventoryDTO> inventories = (resp.getData() != null && resp.getData().getRecords() != null)
            ? resp.getData().getRecords() : Collections.emptyList();

        // 汇总
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

        // 库存明细
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

        // 库龄分析（简化版，按库存金额分段）
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

    // 私有方法

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
        // 查询近30天的销售明细
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
