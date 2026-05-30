package com.example.permission.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.permission.entity.Blacklist;
import com.example.permission.mapper.BlacklistMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 黑名单管理服务实现类
 * <p>
 * 提供黑名单的加入、解除、查询、检查等功能。
 * 加入黑名单时会自动踢出该用户的所有会话。
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BlacklistServiceImpl extends ServiceImpl<BlacklistMapper, Blacklist> implements BlacklistService {

    private final SessionService sessionService;

    /** 黑名单生效状态 */
    private static final int STATUS_ACTIVE = 1;
    /** 黑名单已解除状态 */
    private static final int STATUS_REMOVED = 0;

    /**
     * 加入黑名单
     * <p>
     * 将用户加入黑名单并踢出其所有会话。如果用户已在黑名单中且处于生效状态，则抛出异常。
     * </p>
     *
     * @param userId       被拉黑用户ID
     * @param reason       拉黑原因
     * @param operatorId   操作人ID
     * @param operatorType 操作人类型
     * @param tenantId     租户ID
     * @return 创建的黑名单记录
     */
    @Override
    @Transactional
    public Blacklist addToBlacklist(Long userId, String reason, Long operatorId, String operatorType, Long tenantId) {
        // 检查用户是否已在黑名单中（生效状态）
        Blacklist existing = getOne(
                new LambdaQueryWrapper<Blacklist>()
                        .eq(Blacklist::getTenantId, tenantId)
                        .eq(Blacklist::getUserId, userId)
                        .eq(Blacklist::getStatus, STATUS_ACTIVE)
        );
        if (existing != null) {
            throw new IllegalArgumentException("用户已在黑名单中");
        }

        Blacklist blacklist = new Blacklist();
        blacklist.setTenantId(tenantId);
        blacklist.setUserId(userId);
        blacklist.setReason(reason);
        blacklist.setOperatorId(operatorId);
        blacklist.setOperatorType(operatorType != null ? operatorType : "TENANT_ADMIN");
        blacklist.setStatus(STATUS_ACTIVE);
        blacklist.setAddedAt(LocalDateTime.now());
        blacklist.setCreatedAt(LocalDateTime.now());
        blacklist.setUpdatedAt(LocalDateTime.now());

        save(blacklist);

        // 踢出用户所有会话
        sessionService.kickAllUserSessions(userId);

        log.info("用户 {} 已被加入黑名单，操作人: {}", userId, operatorId);
        return blacklist;
    }

    /**
     * 解除黑名单
     * <p>
     * 将指定的黑名单记录状态改为已解除，记录解除时间和操作人。
     * </p>
     *
     * @param id        黑名单记录ID
     * @param removedBy 解除操作人ID
     */
    @Override
    @Transactional
    public void removeFromBlacklist(Long id, Long removedBy) {
        Blacklist blacklist = getById(id);
        if (blacklist == null) {
            throw new IllegalArgumentException("黑名单记录不存在");
        }
        if (blacklist.getStatus() == STATUS_REMOVED) {
            throw new IllegalArgumentException("该记录已被解除");
        }

        blacklist.setStatus(STATUS_REMOVED);
        blacklist.setRemovedAt(LocalDateTime.now());
        blacklist.setRemovedBy(removedBy);
        blacklist.setUpdatedAt(LocalDateTime.now());
        updateById(blacklist);

        log.info("黑名单记录 {} 已被解除，操作人: {}", id, removedBy);
    }

    /**
     * 检查用户是否在黑名单中
     *
     * @param userId   用户ID
     * @param tenantId 租户ID
     * @return true=在黑名单中，false=不在
     */
    @Override
    public boolean isUserBlacklisted(Long userId, Long tenantId) {
        return count(
                new LambdaQueryWrapper<Blacklist>()
                        .eq(Blacklist::getTenantId, tenantId)
                        .eq(Blacklist::getUserId, userId)
                        .eq(Blacklist::getStatus, STATUS_ACTIVE)
        ) > 0;
    }

    /**
     * 分页查询黑名单
     *
     * @param tenantId 租户ID
     * @param page     当前页码
     * @param size     每页条数
     * @param status   状态过滤（可选）
     * @return 分页结果
     */
    @Override
    public Page<Blacklist> getBlacklistPage(Long tenantId, int page, int size, Integer status) {
        LambdaQueryWrapper<Blacklist> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Blacklist::getTenantId, tenantId);
        if (status != null) {
            wrapper.eq(Blacklist::getStatus, status);
        }
        wrapper.orderByDesc(Blacklist::getAddedAt);
        return page(new Page<>(page, size), wrapper);
    }

    /**
     * 查询用户的黑名单记录
     *
     * @param userId   用户ID
     * @param tenantId 租户ID
     * @return 黑名单记录，不存在则返回null
     */
    @Override
    public Blacklist getByUserId(Long userId, Long tenantId) {
        return getOne(
                new LambdaQueryWrapper<Blacklist>()
                        .eq(Blacklist::getTenantId, tenantId)
                        .eq(Blacklist::getUserId, userId)
                        .eq(Blacklist::getStatus, STATUS_ACTIVE)
                        .orderByDesc(Blacklist::getAddedAt)
                        .last("LIMIT 1")
        );
    }
}
