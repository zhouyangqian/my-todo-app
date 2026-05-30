package com.example.dict.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.common.core.annotation.RequiresPermission;
import com.example.common.core.result.ApiResponse;
import com.example.common.core.result.PageResult;
import com.example.dict.entity.SaasPackage;
import com.example.dict.entity.TenantSubscription;
import com.example.dict.service.SaaSPackageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * SaaS套餐管理控制器
 */
@Tag(name = "SaaS套餐管理", description = "套餐CRUD、功能特性管理、租户订阅管理API")
@RestController
@RequestMapping("/api/dict/package")
@RequiredArgsConstructor
public class SaaSPackageController {

    private final SaaSPackageService saaSPackageService;

    // ==================== 套餐管理 ====================

    @RequiresPermission(code = "dict:package:list", name = "查询套餐列表")
    @Operation(summary = "分页查询套餐")
    @GetMapping("/page")
    public ApiResponse<PageResult<SaasPackage>> getPackagePage(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String packageName) {
        Page<SaasPackage> result = saaSPackageService.getPackagePage(page, size, packageName);
        PageResult<SaasPackage> pageResult = PageResult.of(
                result.getRecords(), result.getTotal(), result.getCurrent(), result.getSize());
        return ApiResponse.success(pageResult);
    }

    @RequiresPermission(code = "dict:package:list", name = "查询套餐列表")
    @Operation(summary = "查询所有有效套餐")
    @GetMapping("/active")
    public ApiResponse<List<SaasPackage>> listActive() {
        return ApiResponse.success(saaSPackageService.listActive());
    }

    @RequiresPermission(code = "dict:package:detail", name = "查询套餐详情")
    @Operation(summary = "获取套餐详情")
    @GetMapping("/{id}")
    public ApiResponse<SaasPackage> getPackage(@PathVariable Long id) {
        return ApiResponse.success(saaSPackageService.getById(id));
    }

    @RequiresPermission(code = "dict:package:create", name = "创建套餐")
    @Operation(summary = "创建套餐")
    @PostMapping
    public ApiResponse<SaasPackage> createPackage(@RequestBody SaasPackage pkg) {
        return ApiResponse.success(saaSPackageService.createPackage(pkg));
    }

    @RequiresPermission(code = "dict:package:update", name = "更新套餐")
    @Operation(summary = "更新套餐")
    @PutMapping("/{id}")
    public ApiResponse<SaasPackage> updatePackage(
            @PathVariable Long id,
            @RequestBody SaasPackage pkg) {
        pkg.setId(id);
        return ApiResponse.success(saaSPackageService.updatePackage(pkg));
    }

    @RequiresPermission(code = "dict:package:delete", name = "删除套餐")
    @Operation(summary = "删除套餐")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deletePackage(@PathVariable Long id) {
        saaSPackageService.deletePackage(id);
        return ApiResponse.success();
    }

    // ==================== 功能特性管理 ====================

    @RequiresPermission(code = "dict:package:update", name = "更新套餐功能特性")
    @Operation(summary = "更新套餐功能特性")
    @PutMapping("/{id}/features")
    public ApiResponse<Void> updateFeatures(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        saaSPackageService.updateFeatures(id, body.get("features"));
        return ApiResponse.success();
    }

    // ==================== 租户订阅管理 ====================

    @RequiresPermission(code = "dict:package:subscribe", name = "订阅套餐")
    @Operation(summary = "订阅套餐")
    @PostMapping("/subscribe")
    public ApiResponse<TenantSubscription> subscribe(
            @RequestHeader("X-Tenant-Id") Long tenantId,
            @RequestBody Map<String, Object> body,
            @RequestHeader("X-User-Id") Long userId) {
        Long packageId = Long.valueOf(body.get("packageId").toString());
        LocalDate startDate = LocalDate.parse(body.get("startDate").toString());
        LocalDate endDate = body.get("endDate") != null
                ? LocalDate.parse(body.get("endDate").toString()) : null;
        return ApiResponse.success(saaSPackageService.subscribe(tenantId, packageId, startDate, endDate));
    }

    @RequiresPermission(code = "dict:package:subscribe", name = "分页查询订阅")
    @Operation(summary = "分页查询租户订阅")
    @GetMapping("/subscriptions/page")
    public ApiResponse<PageResult<TenantSubscription>> getSubscriptionPage(
            @RequestHeader("X-Tenant-Id") Long tenantId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<TenantSubscription> result = saaSPackageService.getSubscriptionPage(tenantId, page, size);
        return ApiResponse.success(PageResult.of(result.getRecords(), result.getTotal(), result.getCurrent(), result.getSize()));
    }

    @RequiresPermission(code = "dict:package:subscribe", name = "查询当前订阅")
    @Operation(summary = "查询租户当前有效订阅")
    @GetMapping("/subscription/active")
    public ApiResponse<TenantSubscription> getActiveSubscription(
            @RequestHeader("X-Tenant-Id") Long tenantId) {
        return ApiResponse.success(saaSPackageService.getActiveSubscription(tenantId));
    }

    @RequiresPermission(code = "dict:package:subscribe", name = "查询订阅历史")
    @Operation(summary = "查询租户订阅历史")
    @GetMapping("/subscription/history")
    public ApiResponse<List<TenantSubscription>> getSubscriptionHistory(
            @RequestHeader("X-Tenant-Id") Long tenantId) {
        return ApiResponse.success(saaSPackageService.getSubscriptionHistory(tenantId));
    }

    @RequiresPermission(code = "dict:package:subscribe", name = "取消订阅")
    @Operation(summary = "取消订阅")
    @DeleteMapping("/subscription/{subscriptionId}")
    public ApiResponse<Void> cancelSubscription(@PathVariable Long subscriptionId) {
        saaSPackageService.cancelSubscription(subscriptionId);
        return ApiResponse.success();
    }
}
