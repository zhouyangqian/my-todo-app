package com.example.user.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.user.entity.AuditLog;

/**
 * 审计日志业务逻辑服务接口
 */
public interface AuditLogService extends IService<AuditLog> {

    void log(Long tenantId, Long operatorId, String operationType,
             String targetType, Long targetId, String detail, String ipAddress);

    Page<AuditLog> getAuditLogPage(Long tenantId, int page, int size,
                                    String operationType, Long operatorId);
}
