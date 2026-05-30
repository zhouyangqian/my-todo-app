package com.example.dict.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.dict.entity.SaasPackage;
import com.example.dict.entity.TenantSubscription;
import com.example.dict.mapper.SaasPackageMapper;
import com.example.dict.mapper.TenantSubscriptionMapper;
import com.example.dict.service.PackageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

/**
 * 套餐管理服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PackageServiceImpl extends ServiceImpl<SaasPackageMapper, SaasPackage> implements PackageService {

    private final TenantSubscriptionMapper tenantSubscriptionMapper;

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
    @Transactional
    public SaasPackage createPackage(SaasPackage pkg) {
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
    public Page<TenantSubscription> getTenantSubscriptionPage(Long tenantId, int page, int size) {
        LambdaQueryWrapper<TenantSubscription> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TenantSubscription::getTenantId, tenantId)
               .orderByDesc(TenantSubscription::getCreatedAt);
        return tenantSubscriptionMapper.selectPage(new Page<>(page, size), wrapper);
    }
}
