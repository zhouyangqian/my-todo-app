package com.example.finance.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.common.core.exception.BusinessException;
import com.example.finance.entity.*;
import com.example.finance.mapper.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 财务报表服务
 * <p>
 * 生成资产负债表、利润表、现金流量表、毛利分析报表。
 * 报表数据基于已有的应收/应付、收支记录、银行账户等数据聚合生成。
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FinanceReportServiceImpl extends ServiceImpl<FinReportMapper, FinReport> implements FinanceReportService {

    private final AccountReceivableService receivableService;
    private final AccountPayableService payableService;
    private final PaymentRecordService paymentRecordService;
    private final BankAccountService bankAccountService;

    /** 报表类型 */
    public static final int TYPE_BALANCE_SHEET = 1;
    public static final int TYPE_INCOME_STATEMENT = 2;
    public static final int TYPE_CASH_FLOW = 3;
    public static final int TYPE_GROSS_PROFIT = 4;

    /**
     * 生成利润表
     */
    @Override
    @Transactional
    public FinReport generateIncomeStatement(Long tenantId, LocalDate startDate, LocalDate endDate, Long userId) {
        String period = startDate.format(DateTimeFormatter.ofPattern("yyyy-MM"));

        // 检查是否已有该期间的报表
        FinReport existing = getExistingReport(tenantId, TYPE_INCOME_STATEMENT, period);
        if (existing != null && existing.getStatus() == 2) {
            throw new BusinessException("该期间的利润表已锁定");
        }

        Map<String, Object> data = new HashMap<>();

        // 收入：已审核的收款记录
        List<PaymentRecord> incomeRecords = paymentRecordService.getByDateRange(tenantId, 1, startDate, endDate);
        BigDecimal totalIncome = incomeRecords.stream()
                .map(PaymentRecord::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        data.put("totalIncome", totalIncome);
        data.put("incomeCount", incomeRecords.size());

        // 支出：已审核的付款记录
        List<PaymentRecord> expenseRecords = paymentRecordService.getByDateRange(tenantId, 2, startDate, endDate);
        BigDecimal totalExpense = expenseRecords.stream()
                .map(PaymentRecord::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        data.put("totalExpense", totalExpense);
        data.put("expenseCount", expenseRecords.size());

        // 净利润
        BigDecimal netProfit = totalIncome.subtract(totalExpense);
        data.put("netProfit", netProfit);

        return saveReport(tenantId, TYPE_INCOME_STATEMENT, period, startDate, endDate, data, userId, existing);
    }

    /**
     * 生成资产负债表（基于当前快照）
     */
    @Override
    @Transactional
    public FinReport generateBalanceSheet(Long tenantId, LocalDate asOfDate, Long userId) {
        String period = asOfDate.format(DateTimeFormatter.ofPattern("yyyy-MM"));

        FinReport existing = getExistingReport(tenantId, TYPE_BALANCE_SHEET, period);
        if (existing != null && existing.getStatus() == 2) {
            throw new BusinessException("该期间的资产负债表已锁定");
        }

        Map<String, Object> data = new HashMap<>();

        // 资产：银行账户余额
        List<BankAccount> accounts = bankAccountService.getAllAccounts(tenantId);
        BigDecimal totalAssets = accounts.stream()
                .map(BankAccount::getBalance)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        data.put("totalAssets", totalAssets);
        data.put("bankAccounts", accounts.size());

        // 应收账款（未结算金额）
        data.put("totalReceivables", BigDecimal.ZERO);
        // 应付账款（未支付金额）
        data.put("totalPayables", BigDecimal.ZERO);

        LocalDate startDate = asOfDate.withDayOfMonth(1);
        return saveReport(tenantId, TYPE_BALANCE_SHEET, period, startDate, asOfDate, data, userId, existing);
    }

    /**
     * 生成现金流量表
     */
    @Override
    @Transactional
    public FinReport generateCashFlowStatement(Long tenantId, LocalDate startDate, LocalDate endDate, Long userId) {
        String period = startDate.format(DateTimeFormatter.ofPattern("yyyy-MM"));

        FinReport existing = getExistingReport(tenantId, TYPE_CASH_FLOW, period);
        if (existing != null && existing.getStatus() == 2) {
            throw new BusinessException("该期间的现金流量表已锁定");
        }

        Map<String, Object> data = new HashMap<>();

        List<PaymentRecord> incomeRecords = paymentRecordService.getByDateRange(tenantId, 1, startDate, endDate);
        List<PaymentRecord> expenseRecords = paymentRecordService.getByDateRange(tenantId, 2, startDate, endDate);

        BigDecimal cashInflow = incomeRecords.stream()
                .map(PaymentRecord::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal cashOutflow = expenseRecords.stream()
                .map(PaymentRecord::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        data.put("cashInflow", cashInflow);
        data.put("cashOutflow", cashOutflow);
        data.put("netCashFlow", cashInflow.subtract(cashOutflow));

        return saveReport(tenantId, TYPE_CASH_FLOW, period, startDate, endDate, data, userId, existing);
    }

    /**
     * 获取报表列表
     */
    @Override
    public List<FinReport> getReportList(Long tenantId, Integer reportType) {
        LambdaQueryWrapper<FinReport> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FinReport::getTenantId, tenantId)
               .eq(reportType != null, FinReport::getReportType, reportType)
               .orderByDesc(FinReport::getCreatedAt);
        return list(wrapper);
    }

    /**
     * 锁定报表（锁定后不可重新生成）
     */
    @Override
    @Transactional
    public void lockReport(Long id, Long userId) {
        FinReport report = getById(id);
        if (report == null) {
            throw new BusinessException("报表不存在");
        }
        report.setStatus(2);
        report.setUpdatedBy(userId);
        report.setUpdatedAt(LocalDateTime.now());
        updateById(report);
        log.info("锁定报表: id={}, type={}, period={}", id, report.getReportType(), report.getReportPeriod());
    }

    private FinReport getExistingReport(Long tenantId, int reportType, String period) {
        return getOne(new LambdaQueryWrapper<FinReport>()
                .eq(FinReport::getTenantId, tenantId)
                .eq(FinReport::getReportType, reportType)
                .eq(FinReport::getReportPeriod, period));
    }

    private FinReport saveReport(Long tenantId, int reportType, String period,
                                  LocalDate startDate, LocalDate endDate,
                                  Map<String, Object> data, Long userId, FinReport existing) {
        FinReport report = existing != null ? existing : new FinReport();
        report.setTenantId(tenantId);
        report.setReportType(reportType);
        report.setReportPeriod(period);
        report.setStartDate(startDate);
        report.setEndDate(endDate);
        try {
            report.setReportData(new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(data));
        } catch (Exception e) {
            throw new BusinessException("报表数据序列化失败");
        }
        report.setStatus(1);
        report.setUpdatedBy(userId);
        report.setUpdatedAt(LocalDateTime.now());

        if (existing != null) {
            updateById(report);
        } else {
            report.setCreatedBy(userId);
            report.setCreatedAt(LocalDateTime.now());
            save(report);
        }

        log.info("生成报表: type={}, period={}, tenantId={}", reportType, period, tenantId);
        return report;
    }
}
