package com.example.permission.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.permission.entity.SysMenu;
import org.apache.ibatis.annotations.Mapper;

/**
 * 菜单数据访问层
 * <p>继承 MyBatis-Plus BaseMapper，提供菜单表的 CRUD 操作</p>
 */
@Mapper
public interface SysMenuMapper extends BaseMapper<SysMenu> {
}
