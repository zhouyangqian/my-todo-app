package com.example.erp.api.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 销售订单明细视图对象
 */
@Data
public class SalesOrderItemDTO {
    /** 明细ID */
    private Long id;

    /** 商品ID */
    private Long productId;

    /** 商品编码 */
    private String productCode;

    /** 商品名称 */
    private String productName;

    /** 规格 */
    private String specification;

    /** 单位 */
    private String unit;

    /** 订单数量 */
    private BigDecimal quantity;

    /** 单价 */
    private BigDecimal price;

    /** 折扣金额 */
    private BigDecimal discountAmount;

    /** 行金额 */
    private BigDecimal amount;

    /** 已发货数量 */
    private BigDecimal deliveredQuantity;

    /** 已发货金额 */
    private BigDecimal deliveredAmount;

    /** 备注 */
    private String remark;

    /** 可发货数量 */
    private BigDecimal shippableQuantity;
}
