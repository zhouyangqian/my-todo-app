package com.example.finance.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.finance.entity.FinCostConfig;
import com.example.finance.entity.FinCostHistory;

import java.math.BigDecimal;

/**
 * 成本核算服务接口
 */
public interface CostCalculationService extends IService<FinCostConfig> {

    /** 成本方法常量 */
    int METHOD_FIFO = 1;
    int METHOD_WEIGHTED_AVERAGE = 2;
    int METHOD_SPECIFIC = 3;

    FinCostConfig getConfig(Long tenantId, Long productId);

    FinCostConfig setCostMethod(Long tenantId, Long productId, Integer costMethod, Long userId);

    BigDecimal calculateOutboundCost(Long tenantId, Long productId, BigDecimal quantity, Long warehouseId);

    void recordInboundCost(Long tenantId, Long productId, Long warehouseId,
                           BigDecimal quantity, BigDecimal costPrice,
                           Integer bizType, String bizNo, Long operatorId);

    Page<FinCostHistory> getCostHistoryPage(Long tenantId, int page, int size,
                                             Long productId, Integer bizType);

    Page<FinCostConfig> getConfigPage(Long tenantId, int page, int size);
}
