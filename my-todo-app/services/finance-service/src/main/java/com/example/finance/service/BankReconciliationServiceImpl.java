package com.example.finance.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.common.core.exception.BusinessException;
import com.example.finance.entity.FinBankReconciliation;
import com.example.finance.entity.FinBankRecord;
import com.example.finance.entity.PaymentRecord;
import com.example.finance.mapper.FinBankReconciliationMapper;
import com.example.finance.mapper.FinBankRecordMapper;
import com.example.finance.mapper.PaymentRecordMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 银行对账服务
 * <p>
 * 提供银行对账单导入、自动匹配、手动匹配、取消匹配等功能。
 * 自动匹配规则：相同金额 + 相近日期(±3天) + 描述相似。
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BankReconciliationServiceImpl extends ServiceImpl<FinBankReconciliationMapper, FinBankReconciliation> implements BankReconciliationService {

    private final FinBankRecordMapper bankRecordMapper;
    private final PaymentRecordMapper paymentRecordMapper;

    /** 日期匹配允许的偏差天数 */
    private static final long DATE_TOLERANCE_DAYS = 3;

    /**
     * 导入银行对账单（CSV格式）
     * <p>
     * CSV格式: 交易日期,金额,描述,参考号,交易类型(DEBIT/CREDIT)
     * </p>
     *
     * @param file          上传的CSV文件
     * @param bankAccountId 银行账户ID
     * @param tenantId      租户ID
     * @return 创建的对账记录
     */
    @Override
    @Transactional
    public FinBankReconciliation importBankStatement(MultipartFile file, Long bankAccountId, Long tenantId) {
        if (file.isEmpty()) {
            throw new BusinessException("上传文件不能为空");
        }

        String batchNo = UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        List<FinBankRecord> records = new ArrayList<>();
        LocalDate minDate = null;
        LocalDate maxDate = null;
        BigDecimal totalAmount = BigDecimal.ZERO;

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            boolean firstLine = true;
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

            while ((line = reader.readLine()) != null) {
                // 跳过表头
                if (firstLine) {
                    firstLine = false;
                    if (line.contains("交易日期") || line.toLowerCase().contains("date")) {
                        continue;
                    }
                }

                String[] fields = line.split(",");
                if (fields.length < 5) continue;

                FinBankRecord record = new FinBankRecord();
                record.setTenantId(tenantId);
                record.setBankAccountId(bankAccountId);
                record.setTransactionDate(LocalDate.parse(fields[0].trim(), formatter));
                record.setAmount(new BigDecimal(fields[1].trim()));
                record.setDescription(fields.length > 2 ? fields[2].trim() : "");
                record.setReferenceNo(fields.length > 3 ? fields[3].trim() : null);
                record.setTransactionType(fields.length > 4 ? fields[4].trim() : "CREDIT");
                record.setMatchStatus(0); // 未匹配
                record.setImportBatch(batchNo);
                record.setCreatedAt(LocalDateTime.now());
                record.setUpdatedAt(LocalDateTime.now());

                records.add(record);
                totalAmount = totalAmount.add(record.getAmount());

                if (minDate == null || record.getTransactionDate().isBefore(minDate)) {
                    minDate = record.getTransactionDate();
                }
                if (maxDate == null || record.getTransactionDate().isAfter(maxDate)) {
                    maxDate = record.getTransactionDate();
                }
            }
        } catch (Exception e) {
            log.error("解析银行对账单失败", e);
            throw new BusinessException("解析银行对账单失败: " + e.getMessage());
        }

        if (records.isEmpty()) {
            throw new BusinessException("对账单中无有效记录");
        }

        // 批量插入银行记录
        for (FinBankRecord record : records) {
            bankRecordMapper.insert(record);
        }

        // 创建对账记录
        FinBankReconciliation reconciliation = new FinBankReconciliation();
        reconciliation.setTenantId(tenantId);
        reconciliation.setBankAccountId(bankAccountId);
        reconciliation.setPeriodStart(minDate);
        reconciliation.setPeriodEnd(maxDate);
        reconciliation.setStatus(0); // 进行中
        reconciliation.setTotalBankAmount(totalAmount);
        reconciliation.setTotalSystemAmount(BigDecimal.ZERO);
        reconciliation.setMatchedCount(0);
        reconciliation.setUnmatchedCount(records.size());
        reconciliation.setCreatedAt(LocalDateTime.now());
        reconciliation.setUpdatedAt(LocalDateTime.now());
        save(reconciliation);

        log.info("导入银行对账单: batchNo={}, 记录数={}, 对账ID={}", batchNo, records.size(), reconciliation.getId());
        return reconciliation;
    }

    /**
     * 自动匹配
     * <p>
     * 匹配规则：相同金额 + 日期相近(±3天) + 描述相似
     * </p>
     *
     * @param reconciliationId 对账记录ID
     * @return 匹配的记录数
     */
    @Override
    @Transactional
    public int autoMatch(Long reconciliationId) {
        FinBankReconciliation reconciliation = getById(reconciliationId);
        if (reconciliation == null) {
            throw new BusinessException("对账记录不存在");
        }

        // 查询该对账期间内未匹配的银行记录
        List<FinBankRecord> unmatchedBankRecords = bankRecordMapper.selectList(
                new LambdaQueryWrapper<FinBankRecord>()
                        .eq(FinBankRecord::getTenantId, reconciliation.getTenantId())
                        .eq(FinBankRecord::getBankAccountId, reconciliation.getBankAccountId())
                        .eq(FinBankRecord::getMatchStatus, 0)
                        .ge(FinBankRecord::getTransactionDate, reconciliation.getPeriodStart())
                        .le(FinBankRecord::getTransactionDate, reconciliation.getPeriodEnd()));

        // 查询该期间的系统收支记录
        List<PaymentRecord> systemRecords = paymentRecordMapper.selectList(
                new LambdaQueryWrapper<PaymentRecord>()
                        .eq(PaymentRecord::getTenantId, reconciliation.getTenantId())
                        .eq(PaymentRecord::getBankAccountId, reconciliation.getBankAccountId())
                        .eq(PaymentRecord::getStatus, 1)); // 已审核

        int matchedCount = 0;
        for (FinBankRecord bankRecord : unmatchedBankRecords) {
            PaymentRecord bestMatch = null;
            long bestScore = 0;

            for (PaymentRecord sysRecord : systemRecords) {
                long score = calculateMatchScore(bankRecord, sysRecord);
                if (score > bestScore && score >= 2) { // 至少金额+日期匹配
                    bestScore = score;
                    bestMatch = sysRecord;
                }
            }

            if (bestMatch != null) {
                bankRecord.setMatchStatus(1); // 自动匹配
                bankRecord.setMatchedRecordId(bestMatch.getId());
                bankRecord.setUpdatedAt(LocalDateTime.now());
                bankRecordMapper.updateById(bankRecord);
                matchedCount++;

                // 从候选列表移除已匹配的系统记录
                systemRecords.remove(bestMatch);
            }
        }

        // 更新对账统计
        updateReconciliationStats(reconciliation, matchedCount);

        log.info("自动匹配完成: reconciliationId={}, 新增匹配={}", reconciliationId, matchedCount);
        return matchedCount;
    }

    /**
     * 计算匹配分数
     * <p>
     * 金额完全一致: +2分
     * 日期在±3天内: +1分
     * 描述包含关键字: +1分
     * </p>
     */
    private long calculateMatchScore(FinBankRecord bankRecord, PaymentRecord sysRecord) {
        long score = 0;

        // 金额匹配（精确到分）
        if (bankRecord.getAmount().compareTo(sysRecord.getAmount()) == 0) {
            score += 2;
        }

        // 日期匹配（±3天内）
        if (sysRecord.getTransactionDate() != null && bankRecord.getTransactionDate() != null) {
            LocalDate sysDate = sysRecord.getTransactionDate().toLocalDate();
            long daysDiff = Math.abs(java.time.temporal.ChronoUnit.DAYS.between(
                    bankRecord.getTransactionDate(), sysDate));
            if (daysDiff <= DATE_TOLERANCE_DAYS) {
                score += 1;
            }
        }

        // 描述相似（简单的包含检查）
        if (bankRecord.getDescription() != null && sysRecord.getRemark() != null) {
            String desc = bankRecord.getDescription().toLowerCase();
            String remark = sysRecord.getRemark().toLowerCase();
            if (desc.contains(remark) || remark.contains(desc)) {
                score += 1;
            } else if (desc.length() >= 3 && remark.contains(desc.substring(0, Math.min(3, desc.length())))) {
                score += 1;
            }
        }

        return score;
    }

    /**
     * 手动匹配
     *
     * @param bankRecordId   银行记录ID
     * @param systemRecordId 系统记录ID
     */
    @Override
    @Transactional
    public void manualMatch(Long bankRecordId, Long systemRecordId) {
        FinBankRecord bankRecord = bankRecordMapper.selectById(bankRecordId);
        if (bankRecord == null) {
            throw new BusinessException("银行记录不存在");
        }
        if (bankRecord.getMatchStatus() != 0 && bankRecord.getMatchStatus() != 3) {
            throw new BusinessException("该银行记录已匹配，不能重复匹配");
        }

        PaymentRecord sysRecord = paymentRecordMapper.selectById(systemRecordId);
        if (sysRecord == null) {
            throw new BusinessException("系统记录不存在");
        }

        bankRecord.setMatchStatus(2); // 手动匹配
        bankRecord.setMatchedRecordId(systemRecordId);
        bankRecord.setUpdatedAt(LocalDateTime.now());
        bankRecordMapper.updateById(bankRecord);

        log.info("手动匹配: bankRecordId={}, systemRecordId={}", bankRecordId, systemRecordId);
    }

    /**
     * 取消匹配
     *
     * @param bankRecordId 银行记录ID
     */
    @Override
    @Transactional
    public void unmatch(Long bankRecordId) {
        FinBankRecord bankRecord = bankRecordMapper.selectById(bankRecordId);
        if (bankRecord == null) {
            throw new BusinessException("银行记录不存在");
        }
        if (bankRecord.getMatchStatus() == 0) {
            throw new BusinessException("该记录未匹配，无需取消");
        }

        bankRecord.setMatchStatus(0); // 恢复未匹配
        bankRecord.setMatchedRecordId(null);
        bankRecord.setUpdatedAt(LocalDateTime.now());
        bankRecordMapper.updateById(bankRecord);

        log.info("取消匹配: bankRecordId={}", bankRecordId);
    }

    /**
     * 分页查询对账列表
     */
    @Override
    public Page<FinBankReconciliation> getReconciliationPage(Long tenantId, int page, int size) {
        return page(new Page<>(page, size),
                new LambdaQueryWrapper<FinBankReconciliation>()
                        .eq(FinBankReconciliation::getTenantId, tenantId)
                        .orderByDesc(FinBankReconciliation::getCreatedAt));
    }

    /**
     * 获取未匹配记录
     *
     * @param reconciliationId 对账记录ID
     * @return 未匹配的银行记录列表
     */
    @Override
    public List<FinBankRecord> getUnmatchedRecords(Long reconciliationId) {
        FinBankReconciliation reconciliation = getById(reconciliationId);
        if (reconciliation == null) {
            throw new BusinessException("对账记录不存在");
        }

        return bankRecordMapper.selectList(
                new LambdaQueryWrapper<FinBankRecord>()
                        .eq(FinBankRecord::getTenantId, reconciliation.getTenantId())
                        .eq(FinBankRecord::getBankAccountId, reconciliation.getBankAccountId())
                        .eq(FinBankRecord::getMatchStatus, 0)
                        .ge(FinBankRecord::getTransactionDate, reconciliation.getPeriodStart())
                        .le(FinBankRecord::getTransactionDate, reconciliation.getPeriodEnd()));
    }

    /**
     * 更新对账统计信息
     */
    private void updateReconciliationStats(FinBankReconciliation reconciliation, int newMatchedCount) {
        // 重新统计
        Long matchedCount = bankRecordMapper.selectCount(
                new LambdaQueryWrapper<FinBankRecord>()
                        .eq(FinBankRecord::getTenantId, reconciliation.getTenantId())
                        .eq(FinBankRecord::getBankAccountId, reconciliation.getBankAccountId())
                        .in(FinBankRecord::getMatchStatus, 1, 2));

        Long unmatchedCount = bankRecordMapper.selectCount(
                new LambdaQueryWrapper<FinBankRecord>()
                        .eq(FinBankRecord::getTenantId, reconciliation.getTenantId())
                        .eq(FinBankRecord::getBankAccountId, reconciliation.getBankAccountId())
                        .eq(FinBankRecord::getMatchStatus, 0)
                        .ge(FinBankRecord::getTransactionDate, reconciliation.getPeriodStart())
                        .le(FinBankRecord::getTransactionDate, reconciliation.getPeriodEnd()));

        reconciliation.setMatchedCount(matchedCount.intValue());
        reconciliation.setUnmatchedCount(unmatchedCount.intValue());
        reconciliation.setUpdatedAt(LocalDateTime.now());

        // 如果全部匹配完成，自动标记为已完成
        if (unmatchedCount == 0) {
            reconciliation.setStatus(1);
        }

        updateById(reconciliation);
    }
}
