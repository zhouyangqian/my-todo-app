package com.example.permission.controller;

import com.example.common.core.result.ApiResponse;
import com.example.permission.api.dto.MenuDTO;
import com.example.permission.api.vo.MenuCreateVO;
import com.example.permission.api.vo.RoleMenuAssignVO;
import com.example.permission.entity.SysMenu;
import com.example.permission.service.MenuService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 菜单管理控制器
 * <p>
 * 提供菜单的增删改查、菜单树获取、角色菜单分配等接口。
 * </p>
 */
@Tag(name = "菜单管理", description = "菜单增删改查与角色分配接口")
@RestController
@RequestMapping("/api/permissions/menus")
@RequiredArgsConstructor
public class MenuController {

    private final MenuService menuService;

    /**
     * 创建菜单
     */
    @Operation(summary = "创建菜单")
    @PostMapping("/create")
    public ApiResponse<SysMenu> createMenu(
            @Valid @RequestBody MenuCreateVO vo,
            @RequestHeader("X-Tenant-Id") Long tenantId,
            @RequestHeader("X-User-Id") Long userId) {
        SysMenu menu = new SysMenu();
        menu.setTenantId(tenantId);
        menu.setParentId(vo.getParentId());
        menu.setMenuName(vo.getMenuName());
        menu.setMenuType(vo.getMenuType());
        menu.setPath(vo.getPath());
        menu.setComponent(vo.getComponent());
        menu.setPermissionCode(vo.getPermissionCode());
        menu.setIcon(vo.getIcon());
        menu.setSortOrder(vo.getSortOrder() != null ? vo.getSortOrder() : 0);
        menu.setVisible(vo.getVisible() != null ? vo.getVisible() : 1);
        menu.setStatus(1);
        menu.setCreatedBy(userId);
        SysMenu created = menuService.createMenu(menu);
        return ApiResponse.success(created);
    }

    /**
     * 更新菜单
     */
    @Operation(summary = "更新菜单")
    @PutMapping("/update/{id}")
    public ApiResponse<SysMenu> updateMenu(
            @PathVariable Long id,
            @Valid @RequestBody MenuCreateVO vo,
            @RequestHeader("X-User-Id") Long userId) {
        SysMenu menu = new SysMenu();
        menu.setId(id);
        menu.setParentId(vo.getParentId());
        menu.setMenuName(vo.getMenuName());
        menu.setMenuType(vo.getMenuType());
        menu.setPath(vo.getPath());
        menu.setComponent(vo.getComponent());
        menu.setPermissionCode(vo.getPermissionCode());
        menu.setIcon(vo.getIcon());
        menu.setSortOrder(vo.getSortOrder());
        menu.setVisible(vo.getVisible());
        menu.setUpdatedBy(userId);
        SysMenu updated = menuService.updateMenu(menu);
        return ApiResponse.success(updated);
    }

    /**
     * 删除菜单
     */
    @Operation(summary = "删除菜单")
    @DeleteMapping("/delete/{id}")
    public ApiResponse<Void> deleteMenu(@PathVariable Long id) {
        menuService.deleteMenu(id);
        return ApiResponse.success();
    }

    /**
     * 获取菜单树
     */
    @Operation(summary = "获取菜单树")
    @GetMapping("/tree")
    public ApiResponse<List<MenuDTO>> getMenuTree(
            @RequestHeader(value = "X-Tenant-Id", required = false) Long tenantId) {
        List<MenuDTO> tree = menuService.getMenuTree(tenantId);
        return ApiResponse.success(tree);
    }

    /**
     * 获取角色的菜单树
     */
    @Operation(summary = "获取角色菜单树")
    @GetMapping("/role-tree/{roleId}")
    public ApiResponse<List<MenuDTO>> getRoleMenuTree(@PathVariable Long roleId) {
        List<MenuDTO> tree = menuService.getMenuTreeByRoleId(roleId);
        return ApiResponse.success(tree);
    }

    /**
     * 分配菜单给角色
     */
    @Operation(summary = "分配菜单给角色")
    @PostMapping("/assign-role")
    public ApiResponse<Void> assignMenusToRole(
            @Valid @RequestBody RoleMenuAssignVO vo,
            @RequestHeader("X-Tenant-Id") Long tenantId) {
        menuService.assignMenusToRole(vo.getRoleId(), vo.getMenuIds(), tenantId);
        return ApiResponse.success();
    }

    /**
     * 获取当前用户的菜单
     */
    @Operation(summary = "获取当前用户菜单")
    @GetMapping("/user-menus")
    public ApiResponse<List<MenuDTO>> getUserMenus(
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader("X-Tenant-Id") Long tenantId) {
        List<MenuDTO> menus = menuService.getUserMenus(userId, tenantId);
        return ApiResponse.success(menus);
    }
}
