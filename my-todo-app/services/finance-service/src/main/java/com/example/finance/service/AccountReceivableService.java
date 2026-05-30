package com.example.finance.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.finance.entity.AccountReceivable;

import java.math.BigDecimal;
import java.util.List;

/**
 * 应收账款业务服务接口
 *
 * @author finance-team
 * @since 1.0
 */
public interface AccountReceivableService extends IService<AccountReceivable> {

    Page<AccountReceivable> getPage(Long tenantId, int page, int size,
                                     Long customerId, Integer status);

    AccountReceivable create(AccountReceivable receivable);

    void receivePayment(Long id, BigDecimal amount);

    BigDecimal getTotalReceivable(Long customerId, Long tenantId);

    List<AccountReceivable> getOverdueList(Long tenantId);

    void createReceivableFromSales(Long tenantId, Long salesOrderId,
            String salesOrderNo, Long customerId, BigDecimal amount, Long operatorId);
}
