package com.example.permission.controller;

import com.example.common.core.annotation.RequiresPermission;
import com.example.common.core.result.ApiResponse;
import com.example.permission.api.dto.PermissionTreeDTO;
import com.example.permission.api.vo.PermissionCreateVO;
import com.example.permission.api.vo.PermissionUpdateVO;
import com.example.permission.entity.Permission;
import com.example.permission.service.DynamicPermissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 动态权限管理控制器
 * <p>
 * 提供权限的动态创建、更新、删除、权限树查询和缓存清除等接口。
 * 通过请求头 X-Tenant-Id 实现多租户隔离。
 * </p>
 */
@Tag(name = "动态权限管理")
@RestController
@RequestMapping("/api/permissions/dynamic")
@RequiredArgsConstructor
public class DynamicPermissionController {

    private final DynamicPermissionService dynamicPermissionService;

    /**
     * 创建权限
     *
     * @param vo       创建权限请求参数
     * @param tenantId 租户ID
     * @return 创建成功的权限对象
     */
    @Operation(summary = "创建权限")
    @RequiresPermission(code = "system:permission:create", name = "新增权限")
    @PostMapping("/create")
    public ApiResponse<Permission> createPermission(
            @Valid @RequestBody PermissionCreateVO vo,
            @RequestHeader("X-Tenant-Id") Long tenantId) {
        Permission permission = dynamicPermissionService.createPermission(vo, tenantId);
        return ApiResponse.success(permission);
    }

    /**
     * 更新权限
     *
     * @param id       权限ID
     * @param vo       更新权限请求参数
     * @param tenantId 租户ID
     * @return 更新后的权限对象
     */
    @Operation(summary = "更新权限")
    @RequiresPermission(code = "system:permission:update", name = "更新权限")
    @PutMapping("/update/{id}")
    public ApiResponse<Permission> updatePermission(
            @PathVariable Long id,
            @RequestBody PermissionUpdateVO vo,
            @RequestHeader("X-Tenant-Id") Long tenantId) {
        Permission permission = dynamicPermissionService.updatePermission(id, vo, tenantId);
        return ApiResponse.success(permission);
    }

    /**
     * 删除权限
     *
     * @param id       权限ID
     * @param tenantId 租户ID
     * @return 空响应
     */
    @Operation(summary = "删除权限")
    @RequiresPermission(code = "system:permission:delete", name = "删除权限")
    @DeleteMapping("/delete/{id}")
    public ApiResponse<Void> deletePermission(
            @PathVariable Long id,
            @RequestHeader("X-Tenant-Id") Long tenantId) {
        dynamicPermissionService.deletePermission(id, tenantId);
        return ApiResponse.success();
    }

    /**
     * 获取权限树
     *
     * @param tenantId 租户ID
     * @return 权限树列表
     */
    @Operation(summary = "获取权限树")
    @RequiresPermission(code = "system:permission:list", name = "查询权限列表")
    @GetMapping("/tree")
    public ApiResponse<List<PermissionTreeDTO>> getPermissionTree(
            @RequestHeader("X-Tenant-Id") Long tenantId) {
        List<PermissionTreeDTO> tree = dynamicPermissionService.getPermissionTree(tenantId);
        return ApiResponse.success(tree);
    }

    /**
     * 清除权限缓存
     *
     * @param tenantId 租户ID
     * @return 空响应
     */
    @Operation(summary = "清除权限缓存")
    @RequiresPermission(code = "system:permission:clear-cache", name = "清除权限缓存")
    @PostMapping("/clear-cache")
    public ApiResponse<Void> clearCache(
            @RequestHeader("X-Tenant-Id") Long tenantId) {
        dynamicPermissionService.invalidateCache(tenantId);
        return ApiResponse.success();
    }
}
