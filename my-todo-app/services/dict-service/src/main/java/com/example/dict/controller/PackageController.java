package com.example.dict.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.common.core.result.ApiResponse;
import com.example.common.core.result.PageResult;
import com.example.dict.entity.SaasPackage;
import com.example.dict.entity.TenantSubscription;
import com.example.dict.service.PackageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

/**
 * 套餐管理控制器
 */
@Tag(name = "套餐管理", description = "SaaS套餐与租户订阅管理")
@RestController
@RequestMapping("/api/packages")
@RequiredArgsConstructor
public class PackageController {

    private final PackageService packageService;

    // ==================== 套餐管理 ====================

    @Operation(summary = "分页查询套餐")
    @GetMapping("/page")
    public ApiResponse<PageResult<SaasPackage>> getPackagePage(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String packageName) {
        Page<SaasPackage> result = packageService.getPackagePage(page, size, packageName);
        return ApiResponse.success(PageResult.of(result.getRecords(), result.getTotal(), result.getCurrent(), result.getSize()));
    }

    @Operation(summary = "获取套餐详情")
    @GetMapping("/{id}")
    public ApiResponse<SaasPackage> getPackage(@PathVariable Long id) {
        return ApiResponse.success(packageService.getById(id));
    }

    @Operation(summary = "创建套餐")
    @PostMapping("/create")
    public ApiResponse<SaasPackage> createPackage(@RequestBody SaasPackage pkg) {
        return ApiResponse.success(packageService.createPackage(pkg));
    }

    @Operation(summary = "更新套餐")
    @PutMapping("/update/{id}")
    public ApiResponse<SaasPackage> updatePackage(
            @PathVariable Long id,
            @RequestBody SaasPackage pkg) {
        pkg.setId(id);
        return ApiResponse.success(packageService.updatePackage(pkg));
    }

    @Operation(summary = "删除套餐")
    @DeleteMapping("/delete/{id}")
    public ApiResponse<Void> deletePackage(@PathVariable Long id) {
        packageService.deletePackage(id);
        return ApiResponse.success();
    }

    // ==================== 租户订阅 ====================

    @Operation(summary = "租户订阅套餐")
    @PostMapping("/subscribe")
    public ApiResponse<TenantSubscription> subscribe(
            @RequestHeader("X-Tenant-Id") Long tenantId,
            @RequestParam Long packageId,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        return ApiResponse.success(packageService.subscribe(tenantId, packageId, startDate, endDate));
    }

    @Operation(summary = "查询租户订阅列表")
    @GetMapping("/subscriptions/page")
    public ApiResponse<PageResult<TenantSubscription>> getTenantSubscriptionPage(
            @RequestHeader("X-Tenant-Id") Long tenantId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<TenantSubscription> result = packageService.getTenantSubscriptionPage(tenantId, page, size);
        return ApiResponse.success(PageResult.of(result.getRecords(), result.getTotal(), result.getCurrent(), result.getSize()));
    }
}
