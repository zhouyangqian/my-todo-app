package com.example.erp.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.common.core.annotation.RequiresPermission;
import com.example.common.core.result.ApiResponse;
import com.example.common.core.result.PageResult;
import com.example.erp.entity.PurchaseReturn;
import com.example.erp.service.PurchaseReturnService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 采购退货管理控制器
 */
@Tag(name = "采购退货管理", description = "采购退货增删改查API")
@RestController
@RequestMapping("/api/erp/purchase-returns")
@RequiredArgsConstructor
public class PurchaseReturnController {

    private final PurchaseReturnService purchaseReturnService;

    @RequiresPermission(code = "erp:purchaseReturn:list", name = "查询采购退货单列表")
    @Operation(summary = "分页查询采购退货单")
    @GetMapping("/get-return-page")
    public ApiResponse<PageResult<PurchaseReturn>> getReturnPage(
            @RequestHeader("X-Tenant-Id") Long tenantId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long supplierId,
            @RequestParam(required = false) Integer returnStatus) {
        Page<PurchaseReturn> result = purchaseReturnService.getReturnPage(tenantId, page, size, supplierId, returnStatus);
        PageResult<PurchaseReturn> pageResult = PageResult.of(result.getRecords(), result.getTotal(), result.getCurrent(), result.getSize());
        return ApiResponse.success(pageResult);
    }

    @RequiresPermission(code = "erp:purchaseReturn:detail", name = "查询采购退货单详情")
    @Operation(summary = "获取退货单详情")
    @GetMapping("/get-return/{id}")
    public ApiResponse<PurchaseReturn> getReturnDetail(@PathVariable Long id) {
        PurchaseReturn purchaseReturn = purchaseReturnService.getReturnDetail(id);
        return ApiResponse.success(purchaseReturn);
    }

    @RequiresPermission(code = "erp:purchaseReturn:create", name = "创建采购退货单")
    @Operation(summary = "创建采购退货单")
    @PostMapping("/create-return")
    public ApiResponse<PurchaseReturn> createReturn(
            @RequestBody PurchaseReturn purchaseReturn,
            @RequestHeader("X-Tenant-Id") Long tenantId,
            @RequestHeader("X-User-Id") Long userId) {
        PurchaseReturn created = purchaseReturnService.createReturn(purchaseReturn, tenantId, userId);
        return ApiResponse.success(created);
    }

    @RequiresPermission(code = "erp:purchaseReturn:submit", name = "提交采购退货审核")
    @Operation(summary = "提交审核")
    @PostMapping("/submit-for-approval/{id}")
    public ApiResponse<Void> submitForApproval(@PathVariable Long id) {
        purchaseReturnService.submitForApproval(id);
        return ApiResponse.success();
    }

    @RequiresPermission(code = "erp:purchaseReturn:approve", name = "审核采购退货单")
    @Operation(summary = "审核退货单")
    @PostMapping("/approve-return/{id}")
    public ApiResponse<Void> approveReturn(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long userId) {
        purchaseReturnService.approveReturn(id, userId);
        return ApiResponse.success();
    }

    @RequiresPermission(code = "erp:purchaseReturn:cancel", name = "取消采购退货单")
    @Operation(summary = "取消退货单")
    @PostMapping("/cancel-return/{id}")
    public ApiResponse<Void> cancelReturn(@PathVariable Long id) {
        purchaseReturnService.cancelReturn(id);
        return ApiResponse.success();
    }
}
