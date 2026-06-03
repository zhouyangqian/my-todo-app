package com.example.dict.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.dict.entity.SystemConfig;
import com.example.dict.mapper.SystemConfigMapper;
import com.example.dict.service.ConfigService;
import com.example.common.core.util.CodeGenerateUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 系统配置服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ConfigServiceImpl extends ServiceImpl<SystemConfigMapper, SystemConfig> implements ConfigService {

    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * 配置缓存Key前缀
     */
    private static final String CONFIG_CACHE_KEY = "config:";
    /**
     * 缓存过期时间(小时)
     */
    private static final long CACHE_EXPIRE_HOURS = 24;

    @Override
    public String getConfigValue(String configCode, Long tenantId) {
        String cacheKey = CONFIG_CACHE_KEY + tenantId + ":" + configCode;

        // 先从缓存获取
        Object cachedValue = redisTemplate.opsForValue().get(cacheKey);
        if (cachedValue != null) {
            return cachedValue.toString();
        }

        // 从数据库查询
        SystemConfig config = getOne(
            new LambdaQueryWrapper<SystemConfig>()
                .eq(SystemConfig::getTenantId, tenantId)
                .eq(SystemConfig::getConfigCode, configCode)
                .eq(SystemConfig::getDeleted, 0)
                .eq(SystemConfig::getStatus, 1)
        );

        String value = config != null ? config.getConfigValue() : null;

        // 写入缓存
        if (value != null) {
            redisTemplate.opsForValue().set(cacheKey, value, CACHE_EXPIRE_HOURS, TimeUnit.HOURS);
        }

        return value;
    }

    @Override
    public SystemConfig getConfig(String configCode, Long tenantId) {
        return getOne(
            new LambdaQueryWrapper<SystemConfig>()
                .eq(SystemConfig::getTenantId, tenantId)
                .eq(SystemConfig::getConfigCode, configCode)
                .eq(SystemConfig::getDeleted, 0)
        );
    }

    @Override
    public List<SystemConfig> getAllConfigs(Long tenantId) {
        return list(
            new LambdaQueryWrapper<SystemConfig>()
                .eq(SystemConfig::getTenantId, tenantId)
                .eq(SystemConfig::getDeleted, 0)
                .orderByAsc(SystemConfig::getSort)
        );
    }

    @Override
    public void setConfigValue(String configCode, String configValue, Long tenantId, Long userId) {
        SystemConfig config = getConfig(configCode, tenantId);
        if (config != null) {
            config.setConfigValue(configValue);
            config.setUpdatedBy(userId);
            updateById(config);
            // 清除缓存
            clearConfigCache(configCode, tenantId);
        } else {
            throw new IllegalArgumentException("配置不存在: " + configCode);
        }
    }

    @Override
    public SystemConfig createConfig(SystemConfig config) {
        // 自动生成配置编码（格式：CFG-拼音首字母-时间戳）
        String generatedCode = CodeGenerateUtil.generate("CFG",
                config.getConfigName(),
                code -> getConfig(code, config.getTenantId()) != null
        );
        config.setConfigCode(generatedCode);
        save(config);
        return config;
    }

    @Override
    public void clearConfigCache(String configCode, Long tenantId) {
        String cacheKey = CONFIG_CACHE_KEY + tenantId + ":" + configCode;
        redisTemplate.delete(cacheKey);
        log.info("清除配置缓存: {}", configCode);
    }
}
