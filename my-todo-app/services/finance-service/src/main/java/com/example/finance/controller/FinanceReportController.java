package com.example.finance.controller;

import com.example.common.core.annotation.RequiresPermission;
import com.example.common.core.result.ApiResponse;
import com.example.finance.entity.FinReport;
import com.example.finance.service.FinanceReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;

/**
 * 财务报表控制器
 */
@Tag(name = "财务报表", description = "财务报表生成和查询API")
@RestController
@RequestMapping("/api/finance/reports")
@RequiredArgsConstructor
public class FinanceReportController {

    private final FinanceReportService reportService;

    @RequiresPermission(code = "finance:report:generate", name = "生成利润表")
    @Operation(summary = "生成利润表")
    @PostMapping("/generate-income-statement")
    public ApiResponse<FinReport> generateIncomeStatement(
            @RequestHeader("X-Tenant-Id") Long tenantId,
            @RequestHeader("X-User-Id") Long userId,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        return ApiResponse.success(reportService.generateIncomeStatement(tenantId, startDate, endDate, userId));
    }

    @RequiresPermission(code = "finance:report:generate", name = "生成资产负债表")
    @Operation(summary = "生成资产负债表")
    @PostMapping("/generate-balance-sheet")
    public ApiResponse<FinReport> generateBalanceSheet(
            @RequestHeader("X-Tenant-Id") Long tenantId,
            @RequestHeader("X-User-Id") Long userId,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate asOfDate) {
        return ApiResponse.success(reportService.generateBalanceSheet(tenantId, asOfDate, userId));
    }

    @RequiresPermission(code = "finance:report:generate", name = "生成现金流量表")
    @Operation(summary = "生成现金流量表")
    @PostMapping("/generate-cash-flow")
    public ApiResponse<FinReport> generateCashFlow(
            @RequestHeader("X-Tenant-Id") Long tenantId,
            @RequestHeader("X-User-Id") Long userId,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        return ApiResponse.success(reportService.generateCashFlowStatement(tenantId, startDate, endDate, userId));
    }

    @RequiresPermission(code = "finance:report:list", name = "查询报表列表")
    @Operation(summary = "获取报表列表")
    @GetMapping("/get-report-list")
    public ApiResponse<List<FinReport>> getReportList(
            @RequestHeader("X-Tenant-Id") Long tenantId,
            @RequestParam(required = false) Integer reportType) {
        return ApiResponse.success(reportService.getReportList(tenantId, reportType));
    }

    @RequiresPermission(code = "finance:report:lock", name = "锁定报表")
    @Operation(summary = "锁定报表")
    @PostMapping("/lock-report/{id}")
    public ApiResponse<Void> lockReport(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long userId) {
        reportService.lockReport(id, userId);
        return ApiResponse.success();
    }

    @RequiresPermission(code = "finance:report:export", name = "导出财务报表")
    @Operation(summary = "导出报表到Excel")
    @GetMapping("/{id}/export/excel")
    public void exportExcel(@PathVariable Long id, HttpServletResponse response) throws IOException {
        FinReport report = reportService.getById(id);
        if (report == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.getWriter().write("报表不存在");
            return;
        }

        String fileName = URLEncoder.encode(
                "财务报表-" + report.getReportPeriod(), StandardCharsets.UTF_8);
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment;filename=" + fileName + ".xlsx");

        reportService.exportToExcel(id, response.getOutputStream());
    }

    @RequiresPermission(code = "finance:report:export", name = "导出财务报表")
    @Operation(summary = "导出报表到PDF")
    @GetMapping("/{id}/export/pdf")
    public void exportPdf(@PathVariable Long id, HttpServletResponse response) throws IOException {
        FinReport report = reportService.getById(id);
        if (report == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.getWriter().write("报表不存在");
            return;
        }

        String fileName = URLEncoder.encode(
                "财务报表-" + report.getReportPeriod(), StandardCharsets.UTF_8);
        response.setContentType("text/plain; charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment;filename=" + fileName + ".txt");

        reportService.exportToPdf(id, response.getOutputStream());
    }
}
