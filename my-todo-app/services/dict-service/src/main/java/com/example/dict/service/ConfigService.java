package com.example.dict.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.dict.entity.SystemConfig;

import java.util.List;

/**
 * 系统配置服务接口
 */
public interface ConfigService extends IService<SystemConfig> {

    String getConfigValue(String configCode, Long tenantId);

    SystemConfig getConfig(String configCode, Long tenantId);

    List<SystemConfig> getAllConfigs(Long tenantId);

    void setConfigValue(String configCode, String configValue, Long tenantId, Long userId);

    SystemConfig createConfig(SystemConfig config);

    void clearConfigCache(String configCode, Long tenantId);
}
