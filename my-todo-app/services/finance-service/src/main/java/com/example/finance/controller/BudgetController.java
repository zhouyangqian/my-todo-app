package com.example.finance.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.common.core.annotation.RequiresPermission;
import com.example.common.core.result.ApiResponse;
import com.example.common.core.result.PageResult;
import com.example.finance.dto.BudgetDTO;
import com.example.finance.entity.FinBudget;
import com.example.finance.service.BudgetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 预算管理控制器
 */
@Tag(name = "预算管理", description = "预算管理API")
@RestController
@RequestMapping("/api/finance/budgets")
@RequiredArgsConstructor
public class BudgetController {

    private final BudgetService budgetService;

    @RequiresPermission(code = "finance:budget:create", name = "创建预算")
    @Operation(summary = "创建预算")
    @PostMapping("/create")
    public ApiResponse<FinBudget> createBudget(
            @RequestHeader("X-Tenant-Id") Long tenantId,
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody BudgetDTO dto) {
        FinBudget budget = new FinBudget();
        budget.setTenantId(tenantId);
        budget.setBudgetName(dto.getBudgetName());
        budget.setBudgetType(dto.getBudgetType());
        budget.setTargetId(dto.getTargetId());
        budget.setPeriodType(dto.getPeriodType());
        budget.setPeriodStart(dto.getPeriodStart());
        budget.setPeriodEnd(dto.getPeriodEnd());
        budget.setBudgetAmount(dto.getBudgetAmount());
        budget.setControlLevel(dto.getControlLevel());
        budget.setWarningThreshold(dto.getWarningThreshold());
        budget.setCreatedBy(userId);
        return ApiResponse.success(budgetService.createBudget(budget));
    }

    @RequiresPermission(code = "finance:budget:update", name = "更新预算")
    @Operation(summary = "更新预算")
    @PutMapping("/update/{id}")
    public ApiResponse<FinBudget> updateBudget(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody BudgetDTO dto) {
        return ApiResponse.success(budgetService.updateBudget(id, dto));
    }

    @RequiresPermission(code = "finance:budget:approve", name = "审批预算")
    @Operation(summary = "审批预算")
    @PostMapping("/approve/{id}")
    public ApiResponse<Void> approveBudget(@PathVariable Long id) {
        budgetService.approveBudget(id);
        return ApiResponse.success();
    }

    @RequiresPermission(code = "finance:budget:delete", name = "删除预算")
    @Operation(summary = "删除预算")
    @DeleteMapping("/delete/{id}")
    public ApiResponse<Void> deleteBudget(@PathVariable Long id) {
        budgetService.removeById(id);
        return ApiResponse.success();
    }

    @RequiresPermission(code = "finance:budget:list", name = "查询预算列表")
    @Operation(summary = "分页查询预算列表")
    @GetMapping("/page")
    public ApiResponse<PageResult<FinBudget>> getBudgetPage(
            @RequestHeader("X-Tenant-Id") Long tenantId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String budgetType,
            @RequestParam(required = false) Integer status) {
        Page<FinBudget> result = budgetService.getBudgetPage(tenantId, page, size, budgetType, status);
        PageResult<FinBudget> pageResult = PageResult.of(
                result.getRecords(), result.getTotal(), result.getCurrent(), result.getSize());
        return ApiResponse.success(pageResult);
    }

    @RequiresPermission(code = "finance:budget:list", name = "查询预算执行情况")
    @Operation(summary = "获取预算执行情况")
    @GetMapping("/execution/{id}")
    public ApiResponse<Map<String, Object>> getBudgetExecution(@PathVariable Long id) {
        return ApiResponse.success(budgetService.getBudgetExecution(id));
    }

    @RequiresPermission(code = "finance:budget:check", name = "检查预算")
    @Operation(summary = "检查预算是否充足")
    @GetMapping("/check")
    public ApiResponse<Map<String, Object>> checkBudget(
            @RequestHeader("X-Tenant-Id") Long tenantId,
            @RequestParam(required = false) Long targetId,
            @RequestParam BigDecimal amount) {
        return ApiResponse.success(budgetService.checkBudget(tenantId, targetId, amount));
    }
}
