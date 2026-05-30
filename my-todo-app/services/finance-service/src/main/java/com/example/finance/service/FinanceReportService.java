package com.example.finance.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.finance.entity.FinReport;

import java.time.LocalDate;
import java.util.List;

/**
 * 财务报表服务接口
 */
public interface FinanceReportService extends IService<FinReport> {

    FinReport generateIncomeStatement(Long tenantId, LocalDate startDate, LocalDate endDate, Long userId);

    FinReport generateBalanceSheet(Long tenantId, LocalDate asOfDate, Long userId);

    FinReport generateCashFlowStatement(Long tenantId, LocalDate startDate, LocalDate endDate, Long userId);

    List<FinReport> getReportList(Long tenantId, Integer reportType);

    void lockReport(Long id, Long userId);
}
