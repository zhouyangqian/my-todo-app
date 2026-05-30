package com.example.finance.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.finance.dto.BudgetDTO;
import com.example.finance.entity.FinBudget;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 预算管理服务接口
 */
public interface BudgetService extends IService<FinBudget> {

    FinBudget createBudget(FinBudget budget);

    void approveBudget(Long id);

    FinBudget updateBudget(Long id, BudgetDTO dto);

    Page<FinBudget> getBudgetPage(Long tenantId, int page, int size, String budgetType, Integer status);

    Map<String, Object> checkBudget(Long tenantId, Long targetId, BigDecimal amount);

    void recordUsage(Long budgetId, BigDecimal amount);

    Map<String, Object> getBudgetExecution(Long id);
}
