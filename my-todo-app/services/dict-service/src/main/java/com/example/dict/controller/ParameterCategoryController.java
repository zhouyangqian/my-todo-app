package com.example.dict.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.common.core.annotation.RequiresPermission;
import com.example.common.core.result.ApiResponse;
import com.example.common.core.result.PageResult;
import com.example.dict.entity.ParameterCategory;
import com.example.dict.service.ParameterCategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 参数分类管理控制器
 */
@Tag(name = "参数分类管理", description = "参数分类增删改查API")
@RestController
@RequestMapping("/api/parameter-categories")
@RequiredArgsConstructor
public class ParameterCategoryController {

    private final ParameterCategoryService parameterCategoryService;

    @RequiresPermission(code = "dict:parameter:category:create", name = "创建参数分类")
    @Operation(summary = "创建参数分类")
    @PostMapping("/create-category")
    public ApiResponse<ParameterCategory> createCategory(
            @RequestBody ParameterCategory category,
            @RequestHeader("X-User-Id") Long userId) {
        category.setCreatedBy(userId);
        ParameterCategory created = parameterCategoryService.createCategory(category);
        return ApiResponse.success(created);
    }

    @RequiresPermission(code = "dict:parameter:category:update", name = "更新参数分类")
    @Operation(summary = "更新参数分类")
    @PutMapping("/update-category/{id}")
    public ApiResponse<ParameterCategory> updateCategory(
            @PathVariable Long id,
            @RequestBody ParameterCategory category,
            @RequestHeader("X-User-Id") Long userId) {
        category.setId(id);
        category.setUpdatedBy(userId);
        ParameterCategory updated = parameterCategoryService.updateCategory(category);
        return ApiResponse.success(updated);
    }

    @RequiresPermission(code = "dict:parameter:category:delete", name = "删除参数分类")
    @Operation(summary = "删除参数分类")
    @DeleteMapping("/delete-category/{id}")
    public ApiResponse<Void> deleteCategory(@PathVariable Long id) {
        parameterCategoryService.deleteCategory(id);
        return ApiResponse.success();
    }

    @RequiresPermission(code = "dict:parameter:category:list", name = "查询参数分类列表")
    @Operation(summary = "分页查询参数分类")
    @GetMapping("/get-category-page")
    public ApiResponse<PageResult<ParameterCategory>> getCategoryPage(
            @RequestHeader("X-Tenant-Id") Long tenantId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String categoryName,
            @RequestParam(required = false) String categoryType) {
        Page<ParameterCategory> result = parameterCategoryService.getCategoryPage(tenantId, page, size, categoryName, categoryType);
        PageResult<ParameterCategory> pageResult = PageResult.of(result.getRecords(), result.getTotal(), result.getCurrent(), result.getSize());
        return ApiResponse.success(pageResult);
    }

    @RequiresPermission(code = "dict:parameter:category:list", name = "查询参数分类列表")
    @Operation(summary = "获取所有启用的参数分类")
    @GetMapping("/get-all-categories")
    public ApiResponse<List<ParameterCategory>> getAllCategories(
            @RequestHeader("X-Tenant-Id") Long tenantId) {
        List<ParameterCategory> categories = parameterCategoryService.getAllCategories(tenantId);
        return ApiResponse.success(categories);
    }
}
