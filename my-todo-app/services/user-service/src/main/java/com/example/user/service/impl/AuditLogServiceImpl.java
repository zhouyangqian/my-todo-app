package com.example.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.user.entity.AuditLog;
import com.example.user.mapper.AuditLogMapper;
import com.example.user.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 审计日志业务逻辑服务实现类
 * <p>
 * 提供审计日志的记录和分页查询功能。
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuditLogServiceImpl extends ServiceImpl<AuditLogMapper, AuditLog> implements AuditLogService {

    /**
     * 记录审计日志
     *
     * @param tenantId      租户ID
     * @param operatorId    操作人ID
     * @param operationType 操作类型
     * @param targetType    目标类型
     * @param targetId      目标ID
     * @param detail        操作详情
     * @param ipAddress     操作人IP地址
     */
    @Override
    public void log(Long tenantId, Long operatorId, String operationType,
                    String targetType, Long targetId, String detail, String ipAddress) {
        AuditLog auditLog = new AuditLog();
        auditLog.setTenantId(tenantId);
        auditLog.setOperatorId(operatorId);
        auditLog.setOperationType(operationType);
        auditLog.setTargetType(targetType);
        auditLog.setTargetId(targetId);
        auditLog.setResult(1);
        auditLog.setIpAddress(ipAddress);
        auditLog.setDetail(detail);
        save(auditLog);
    }

    /**
     * 分页查询审计日志
     *
     * @param tenantId      租户ID
     * @param page          页码
     * @param size          每页大小
     * @param operationType 操作类型（可选）
     * @param operatorId    操作人ID（可选）
     * @return 分页结果
     */
    @Override
    public Page<AuditLog> getAuditLogPage(Long tenantId, int page, int size,
                                           String operationType, Long operatorId) {
        LambdaQueryWrapper<AuditLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AuditLog::getTenantId, tenantId)
               .eq(AuditLog::getDeleted, 0);
        if (operationType != null && !operationType.isEmpty()) {
            wrapper.eq(AuditLog::getOperationType, operationType);
        }
        if (operatorId != null) {
            wrapper.eq(AuditLog::getOperatorId, operatorId);
        }
        wrapper.orderByDesc(AuditLog::getCreatedAt);
        return page(new Page<>(page, size), wrapper);
    }
}
