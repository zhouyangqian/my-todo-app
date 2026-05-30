package com.example.dict.controller;

import com.example.common.core.annotation.RequiresPermission;
import com.example.common.core.result.ApiResponse;
import com.example.dict.entity.ParameterItem;
import com.example.dict.service.ParameterItemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 参数项管理控制器
 */
@Tag(name = "参数项管理", description = "参数项增删改查API")
@RestController
@RequestMapping("/api/parameter-items")
@RequiredArgsConstructor
public class ParameterItemController {

    private final ParameterItemService parameterItemService;

    @RequiresPermission(code = "dict:parameter:item:create", name = "创建参数项")
    @Operation(summary = "创建参数项")
    @PostMapping("/create-item")
    public ApiResponse<ParameterItem> createItem(
            @RequestBody ParameterItem item,
            @RequestHeader("X-User-Id") Long userId) {
        item.setCreatedBy(userId);
        ParameterItem created = parameterItemService.createItem(item);
        return ApiResponse.success(created);
    }

    @RequiresPermission(code = "dict:parameter:item:update", name = "更新参数项")
    @Operation(summary = "更新参数项")
    @PutMapping("/update-item/{id}")
    public ApiResponse<ParameterItem> updateItem(
            @PathVariable Long id,
            @RequestBody ParameterItem item,
            @RequestHeader("X-User-Id") Long userId) {
        item.setId(id);
        item.setUpdatedBy(userId);
        ParameterItem updated = parameterItemService.updateItem(item);
        return ApiResponse.success(updated);
    }

    @RequiresPermission(code = "dict:parameter:item:delete", name = "删除参数项")
    @Operation(summary = "删除参数项")
    @DeleteMapping("/delete-item/{id}")
    public ApiResponse<Void> deleteItem(@PathVariable Long id) {
        parameterItemService.deleteItem(id);
        return ApiResponse.success();
    }

    @RequiresPermission(code = "dict:parameter:item:list", name = "查询参数项列表")
    @Operation(summary = "查询参数项列表")
    @GetMapping("/get-items/{dictionaryId}")
    public ApiResponse<List<ParameterItem>> getItemsByDictionaryId(@PathVariable Long dictionaryId) {
        List<ParameterItem> items = parameterItemService.getItemsByDictionaryId(dictionaryId);
        return ApiResponse.success(items);
    }

    @RequiresPermission(code = "dict:parameter:item:create", name = "批量保存参数项")
    @Operation(summary = "批量保存参数项")
    @PostMapping("/batch-save")
    public ApiResponse<Void> batchSaveItems(
            @RequestBody BatchSaveRequest request,
            @RequestHeader("X-User-Id") Long userId) {
        // 设置创建人
        for (ParameterItem item : request.getItems()) {
            item.setCreatedBy(userId);
        }
        parameterItemService.batchSaveItems(request.getDictionaryId(), request.getItems());
        return ApiResponse.success();
    }

    /**
     * 批量保存请求体
     */
    @lombok.Data
    public static class BatchSaveRequest {
        private Long dictionaryId;
        private List<ParameterItem> items;
    }
}
