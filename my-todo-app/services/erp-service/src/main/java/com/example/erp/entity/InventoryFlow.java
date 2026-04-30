package com.example.erp.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 库存流水实体类
 * <p>
 * 对应数据库表 erp_inventory_flow，用于记录ERP系统中所有库存变动的流水日志。
 * 每次库存发生变动（入库/出库/调拨/盘点等）时，都会生成一条流水记录，
 * 记录变动前后的数量、变动数量（正数表示入库，负数表示出库）、关联的业务单号等信息。
 * 支持多种业务类型：采购入库、销售出库、调拨入库/出库、盘盈/盘亏、退货入库/出库。
 * 库存流水不可修改和删除，确保库存变动的完整追溯链。
 * </p>
 *
 * @author ERP系统
 * @since 1.0
 */
@Data
@TableName("erp_inventory_flow")
public class InventoryFlow implements Serializable {

    /** 序列化版本号 */
    private static final long serialVersionUID = 1L;

    /**
     * 流水ID（主键，自增）
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 租户ID，用于多租户数据隔离，插入时自动填充
     */
    @TableField(fill = FieldFill.INSERT)
    private Long tenantId;

    /**
     * 仓库ID，关联 erp_warehouse 表，标识库存变动发生的仓库
     */
    private Long warehouseId;

    /**
     * 商品ID，关联 erp_product 表，标识发生变动的商品
     */
    private Long productId;

    /**
     * 业务类型，标识库存变动的原因：
     * 1-采购入库（从供应商采购商品入库），
     * 2-销售出库（向客户销售商品出库），
     * 3-调拨入库（从其他仓库调拨商品入库），
     * 4-调拨出库（将商品调拨到其他仓库出库），
     * 5-盘盈（盘点时发现库存多于账面数量），
     * 6-盘亏（盘点时发现库存少于账面数量），
     * 7-退货入库（客户退货入库），
     * 8-退货出库（向供应商退货出库）
     */
    private Integer bizType;

    /**
     * 业务单号，关联的业务单据编号（如采购订单号、销售订单号等），用于追溯业务来源
     */
    private String bizNo;

    /**
     * 业务ID，关联的业务单据主键ID，用于精确关联业务记录
     */
    private Long bizId;

    /**
     * 变动数量，正数表示入库增加，负数表示出库减少
     */
    private BigDecimal quantity;

    /**
     * 变动前数量，库存变动前的商品库存数量
     */
    private BigDecimal beforeQuantity;

    /**
     * 变动后数量，库存变动后的商品库存数量 = 变动前数量 + 变动数量
     */
    private BigDecimal afterQuantity;

    /**
     * 成本价，变动时商品的单位成本价格
     */
    private BigDecimal costPrice;

    /**
     * 批次号，关联库存的批次号，用于批次追溯
     */
    private String batchNo;

    /**
     * 备注，用于记录库存变动的补充说明信息
     */
    private String remark;

    /**
     * 操作人ID，执行此次库存变动的操作人员用户ID
     */
    private Long operatorId;

    /**
     * 创建时间，流水记录的创建时间（即库存变动发生时间），插入时自动填充
     */
    @TableField(fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private LocalDateTime createdAt;
}
