package com.example.erp.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.common.core.annotation.RequiresPermission;
import com.example.common.core.result.ApiResponse;
import com.example.common.core.result.PageResult;
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

    @RequiresPermission(code = "erp:supplier:list", name = "查询供应商列表")
    @Operation(summary = "分页查询供应商")
    @GetMapping("/get-supplier-page")
    public ApiResponse<PageResult<Supplier>> getSupplierPage(
            @RequestHeader("X-Tenant-Id") Long tenantId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String supplierName,
            @RequestParam(required = false) Integer status) {
        Page<Supplier> result = supplierService.getSupplierPage(tenantId, page, size, supplierName, status);
        PageResult<Supplier> pageResult = PageResult.of(result.getRecords(), result.getTotal(), result.getCurrent(), result.getSize());
        return ApiResponse.success(pageResult);
    }

    @RequiresPermission(code = "erp:supplier:detail", name = "查询供应商详情")
    @Operation(summary = "获取供应商详情")
    @GetMapping("/get-supplier/{id}")
    public ApiResponse<Supplier> getSupplier(@PathVariable Long id) {
        Supplier supplier = supplierService.getById(id);
        return ApiResponse.success(supplier);
    }

    @RequiresPermission(code = "erp:supplier:create", name = "新增供应商")
    @Operation(summary = "创建供应商")
    @PostMapping("/create-supplier")
    public ApiResponse<Supplier> createSupplier(
            @RequestBody Supplier supplier,
            @RequestHeader("X-Tenant-Id") Long tenantId,
            @RequestHeader("X-User-Id") Long userId) {
        supplier.setTenantId(tenantId);
        supplier.setCreatedBy(userId);
        Supplier created = supplierService.createSupplier(supplier);
        return ApiResponse.success(created);
    }

    @RequiresPermission(code = "erp:supplier:update", name = "更新供应商")
    @Operation(summary = "更新供应商")
    @PutMapping("/update-supplier/{id}")
    public ApiResponse<Supplier> updateSupplier(
            @PathVariable Long id,
            @RequestBody Supplier supplier,
            @RequestHeader("X-User-Id") Long userId) {
        supplier.setId(id);
        supplier.setUpdatedBy(userId);
        Supplier updated = supplierService.updateSupplier(supplier);
        return ApiResponse.success(updated);
    }

    @RequiresPermission(code = "erp:supplier:delete", name = "删除供应商")
    @Operation(summary = "删除供应商")
    @DeleteMapping("/delete-supplier/{id}")
    public ApiResponse<Void> deleteSupplier(@PathVariable Long id) {
        supplierService.deleteSupplier(id);
        return ApiResponse.success();
    }
}
