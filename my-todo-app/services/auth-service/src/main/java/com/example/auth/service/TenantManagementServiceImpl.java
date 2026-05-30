package com.example.auth.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.auth.entity.Tenant;
import com.example.auth.entity.TenantQuota;
import com.example.auth.entity.TenantResourceUsage;
import com.example.auth.mapper.TenantMapper;
import com.example.auth.mapper.TenantQuotaMapper;
import com.example.auth.mapper.TenantResourceUsageMapper;
import com.example.common.core.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 租户管理服务实现
 * <p>
 * 处理租户配额管理、资源使用量统计等业务逻辑。
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TenantManagementServiceImpl implements TenantManagementService {

    private final TenantQuotaMapper tenantQuotaMapper;
    private final TenantResourceUsageMapper tenantResourceUsageMapper;
    private final TenantMapper tenantMapper;

    @Override
    @Transactional
    public void initTenantQuota(Long tenantId) {
        TenantQuota existing = tenantQuotaMapper.selectOne(
                new LambdaQueryWrapper<TenantQuota>()
                        .eq(TenantQuota::getTenantId, tenantId)
        );
        if (existing != null) {
            log.warn("租户配额已存在: tenantId={}", tenantId);
            return;
        }

        TenantQuota quota = new TenantQuota();
        quota.setTenantId(tenantId);
        quota.setMaxUsers(5);
        quota.setMaxStorageMb(1024);
        quota.setMaxApiCallsPerDay(10000);
        quota.setMaxConcurrentRequests(100);
        quota.setCreatedAt(LocalDateTime.now());
        quota.setUpdatedAt(LocalDateTime.now());
        tenantQuotaMapper.insert(quota);

        // 初始化今日资源使用记录
        TenantResourceUsage usage = new TenantResourceUsage();
        usage.setTenantId(tenantId);
        usage.setUserCount(0);
        usage.setStorageUsedMb(BigDecimal.ZERO);
        usage.setApiCallsToday(0);
        usage.setConcurrentRequests(0);
        usage.setRecordDate(LocalDate.now());
        usage.setCreatedAt(LocalDateTime.now());
        tenantResourceUsageMapper.insert(usage);

        log.info("租户配额初始化成功: tenantId={}", tenantId);
    }

    @Override
    public TenantQuota getTenantQuota(Long tenantId) {
        TenantQuota quota = tenantQuotaMapper.selectOne(
                new LambdaQueryWrapper<TenantQuota>()
                        .eq(TenantQuota::getTenantId, tenantId)
        );
        if (quota == null) {
            throw new BusinessException(404, "租户配额不存在");
        }
        return quota;
    }

    @Override
    @Transactional
    public TenantQuota updateTenantQuota(Long tenantId, TenantQuota quota) {
        TenantQuota existing = getTenantQuota(tenantId);
        if (quota.getMaxUsers() != null) {
            existing.setMaxUsers(quota.getMaxUsers());
        }
        if (quota.getMaxStorageMb() != null) {
            existing.setMaxStorageMb(quota.getMaxStorageMb());
        }
        if (quota.getMaxApiCallsPerDay() != null) {
            existing.setMaxApiCallsPerDay(quota.getMaxApiCallsPerDay());
        }
        if (quota.getMaxConcurrentRequests() != null) {
            existing.setMaxConcurrentRequests(quota.getMaxConcurrentRequests());
        }
        existing.setUpdatedAt(LocalDateTime.now());
        tenantQuotaMapper.updateById(existing);
        log.info("租户配额更新成功: tenantId={}", tenantId);
        return existing;
    }

    @Override
    @Transactional
    public void checkAndIncrementUserCount(Long tenantId) {
        TenantQuota quota = getTenantQuota(tenantId);
        TenantResourceUsage usage = getTodayUsage(tenantId);

        if (usage.getUserCount() >= quota.getMaxUsers()) {
            throw new BusinessException(400, "已达到用户数量上限（" + quota.getMaxUsers() + "），无法添加更多用户");
        }

        usage.setUserCount(usage.getUserCount() + 1);
        tenantResourceUsageMapper.updateById(usage);
        log.info("租户用户计数递增: tenantId={}, userCount={}", tenantId, usage.getUserCount());
    }

    @Override
    @Transactional
    public void decrementUserCount(Long tenantId) {
        TenantResourceUsage usage = getTodayUsage(tenantId);
        if (usage.getUserCount() > 0) {
            usage.setUserCount(usage.getUserCount() - 1);
            tenantResourceUsageMapper.updateById(usage);
            log.info("租户用户计数递减: tenantId={}, userCount={}", tenantId, usage.getUserCount());
        }
    }

    @Override
    public TenantResourceUsage getTenantResourceUsage(Long tenantId) {
        return getTodayUsage(tenantId);
    }

    @Override
    @Transactional
    public void recordDailyUsage(Long tenantId, int apiCalls) {
        TenantResourceUsage usage = getTodayUsage(tenantId);
        usage.setApiCallsToday(usage.getApiCallsToday() + apiCalls);
        tenantResourceUsageMapper.updateById(usage);
    }

    @Override
    public Page<Tenant> getTenantList(int page, int size, String tenantName, Integer status) {
        LambdaQueryWrapper<Tenant> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Tenant::getDeleted, 0);
        if (tenantName != null && !tenantName.isEmpty()) {
            wrapper.like(Tenant::getTenantName, tenantName);
        }
        if (status != null) {
            wrapper.eq(Tenant::getStatus, status);
        }
        wrapper.orderByDesc(Tenant::getCreatedAt);
        return tenantMapper.selectPage(new Page<>(page, size), wrapper);
    }

    /**
     * 获取今日资源使用记录，不存在则创建
     */
    private TenantResourceUsage getTodayUsage(Long tenantId) {
        LocalDate today = LocalDate.now();
        TenantResourceUsage usage = tenantResourceUsageMapper.selectOne(
                new LambdaQueryWrapper<TenantResourceUsage>()
                        .eq(TenantResourceUsage::getTenantId, tenantId)
                        .eq(TenantResourceUsage::getRecordDate, today)
        );
        if (usage == null) {
            usage = new TenantResourceUsage();
            usage.setTenantId(tenantId);
            usage.setUserCount(0);
            usage.setStorageUsedMb(BigDecimal.ZERO);
            usage.setApiCallsToday(0);
            usage.setConcurrentRequests(0);
            usage.setRecordDate(today);
            usage.setCreatedAt(LocalDateTime.now());
            tenantResourceUsageMapper.insert(usage);
        }
        return usage;
    }
}
