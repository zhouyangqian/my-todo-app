package com.example.inventory.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.common.core.result.ApiResponse;
import com.example.common.core.result.PageResult;
import com.example.inventory.dto.InventoryCheckCreateRequest;
import com.example.inventory.dto.InventoryCheckSubmitRequest;
import com.example.inventory.entity.InventoryCheck;
import com.example.inventory.service.InventoryCheckService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 库存盘点控制器
 */
@Tag(name = "库存盘点", description = "库存盘点管理API")
@RestController
@RequestMapping("/api/inventory/checks")
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
            @Valid @RequestBody InventoryCheckCreateRequest request,
            @RequestHeader("X-Tenant-Id") Long tenantId,
            @RequestHeader("X-User-Id") Long userId) {
        InventoryCheck check = new InventoryCheck();
        check.setWarehouseId(request.getWarehouseId());
        check.setCheckType(request.getCheckType());
        check.setCheckDate(request.getCheckDate());
        check.setRemark(request.getRemark());
        InventoryCheck created = inventoryCheckService.createCheck(check, request, tenantId, userId);
        return ApiResponse.success(created);
    }

    @Operation(summary = "提交盘点结果")
    @PostMapping("/submit-check/{id}")
    public ApiResponse<Void> submitCheckResult(
            @PathVariable Long id,
            @Valid @RequestBody InventoryCheckSubmitRequest request,
            @RequestHeader("X-User-Id") Long userId) {
        inventoryCheckService.submitCheckResult(id, request.getItems(), userId);
        return ApiResponse.success();
    }

    @Operation(summary = "取消盘点单")
    @PostMapping("/cancel-check/{id}")
    public ApiResponse<Void> cancelCheck(@PathVariable Long id) {
        inventoryCheckService.cancelCheck(id);
        return ApiResponse.success();
    }
}
