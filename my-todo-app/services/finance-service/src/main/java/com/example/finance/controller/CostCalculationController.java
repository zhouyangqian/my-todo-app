package com.example.finance.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.common.core.annotation.RequiresPermission;
import com.example.common.core.result.ApiResponse;
import com.example.common.core.result.PageResult;
import com.example.finance.entity.FinCostConfig;
import com.example.finance.entity.FinCostHistory;
import com.example.finance.service.CostCalculationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

/**
 * 成本核算控制器
 */
@Tag(name = "成本核算", description = "成本核算管理API")
@RestController
@RequestMapping("/api/finance/cost")
@RequiredArgsConstructor
public class CostCalculationController {

    private final CostCalculationService costCalculationService;

    @RequiresPermission(code = "finance:cost:list", name = "查询成本配置")
    @Operation(summary = "分页查询成本配置")
    @GetMapping("/get-config-page")
    public ApiResponse<PageResult<FinCostConfig>> getConfigPage(
            @RequestHeader("X-Tenant-Id") Long tenantId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<FinCostConfig> result = costCalculationService.getConfigPage(tenantId, page, size);
        PageResult<FinCostConfig> pageResult = PageResult.of(result.getRecords(), result.getTotal(), result.getCurrent(), result.getSize());
        return ApiResponse.success(pageResult);
    }

    @RequiresPermission(code = "finance:cost:update", name = "设置成本方法")
    @Operation(summary = "设置商品成本核算方法")
    @PostMapping("/set-cost-method")
    public ApiResponse<FinCostConfig> setCostMethod(
            @RequestHeader("X-Tenant-Id") Long tenantId,
            @RequestHeader("X-User-Id") Long userId,
            @RequestParam Long productId,
            @RequestParam Integer costMethod) {
        return ApiResponse.success(costCalculationService.setCostMethod(tenantId, productId, costMethod, userId));
    }

    @RequiresPermission(code = "finance:cost:calculate", name = "计算出库成本")
    @Operation(summary = "计算商品出库成本")
    @GetMapping("/calculate-outbound-cost")
    public ApiResponse<BigDecimal> calculateOutboundCost(
            @RequestHeader("X-Tenant-Id") Long tenantId,
            @RequestParam Long productId,
            @RequestParam BigDecimal quantity,
            @RequestParam(required = false) Long warehouseId) {
        BigDecimal cost = costCalculationService.calculateOutboundCost(tenantId, productId, quantity, warehouseId);
        return ApiResponse.success(cost);
    }

    @RequiresPermission(code = "finance:cost:list", name = "查询成本历史")
    @Operation(summary = "分页查询成本历史")
    @GetMapping("/get-cost-history-page")
    public ApiResponse<PageResult<FinCostHistory>> getCostHistoryPage(
            @RequestHeader("X-Tenant-Id") Long tenantId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long productId,
            @RequestParam(required = false) Integer bizType) {
        Page<FinCostHistory> result = costCalculationService.getCostHistoryPage(tenantId, page, size, productId, bizType);
        PageResult<FinCostHistory> pageResult = PageResult.of(result.getRecords(), result.getTotal(), result.getCurrent(), result.getSize());
        return ApiResponse.success(pageResult);
    }
}
