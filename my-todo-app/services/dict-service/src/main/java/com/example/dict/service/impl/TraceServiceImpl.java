package com.example.dict.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.dict.entity.TraceAlert;
import com.example.dict.entity.TraceConfig;
import com.example.dict.mapper.TraceAlertMapper;
import com.example.dict.mapper.TraceConfigMapper;
import com.example.dict.service.TraceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

/**
 * 分布式追踪服务实现
 */
@Service
@RequiredArgsConstructor
public class TraceServiceImpl extends ServiceImpl<TraceConfigMapper, TraceConfig> implements TraceService {

    private final TraceAlertMapper traceAlertMapper;

    // ==================== 追踪配置管理 ====================

    @Override
    public Page<TraceConfig> getTraceConfigPage(Long tenantId, int page, int size, String serviceName) {
        LambdaQueryWrapper<TraceConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TraceConfig::getTenantId, tenantId);
        if (serviceName != null && !serviceName.isEmpty()) {
            wrapper.like(TraceConfig::getServiceName, serviceName);
        }
        wrapper.orderByDesc(TraceConfig::getCreatedAt);
        return page(new Page<>(page, size), wrapper);
    }

    @Override
    @Transactional
    public TraceConfig createTraceConfig(TraceConfig traceConfig) {
        // 检查同服务名是否已存在
        TraceConfig existing = getOne(
            new LambdaQueryWrapper<TraceConfig>()
                .eq(TraceConfig::getTenantId, traceConfig.getTenantId())
                .eq(TraceConfig::getServiceName, traceConfig.getServiceName())
        );
        if (existing != null) {
            throw new IllegalArgumentException("该服务的追踪配置已存在: " + traceConfig.getServiceName());
        }
        save(traceConfig);
        return traceConfig;
    }

    @Override
    @Transactional
    public TraceConfig updateTraceConfig(TraceConfig traceConfig) {
        updateById(traceConfig);
        return traceConfig;
    }

    @Override
    @Transactional
    public void deleteTraceConfig(Long id) {
        removeById(id);
    }

    @Override
    @Transactional
    public TraceConfig toggleTraceConfig(Long id, Integer enabled) {
        TraceConfig config = getById(id);
        if (config != null) {
            config.setEnabled(enabled);
            updateById(config);
        }
        return config;
    }

    // ==================== 追踪告警管理 ====================

    @Override
    public Page<TraceAlert> getTraceAlertPage(Long tenantId, int page, int size, String serviceName) {
        LambdaQueryWrapper<TraceAlert> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TraceAlert::getTenantId, tenantId);
        if (serviceName != null && !serviceName.isEmpty()) {
            wrapper.like(TraceAlert::getServiceName, serviceName);
        }
        wrapper.orderByDesc(TraceAlert::getCreatedAt);
        return traceAlertMapper.selectPage(new Page<>(page, size), wrapper);
    }

    @Override
    public TraceAlert getTraceAlert(Long id) {
        return traceAlertMapper.selectById(id);
    }

    @Override
    @Transactional
    public TraceAlert createTraceAlert(TraceAlert traceAlert) {
        traceAlertMapper.insert(traceAlert);
        return traceAlert;
    }

    @Override
    @Transactional
    public TraceAlert updateTraceAlert(TraceAlert traceAlert) {
        traceAlertMapper.updateById(traceAlert);
        return traceAlert;
    }

    @Override
    @Transactional
    public void deleteTraceAlert(Long id) {
        traceAlertMapper.deleteById(id);
    }

    @Override
    @Transactional
    public TraceAlert toggleTraceAlert(Long id, Integer enabled) {
        TraceAlert alert = traceAlertMapper.selectById(id);
        if (alert != null) {
            alert.setEnabled(enabled);
            traceAlertMapper.updateById(alert);
        }
        return alert;
    }

    @Override
    @Transactional
    public TraceAlert acknowledgeAlert(Long id) {
        TraceAlert alert = traceAlertMapper.selectById(id);
        if (alert != null) {
            alert.setEnabled(0);
            traceAlertMapper.updateById(alert);
        }
        return alert;
    }

    @Override
    public Map<String, Object> getAlertStats(Long tenantId) {
        LambdaQueryWrapper<TraceAlert> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TraceAlert::getTenantId, tenantId);
        Long totalCount = traceAlertMapper.selectCount(wrapper);

        LambdaQueryWrapper<TraceAlert> enabledWrapper = new LambdaQueryWrapper<>();
        enabledWrapper.eq(TraceAlert::getTenantId, tenantId)
                      .eq(TraceAlert::getEnabled, 1);
        Long enabledCount = traceAlertMapper.selectCount(enabledWrapper);

        return Map.of(
            "totalCount", totalCount,
            "enabledCount", enabledCount,
            "disabledCount", totalCount - enabledCount
        );
    }
}
