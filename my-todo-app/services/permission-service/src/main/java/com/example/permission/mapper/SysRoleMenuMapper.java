package com.example.permission.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.permission.entity.SysRoleMenu;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 角色-菜单关联数据访问层
 * <p>继承 MyBatis-Plus BaseMapper，提供角色菜单关联表的 CRUD 操作</p>
 */
@Mapper
public interface SysRoleMenuMapper extends BaseMapper<SysRoleMenu> {

    /**
     * 根据角色ID列表查询菜单ID
     *
     * @param roleIds 角色ID列表
     * @return 菜单ID列表
     */
    List<Long> selectMenuIdsByRoleIds(@Param("roleIds") List<Long> roleIds);
}
