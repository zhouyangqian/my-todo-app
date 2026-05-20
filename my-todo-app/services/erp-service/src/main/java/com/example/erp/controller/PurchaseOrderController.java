package com.example.erp.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.common.core.annotation.RequiresPermission;
import com.example.common.core.result.ApiResponse;
import com.example.common.core.result.PageResult;
import com.example.erp.api.vo.PurchaseInboundVO;
import com.example.erp.api.vo.CreatePurchaseOrderVO;
import com.example.erp.api.dto.PurchaseOrderDTO;
import com.example.erp.entity.PurchaseOrder;
import com.example.erp.service.PurchaseOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 采购订单管理控制器
 */
@Tag(name = "采购订单管理", description = "采购订单增删改查及审核API")
@RestController
@RequestMapping("/api/erp/purchase-orders")
@RequiredArgsConstructor
public class PurchaseOrderController {

    private final PurchaseOrderService purchaseOrderService;

    @RequiresPermission(code = "erp:purchaseOrder:list", name = "查询采购订单列表")
    @Operation(summary = "分页查询采购订单")
    @GetMapping("/get-purchase-order-page")
    public ApiResponse<PageResult<PurchaseOrderDTO>> getOrderPage(
            @RequestHeader("X-Tenant-Id") Long tenantId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String orderNo,
            @RequestParam(required = false) Long supplierId,
            @RequestParam(required = false) Integer status) {
        Page<PurchaseOrderDTO> result = purchaseOrderService.getOrderPage(tenantId, page, size, orderNo, supplierId, status);
        PageResult<PurchaseOrderDTO> pageResult = PageResult.of(result.getRecords(), result.getTotal(), result.getCurrent(), result.getSize());
        return ApiResponse.success(pageResult);
    }

    @RequiresPermission(code = "erp:purchaseOrder:detail", name = "查询采购订单详情")
    @Operation(summary = "获取采购订单详情")
    @GetMapping("/get-purchase-order/{id}")
    public ApiResponse<PurchaseOrderDTO> getOrderDetail(@PathVariable Long id) {
        PurchaseOrderDTO order = purchaseOrderService.getOrderDetail(id);
        return ApiResponse.success(order);
    }

    @RequiresPermission(code = "erp:purchaseOrder:create", name = "新建采购订单")
    @Operation(summary = "创建采购订单")
    @PostMapping("/create-purchase-order")
    public ApiResponse<PurchaseOrder> createOrder(
            @Valid @RequestBody CreatePurchaseOrderVO request,
            @RequestHeader("X-Tenant-Id") Long tenantId,
            @RequestHeader("X-User-Id") Long userId) {
        PurchaseOrder order = purchaseOrderService.createOrder(request, tenantId, userId);
        return ApiResponse.success(order);
    }

    @RequiresPermission(code = "erp:purchaseOrder:update", name = "更新采购订单")
    @Operation(summary = "更新采购订单")
    @PutMapping("/update-purchase-order/{id}")
    public ApiResponse<PurchaseOrder> updateOrder(
            @PathVariable Long id,
            @Valid @RequestBody CreatePurchaseOrderVO request,
            @RequestHeader("X-User-Id") Long userId) {
        PurchaseOrder order = purchaseOrderService.updateOrder(id, request, userId);
        return ApiResponse.success(order);
    }

    @RequiresPermission(code = "erp:purchaseOrder:submit", name = "提交采购订单审核")
    @Operation(summary = "提交采购订单审核")
    @PostMapping("/submit-for-approval/{id}")
    public ApiResponse<Void> submitOrder(@PathVariable Long id) {
        purchaseOrderService.submitForApproval(id);
        return ApiResponse.success();
    }

    @RequiresPermission(code = "erp:purchaseOrder:approve", name = "审核采购订单")
    @Operation(summary = "审核采购订单")
    @PostMapping("/approve-order/{id}")
    public ApiResponse<Void> approveOrder(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long userId) {
        purchaseOrderService.approveOrder(id, userId);
        return ApiResponse.success();
    }

    @RequiresPermission(code = "erp:purchaseOrder:cancel", name = "取消采购订单")
    @Operation(summary = "取消采购订单")
    @PostMapping("/cancel-order/{id}")
    public ApiResponse<Void> cancelOrder(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long userId) {
        purchaseOrderService.cancelOrder(id, userId);
        return ApiResponse.success();
    }

    @RequiresPermission(code = "erp:inventory:inbound", name = "采购入库")
    @Operation(summary = "采购入库")
    @PostMapping("/inbound/{id}")
    public ApiResponse<Void> inbound(
            @PathVariable Long id,
            @Valid @RequestBody PurchaseInboundVO request,
            @RequestHeader("X-Tenant-Id") Long tenantId,
            @RequestHeader("X-User-Id") Long userId) {
        purchaseOrderService.inbound(id, request, tenantId, userId);
        return ApiResponse.success();
    }
}
