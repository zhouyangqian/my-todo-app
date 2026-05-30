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
 * FIFO（先进先出）成本核算策略
 * <p>
 * 从 fin_cost_history 按入库时间排序（先进先出），逐批扣减数量计算加权成本。
 * </p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FifoCostStrategy implements CostCalculationStrategy {

    private final FinCostHistoryMapper costHistoryMapper;

    @Override
    public BigDecimal calculateOutboundCost(Long tenantId, Long productId, BigDecimal quantity) {
        // 查询所有采购入库记录，按入库时间升序排列（先进先出）
        List<FinCostHistory> inboundRecords = costHistoryMapper.selectList(
                new LambdaQueryWrapper<FinCostHistory>()
                        .eq(FinCostHistory::getTenantId, tenantId)
                        .eq(FinCostHistory::getProductId, productId)
                        .eq(FinCostHistory::getBizType, 1) // 采购入库
                        .orderByAsc(FinCostHistory::getCreatedAt));

        BigDecimal remaining = quantity;
        BigDecimal totalCost = BigDecimal.ZERO;

        for (FinCostHistory record : inboundRecords) {
            if (remaining.compareTo(BigDecimal.ZERO) <= 0) {
                break;
            }
            BigDecimal available = record.getQuantity();
            BigDecimal used = remaining.min(available);
            totalCost = totalCost.add(used.multiply(record.getCostPrice()));
            remaining = remaining.subtract(used);
        }

        if (remaining.compareTo(BigDecimal.ZERO) > 0) {
            throw new BusinessException("FIFO成本计算失败: 库存记录不足以覆盖出库数量");
        }

        log.debug("FIFO成本计算: tenantId={}, productId={}, quantity={}, cost={}",
                tenantId, productId, quantity, totalCost);
        return totalCost.setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public String getType() {
        return "FIFO";
    }
}
