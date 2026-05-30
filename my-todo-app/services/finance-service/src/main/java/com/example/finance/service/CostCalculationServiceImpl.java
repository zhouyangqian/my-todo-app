package com.example.finance.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.common.core.exception.BusinessException;
import com.example.finance.entity.FinCostConfig;
import com.example.finance.entity.FinCostHistory;
import com.example.finance.mapper.FinCostConfigMapper;
import com.example.finance.mapper.FinCostHistoryMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 成本核算服务
 * <p>
 * 支持三种成本核算方法：
 * 1. FIFO（先进先出）
 * 2. 加权平均
 * 3. 个别计价
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CostCalculationServiceImpl extends ServiceImpl<FinCostConfigMapper, FinCostConfig> implements CostCalculationService {

    private final FinCostHistoryMapper costHistoryMapper;

    /**
     * 获取商品的成本核算配置
     */
    @Override
    public FinCostConfig getConfig(Long tenantId, Long productId) {
        return getOne(new LambdaQueryWrapper<FinCostConfig>()
                .eq(FinCostConfig::getTenantId, tenantId)
                .eq(FinCostConfig::getProductId, productId)
                .eq(FinCostConfig::getStatus, 1));
    }

    /**
     * 设置商品的成本核算方法
     */
    @Override
    @Transactional
    public FinCostConfig setCostMethod(Long tenantId, Long productId, Integer costMethod, Long userId) {
        if (costMethod < 1 || costMethod > 3) {
            throw new BusinessException("无效的成本核算方法");
        }

        FinCostConfig config = getConfig(tenantId, productId);
        if (config == null) {
            config = new FinCostConfig();
            config.setTenantId(tenantId);
            config.setProductId(productId);
            config.setStatus(1);
            config.setCreatedBy(userId);
            config.setCreatedAt(LocalDateTime.now());
        }
        config.setCostMethod(costMethod);
        config.setUpdatedBy(userId);
        config.setUpdatedAt(LocalDateTime.now());
        saveOrUpdate(config);

        log.info("设置成本核算方法: tenantId={}, productId={}, method={}", tenantId, productId, costMethod);
        return config;
    }

    /**
     * 计算商品出库成本（根据配置的成本方法）
     *
     * @param tenantId   租户ID
     * @param productId  商品ID
     * @param quantity   出库数量
     * @param warehouseId 仓库ID
     * @return 出库总成本
     */
    @Override
    public BigDecimal calculateOutboundCost(Long tenantId, Long productId, BigDecimal quantity, Long warehouseId) {
        FinCostConfig config = getConfig(tenantId, productId);
        int method = config != null ? config.getCostMethod() : METHOD_WEIGHTED_AVERAGE;

        return switch (method) {
            case METHOD_FIFO -> calculateFifoCost(tenantId, productId, quantity);
            case METHOD_SPECIFIC -> calculateSpecificCost(tenantId, productId, quantity);
            default -> calculateWeightedAverageCost(tenantId, productId, quantity);
        };
    }

    /**
     * 记录入库成本
     */
    @Override
    @Transactional
    public void recordInboundCost(Long tenantId, Long productId, Long warehouseId,
                                   BigDecimal quantity, BigDecimal costPrice,
                                   Integer bizType, String bizNo, Long operatorId) {
        FinCostConfig config = getConfig(tenantId, productId);
        int method = config != null ? config.getCostMethod() : METHOD_WEIGHTED_AVERAGE;

        FinCostHistory history = new FinCostHistory();
        history.setTenantId(tenantId);
        history.setProductId(productId);
        history.setWarehouseId(warehouseId);
        history.setCostPrice(costPrice);
        history.setQuantity(quantity);
        history.setTotalCost(costPrice.multiply(quantity));
        history.setBizType(bizType);
        history.setBizNo(bizNo);
        history.setCostMethod(method);
        history.setCreatedBy(operatorId);
        history.setCreatedAt(LocalDateTime.now());
        costHistoryMapper.insert(history);
    }

    /**
     * 分页查询成本历史
     */
    @Override
    public Page<FinCostHistory> getCostHistoryPage(Long tenantId, int page, int size,
                                                     Long productId, Integer bizType) {
        LambdaQueryWrapper<FinCostHistory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FinCostHistory::getTenantId, tenantId)
               .eq(productId != null, FinCostHistory::getProductId, productId)
               .eq(bizType != null, FinCostHistory::getBizType, bizType)
               .orderByDesc(FinCostHistory::getCreatedAt);
        return costHistoryMapper.selectPage(new Page<>(page, size), wrapper);
    }

    /**
     * 分页查询成本配置
     */
    @Override
    public Page<FinCostConfig> getConfigPage(Long tenantId, int page, int size) {
        return page(new Page<>(page, size),
                new LambdaQueryWrapper<FinCostConfig>()
                        .eq(FinCostConfig::getTenantId, tenantId)
                        .orderByDesc(FinCostConfig::getCreatedAt));
    }

    /**
     * FIFO 成本计算：取最早入库的成本记录
     */
    private BigDecimal calculateFifoCost(Long tenantId, Long productId, BigDecimal quantity) {
        List<FinCostHistory> inboundRecords = costHistoryMapper.selectList(
                new LambdaQueryWrapper<FinCostHistory>()
                        .eq(FinCostHistory::getTenantId, tenantId)
                        .eq(FinCostHistory::getProductId, productId)
                        .eq(FinCostHistory::getBizType, 1) // 采购入库
                        .orderByAsc(FinCostHistory::getCreatedAt));

        BigDecimal remaining = quantity;
        BigDecimal totalCost = BigDecimal.ZERO;

        for (FinCostHistory record : inboundRecords) {
            if (remaining.compareTo(BigDecimal.ZERO) <= 0) break;

            BigDecimal available = record.getQuantity();
            BigDecimal used = remaining.min(available);
            totalCost = totalCost.add(used.multiply(record.getCostPrice()));
            remaining = remaining.subtract(used);
        }

        if (remaining.compareTo(BigDecimal.ZERO) > 0) {
            throw new BusinessException("FIFO成本计算失败: 库存记录不足以覆盖出库数量");
        }

        return totalCost.setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * 加权平均成本计算
     */
    private BigDecimal calculateWeightedAverageCost(Long tenantId, Long productId, BigDecimal quantity) {
        List<FinCostHistory> records = costHistoryMapper.selectList(
                new LambdaQueryWrapper<FinCostHistory>()
                        .eq(FinCostHistory::getTenantId, tenantId)
                        .eq(FinCostHistory::getProductId, productId)
                        .eq(FinCostHistory::getBizType, 1));

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

        BigDecimal avgCost = totalCost.divide(totalQuantity, 4, RoundingMode.HALF_UP);
        return avgCost.multiply(quantity).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * 个别计价法：使用最近的采购成本
     */
    private BigDecimal calculateSpecificCost(Long tenantId, Long productId, BigDecimal quantity) {
        FinCostHistory latestRecord = costHistoryMapper.selectOne(
                new LambdaQueryWrapper<FinCostHistory>()
                        .eq(FinCostHistory::getTenantId, tenantId)
                        .eq(FinCostHistory::getProductId, productId)
                        .eq(FinCostHistory::getBizType, 1)
                        .orderByDesc(FinCostHistory::getCreatedAt)
                        .last("LIMIT 1"));

        if (latestRecord == null) {
            throw new BusinessException("无采购成本记录");
        }

        return latestRecord.getCostPrice().multiply(quantity).setScale(2, RoundingMode.HALF_UP);
    }
}
