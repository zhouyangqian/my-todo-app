package com.example.finance.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.common.core.annotation.RequiresPermission;
import com.example.common.core.result.ApiResponse;
import com.example.common.core.result.PageResult;
import com.example.finance.entity.Invoice;
import com.example.finance.service.InvoiceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 发票管理控制器
 */
@Tag(name = "发票管理", description = "发票相关API")
@RestController
@RequestMapping("/api/finance/invoices")
@RequiredArgsConstructor
public class InvoiceController {

    private final InvoiceService invoiceService;

    @RequiresPermission(code = "finance:invoice:list", name = "查询发票列表")
    @Operation(summary = "分页查询发票")
    @GetMapping("/get-invoice-page")
    public ApiResponse<PageResult<Invoice>> getInvoicePage(
            @RequestHeader("X-Tenant-Id") Long tenantId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Integer invoiceType,
            @RequestParam(required = false) Integer invoiceDirection,
            @RequestParam(required = false) Integer status) {
        Page<Invoice> result = invoiceService.getPage(tenantId, page, size, invoiceType, invoiceDirection, status);
        PageResult<Invoice> pageResult = PageResult.of(result.getRecords(), result.getTotal(), result.getCurrent(), result.getSize());
        return ApiResponse.success(pageResult);
    }

    @RequiresPermission(code = "finance:invoice:detail", name = "查询发票详情")
    @Operation(summary = "获取发票详情")
    @GetMapping("/get-invoice/{id}")
    public ApiResponse<Invoice> getInvoice(@PathVariable Long id) {
        return ApiResponse.success(invoiceService.getInvoiceById(id));
    }

    @RequiresPermission(code = "finance:invoice:create", name = "创建发票")
    @Operation(summary = "创建发票")
    @PostMapping("/create-invoice")
    public ApiResponse<Invoice> createInvoice(
            @RequestBody Invoice invoice,
            @RequestHeader("X-Tenant-Id") Long tenantId,
            @RequestHeader("X-User-Id") Long userId) {
        invoice.setTenantId(tenantId);
        invoice.setCreatedBy(userId);
        return ApiResponse.success(invoiceService.create(invoice));
    }

    @RequiresPermission(code = "finance:invoice:update", name = "更新发票")
    @Operation(summary = "更新发票")
    @PutMapping("/update-invoice/{id}")
    public ApiResponse<Invoice> updateInvoice(
            @PathVariable Long id,
            @RequestBody Invoice invoice,
            @RequestHeader("X-User-Id") Long userId) {
        invoice.setId(id);
        invoice.setUpdatedBy(userId);
        return ApiResponse.success(invoiceService.update(invoice));
    }

    @RequiresPermission(code = "finance:invoice:delete", name = "删除发票")
    @Operation(summary = "删除发票")
    @DeleteMapping("/delete-invoice/{id}")
    public ApiResponse<Void> deleteInvoice(@PathVariable Long id) {
        invoiceService.delete(id);
        return ApiResponse.success();
    }

    @RequiresPermission(code = "finance:invoice:void", name = "作废发票")
    @Operation(summary = "作废发票")
    @PostMapping("/void-invoice/{id}")
    public ApiResponse<Void> voidInvoice(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long userId) {
        invoiceService.voidInvoice(id, userId);
        return ApiResponse.success();
    }

    @RequiresPermission(code = "finance:invoice:list", name = "查询发票统计")
    @Operation(summary = "获取发票统计")
    @GetMapping("/get-statistics")
    public ApiResponse<Map<String, Object>> getStatistics(
            @RequestHeader("X-Tenant-Id") Long tenantId) {
        return ApiResponse.success(invoiceService.getStatistics(tenantId));
    }
}
