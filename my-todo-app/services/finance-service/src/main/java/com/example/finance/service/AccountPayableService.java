package com.example.finance.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.finance.entity.AccountPayable;

import java.math.BigDecimal;
import java.util.List;

/**
 * 应付账款业务服务接口
 *
 * @author finance-team
 * @since 1.0
 */
public interface AccountPayableService extends IService<AccountPayable> {

    Page<AccountPayable> getPage(Long tenantId, int page, int size,
                                  Long supplierId, Integer status);

    AccountPayable create(AccountPayable payable);

    void makePayment(Long id, BigDecimal amount);

    BigDecimal getTotalPayable(Long supplierId, Long tenantId);

    List<AccountPayable> getOverdueList(Long tenantId);

    void createPayableFromPurchase(Long tenantId, Long purchaseOrderId,
            String purchaseOrderNo, Long supplierId, BigDecimal amount, Long operatorId);
}
