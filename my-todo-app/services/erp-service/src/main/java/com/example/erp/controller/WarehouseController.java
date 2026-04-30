package com.example.erp.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.common.core.result.ApiResponse;
import com.example.common.core.result.PageResult;
import com.example.erp.entity.Warehouse;
import com.example.erp.service.WarehouseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 仓库管理控制器
 */
@Tag(name = "仓库管理", description = "仓库增删改查API")
@RestController
@RequestMapping("/api/erp/warehouses")
@RequiredArgsConstructor
public class WarehouseController {

    private final WarehouseService warehouseService;

    @Operation(summary = "分页查询仓库")
    @GetMapping("/get-warehouse-page")
    public ApiResponse<PageResult<Warehouse>> getWarehousePage(
            @RequestHeader("X-Tenant-Id") Long tenantId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String warehouseName,
            @RequestParam(required = false) Integer status) {
        Page<Warehouse> result = warehouseService.getWarehousePage(tenantId, page, size, warehouseName, status);
        PageResult<Warehouse> pageResult = PageResult.of(result.getRecords(), result.getTotal(), result.getCurrent(), result.getSize());
        return ApiResponse.success(pageResult);
    }

    @Operation(summary = "获取所有仓库(下拉选择)")
    @GetMapping("/get-all-warehouses")
    public ApiResponse<List<Warehouse>> getAllWarehouses(
            @RequestHeader("X-Tenant-Id") Long tenantId) {
        List<Warehouse> warehouses = warehouseService.getAllWarehouses(tenantId);
        return ApiResponse.success(warehouses);
    }

    @Operation(summary = "获取默认仓库")
    @GetMapping("/get-default-warehouse")
    public ApiResponse<Warehouse> getDefaultWarehouse(
            @RequestHeader("X-Tenant-Id") Long tenantId) {
        Warehouse warehouse = warehouseService.getDefaultWarehouse(tenantId);
        return ApiResponse.success(warehouse);
    }

    @Operation(summary = "获取仓库详情")
    @GetMapping("/get-warehouse/{id}")
    public ApiResponse<Warehouse> getWarehouse(@PathVariable Long id) {
        Warehouse warehouse = warehouseService.getById(id);
        return ApiResponse.success(warehouse);
    }

    @Operation(summary = "创建仓库")
    @PostMapping("/create-warehouse")
    public ApiResponse<Warehouse> createWarehouse(
            @RequestBody Warehouse warehouse,
            @RequestHeader("X-Tenant-Id") Long tenantId,
            @RequestHeader("X-User-Id") Long userId) {
        warehouse.setTenantId(tenantId);
        warehouse.setCreatedBy(userId);
        Warehouse created = warehouseService.createWarehouse(warehouse);
        return ApiResponse.success(created);
    }

    @Operation(summary = "更新仓库")
    @PutMapping("/update-warehouse/{id}")
    public ApiResponse<Warehouse> updateWarehouse(
            @PathVariable Long id,
            @RequestBody Warehouse warehouse,
            @RequestHeader("X-User-Id") Long userId) {
        warehouse.setId(id);
        warehouse.setUpdatedBy(userId);
        Warehouse updated = warehouseService.updateWarehouse(warehouse);
        return ApiResponse.success(updated);
    }

    @Operation(summary = "删除仓库")
    @DeleteMapping("/delete-warehouse/{id}")
    public ApiResponse<Void> deleteWarehouse(@PathVariable Long id) {
        warehouseService.deleteWarehouse(id);
        return ApiResponse.success();
    }
}
