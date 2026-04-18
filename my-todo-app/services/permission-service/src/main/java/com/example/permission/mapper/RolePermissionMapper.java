package com.example.permission.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.permission.entity.RolePermission;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 角色-权限关联数据访问接口
 * <p>
 * 继承 MyBatis-Plus 的 BaseMapper，提供角色-权限关联表的增删改查基础操作。
 * 扩展了根据角色ID列表批量查询权限编码的自定义方法。
 * </p>
 */
@Mapper
public interface RolePermissionMapper extends BaseMapper<RolePermission> {

    /**
     * 根据角色ID列表批量查询对应的权限编码
     *
     * @param roleIds 角色ID列表
     * @return 这些角色关联的所有权限编码列表
     */
    List<String> selectPermissionCodesByRoleIds(@Param("roleIds") List<Long> roleIds);
}
