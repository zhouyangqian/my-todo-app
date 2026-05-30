package com.example.finance.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.common.core.exception.BusinessException;
import com.example.finance.dto.BudgetDTO;
import com.example.finance.entity.FinBudget;
import com.example.finance.mapper.FinBudgetMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 预算管理服务
 * <p>
 * 提供预算的创建、审批、修改、查询、控制检查等功能。
 * 控制级别: FORCE(强制阻止超额) / WARN(警告但不阻止) / LOG(仅记录)
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BudgetServiceImpl extends ServiceImpl<FinBudgetMapper, FinBudget> implements BudgetService {

    /**
     * 创建预算
     */
    @Override
    @Transactional
    public FinBudget createBudget(FinBudget budget) {
        // 校验期间重叠
        validatePeriodOverlap(budget);

        budget.setUsedAmount(BigDecimal.ZERO);
        budget.setFrozenAmount(BigDecimal.ZERO);
        budget.setRemainingAmount(budget.getBudgetAmount());
        if (budget.getControlLevel() == null) {
            budget.setControlLevel("WARN");
        }
        if (budget.getWarningThreshold() == null) {
            budget.setWarningThreshold(new BigDecimal("80.00"));
        }
        budget.setStatus(0); // 草稿
        budget.setCreatedAt(LocalDateTime.now());
        budget.setUpdatedAt(LocalDateTime.now());
        save(budget);

        log.info("创建预算: id={}, name={}, amount={}", budget.getId(), budget.getBudgetName(), budget.getBudgetAmount());
        return budget;
    }

    /**
     * 审批预算
     *
     * @param id 预算ID
     */
    @Override
    @Transactional
    public void approveBudget(Long id) {
        FinBudget budget = getById(id);
        if (budget == null) {
            throw new BusinessException("预算不存在");
        }
        if (budget.getStatus() != 0) {
            throw new BusinessException("只有草稿状态的预算才能审批");
        }

        budget.setStatus(1); // 已审批
        budget.setUpdatedAt(LocalDateTime.now());
        updateById(budget);

        log.info("预算审批通过: id={}", id);
    }

    /**
     * 更新预算
     */
    @Override
    @Transactional
    public FinBudget updateBudget(Long id, BudgetDTO dto) {
        FinBudget budget = getById(id);
        if (budget == null) {
            throw new BusinessException("预算不存在");
        }
        if (budget.getStatus() == 3) {
            throw new BusinessException("已结束的预算不能修改");
        }

        budget.setBudgetName(dto.getBudgetName());
        budget.setBudgetType(dto.getBudgetType());
        budget.setTargetId(dto.getTargetId());
        budget.setPeriodType(dto.getPeriodType());
        budget.setPeriodStart(dto.getPeriodStart());
        budget.setPeriodEnd(dto.getPeriodEnd());
        budget.setBudgetAmount(dto.getBudgetAmount());
        if (dto.getControlLevel() != null) {
            budget.setControlLevel(dto.getControlLevel());
        }
        if (dto.getWarningThreshold() != null) {
            budget.setWarningThreshold(dto.getWarningThreshold());
        }
        // 重新计算剩余金额
        budget.setRemainingAmount(budget.getBudgetAmount()
                .subtract(budget.getUsedAmount())
                .subtract(budget.getFrozenAmount()));
        budget.setUpdatedAt(LocalDateTime.now());
        updateById(budget);

        log.info("更新预算: id={}", id);
        return budget;
    }

    /**
     * 分页查询预算
     *
     * @param tenantId    租户ID
     * @param page        页码
     * @param size        每页大小
     * @param budgetType  预算类型（可选）
     * @param status      状态（可选）
     * @return 分页结果
     */
    @Override
    public Page<FinBudget> getBudgetPage(Long tenantId, int page, int size, String budgetType, Integer status) {
        LambdaQueryWrapper<FinBudget> wrapper = new LambdaQueryWrapper<FinBudget>()
                .eq(FinBudget::getTenantId, tenantId)
                .eq(budgetType != null, FinBudget::getBudgetType, budgetType)
                .eq(status != null, FinBudget::getStatus, status)
                .orderByDesc(FinBudget::getCreatedAt);
        return page(new Page<>(page, size), wrapper);
    }

    /**
     * 检查预算是否充足
     * <p>
     * 根据控制级别执行不同行为：
     * - FORCE: 超预算抛异常阻止
     * - WARN: 超预算记录警告但不阻止
     * - LOG: 仅记录
     * </p>
     *
     * @param tenantId 租户ID
     * @param targetId 目标ID（部门/项目）
     * @param amount   拟使用金额
     * @return 检查结果信息
     */
    @Override
    public Map<String, Object> checkBudget(Long tenantId, Long targetId, BigDecimal amount) {
        Map<String, Object> result = new HashMap<>();
        result.put("allowed", true);
        result.put("message", "");

        // 查找该目标的有效预算
        FinBudget budget = findActiveBudget(tenantId, targetId);
        if (budget == null) {
            result.put("message", "未找到有效的预算配置");
            return result;
        }

        BigDecimal remaining = budget.getRemainingAmount();
        BigDecimal afterUse = remaining.subtract(amount);
        BigDecimal usageRate = budget.getUsedAmount().add(amount)
                .divide(budget.getBudgetAmount(), 4, RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100"));

        result.put("budgetId", budget.getId());
        result.put("budgetAmount", budget.getBudgetAmount());
        result.put("usedAmount", budget.getUsedAmount());
        result.put("remainingAmount", remaining);
        result.put("usageRate", usageRate);

        // 检查警告阈值
        if (usageRate.compareTo(budget.getWarningThreshold()) >= 0 && afterUse.compareTo(BigDecimal.ZERO) >= 0) {
            result.put("message", String.format("预算使用率已达 %.1f%%，接近或超过警告阈值 %.1f%%",
                    usageRate, budget.getWarningThreshold()));
        }

        // 检查是否超预算
        if (afterUse.compareTo(BigDecimal.ZERO) < 0) {
            String warningMsg = String.format("超出预算: 剩余 %.2f, 拟使用 %.2f", remaining, amount);

            switch (budget.getControlLevel()) {
                case "FORCE":
                    result.put("allowed", false);
                    result.put("message", warningMsg + "（强制控制，操作被阻止）");
                    throw new BusinessException(warningMsg + "（强制控制级别，不允许超额使用）");
                case "WARN":
                    result.put("message", warningMsg + "（警告级别，请注意控制）");
                    log.warn("预算超支警告: budgetId={}, {}", budget.getId(), warningMsg);
                    break;
                case "LOG":
                    result.put("message", warningMsg + "（仅记录）");
                    log.info("预算超支记录: budgetId={}, {}", budget.getId(), warningMsg);
                    break;
            }
        }

        return result;
    }

    /**
     * 记录预算使用
     *
     * @param budgetId 预算ID
     * @param amount   使用金额
     */
    @Override
    @Transactional
    public void recordUsage(Long budgetId, BigDecimal amount) {
        FinBudget budget = getById(budgetId);
        if (budget == null) {
            throw new BusinessException("预算不存在");
        }
        if (budget.getStatus() < 1) {
            throw new BusinessException("预算尚未审批，不能记录使用");
        }

        budget.setUsedAmount(budget.getUsedAmount().add(amount));
        budget.setRemainingAmount(budget.getBudgetAmount()
                .subtract(budget.getUsedAmount())
                .subtract(budget.getFrozenAmount()));

        // 如果已审批且首次使用，自动变为执行中
        if (budget.getStatus() == 1) {
            budget.setStatus(2);
        }

        budget.setUpdatedAt(LocalDateTime.now());
        updateById(budget);

        log.info("记录预算使用: budgetId={}, amount={}, remaining={}",
                budgetId, amount, budget.getRemainingAmount());
    }

    /**
     * 获取预算执行情况
     *
     * @param id 预算ID
     * @return 执行情况详情
     */
    @Override
    public Map<String, Object> getBudgetExecution(Long id) {
        FinBudget budget = getById(id);
        if (budget == null) {
            throw new BusinessException("预算不存在");
        }

        Map<String, Object> execution = new HashMap<>();
        execution.put("id", budget.getId());
        execution.put("budgetName", budget.getBudgetName());
        execution.put("budgetType", budget.getBudgetType());
        execution.put("periodStart", budget.getPeriodStart());
        execution.put("periodEnd", budget.getPeriodEnd());
        execution.put("budgetAmount", budget.getBudgetAmount());
        execution.put("usedAmount", budget.getUsedAmount());
        execution.put("frozenAmount", budget.getFrozenAmount());
        execution.put("remainingAmount", budget.getRemainingAmount());
        execution.put("controlLevel", budget.getControlLevel());
        execution.put("status", budget.getStatus());

        // 计算使用率
        BigDecimal usageRate = BigDecimal.ZERO;
        if (budget.getBudgetAmount().compareTo(BigDecimal.ZERO) > 0) {
            usageRate = budget.getUsedAmount()
                    .divide(budget.getBudgetAmount(), 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100"));
        }
        execution.put("usageRate", usageRate);
        execution.put("warningThreshold", budget.getWarningThreshold());

        // 判断是否超阈值
        execution.put("isOverThreshold", usageRate.compareTo(budget.getWarningThreshold()) >= 0);
        execution.put("isOverBudget", budget.getRemainingAmount().compareTo(BigDecimal.ZERO) < 0);

        return execution;
    }

    /**
     * 查找有效的预算（已审批或执行中）
     */
    private FinBudget findActiveBudget(Long tenantId, Long targetId) {
        LambdaQueryWrapper<FinBudget> wrapper = new LambdaQueryWrapper<FinBudget>()
                .eq(FinBudget::getTenantId, tenantId)
                .in(FinBudget::getStatus, 1, 2) // 已审批或执行中
                .orderByDesc(FinBudget::getCreatedAt);

        if (targetId != null) {
            wrapper.and(w -> w.eq(FinBudget::getTargetId, targetId)
                    .or().eq(FinBudget::getBudgetType, "OVERALL"));
        } else {
            wrapper.eq(FinBudget::getBudgetType, "OVERALL");
        }

        wrapper.last("LIMIT 1");
        return getOne(wrapper);
    }

    /**
     * 校验期间是否重叠
     */
    private void validatePeriodOverlap(FinBudget budget) {
        long count = count(new LambdaQueryWrapper<FinBudget>()
                .eq(FinBudget::getTenantId, budget.getTenantId())
                .eq(budget.getTargetId() != null, FinBudget::getTargetId, budget.getTargetId())
                .eq(FinBudget::getBudgetType, budget.getBudgetType())
                .le(FinBudget::getPeriodStart, budget.getPeriodEnd())
                .ge(FinBudget::getPeriodEnd, budget.getPeriodStart()));

        if (count > 0) {
            throw new BusinessException("该期间内已存在相同类型的预算");
        }
    }
}
