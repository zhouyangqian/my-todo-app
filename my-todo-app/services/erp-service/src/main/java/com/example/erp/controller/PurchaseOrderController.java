package com.example.erp.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.common.core.result.ApiResponse;
import com.example.common.core.result.PageResult;
import com.example.erp.dto.PurchaseInboundRequest;
import com.example.erp.dto.PurchaseOrderCreateRequest;
import com.example.erp.dto.PurchaseOrderVO;
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

    @Operation(summary = "分页查询采购订单")
    @GetMapping("/get-purchase-order-page")
    public ApiResponse<PageResult<PurchaseOrderVO>> getOrderPage(
            @RequestHeader("X-Tenant-Id") Long tenantId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String orderNo,
            @RequestParam(required = false) Long supplierId,
            @RequestParam(required = false) Integer status) {
        Page<PurchaseOrderVO> result = purchaseOrderService.getOrderPage(tenantId, page, size, orderNo, supplierId, status);
        PageResult<PurchaseOrderVO> pageResult = PageResult.of(result.getRecords(), result.getTotal(), result.getCurrent(), result.getSize());
        return ApiResponse.success(pageResult);
    }

    @Operation(summary = "获取采购订单详情")
    @GetMapping("/get-purchase-order/{id}")
    public ApiResponse<PurchaseOrderVO> getOrderDetail(@PathVariable Long id) {
        PurchaseOrderVO order = purchaseOrderService.getOrderDetail(id);
        return ApiResponse.success(order);
    }

    @Operation(summary = "创建采购订单")
    @PostMapping("/create-purchase-order")
    public ApiResponse<PurchaseOrder> createOrder(
            @Valid @RequestBody PurchaseOrderCreateRequest request,
            @RequestHeader("X-Tenant-Id") Long tenantId,
            @RequestHeader("X-User-Id") Long userId) {
        PurchaseOrder order = purchaseOrderService.createOrder(request, tenantId, userId);
        return ApiResponse.success(order);
    }

    @Operation(summary = "更新采购订单")
    @PutMapping("/update-purchase-order/{id}")
    public ApiResponse<PurchaseOrder> updateOrder(
            @PathVariable Long id,
            @Valid @RequestBody PurchaseOrderCreateRequest request,
            @RequestHeader("X-User-Id") Long userId) {
        PurchaseOrder order = purchaseOrderService.updateOrder(id, request, userId);
        return ApiResponse.success(order);
    }

    @Operation(summary = "提交采购订单审核")
    @PostMapping("/submit-for-approval/{id}")
    public ApiResponse<Void> submitOrder(@PathVariable Long id) {
        purchaseOrderService.submitForApproval(id);
        return ApiResponse.success();
    }

    @Operation(summary = "审核采购订单")
    @PostMapping("/approve-order/{id}")
    public ApiResponse<Void> approveOrder(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long userId) {
        purchaseOrderService.approveOrder(id, userId);
        return ApiResponse.success();
    }

    @Operation(summary = "取消采购订单")
    @PostMapping("/cancel-order/{id}")
    public ApiResponse<Void> cancelOrder(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long userId) {
        purchaseOrderService.cancelOrder(id, userId);
        return ApiResponse.success();
    }

    @Operation(summary = "采购入库")
    @PostMapping("/inbound/{id}")
    public ApiResponse<Void> inbound(
            @PathVariable Long id,
            @Valid @RequestBody PurchaseInboundRequest request,
            @RequestHeader("X-Tenant-Id") Long tenantId,
            @RequestHeader("X-User-Id") Long userId) {
        purchaseOrderService.inbound(id, request, tenantId, userId);
        return ApiResponse.success();
    }
}
