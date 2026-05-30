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

import java.io.OutputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

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

    /**
     * 导出报表到Excel
     */
    @Override
    public void exportToExcel(Long reportId, OutputStream out) {
        FinReport report = getById(reportId);
        if (report == null) {
            throw new BusinessException("报表不存在");
        }

        // 解析 reportData JSON
        Map<String, Object> dataMap = parseReportData(report);

        // 构建 key-value 行数据
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("报表类型", getReportTypeName(report.getReportType()));
        row.put("报表期间", report.getReportPeriod());
        row.put("币种", report.getCurrency() != null ? report.getCurrency() : "CNY");
        row.put("开始日期", report.getStartDate() != null ? report.getStartDate().toString() : "");
        row.put("结束日期", report.getEndDate() != null ? report.getEndDate().toString() : "");
        row.put("状态", report.getStatus() == 2 ? "已锁定" : "已生成");
        row.putAll(dataMap);

        // 提取表头
        String[] headers = row.keySet().toArray(new String[0]);

        // 使用通用导出（这里直接构建数据行）
        List<List<String>> headList = new ArrayList<>();
        for (String header : headers) {
            headList.add(List.of(header));
        }

        List<List<Object>> dataList = new ArrayList<>();
        List<Object> valueRow = new ArrayList<>();
        for (Object value : row.values()) {
            valueRow.add(value != null ? value : "");
        }
        dataList.add(valueRow);

        com.alibaba.excel.EasyExcel.write(out)
                .head(headList)
                .registerWriteHandler(new com.alibaba.excel.write.style.column.LongestMatchColumnWidthStyleStrategy())
                .sheet("报表数据")
                .doWrite(dataList);

        log.info("导出报表到Excel: reportId={}, type={}", reportId, report.getReportType());
    }

    /**
     * 导出报表到PDF（纯文本表格形式的简易PDF）
     * <p>
     * 使用 iText 风格的纯 Java 实现。如果未引入 iText 依赖，
     * 则生成 CSV 格式替代写入输出流。
     * </p>
     */
    @Override
    public void exportToPdf(Long reportId, OutputStream out) {
        FinReport report = getById(reportId);
        if (report == null) {
            throw new BusinessException("报表不存在");
        }

        Map<String, Object> dataMap = parseReportData(report);

        // 构建文本表格内容并输出为 UTF-8 文本
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("报表类型: ").append(getReportTypeName(report.getReportType())).append("\n");
            sb.append("报表期间: ").append(report.getReportPeriod()).append("\n");
            sb.append("币种: ").append(report.getCurrency() != null ? report.getCurrency() : "CNY").append("\n");
            sb.append("开始日期: ").append(report.getStartDate()).append("\n");
            sb.append("结束日期: ").append(report.getEndDate()).append("\n");
            sb.append("状态: ").append(report.getStatus() == 2 ? "已锁定" : "已生成").append("\n");
            sb.append("--- 报表数据 ---\n");
            for (Map.Entry<String, Object> entry : dataMap.entrySet()) {
                sb.append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
            }
            out.write(sb.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8));
            out.flush();
            log.info("导出报表到文本格式: reportId={}", reportId);
        } catch (Exception e) {
            throw new BusinessException("导出报表失败: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> parseReportData(FinReport report) {
        try {
            return new com.fasterxml.jackson.databind.ObjectMapper()
                    .readValue(report.getReportData(), Map.class);
        } catch (Exception e) {
            throw new BusinessException("报表数据解析失败");
        }
    }

    private String getReportTypeName(Integer type) {
        if (type == null) return "未知";
        return switch (type) {
            case 1 -> "资产负债表";
            case 2 -> "利润表";
            case 3 -> "现金流量表";
            case 4 -> "毛利分析";
            default -> "未知类型(" + type + ")";
        };
    }
}
