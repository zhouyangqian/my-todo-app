package com.example.dict.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.dict.entity.SaasPackage;
import com.example.dict.entity.TenantSubscription;
import com.example.dict.mapper.SaasPackageMapper;
import com.example.dict.mapper.TenantSubscriptionMapper;
import com.example.dict.service.SaaSPackageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * SaaS套餐服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SaaSPackageServiceImpl extends ServiceImpl<SaasPackageMapper, SaasPackage> implements SaaSPackageService {

    private final TenantSubscriptionMapper tenantSubscriptionMapper;

    // ==================== 套餐管理 ====================

    @Override
    public Page<SaasPackage> getPackagePage(int page, int size, String packageName) {
        LambdaQueryWrapper<SaasPackage> wrapper = new LambdaQueryWrapper<>();
        if (packageName != null && !packageName.isEmpty()) {
            wrapper.like(SaasPackage::getPackageName, packageName);
        }
        wrapper.orderByDesc(SaasPackage::getCreatedAt);
        return page(new Page<>(page, size), wrapper);
    }

    @Override
    public List<SaasPackage> listActive() {
        return list(new LambdaQueryWrapper<SaasPackage>()
                .eq(SaasPackage::getStatus, 1)
                .orderByAsc(SaasPackage::getPrice));
    }

    @Override
    @Transactional
    public SaasPackage createPackage(SaasPackage pkg) {
        // 检查编码唯一性
        SaasPackage existing = getOne(new LambdaQueryWrapper<SaasPackage>()
                .eq(SaasPackage::getPackageCode, pkg.getPackageCode()));
        if (existing != null) {
            throw new IllegalArgumentException("套餐编码已存在: " + pkg.getPackageCode());
        }
        save(pkg);
        return pkg;
    }

    @Override
    @Transactional
    public SaasPackage updatePackage(SaasPackage pkg) {
        updateById(pkg);
        return pkg;
    }

    @Override
    @Transactional
    public void deletePackage(Long id) {
        removeById(id);
    }

    // ==================== 功能特性管理(存储在features JSON字段中) ====================

    @Override
    @Transactional
    public void updateFeatures(Long packageId, String featuresJson) {
        SaasPackage pkg = getById(packageId);
        if (pkg == null) {
            throw new IllegalArgumentException("套餐不存在: " + packageId);
        }
        pkg.setFeatures(featuresJson);
        updateById(pkg);
    }

    // ==================== 租户订阅管理 ====================

    @Override
    @Transactional
    public TenantSubscription subscribe(Long tenantId, Long packageId, LocalDate startDate, LocalDate endDate) {
        TenantSubscription sub = new TenantSubscription();
        sub.setTenantId(tenantId);
        sub.setPackageId(packageId);
        sub.setStartDate(startDate);
        sub.setEndDate(endDate);
        sub.setStatus(1);
        tenantSubscriptionMapper.insert(sub);
        return sub;
    }

    @Override
    public TenantSubscription getActiveSubscription(Long tenantId) {
        return tenantSubscriptionMapper.selectOne(
                new LambdaQueryWrapper<TenantSubscription>()
                        .eq(TenantSubscription::getTenantId, tenantId)
                        .eq(TenantSubscription::getStatus, 1)
                        .orderByDesc(TenantSubscription::getCreatedAt)
                        .last("LIMIT 1"));
    }

    @Override
    public Page<TenantSubscription> getSubscriptionPage(Long tenantId, int page, int size) {
        LambdaQueryWrapper<TenantSubscription> wrapper = new LambdaQueryWrapper<TenantSubscription>()
                .eq(TenantSubscription::getTenantId, tenantId)
                .orderByDesc(TenantSubscription::getCreatedAt);
        return tenantSubscriptionMapper.selectPage(new Page<>(page, size), wrapper);
    }

    @Override
    public List<TenantSubscription> getSubscriptionHistory(Long tenantId) {
        return tenantSubscriptionMapper.selectList(
                new LambdaQueryWrapper<TenantSubscription>()
                        .eq(TenantSubscription::getTenantId, tenantId)
                        .orderByDesc(TenantSubscription::getCreatedAt));
    }

    @Override
    @Transactional
    public void cancelSubscription(Long subscriptionId) {
        TenantSubscription sub = tenantSubscriptionMapper.selectById(subscriptionId);
        if (sub != null) {
            sub.setStatus(0);
            tenantSubscriptionMapper.updateById(sub);
        }
    }
}
