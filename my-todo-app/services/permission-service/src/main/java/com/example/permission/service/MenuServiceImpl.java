package com.example.permission.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.permission.api.dto.MenuDTO;
import com.example.permission.entity.SysMenu;
import com.example.permission.entity.SysRoleMenu;
import com.example.permission.entity.UserRole;
import com.example.permission.mapper.SysMenuMapper;
import com.example.permission.mapper.SysRoleMenuMapper;
import com.example.permission.mapper.UserRoleMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 菜单管理服务实现类
 * <p>
 * 处理菜单的增删改查、菜单树构建、角色菜单分配等业务逻辑。
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MenuServiceImpl extends ServiceImpl<SysMenuMapper, SysMenu> implements MenuService {

    private final SysRoleMenuMapper sysRoleMenuMapper;
    private final UserRoleMapper userRoleMapper;

    /**
     * 创建菜单
     *
     * @param menu 菜单实体
     * @return 创建后的菜单
     */
    @Override
    @Transactional
    public SysMenu createMenu(SysMenu menu) {
        // 校验权限编码唯一性
        if (menu.getPermissionCode() != null && !menu.getPermissionCode().isEmpty()) {
            SysMenu existing = getOne(
                    new LambdaQueryWrapper<SysMenu>()
                            .eq(SysMenu::getPermissionCode, menu.getPermissionCode())
                            .eq(SysMenu::getDeleted, 0)
            );
            if (existing != null) {
                throw new IllegalArgumentException("权限编码已存在: " + menu.getPermissionCode());
            }
        }
        menu.setCreatedAt(LocalDateTime.now());
        menu.setUpdatedAt(LocalDateTime.now());
        save(menu);
        log.info("菜单创建成功: id={}, name={}", menu.getId(), menu.getMenuName());
        return menu;
    }

    /**
     * 更新菜单
     *
     * @param menu 菜单实体
     * @return 更新后的菜单
     */
    @Override
    @Transactional
    public SysMenu updateMenu(SysMenu menu) {
        // 校验权限编码唯一性（排除自身）
        if (menu.getPermissionCode() != null && !menu.getPermissionCode().isEmpty()) {
            SysMenu existing = getOne(
                    new LambdaQueryWrapper<SysMenu>()
                            .eq(SysMenu::getPermissionCode, menu.getPermissionCode())
                            .ne(SysMenu::getId, menu.getId())
                            .eq(SysMenu::getDeleted, 0)
            );
            if (existing != null) {
                throw new IllegalArgumentException("权限编码已存在: " + menu.getPermissionCode());
            }
        }
        menu.setUpdatedAt(LocalDateTime.now());
        updateById(menu);
        log.info("菜单更新成功: id={}", menu.getId());
        return menu;
    }

    /**
     * 删除菜单（检查子菜单）
     *
     * @param id 菜单ID
     */
    @Override
    @Transactional
    public void deleteMenu(Long id) {
        // 检查是否有子菜单
        long childCount = count(
                new LambdaQueryWrapper<SysMenu>()
                        .eq(SysMenu::getParentId, id)
                        .eq(SysMenu::getDeleted, 0)
        );
        if (childCount > 0) {
            throw new IllegalArgumentException("存在子菜单，无法删除");
        }

        // 逻辑删除
        SysMenu menu = getById(id);
        if (menu != null) {
            menu.setDeleted(1);
            updateById(menu);
        }

        // 清理角色-菜单关联
        sysRoleMenuMapper.delete(
                new LambdaQueryWrapper<SysRoleMenu>()
                        .eq(SysRoleMenu::getMenuId, id)
        );
        log.info("菜单删除成功: id={}", id);
    }

    /**
     * 获取菜单树
     *
     * @param tenantId 租户ID
     * @return 菜单树（DTO格式）
     */
    @Override
    public List<MenuDTO> getMenuTree(Long tenantId) {
        List<SysMenu> allMenus = list(
                new LambdaQueryWrapper<SysMenu>()
                        .eq(SysMenu::getDeleted, 0)
                        .orderByAsc(SysMenu::getSortOrder)
        );
        List<MenuDTO> dtoList = allMenus.stream()
                .filter(m -> tenantId == null || tenantId.equals(0L) || tenantId.equals(m.getTenantId()) || m.getTenantId().equals(0L))
                .map(this::toDTO)
                .collect(Collectors.toList());
        return buildTree(dtoList);
    }

    /**
     * 获取角色的菜单树（带勾选状态）
     *
     * @param roleId   角色ID
     * @return 菜单树
     */
    @Override
    public List<MenuDTO> getMenuTreeByRoleId(Long roleId) {
        // 查询角色关联的菜单ID
        List<SysRoleMenu> roleMenus = sysRoleMenuMapper.selectList(
                new LambdaQueryWrapper<SysRoleMenu>()
                        .eq(SysRoleMenu::getRoleId, roleId)
        );
        Set<Long> roleMenuIds = roleMenus.stream()
                .map(SysRoleMenu::getMenuId)
                .collect(Collectors.toSet());

        List<SysMenu> allMenus = list(
                new LambdaQueryWrapper<SysMenu>()
                        .eq(SysMenu::getDeleted, 0)
                        .orderByAsc(SysMenu::getSortOrder)
        );

        List<MenuDTO> dtoList = allMenus.stream()
                .map(m -> {
                    MenuDTO dto = toDTO(m);
                    return dto;
                })
                .collect(Collectors.toList());

        return buildTree(dtoList);
    }

    /**
     * 获取角色已分配的菜单ID列表
     *
     * @param roleId 角色ID
     * @return 菜单ID列表
     */
    @Override
    public List<Long> getMenuIdsByRoleId(Long roleId) {
        List<SysRoleMenu> roleMenus = sysRoleMenuMapper.selectList(
                new LambdaQueryWrapper<SysRoleMenu>()
                        .eq(SysRoleMenu::getRoleId, roleId)
        );
        return roleMenus.stream()
                .map(SysRoleMenu::getMenuId)
                .collect(Collectors.toList());
    }

    /**
     * 分配菜单给角色
     *
     * @param roleId   角色ID
     * @param menuIds  菜单ID列表
     * @param tenantId 租户ID
     */
    @Override
    @Transactional
    public void assignMenusToRole(Long roleId, List<Long> menuIds, Long tenantId) {
        // 先删除原有关联
        sysRoleMenuMapper.delete(
                new LambdaQueryWrapper<SysRoleMenu>()
                        .eq(SysRoleMenu::getRoleId, roleId)
        );

        // 批量插入新关联
        if (menuIds != null && !menuIds.isEmpty()) {
            for (Long menuId : menuIds) {
                SysRoleMenu roleMenu = new SysRoleMenu();
                roleMenu.setRoleId(roleId);
                roleMenu.setMenuId(menuId);
                roleMenu.setTenantId(tenantId);
                roleMenu.setCreatedAt(LocalDateTime.now());
                sysRoleMenuMapper.insert(roleMenu);
            }
        }
        log.info("角色菜单分配成功: roleId={}, menuCount={}", roleId, menuIds != null ? menuIds.size() : 0);
    }

    /**
     * 获取用户的菜单（通过角色关联）
     *
     * @param userId   用户ID
     * @param tenantId 租户ID
     * @return 菜单树
     */
    @Override
    public List<MenuDTO> getUserMenus(Long userId, Long tenantId) {
        // 查询用户角色
        List<Long> roleIds = userRoleMapper.selectRoleIdsByUserId(userId);
        if (roleIds.isEmpty()) {
            return List.of();
        }

        // 查询角色关联的菜单ID
        List<SysRoleMenu> roleMenus = sysRoleMenuMapper.selectList(
                new LambdaQueryWrapper<SysRoleMenu>()
                        .in(SysRoleMenu::getRoleId, roleIds)
        );
        List<Long> menuIds = roleMenus.stream()
                .map(SysRoleMenu::getMenuId)
                .distinct()
                .collect(Collectors.toList());

        if (menuIds.isEmpty()) {
            return List.of();
        }

        // 查询菜单详情
        List<SysMenu> menus = listByIds(menuIds);
        List<MenuDTO> dtoList = menus.stream()
                .filter(m -> m.getDeleted() == 0 && m.getStatus() == 1 && m.getVisible() == 1)
                .map(this::toDTO)
                .collect(Collectors.toList());

        return buildTree(dtoList);
    }

    /**
     * 构建菜单树
     */
    private List<MenuDTO> buildTree(List<MenuDTO> allMenus) {
        Map<Long, MenuDTO> menuMap = new LinkedHashMap<>();
        for (MenuDTO dto : allMenus) {
            menuMap.put(dto.getId(), dto);
        }

        List<MenuDTO> roots = new ArrayList<>();
        for (MenuDTO dto : allMenus) {
            if (dto.getParentId() == null || dto.getParentId() == 0L) {
                roots.add(dto);
            } else {
                MenuDTO parent = menuMap.get(dto.getParentId());
                if (parent != null) {
                    if (parent.getChildren() == null) {
                        parent.setChildren(new ArrayList<>());
                    }
                    parent.getChildren().add(dto);
                }
            }
        }
        return roots;
    }

    /**
     * SysMenu 转 MenuDTO
     */
    private MenuDTO toDTO(SysMenu menu) {
        return MenuDTO.builder()
                .id(menu.getId())
                .parentId(menu.getParentId())
                .menuName(menu.getMenuName())
                .menuType(menu.getMenuType())
                .path(menu.getPath())
                .component(menu.getComponent())
                .permissionCode(menu.getPermissionCode())
                .icon(menu.getIcon())
                .sortOrder(menu.getSortOrder())
                .visible(menu.getVisible())
                .status(menu.getStatus())
                .build();
    }
}
