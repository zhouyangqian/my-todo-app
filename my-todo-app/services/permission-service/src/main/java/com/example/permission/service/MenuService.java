package com.example.permission.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.permission.api.dto.MenuDTO;
import com.example.permission.entity.SysMenu;

import java.util.List;

/**
 * 菜单管理服务接口
 */
public interface MenuService extends IService<SysMenu> {

    SysMenu createMenu(SysMenu menu);

    SysMenu updateMenu(SysMenu menu);

    void deleteMenu(Long id);

    List<MenuDTO> getMenuTree(Long tenantId);

    List<MenuDTO> getMenuTreeByRoleId(Long roleId);

    List<Long> getMenuIdsByRoleId(Long roleId);

    void assignMenusToRole(Long roleId, List<Long> menuIds, Long tenantId);

    List<MenuDTO> getUserMenus(Long userId, Long tenantId);
}
