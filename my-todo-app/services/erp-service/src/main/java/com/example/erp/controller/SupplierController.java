package com.example.erp.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.common.core.result.ApiResponse;
import com.example.erp.entity.Supplier;
import com.example.erp.service.SupplierService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 供应商管理控制器
 */
@Tag(name = "供应商管理", description = "供应商增删改查API")
@RestController
@RequestMapping("/api/erp/suppliers")
@RequiredArgsConstructor
public class SupplierController {

    private final SupplierService supplierService;

    @Operation(summary = "分页查询供应商")
    @GetMapping
    public ApiResponse<Page<Supplier>> getSupplierPage(
            @RequestHeader("X-Tenant-Id") Long tenantId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String supplierName,
            @RequestParam(required = false) Integer status) {
        Page<Supplier> result = supplierService.getSupplierPage(tenantId, page, size, supplierName, status);
        return ApiResponse.success(result);
    }

    @Operation(summary = "获取供应商详情")
    @GetMapping("/{id}")
    public ApiResponse<Supplier> getSupplier(@PathVariable Long id) {
        Supplier supplier = supplierService.getById(id);
        return ApiResponse.success(supplier);
    }

    @Operation(summary = "创建供应商")
    @PostMapping
    public ApiResponse<Supplier> createSupplier(
            @RequestBody Supplier supplier,
            @RequestHeader("X-Tenant-Id") Long tenantId,
            @RequestHeader("X-User-Id") Long userId) {
        supplier.setTenantId(tenantId);
        supplier.setCreatedBy(userId);
        Supplier created = supplierService.createSupplier(supplier);
        return ApiResponse.success(created);
    }

    @Operation(summary = "更新供应商")
    @PutMapping("/{id}")
    public ApiResponse<Supplier> updateSupplier(
            @PathVariable Long id,
            @RequestBody Supplier supplier,
            @RequestHeader("X-User-Id") Long userId) {
        supplier.setId(id);
        supplier.setUpdatedBy(userId);
        Supplier updated = supplierService.updateSupplier(supplier);
        return ApiResponse.success(updated);
    }

    @Operation(summary = "删除供应商")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteSupplier(@PathVariable Long id) {
        supplierService.deleteSupplier(id);
        return ApiResponse.success();
    }
}
