package com.example.dict.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.dict.entity.TraceAlert;
import com.example.dict.entity.TraceConfig;

import java.util.Map;

/**
 * 分布式追踪服务接口
 */
public interface TraceService extends IService<TraceConfig> {

    Page<TraceConfig> getTraceConfigPage(Long tenantId, int page, int size, String serviceName);

    TraceConfig createTraceConfig(TraceConfig traceConfig);

    TraceConfig updateTraceConfig(TraceConfig traceConfig);

    void deleteTraceConfig(Long id);

    TraceConfig toggleTraceConfig(Long id, Integer enabled);

    Page<TraceAlert> getTraceAlertPage(Long tenantId, int page, int size, String serviceName);

    TraceAlert getTraceAlert(Long id);

    TraceAlert createTraceAlert(TraceAlert traceAlert);

    TraceAlert updateTraceAlert(TraceAlert traceAlert);

    void deleteTraceAlert(Long id);

    TraceAlert toggleTraceAlert(Long id, Integer enabled);

    TraceAlert acknowledgeAlert(Long id);

    Map<String, Object> getAlertStats(Long tenantId);
}
