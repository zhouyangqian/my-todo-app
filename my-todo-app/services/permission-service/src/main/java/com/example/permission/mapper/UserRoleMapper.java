package com.example.permission.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.permission.entity.UserRole;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 用户-角色关联数据访问接口
 * <p>
 * 继承 MyBatis-Plus 的 BaseMapper，提供用户-角色关联表的增删改查基础操作。
 * 扩展了根据用户ID查询角色编码和角色ID的自定义方法。
 * </p>
 */
@Mapper
public interface UserRoleMapper extends BaseMapper<UserRole> {

    /**
     * 根据用户ID查询该用户拥有的所有角色编码
     *
     * @param userId 用户ID
     * @return 角色编码列表（如 ["ADMIN", "EDITOR"]）
     */
    List<String> selectRoleCodesByUserId(@Param("userId") Long userId);

    /**
     * 根据用户ID查询该用户拥有的所有角色ID
     *
     * @param userId 用户ID
     * @return 角色ID列表
     */
    List<Long> selectRoleIdsByUserId(@Param("userId") Long userId);
}
