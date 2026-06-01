package com.example.dict.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.common.core.annotation.RequiresPermission;
import com.example.common.core.result.ApiResponse;
import com.example.common.core.result.PageResult;
import com.example.dict.entity.ErrorCategory;
import com.example.dict.entity.ErrorSolution;
import com.example.dict.service.ErrorDocService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 错误文档管理控制器
 */
@Tag(name = "错误文档", description = "错误分类CRUD、解决方案CRUD、错误码搜索API")
@RestController
@RequestMapping("/api/dict/error-doc")
@RequiredArgsConstructor
public class ErrorDocController {

    private final ErrorDocService errorDocService;

    // ==================== 错误分类 ====================

    @RequiresPermission(code = "dict:error-doc:category:list", name = "查询错误分类列表")
    @Operation(summary = "分页查询错误分类")
    @GetMapping("/category/page")
    public ApiResponse<PageResult<ErrorCategory>> getCategoryPage(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String categoryName) {
        Page<ErrorCategory> result = errorDocService.getCategoryPage(page, size, categoryName);
        PageResult<ErrorCategory> pageResult = PageResult.of(
                result.getRecords(), result.getTotal(), result.getCurrent(), result.getSize());
        return ApiResponse.success(pageResult);
    }

    @RequiresPermission(code = "dict:error-doc:category:list", name = "查询所有错误分类")
    @Operation(summary = "查询所有错误分类")
    @GetMapping("/category/list")
    public ApiResponse<List<ErrorCategory>> listAllCategories() {
        return ApiResponse.success(errorDocService.listAllCategories());
    }

    @RequiresPermission(code = "dict:error-doc:category:list", name = "查询错误分类详情")
    @Operation(summary = "获取错误分类详情")
    @GetMapping("/category/{id}")
    public ApiResponse<ErrorCategory> getCategory(@PathVariable Long id) {
        return ApiResponse.success(errorDocService.getById(id));
    }

    @RequiresPermission(code = "dict:error-doc:category:create", name = "创建错误分类")
    @Operation(summary = "创建错误分类")
    @PostMapping("/category")
    public ApiResponse<ErrorCategory> createCategory(@RequestBody ErrorCategory category) {
        return ApiResponse.success(errorDocService.createCategory(category));
    }

    @RequiresPermission(code = "dict:error-doc:category:update", name = "更新错误分类")
    @Operation(summary = "更新错误分类")
    @PutMapping("/category/{id}")
    public ApiResponse<ErrorCategory> updateCategory(
            @PathVariable Long id,
            @RequestBody ErrorCategory category) {
        category.setId(id);
        return ApiResponse.success(errorDocService.updateCategory(category));
    }

    @RequiresPermission(code = "dict:error-doc:category:delete", name = "删除错误分类")
    @Operation(summary = "删除错误分类")
    @DeleteMapping("/category/{id}")
    public ApiResponse<Void> deleteCategory(@PathVariable Long id) {
        errorDocService.deleteCategory(id);
        return ApiResponse.success();
    }

    // ==================== 错误解决方案 ====================

    @RequiresPermission(code = "dict:error-doc:solution:list", name = "查询解决方案列表")
    @Operation(summary = "分页查询解决方案")
    @GetMapping("/solution/page")
    public ApiResponse<PageResult<ErrorSolution>> getSolutionPage(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String errorCode) {
        Page<ErrorSolution> result = errorDocService.getSolutionPage(page, size, categoryId, errorCode);
        PageResult<ErrorSolution> pageResult = PageResult.of(
                result.getRecords(), result.getTotal(), result.getCurrent(), result.getSize());
        return ApiResponse.success(pageResult);
    }

    @RequiresPermission(code = "dict:error-doc:solution:list", name = "查询解决方案详情")
    @Operation(summary = "获取解决方案详情")
    @GetMapping("/solution/{id}")
    public ApiResponse<ErrorSolution> getSolution(@PathVariable Long id) {
        return ApiResponse.success(errorDocService.getSolution(id));
    }

    @RequiresPermission(code = "dict:error-doc:solution:create", name = "创建解决方案")
    @Operation(summary = "创建错误解决方案")
    @PostMapping("/solution")
    public ApiResponse<ErrorSolution> createSolution(@RequestBody ErrorSolution solution) {
        return ApiResponse.success(errorDocService.createSolution(solution));
    }

    @RequiresPermission(code = "dict:error-doc:solution:update", name = "更新解决方案")
    @Operation(summary = "更新错误解决方案")
    @PutMapping("/solution/{id}")
    public ApiResponse<ErrorSolution> updateSolution(
            @PathVariable Long id,
            @RequestBody ErrorSolution solution) {
        solution.setId(id);
        return ApiResponse.success(errorDocService.updateSolution(solution));
    }

    @RequiresPermission(code = "dict:error-doc:solution:delete", name = "删除解决方案")
    @Operation(summary = "删除错误解决方案")
    @DeleteMapping("/solution/{id}")
    public ApiResponse<Void> deleteSolution(@PathVariable Long id) {
        errorDocService.deleteSolution(id);
        return ApiResponse.success();
    }

    // ==================== 搜索 ====================

    @RequiresPermission(code = "dict:error-doc:search", name = "搜索错误文档")
    @Operation(summary = "根据错误码搜索解决方案")
    @GetMapping("/search")
    public ApiResponse<List<ErrorSolution>> searchByErrorCode(@RequestParam String errorCode) {
        return ApiResponse.success(errorDocService.searchByErrorCode(errorCode));
    }
}
