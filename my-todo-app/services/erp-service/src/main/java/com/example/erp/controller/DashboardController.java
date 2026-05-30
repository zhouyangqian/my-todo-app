package com.example.erp.controller;

import com.example.common.core.result.ApiResponse;
import com.example.erp.api.feign.InventoryFeignClient;
import com.example.erp.entity.Product;
import com.example.erp.entity.PurchaseOrder;
import com.example.erp.entity.SalesOrder;
import com.example.erp.service.ProductService;
import com.example.erp.service.PurchaseOrderService;
import com.example.erp.service.SalesOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * 工作台统计控制器
 * <p>
 * 提供 Dashboard 页面所需的聚合统计数据接口，
 * 汇总商品、销售订单、采购订单、库存预警等多模块信息。
 * </p>
 */
@Tag(name = "工作台统计", description = "Dashboard统计数据API")
@RestController
@RequestMapping("/api/erp/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final SalesOrderService salesOrderService;
    private final PurchaseOrderService purchaseOrderService;
    private final ProductService productService;
    private final InventoryFeignClient inventoryFeignClient;

    @Operation(summary = "获取工作台统计数据")
    @GetMapping("/stats")
    public ApiResponse<Map<String, Object>> getDashboardStats(
            @RequestHeader("X-Tenant-Id") Long tenantId) {
        Map<String, Object> stats = new HashMap<>();

        // 商品总数
        stats.put("productCount", productService.lambdaQuery()
            .eq(Product::getTenantId, tenantId)
            .eq(Product::getDeleted, 0)
            .count());

        // 销售订单统计
        stats.put("salesOrderCount", salesOrderService.lambdaQuery()
            .eq(SalesOrder::getTenantId, tenantId)
            .eq(SalesOrder::getDeleted, 0)
            .count());
        stats.put("pendingSalesOrders", salesOrderService.countByStatus(tenantId, 1));
        stats.put("salesOrderAmount", salesOrderService.sumTotalAmount(tenantId));

        // 采购订单统计
        stats.put("purchaseOrderCount", purchaseOrderService.lambdaQuery()
            .eq(PurchaseOrder::getTenantId, tenantId)
            .eq(PurchaseOrder::getDeleted, 0)
            .count());
        stats.put("pendingPurchaseOrders", purchaseOrderService.countByStatus(tenantId, 1));
        stats.put("purchaseOrderAmount", purchaseOrderService.sumTotalAmount(tenantId));

        // 库存预警
        try {
            var alertResponse = inventoryFeignClient.getAlertInventories(tenantId, null);
            stats.put("lowStockCount", alertResponse.getData() != null ? alertResponse.getData().size() : 0);
        } catch (Exception e) {
            // inventory-service 不可用时返回0
            stats.put("lowStockCount", 0);
        }

        return ApiResponse.success(stats);
    }
}
