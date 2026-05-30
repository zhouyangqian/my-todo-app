package com.example.dict.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.dict.entity.TraceAlert;
import com.example.dict.entity.TraceConfig;
import com.example.dict.mapper.TraceAlertMapper;
import com.example.dict.mapper.TraceConfigMapper;
import com.example.dict.service.TracingConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 分布式追踪配置服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TracingConfigServiceImpl extends ServiceImpl<TraceConfigMapper, TraceConfig> implements TracingConfigService {

    private final TraceAlertMapper traceAlertMapper;

    @Override
    public Page<TraceConfig> getTraceConfigPage(Long tenantId, int page, int size) {
        LambdaQueryWrapper<TraceConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TraceConfig::getTenantId, tenantId)
               .orderByDesc(TraceConfig::getCreatedAt);
        return page(new Page<>(page, size), wrapper);
    }

    @Override
    @Transactional
    public TraceConfig createTraceConfig(TraceConfig config) {
        save(config);
        return config;
    }

    @Override
    @Transactional
    public TraceConfig updateTraceConfig(TraceConfig config) {
        updateById(config);
        return config;
    }

    @Override
    @Transactional
    public void deleteTraceConfig(Long id) {
        removeById(id);
    }

    @Override
    public Page<TraceAlert> getTraceAlertPage(Long tenantId, int page, int size) {
        LambdaQueryWrapper<TraceAlert> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TraceAlert::getTenantId, tenantId)
               .orderByDesc(TraceAlert::getCreatedAt);
        return traceAlertMapper.selectPage(new Page<>(page, size), wrapper);
    }

    @Override
    @Transactional
    public TraceAlert createTraceAlert(TraceAlert alert) {
        traceAlertMapper.insert(alert);
        return alert;
    }

    @Override
    @Transactional
    public TraceAlert updateTraceAlert(TraceAlert alert) {
        traceAlertMapper.updateById(alert);
        return alert;
    }

    @Override
    @Transactional
    public void deleteTraceAlert(Long id) {
        traceAlertMapper.deleteById(id);
    }
}
