package com.example.erp.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 销售报价单明细实体类
 * <p>
 * 对应数据库表 erp_sales_quotation_item，用于记录销售报价单的商品明细。
 * 每条记录包含商品信息、报价数量、单价、折扣率及行金额。
 * 支持多租户隔离（tenantId）、软删除（deleted）和自动填充审计字段。
 * </p>
 *
 * @author ERP系统
 * @since 1.0
 */
@Data
@TableName("erp_sales_quotation_item")
public class SalesQuotationItem implements Serializable {

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
     * 报价单ID，关联销售报价单表
     */
    private Long quotationId;

    /**
     * 商品ID，关联商品表
     */
    private Long productId;

    /**
     * 商品名称，冗余存储便于查询显示
     */
    private String productName;

    /**
     * 商品编码，冗余存储便于查询显示
     */
    private String productCode;

    /**
     * 报价数量
     */
    private BigDecimal quantity;

    /**
     * 单价，商品的报价单价
     */
    private BigDecimal unitPrice;

    /**
     * 折扣率，百分比（默认100表示无折扣）
     */
    private BigDecimal discountRate;

    /**
     * 行金额 = 数量 * 单价 * 折扣率 / 100
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

    /**
     * 更新人ID，记录最近一次修改者，更新时自动填充
     */
    @TableField(fill = FieldFill.UPDATE)
    private Long updatedBy;

    /**
     * 更新时间，记录最近一次修改的时间戳，插入和更新时自动填充
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private LocalDateTime updatedAt;
}
