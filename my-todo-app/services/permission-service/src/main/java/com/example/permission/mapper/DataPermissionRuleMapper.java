package com.example.permission.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.permission.entity.DataPermissionRule;
import org.apache.ibatis.annotations.Mapper;

/**
 * 数据权限规则数据访问接口
 * <p>
 * 继承 MyBatis-Plus 的 BaseMapper，提供数据权限规则表的增删改查基础操作。
 * </p>
 */
@Mapper
public interface DataPermissionRuleMapper extends BaseMapper<DataPermissionRule> {
}
