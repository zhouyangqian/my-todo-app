package com.example.user.service;

import com.example.user.api.dto.BatchResultDTO;

import java.util.List;

/**
 * 批量操作业务逻辑服务接口
 */
public interface BatchOperationService {

    BatchResultDTO batchDisableUsers(List<Long> userIds, Long tenantId, Long operatorId, String ipAddress);

    BatchResultDTO batchDeleteUsers(List<Long> userIds, Long tenantId, Long operatorId, String ipAddress);

    BatchResultDTO batchAssignRoles(List<Long> userIds, List<Long> roleIds,
                                    Long tenantId, Long operatorId, String ipAddress);
}
