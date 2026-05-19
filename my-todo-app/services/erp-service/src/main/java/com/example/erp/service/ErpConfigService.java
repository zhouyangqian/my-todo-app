package com.example.erp.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.erp.entity.ErpConfig;
import com.example.erp.mapper.ErpConfigMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 系统配置服务类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ErpConfigService extends ServiceImpl<ErpConfigMapper, ErpConfig> {

    /**
     * 分页查询配置
     */
    public Page<ErpConfig> getConfigPage(Long tenantId, int page, int size,
                                          String configType, String configName) {
        LambdaQueryWrapper<ErpConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ErpConfig::getTenantId, tenantId);
        if (configType != null && !configType.isEmpty()) {
            wrapper.eq(ErpConfig::getConfigType, configType);
        }
        if (configName != null && !configName.isEmpty()) {
            wrapper.like(ErpConfig::getConfigName, configName);
        }
        wrapper.orderByAsc(ErpConfig::getConfigType);
        return page(new Page<>(page, size), wrapper);
    }

    /**
     * 根据类型获取配置列表
     */
    public List<ErpConfig> getConfigsByType(Long tenantId, String configType) {
        return list(
            new LambdaQueryWrapper<ErpConfig>()
                .eq(ErpConfig::getTenantId, tenantId)
                .eq(ErpConfig::getConfigType, configType)
                .eq(ErpConfig::getDeleted, 0)
                .orderByAsc(ErpConfig::getConfigKey)
        );
    }

    /**
     * 根据Key获取配置值
     */
    public String getConfigValue(Long tenantId, String configKey) {
        ErpConfig config = getOne(
            new LambdaQueryWrapper<ErpConfig>()
                .eq(ErpConfig::getTenantId, tenantId)
                .eq(ErpConfig::getConfigKey, configKey)
                .eq(ErpConfig::getDeleted, 0)
        );
        return config != null ? config.getConfigValue() : null;
    }

    /**
     * 获取配置Map（key -> value）
     */
    public Map<String, String> getConfigMap(Long tenantId, String configType) {
        List<ErpConfig> configs = getConfigsByType(tenantId, configType);
        return configs.stream()
            .collect(Collectors.toMap(ErpConfig::getConfigKey, ErpConfig::getConfigValue, (a, b) -> b));
    }

    /**
     * 创建配置
     */
    @Transactional
    public ErpConfig createConfig(ErpConfig config) {
        // 校验key唯一性
        ErpConfig existing = getOne(
            new LambdaQueryWrapper<ErpConfig>()
                .eq(ErpConfig::getConfigKey, config.getConfigKey())
                .eq(ErpConfig::getTenantId, config.getTenantId())
                .eq(ErpConfig::getDeleted, 0)
        );
        if (existing != null) {
            throw new IllegalArgumentException("配置键已存在: " + config.getConfigKey());
        }
        save(config);
        log.info("创建系统配置: {}={}", config.getConfigKey(), config.getConfigValue());
        return config;
    }

    /**
     * 更新配置
     */
    @Transactional
    public ErpConfig updateConfig(ErpConfig config) {
        updateById(config);
        log.info("更新系统配置: {}={}", config.getConfigKey(), config.getConfigValue());
        return config;
    }

    /**
     * 删除配置
     */
    @Transactional
    public void deleteConfig(Long id) {
        ErpConfig config = getById(id);
        if (config != null) {
            config.setDeleted(1);
            updateById(config);
            log.info("删除系统配置: {}", config.getConfigKey());
        }
    }

    /**
     * 批量更新配置
     */
    @Transactional
    public void batchUpdateConfigs(Long tenantId, Map<String, String> configs, String configType, Long userId) {
        for (Map.Entry<String, String> entry : configs.entrySet()) {
            ErpConfig existing = getOne(
                new LambdaQueryWrapper<ErpConfig>()
                    .eq(ErpConfig::getConfigKey, entry.getKey())
                    .eq(ErpConfig::getTenantId, tenantId)
                    .eq(ErpConfig::getDeleted, 0)
            );
            if (existing != null) {
                existing.setConfigValue(entry.getValue());
                existing.setUpdatedBy(userId);
                updateById(existing);
            } else {
                ErpConfig newConfig = new ErpConfig();
                newConfig.setTenantId(tenantId);
                newConfig.setConfigKey(entry.getKey());
                newConfig.setConfigValue(entry.getValue());
                newConfig.setConfigName(entry.getKey());
                newConfig.setConfigType(configType);
                newConfig.setCreatedBy(userId);
                save(newConfig);
            }
        }
        log.info("批量更新系统配置: type={}, count={}", configType, configs.size());
    }
}
