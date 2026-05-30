package com.example.dict.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.dict.entity.TraceAlert;
import com.example.dict.entity.TraceConfig;

/**
 * 分布式追踪配置服务接口
 */
public interface TracingConfigService extends IService<TraceConfig> {

    Page<TraceConfig> getTraceConfigPage(Long tenantId, int page, int size);

    TraceConfig createTraceConfig(TraceConfig config);

    TraceConfig updateTraceConfig(TraceConfig config);

    void deleteTraceConfig(Long id);

    Page<TraceAlert> getTraceAlertPage(Long tenantId, int page, int size);

    TraceAlert createTraceAlert(TraceAlert alert);

    TraceAlert updateTraceAlert(TraceAlert alert);

    void deleteTraceAlert(Long id);
}
