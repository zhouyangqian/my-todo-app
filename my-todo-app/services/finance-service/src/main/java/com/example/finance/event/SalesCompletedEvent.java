package com.example.finance.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.math.BigDecimal;

/**
 * 销售完成事件
 * <p>
 * 当 ERP 销售订单完成后发布此事件，由财务服务监听并自动生成应收账款记录。
 * </p>
 */
@Getter
public class SalesCompletedEvent extends ApplicationEvent {

    /** 租户ID */
    private final Long tenantId;

    /** 销售订单ID */
    private final Long salesOrderId;

    /** 销售订单号 */
    private final String salesOrderNo;

    /** 客户ID */
    private final Long customerId;

    /** 销售总金额 */
    private final BigDecimal totalAmount;

    /** 操作人ID */
    private final Long operatorId;

    public SalesCompletedEvent(Object source, Long tenantId, Long salesOrderId,
            String salesOrderNo, Long customerId, BigDecimal totalAmount, Long operatorId) {
        super(source);
        this.tenantId = tenantId;
        this.salesOrderId = salesOrderId;
        this.salesOrderNo = salesOrderNo;
        this.customerId = customerId;
        this.totalAmount = totalAmount;
        this.operatorId = operatorId;
    }
}
