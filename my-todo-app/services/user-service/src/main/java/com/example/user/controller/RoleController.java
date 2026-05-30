package com.example.user.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.common.core.annotation.RequiresPermission;
import com.example.common.core.result.ApiResponse;
import com.example.common.core.result.PageResult;
import com.example.user.api.vo.RoleCreateVO;
import com.example.user.api.vo.RoleUpdateVO;
import com.example.user.api.vo.UserRoleAssignVO;
import com.example.user.entity.Role;
import com.example.user.service.AuditLogService;
import com.example.user.service.RoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 角色管理控制器
 * <p>
 * 提供角色的增删改查、用户角色分配/移除等接口。
 * </p>
 */
@Tag(name = "角色管理", description = "角色增删改查与用户角色分配API")
@RestController
@RequestMapping("/api/users/roles")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;
    private final AuditLogService auditLogService;

    /**
     * 创建角色
     */
    @RequiresPermission(code = "system:role:create", name = "创建角色")
    @Operation(summary = "创建角色")
    @PostMapping("/create-role")
    public ApiResponse<Role> createRole(
            @RequestBody @Valid RoleCreateVO vo,
            @RequestHeader("X-Tenant-Id") Long tenantId,
            @RequestHeader(value = "X-User-Id", required = false) Long userId,
            HttpServletRequest request) {
        Role role = new Role();
        BeanUtils.copyProperties(vo, role);
        role.setTenantId(tenantId);
        role.setCreatedBy(userId);
        Role created = roleService.createRole(role);
        // 记录审计日志
        auditLogService.log(tenantId, userId, "CREATE_ROLE", "ROLE",
                created.getId(), "创建角色: " + created.getRoleCode(), getClientIp(request));
        return ApiResponse.success(created);
    }

    /**
     * 更新角色
     */
    @RequiresPermission(code = "system:role:update", name = "更新角色")
    @Operation(summary = "更新角色")
    @PutMapping("/update-role/{id}")
    public ApiResponse<Role> updateRole(
            @PathVariable Long id,
            @RequestBody @Valid RoleUpdateVO vo,
            @RequestHeader("X-Tenant-Id") Long tenantId,
            @RequestHeader(value = "X-User-Id", required = false) Long userId,
            HttpServletRequest request) {
        Role role = new Role();
        BeanUtils.copyProperties(vo, role);
        role.setId(id);
        role.setUpdatedBy(userId);
        Role updated = roleService.updateRole(role);
        // 记录审计日志
        auditLogService.log(tenantId, userId, "UPDATE_ROLE", "ROLE",
                id, "更新角色: " + id, getClientIp(request));
        return ApiResponse.success(updated);
    }

    /**
     * 删除角色（软删除）
     */
    @RequiresPermission(code = "system:role:delete", name = "删除角色")
    @Operation(summary = "删除角色")
    @DeleteMapping("/delete-role/{id}")
    public ApiResponse<Void> deleteRole(
            @PathVariable Long id,
            @RequestHeader("X-Tenant-Id") Long tenantId,
            @RequestHeader(value = "X-User-Id", required = false) Long userId,
            HttpServletRequest request) {
        roleService.deleteRole(id);
        // 记录审计日志
        auditLogService.log(tenantId, userId, "DELETE_ROLE", "ROLE",
                id, "删除角色: " + id, getClientIp(request));
        return ApiResponse.success();
    }

    /**
     * 分页查询角色列表
     */
    @RequiresPermission(code = "system:role:list", name = "查询角色列表")
    @Operation(summary = "分页查询角色")
    @GetMapping("/get-role-page")
    public ApiResponse<PageResult<Role>> getRolePage(
            @RequestHeader("X-Tenant-Id") Long tenantId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String roleName) {
        Page<Role> result = roleService.getRolePage(tenantId, page, size, roleName);
        PageResult<Role> pageResult = PageResult.of(result.getRecords(), result.getTotal(), result.getCurrent(), result.getSize());
        return ApiResponse.success(pageResult);
    }

    /**
     * 为用户分配角色
     */
    @RequiresPermission(code = "system:role:assignUser", name = "分配用户角色")
    @Operation(summary = "分配用户角色")
    @PostMapping("/assign-roles")
    public ApiResponse<Void> assignRoles(
            @RequestBody @Valid UserRoleAssignVO vo,
            @RequestHeader("X-Tenant-Id") Long tenantId,
            @RequestHeader(value = "X-User-Id", required = false) Long userId,
            HttpServletRequest request) {
        roleService.assignRolesToUser(vo.getUserId(), vo.getRoleIds(), tenantId);
        // 记录审计日志
        auditLogService.log(tenantId, userId, "ASSIGN_ROLE", "USER",
                vo.getUserId(), "为用户分配角色: roleIds=" + vo.getRoleIds(), getClientIp(request));
        return ApiResponse.success();
    }

    /**
     * 移除用户角色
     */
    @RequiresPermission(code = "system:role:assignUser", name = "移除用户角色")
    @Operation(summary = "移除用户角色")
    @DeleteMapping("/remove-roles")
    public ApiResponse<Void> removeRoles(
            @RequestBody @Valid UserRoleAssignVO vo,
            @RequestHeader("X-Tenant-Id") Long tenantId,
            @RequestHeader(value = "X-User-Id", required = false) Long userId,
            HttpServletRequest request) {
        roleService.removeRolesFromUser(vo.getUserId(), vo.getRoleIds(), tenantId);
        // 记录审计日志
        auditLogService.log(tenantId, userId, "REMOVE_ROLE", "USER",
                vo.getUserId(), "移除用户角色: roleIds=" + vo.getRoleIds(), getClientIp(request));
        return ApiResponse.success();
    }

    /**
     * 查询用户拥有的角色列表
     */
    @RequiresPermission(code = "system:role:list", name = "查询用户角色")
    @Operation(summary = "查询用户角色")
    @GetMapping("/get-user-roles/{userId}")
    public ApiResponse<List<Role>> getUserRoles(@PathVariable Long userId) {
        List<Role> roles = roleService.getRolesByUserId(userId);
        return ApiResponse.success(roles);
    }

    /**
     * 获取客户端IP地址
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 多级代理时取第一个IP
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
