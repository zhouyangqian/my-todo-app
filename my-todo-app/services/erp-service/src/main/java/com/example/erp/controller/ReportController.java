package com.example.erp.controller;

import com.example.common.core.result.ApiResponse;
import com.example.erp.dto.*;
import com.example.erp.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 报表统计控制器
 */
@Tag(name = "报表统计", description = "Dashboard和各类统计报表API")
@RestController
@RequestMapping("/api/erp/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @Operation(summary = "获取Dashboard统计数据")
    @GetMapping("/dashboard")
    public ApiResponse<DashboardVO> getDashboard(
            @RequestHeader("X-Tenant-Id") Long tenantId) {
        DashboardVO data = reportService.getDashboardData(tenantId);
        return ApiResponse.success(data);
    }

    @Operation(summary = "获取销售报表")
    @GetMapping("/sales")
    public ApiResponse<SalesReportVO> getSalesReport(
            @RequestHeader("X-Tenant-Id") Long tenantId,
            @RequestParam String startDate,
            @RequestParam String endDate) {
        SalesReportVO report = reportService.getSalesReport(tenantId, startDate, endDate);
        return ApiResponse.success(report);
    }

    @Operation(summary = "获取采购报表")
    @GetMapping("/purchase")
    public ApiResponse<PurchaseReportVO> getPurchaseReport(
            @RequestHeader("X-Tenant-Id") Long tenantId,
            @RequestParam String startDate,
            @RequestParam String endDate) {
        PurchaseReportVO report = reportService.getPurchaseReport(tenantId, startDate, endDate);
        return ApiResponse.success(report);
    }

    @Operation(summary = "获取库存报表")
    @GetMapping("/inventory")
    public ApiResponse<InventoryReportVO> getInventoryReport(
            @RequestHeader("X-Tenant-Id") Long tenantId,
            @RequestParam(required = false) Long warehouseId) {
        InventoryReportVO report = reportService.getInventoryReport(tenantId, warehouseId);
        return ApiResponse.success(report);
    }
}
