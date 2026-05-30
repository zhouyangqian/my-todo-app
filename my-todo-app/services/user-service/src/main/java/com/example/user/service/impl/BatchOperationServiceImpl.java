package com.example.user.service.impl;

import com.example.user.api.dto.BatchResultDTO;
import com.example.user.entity.User;
import com.example.user.service.AuditLogService;
import com.example.user.service.BatchOperationService;
import com.example.user.service.RoleService;
import com.example.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 批量操作业务逻辑服务实现类
 * <p>
 * 提供用户的批量禁用、批量删除（软删除）、批量分配角色等功能，
 * 单次操作最多支持100个用户。
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BatchOperationServiceImpl implements BatchOperationService {

    private final UserService userService;
    private final RoleService roleService;
    private final AuditLogService auditLogService;

    /** 单次批量操作最大数量 */
    private static final int MAX_BATCH_SIZE = 100;

    /**
     * 批量禁用用户
     *
     * @param userIds    用户ID列表
     * @param tenantId   租户ID
     * @param operatorId 操作人ID
     * @param ipAddress  操作人IP地址
     * @return 批量操作结果
     */
    @Transactional
    @Override
    public BatchResultDTO batchDisableUsers(List<Long> userIds, Long tenantId, Long operatorId, String ipAddress) {
        validateBatchSize(userIds);

        int successCount = 0;
        List<BatchResultDTO.BatchFailureItem> failures = new ArrayList<>();

        for (Long userId : userIds) {
            try {
                User user = userService.getUserById(userId);
                if (user == null) {
                    failures.add(BatchResultDTO.BatchFailureItem.builder()
                            .userId(userId).reason("用户不存在").build());
                    continue;
                }
                userService.updateUserStatus(userId, 0);
                successCount++;
            } catch (Exception e) {
                log.error("批量禁用用户失败: userId={}", userId, e);
                failures.add(BatchResultDTO.BatchFailureItem.builder()
                        .userId(userId).reason(e.getMessage()).build());
            }
        }

        // 记录审计日志
        auditLogService.log(tenantId, operatorId, "BATCH_DISABLE", "USER", null,
                "批量禁用用户: 成功" + successCount + "个, 失败" + failures.size() + "个", ipAddress);

        return BatchResultDTO.builder()
                .successCount(successCount)
                .failCount(failures.size())
                .failures(failures)
                .build();
    }

    /**
     * 批量删除用户（软删除）
     *
     * @param userIds    用户ID列表
     * @param tenantId   租户ID
     * @param operatorId 操作人ID
     * @param ipAddress  操作人IP地址
     * @return 批量操作结果
     */
    @Transactional
    @Override
    public BatchResultDTO batchDeleteUsers(List<Long> userIds, Long tenantId, Long operatorId, String ipAddress) {
        validateBatchSize(userIds);

        int successCount = 0;
        List<BatchResultDTO.BatchFailureItem> failures = new ArrayList<>();

        for (Long userId : userIds) {
            try {
                User user = userService.getUserById(userId);
                if (user == null) {
                    failures.add(BatchResultDTO.BatchFailureItem.builder()
                            .userId(userId).reason("用户不存在").build());
                    continue;
                }
                userService.deleteUser(userId);
                successCount++;
            } catch (Exception e) {
                log.error("批量删除用户失败: userId={}", userId, e);
                failures.add(BatchResultDTO.BatchFailureItem.builder()
                        .userId(userId).reason(e.getMessage()).build());
            }
        }

        // 记录审计日志
        auditLogService.log(tenantId, operatorId, "BATCH_DELETE", "USER", null,
                "批量删除用户: 成功" + successCount + "个, 失败" + failures.size() + "个", ipAddress);

        return BatchResultDTO.builder()
                .successCount(successCount)
                .failCount(failures.size())
                .failures(failures)
                .build();
    }

    /**
     * 批量分配角色
     *
     * @param userIds    用户ID列表
     * @param roleIds    角色ID列表
     * @param tenantId   租户ID
     * @param operatorId 操作人ID
     * @param ipAddress  操作人IP地址
     * @return 批量操作结果
     */
    @Transactional
    @Override
    public BatchResultDTO batchAssignRoles(List<Long> userIds, List<Long> roleIds,
                                           Long tenantId, Long operatorId, String ipAddress) {
        validateBatchSize(userIds);

        if (roleIds == null || roleIds.isEmpty()) {
            throw new IllegalArgumentException("角色列表不能为空");
        }

        int successCount = 0;
        List<BatchResultDTO.BatchFailureItem> failures = new ArrayList<>();

        for (Long userId : userIds) {
            try {
                User user = userService.getUserById(userId);
                if (user == null) {
                    failures.add(BatchResultDTO.BatchFailureItem.builder()
                            .userId(userId).reason("用户不存在").build());
                    continue;
                }
                roleService.assignRolesToUser(userId, roleIds, tenantId);
                successCount++;
            } catch (Exception e) {
                log.error("批量分配角色失败: userId={}", userId, e);
                failures.add(BatchResultDTO.BatchFailureItem.builder()
                        .userId(userId).reason(e.getMessage()).build());
            }
        }

        // 记录审计日志
        auditLogService.log(tenantId, operatorId, "BATCH_ASSIGN_ROLE", "USER", null,
                "批量分配角色: 成功" + successCount + "个, 失败" + failures.size() + "个, 角色IDs=" + roleIds,
                ipAddress);

        return BatchResultDTO.builder()
                .successCount(successCount)
                .failCount(failures.size())
                .failures(failures)
                .build();
    }

    /**
     * 校验批量操作数量
     */
    private void validateBatchSize(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            throw new IllegalArgumentException("用户列表不能为空");
        }
        if (userIds.size() > MAX_BATCH_SIZE) {
            throw new IllegalArgumentException("单次操作不能超过" + MAX_BATCH_SIZE + "个用户");
        }
    }
}
