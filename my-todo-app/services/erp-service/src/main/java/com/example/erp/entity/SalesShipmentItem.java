package com.example.erp.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 销售出库单明细实体类
 * <p>
 * 对应数据库表 erp_sales_shipment_item，用于记录销售出库单的商品明细。
 * 每条记录关联到销售订单明细，记录实际出库的商品、数量和金额。
 * 通过 orderItemId 关联到销售订单明细，实现发货数量的精确追踪。
 * 支持多租户隔离（tenantId）、软删除（deleted）和自动填充审计字段。
 * </p>
 *
 * @author ERP系统
 * @since 1.0
 */
@Data
@TableName("erp_sales_shipment_item")
public class SalesShipmentItem implements Serializable {

    /** 序列化版本号 */
    private static final long serialVersionUID = 1L;

    /**
     * 明细ID（主键，自增）
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 租户ID，用于多租户数据隔离，插入时自动填充
     */
    @TableField(fill = FieldFill.INSERT)
    private Long tenantId;

    /**
     * 出库单ID，关联销售出库单表
     */
    private Long shipmentId;

    /**
     * 订单明细ID，关联销售订单明细表
     */
    private Long orderItemId;

    /**
     * 商品ID，关联商品表
     */
    private Long productId;

    /**
     * 商品编码，冗余存储便于查询显示
     */
    private String productCode;

    /**
     * 商品名称，冗余存储便于查询显示
     */
    private String productName;

    /**
     * 规格型号，商品的规格描述
     */
    private String specification;

    /**
     * 单位，商品的计量单位
     */
    private String unit;

    /**
     * 出库数量，实际出库的商品数量
     */
    private BigDecimal quantity;

    /**
     * 单价，商品的销售单价
     */
    private BigDecimal price;

    /**
     * 折扣金额，该明细行的优惠金额
     */
    private BigDecimal discountAmount;

    /**
     * 金额，该明细的总金额 = 数量 * 单价 - 折扣金额
     */
    private BigDecimal amount;

    /**
     * 备注，用于记录明细的补充说明信息
     */
    private String remark;

    /**
     * 软删除标记：0-未删除，1-已删除
     */
    @TableLogic
    @TableField(fill = FieldFill.INSERT)
    private Integer deleted;

    /**
     * 创建人ID，记录明细的创建者，插入时自动填充
     */
    @TableField(fill = FieldFill.INSERT)
    private Long createdBy;

    /**
     * 创建时间，记录明细的创建时间戳，插入时自动填充
     */
    @TableField(fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private LocalDateTime createdAt;
}
