package com.example.finance.strategy;

import java.math.BigDecimal;

/**
 * 成本核算策略接口
 * <p>
 * 定义成本核算的统一行为，支持多种成本核算方法：
 * FIFO（先进先出）、加权平均、个别计价
 * </p>
 */
public interface CostCalculationStrategy {

    /**
     * 计算出库成本
     *
     * @param tenantId  租户ID
     * @param productId 商品ID
     * @param quantity  出库数量
     * @return 出库总成本
     */
    BigDecimal calculateOutboundCost(Long tenantId, Long productId, BigDecimal quantity);

    /**
     * 获取策略类型标识
     *
     * @return 策略类型字符串
     */
    String getType();
}
