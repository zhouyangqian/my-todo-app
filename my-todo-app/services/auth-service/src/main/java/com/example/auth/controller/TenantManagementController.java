package com.example.auth.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.auth.entity.Tenant;
import com.example.auth.entity.TenantQuota;
import com.example.auth.entity.TenantResourceUsage;
import com.example.auth.service.TenantManagementService;
import com.example.common.core.result.ApiResponse;
import com.example.common.core.result.PageResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 租户管理控制器
 * <p>
 * 提供租户列表、配额管理、资源使用查询等接口（管理员）。
 * </p>
 */
@Tag(name = "租户管理（管理员）", description = "租户配额与资源管理接口")
@RestController
@RequestMapping("/api/auth/tenant-management")
@RequiredArgsConstructor
public class TenantManagementController {

    private final TenantManagementService tenantManagementService;

    /**
     * 租户列表（管理员）
     */
    @Operation(summary = "租户列表")
    @GetMapping("/list")
    public ApiResponse<PageResult<Tenant>> getTenantList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String tenantName,
            @RequestParam(required = false) Integer status) {
        Page<Tenant> tenantPage = tenantManagementService.getTenantList(page, size, tenantName, status);
        PageResult<Tenant> pageResult = PageResult.of(
                tenantPage.getRecords(), tenantPage.getTotal(),
                tenantPage.getCurrent(), tenantPage.getSize());
        return ApiResponse.success(pageResult);
    }

    /**
     * 查询租户配额
     */
    @Operation(summary = "查询租户配额")
    @GetMapping("/quota/{tenantId}")
    public ApiResponse<TenantQuota> getTenantQuota(@PathVariable Long tenantId) {
        TenantQuota quota = tenantManagementService.getTenantQuota(tenantId);
        return ApiResponse.success(quota);
    }

    /**
     * 更新租户配额
     */
    @Operation(summary = "更新租户配额")
    @PutMapping("/quota/{tenantId}")
    public ApiResponse<TenantQuota> updateTenantQuota(
            @PathVariable Long tenantId,
            @RequestBody TenantQuota quota) {
        TenantQuota updated = tenantManagementService.updateTenantQuota(tenantId, quota);
        return ApiResponse.success(updated);
    }

    /**
     * 查询租户资源使用量
     */
    @Operation(summary = "查询租户资源使用量")
    @GetMapping("/usage/{tenantId}")
    public ApiResponse<TenantResourceUsage> getTenantUsage(@PathVariable Long tenantId) {
        TenantResourceUsage usage = tenantManagementService.getTenantResourceUsage(tenantId);
        return ApiResponse.success(usage);
    }
}
