package com.example.dict.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.dict.entity.ApiDefinition;
import com.example.dict.entity.ApiSubscription;
import com.example.dict.entity.ApiUsageRecord;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * API市场服务接口
 */
public interface ApiMarketService extends IService<ApiDefinition> {

    Page<ApiDefinition> getApiDefinitionPage(Long tenantId, int page, int size, String apiName);

    ApiDefinition createApiDefinition(ApiDefinition apiDefinition);

    ApiDefinition updateApiDefinition(ApiDefinition apiDefinition);

    void deleteApiDefinition(Long id);

    Page<ApiSubscription> getSubscriptionPage(Long tenantId, int page, int size, Long apiId);

    ApiSubscription subscribe(Long tenantId, Long apiId, String subscriberName,
                              Integer callLimit, LocalDateTime expiresAt);

    void unsubscribe(Long subscriptionId, Long tenantId);

    ApiUsageRecord recordUsage(Long tenantId, Long apiId, Long subscriptionId,
                               Integer responseStatus, Integer responseTimeMs);

    Page<ApiUsageRecord> getUsageRecordPage(Long tenantId, int page, int size, Long apiId);

    Map<String, Object> getUsageStats(Long tenantId, Long apiId);
}
