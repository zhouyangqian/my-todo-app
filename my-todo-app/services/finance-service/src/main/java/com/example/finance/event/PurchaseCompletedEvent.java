package com.example.finance.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.math.BigDecimal;

/**
 * 采购完成事件
 * <p>
 * 当 ERP 采购订单完成后发布此事件，由财务服务监听并自动生成应付账款记录。
 * </p>
 */
@Getter
public class PurchaseCompletedEvent extends ApplicationEvent {

    /** 租户ID */
    private final Long tenantId;

    /** 采购订单ID */
    private final Long purchaseOrderId;

    /** 采购订单号 */
    private final String purchaseOrderNo;

    /** 供应商ID */
    private final Long supplierId;

    /** 采购总金额 */
    private final BigDecimal totalAmount;

    /** 操作人ID */
    private final Long operatorId;

    public PurchaseCompletedEvent(Object source, Long tenantId, Long purchaseOrderId,
            String purchaseOrderNo, Long supplierId, BigDecimal totalAmount, Long operatorId) {
        super(source);
        this.tenantId = tenantId;
        this.purchaseOrderId = purchaseOrderId;
        this.purchaseOrderNo = purchaseOrderNo;
        this.supplierId = supplierId;
        this.totalAmount = totalAmount;
        this.operatorId = operatorId;
    }
}
