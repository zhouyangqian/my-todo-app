package com.example.permission.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.permission.entity.RoleInheritance;
import org.apache.ibatis.annotations.Mapper;

/**
 * 角色继承关系数据访问接口
 * <p>
 * 继承 MyBatis-Plus 的 BaseMapper，提供角色继承关系表的增删改查基础操作。
 * </p>
 */
@Mapper
public interface RoleInheritanceMapper extends BaseMapper<RoleInheritance> {
}
