package com.example.dict.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.dict.entity.ApiDefinition;
import com.example.dict.entity.ApiSubscription;
import com.example.dict.entity.ApiUsageRecord;
import com.example.dict.mapper.ApiDefinitionMapper;
import com.example.dict.mapper.ApiSubscriptionMapper;
import com.example.dict.mapper.ApiUsageRecordMapper;
import com.example.dict.service.ApiMarketService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * API市场服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ApiMarketServiceImpl extends ServiceImpl<ApiDefinitionMapper, ApiDefinition> implements ApiMarketService {

    private final ApiSubscriptionMapper subscriptionMapper;
    private final ApiUsageRecordMapper usageRecordMapper;

    // ==================== API定义管理 ====================

    @Override
    public Page<ApiDefinition> getApiDefinitionPage(Long tenantId, int page, int size, String apiName) {
        LambdaQueryWrapper<ApiDefinition> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ApiDefinition::getTenantId, tenantId);
        if (apiName != null && !apiName.isEmpty()) {
            wrapper.like(ApiDefinition::getApiName, apiName);
        }
        wrapper.orderByDesc(ApiDefinition::getCreatedAt);
        return page(new Page<>(page, size), wrapper);
    }

    @Override
    @Transactional
    public ApiDefinition createApiDefinition(ApiDefinition apiDefinition) {
        save(apiDefinition);
        return apiDefinition;
    }

    @Override
    @Transactional
    public ApiDefinition updateApiDefinition(ApiDefinition apiDefinition) {
        updateById(apiDefinition);
        return apiDefinition;
    }

    @Override
    @Transactional
    public void deleteApiDefinition(Long id) {
        removeById(id);
    }

    // ==================== API订阅管理 ====================

    @Override
    public Page<ApiSubscription> getSubscriptionPage(Long tenantId, int page, int size, Long apiId) {
        LambdaQueryWrapper<ApiSubscription> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ApiSubscription::getTenantId, tenantId);
        if (apiId != null) {
            wrapper.eq(ApiSubscription::getApiId, apiId);
        }
        wrapper.orderByDesc(ApiSubscription::getCreatedAt);
        return subscriptionMapper.selectPage(new Page<>(page, size), wrapper);
    }

    @Override
    @Transactional
    public ApiSubscription subscribe(Long tenantId, Long apiId, String subscriberName,
                                      Integer callLimit, LocalDateTime expiresAt) {
        // 检查API定义是否存在且上线
        ApiDefinition apiDefinition = getById(apiId);
        if (apiDefinition == null || apiDefinition.getStatus() != 1) {
            throw new IllegalArgumentException("API不存在或未上线");
        }

        // 检查是否已订阅
        ApiSubscription existing = subscriptionMapper.selectOne(
            new LambdaQueryWrapper<ApiSubscription>()
                .eq(ApiSubscription::getTenantId, tenantId)
                .eq(ApiSubscription::getApiId, apiId)
        );
        if (existing != null) {
            throw new IllegalArgumentException("已订阅该API");
        }

        ApiSubscription subscription = new ApiSubscription();
        subscription.setTenantId(tenantId);
        subscription.setApiId(apiId);
        subscription.setSubscriberName(subscriberName);
        subscription.setApiKey(UUID.randomUUID().toString().replace("-", ""));
        subscription.setCallLimit(callLimit != null ? callLimit : 1000);
        subscription.setCallCount(0);
        subscription.setStatus(1);
        subscription.setExpiresAt(expiresAt);
        subscriptionMapper.insert(subscription);
        return subscription;
    }

    @Override
    @Transactional
    public void unsubscribe(Long subscriptionId, Long tenantId) {
        ApiSubscription subscription = subscriptionMapper.selectById(subscriptionId);
        if (subscription != null && subscription.getTenantId().equals(tenantId)) {
            subscriptionMapper.deleteById(subscriptionId);
        }
    }

    // ==================== API使用记录 ====================

    @Override
    @Transactional
    public ApiUsageRecord recordUsage(Long tenantId, Long apiId, Long subscriptionId,
                                       Integer responseStatus, Integer responseTimeMs) {
        // 更新调用次数
        ApiSubscription subscription = subscriptionMapper.selectById(subscriptionId);
        if (subscription != null) {
            subscription.setCallCount(subscription.getCallCount() + 1);
            subscriptionMapper.updateById(subscription);
        }

        ApiUsageRecord record = new ApiUsageRecord();
        record.setTenantId(tenantId);
        record.setApiId(apiId);
        record.setSubscriptionId(subscriptionId);
        record.setRequestTime(LocalDateTime.now());
        record.setResponseStatus(responseStatus);
        record.setResponseTimeMs(responseTimeMs);
        usageRecordMapper.insert(record);
        return record;
    }

    @Override
    public Page<ApiUsageRecord> getUsageRecordPage(Long tenantId, int page, int size, Long apiId) {
        LambdaQueryWrapper<ApiUsageRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ApiUsageRecord::getTenantId, tenantId);
        if (apiId != null) {
            wrapper.eq(ApiUsageRecord::getApiId, apiId);
        }
        wrapper.orderByDesc(ApiUsageRecord::getCreatedAt);
        return usageRecordMapper.selectPage(new Page<>(page, size), wrapper);
    }

    @Override
    public Map<String, Object> getUsageStats(Long tenantId, Long apiId) {
        LambdaQueryWrapper<ApiUsageRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ApiUsageRecord::getTenantId, tenantId);
        if (apiId != null) {
            wrapper.eq(ApiUsageRecord::getApiId, apiId);
        }
        List<ApiUsageRecord> records = usageRecordMapper.selectList(wrapper);

        long totalCalls = records.size();
        double avgResponseTime = records.stream()
            .mapToInt(r -> r.getResponseTimeMs() != null ? r.getResponseTimeMs() : 0)
            .average()
            .orElse(0.0);
        long errorCount = records.stream()
            .filter(r -> r.getResponseStatus() != null && r.getResponseStatus() >= 400)
            .count();

        // 按API分组统计
        Map<Long, Long> callsByApi = records.stream()
            .collect(Collectors.groupingBy(ApiUsageRecord::getApiId, Collectors.counting()));

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalCalls", totalCalls);
        stats.put("avgResponseTime", avgResponseTime);
        stats.put("errorCount", errorCount);
        stats.put("errorRate", totalCalls > 0 ? (double) errorCount / totalCalls : 0.0);
        stats.put("callsByApi", callsByApi);
        return stats;
    }
}
