package com.example.erp.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 采购订单明细实体类
 * 对应数据库表 erp_purchase_order_item
 */
@Data
@TableName("erp_purchase_order_item")
public class PurchaseOrderItem implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField(fill = FieldFill.INSERT)
    private Long tenantId;

    /** 订单ID */
    private Long orderId;

    /** 商品ID */
    private Long productId;

    /** 商品编码 */
    private String productCode;

    /** 商品名称 */
    private String productName;

    /** 规格型号 */
    private String specification;

    /** 单位 */
    private String unit;

    /** 订单数量 */
    private BigDecimal quantity;

    /** 单价 */
    private BigDecimal price;

    /** 行折扣金额 */
    private BigDecimal discountAmount;

    /** 行金额 = 数量 * 单价 - 折扣 */
    private BigDecimal amount;

    /** 已入库数量 */
    private BigDecimal receivedQuantity;

    /** 已入库金额 */
    private BigDecimal receivedAmount;

    /** 备注 */
    private String remark;

    @TableLogic
    @TableField(fill = FieldFill.INSERT)
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    private Long createdBy;

    @TableField(fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.UPDATE)
    private Long updatedBy;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private LocalDateTime updatedAt;
}
