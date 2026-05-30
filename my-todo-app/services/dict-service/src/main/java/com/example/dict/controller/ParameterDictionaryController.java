package com.example.dict.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.common.core.annotation.RequiresPermission;
import com.example.common.core.result.ApiResponse;
import com.example.common.core.result.PageResult;
import com.example.dict.entity.ParameterDictionary;
import com.example.dict.service.ParameterDictionaryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 参数字典管理控制器
 */
@Tag(name = "参数字典管理", description = "参数字典增删改查API")
@RestController
@RequestMapping("/api/parameter-dictionaries")
@RequiredArgsConstructor
public class ParameterDictionaryController {

    private final ParameterDictionaryService parameterDictionaryService;

    @RequiresPermission(code = "dict:parameter:dictionary:create", name = "创建参数字典")
    @Operation(summary = "创建参数字典")
    @PostMapping("/create-dictionary")
    public ApiResponse<ParameterDictionary> createDictionary(
            @RequestBody ParameterDictionary dictionary,
            @RequestHeader("X-User-Id") Long userId) {
        dictionary.setCreatedBy(userId);
        ParameterDictionary created = parameterDictionaryService.createDictionary(dictionary);
        return ApiResponse.success(created);
    }

    @RequiresPermission(code = "dict:parameter:dictionary:update", name = "更新参数字典")
    @Operation(summary = "更新参数字典")
    @PutMapping("/update-dictionary/{id}")
    public ApiResponse<ParameterDictionary> updateDictionary(
            @PathVariable Long id,
            @RequestBody ParameterDictionary dictionary,
            @RequestHeader("X-User-Id") Long userId) {
        dictionary.setId(id);
        dictionary.setUpdatedBy(userId);
        ParameterDictionary updated = parameterDictionaryService.updateDictionary(dictionary);
        return ApiResponse.success(updated);
    }

    @RequiresPermission(code = "dict:parameter:dictionary:delete", name = "删除参数字典")
    @Operation(summary = "删除参数字典")
    @DeleteMapping("/delete-dictionary/{id}")
    public ApiResponse<Void> deleteDictionary(@PathVariable Long id) {
        parameterDictionaryService.deleteDictionary(id);
        return ApiResponse.success();
    }

    @RequiresPermission(code = "dict:parameter:dictionary:list", name = "查询参数字典列表")
    @Operation(summary = "分页查询参数字典")
    @GetMapping("/get-dictionary-page")
    public ApiResponse<PageResult<ParameterDictionary>> getDictionaryPage(
            @RequestHeader("X-Tenant-Id") Long tenantId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String paramName) {
        Page<ParameterDictionary> result = parameterDictionaryService.getDictionaryPage(tenantId, page, size, categoryId, paramName);
        PageResult<ParameterDictionary> pageResult = PageResult.of(result.getRecords(), result.getTotal(), result.getCurrent(), result.getSize());
        return ApiResponse.success(pageResult);
    }

    @RequiresPermission(code = "dict:parameter:dictionary:detail", name = "查询参数字典详情")
    @Operation(summary = "按编码查询参数")
    @GetMapping("/get-by-code")
    public ApiResponse<ParameterDictionary> getDictionaryByCode(
            @RequestParam String code,
            @RequestHeader("X-Tenant-Id") Long tenantId) {
        ParameterDictionary dictionary = parameterDictionaryService.getDictionaryByCode(tenantId, code);
        return ApiResponse.success(dictionary);
    }
}
