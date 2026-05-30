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
import java.util.List;

/**
 * 加权平均成本核算策略
 * <p>
 * 计算当前库存总成本 / 总数量 = 加权平均单价，再乘以出库数量得出库成本。
 * </p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WeightedAverageCostStrategy implements CostCalculationStrategy {

    private final FinCostHistoryMapper costHistoryMapper;

    @Override
    public BigDecimal calculateOutboundCost(Long tenantId, Long productId, BigDecimal quantity) {
        List<FinCostHistory> records = costHistoryMapper.selectList(
                new LambdaQueryWrapper<FinCostHistory>()
                        .eq(FinCostHistory::getTenantId, tenantId)
                        .eq(FinCostHistory::getProductId, productId)
                        .eq(FinCostHistory::getBizType, 1)); // 采购入库

        if (records.isEmpty()) {
            throw new BusinessException("无库存成本记录，无法计算加权平均成本");
        }

        BigDecimal totalQuantity = records.stream()
                .map(FinCostHistory::getQuantity)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalCost = records.stream()
                .map(FinCostHistory::getTotalCost)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (totalQuantity.compareTo(BigDecimal.ZERO) == 0) {
            throw new BusinessException("库存数量为零，无法计算加权平均成本");
        }

        // 加权平均单价 = 总成本 / 总数量
        BigDecimal avgCost = totalCost.divide(totalQuantity, 4, RoundingMode.HALF_UP);
        BigDecimal result = avgCost.multiply(quantity).setScale(2, RoundingMode.HALF_UP);

        log.debug("加权平均成本计算: tenantId={}, productId={}, avgCost={}, quantity={}, result={}",
                tenantId, productId, avgCost, quantity, result);
        return result;
    }

    @Override
    public String getType() {
        return "WEIGHTED_AVERAGE";
    }
}
