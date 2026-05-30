package com.example.finance.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.finance.entity.FinBankReconciliation;
import com.example.finance.entity.FinBankRecord;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 银行对账服务接口
 */
public interface BankReconciliationService extends IService<FinBankReconciliation> {

    FinBankReconciliation importBankStatement(MultipartFile file, Long bankAccountId, Long tenantId);

    int autoMatch(Long reconciliationId);

    void manualMatch(Long bankRecordId, Long systemRecordId);

    void unmatch(Long bankRecordId);

    Page<FinBankReconciliation> getReconciliationPage(Long tenantId, int page, int size);

    List<FinBankRecord> getUnmatchedRecords(Long reconciliationId);
}
