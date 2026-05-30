package com.example.finance.service;

import java.math.BigDecimal;

/**
 * ERP-财务结算服务接口
 */
public interface SettlementService {

    void settlePurchase(Long tenantId, Long purchaseOrderId, String purchaseOrderNo,
            Long supplierId, BigDecimal amount, Long operatorId);

    void settleSales(Long tenantId, Long salesOrderId, String salesOrderNo,
            Long customerId, BigDecimal amount, Long operatorId);
}
