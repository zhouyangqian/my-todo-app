package com.example.dict.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.dict.entity.ThirdPartyApi;
import com.example.dict.entity.ThirdPartyCallLog;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 第三方API服务接口
 */
public interface ThirdPartyApiService extends IService<ThirdPartyApi> {

    Page<ThirdPartyApi> getThirdPartyApiPage(Long tenantId, int page, int size, String apiName);

    ThirdPartyApi createThirdPartyApi(ThirdPartyApi api);

    ThirdPartyApi updateThirdPartyApi(ThirdPartyApi api);

    void deleteThirdPartyApi(Long id, Long tenantId);

    String generateApiKey(Long id, Long tenantId);

    void toggleApiStatus(Long id, Long tenantId);

    Map<String, Object> healthCheck(Long id, Long tenantId);

    Page<ThirdPartyCallLog> getCallLogPage(Long tenantId, Long apiId, int page, int size);

    void recordCallLog(Long tenantId, Long apiId, LocalDateTime requestTime,
                       Integer responseStatus, Integer responseTimeMs, String errorMessage);
}
