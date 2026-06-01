package com.example.finance.controller;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.common.core.annotation.RequiresPermission;
import com.example.common.core.result.ApiResponse;
import com.example.common.core.result.PageResult;
import com.example.finance.entity.*;
import com.example.finance.service.*;
import com.example.finance.vo.TransferVO;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 财务管理统一控制器
 * <p>
 * 提供财务管理模块的全部 REST API 接口，包含以下四大子模块：
 * <ul>
 *   <li>应收账款管理 - 查询、创建、收款、逾期查询</li>
 *   <li>应付账款管理 - 查询、创建、付款、逾期查询</li>
 *   <li>收支记录管理 - 查询、创建、审核、取消</li>
 *   <li>银行账户管理 - 查询、创建、更新、删除</li>
 * </ul>
 * </p>
 * <p>
 * 接口通过请求头 X-Tenant-Id 实现多租户数据隔离，
 * 通过请求头 X-User-Id 获取当前操作用户信息。
 * 集成 Swagger/OpenAPI 文档注解，便于接口测试与文档生成。
 * </p>
 *
 * @author finance-team
 * @since 1.0
 */
@Tag(name = "财务管理", description = "财务相关API")
@RestController
@RequestMapping("/api/finance")
@RequiredArgsConstructor
public class FinanceController {

    /** 应收账款服务 */
    private final AccountReceivableService accountReceivableService;

    /** 应付账款服务 */
    private final AccountPayableService accountPayableService;

    /** 收支记录服务 */
    private final PaymentRecordService paymentRecordService;

    /** 银行账户服务 */
    private final BankAccountService bankAccountService;

    /** 财务报表服务 */
    private final FinanceReportService financeReportService;

    // ==================== 应收账款接口 ====================

    /**
     * 分页查询应收账款列表
     *
     * @param tenantId   租户ID（从请求头获取）
     * @param page       当前页码，默认第1页
     * @param size       每页记录数，默认10条
     * @param customerId 客户ID（可选筛选条件）
     * @param status     结算状态（可选筛选条件）：0-未结算, 1-部分结算, 2-已结算
     * @return 分页查询结果
     */
    @RequiresPermission(code = "finance:receivable:list", name = "查询应收账款列表")
    @Operation(summary = "分页查询应收账款")
    @GetMapping("/receivables/get-receivable-page")
    public ApiResponse<PageResult<AccountReceivable>> getReceivablePage(
            @RequestHeader("X-Tenant-Id") Long tenantId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long customerId,
            @RequestParam(required = false) Integer status) {
        Page<AccountReceivable> result = accountReceivableService.getPage(tenantId, page, size, customerId, status);
        PageResult<AccountReceivable> pageResult = PageResult.of(result.getRecords(), result.getTotal(), result.getCurrent(), result.getSize());
        return ApiResponse.success(pageResult);
    }

    /**
     * 创建应收账款记录
     *
     * @param receivable 应收账款数据（请求体）
     * @param tenantId   租户ID（从请求头获取）
     * @param userId     当前操作用户ID（从请求头获取）
     * @return 创建成功的应收账款对象
     */
    @RequiresPermission(code = "finance:receivable:create", name = "创建应收账款")
    @Operation(summary = "创建应收账款")
    @PostMapping("/receivables/create-receivable")
    public ApiResponse<AccountReceivable> createReceivable(
            @RequestBody AccountReceivable receivable,
            @RequestHeader("X-Tenant-Id") Long tenantId,
            @RequestHeader("X-User-Id") Long userId) {
        // 从请求头中设置租户ID和创建人ID，确保数据安全
        receivable.setTenantId(tenantId);
        receivable.setCreatedBy(userId);
        return ApiResponse.success(accountReceivableService.create(receivable));
    }

    /**
     * 应收账款收款确认
     *
     * @param id     应收账款记录ID（路径参数）
     * @param amount 本次收款金额（请求参数）
     * @return 操作结果
     */
    @RequiresPermission(code = "finance:receivable:receive", name = "应收账款收款")
    @Operation(summary = "应收账款收款")
    @PostMapping("/receivables/receive-payment/{id}")
    public ApiResponse<Void> receivePayment(
            @PathVariable Long id,
            @RequestParam BigDecimal amount) {
        accountReceivableService.receivePayment(id, amount);
        return ApiResponse.success();
    }

    /**
     * 获取逾期应收账款列表
     * <p>
     * 返回所有到期日期已过但仍未结清的应收账款记录，用于催收管理。
     * </p>
     *
     * @param tenantId 租户ID（从请求头获取）
     * @return 逾期的应收账款列表
     */
    @RequiresPermission(code = "finance:receivable:list", name = "查询应收账款列表")
    @Operation(summary = "获取逾期应收账款")
    @GetMapping("/receivables/get-overdue-receivables")
    public ApiResponse<List<AccountReceivable>> getOverdueReceivables(
            @RequestHeader("X-Tenant-Id") Long tenantId) {
        return ApiResponse.success(accountReceivableService.getOverdueList(tenantId));
    }

    /**
     * 删除应收账款（软删除）
     * <p>
     * 仅允许删除未结算状态（status=0）的应收账款记录。
     * </p>
     *
     * @param id 应收账款ID（路径参数）
     * @return 操作结果
     */
    @RequiresPermission(code = "finance:receivable:delete", name = "删除应收账款")
    @Operation(summary = "删除应收账款")
    @DeleteMapping("/receivables/delete-receivable/{id}")
    public ApiResponse<Void> deleteReceivable(@PathVariable Long id) {
        accountReceivableService.removeById(id);
        return ApiResponse.success();
    }

    // ==================== 应付账款接口 ====================

    /**
     * 分页查询应付账款列表
     *
     * @param tenantId   租户ID（从请求头获取）
     * @param page       当前页码，默认第1页
     * @param size       每页记录数，默认10条
     * @param supplierId 供应商ID（可选筛选条件）
     * @param status     结算状态（可选筛选条件）：0-未结算, 1-部分结算, 2-已结算
     * @return 分页查询结果
     */
    @RequiresPermission(code = "finance:payable:list", name = "查询应付账款列表")
    @Operation(summary = "分页查询应付账款")
    @GetMapping("/payables/get-payable-page")
    public ApiResponse<PageResult<AccountPayable>> getPayablePage(
            @RequestHeader("X-Tenant-Id") Long tenantId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long supplierId,
            @RequestParam(required = false) Integer status) {
        Page<AccountPayable> result = accountPayableService.getPage(tenantId, page, size, supplierId, status);
        PageResult<AccountPayable> pageResult = PageResult.of(result.getRecords(), result.getTotal(), result.getCurrent(), result.getSize());
        return ApiResponse.success(pageResult);
    }

    /**
     * 创建应付账款记录
     *
     * @param payable  应付账款数据（请求体）
     * @param tenantId 租户ID（从请求头获取）
     * @param userId   当前操作用户ID（从请求头获取）
     * @return 创建成功的应付账款对象
     */
    @RequiresPermission(code = "finance:payable:create", name = "创建应付账款")
    @Operation(summary = "创建应付账款")
    @PostMapping("/payables/create-payable")
    public ApiResponse<AccountPayable> createPayable(
            @RequestBody AccountPayable payable,
            @RequestHeader("X-Tenant-Id") Long tenantId,
            @RequestHeader("X-User-Id") Long userId) {
        // 从请求头中设置租户ID和创建人ID，确保数据安全
        payable.setTenantId(tenantId);
        payable.setCreatedBy(userId);
        return ApiResponse.success(accountPayableService.create(payable));
    }

    /**
     * 应付账款付款确认
     *
     * @param id     应付账款记录ID（路径参数）
     * @param amount 本次付款金额（请求参数）
     * @return 操作结果
     */
    @RequiresPermission(code = "finance:payable:pay", name = "应付账款付款")
    @Operation(summary = "应付账款付款")
    @PostMapping("/payables/make-payment/{id}")
    public ApiResponse<Void> makePayment(
            @PathVariable Long id,
            @RequestParam BigDecimal amount) {
        accountPayableService.makePayment(id, amount);
        return ApiResponse.success();
    }

    /**
     * 获取逾期应付账款列表
     * <p>
     * 返回所有到期日期已过但仍未付清的应付账款记录，用于付款提醒。
     * </p>
     *
     * @param tenantId 租户ID（从请求头获取）
     * @return 逾期的应付账款列表
     */
    @RequiresPermission(code = "finance:payable:list", name = "查询应付账款列表")
    @Operation(summary = "获取逾期应付账款")
    @GetMapping("/payables/get-overdue-payables")
    public ApiResponse<List<AccountPayable>> getOverduePayables(
            @RequestHeader("X-Tenant-Id") Long tenantId) {
        return ApiResponse.success(accountPayableService.getOverdueList(tenantId));
    }

    /**
     * 删除应付账款（软删除）
     * <p>
     * 仅允许删除未结算状态（status=0）的应付账款记录。
     * </p>
     *
     * @param id 应付账款ID（路径参数）
     * @return 操作结果
     */
    @RequiresPermission(code = "finance:payable:delete", name = "删除应付账款")
    @Operation(summary = "删除应付账款")
    @DeleteMapping("/payables/delete-payable/{id}")
    public ApiResponse<Void> deletePayable(@PathVariable Long id) {
        accountPayableService.removeById(id);
        return ApiResponse.success();
    }

    // ==================== 收支记录接口 ====================

    /**
     * 分页查询收支记录列表
     * <p>
     * 支持按收支类型、业务类型和交易日期范围进行多维筛选。
     * </p>
     *
     * @param tenantId   租户ID（从请求头获取）
     * @param page       当前页码，默认第1页
     * @param size       每页记录数，默认10条
     * @param recordType 收支类型（可选）：1-收入, 2-支出
     * @param bizType    业务类型（可选）：1-销售收款, 2-采购付款, 3-退款, 4-其他收入, 5-其他支出
     * @param startDate  交易日期起始范围（可选，格式：yyyy-MM-dd HH:mm:ss）
     * @param endDate    交易日期结束范围（可选，格式：yyyy-MM-dd HH:mm:ss）
     * @return 分页查询结果
     */
    @RequiresPermission(code = "finance:record:list", name = "查询收支记录列表")
    @Operation(summary = "分页查询收支记录")
    @GetMapping("/records/get-record-page")
    public ApiResponse<PageResult<PaymentRecord>> getRecordPage(
            @RequestHeader("X-Tenant-Id") Long tenantId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Integer recordType,
            @RequestParam(required = false) Integer bizType,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endDate) {
        Page<PaymentRecord> result = paymentRecordService.getPage(tenantId, page, size, recordType, bizType, startDate, endDate);
        PageResult<PaymentRecord> pageResult = PageResult.of(result.getRecords(), result.getTotal(), result.getCurrent(), result.getSize());
        return ApiResponse.success(pageResult);
    }

    /**
     * 创建收支记录
     * <p>
     * 创建后状态为"待审核"，需经审核后才会实际影响银行账户余额。
     * 自动从请求头获取租户ID、创建人和经手人信息。
     * </p>
     *
     * @param record   收支记录数据（请求体）
     * @param tenantId 租户ID（从请求头获取）
     * @param userId   当前操作用户ID（从请求头获取，同时作为创建人和经手人）
     * @return 创建成功的收支记录对象（含自动生成的单据编号）
     */
    @RequiresPermission(code = "finance:record:create", name = "创建收支记录")
    @Operation(summary = "创建收支记录")
    @PostMapping("/records/create-record")
    public ApiResponse<PaymentRecord> createRecord(
            @RequestBody PaymentRecord record,
            @RequestHeader("X-Tenant-Id") Long tenantId,
            @RequestHeader("X-User-Id") Long userId) {
        // 从请求头中设置租户ID、创建人和经手人ID
        record.setTenantId(tenantId);
        record.setCreatedBy(userId);
        record.setHandlerId(userId);
        return ApiResponse.success(paymentRecordService.create(record));
    }

    /**
     * 审核收支记录
     * <p>
     * 审核通过后状态变为"已审核"，同时自动调整关联银行账户的余额。
     * 仅"待审核"状态的记录允许审核操作。
     * </p>
     *
     * @param id     收支记录ID（路径参数）
     * @param userId 审核人ID（从请求头获取）
     * @return 操作结果
     */
    @RequiresPermission(code = "finance:record:approve", name = "审核收支记录")
    @Operation(summary = "审核收支记录")
    @PostMapping("/records/approve-record/{id}")
    public ApiResponse<Void> approveRecord(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long userId) {
        paymentRecordService.approve(id, userId);
        return ApiResponse.success();
    }

    /**
     * 取消收支记录
     * <p>
     * 如果记录已审核通过，取消时会反向调整银行账户余额（撤销之前的余额变动）。
     * 取消后状态变为"已取消"。
     * </p>
     *
     * @param id 收支记录ID（路径参数）
     * @return 操作结果
     */
    @RequiresPermission(code = "finance:record:cancel", name = "取消收支记录")
    @Operation(summary = "取消收支记录")
    @PostMapping("/records/cancel-record/{id}")
    public ApiResponse<Void> cancelRecord(@PathVariable Long id) {
        paymentRecordService.cancel(id);
        return ApiResponse.success();
    }

    // ==================== 银行账户接口 ====================

    /**
     * 分页查询银行账户列表
     *
     * @param tenantId    租户ID（从请求头获取）
     * @param page        当前页码，默认第1页
     * @param size        每页记录数，默认10条
     * @param accountName 账户名称（可选，模糊匹配）
     * @param accountType 账户类型（可选）：1-现金账户, 2-银行账户, 3-支付宝, 4-微信
     * @return 分页查询结果
     */
    @RequiresPermission(code = "finance:bankAccount:list", name = "查询银行账户列表")
    @Operation(summary = "分页查询银行账户")
    @GetMapping("/bank-accounts/get-bank-account-page")
    public ApiResponse<PageResult<BankAccount>> getBankAccountPage(
            @RequestHeader("X-Tenant-Id") Long tenantId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String accountName,
            @RequestParam(required = false) Integer accountType) {
        Page<BankAccount> result = bankAccountService.getPage(tenantId, page, size, accountName, accountType);
        PageResult<BankAccount> pageResult = PageResult.of(result.getRecords(), result.getTotal(), result.getCurrent(), result.getSize());
        return ApiResponse.success(pageResult);
    }

    /**
     * 获取所有启用的银行账户列表（不分页）
     * <p>
     * 通常用于前端下拉选择框的数据源，仅返回状态为"启用"的账户。
     * </p>
     *
     * @param tenantId 租户ID（从请求头获取）
     * @return 所有启用状态的银行账户列表
     */
    @RequiresPermission(code = "finance:bankAccount:list", name = "查询银行账户列表")
    @Operation(summary = "获取所有银行账户(下拉选择)")
    @GetMapping("/bank-accounts/get-all-bank-accounts")
    public ApiResponse<List<BankAccount>> getAllBankAccounts(
            @RequestHeader("X-Tenant-Id") Long tenantId) {
        return ApiResponse.success(bankAccountService.getAllAccounts(tenantId));
    }

    /**
     * 创建银行账户
     *
     * @param account  银行账户数据（请求体）
     * @param tenantId 租户ID（从请求头获取）
     * @param userId   当前操作用户ID（从请求头获取）
     * @return 创建成功的银行账户对象
     */
    @RequiresPermission(code = "finance:bankAccount:create", name = "创建银行账户")
    @Operation(summary = "创建银行账户")
    @PostMapping("/bank-accounts/create-bank-account")
    public ApiResponse<BankAccount> createBankAccount(
            @RequestBody BankAccount account,
            @RequestHeader("X-Tenant-Id") Long tenantId,
            @RequestHeader("X-User-Id") Long userId) {
        // 从请求头中设置租户ID和创建人ID
        account.setTenantId(tenantId);
        account.setCreatedBy(userId);
        return ApiResponse.success(bankAccountService.create(account));
    }

    /**
     * 更新银行账户信息
     *
     * @param id      银行账户ID（路径参数）
     * @param account 银行账户数据（请求体）
     * @param userId  当前操作用户ID（从请求头获取）
     * @return 更新后的银行账户对象
     */
    @RequiresPermission(code = "finance:bankAccount:update", name = "更新银行账户")
    @Operation(summary = "更新银行账户")
    @PutMapping("/bank-accounts/update-bank-account/{id}")
    public ApiResponse<BankAccount> updateBankAccount(
            @PathVariable Long id,
            @RequestBody BankAccount account,
            @RequestHeader("X-User-Id") Long userId) {
        // 设置账户ID和更新人ID
        account.setId(id);
        account.setUpdatedBy(userId);
        return ApiResponse.success(bankAccountService.update(account));
    }

    /**
     * 删除银行账户（软删除）
     * <p>
     * 仅允许删除余额为零的账户。
     * </p>
     *
     * @param id 银行账户ID（路径参数）
     * @return 操作结果
     */
    @RequiresPermission(code = "finance:bankAccount:delete", name = "删除银行账户")
    @Operation(summary = "删除银行账户")
    @DeleteMapping("/bank-accounts/delete-bank-account/{id}")
    public ApiResponse<Void> deleteBankAccount(@PathVariable Long id) {
        bankAccountService.delete(id);
        return ApiResponse.success();
    }

    /**
     * 调整银行账户余额
     * <p>
     * 支持增加或减少账户余额，需提供调整类型（1-增加/2-减少）、调整金额和调整原因。
     * </p>
     *
     * @param id   银行账户ID（路径参数）
     * @param body 调整参数（adjustType: 1-增加/2-减少, amount: 金额, reason: 原因）
     * @return 操作结果
     */
    @RequiresPermission(code = "finance:bankAccount:update", name = "调整银行账户余额")
    @Operation(summary = "调整银行账户余额")
    @PostMapping("/bank-accounts/adjust-balance/{id}")
    public ApiResponse<Void> adjustBalance(
            @PathVariable Long id,
            @RequestBody Map<String, Object> body) {
        Integer adjustType = (Integer) body.get("adjustType");
        BigDecimal amount = new BigDecimal(body.get("amount").toString());
        boolean isAdd = (adjustType != null && adjustType == 1);
        bankAccountService.adjustBalance(id, amount, isAdd);
        return ApiResponse.success();
    }

    // ==================== 转账与统计接口 ====================

    /**
     * 账户间转账
     * <p>
     * 创建两条收支记录：转出账户生成EXPENSE记录，转入账户生成INCOME记录，
     * 两条记录通过 relatedTransId 互相关联。
     * </p>
     *
     * @param transferVO 转账请求参数
     * @param tenantId   租户ID（从请求头获取）
     * @param userId     当前操作用户ID（从请求头获取）
     * @return 操作结果
     */
    @RequiresPermission(code = "finance:record:create", name = "创建收支记录")
    @Operation(summary = "账户间转账")
    @PostMapping("/transactions/transfer")
    public ApiResponse<Void> transfer(
            @RequestBody @Valid TransferVO transferVO,
            @RequestHeader("X-Tenant-Id") Long tenantId,
            @RequestHeader("X-User-Id") Long userId) {
        paymentRecordService.transfer(transferVO, tenantId, userId);
        return ApiResponse.success();
    }

    /**
     * 账单汇总统计
     * <p>
     * 汇总应收/应付账款的总金额、已付/已收金额、未付/未收金额和逾期金额。
     * </p>
     *
     * @param tenantId 租户ID（从请求头获取）
     * @return 包含AR/AP汇总统计的Map
     */
    @RequiresPermission(code = "finance:receivable:list", name = "查询应收账款列表")
    @Operation(summary = "账单汇总统计")
    @GetMapping("/ar-ap/summary")
    public ApiResponse<Map<String, Object>> getBillSummary(
            @RequestHeader("X-Tenant-Id") Long tenantId) {
        Map<String, Object> summary = new HashMap<>();

        // 应收账款统计
        List<AccountReceivable> receivables = accountReceivableService.list(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<AccountReceivable>()
                        .eq(AccountReceivable::getTenantId, tenantId)
                        .eq(AccountReceivable::getDeleted, 0));
        BigDecimal totalAR = receivables.stream().map(AccountReceivable::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal receivedAR = receivables.stream().map(AccountReceivable::getReceivedAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal unreceivedAR = receivables.stream().map(AccountReceivable::getUnreceivedAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        List<AccountReceivable> overdueReceivables = accountReceivableService.getOverdueList(tenantId);
        BigDecimal overdueAR = overdueReceivables.stream().map(AccountReceivable::getUnreceivedAmount).reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, Object> arSummary = new HashMap<>();
        arSummary.put("total", totalAR);
        arSummary.put("paid", receivedAR);
        arSummary.put("unpaid", unreceivedAR);
        arSummary.put("overdue", overdueAR);
        summary.put("accountsReceivable", arSummary);

        // 应付账款统计
        List<AccountPayable> payables = accountPayableService.list(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<AccountPayable>()
                        .eq(AccountPayable::getTenantId, tenantId)
                        .eq(AccountPayable::getDeleted, 0));
        BigDecimal totalAP = payables.stream().map(AccountPayable::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal paidAP = payables.stream().map(AccountPayable::getPaidAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal unpaidAP = payables.stream().map(AccountPayable::getUnpaidAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        List<AccountPayable> overduePayables = accountPayableService.getOverdueList(tenantId);
        BigDecimal overdueAP = overduePayables.stream().map(AccountPayable::getUnpaidAmount).reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, Object> apSummary = new HashMap<>();
        apSummary.put("total", totalAP);
        apSummary.put("paid", paidAP);
        apSummary.put("unpaid", unpaidAP);
        apSummary.put("overdue", overdueAP);
        summary.put("accountsPayable", apSummary);

        return ApiResponse.success(summary);
    }

    /**
     * 导出财务报表到Excel
     * <p>
     * 将指定报表的 reportData（JSON格式）解析后导出为Excel文件。
     * 使用 EasyExcel 进行导出。
     * </p>
     *
     * @param id       报表ID（路径参数）
     * @param response HTTP响应对象
     * @throws IOException 写出文件时可能抛出的IO异常
     */
    @RequiresPermission(code = "finance:report:export", name = "导出财务报表")
    @Operation(summary = "导出报表到Excel")
    @GetMapping("/reports/{id}/export")
    public void exportReport(@PathVariable Long id, HttpServletResponse response) throws IOException {
        FinReport report = financeReportService.getById(id);
        if (report == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.getWriter().write("报表不存在");
            return;
        }

        // 设置响应头
        String fileName = URLEncoder.encode(
                "财务报表-" + report.getReportPeriod() + "-" + report.getReportType(), StandardCharsets.UTF_8);
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment;filename=" + fileName + ".xlsx");

        // 解析 reportData JSON 为 List<List<String>> 两行结构（标题行 + 数值行）
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> dataMap = objectMapper.readValue(report.getReportData(), Map.class);

        List<List<String>> headList = new ArrayList<>();
        List<List<Object>> dataList = new ArrayList<>();
        List<Object> valueRow = new ArrayList<>();

        for (Map.Entry<String, Object> entry : dataMap.entrySet()) {
            List<String> headCol = new ArrayList<>();
            headCol.add(entry.getKey());
            headList.add(headCol);
            valueRow.add(entry.getValue());
        }
        dataList.add(valueRow);

        EasyExcel.write(response.getOutputStream())
                .head(headList)
                .sheet("报表数据")
                .doWrite(dataList);
    }
}
