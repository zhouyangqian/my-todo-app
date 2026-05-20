package com.example.erp.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.common.core.annotation.RequiresPermission;
import com.example.common.core.result.ApiResponse;
import com.example.common.core.result.PageResult;
import com.example.erp.api.vo.CreateSalesOrderVO;
import com.example.erp.api.dto.SalesOrderDTO;
import com.example.erp.entity.SalesOrder;
import com.example.erp.service.SalesOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 销售订单管理控制器
 */
@Tag(name = "销售订单管理", description = "销售订单增删改查及审核API")
@RestController
@RequestMapping("/api/erp/sales-orders")
@RequiredArgsConstructor
public class SalesOrderController {

    private final SalesOrderService salesOrderService;

    @RequiresPermission(code = "erp:salesOrder:list", name = "查询销售订单列表")
    @Operation(summary = "分页查询销售订单")
    @GetMapping("/get-sales-order-page")
    public ApiResponse<PageResult<SalesOrderDTO>> getOrderPage(
            @RequestHeader("X-Tenant-Id") Long tenantId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String orderNo,
            @RequestParam(required = false) Long customerId,
            @RequestParam(required = false) Integer status) {
        Page<SalesOrderDTO> result = salesOrderService.getOrderPage(tenantId, page, size, orderNo, customerId, status);
        PageResult<SalesOrderDTO> pageResult = PageResult.of(result.getRecords(), result.getTotal(), result.getCurrent(), result.getSize());
        return ApiResponse.success(pageResult);
    }

    @RequiresPermission(code = "erp:salesOrder:detail", name = "查询销售订单详情")
    @Operation(summary = "获取销售订单详情")
    @GetMapping("/get-sales-order/{id}")
    public ApiResponse<SalesOrderDTO> getOrderDetail(@PathVariable Long id) {
        SalesOrderDTO order = salesOrderService.getOrderDetail(id);
        return ApiResponse.success(order);
    }

    @RequiresPermission(code = "erp:salesOrder:create", name = "新建销售订单")
    @Operation(summary = "创建销售订单")
    @PostMapping("/create-sales-order")
    public ApiResponse<SalesOrder> createOrder(
            @Valid @RequestBody CreateSalesOrderVO request,
            @RequestHeader("X-Tenant-Id") Long tenantId,
            @RequestHeader("X-User-Id") Long userId) {
        SalesOrder order = salesOrderService.createOrder(request, userId);
        return ApiResponse.success(order);
    }

    @RequiresPermission(code = "erp:salesOrder:update", name = "更新销售订单")
    @Operation(summary = "更新销售订单")
    @PutMapping("/update-sales-order/{id}")
    public ApiResponse<SalesOrder> updateOrder(
            @PathVariable Long id,
            @Valid @RequestBody CreateSalesOrderVO request,
            @RequestHeader("X-User-Id") Long userId) {
        SalesOrder order = salesOrderService.updateOrder(id, request, userId);
        return ApiResponse.success(order);
    }

    @RequiresPermission(code = "erp:salesOrder:submit", name = "提交销售订单审核")
    @Operation(summary = "提交销售订单审核")
    @PostMapping("/submit-for-approval/{id}")
    public ApiResponse<Void> submitOrder(@PathVariable Long id) {
        salesOrderService.submitForApproval(id);
        return ApiResponse.success();
    }

    @RequiresPermission(code = "erp:salesOrder:approve", name = "审核销售订单")
    @Operation(summary = "审核销售订单")
    @PostMapping("/approve-order/{id}")
    public ApiResponse<Void> approveOrder(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long userId) {
        salesOrderService.approveOrder(id, userId);
        return ApiResponse.success();
    }

    @RequiresPermission(code = "erp:salesOrder:cancel", name = "取消销售订单")
    @Operation(summary = "取消销售订单")
    @PostMapping("/cancel-order/{id}")
    public ApiResponse<Void> cancelOrder(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long userId) {
        salesOrderService.cancelOrder(id, userId);
        return ApiResponse.success();
    }
}
