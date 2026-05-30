package com.example.permission.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.permission.entity.PermissionTemplate;
import org.apache.ibatis.annotations.Mapper;

/**
 * 权限模板数据访问层
 */
@Mapper
public interface PermissionTemplateMapper extends BaseMapper<PermissionTemplate> {
}
