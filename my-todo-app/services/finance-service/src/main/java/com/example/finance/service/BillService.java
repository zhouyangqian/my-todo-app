package com.example.finance.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.finance.entity.Bill;
import com.example.finance.entity.Invoice;
import com.example.finance.entity.PaymentRecord;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 统一账单业务服务接口
 *
 * @author finance-team
 * @since 1.0
 */
public interface BillService extends IService<Bill> {

    Bill createBill(Bill bill);

    Bill updateBill(Bill bill);

    Page<Bill> getBillPage(Long tenantId, int page, int size,
                           Integer billType, Integer status,
                           Integer partnerType, Long partnerId,
                           LocalDate startDate, LocalDate endDate,
                           String keyword);

    Bill getBillById(Long id);

    void submitBill(Long id);

    void approveBill(Long id, Long auditBy, String auditRemark);

    void rejectBill(Long id, Long auditBy, String auditRemark);

    void cancelBill(Long id);

    List<PaymentRecord> getBillPayments(Long id, Long tenantId);

    List<Invoice> getBillInvoices(Long id);

    Map<String, BigDecimal> getBillSummary(Long tenantId, Integer billType);

    Bill createBillFromPurchase(Long tenantId, Long purchaseId, String purchaseNo,
                                Long supplierId, String supplierName,
                                BigDecimal amount, boolean isReturn, Long operatorId);

    Bill createBillFromSales(Long tenantId, Long salesId, String salesNo,
                             Long customerId, String customerName,
                             BigDecimal amount, boolean isReturn, Long operatorId);
}
