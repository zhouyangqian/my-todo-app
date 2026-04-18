package com.example.erp.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 销售订单实体类
 * <p>
 * 对应数据库表 erp_sales_order，用于管理ERP系统中的销售订单信息。
 * 记录向客户销售商品的完整订单流程，包括客户信息、发货仓库、
 * 订单金额（总金额/优惠金额/实收金额）、订单状态流转（草稿->待审核->已审核->已出库->已完成/已取消）、
 * 审核流程（审核人/审核时间）以及销售员归属等核心业务数据。
 * 支持多租户隔离（tenantId）、软删除（deleted）和自动填充审计字段。
 * </p>
 *
 * @author ERP系统
 * @since 1.0
 */
@Data
@TableName("erp_sales_order")
public class SalesOrder implements Serializable {

    /** 序列化版本号 */
    private static final long serialVersionUID = 1L;

    /**
     * 订单ID（主键，自增）
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 租户ID，用于多租户数据隔离，插入时自动填充
     */
    @TableField(fill = FieldFill.INSERT)
    private Long tenantId;

    /**
     * 订单编号，销售订单的唯一编号，通常按规则自动生成
     */
    private String orderNo;

    /**
     * 客户ID，关联 erp_customer 表，标识购买的客户
     */
    private Long customerId;

    /**
     * 仓库ID，关联 erp_warehouse 表，标识销售商品的发货仓库
     */
    private Long warehouseId;

    /**
     * 订单日期，销售订单的下单日期
     */
    private LocalDateTime orderDate;

    /**
     * 预计发货日期，与客户约定的预计发货时间
     */
    private LocalDateTime expectedDate;

    /**
     * 订单金额，销售订单的总金额（优惠前）
     */
    private BigDecimal totalAmount;

    /**
     * 优惠金额，给予客户的折扣金额
     */
    private BigDecimal discountAmount;

    /**
     * 实收金额，实际收取的金额 = 订单金额 - 优惠金额
     */
    private BigDecimal receivedAmount;

    /**
     * 订单状态流转：
     * 0-草稿（初始状态，可编辑），
     * 1-待审核（提交审核后等待审批），
     * 2-已审核（审核通过，可执行出库），
     * 3-已出库（商品已出库完成），
     * 4-已完成（订单全部完成），
     * 5-已取消（订单被取消）
     */
    private Integer orderStatus;

    /**
     * 审核人ID，审核通过该订单的用户ID，关联用户表
     */
    private Long approvedBy;

    /**
     * 审核时间，订单审核通过的时间戳
     */
    private LocalDateTime approvedAt;

    /**
     * 销售员ID，负责该销售订单的销售人员用户ID，用于销售业绩统计
     */
    private Long salesId;

    /**
     * 备注，用于记录销售订单的补充说明信息
     */
    private String remark;

    /**
     * 软删除标记：0-未删除，1-已删除。使用逻辑删除避免数据物理丢失
     */
    @TableLogic
    @TableField(fill = FieldFill.INSERT)
    private Integer deleted;

    /**
     * 创建人ID，记录销售订单的创建者，插入时自动填充
     */
    @TableField(fill = FieldFill.INSERT)
    private Long createdBy;

    /**
     * 创建时间，记录销售订单的创建时间戳，插入时自动填充
     */
    @TableField(fill = FieldFill.INSERT)
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
    private LocalDateTime updatedAt;
}
