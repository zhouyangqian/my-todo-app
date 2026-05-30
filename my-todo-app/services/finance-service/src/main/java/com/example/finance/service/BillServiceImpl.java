package com.example.finance.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.finance.entity.Bill;
import com.example.finance.entity.BillInvoice;
import com.example.finance.entity.Invoice;
import com.example.finance.entity.PaymentRecord;
import com.example.finance.interceptor.BudgetControlInterceptor;
import com.example.finance.mapper.BillInvoiceMapper;
import com.example.finance.mapper.BillMapper;
import com.example.finance.mapper.InvoiceMapper;
import com.example.finance.mapper.PaymentRecordMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 统一账单业务服务类
 * <p>
 * 继承 MyBatis-Plus 的 ServiceImpl，提供统一账单的完整生命周期管理，
 * 包括创建、编辑、提交审核、审核通过/驳回、取消、收付查询和汇总统计。
 * 同时支持从采购订单和销售订单自动创建应付/应收账单。
 * </p>
 *
 * @author finance-team
 * @since 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BillServiceImpl extends ServiceImpl<BillMapper, Bill> implements BillService {

    private final BillInvoiceMapper billInvoiceMapper;
    private final InvoiceMapper invoiceMapper;
    private final PaymentRecordMapper paymentRecordMapper;
    private final BudgetControlInterceptor budgetControlInterceptor;

    /** 状态常量 */
    private static final int STATUS_DRAFT = 0;
    private static final int STATUS_PENDING_APPROVAL = 1;
    private static final int STATUS_APPROVED = 2;
    private static final int STATUS_PARTIAL_PAID = 3;
    private static final int STATUS_COMPLETED = 4;
    private static final int STATUS_CANCELLED = 5;

    /** 审核状态常量 */
    private static final int AUDIT_NONE = 0;
    private static final int AUDIT_PENDING = 1;
    private static final int AUDIT_APPROVED = 2;
    private static final int AUDIT_REJECTED = 3;

    /** 账单类型 */
    private static final int BILL_TYPE_RECEIVABLE = 1;
    private static final int BILL_TYPE_PAYABLE = 2;

    /**
     * 创建账单
     *
     * @param bill 账单数据
     * @return 创建后的账单对象
     */
    @Override
    @Transactional
    public Bill createBill(Bill bill) {
        // 应付账单检查预算
        if (bill.getBillType() != null && bill.getBillType() == BILL_TYPE_PAYABLE
                && bill.getAmount() != null && bill.getTenantId() != null) {
            budgetControlInterceptor.checkBudgetBeforePayment(
                    bill.getTenantId(), null, bill.getAmount());
        }

        bill.setBillNo(generateBillNo(bill.getBillType()));
        if (bill.getDirection() == null) {
            bill.setDirection(1);
        }
        if (bill.getCurrency() == null) {
            bill.setCurrency("CNY");
        }
        if (bill.getExchangeRate() == null) {
            bill.setExchangeRate(BigDecimal.ONE);
        }
        if (bill.getPaidAmount() == null) {
            bill.setPaidAmount(BigDecimal.ZERO);
        }
        // 计算本位币金额
        if (bill.getAmount() != null && bill.getExchangeRate() != null) {
            bill.setBaseAmount(bill.getAmount().multiply(bill.getExchangeRate()));
        }
        bill.setStatus(STATUS_DRAFT);
        bill.setAuditStatus(AUDIT_NONE);
        save(bill);
        log.info("创建账单: billNo={}, type={}, amount={}", bill.getBillNo(), bill.getBillType(), bill.getAmount());
        return bill;
    }

    /**
     * 更新账单（仅草稿状态可编辑）
     *
     * @param bill 账单数据
     * @return 更新后的账单对象
     */
    @Override
    @Transactional
    public Bill updateBill(Bill bill) {
        Bill existing = getById(bill.getId());
        if (existing == null) {
            throw new IllegalArgumentException("账单不存在");
        }
        if (existing.getStatus() != STATUS_DRAFT) {
            throw new IllegalStateException("仅草稿状态的账单允许编辑");
        }
        // 重新计算本位币金额
        if (bill.getAmount() != null && bill.getExchangeRate() != null) {
            bill.setBaseAmount(bill.getAmount().multiply(bill.getExchangeRate()));
        }
        updateById(bill);
        log.info("更新账单: id={}", bill.getId());
        return getById(bill.getId());
    }

    /**
     * 分页查询账单列表
     *
     * @param tenantId    租户ID
     * @param page        当前页码
     * @param size        每页记录数
     * @param billType    账单类型（可选）
     * @param status      状态（可选）
     * @param partnerType 往来单位类型（可选）
     * @param partnerId   往来单位ID（可选）
     * @param startDate   账单日期起始范围（可选）
     * @param endDate     账单日期结束范围（可选）
     * @param keyword     关键词搜索（可选，匹配账单编号/来源单号/往来单位名称）
     * @return 分页结果
     */
    @Override
    public Page<Bill> getBillPage(Long tenantId, int page, int size,
                                  Integer billType, Integer status,
                                  Integer partnerType, Long partnerId,
                                  LocalDate startDate, LocalDate endDate,
                                  String keyword) {
        LambdaQueryWrapper<Bill> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Bill::getTenantId, tenantId);
        if (billType != null) {
            wrapper.eq(Bill::getBillType, billType);
        }
        if (status != null) {
            wrapper.eq(Bill::getStatus, status);
        }
        if (partnerType != null) {
            wrapper.eq(Bill::getPartnerType, partnerType);
        }
        if (partnerId != null) {
            wrapper.eq(Bill::getPartnerId, partnerId);
        }
        if (startDate != null) {
            wrapper.ge(Bill::getBillDate, startDate);
        }
        if (endDate != null) {
            wrapper.le(Bill::getBillDate, endDate);
        }
        if (keyword != null && !keyword.trim().isEmpty()) {
            wrapper.and(w -> w
                    .like(Bill::getBillNo, keyword)
                    .or().like(Bill::getSourceNo, keyword)
                    .or().like(Bill::getPartnerName, keyword)
            );
        }
        wrapper.orderByDesc(Bill::getCreatedAt);
        return page(new Page<>(page, size), wrapper);
    }

    /**
     * 根据ID查询账单详情
     *
     * @param id 账单ID
     * @return 账单对象
     */
    @Override
    public Bill getBillById(Long id) {
        Bill bill = getById(id);
        if (bill == null) {
            throw new IllegalArgumentException("账单不存在");
        }
        return bill;
    }

    /**
     * 提交账单（草稿 -> 待审核）
     *
     * @param id 账单ID
     */
    @Override
    @Transactional
    public void submitBill(Long id) {
        Bill bill = getById(id);
        if (bill == null) {
            throw new IllegalArgumentException("账单不存在");
        }
        if (bill.getStatus() != STATUS_DRAFT) {
            throw new IllegalStateException("仅草稿状态的账单允许提交");
        }
        bill.setStatus(STATUS_PENDING_APPROVAL);
        bill.setAuditStatus(AUDIT_PENDING);
        updateById(bill);
        log.info("提交账单: billNo={}", bill.getBillNo());
    }

    /**
     * 审核通过账单（待审核 -> 已审核）
     *
     * @param id          账单ID
     * @param auditBy     审核人ID
     * @param auditRemark 审核备注（可选）
     */
    @Override
    @Transactional
    public void approveBill(Long id, Long auditBy, String auditRemark) {
        Bill bill = getById(id);
        if (bill == null) {
            throw new IllegalArgumentException("账单不存在");
        }
        if (bill.getStatus() != STATUS_PENDING_APPROVAL) {
            throw new IllegalStateException("仅待审核状态的账单允许审核");
        }
        bill.setStatus(STATUS_APPROVED);
        bill.setAuditStatus(AUDIT_APPROVED);
        bill.setAuditBy(auditBy);
        bill.setAuditAt(LocalDateTime.now());
        bill.setAuditRemark(auditRemark);
        updateById(bill);
        log.info("审核通过账单: billNo={}, auditBy={}", bill.getBillNo(), auditBy);
    }

    /**
     * 驳回账单（待审核 -> 草稿）
     *
     * @param id          账单ID
     * @param auditBy     审核人ID
     * @param auditRemark 驳回原因
     */
    @Override
    @Transactional
    public void rejectBill(Long id, Long auditBy, String auditRemark) {
        Bill bill = getById(id);
        if (bill == null) {
            throw new IllegalArgumentException("账单不存在");
        }
        if (bill.getStatus() != STATUS_PENDING_APPROVAL) {
            throw new IllegalStateException("仅待审核状态的账单允许驳回");
        }
        bill.setStatus(STATUS_DRAFT);
        bill.setAuditStatus(AUDIT_REJECTED);
        bill.setAuditBy(auditBy);
        bill.setAuditAt(LocalDateTime.now());
        bill.setAuditRemark(auditRemark);
        updateById(bill);
        log.info("驳回账单: billNo={}, reason={}", bill.getBillNo(), auditRemark);
    }

    /**
     * 取消账单
     * <p>
     * 草稿或待审核状态可直接取消；已审核/部分收付状态不允许取消。
     * </p>
     *
     * @param id 账单ID
     */
    @Override
    @Transactional
    public void cancelBill(Long id) {
        Bill bill = getById(id);
        if (bill == null) {
            throw new IllegalArgumentException("账单不存在");
        }
        if (bill.getStatus() != STATUS_DRAFT && bill.getStatus() != STATUS_PENDING_APPROVAL) {
            throw new IllegalStateException("仅草稿或待审核状态的账单允许取消");
        }
        bill.setStatus(STATUS_CANCELLED);
        bill.setAuditStatus(AUDIT_NONE);
        updateById(bill);
        log.info("取消账单: billNo={}", bill.getBillNo());
    }

    /**
     * 获取账单关联的收支记录
     *
     * @param id       账单ID
     * @param tenantId 租户ID
     * @return 收支记录列表
     */
    @Override
    public List<PaymentRecord> getBillPayments(Long id, Long tenantId) {
        Bill bill = getById(id);
        if (bill == null) {
            throw new IllegalArgumentException("账单不存在");
        }
        LambdaQueryWrapper<PaymentRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PaymentRecord::getTenantId, tenantId)
               .eq(PaymentRecord::getDeleted, 0);
        // 通过来源类型和来源ID关联收支记录
        wrapper.eq(PaymentRecord::getBizType, bill.getBillType() == BILL_TYPE_RECEIVABLE ? 1 : 2);
        wrapper.eq(PaymentRecord::getBankAccountId, bill.getAccountId());
        wrapper.orderByDesc(PaymentRecord::getTransactionDate);
        return paymentRecordMapper.selectList(wrapper);
    }

    /**
     * 获取账单关联的发票列表
     *
     * @param id 账单ID
     * @return 发票列表
     */
    @Override
    public List<Invoice> getBillInvoices(Long id) {
        Bill bill = getById(id);
        if (bill == null) {
            throw new IllegalArgumentException("账单不存在");
        }
        // 查询关联关系
        LambdaQueryWrapper<BillInvoice> biWrapper = new LambdaQueryWrapper<>();
        biWrapper.eq(BillInvoice::getBillId, id)
                 .eq(BillInvoice::getDeleted, 0);
        List<BillInvoice> billInvoices = billInvoiceMapper.selectList(biWrapper);
        if (billInvoices.isEmpty()) {
            return List.of();
        }
        List<Long> invoiceIds = billInvoices.stream().map(BillInvoice::getInvoiceId).toList();
        LambdaQueryWrapper<Invoice> invWrapper = new LambdaQueryWrapper<>();
        invWrapper.in(Invoice::getId, invoiceIds)
                  .eq(Invoice::getDeleted, 0);
        return invoiceMapper.selectList(invWrapper);
    }

    /**
     * 获取账单汇总统计
     *
     * @param tenantId 租户ID
     * @param billType 账单类型（1=应收, 2=应付）
     * @return 汇总数据：totalAmount, paidAmount, unpaidAmount, overdueAmount
     */
    @Override
    public Map<String, BigDecimal> getBillSummary(Long tenantId, Integer billType) {
        LambdaQueryWrapper<Bill> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Bill::getTenantId, tenantId)
               .eq(Bill::getBillType, billType)
               .ne(Bill::getStatus, STATUS_CANCELLED);
        List<Bill> bills = list(wrapper);

        BigDecimal totalAmount = BigDecimal.ZERO;
        BigDecimal paidAmount = BigDecimal.ZERO;
        BigDecimal unpaidAmount = BigDecimal.ZERO;
        BigDecimal overdueAmount = BigDecimal.ZERO;
        LocalDate today = LocalDate.now();

        for (Bill bill : bills) {
            BigDecimal amount = bill.getAmount() != null ? bill.getAmount() : BigDecimal.ZERO;
            BigDecimal paid = bill.getPaidAmount() != null ? bill.getPaidAmount() : BigDecimal.ZERO;
            totalAmount = totalAmount.add(amount);
            paidAmount = paidAmount.add(paid);
            unpaidAmount = unpaidAmount.add(amount.subtract(paid).max(BigDecimal.ZERO));
            // 逾期：到期日已过且未完成
            if (bill.getDueDate() != null && bill.getDueDate().isBefore(today)
                    && bill.getStatus() != STATUS_COMPLETED) {
                overdueAmount = overdueAmount.add(amount.subtract(paid).max(BigDecimal.ZERO));
            }
        }

        Map<String, BigDecimal> summary = new HashMap<>();
        summary.put("totalAmount", totalAmount);
        summary.put("paidAmount", paidAmount);
        summary.put("unpaidAmount", unpaidAmount);
        summary.put("overdueAmount", overdueAmount);
        return summary;
    }

    /**
     * 从采购订单自动创建应付账单
     *
     * @param tenantId    租户ID
     * @param purchaseId  采购订单ID
     * @param purchaseNo  采购订单编号
     * @param supplierId  供应商ID
     * @param supplierName 供应商名称
     * @param amount      金额
     * @param isReturn    是否退货（退货则direction=-1）
     * @param operatorId  操作人ID
     * @return 创建的账单
     */
    @Override
    @Transactional
    public Bill createBillFromPurchase(Long tenantId, Long purchaseId, String purchaseNo,
                                       Long supplierId, String supplierName,
                                       BigDecimal amount, boolean isReturn, Long operatorId) {
        Bill bill = new Bill();
        bill.setTenantId(tenantId);
        bill.setCreatedBy(operatorId);
        bill.setBillType(BILL_TYPE_PAYABLE);
        bill.setDirection(isReturn ? -1 : 1);
        bill.setPartnerType(2); // 供应商
        bill.setPartnerId(supplierId);
        bill.setPartnerName(supplierName);
        bill.setAmount(amount);
        bill.setPaidAmount(BigDecimal.ZERO);
        bill.setCurrency("CNY");
        bill.setExchangeRate(BigDecimal.ONE);
        bill.setBaseAmount(amount);
        bill.setBillDate(LocalDate.now());
        bill.setSourceType("PURCHASE");
        bill.setSourceId(purchaseId);
        bill.setSourceNo(purchaseNo);
        bill.setStatus(STATUS_DRAFT);
        bill.setAuditStatus(AUDIT_NONE);
        bill.setBillNo(generateBillNo(BILL_TYPE_PAYABLE));
        save(bill);
        log.info("从采购订单创建应付账单: purchaseNo={}, amount={}, isReturn={}", purchaseNo, amount, isReturn);
        return bill;
    }

    /**
     * 从销售订单自动创建应收账单
     *
     * @param tenantId    租户ID
     * @param salesId     销售订单ID
     * @param salesNo     销售订单编号
     * @param customerId  客户ID
     * @param customerName 客户名称
     * @param amount      金额
     * @param isReturn    是否退货（退货则direction=-1）
     * @param operatorId  操作人ID
     * @return 创建的账单
     */
    @Override
    @Transactional
    public Bill createBillFromSales(Long tenantId, Long salesId, String salesNo,
                                     Long customerId, String customerName,
                                     BigDecimal amount, boolean isReturn, Long operatorId) {
        Bill bill = new Bill();
        bill.setTenantId(tenantId);
        bill.setCreatedBy(operatorId);
        bill.setBillType(BILL_TYPE_RECEIVABLE);
        bill.setDirection(isReturn ? -1 : 1);
        bill.setPartnerType(1); // 客户
        bill.setPartnerId(customerId);
        bill.setPartnerName(customerName);
        bill.setAmount(amount);
        bill.setPaidAmount(BigDecimal.ZERO);
        bill.setCurrency("CNY");
        bill.setExchangeRate(BigDecimal.ONE);
        bill.setBaseAmount(amount);
        bill.setBillDate(LocalDate.now());
        bill.setSourceType("SALE");
        bill.setSourceId(salesId);
        bill.setSourceNo(salesNo);
        bill.setStatus(STATUS_DRAFT);
        bill.setAuditStatus(AUDIT_NONE);
        bill.setBillNo(generateBillNo(BILL_TYPE_RECEIVABLE));
        save(bill);
        log.info("从销售订单创建应收账单: salesNo={}, amount={}, isReturn={}", salesNo, amount, isReturn);
        return bill;
    }

    /**
     * 生成账单编号
     * <p>
     * 编号规则：YS(应收)/YF(应付) + 日期(yyyyMMdd) + 6位随机字符
     * </p>
     */
    private String generateBillNo(Integer billType) {
        String prefix = billType == BILL_TYPE_RECEIVABLE ? "YS" : "YF";
        String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String random = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        return prefix + dateStr + random;
    }
}
