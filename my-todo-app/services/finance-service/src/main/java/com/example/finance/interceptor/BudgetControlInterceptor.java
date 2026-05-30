package com.example.finance.interceptor;

import com.example.common.core.exception.BusinessException;
import com.example.finance.service.BudgetService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 预算控制拦截器
 * <p>
 * 在创建账单或支付记录前检查预算是否充足。
 * 通过调用 BudgetService.checkBudget() 判断是否允许操作。
 * 如果预算控制级别为 FORCE 且超出预算，则抛出 BusinessException 阻止操作。
 * </p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BudgetControlInterceptor {

    private final BudgetService budgetService;

    /**
     * 检查预算是否允许该笔支出
     *
     * @param tenantId 租户ID
     * @param targetId 目标ID（部门/项目，可为null表示使用总预算）
     * @param amount   拟支出金额
     */
    public void checkBudgetBeforePayment(Long tenantId, Long targetId, BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }
        try {
            Map<String, Object> result = budgetService.checkBudget(tenantId, targetId, amount);
            Boolean allowed = (Boolean) result.get("allowed");
            if (allowed != null && !allowed) {
                String message = (String) result.getOrDefault("message", "预算不足，操作被阻止");
                throw new BusinessException(message);
            }
            // 即使允许，也记录警告信息
            String message = (String) result.get("message");
            if (message != null && !message.isEmpty()) {
                log.warn("预算控制警告: tenantId={}, targetId={}, amount={}, message={}",
                        tenantId, targetId, amount, message);
            }
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("预算检查异常: tenantId={}, targetId={}, amount={}", tenantId, targetId, amount, e);
        }
    }
}
