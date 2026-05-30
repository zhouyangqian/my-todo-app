package com.example.erp.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.erp.entity.ErpConfig;

import java.util.List;
import java.util.Map;

/**
 * 系统配置服务接口
 */
public interface ErpConfigService extends IService<ErpConfig> {

    Page<ErpConfig> getConfigPage(Long tenantId, int page, int size,
                                   String configType, String configName);

    List<ErpConfig> getConfigsByType(Long tenantId, String configType);

    String getConfigValue(Long tenantId, String configKey);

    Map<String, String> getConfigMap(Long tenantId, String configType);

    ErpConfig createConfig(ErpConfig config);

    ErpConfig updateConfig(ErpConfig config);

    void deleteConfig(Long id);

    void batchUpdateConfigs(Long tenantId, Map<String, String> configs, String configType, Long userId);
}
