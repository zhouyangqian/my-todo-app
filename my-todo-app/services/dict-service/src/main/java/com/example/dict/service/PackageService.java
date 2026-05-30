package com.example.dict.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.dict.entity.SaasPackage;
import com.example.dict.entity.TenantSubscription;

import java.time.LocalDate;

/**
 * 套餐管理服务接口
 */
public interface PackageService extends IService<SaasPackage> {

    Page<SaasPackage> getPackagePage(int page, int size, String packageName);

    SaasPackage createPackage(SaasPackage pkg);

    SaasPackage updatePackage(SaasPackage pkg);

    void deletePackage(Long id);

    TenantSubscription subscribe(Long tenantId, Long packageId, LocalDate startDate, LocalDate endDate);

    Page<TenantSubscription> getTenantSubscriptionPage(Long tenantId, int page, int size);
}
