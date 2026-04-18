package com.example.erp.controller;

import com.example.common.core.result.ApiResponse;
import com.example.erp.entity.Inventory;
import com.example.erp.service.InventoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * 库存管理控制器
 */
@Tag(name = "库存管理", description = "库存查询和操作API")
@RestController
@RequestMapping("/api/erp/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    @Operation(summary = "查询商品库存")
    @GetMapping("/query")
    public ApiResponse<Inventory> getInventory(
            @RequestParam Long warehouseId,
            @RequestParam Long productId) {
        Inventory inventory = inventoryService.getInventory(warehouseId, productId);
        return ApiResponse.success(inventory);
    }

    @Operation(summary = "获取库存数量")
    @GetMapping("/quantity")
    public ApiResponse<BigDecimal> getStockQuantity(
            @RequestParam Long warehouseId,
            @RequestParam Long productId,
            @RequestHeader("X-Tenant-Id") Long tenantId) {
        BigDecimal quantity = inventoryService.getStockQuantity(warehouseId, productId, tenantId);
        return ApiResponse.success(quantity);
    }

    @Operation(summary = "入库操作")
    @PostMapping("/inbound")
    public ApiResponse<Void> inbound(
            @RequestParam Long warehouseId,
            @RequestParam Long productId,
            @RequestParam BigDecimal quantity,
            @RequestParam(required = false) BigDecimal costPrice,
            @RequestParam(required = false) String batchNo,
            @RequestParam Integer bizType,
            @RequestParam String bizNo,
            @RequestParam(required = false) Long bizId,
            @RequestHeader("X-Tenant-Id") Long tenantId,
            @RequestHeader("X-User-Id") Long userId) {
        inventoryService.inbound(warehouseId, productId, quantity, costPrice, batchNo,
                bizType, bizNo, bizId, tenantId, userId);
        return ApiResponse.success();
    }

    @Operation(summary = "出库操作")
    @PostMapping("/outbound")
    public ApiResponse<Void> outbound(
            @RequestParam Long warehouseId,
            @RequestParam Long productId,
            @RequestParam BigDecimal quantity,
            @RequestParam Integer bizType,
            @RequestParam String bizNo,
            @RequestParam(required = false) Long bizId,
            @RequestHeader("X-Tenant-Id") Long tenantId,
            @RequestHeader("X-User-Id") Long userId) {
        inventoryService.outbound(warehouseId, productId, quantity, bizType, bizNo, bizId, tenantId, userId);
        return ApiResponse.success();
    }

    @Operation(summary = "锁定库存")
    @PostMapping("/lock")
    public ApiResponse<Void> lockStock(
            @RequestParam Long warehouseId,
            @RequestParam Long productId,
            @RequestParam BigDecimal quantity,
            @RequestHeader("X-Tenant-Id") Long tenantId) {
        inventoryService.lockStock(warehouseId, productId, quantity, tenantId);
        return ApiResponse.success();
    }

    @Operation(summary = "解锁库存")
    @PostMapping("/unlock")
    public ApiResponse<Void> unlockStock(
            @RequestParam Long warehouseId,
            @RequestParam Long productId,
            @RequestParam BigDecimal quantity,
            @RequestHeader("X-Tenant-Id") Long tenantId) {
        inventoryService.unlockStock(warehouseId, productId, quantity, tenantId);
        return ApiResponse.success();
    }

    @Operation(summary = "获取库存预警列表")
    @GetMapping("/alert")
    public ApiResponse<List<Inventory>> getAlertInventories(
            @RequestHeader("X-Tenant-Id") Long tenantId,
            @RequestParam(required = false) Long warehouseId) {
        List<Inventory> inventories = inventoryService.getAlertInventories(tenantId, warehouseId);
        return ApiResponse.success(inventories);
    }
}
