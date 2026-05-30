package com.example.finance.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.common.core.annotation.RequiresPermission;
import com.example.common.core.result.ApiResponse;
import com.example.common.core.result.PageResult;
import com.example.finance.entity.Bill;
import com.example.finance.entity.Invoice;
import com.example.finance.entity.PaymentRecord;
import com.example.finance.service.BillService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 统一账单管理控制器
 * <p>
 * 提供统一账单的完整生命周期 REST API，包括：
 * <ul>
 *   <li>账单 CRUD 操作</li>
 *   <li>账单审核流程：提交、审核通过、驳回、取消</li>
 *   <li>账单关联数据查询：收支记录、发票</li>
 *   <li>账单汇总统计</li>
 * </ul>
 * </p>
 *
 * @author finance-team
 * @since 1.0
 */
@Tag(name = "账单管理", description = "统一账单相关API")
@RestController
@RequestMapping("/api/finance/bills")
@RequiredArgsConstructor
public class BillController {

    private final BillService billService;

    /**
     * 分页查询账单列表
     *
     * @param tenantId    租户ID（从请求头获取）
     * @param page        当前页码，默认第1页
     * @param size        每页记录数，默认10条
     * @param billType    账单类型（可选）：1=应收, 2=应付
     * @param status      状态（可选）：0=草稿, 1=待审核, 2=已审核, 3=部分收付, 4=已完成, 5=已取消
     * @param partnerType 往来单位类型（可选）：1=客户, 2=供应商
     * @param partnerId   往来单位ID（可选）
     * @param startDate   账单日期起始范围（可选，格式：yyyy-MM-dd）
     * @param endDate     账单日期结束范围（可选，格式：yyyy-MM-dd）
     * @param keyword     关键词搜索（可选）
     * @return 分页查询结果
     */
    @RequiresPermission(code = "finance:bill:list", name = "查询账单列表")
    @Operation(summary = "分页查询账单列表")
    @GetMapping
    public ApiResponse<PageResult<Bill>> getBillPage(
            @RequestHeader("X-Tenant-Id") Long tenantId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Integer billType,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Integer partnerType,
            @RequestParam(required = false) Long partnerId,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            @RequestParam(required = false) String keyword) {
        Page<Bill> result = billService.getBillPage(tenantId, page, size,
                billType, status, partnerType, partnerId, startDate, endDate, keyword);
        PageResult<Bill> pageResult = PageResult.of(result.getRecords(), result.getTotal(), result.getCurrent(), result.getSize());
        return ApiResponse.success(pageResult);
    }

    /**
     * 创建账单
     *
     * @param bill     账单数据（请求体）
     * @param tenantId 租户ID（从请求头获取）
     * @param userId   当前操作用户ID（从请求头获取）
     * @return 创建成功的账单对象
     */
    @RequiresPermission(code = "finance:bill:create", name = "创建账单")
    @Operation(summary = "创建账单")
    @PostMapping
    public ApiResponse<Bill> createBill(
            @RequestBody Bill bill,
            @RequestHeader("X-Tenant-Id") Long tenantId,
            @RequestHeader("X-User-Id") Long userId) {
        bill.setTenantId(tenantId);
        bill.setCreatedBy(userId);
        return ApiResponse.success(billService.createBill(bill));
    }

    /**
     * 根据ID查询账单详情
     *
     * @param id 账单ID
     * @return 账单详情
     */
    @RequiresPermission(code = "finance:bill:list", name = "查询账单列表")
    @Operation(summary = "查询账单详情")
    @GetMapping("/{id}")
    public ApiResponse<Bill> getBillById(@PathVariable Long id) {
        return ApiResponse.success(billService.getBillById(id));
    }

    /**
     * 更新账单（仅草稿状态可编辑）
     *
     * @param id       账单ID
     * @param bill     账单数据（请求体）
     * @param userId   当前操作用户ID（从请求头获取）
     * @return 更新后的账单对象
     */
    @RequiresPermission(code = "finance:bill:update", name = "更新账单")
    @Operation(summary = "更新账单")
    @PutMapping("/{id}")
    public ApiResponse<Bill> updateBill(
            @PathVariable Long id,
            @RequestBody Bill bill,
            @RequestHeader("X-User-Id") Long userId) {
        bill.setId(id);
        bill.setUpdatedBy(userId);
        return ApiResponse.success(billService.updateBill(bill));
    }

    /**
     * 提交账单（草稿 -> 待审核）
     *
     * @param id 账单ID
     * @return 操作结果
     */
    @RequiresPermission(code = "finance:bill:submit", name = "提交账单")
    @Operation(summary = "提交账单")
    @PostMapping("/{id}/submit")
    public ApiResponse<Void> submitBill(@PathVariable Long id) {
        billService.submitBill(id);
        return ApiResponse.success();
    }

    /**
     * 审核通过账单（待审核 -> 已审核）
     *
     * @param id          账单ID
     * @param userId      审核人ID（从请求头获取）
     * @param auditRemark 审核备注（可选）
     * @return 操作结果
     */
    @RequiresPermission(code = "finance:bill:approve", name = "审核账单")
    @Operation(summary = "审核通过账单")
    @PostMapping("/{id}/approve")
    public ApiResponse<Void> approveBill(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long userId,
            @RequestParam(required = false) String auditRemark) {
        billService.approveBill(id, userId, auditRemark);
        return ApiResponse.success();
    }

    /**
     * 驳回账单（待审核 -> 草稿）
     *
     * @param id          账单ID
     * @param userId      审核人ID（从请求头获取）
     * @param auditRemark 驳回原因（必填）
     * @return 操作结果
     */
    @RequiresPermission(code = "finance:bill:approve", name = "审核账单")
    @Operation(summary = "驳回账单")
    @PostMapping("/{id}/reject")
    public ApiResponse<Void> rejectBill(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long userId,
            @RequestParam String auditRemark) {
        billService.rejectBill(id, userId, auditRemark);
        return ApiResponse.success();
    }

    /**
     * 取消账单
     *
     * @param id 账单ID
     * @return 操作结果
     */
    @RequiresPermission(code = "finance:bill:cancel", name = "取消账单")
    @Operation(summary = "取消账单")
    @PostMapping("/{id}/cancel")
    public ApiResponse<Void> cancelBill(@PathVariable Long id) {
        billService.cancelBill(id);
        return ApiResponse.success();
    }

    /**
     * 获取账单关联的收支记录
     *
     * @param id       账单ID
     * @param tenantId 租户ID（从请求头获取）
     * @return 收支记录列表
     */
    @RequiresPermission(code = "finance:bill:list", name = "查询账单列表")
    @Operation(summary = "获取账单关联收支记录")
    @GetMapping("/{id}/payments")
    public ApiResponse<List<PaymentRecord>> getBillPayments(
            @PathVariable Long id,
            @RequestHeader("X-Tenant-Id") Long tenantId) {
        return ApiResponse.success(billService.getBillPayments(id, tenantId));
    }

    /**
     * 获取账单关联的发票列表
     *
     * @param id 账单ID
     * @return 发票列表
     */
    @RequiresPermission(code = "finance:bill:list", name = "查询账单列表")
    @Operation(summary = "获取账单关联发票")
    @GetMapping("/{id}/invoices")
    public ApiResponse<List<Invoice>> getBillInvoices(@PathVariable Long id) {
        return ApiResponse.success(billService.getBillInvoices(id));
    }

    /**
     * 获取账单汇总统计
     *
     * @param tenantId 租户ID（从请求头获取）
     * @param billType 账单类型：1=应收, 2=应付
     * @return 汇总数据（totalAmount, paidAmount, unpaidAmount, overdueAmount）
     */
    @RequiresPermission(code = "finance:bill:list", name = "查询账单列表")
    @Operation(summary = "获取账单汇总统计")
    @GetMapping("/summary")
    public ApiResponse<Map<String, BigDecimal>> getBillSummary(
            @RequestHeader("X-Tenant-Id") Long tenantId,
            @RequestParam Integer billType) {
        return ApiResponse.success(billService.getBillSummary(tenantId, billType));
    }
}
