package com.example.erp.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.common.core.annotation.RequiresPermission;
import com.example.common.core.result.ApiResponse;
import com.example.common.core.result.PageResult;
import com.example.erp.api.vo.CreateSalesQuotationVO;
import com.example.erp.api.dto.SalesQuotationDTO;
import com.example.erp.entity.SalesOrder;
import com.example.erp.entity.SalesQuotation;
import com.example.erp.service.SalesQuotationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 销售报价单管理控制器
 */
@Tag(name = "销售报价单管理", description = "销售报价单增删改查及状态流转API")
@RestController
@RequestMapping("/api/erp/sales-quotations")
@RequiredArgsConstructor
public class SalesQuotationController {

    private final SalesQuotationService salesQuotationService;

    @RequiresPermission(code = "erp:salesQuotation:list", name = "查询销售报价单列表")
    @Operation(summary = "分页查询销售报价单")
    @GetMapping
    public ApiResponse<PageResult<SalesQuotationDTO>> getQuotationPage(
            @RequestHeader("X-Tenant-Id") Long tenantId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long customerId,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        Page<SalesQuotationDTO> result = salesQuotationService.getQuotationPage(tenantId, page, size, customerId, status, startDate, endDate);
        PageResult<SalesQuotationDTO> pageResult = PageResult.of(result.getRecords(), result.getTotal(), result.getCurrent(), result.getSize());
        return ApiResponse.success(pageResult);
    }

    @RequiresPermission(code = "erp:salesQuotation:create", name = "新建销售报价单")
    @Operation(summary = "创建销售报价单")
    @PostMapping
    public ApiResponse<SalesQuotation> createQuotation(
            @Valid @RequestBody CreateSalesQuotationVO request,
            @RequestHeader("X-Tenant-Id") Long tenantId) {
        SalesQuotation quotation = salesQuotationService.createQuotation(request, tenantId);
        return ApiResponse.success(quotation);
    }

    @RequiresPermission(code = "erp:salesQuotation:detail", name = "查询销售报价单详情")
    @Operation(summary = "获取销售报价单详情")
    @GetMapping("/{id}")
    public ApiResponse<SalesQuotationDTO> getQuotationById(@PathVariable Long id) {
        SalesQuotationDTO dto = salesQuotationService.getQuotationById(id);
        return ApiResponse.success(dto);
    }

    @RequiresPermission(code = "erp:salesQuotation:update", name = "更新销售报价单")
    @Operation(summary = "更新销售报价单")
    @PutMapping("/{id}")
    public ApiResponse<SalesQuotation> updateQuotation(
            @PathVariable Long id,
            @Valid @RequestBody CreateSalesQuotationVO request) {
        SalesQuotation quotation = salesQuotationService.updateQuotation(id, request);
        return ApiResponse.success(quotation);
    }

    @RequiresPermission(code = "erp:salesQuotation:send", name = "发送销售报价单")
    @Operation(summary = "发送销售报价单")
    @PostMapping("/{id}/send")
    public ApiResponse<Void> sendQuotation(@PathVariable Long id) {
        salesQuotationService.sendQuotation(id);
        return ApiResponse.success();
    }

    @RequiresPermission(code = "erp:salesQuotation:accept", name = "接受销售报价单")
    @Operation(summary = "接受销售报价单")
    @PostMapping("/{id}/accept")
    public ApiResponse<Void> acceptQuotation(@PathVariable Long id) {
        salesQuotationService.acceptQuotation(id);
        return ApiResponse.success();
    }

    @RequiresPermission(code = "erp:salesQuotation:reject", name = "拒绝销售报价单")
    @Operation(summary = "拒绝销售报价单")
    @PostMapping("/{id}/reject")
    public ApiResponse<Void> rejectQuotation(@PathVariable Long id) {
        salesQuotationService.rejectQuotation(id);
        return ApiResponse.success();
    }

    @RequiresPermission(code = "erp:salesQuotation:convert", name = "报价单转销售订单")
    @Operation(summary = "报价单转销售订单")
    @PostMapping("/{id}/convert-to-order")
    public ApiResponse<SalesOrder> convertToOrder(
            @PathVariable Long id,
            @RequestParam Long warehouseId) {
        SalesOrder order = salesQuotationService.convertToOrder(id, warehouseId);
        return ApiResponse.success(order);
    }
}
