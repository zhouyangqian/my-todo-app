package com.example.inventory.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 库存实体类
 * <p>
 * 对应数据库表 erp_inventory，用于管理ERP系统中各仓库的商品库存信息。
 * 记录商品在不同仓库中的实际库存数量、锁定数量（已下单未出库）和可用数量，
 * 支持批次管理、成本价跟踪、保质期管理（生产日期/过期日期），
 * 使用乐观锁（version字段）防止并发修改导致的库存超卖问题。
 * </p>
 */
@Data
@TableName("erp_inventory")
public class Inventory implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField(fill = FieldFill.INSERT)
    private Long tenantId;

    private Long warehouseId;

    private Long productId;

    private String batchNo;

    private BigDecimal quantity;

    private BigDecimal lockedQuantity;

    private BigDecimal availableQuantity;

    private BigDecimal stockMin;

    private BigDecimal stockMax;

    private BigDecimal costPrice;

    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private LocalDateTime productionDate;

    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private LocalDateTime expiryDate;

    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private LocalDateTime updatedAt;

    @Version
    private Integer version;
}
