package com.example.dict.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.dict.entity.SaasPackage;
import com.example.dict.entity.TenantSubscription;

import java.time.LocalDate;
import java.util.List;

/**
 * SaaS套餐服务接口
 */
public interface SaaSPackageService extends IService<SaasPackage> {

    Page<SaasPackage> getPackagePage(int page, int size, String packageName);

    List<SaasPackage> listActive();

    SaasPackage createPackage(SaasPackage pkg);

    SaasPackage updatePackage(SaasPackage pkg);

    void deletePackage(Long id);

    void updateFeatures(Long packageId, String featuresJson);

    TenantSubscription subscribe(Long tenantId, Long packageId, LocalDate startDate, LocalDate endDate);

    TenantSubscription getActiveSubscription(Long tenantId);

    Page<TenantSubscription> getSubscriptionPage(Long tenantId, int page, int size);

    List<TenantSubscription> getSubscriptionHistory(Long tenantId);

    void cancelSubscription(Long subscriptionId);
}
