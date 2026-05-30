package com.example.finance.listener;

import com.example.finance.event.PurchaseCompletedEvent;
import com.example.finance.event.SalesCompletedEvent;
import com.example.finance.service.AccountPayableService;
import com.example.finance.service.AccountReceivableService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * ERP 业务事件监听器
 * <p>
 * 监听采购/销售完成事件，自动生成对应的应付/应收账款记录。
 * 使用 @Async 实现异步处理，不阻塞主业务流程。
 * </p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ErpEventListener {

    private final AccountPayableService accountPayableService;
    private final AccountReceivableService accountReceivableService;

    /**
     * 处理采购完成事件，自动生成应付账款
     */
    @EventListener
    @Async
    public void handlePurchaseCompleted(PurchaseCompletedEvent event) {
        log.info("采购完成事件: orderNo={}, amount={}", event.getPurchaseOrderNo(), event.getTotalAmount());
        accountPayableService.createPayableFromPurchase(
                event.getTenantId(), event.getPurchaseOrderId(),
                event.getPurchaseOrderNo(), event.getSupplierId(),
                event.getTotalAmount(), event.getOperatorId()
        );
    }

    /**
     * 处理销售完成事件，自动生成应收账款
     */
    @EventListener
    @Async
    public void handleSalesCompleted(SalesCompletedEvent event) {
        log.info("销售完成事件: orderNo={}, amount={}", event.getSalesOrderNo(), event.getTotalAmount());
        accountReceivableService.createReceivableFromSales(
                event.getTenantId(), event.getSalesOrderId(),
                event.getSalesOrderNo(), event.getCustomerId(),
                event.getTotalAmount(), event.getOperatorId()
        );
    }
}
