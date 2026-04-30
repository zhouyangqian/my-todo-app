package com.example.erp.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 销售出库单实体类
 * <p>
 * 对应数据库表 erp_sales_shipment，用于记录销售出库单的主表信息。
 * 销售出库单是销售订单的执行单据，记录商品的实际出库情况。
 * 出库单审核后会触发库存扣减（调用 InventoryService.outbound）和应收账款生成（调用 FinanceService）。
 * 支持多租户隔离（tenantId）、软删除（deleted）和自动填充审计字段。
 * </p>
 *
 * @author ERP系统
 * @since 1.0
 */
@Data
@TableName("erp_sales_shipment")
public class SalesShipment implements Serializable {

    /** 序列化版本号 */
    private static final long serialVersionUID = 1L;

    /**
     * 出库单ID（主键，自增）
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 租户ID，用于多租户数据隔离，插入时自动填充
     */
    @TableField(fill = FieldFill.INSERT)
    private Long tenantId;

    /**
     * 出库单号，出库单的唯一编号，通常按规则自动生成
     */
    private String shipmentNo;

    /**
     * 订单ID，关联销售订单表
     */
    private Long orderId;

    /**
     * 订单编号，冗余存储便于查询显示
     */
    private String orderNo;

    /**
     * 客户ID，关联客户表
     */
    private Long customerId;

    /**
     * 仓库ID，关联仓库表，指定出库的仓库
     */
    private Long warehouseId;

    /**
     * 出库日期，商品实际出库的日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private LocalDateTime shipmentDate;

    /**
     * 出库总金额，出库单的总金额（优惠前）
     */
    private BigDecimal totalAmount;

    /**
     * 优惠金额，给予客户的折扣金额
     */
    private BigDecimal discountAmount;

    /**
     * 实收金额，实际收取的金额 = 总金额 - 优惠金额
     */
    private BigDecimal receivedAmount;

    /**
     * 出库状态：
     * 0-草稿（初始状态，可编辑），
     * 1-待审核（提交审核后等待审批），
     * 2-已出库（审核通过，库存已扣减，应收已生成），
     * 3-已取消（出库单被取消）
     */
    private Integer shipmentStatus;

    /**
     * 备注，用于记录出库单的补充说明信息
     */
    private String remark;

    /**
     * 软删除标记：0-未删除，1-已删除
     */
    @TableLogic
    @TableField(fill = FieldFill.INSERT)
    private Integer deleted;

    /**
     * 创建人ID，记录出库单的创建者，插入时自动填充
     */
    @TableField(fill = FieldFill.INSERT)
    private Long createdBy;

    /**
     * 创建时间，记录出库单的创建时间戳，插入时自动填充
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
