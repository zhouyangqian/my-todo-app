package com.example.finance.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.common.core.exception.BusinessException;
import com.example.finance.entity.PaymentRecord;
import com.example.finance.mapper.PaymentRecordMapper;
import com.example.finance.vo.TransferVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

/**
 * 收支记录业务服务类
 * <p>
 * 继承 MyBatis-Plus 的 ServiceImpl，提供收支记录的完整业务逻辑，
 * 包括分页查询、创建、审核、取消以及单据编号自动生成。
 * 审核通过后会自动调用 BankAccountService 调整对应银行账户的余额。
 * 所有数据库写操作均使用 @Transactional 注解保证事务一致性。
 * </p>
 *
 * @author finance-team
 * @since 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentRecordServiceImpl extends ServiceImpl<PaymentRecordMapper, PaymentRecord> implements PaymentRecordService {

    /** 银行账户服务，用于审核收支记录时调整账户余额 */
    private final BankAccountService bankAccountService;

    /**
     * 分页查询收支记录列表
     *
     * @param tenantId   租户ID，用于多租户数据隔离
     * @param page       当前页码（从1开始）
     * @param size       每页记录数
     * @param recordType 收支类型（可选），1-收入, 2-支出
     * @param bizType    业务类型（可选），1-销售收款, 2-采购付款, 3-退款, 4-其他收入, 5-其他支出
     * @param startDate  交易日期起始范围（可选）
     * @param endDate    交易日期结束范围（可选）
     * @return 分页结果对象，包含符合条件的收支记录列表及分页信息
     */
    @Override
    public Page<PaymentRecord> getPage(Long tenantId, int page, int size,
                                        Integer recordType, Integer bizType,
                                        LocalDateTime startDate, LocalDateTime endDate) {
        // 构建查询条件：限定租户 + 未删除
        LambdaQueryWrapper<PaymentRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PaymentRecord::getTenantId, tenantId)
               .eq(PaymentRecord::getDeleted, 0);
        // 可选过滤：按收支类型筛选（收入/支出）
        if (recordType != null) {
            wrapper.eq(PaymentRecord::getRecordType, recordType);
        }
        // 可选过滤：按业务类型筛选
        if (bizType != null) {
            wrapper.eq(PaymentRecord::getBizType, bizType);
        }
        // 可选过滤：交易日期起始范围
        if (startDate != null) {
            wrapper.ge(PaymentRecord::getTransactionDate, startDate);
        }
        // 可选过滤：交易日期结束范围
        if (endDate != null) {
            wrapper.le(PaymentRecord::getTransactionDate, endDate);
        }
        // 按交易日期倒序排列，最新的记录排在前面
        wrapper.orderByDesc(PaymentRecord::getTransactionDate);
        return page(new Page<>(page, size), wrapper);
    }

    /**
     * 创建收支记录
     *
     * @param record 收支记录实体对象（需包含 tenantId, recordType, amount, bankAccountId 等字段）
     * @return 创建成功后的收支记录对象（含自动生成的单据编号和ID）
     */
    @Override
    @Transactional
    public PaymentRecord create(PaymentRecord record) {
        // 根据收支类型生成对应的单据编号（SK收款/FK付款）
        record.setRecordNo(generateRecordNo(record.getRecordType()));
        // 初始状态为待审核
        record.setStatus(0);
        save(record);
        log.info("创建收支记录: {}, 金额={}", record.getRecordNo(), record.getAmount());
        return record;
    }

    /**
     * 审核收支记录
     *
     * @param id          收支记录ID
     * @param approverId  审核人ID
     * @throws IllegalArgumentException 当收支记录不存在时抛出
     * @throws IllegalStateException    当记录状态不是"待审核"时抛出
     */
    @Override
    @Transactional
    public void approve(Long id, Long approverId) {
        PaymentRecord record = getById(id);
        if (record == null) {
            throw new IllegalArgumentException("收支记录不存在");
        }
        // 校验状态：只有待审核状态的记录才能审核
        if (record.getStatus() != 0) {
            throw new IllegalStateException("只能审核待审核状态的记录");
        }

        // 更新状态为已审核
        record.setStatus(1);
        updateById(record);

        // 审核通过后，自动调整关联银行账户的余额
        if (record.getBankAccountId() != null) {
            bankAccountService.adjustBalance(record.getBankAccountId(), record.getAmount(),
                    record.getRecordType() == 1); // 收入类型(recordType==1)增加余额，支出类型减少余额
        }

        log.info("审核收支记录: {}", record.getRecordNo());
    }

    /**
     * 取消收支记录
     *
     * @param id 收支记录ID
     * @throws IllegalArgumentException 当收支记录不存在时抛出
     */
    @Override
    @Transactional
    public void cancel(Long id) {
        PaymentRecord record = getById(id);
        if (record == null) {
            throw new IllegalArgumentException("收支记录不存在");
        }
        // 如果记录已审核通过，需要先回滚账户余额（反向操作）
        if (record.getStatus() == 1) {
            if (record.getBankAccountId() != null) {
                // 反向调整：原来是收入则减少余额，原来是支出则增加余额
                bankAccountService.adjustBalance(record.getBankAccountId(), record.getAmount(),
                        record.getRecordType() != 1);
            }
        }
        // 更新状态为已取消
        record.setStatus(2);
        updateById(record);
        log.info("取消收支记录: {}", record.getRecordNo());
    }

    /**
     * 生成收支单据编号
     *
     * @param recordType 收支类型，1-收入生成SK前缀，2-支出生成FK前缀
     * @return 生成的唯一单据编号
     */
    private String generateRecordNo(Integer recordType) {
        String prefix = recordType == 1 ? "SK" : "FK"; // SK=收款, FK=付款
        String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String random = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        return prefix + dateStr + random;
    }

    /**
     * 按日期范围查询已审核的收支记录（用于报表生成）
     *
     * @param tenantId   租户ID
     * @param recordType 收支类型: 1-收入, 2-支出
     * @param startDate  开始日期
     * @param endDate    结束日期
     * @return 符合条件的已审核收支记录列表
     */
    @Override
    public List<PaymentRecord> getByDateRange(Long tenantId, Integer recordType,
                                               LocalDate startDate, LocalDate endDate) {
        LambdaQueryWrapper<PaymentRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PaymentRecord::getTenantId, tenantId)
               .eq(PaymentRecord::getDeleted, 0)
               .eq(PaymentRecord::getStatus, 1) // 已审核
               .eq(recordType != null, PaymentRecord::getRecordType, recordType)
               .ge(PaymentRecord::getTransactionDate, startDate.atStartOfDay())
               .le(PaymentRecord::getTransactionDate, endDate.atTime(23, 59, 59))
               .orderByDesc(PaymentRecord::getTransactionDate);
        return list(wrapper);
    }

    /**
     * 账户间转账
     * <p>
     * 在同一事务内创建两条收支记录（转出+转入），通过 relatedTransId 互相关联。
     * 任何一步失败将整体回滚，保证数据一致性。
     * </p>
     *
     * @param transferVO 转账请求参数
     * @param tenantId   租户ID
     * @param userId     当前操作用户ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void transfer(TransferVO transferVO, Long tenantId, Long userId) {
        if (transferVO.getFromAccountId().equals(transferVO.getToAccountId())) {
            throw new BusinessException("转出账户与转入账户不能相同");
        }
        if (transferVO.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("转账金额必须大于零");
        }

        BigDecimal exchangeRate = transferVO.getExchangeRate() != null
                ? transferVO.getExchangeRate() : BigDecimal.ONE;

        // 构建转出记录（EXPENSE）
        PaymentRecord expenseRecord = new PaymentRecord();
        expenseRecord.setTenantId(tenantId);
        expenseRecord.setCreatedBy(userId);
        expenseRecord.setHandlerId(userId);
        expenseRecord.setRecordType(2); // 支出
        expenseRecord.setTransType("EXPENSE");
        expenseRecord.setAmount(transferVO.getAmount());
        expenseRecord.setCurrency(transferVO.getCurrency());
        expenseRecord.setBankAccountId(transferVO.getFromAccountId());
        expenseRecord.setExchangeRate(exchangeRate);
        expenseRecord.setBaseAmount(transferVO.getAmount().multiply(exchangeRate));
        expenseRecord.setSourceType("MANUAL");
        expenseRecord.setRemark(transferVO.getRemark() != null ? transferVO.getRemark() : "账户转账-转出");
        create(expenseRecord);

        // 构建转入记录（INCOME）
        PaymentRecord incomeRecord = new PaymentRecord();
        incomeRecord.setTenantId(tenantId);
        incomeRecord.setCreatedBy(userId);
        incomeRecord.setHandlerId(userId);
        incomeRecord.setRecordType(1); // 收入
        incomeRecord.setTransType("INCOME");
        incomeRecord.setAmount(transferVO.getAmount());
        incomeRecord.setCurrency(transferVO.getCurrency());
        incomeRecord.setBankAccountId(transferVO.getToAccountId());
        incomeRecord.setExchangeRate(exchangeRate);
        incomeRecord.setBaseAmount(transferVO.getAmount().multiply(exchangeRate));
        incomeRecord.setSourceType("MANUAL");
        incomeRecord.setRemark(transferVO.getRemark() != null ? transferVO.getRemark() : "账户转账-转入");
        create(incomeRecord);

        // 互相关联
        expenseRecord.setRelatedTransId(incomeRecord.getId());
        incomeRecord.setRelatedTransId(expenseRecord.getId());
        updateById(expenseRecord);
        updateById(incomeRecord);

        log.info("账户转账完成: 从账户{}转入账户{}, 金额={}", transferVO.getFromAccountId(), transferVO.getToAccountId(), transferVO.getAmount());
    }
}
