package com.example.erp.api.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 采购订单明细视图对象
 */
@Data
public class PurchaseOrderItemDTO {
    private Long id;
    private Long productId;
    private String productCode;
    private String productName;
    private String specification;
    private String unit;
    private BigDecimal quantity;
    private BigDecimal price;
    private BigDecimal discountAmount;
    private BigDecimal amount;
    private BigDecimal receivedQuantity;
    private BigDecimal receivedAmount;
    private String remark;

    /** 可入库数量 = 订单数量 - 已入库数量 */
    private BigDecimal receivableQuantity;
}
