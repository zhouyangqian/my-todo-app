package com.example.erp.api.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 销售出库单明细视图对象
 */
@Data
public class SalesShipmentItemDTO {
    /** 明细ID */
    private Long id;

    /** 订单明细ID */
    private Long orderItemId;

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

    /** 出库数量 */
    private BigDecimal quantity;

    /** 单价 */
    private BigDecimal price;

    /** 折扣金额 */
    private BigDecimal discountAmount;

    /** 金额 */
    private BigDecimal amount;

    /** 备注 */
    private String remark;
}
