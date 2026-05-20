package com.example.erp.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.common.core.annotation.RequiresPermission;
import com.example.common.core.result.ApiResponse;
import com.example.common.core.result.PageResult;
import com.example.erp.entity.ProductCategory;
import com.example.erp.service.ProductCategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 商品分类管理控制器
 */
@Tag(name = "商品分类管理", description = "商品分类增删改查API")
@RestController
@RequestMapping("/api/erp/product-categories")
@RequiredArgsConstructor
public class ProductCategoryController {

    private final ProductCategoryService productCategoryService;

    @RequiresPermission(code = "erp:productCategory:list", name = "查询分类列表")
    @Operation(summary = "分页查询商品分类")
    @GetMapping("/get-category-page")
    public ApiResponse<PageResult<ProductCategory>> getCategoryPage(
            @RequestHeader("X-Tenant-Id") Long tenantId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String categoryName,
            @RequestParam(required = false) Integer status) {
        Page<ProductCategory> result = productCategoryService.getCategoryPage(tenantId, page, size, categoryName, status);
        PageResult<ProductCategory> pageResult = PageResult.of(result.getRecords(), result.getTotal(), result.getCurrent(), result.getSize());
        return ApiResponse.success(pageResult);
    }

    @RequiresPermission(code = "erp:productCategory:list", name = "查询分类列表")
    @Operation(summary = "获取所有分类(下拉选择)")
    @GetMapping("/get-all-categories")
    public ApiResponse<List<ProductCategory>> getAllCategories(
            @RequestHeader("X-Tenant-Id") Long tenantId) {
        List<ProductCategory> categories = productCategoryService.getAllCategories(tenantId);
        return ApiResponse.success(categories);
    }

    @RequiresPermission(code = "erp:productCategory:detail", name = "查询分类详情")
    @Operation(summary = "获取分类详情")
    @GetMapping("/get-category/{id}")
    public ApiResponse<ProductCategory> getCategory(@PathVariable Long id) {
        ProductCategory category = productCategoryService.getById(id);
        return ApiResponse.success(category);
    }

    @RequiresPermission(code = "erp:productCategory:create", name = "新增分类")
    @Operation(summary = "创建分类")
    @PostMapping("/create-category")
    public ApiResponse<ProductCategory> createCategory(
            @RequestBody ProductCategory category,
            @RequestHeader("X-Tenant-Id") Long tenantId,
            @RequestHeader("X-User-Id") Long userId) {
        category.setTenantId(tenantId);
        category.setCreatedBy(userId);
        ProductCategory created = productCategoryService.createCategory(category);
        return ApiResponse.success(created);
    }

    @RequiresPermission(code = "erp:productCategory:update", name = "更新分类")
    @Operation(summary = "更新分类")
    @PutMapping("/update-category/{id}")
    public ApiResponse<ProductCategory> updateCategory(
            @PathVariable Long id,
            @RequestBody ProductCategory category,
            @RequestHeader("X-User-Id") Long userId) {
        category.setId(id);
        category.setUpdatedBy(userId);
        ProductCategory updated = productCategoryService.updateCategory(category);
        return ApiResponse.success(updated);
    }

    @RequiresPermission(code = "erp:productCategory:delete", name = "删除分类")
    @Operation(summary = "删除分类")
    @DeleteMapping("/delete-category/{id}")
    public ApiResponse<Void> deleteCategory(@PathVariable Long id) {
        productCategoryService.deleteCategory(id);
        return ApiResponse.success();
    }
}
