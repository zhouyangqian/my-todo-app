package com.example.finance.strategy;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.common.core.exception.BusinessException;
import com.example.finance.entity.FinCostConfig;
import com.example.finance.mapper.FinCostConfigMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 成本核算策略工厂
 * <p>
 * 注入所有 CostCalculationStrategy 实现，根据配置或指定类型返回对应策略。
 * 查询 fin_cost_config 获取产品的成本核算方法。
 * </p>
 */
@Slf4j
@Component
public class CostStrategyFactory {

    private final FinCostConfigMapper costConfigMapper;

    /** 策略类型与 costMethod 数值的映射 */
    private static final Map<String, Integer> TYPE_TO_METHOD = Map.of(
            "FIFO", 1,
            "WEIGHTED_AVERAGE", 2,
            "SPECIFIC", 3
    );

    private static final Map<Integer, String> METHOD_TO_TYPE = Map.of(
            1, "FIFO",
            2, "WEIGHTED_AVERAGE",
            3, "SPECIFIC"
    );

    private Map<String, CostCalculationStrategy> strategyMap;

    /**
     * 通过构造器注入所有策略实现，并构建映射表
     */
    public CostStrategyFactory(FinCostConfigMapper costConfigMapper, List<CostCalculationStrategy> strategies) {
        this.costConfigMapper = costConfigMapper;
        this.strategyMap = strategies.stream()
                .collect(Collectors.toMap(CostCalculationStrategy::getType, Function.identity()));
        log.info("加载成本核算策略: {}", strategyMap.keySet());
    }

    /**
     * 根据策略类型字符串获取策略
     *
     * @param type 策略类型: FIFO / WEIGHTED_AVERAGE / SPECIFIC
     * @return 对应的成本核算策略
     */
    public CostCalculationStrategy getStrategy(String type) {
        CostCalculationStrategy strategy = strategyMap.get(type);
        if (strategy == null) {
            throw new BusinessException("未知的成本核算策略类型: " + type);
        }
        return strategy;
    }

    /**
     * 根据产品的成本配置获取对应策略
     * <p>
     * 查询 fin_cost_config 获取产品配置的成本方法，未配置则默认使用加权平均。
     * </p>
     *
     * @param tenantId  租户ID
     * @param productId 商品ID
     * @return 对应的成本核算策略
     */
    public CostCalculationStrategy getStrategyByProduct(Long tenantId, Long productId) {
        FinCostConfig config = costConfigMapper.selectOne(
                new LambdaQueryWrapper<FinCostConfig>()
                        .eq(FinCostConfig::getTenantId, tenantId)
                        .eq(FinCostConfig::getProductId, productId)
                        .eq(FinCostConfig::getStatus, 1));

        int method = (config != null) ? config.getCostMethod() : 2; // 默认加权平均
        String type = METHOD_TO_TYPE.getOrDefault(method, "WEIGHTED_AVERAGE");
        return getStrategy(type);
    }
}
