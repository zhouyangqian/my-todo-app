package com.example.inventory.entity;

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
 * 库存流水不可修改和删除，确保库存变动的完整追溯链。
 * </p>
 */
@Data
@TableName("erp_inventory_flow")
public class InventoryFlow implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField(fill = FieldFill.INSERT)
    private Long tenantId;

    private Long warehouseId;

    private Long productId;

    private Integer bizType;

    private String bizNo;

    private Long bizId;

    private BigDecimal quantity;

    private BigDecimal beforeQuantity;

    private BigDecimal afterQuantity;

    private BigDecimal costPrice;

    private String batchNo;

    private String remark;

    private Long operatorId;

    @TableField(fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private LocalDateTime createdAt;
}
