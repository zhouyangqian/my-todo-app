package com.example.erp.api.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 销售报价单明细视图对象
 */
@Data
public class SalesQuotationItemDTO {

    /** 明细ID */
    private Long id;

    /** 商品ID */
    private Long productId;

    /** 商品名称 */
    private String productName;

    /** 商品编码 */
    private String productCode;

    /** 报价数量 */
    private BigDecimal quantity;

    /** 单价 */
    private BigDecimal unitPrice;

    /** 折扣率 */
    private BigDecimal discountRate;

    /** 行金额 */
    private BigDecimal amount;

    /** 备注 */
    private String remark;
}
