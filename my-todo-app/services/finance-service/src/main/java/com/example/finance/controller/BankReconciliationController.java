package com.example.finance.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.common.core.annotation.RequiresPermission;
import com.example.common.core.result.ApiResponse;
import com.example.common.core.result.PageResult;
import com.example.finance.dto.ManualMatchDTO;
import com.example.finance.entity.FinBankReconciliation;
import com.example.finance.entity.FinBankRecord;
import com.example.finance.service.BankReconciliationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 银行对账控制器
 */
@Tag(name = "银行对账", description = "银行对账管理API")
@RestController
@RequestMapping("/api/finance/bank-reconciliation")
@RequiredArgsConstructor
public class BankReconciliationController {

    private final BankReconciliationService bankReconciliationService;

    @RequiresPermission(code = "finance:bankReconciliation:import", name = "导入银行对账单")
    @Operation(summary = "导入银行对账单")
    @PostMapping("/import")
    public ApiResponse<FinBankReconciliation> importBankStatement(
            @RequestParam("file") MultipartFile file,
            @RequestParam("bankAccountId") Long bankAccountId,
            @RequestHeader("X-Tenant-Id") Long tenantId) {
        return ApiResponse.success(bankReconciliationService.importBankStatement(file, bankAccountId, tenantId));
    }

    @RequiresPermission(code = "finance:bankReconciliation:autoMatch", name = "自动匹配")
    @Operation(summary = "自动匹配对账")
    @PostMapping("/auto-match/{id}")
    public ApiResponse<Integer> autoMatch(@PathVariable Long id) {
        int matched = bankReconciliationService.autoMatch(id);
        return ApiResponse.success(matched);
    }

    @RequiresPermission(code = "finance:bankReconciliation:manualMatch", name = "手动匹配")
    @Operation(summary = "手动匹配")
    @PostMapping("/manual-match")
    public ApiResponse<Void> manualMatch(@Valid @RequestBody ManualMatchDTO dto) {
        bankReconciliationService.manualMatch(dto.getBankRecordId(), dto.getSystemRecordId());
        return ApiResponse.success();
    }

    @RequiresPermission(code = "finance:bankReconciliation:list", name = "查询对账列表")
    @Operation(summary = "分页查询对账列表")
    @GetMapping("/page")
    public ApiResponse<PageResult<FinBankReconciliation>> getReconciliationPage(
            @RequestHeader("X-Tenant-Id") Long tenantId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<FinBankReconciliation> result = bankReconciliationService.getReconciliationPage(tenantId, page, size);
        PageResult<FinBankReconciliation> pageResult = PageResult.of(
                result.getRecords(), result.getTotal(), result.getCurrent(), result.getSize());
        return ApiResponse.success(pageResult);
    }

    @RequiresPermission(code = "finance:bankReconciliation:list", name = "查询未匹配记录")
    @Operation(summary = "获取未匹配记录")
    @GetMapping("/unmatched/{id}")
    public ApiResponse<List<FinBankRecord>> getUnmatchedRecords(@PathVariable Long id) {
        return ApiResponse.success(bankReconciliationService.getUnmatchedRecords(id));
    }

    @RequiresPermission(code = "finance:bankReconciliation:unmatch", name = "取消匹配")
    @Operation(summary = "取消匹配")
    @PostMapping("/unmatch/{bankRecordId}")
    public ApiResponse<Void> unmatch(@PathVariable Long bankRecordId) {
        bankReconciliationService.unmatch(bankRecordId);
        return ApiResponse.success();
    }
}
