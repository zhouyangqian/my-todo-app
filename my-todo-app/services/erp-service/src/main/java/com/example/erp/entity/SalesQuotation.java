package com.example.erp.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 销售报价单实体类
 * <p>
 * 对应数据库表 erp_sales_quotation，用于管理ERP系统中的销售报价单信息。
 * 记录向客户提供的产品报价，包括报价日期、有效期、总金额、状态流转
 * （草稿->已发送->已接受/已拒绝/已过期->已转订单）以及转订单关联等核心业务数据。
 * 支持多租户隔离（tenantId）、软删除（deleted）和自动填充审计字段。
 * </p>
 *
 * @author ERP系统
 * @since 1.0
 */
@Data
@TableName("erp_sales_quotation")
public class SalesQuotation implements Serializable {

    /** 序列化版本号 */
    private static final long serialVersionUID = 1L;

    /**
     * 报价单ID（主键，自增）
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 租户ID，用于多租户数据隔离，插入时自动填充
     */
    @TableField(fill = FieldFill.INSERT)
    private Long tenantId;

    /**
     * 报价单编号，同一租户下唯一，按规则自动生成
     */
    private String quotationNo;

    /**
     * 客户ID，关联 erp_customer 表，标识报价的目标客户
     */
    private Long customerId;

    /**
     * 客户名称，冗余存储便于查询显示
     */
    private String customerName;

    /**
     * 报价日期，报价单的创建日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private LocalDate quotationDate;

    /**
     * 有效期至，报价的最后有效日期，超过此日期报价自动失效
     */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private LocalDate validUntil;

    /**
     * 总金额，报价单所有明细行的金额汇总
     */
    private BigDecimal totalAmount;

    /**
     * 报价单状态流转：
     * 0-草稿（初始状态，可编辑），
     * 1-已发送（已发送给客户，等待回复），
     * 2-已接受（客户接受报价），
     * 3-已拒绝（客户拒绝报价），
     * 4-已过期（超过有效期未回复），
     * 5-已转订单（已转为销售订单）
     */
    private Integer status;

    /**
     * 转订单ID，转为销售订单后关联的销售订单ID
     */
    private Long convertedOrderId;

    /**
     * 备注，用于记录报价单的补充说明信息
     */
    private String remark;

    /**
     * 软删除标记：0-未删除，1-已删除。使用逻辑删除避免数据物理丢失
     */
    @TableLogic
    @TableField(fill = FieldFill.INSERT)
    private Integer deleted;

    /**
     * 创建人ID，记录报价单的创建者，插入时自动填充
     */
    @TableField(fill = FieldFill.INSERT)
    private Long createdBy;

    /**
     * 创建时间，记录报价单的创建时间戳，插入时自动填充
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
