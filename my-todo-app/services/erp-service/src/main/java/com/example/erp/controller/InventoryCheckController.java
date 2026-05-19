package com.example.erp.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.common.core.result.ApiResponse;
import com.example.common.core.result.PageResult;
import com.example.erp.entity.InventoryCheck;
import com.example.erp.entity.InventoryCheckItem;
import com.example.erp.service.InventoryCheckService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 库存盘点管理控制器
 */
@Tag(name = "库存盘点管理", description = "库存盘点增删改查API")
@RestController
@RequestMapping("/api/erp/inventory-checks")
@RequiredArgsConstructor
public class InventoryCheckController {

    private final InventoryCheckService inventoryCheckService;

    @Operation(summary = "分页查询盘点单")
    @GetMapping("/get-check-page")
    public ApiResponse<PageResult<InventoryCheck>> getCheckPage(
            @RequestHeader("X-Tenant-Id") Long tenantId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long warehouseId,
            @RequestParam(required = false) Integer checkStatus) {
        Page<InventoryCheck> result = inventoryCheckService.getCheckPage(tenantId, page, size, warehouseId, checkStatus);
        PageResult<InventoryCheck> pageResult = PageResult.of(result.getRecords(), result.getTotal(), result.getCurrent(), result.getSize());
        return ApiResponse.success(pageResult);
    }

    @Operation(summary = "获取盘点单详情")
    @GetMapping("/get-check/{id}")
    public ApiResponse<InventoryCheck> getCheckDetail(@PathVariable Long id) {
        InventoryCheck check = inventoryCheckService.getCheckDetail(id);
        return ApiResponse.success(check);
    }

    @Operation(summary = "创建盘点单")
    @PostMapping("/create-check")
    public ApiResponse<InventoryCheck> createCheck(
            @RequestBody InventoryCheck check,
            @RequestHeader("X-Tenant-Id") Long tenantId,
            @RequestHeader("X-User-Id") Long userId) {
        InventoryCheck created = inventoryCheckService.createCheck(check, tenantId, userId);
        return ApiResponse.success(created);
    }

    @Operation(summary = "提交盘点结果")
    @PostMapping("/submit-check/{id}")
    public ApiResponse<Void> submitCheckResult(
            @PathVariable Long id,
            @RequestBody List<InventoryCheckItem> items,
            @RequestHeader("X-User-Id") Long userId) {
        inventoryCheckService.submitCheckResult(id, items, userId);
        return ApiResponse.success();
    }

    @Operation(summary = "取消盘点单")
    @PostMapping("/cancel-check/{id}")
    public ApiResponse<Void> cancelCheck(@PathVariable Long id) {
        inventoryCheckService.cancelCheck(id);
        return ApiResponse.success();
    }
}
