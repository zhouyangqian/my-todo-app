package com.example.permission.controller;

import com.example.common.core.annotation.RequiresPermission;
import com.example.common.core.result.ApiResponse;
import com.example.permission.api.dto.UserInfoDTO;
import com.example.permission.api.dto.UserPermissionDTO;
import com.example.permission.api.vo.PermissionCheckVO;
import com.example.permission.entity.Permission;
import com.example.permission.service.PermissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

/**
 * 权限管理控制器
 * <p>
 * 提供权限相关的RESTful API接口，包括：
 * - 查询用户权限和角色
 * - 校验用户权限
 * - 获取权限树
 * - 清除用户权限缓存
 * - 权限的增删改操作
 * </p>
 * <p>
 * 所有接口通过请求头 X-Tenant-Id 实现多租户隔离
 * </p>
 */
@Tag(name = "Permission", description = "Permission Management API")
@RestController
@RequestMapping("/api/permissions")
@RequiredArgsConstructor
public class PermissionController {

    /** 权限服务，处理权限相关的业务逻辑 */
    private final PermissionService permissionService;

    /**
     * 获取当前登录用户的权限信息
     * <p>
     * 从请求头的 X-User-Id、X-Username 等中获取当前用户信息，查询该用户拥有的所有权限编码和角色编码。
     * 这个端点供前端在登录成功后获取当前用户的权限和角色信息。
     * </p>
     *
     * @param userId   用户ID（从网关注入的请求头 X-User-Id 获取）
     * @param username 用户名（从网关注入的请求头 X-Username 获取）
     * @param tenantId 租户ID（从网关注入的请求头 X-Tenant-Id 获取）
     * @return 包含用户信息、权限编码集合和角色编码集合的响应对象
     */
    @Operation(summary = "Get current user permissions")
    @GetMapping("/get-current-user-permissions")
    public ApiResponse<UserPermissionDTO> getCurrentUserPermissions(
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader("X-Username") String username,
            @RequestHeader(value = "X-Email", required = false) String email,
            @RequestHeader(value = "X-RealName", required = false) String realName,
            @RequestHeader(value = "X-Phone", required = false) String phone,
            @RequestHeader(value = "X-Avatar", required = false) String avatar,
            @RequestHeader("X-Tenant-Id") Long tenantId) {
        // 查询用户拥有的所有权限编码
        Set<String> permissions = permissionService.getUserPermissions(userId, tenantId);
        // 查询用户拥有的所有角色编码
        Set<String> roles = permissionService.getUserRoles(userId, tenantId);

        // 构建用户信息
        UserInfoDTO userInfo = new UserInfoDTO();
        userInfo.setUserId(userId);
        userInfo.setUsername(username);
        userInfo.setEmail(email);
        userInfo.setRealName(realName);
        userInfo.setPhone(phone);
        userInfo.setAvatar(avatar);
        userInfo.setTenantId(tenantId);

        // 组装响应对象
        UserPermissionDTO response = new UserPermissionDTO();
        response.setUserInfo(userInfo);
        response.setUserId(userId);  // 保留兼容性
        response.setPermissions(permissions);
        response.setRoles(roles);

        return ApiResponse.success(response);
    }

    /**
     * 获取指定用户的权限信息
     * <p>
     * 根据用户ID和租户ID查询该用户拥有的所有权限编码和角色编码，
     * 结果包含权限编码集合和角色编码集合
     * </p>
     *
     * @param userId   用户ID（路径参数）
     * @param tenantId 租户ID（请求头）
     * @return 包含用户权限编码集合和角色编码集合的响应对象
     */
    @RequiresPermission(code = "system:permission:list", name = "查询权限列表")
    @Operation(summary = "Get user permissions by id")
    @GetMapping("/get-user-permissions/{userId}")
    public ApiResponse<UserPermissionDTO> getUserPermissions(
            @PathVariable Long userId,
            @RequestHeader("X-Tenant-Id") Long tenantId) {
        // 查询用户拥有的所有权限编码
        Set<String> permissions = permissionService.getUserPermissions(userId, tenantId);
        // 查询用户拥有的所有角色编码
        Set<String> roles = permissionService.getUserRoles(userId, tenantId);

        // 组装响应对象
        UserPermissionDTO response = new UserPermissionDTO();
        response.setUserId(userId);
        response.setPermissions(permissions);
        response.setRoles(roles);

        return ApiResponse.success(response);
    }

    /**
     * 校验用户是否拥有指定权限
     * <p>
     * 检查指定用户是否拥有请求中的权限编码对应的权限，
     * 如果用户拥有通配符权限（"*"），则视为拥有所有权限
     * </p>
     *
     * @param request  权限校验请求体，包含用户ID和权限编码
     * @param tenantId 租户ID（请求头）
     * @return 是否拥有该权限（true/false）
     */
    @Operation(summary = "Check permission")
    @PostMapping("/check-permission")
    public ApiResponse<Boolean> checkPermission(
            @RequestBody PermissionCheckVO request,
            @RequestHeader("X-Tenant-Id") Long tenantId) {
        boolean hasPermission = permissionService.hasPermission(
                request.getUserId(), tenantId, request.getPermissionCode());
        return ApiResponse.success(hasPermission);
    }

    /**
     * 获取权限树
     * <p>
     * 根据租户ID查询该租户下的所有权限，并构建成树形结构返回。
     * 树形结构通过 parentId 字段关联父子权限节点。
     * </p>
     *
     * @param tenantId 租户ID（请求头）
     * @return 权限树列表（顶级权限节点列表）
     */
    @RequiresPermission(code = "system:permission:list", name = "查询权限列表")
    @Operation(summary = "Get permission tree")
    @GetMapping("/get-permission-tree")
    public ApiResponse<List<Permission>> getPermissionTree(
            @RequestHeader("X-Tenant-Id") Long tenantId) {
        List<Permission> tree = permissionService.getPermissionTree(tenantId);
        return ApiResponse.success(tree);
    }

    /**
     * 清除用户权限缓存
     * <p>
     * 当用户的权限或角色发生变更时，需要调用此接口清除该用户的Redis缓存，
     * 以确保下次查询时获取到最新的权限数据
     * </p>
     *
     * @param userId   用户ID（路径参数）
     * @param tenantId 租户ID（请求头）
     * @return 空响应
     */
    @Operation(summary = "Clear user permission cache")
    @DeleteMapping("/clear-user-cache/{userId}")
    public ApiResponse<Void> clearUserCache(
            @PathVariable Long userId,
            @RequestHeader("X-Tenant-Id") Long tenantId) {
        permissionService.clearUserPermissionCache(userId, tenantId);
        return ApiResponse.success();
    }

    /**
     * 创建权限
     *
     * @param permission 权限实体对象（请求体）
     * @return 创建成功的权限对象
     */
    @Operation(summary = "Create permission")
    @RequiresPermission(code = "system:permission:create", name = "新增权限")
    @PostMapping("/create-permission")
    public ApiResponse<Permission> createPermission(@RequestBody Permission permission) {
        permissionService.save(permission);
        return ApiResponse.success(permission);
    }

    /**
     * 更新权限
     *
     * @param id         权限ID（路径参数）
     * @param permission 权限实体对象（请求体）
     * @return 更新后的权限对象
     */
    @Operation(summary = "Update permission")
    @RequiresPermission(code = "system:permission:update", name = "更新权限")
    @PutMapping("/update-permission/{id}")
    public ApiResponse<Permission> updatePermission(
            @PathVariable Long id,
            @RequestBody Permission permission) {
        permission.setId(id);
        permissionService.updateById(permission);
        return ApiResponse.success(permission);
    }

    /**
     * 删除权限
     * <p>
     * 通过MyBatis-Plus的removeById方法删除权限（实际为逻辑删除）
     * </p>
     *
     * @param id 权限ID（路径参数）
     * @return 空响应
     */
    @Operation(summary = "Delete permission")
    @RequiresPermission(code = "system:permission:delete", name = "删除权限")
    @DeleteMapping("/delete-permission/{id}")
    public ApiResponse<Void> deletePermission(@PathVariable Long id) {
        permissionService.removeById(id);
        return ApiResponse.success();
    }
}
