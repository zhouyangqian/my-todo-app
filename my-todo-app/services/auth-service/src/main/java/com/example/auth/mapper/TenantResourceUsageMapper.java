package com.example.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.auth.entity.TenantResourceUsage;
import org.apache.ibatis.annotations.Mapper;

/**
 * 租户资源使用量数据访问层
 * <p>继承 MyBatis-Plus BaseMapper，提供租户资源使用量表的 CRUD 操作</p>
 */
@Mapper
public interface TenantResourceUsageMapper extends BaseMapper<TenantResourceUsage> {
}
