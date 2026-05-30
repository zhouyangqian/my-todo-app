package com.example.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.auth.entity.Tenant;
import org.apache.ibatis.annotations.Mapper;

/**
 * 租户数据访问层
 * <p>继承 MyBatis-Plus BaseMapper，提供租户表的 CRUD 操作</p>
 */
@Mapper
public interface TenantMapper extends BaseMapper<Tenant> {
}
