package com.example.permission.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.permission.entity.PermissionLog;

/**
 * 权限审计日志服务接口
 */
public interface PermissionLogService extends IService<PermissionLog> {

    void logPermissionChange(Long tenantId, Long userId, String resource,
                             String action, String permission,
                             Integer result, String reason, String ipAddress);

    Page<PermissionLog> getPermissionLogPage(Long tenantId, int page, int size,
                                              Long userId, String action, Integer result);
}
