package com.example.erp.controller;

import com.example.common.core.annotation.RequiresPermission;
import com.example.common.core.result.ApiResponse;
import com.example.erp.api.dto.DashboardDTO;
import com.example.erp.api.dto.SalesReportDTO;
import com.example.erp.api.dto.PurchaseReportDTO;
import com.example.erp.api.dto.InventoryReportDTO;
import com.example.erp.api.dto.ProfitReportDTO;
import com.example.erp.api.dto.SupplierStatementDTO;
import com.example.erp.api.dto.CustomerStatementDTO;
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

    @RequiresPermission(code = "erp:report:dashboard", name = "查看Dashboard统计")
    @Operation(summary = "获取Dashboard统计数据")
    @GetMapping("/dashboard")
    public ApiResponse<DashboardDTO> getDashboard(
            @RequestHeader("X-Tenant-Id") Long tenantId) {
        DashboardDTO data = reportService.getDashboardData(tenantId);
        return ApiResponse.success(data);
    }

    @RequiresPermission(code = "erp:report:sales", name = "查看销售报表")
    @Operation(summary = "获取销售报表")
    @GetMapping("/sales")
    public ApiResponse<SalesReportDTO> getSalesReport(
            @RequestHeader("X-Tenant-Id") Long tenantId,
            @RequestParam String startDate,
            @RequestParam String endDate) {
        SalesReportDTO report = reportService.getSalesReport(tenantId, startDate, endDate);
        return ApiResponse.success(report);
    }

    @RequiresPermission(code = "erp:report:purchase", name = "查看采购报表")
    @Operation(summary = "获取采购报表")
    @GetMapping("/purchase")
    public ApiResponse<PurchaseReportDTO> getPurchaseReport(
            @RequestHeader("X-Tenant-Id") Long tenantId,
            @RequestParam String startDate,
            @RequestParam String endDate) {
        PurchaseReportDTO report = reportService.getPurchaseReport(tenantId, startDate, endDate);
        return ApiResponse.success(report);
    }

    @RequiresPermission(code = "erp:report:inventory", name = "查看库存报表")
    @Operation(summary = "获取库存报表")
    @GetMapping("/inventory")
    public ApiResponse<InventoryReportDTO> getInventoryReport(
            @RequestHeader("X-Tenant-Id") Long tenantId,
            @RequestParam(required = false) Long warehouseId) {
        InventoryReportDTO report = reportService.getInventoryReport(tenantId, warehouseId);
        return ApiResponse.success(report);
    }

    @RequiresPermission(code = "erp:report:profit", name = "查看利润分析")
    @Operation(summary = "获取利润分析报表")
    @GetMapping("/profit")
    public ApiResponse<ProfitReportDTO> getProfitReport(
            @RequestHeader("X-Tenant-Id") Long tenantId,
            @RequestParam String startDate,
            @RequestParam String endDate) {
        ProfitReportDTO report = reportService.getProfitReport(tenantId, startDate, endDate);
        return ApiResponse.success(report);
    }

    @RequiresPermission(code = "erp:report:supplierStatement", name = "查看供应商对账单")
    @Operation(summary = "获取供应商对账单")
    @GetMapping("/supplier-statement/{supplierId}")
    public ApiResponse<SupplierStatementDTO> getSupplierStatement(
            @RequestHeader("X-Tenant-Id") Long tenantId,
            @PathVariable Long supplierId,
            @RequestParam String startDate,
            @RequestParam String endDate) {
        SupplierStatementDTO statement = reportService.getSupplierStatement(tenantId, supplierId, startDate, endDate);
        return ApiResponse.success(statement);
    }

    @RequiresPermission(code = "erp:report:customerStatement", name = "查看客户对账单")
    @Operation(summary = "获取客户对账单")
    @GetMapping("/customer-statement/{customerId}")
    public ApiResponse<CustomerStatementDTO> getCustomerStatement(
            @RequestHeader("X-Tenant-Id") Long tenantId,
            @PathVariable Long customerId,
            @RequestParam String startDate,
            @RequestParam String endDate) {
        CustomerStatementDTO statement = reportService.getCustomerStatement(tenantId, customerId, startDate, endDate);
        return ApiResponse.success(statement);
    }
}
