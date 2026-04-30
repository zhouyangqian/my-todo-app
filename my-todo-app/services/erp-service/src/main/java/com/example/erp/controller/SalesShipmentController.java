package com.example.erp.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.common.core.result.ApiResponse;
import com.example.common.core.result.PageResult;
import com.example.erp.dto.SalesShipmentCreateRequest;
import com.example.erp.dto.SalesShipmentVO;
import com.example.erp.dto.SalesOrderVO;
import com.example.erp.entity.SalesShipment;
import com.example.erp.service.SalesShipmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 销售出库单管理控制器
 */
@Tag(name = "销售出库单管理", description = "销售出库单增删改查及审核API")
@RestController
@RequestMapping("/api/erp/sales-shipments")
@RequiredArgsConstructor
public class SalesShipmentController {

    private final SalesShipmentService salesShipmentService;

    @Operation(summary = "分页查询销售出库单")
    @GetMapping("/get-sales-shipment-page")
    public ApiResponse<PageResult<SalesShipmentVO>> getShipmentPage(
            @RequestHeader("X-Tenant-Id") Long tenantId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String shipmentNo,
            @RequestParam(required = false) Long orderId,
            @RequestParam(required = false) Integer status) {
        Page<SalesShipmentVO> result = salesShipmentService.getShipmentPage(tenantId, page, size, shipmentNo, orderId, status);
        PageResult<SalesShipmentVO> pageResult = PageResult.of(result.getRecords(), result.getTotal(), result.getCurrent(), result.getSize());
        return ApiResponse.success(pageResult);
    }

    @Operation(summary = "获取销售出库单详情")
    @GetMapping("/get-sales-shipment/{id}")
    public ApiResponse<SalesShipmentVO> getShipmentDetail(@PathVariable Long id) {
        SalesShipmentVO shipment = salesShipmentService.getShipmentDetail(id);
        return ApiResponse.success(shipment);
    }

    @Operation(summary = "获取订单的可发货商品列表")
    @GetMapping("/get-shippable-items/{orderId}")
    public ApiResponse<List<SalesOrderVO.SalesOrderItemVO>> getShippableItems(@PathVariable Long orderId) {
        List<SalesOrderVO.SalesOrderItemVO> items = salesShipmentService.getShippableItems(orderId);
        return ApiResponse.success(items);
    }

    @Operation(summary = "创建销售出库单")
    @PostMapping("/create-sales-shipment")
    public ApiResponse<SalesShipment> createShipment(
            @Valid @RequestBody SalesShipmentCreateRequest request,
            @RequestHeader("X-Tenant-Id") Long tenantId,
            @RequestHeader("X-User-Id") Long userId) {
        SalesShipment shipment = salesShipmentService.createShipment(request, userId);
        return ApiResponse.success(shipment);
    }

    @Operation(summary = "审核销售出库单")
    @PostMapping("/approve-shipment/{id}")
    public ApiResponse<Void> approveShipment(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long userId) {
        salesShipmentService.approveShipment(id, userId);
        return ApiResponse.success();
    }

    @Operation(summary = "取消销售出库单")
    @PostMapping("/cancel-shipment/{id}")
    public ApiResponse<Void> cancelShipment(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long userId) {
        salesShipmentService.cancelShipment(id, userId);
        return ApiResponse.success();
    }
}
