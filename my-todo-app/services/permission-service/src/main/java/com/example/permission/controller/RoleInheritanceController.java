package com.example.permission.controller;

import com.example.common.core.annotation.RequiresPermission;
import com.example.common.core.result.ApiResponse;
import com.example.permission.entity.Permission;
import com.example.permission.entity.Role;
import com.example.permission.service.RoleInheritanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 角色继承管理控制器
 * <p>
 * 提供角色继承关系的RESTful API接口，包括：
 * - 设置/移除继承关系
 * - 查询父角色列表
 * - 获取继承的权限
 * - 获取所有有效权限
 * - 获取继承树
 * </p>
 */
@Tag(name = "RoleInheritance", description = "角色继承管理 API")
@RestController
@RequestMapping("/api/permissions/role-inheritance")
@RequiredArgsConstructor
public class RoleInheritanceController {

    /** 角色继承服务 */
    private final RoleInheritanceService roleInheritanceService;

    /**
     * 设置继承关系
     *
     * @param request  请求体，包含 childRoleId 和 parentRoleId
     * @param tenantId 租户ID（请求头）
     * @return 空响应
     */
    @Operation(summary = "设置角色继承关系")
    @RequiresPermission(code = "system:role:inheritance", name = "设置角色继承")
    @PostMapping("/set-parent")
    public ApiResponse<Void> setParent(
            @RequestBody SetParentRequest request,
            @RequestHeader("X-Tenant-Id") Long tenantId) {
        roleInheritanceService.setParent(request.getChildRoleId(), request.getParentRoleId(), tenantId);
        return ApiResponse.success();
    }

    /**
     * 移除继承关系
     *
     * @param request 请求体，包含 childRoleId 和 parentRoleId
     * @return 空响应
     */
    @Operation(summary = "移除角色继承关系")
    @RequiresPermission(code = "system:role:inheritance", name = "移除角色继承")
    @DeleteMapping("/remove-parent")
    public ApiResponse<Void> removeParent(@RequestBody SetParentRequest request) {
        roleInheritanceService.removeParent(request.getChildRoleId(), request.getParentRoleId());
        return ApiResponse.success();
    }

    /**
     * 获取指定角色的所有父角色
     *
     * @param roleId 角色ID（路径参数）
     * @return 父角色列表
     */
    @Operation(summary = "获取父角色列表")
    @RequiresPermission(code = "system:role:detail", name = "查询角色详情")
    @GetMapping("/parents/{roleId}")
    public ApiResponse<List<Role>> getParentRoles(@PathVariable Long roleId) {
        List<Role> parents = roleInheritanceService.getParentRoles(roleId);
        return ApiResponse.success(parents);
    }

    /**
     * 获取继承的权限（仅继承部分，不含自身权限）
     *
     * @param roleId 角色ID（路径参数）
     * @return 继承的权限列表
     */
    @Operation(summary = "获取继承的权限")
    @RequiresPermission(code = "system:role:detail", name = "查询角色详情")
    @GetMapping("/inherited-permissions/{roleId}")
    public ApiResponse<List<Permission>> getInheritedPermissions(@PathVariable Long roleId) {
        List<Permission> permissions = roleInheritanceService.getInheritedPermissions(roleId);
        return ApiResponse.success(permissions);
    }

    /**
     * 获取所有有效权限（自身权限 + 继承权限）
     *
     * @param roleId 角色ID（路径参数）
     * @return 所有有效权限列表
     */
    @Operation(summary = "获取所有有效权限")
    @RequiresPermission(code = "system:role:detail", name = "查询角色详情")
    @GetMapping("/effective-permissions/{roleId}")
    public ApiResponse<List<Permission>> getEffectivePermissions(@PathVariable Long roleId) {
        List<Permission> permissions = roleInheritanceService.getAllEffectivePermissions(roleId);
        return ApiResponse.success(permissions);
    }

    /**
     * 获取继承树
     *
     * @param roleId 角色ID（路径参数）
     * @return 继承树结构
     */
    @Operation(summary = "获取继承树")
    @RequiresPermission(code = "system:role:detail", name = "查询角色详情")
    @GetMapping("/tree/{roleId}")
    public ApiResponse<List<Map<String, Object>>> getInheritanceTree(@PathVariable Long roleId) {
        List<Map<String, Object>> tree = roleInheritanceService.getInheritanceTree(roleId);
        return ApiResponse.success(tree);
    }

    /**
     * 设置/移除继承关系请求体
     */
    @Data
    public static class SetParentRequest {
        /** 子角色ID */
        private Long childRoleId;
        /** 父角色ID */
        private Long parentRoleId;
    }
}
