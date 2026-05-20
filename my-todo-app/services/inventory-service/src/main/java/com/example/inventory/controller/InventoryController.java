package com.example.inventory.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.common.core.annotation.RequiresPermission;
import com.example.common.core.result.ApiResponse;
import com.example.common.core.result.PageResult;
import com.example.inventory.api.dto.InventoryDTO;
import com.example.inventory.api.vo.*;
import com.example.inventory.entity.Inventory;
import com.example.inventory.entity.InventoryFlow;
import com.example.inventory.service.InventoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * 库存管理控制器
 */
@Tag(name = "库存管理", description = "库存查询和操作API")
@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    @RequiresPermission(code = "inventory:inventory:list", name = "查询库存列表")
    @Operation(summary = "分页查询库存")
    @GetMapping("/get-inventory-page")
    public ApiResponse<PageResult<InventoryDTO>> getInventoryPage(
            @RequestHeader("X-Tenant-Id") Long tenantId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long warehouseId,
            @RequestParam(required = false) String productName) {
        Page<InventoryDTO> result = inventoryService.getInventoryPage(tenantId, page, size, warehouseId, productName);
        PageResult<InventoryDTO> pageResult = PageResult.of(result.getRecords(), result.getTotal(), result.getCurrent(), result.getSize());
        return ApiResponse.success(pageResult);
    }

    @RequiresPermission(code = "inventory:inventory:detail", name = "查询库存详情")
    @Operation(summary = "查询商品库存")
    @GetMapping("/get-inventory")
    public ApiResponse<Inventory> getInventory(
            @RequestParam Long warehouseId,
            @RequestParam Long productId) {
        Inventory inventory = inventoryService.getInventory(warehouseId, productId);
        return ApiResponse.success(inventory);
    }

    @RequiresPermission(code = "inventory:inventory:detail", name = "查询库存详情")
    @Operation(summary = "获取库存数量")
    @GetMapping("/get-stock-quantity")
    public ApiResponse<BigDecimal> getStockQuantity(
            @RequestParam Long warehouseId,
            @RequestParam Long productId,
            @RequestHeader("X-Tenant-Id") Long tenantId) {
        BigDecimal quantity = inventoryService.getStockQuantity(warehouseId, productId, tenantId);
        return ApiResponse.success(quantity);
    }

    @RequiresPermission(code = "erp:inventory:inbound", name = "入库操作")
    @Operation(summary = "入库操作")
    @PostMapping("/inbound")
    public ApiResponse<Void> inbound(
            @Valid @RequestBody InboundVO request,
            @RequestHeader("X-Tenant-Id") Long tenantId,
            @RequestHeader("X-User-Id") Long userId) {
        inventoryService.inbound(request.getWarehouseId(), request.getProductId(), request.getQuantity(),
                request.getCostPrice(), request.getBatchNo(), request.getBizType(),
                request.getBizNo(), request.getBizId(), tenantId, userId);
        return ApiResponse.success();
    }

    @RequiresPermission(code = "erp:inventory:outbound", name = "出库操作")
    @Operation(summary = "出库操作")
    @PostMapping("/outbound")
    public ApiResponse<Void> outbound(
            @Valid @RequestBody OutboundVO request,
            @RequestHeader("X-Tenant-Id") Long tenantId,
            @RequestHeader("X-User-Id") Long userId) {
        inventoryService.outbound(request.getWarehouseId(), request.getProductId(), request.getQuantity(),
                request.getBizType(), request.getBizNo(), request.getBizId(), tenantId, userId);
        return ApiResponse.success();
    }

    @RequiresPermission(code = "erp:inventory:inbound", name = "批量入库操作")
    @Operation(summary = "批量入库操作")
    @PostMapping("/batch-inbound")
    public ApiResponse<Void> batchInbound(
            @Valid @RequestBody BatchInboundVO request,
            @RequestHeader("X-Tenant-Id") Long tenantId,
            @RequestHeader("X-User-Id") Long userId) {
        for (InboundVO item : request.getItems()) {
            inventoryService.inbound(item.getWarehouseId(), item.getProductId(), item.getQuantity(),
                    item.getCostPrice(), item.getBatchNo(), item.getBizType(),
                    item.getBizNo(), item.getBizId(), tenantId, userId);
        }
        return ApiResponse.success();
    }

    @RequiresPermission(code = "erp:inventory:outbound", name = "批量出库操作")
    @Operation(summary = "批量出库操作")
    @PostMapping("/batch-outbound")
    public ApiResponse<Void> batchOutbound(
            @Valid @RequestBody BatchOutboundVO request,
            @RequestHeader("X-Tenant-Id") Long tenantId,
            @RequestHeader("X-User-Id") Long userId) {
        for (OutboundVO item : request.getItems()) {
            inventoryService.outbound(item.getWarehouseId(), item.getProductId(), item.getQuantity(),
                    item.getBizType(), item.getBizNo(), item.getBizId(), tenantId, userId);
        }
        return ApiResponse.success();
    }

    @RequiresPermission(code = "erp:inventory:inbound", name = "锁定库存")
    @Operation(summary = "锁定库存")
    @PostMapping("/lock-stock")
    public ApiResponse<Void> lockStock(
            @Valid @RequestBody LockStockVO request,
            @RequestHeader("X-Tenant-Id") Long tenantId) {
        inventoryService.lockStock(request.getWarehouseId(), request.getProductId(), request.getQuantity(), tenantId);
        return ApiResponse.success();
    }

    @RequiresPermission(code = "erp:inventory:outbound", name = "解锁库存")
    @Operation(summary = "解锁库存")
    @PostMapping("/unlock-stock")
    public ApiResponse<Void> unlockStock(
            @Valid @RequestBody UnlockStockVO request,
            @RequestHeader("X-Tenant-Id") Long tenantId) {
        inventoryService.unlockStock(request.getWarehouseId(), request.getProductId(), request.getQuantity(), tenantId);
        return ApiResponse.success();
    }

    @RequiresPermission(code = "inventory:flow:list", name = "查询库存流水列表")
    @Operation(summary = "分页查询库存流水")
    @GetMapping("/get-flow-page")
    public ApiResponse<PageResult<InventoryFlow>> getFlowPage(
            @RequestHeader("X-Tenant-Id") Long tenantId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long warehouseId,
            @RequestParam(required = false) Long productId,
            @RequestParam(required = false) Integer bizType) {
        Page<InventoryFlow> result = inventoryService.getFlowPage(tenantId, page, size, warehouseId, productId, bizType);
        PageResult<InventoryFlow> pageResult = PageResult.of(result.getRecords(), result.getTotal(), result.getCurrent(), result.getSize());
        return ApiResponse.success(pageResult);
    }

    @RequiresPermission(code = "erp:inventory:transfer", name = "库存调拨")
    @Operation(summary = "库存调拨")
    @PostMapping("/transfer")
    public ApiResponse<Void> transfer(
            @Valid @RequestBody StockTransferVO request,
            @RequestHeader("X-Tenant-Id") Long tenantId,
            @RequestHeader("X-User-Id") Long userId) {
        inventoryService.transfer(request.getFromWarehouseId(), request.getToWarehouseId(),
                request.getProductId(), request.getQuantity(),
                request.getTransferNo(), tenantId, userId);
        return ApiResponse.success();
    }

    @RequiresPermission(code = "inventory:inventory:list", name = "查询库存列表")
    @Operation(summary = "获取库存预警列表")
    @GetMapping("/get-alert-inventories")
    public ApiResponse<List<Inventory>> getAlertInventories(
            @RequestHeader("X-Tenant-Id") Long tenantId,
            @RequestParam(required = false) Long warehouseId) {
        List<Inventory> inventories = inventoryService.getAlertInventories(tenantId, warehouseId);
        return ApiResponse.success(inventories);
    }
}
