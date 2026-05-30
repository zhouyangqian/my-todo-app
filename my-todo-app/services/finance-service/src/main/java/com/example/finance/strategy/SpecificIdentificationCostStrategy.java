package com.example.finance.strategy;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.common.core.exception.BusinessException;
import com.example.finance.entity.FinCostHistory;
import com.example.finance.mapper.FinCostHistoryMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 个别计价成本核算策略
 * <p>
 * 按指定批次的成本价计算，取最近一次采购入库记录的成本单价。
 * </p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SpecificIdentificationCostStrategy implements CostCalculationStrategy {

    private final FinCostHistoryMapper costHistoryMapper;

    @Override
    public BigDecimal calculateOutboundCost(Long tenantId, Long productId, BigDecimal quantity) {
        // 取最近一次采购入库的成本记录作为指定批次
        FinCostHistory latestRecord = costHistoryMapper.selectOne(
                new LambdaQueryWrapper<FinCostHistory>()
                        .eq(FinCostHistory::getTenantId, tenantId)
                        .eq(FinCostHistory::getProductId, productId)
                        .eq(FinCostHistory::getBizType, 1) // 采购入库
                        .orderByDesc(FinCostHistory::getCreatedAt)
                        .last("LIMIT 1"));

        if (latestRecord == null) {
            throw new BusinessException("无采购成本记录，无法进行个别计价");
        }

        BigDecimal result = latestRecord.getCostPrice().multiply(quantity).setScale(2, RoundingMode.HALF_UP);
        log.debug("个别计价成本计算: tenantId={}, productId={}, costPrice={}, quantity={}, result={}",
                tenantId, productId, latestRecord.getCostPrice(), quantity, result);
        return result;
    }

    @Override
    public String getType() {
        return "SPECIFIC";
    }
}
