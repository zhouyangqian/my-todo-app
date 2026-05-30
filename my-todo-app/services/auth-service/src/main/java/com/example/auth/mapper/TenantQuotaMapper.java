package com.example.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.auth.entity.TenantQuota;
import org.apache.ibatis.annotations.Mapper;

/**
 * 租户配额数据访问层
 * <p>继承 MyBatis-Plus BaseMapper，提供租户配额表的 CRUD 操作</p>
 */
@Mapper
public interface TenantQuotaMapper extends BaseMapper<TenantQuota> {
}
