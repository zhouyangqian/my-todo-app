package com.example.erp.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.common.core.annotation.RequiresPermission;
import com.example.common.core.result.ApiResponse;
import com.example.common.core.result.PageResult;
import com.example.erp.entity.SalesReturn;
import com.example.erp.service.SalesReturnService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 销售退货管理控制器
 */
@Tag(name = "销售退货管理", description = "销售退货增删改查API")
@RestController
@RequestMapping("/api/erp/sales-returns")
@RequiredArgsConstructor
public class SalesReturnController {

    private final SalesReturnService salesReturnService;

    @RequiresPermission(code = "erp:salesReturn:list", name = "查询销售退货单列表")
    @Operation(summary = "分页查询销售退货单")
    @GetMapping("/get-return-page")
    public ApiResponse<PageResult<SalesReturn>> getReturnPage(
            @RequestHeader("X-Tenant-Id") Long tenantId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long customerId,
            @RequestParam(required = false) Integer returnStatus) {
        Page<SalesReturn> result = salesReturnService.getReturnPage(tenantId, page, size, customerId, returnStatus);
        PageResult<SalesReturn> pageResult = PageResult.of(result.getRecords(), result.getTotal(), result.getCurrent(), result.getSize());
        return ApiResponse.success(pageResult);
    }

    @RequiresPermission(code = "erp:salesReturn:detail", name = "查询销售退货单详情")
    @Operation(summary = "获取退货单详情")
    @GetMapping("/get-return/{id}")
    public ApiResponse<SalesReturn> getReturnDetail(@PathVariable Long id) {
        SalesReturn salesReturn = salesReturnService.getReturnDetail(id);
        return ApiResponse.success(salesReturn);
    }

    @RequiresPermission(code = "erp:salesReturn:create", name = "创建销售退货单")
    @Operation(summary = "创建销售退货单")
    @PostMapping("/create-return")
    public ApiResponse<SalesReturn> createReturn(
            @RequestBody SalesReturn salesReturn,
            @RequestHeader("X-Tenant-Id") Long tenantId,
            @RequestHeader("X-User-Id") Long userId) {
        SalesReturn created = salesReturnService.createReturn(salesReturn, tenantId, userId);
        return ApiResponse.success(created);
    }

    @RequiresPermission(code = "erp:salesReturn:submit", name = "提交销售退货审核")
    @Operation(summary = "提交审核")
    @PostMapping("/submit-for-approval/{id}")
    public ApiResponse<Void> submitForApproval(@PathVariable Long id) {
        salesReturnService.submitForApproval(id);
        return ApiResponse.success();
    }

    @RequiresPermission(code = "erp:salesReturn:approve", name = "审核销售退货单")
    @Operation(summary = "审核退货单")
    @PostMapping("/approve-return/{id}")
    public ApiResponse<Void> approveReturn(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long userId) {
        salesReturnService.approveReturn(id, userId);
        return ApiResponse.success();
    }

    @RequiresPermission(code = "erp:salesReturn:cancel", name = "取消销售退货单")
    @Operation(summary = "取消退货单")
    @PostMapping("/cancel-return/{id}")
    public ApiResponse<Void> cancelReturn(@PathVariable Long id) {
        salesReturnService.cancelReturn(id);
        return ApiResponse.success();
    }
}
