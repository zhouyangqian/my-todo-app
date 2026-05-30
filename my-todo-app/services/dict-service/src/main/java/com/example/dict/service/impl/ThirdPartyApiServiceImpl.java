package com.example.dict.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.dict.entity.ThirdPartyApi;
import com.example.dict.entity.ThirdPartyCallLog;
import com.example.dict.mapper.ThirdPartyApiMapper;
import com.example.dict.mapper.ThirdPartyCallLogMapper;
import com.example.dict.service.ThirdPartyApiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 第三方API服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ThirdPartyApiServiceImpl extends ServiceImpl<ThirdPartyApiMapper, ThirdPartyApi> implements ThirdPartyApiService {

    private final ThirdPartyCallLogMapper callLogMapper;

    @Override
    public Page<ThirdPartyApi> getThirdPartyApiPage(Long tenantId, int page, int size, String apiName) {
        LambdaQueryWrapper<ThirdPartyApi> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ThirdPartyApi::getTenantId, tenantId);
        if (apiName != null && !apiName.isEmpty()) {
            wrapper.like(ThirdPartyApi::getApiName, apiName);
        }
        wrapper.orderByDesc(ThirdPartyApi::getCreatedAt);
        return page(new Page<>(page, size), wrapper);
    }

    @Override
    @Transactional
    public ThirdPartyApi createThirdPartyApi(ThirdPartyApi api) {
        save(api);
        log.info("创建第三方API: id={}, name={}", api.getId(), api.getApiName());
        return api;
    }

    @Override
    @Transactional
    public ThirdPartyApi updateThirdPartyApi(ThirdPartyApi api) {
        updateById(api);
        log.info("更新第三方API: id={}", api.getId());
        return api;
    }

    @Override
    @Transactional
    public void deleteThirdPartyApi(Long id, Long tenantId) {
        ThirdPartyApi api = getById(id);
        if (api != null) {
            removeById(id);
            log.info("删除第三方API: id={}, name={}", id, api.getApiName());
        }
    }

    @Override
    @Transactional
    public String generateApiKey(Long id, Long tenantId) {
        ThirdPartyApi api = getById(id);
        if (api == null) {
            throw new IllegalArgumentException("API不存在: id=" + id);
        }
        if (!api.getTenantId().equals(tenantId)) {
            throw new IllegalArgumentException("无权操作该API");
        }

        String apiKey = "tpk_" + UUID.randomUUID().toString().replace("-", "");
        api.setApiKeyValue(apiKey);
        if (api.getApiKeyHeader() == null || api.getApiKeyHeader().isEmpty()) {
            api.setApiKeyHeader("X-API-Key");
        }
        api.setAuthType("API_KEY");
        updateById(api);
        log.info("生成API Key: apiId={}", id);
        return apiKey;
    }

    @Override
    @Transactional
    public void toggleApiStatus(Long id, Long tenantId) {
        ThirdPartyApi api = getById(id);
        if (api == null) {
            throw new IllegalArgumentException("API不存在: id=" + id);
        }
        if (!api.getTenantId().equals(tenantId)) {
            throw new IllegalArgumentException("无权操作该API");
        }
        api.setStatus(api.getStatus() == 1 ? 0 : 1);
        updateById(api);
        log.info("切换API状态: id={}, status={}", id, api.getStatus());
    }

    @Override
    public Map<String, Object> healthCheck(Long id, Long tenantId) {
        ThirdPartyApi api = getById(id);
        if (api == null) {
            throw new IllegalArgumentException("API不存在: id=" + id);
        }
        if (!api.getTenantId().equals(tenantId)) {
            throw new IllegalArgumentException("无权操作该API");
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("apiId", id);
        result.put("apiName", api.getApiName());
        result.put("apiUrl", api.getApiUrl());

        long startTime = System.currentTimeMillis();
        int statusCode = 0;
        String errorMessage = null;

        try {
            RestTemplate restTemplate = new RestTemplate();
            var responseEntity = restTemplate.getForEntity(api.getApiUrl(), String.class);
            statusCode = responseEntity.getStatusCode().value();
        } catch (Exception e) {
            statusCode = 0;
            errorMessage = e.getMessage();
            if (errorMessage != null && errorMessage.length() > 500) {
                errorMessage = errorMessage.substring(0, 500);
            }
            log.warn("健康检查失败: apiId={}, error={}", id, errorMessage);
        }

        long responseTimeMs = System.currentTimeMillis() - startTime;
        result.put("responseStatus", statusCode);
        result.put("responseTimeMs", responseTimeMs);
        result.put("healthy", statusCode >= 200 && statusCode < 300);
        if (errorMessage != null) {
            result.put("errorMessage", errorMessage);
        }

        // 记录调用日志
        recordCallLog(tenantId, id, LocalDateTime.now(), statusCode, (int) responseTimeMs, errorMessage);

        return result;
    }

    @Override
    public Page<ThirdPartyCallLog> getCallLogPage(Long tenantId, Long apiId, int page, int size) {
        LambdaQueryWrapper<ThirdPartyCallLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ThirdPartyCallLog::getTenantId, tenantId);
        if (apiId != null) {
            wrapper.eq(ThirdPartyCallLog::getApiId, apiId);
        }
        wrapper.orderByDesc(ThirdPartyCallLog::getRequestTime);
        return callLogMapper.selectPage(new Page<>(page, size), wrapper);
    }

    @Override
    public void recordCallLog(Long tenantId, Long apiId, LocalDateTime requestTime,
                              Integer responseStatus, Integer responseTimeMs, String errorMessage) {
        ThirdPartyCallLog callLog = new ThirdPartyCallLog();
        callLog.setTenantId(tenantId);
        callLog.setApiId(apiId);
        callLog.setRequestTime(requestTime);
        callLog.setResponseStatus(responseStatus);
        callLog.setResponseTimeMs(responseTimeMs);
        callLog.setErrorMessage(errorMessage);
        callLogMapper.insert(callLog);
    }
}
