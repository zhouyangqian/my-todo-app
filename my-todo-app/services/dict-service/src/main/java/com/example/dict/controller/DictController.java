package com.example.dict.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.common.core.annotation.RequiresPermission;
import com.example.common.core.result.ApiResponse;
import com.example.common.core.result.PageResult;
import com.example.dict.entity.DictItem;
import com.example.dict.entity.DictType;
import com.example.dict.service.DictService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 字典管理控制器
 */
@Tag(name = "字典管理", description = "字典类型和字典项管理API")
@RestController
@RequestMapping("/api/dict")
@RequiredArgsConstructor
public class DictController {

    private final DictService dictService;

    // ==================== 字典类型 ====================

    @RequiresPermission(code = "dict:type:list", name = "查询字典类型列表")
    @Operation(summary = "分页查询字典类型")
    @GetMapping("/types/get-dict-type-page")
    public ApiResponse<PageResult<DictType>> getDictTypePage(
            @RequestHeader("X-Tenant-Id") Long tenantId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String dictName) {
        Page<DictType> result = dictService.getDictTypePage(tenantId, page, size, dictName);
        PageResult<DictType> pageResult = PageResult.of(result.getRecords(), result.getTotal(), result.getCurrent(), result.getSize());
        return ApiResponse.success(pageResult);
    }

    @RequiresPermission(code = "dict:type:detail", name = "查询字典类型详情")
    @Operation(summary = "获取字典类型详情")
    @GetMapping("/types/get-dict-type/{id}")
    public ApiResponse<DictType> getDictType(@PathVariable Long id) {
        DictType dictType = dictService.getById(id);
        return ApiResponse.success(dictType);
    }

    @RequiresPermission(code = "dict:type:create", name = "创建字典类型")
    @Operation(summary = "创建字典类型")
    @PostMapping("/types/create-dict-type")
    public ApiResponse<DictType> createDictType(
            @RequestBody DictType dictType,
            @RequestHeader("X-User-Id") Long userId) {
        dictType.setCreatedBy(userId);
        DictType created = dictService.createDictType(dictType);
        return ApiResponse.success(created);
    }

    @RequiresPermission(code = "dict:type:update", name = "更新字典类型")
    @Operation(summary = "更新字典类型")
    @PutMapping("/types/update-dict-type/{id}")
    public ApiResponse<DictType> updateDictType(
            @PathVariable Long id,
            @RequestBody DictType dictType,
            @RequestHeader("X-User-Id") Long userId) {
        dictType.setId(id);
        dictType.setUpdatedBy(userId);
        DictType updated = dictService.updateDictType(dictType);
        return ApiResponse.success(updated);
    }

    @RequiresPermission(code = "dict:type:delete", name = "删除字典类型")
    @Operation(summary = "删除字典类型")
    @DeleteMapping("/types/delete-dict-type/{id}")
    public ApiResponse<Void> deleteDictType(
            @PathVariable Long id,
            @RequestHeader("X-Tenant-Id") Long tenantId) {
        dictService.deleteDictType(id, tenantId);
        return ApiResponse.success();
    }

    // ==================== 字典项 ====================

    @RequiresPermission(code = "dict:item:list", name = "查询字典项列表")
    @Operation(summary = "根据字典编码获取字典项")
    @GetMapping("/items/code/{dictCode}")
    public ApiResponse<List<DictItem>> getDictItemsByCode(
            @PathVariable String dictCode,
            @RequestHeader("X-Tenant-Id") Long tenantId) {
        List<DictItem> items = dictService.getDictItemsByCode(dictCode, tenantId);
        return ApiResponse.success(items);
    }

    @RequiresPermission(code = "dict:item:list", name = "查询字典项列表")
    @Operation(summary = "根据字典类型ID获取字典项")
    @GetMapping("/items/type/{typeId}")
    public ApiResponse<List<DictItem>> getDictItemsByTypeId(@PathVariable Long typeId) {
        List<DictItem> items = dictService.getDictItemsByTypeId(typeId);
        return ApiResponse.success(items);
    }

    @RequiresPermission(code = "dict:type:create", name = "添加字典项")
    @Operation(summary = "添加字典项")
    @PostMapping("/items/add-dict-item")
    public ApiResponse<DictItem> addDictItem(
            @RequestBody DictItem dictItem,
            @RequestHeader("X-User-Id") Long userId) {
        dictItem.setCreatedBy(userId);
        DictItem created = dictService.addDictItem(dictItem);
        return ApiResponse.success(created);
    }

    @RequiresPermission(code = "dict:type:update", name = "更新字典项")
    @Operation(summary = "更新字典项")
    @PutMapping("/items/update-dict-item/{id}")
    public ApiResponse<DictItem> updateDictItem(
            @PathVariable Long id,
            @RequestBody DictItem dictItem,
            @RequestHeader("X-User-Id") Long userId) {
        dictItem.setId(id);
        dictItem.setUpdatedBy(userId);
        DictItem updated = dictService.updateDictItem(dictItem);
        return ApiResponse.success(updated);
    }

    @RequiresPermission(code = "dict:type:delete", name = "删除字典项")
    @Operation(summary = "删除字典项")
    @DeleteMapping("/items/delete-dict-item/{id}")
    public ApiResponse<Void> deleteDictItem(
            @PathVariable Long id,
            @RequestHeader("X-Tenant-Id") Long tenantId) {
        dictService.deleteDictItem(id, tenantId);
        return ApiResponse.success();
    }

    // ==================== 缓存管理 ====================

    @Operation(summary = "清除字典缓存")
    @DeleteMapping("/cache/{dictCode}")
    public ApiResponse<Void> clearDictCache(
            @PathVariable String dictCode,
            @RequestHeader("X-Tenant-Id") Long tenantId) {
        dictService.clearDictCache(dictCode, tenantId);
        return ApiResponse.success();
    }

    @Operation(summary = "清除所有字典缓存")
    @DeleteMapping("/cache/all")
    public ApiResponse<Void> clearAllDictCache(
            @RequestHeader("X-Tenant-Id") Long tenantId) {
        dictService.clearAllDictCache(tenantId);
        return ApiResponse.success();
    }
}
