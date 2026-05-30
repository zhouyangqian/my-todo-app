package com.example.finance.service;

import com.example.finance.event.PurchaseCompletedEvent;
import com.example.finance.event.SalesCompletedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * ERP-财务结算服务
 * <p>
 * 提供采购/销售结算集成能力，通过 Spring ApplicationEvent 实现事件驱动，
 * 由 ERP 服务通过 Feign 或内部调用触发。
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SettlementServiceImpl implements SettlementService {

    private final ApplicationEventPublisher eventPublisher;

    /**
     * 采购完成结算（由ERP服务通过Feign调用）
     *
     * @param tenantId        租户ID
     * @param purchaseOrderId 采购订单ID
     * @param purchaseOrderNo 采购订单号
     * @param supplierId      供应商ID
     * @param amount          采购金额
     * @param operatorId      操作人ID
     */
    @Override
    public void settlePurchase(Long tenantId, Long purchaseOrderId, String purchaseOrderNo,
            Long supplierId, BigDecimal amount, Long operatorId) {
        log.info("采购结算: orderNo={}, amount={}", purchaseOrderNo, amount);
        eventPublisher.publishEvent(
                new PurchaseCompletedEvent(this, tenantId, purchaseOrderId, purchaseOrderNo, supplierId, amount, operatorId)
        );
    }

    /**
     * 销售完成结算（由ERP服务通过Feign调用）
     *
     * @param tenantId      租户ID
     * @param salesOrderId  销售订单ID
     * @param salesOrderNo  销售订单号
     * @param customerId    客户ID
     * @param amount        销售金额
     * @param operatorId    操作人ID
     */
    @Override
    public void settleSales(Long tenantId, Long salesOrderId, String salesOrderNo,
            Long customerId, BigDecimal amount, Long operatorId) {
        log.info("销售结算: orderNo={}, amount={}", salesOrderNo, amount);
        eventPublisher.publishEvent(
                new SalesCompletedEvent(this, tenantId, salesOrderId, salesOrderNo, customerId, amount, operatorId)
        );
    }
}
