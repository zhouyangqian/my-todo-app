package com.example.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.user.entity.AuditLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 审计日志数据访问接口
 */
@Mapper
public interface AuditLogMapper extends BaseMapper<AuditLog> {
}
