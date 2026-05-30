package com.example.permission.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.permission.entity.PermissionLog;
import com.example.permission.mapper.PermissionLogMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 权限审计日志服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PermissionLogServiceImpl extends ServiceImpl<PermissionLogMapper, PermissionLog> implements PermissionLogService {

    /**
     * 记录权限变更日志
     */
    @Override
    public void logPermissionChange(Long tenantId, Long userId, String resource,
                                    String action, String permission,
                                    Integer result, String reason, String ipAddress) {
        PermissionLog permissionLog = new PermissionLog();
        permissionLog.setTenantId(tenantId);
        permissionLog.setUserId(userId);
        permissionLog.setResource(resource);
        permissionLog.setAction(action);
        permissionLog.setPermission(permission);
        permissionLog.setResult(result);
        permissionLog.setReason(reason);
        permissionLog.setIpAddress(ipAddress);
        save(permissionLog);
        log.debug("权限审计日志已记录: userId={}, permission={}, result={}", userId, permission, result);
    }

    /**
     * 分页查询审计日志
     */
    @Override
    public Page<PermissionLog> getPermissionLogPage(Long tenantId, int page, int size,
                                                     Long userId, String action, Integer result) {
        LambdaQueryWrapper<PermissionLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PermissionLog::getTenantId, tenantId);
        if (userId != null) {
            wrapper.eq(PermissionLog::getUserId, userId);
        }
        if (action != null && !action.isEmpty()) {
            wrapper.eq(PermissionLog::getAction, action);
        }
        if (result != null) {
            wrapper.eq(PermissionLog::getResult, result);
        }
        wrapper.orderByDesc(PermissionLog::getCreatedAt);
        return page(new Page<>(page, size), wrapper);
    }
}
