package com.example.erp.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 商品促销实体类
 * 支持限时折扣、满减、买赠等促销策略
 */
@Data
@TableName("erp_product_promotion")
public class ProductPromotion implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField(fill = FieldFill.INSERT)
    private Long tenantId;

    /** 促销名称 */
    private String promotionName;

    /** 促销类型: 1-限时折扣, 2-满减, 3-买赠 */
    private Integer promotionType;

    /** 关联商品ID（null表示全场活动） */
    private Long productId;

    /** 折扣率（限时折扣用，如0.8表示8折） */
    private BigDecimal discountRate;

    /** 满减门槛金额（满减用） */
    private BigDecimal minAmount;

    /** 满减优惠金额（满减用） */
    private BigDecimal reduceAmount;

    /** 赠品商品ID（买赠用） */
    private Long giftProductId;

    /** 赠品数量（买赠用） */
    private Integer giftQuantity;

    /** 促销开始时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "GMT+8")
    private LocalDateTime startDate;

    /** 促销结束时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "GMT+8")
    private LocalDateTime endDate;

    /** 状态: 0-未开始, 1-进行中, 2-已结束, 3-已停用 */
    private Integer status;

    private String remark;

    @TableLogic
    @TableField(fill = FieldFill.INSERT)
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    private Long createdBy;

    @TableField(fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.UPDATE)
    private Long updatedBy;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime updatedAt;

    /** 商品名称（非持久化） */
    @TableField(exist = false)
    private String productName;

    /** 商品编码（非持久化） */
    @TableField(exist = false)
    private String productCode;

    /** 赠品名称（非持久化） */
    @TableField(exist = false)
    private String giftProductName;
}
