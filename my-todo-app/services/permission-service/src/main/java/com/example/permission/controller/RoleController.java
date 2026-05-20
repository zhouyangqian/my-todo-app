package com.example.permission.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.common.core.annotation.RequiresPermission;
import com.example.common.core.result.ApiResponse;
import com.example.common.core.result.PageResult;
import com.example.permission.api.vo.AssignPermissionsVO;
import com.example.permission.api.vo.AssignRolesVO;
import com.example.permission.entity.Role;
import com.example.permission.service.RoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 角色管理控制器
 * <p>
 * 提供角色相关的RESTful API接口，包括：
 * - 角色的分页查询、详情查询
 * - 角色的创建、更新、删除
 * - 查询用户拥有的角色
 * - 给用户分配角色
 * - 查询角色关联的权限
 * - 给角色分配权限
 * </p>
 * <p>
 * 所有接口通过请求头 X-Tenant-Id 实现多租户隔离，
 * 通过请求头 X-User-Id 获取当前操作用户ID
 * </p>
 */
@Tag(name = "Role", description = "Role Management API")
@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
public class RoleController {

    /** 角色服务，处理角色相关的业务逻辑 */
    private final RoleService roleService;

    /**
     * 分页查询角色列表
     * <p>
     * 根据租户ID查询该租户下的角色列表，支持按角色名称模糊搜索，
     * 结果按排序字段升序排列
     * </p>
     *
     * @param tenantId 租户ID（请求头）
     * @param page     当前页码，默认第1页
     * @param size     每页条数，默认10条
     * @param roleName 角色名称（可选，用于模糊搜索）
     * @return 角色分页数据
     */
    @RequiresPermission(code = "system:role:list", name = "查询角色列表")
    @Operation(summary = "Get role list")
    @GetMapping("/get-role-list")
    public ApiResponse<PageResult<Role>> getRoleList(
            @RequestHeader("X-Tenant-Id") Long tenantId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String roleName) {
        Page<Role> rolePage = roleService.getRolePage(tenantId, page, size, roleName);
        PageResult<Role> pageResult = PageResult.of(rolePage.getRecords(), rolePage.getTotal(), rolePage.getCurrent(), rolePage.getSize());
        return ApiResponse.success(pageResult);
    }

    /**
     * 根据ID查询角色详情
     *
     * @param id 角色ID（路径参数）
     * @return 角色对象
     */
    @RequiresPermission(code = "system:role:detail", name = "查询角色详情")
    @Operation(summary = "Get role by ID")
    @GetMapping("/get-role/{id}")
    public ApiResponse<Role> getRole(@PathVariable Long id) {
        Role role = roleService.getById(id);
        return ApiResponse.success(role);
    }

    /**
     * 创建角色
     * <p>
     * 创建新角色，通过请求头获取当前操作用户ID作为创建人，
     * 会校验角色编码在同一租户下的唯一性
     * </p>
     *
     * @param role   角色实体对象（请求体）
     * @param userId 当前操作用户ID（请求头）
     * @return 创建成功的角色对象
     */
    @Operation(summary = "Create role")
    @RequiresPermission(code = "system:role:create", name = "新增角色")
    @PostMapping("/create-role")
    public ApiResponse<Role> createRole(
            @RequestBody Role role,
            @RequestHeader("X-User-Id") Long userId) {
        role.setCreatedBy(userId);
        Role created = roleService.createRole(role);
        return ApiResponse.success(created);
    }

    /**
     * 更新角色
     * <p>
     * 更新指定ID的角色信息，通过请求头获取当前操作用户ID作为更新人，
     * 会校验角色编码在同一租户下的唯一性（排除自身）
     * </p>
     *
     * @param id     角色ID（路径参数）
     * @param role   角色实体对象（请求体）
     * @param userId 当前操作用户ID（请求头）
     * @return 更新后的角色对象
     */
    @Operation(summary = "Update role")
    @RequiresPermission(code = "system:role:update", name = "更新角色")
    @PutMapping("/update-role/{id}")
    public ApiResponse<Role> updateRole(
            @PathVariable Long id,
            @RequestBody Role role,
            @RequestHeader("X-User-Id") Long userId) {
        role.setId(id);
        role.setUpdatedBy(userId);
        Role updated = roleService.updateRole(role);
        return ApiResponse.success(updated);
    }

    /**
     * 删除角色（逻辑删除）
     * <p>
     * 逻辑删除指定角色，同时清理该角色关联的用户-角色关系和角色-权限关系
     * </p>
     *
     * @param id 角色ID（路径参数）
     * @return 空响应
     */
    @Operation(summary = "Delete role")
    @RequiresPermission(code = "system:role:delete", name = "删除角色")
    @DeleteMapping("/delete-role/{id}")
    public ApiResponse<Void> deleteRole(@PathVariable Long id) {
        roleService.deleteRole(id);
        return ApiResponse.success();
    }

    /**
     * 查询用户拥有的角色列表
     *
     * @param userId 用户ID（路径参数）
     * @return 该用户拥有的角色列表
     */
    @RequiresPermission(code = "system:role:list", name = "查询角色列表")
    @Operation(summary = "Get roles by user ID")
    @GetMapping("/get-roles-by-user/{userId}")
    public ApiResponse<List<Role>> getRolesByUser(@PathVariable Long userId) {
        List<Role> roles = roleService.getRolesByUserId(userId);
        return ApiResponse.success(roles);
    }

    /**
     * 给用户分配角色
     * <p>
     * 先删除该用户原有的所有角色关联，再批量插入新的角色关联。
     * 该操作在事务中执行，确保数据一致性。
     * </p>
     *
     * @param request  分配角色请求体，包含目标用户ID和角色ID列表
     * @param tenantId 租户ID（请求头）
     * @param userId   当前操作用户ID（请求头）
     * @return 空响应
     */
    @Operation(summary = "Assign roles to user")
    @RequiresPermission(code = "system:role:assignToUser", name = "分配用户角色")
    @PostMapping("/assign-to-user")
    public ApiResponse<Void> assignRolesToUser(
            @RequestBody AssignRolesVO request,
            @RequestHeader("X-Tenant-Id") Long tenantId,
            @RequestHeader("X-User-Id") Long userId) {
        roleService.assignRolesToUser(request.getUserId(), tenantId, request.getRoleIds(), userId);
        return ApiResponse.success();
    }

    /**
     * 查询角色关联的权限ID列表
     *
     * @param roleId 角色ID（路径参数）
     * @return 该角色关联的权限ID列表
     */
    @RequiresPermission(code = "system:role:detail", name = "查询角色详情")
    @Operation(summary = "Get permissions by role ID")
    @GetMapping("/get-role-permissions/{roleId}")
    public ApiResponse<List<Long>> getRolePermissions(@PathVariable Long roleId) {
        List<Long> permissionIds = roleService.getPermissionIdsByRoleId(roleId);
        return ApiResponse.success(permissionIds);
    }

    /**
     * 给角色分配权限
     * <p>
     * 先删除该角色原有的所有权限关联，再批量插入新的权限关联。
     * 该操作在事务中执行，确保数据一致性。
     * </p>
     *
     * @param roleId   角色ID（路径参数）
     * @param request  分配权限请求体，包含权限ID列表
     * @param tenantId 租户ID（请求头）
     * @param userId   当前操作用户ID（请求头）
     * @return 空响应
     */
    @Operation(summary = "Assign permissions to role")
    @RequiresPermission(code = "system:role:assignPerm", name = "分配角色权限")
    @PostMapping("/{roleId}/permissions")
    public ApiResponse<Void> assignPermissions(
            @PathVariable Long roleId,
            @RequestBody AssignPermissionsVO request,
            @RequestHeader("X-Tenant-Id") Long tenantId,
            @RequestHeader("X-User-Id") Long userId) {
        roleService.assignPermissionsToRole(roleId, tenantId, request.getPermissionIds(), userId);
        return ApiResponse.success();
    }
}
