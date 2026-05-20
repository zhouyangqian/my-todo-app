package com.example.finance.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 成本历史记录实体
 */
@Data
@TableName("fin_cost_history")
public class FinCostHistory implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField(fill = FieldFill.INSERT)
    private Long tenantId;

    private Long productId;

    private Long warehouseId;

    private BigDecimal costPrice;

    private BigDecimal quantity;

    private BigDecimal totalCost;

    /** 业务类型: 1-采购入库, 2-销售出库, 3-调拨, 4-盘点调整 */
    private Integer bizType;

    private String bizNo;

    /** 使用的成本方法: 1-FIFO, 2-加权平均, 3-个别计价 */
    private Integer costMethod;

    @TableLogic
    @TableField(fill = FieldFill.INSERT)
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    private Long createdBy;

    @TableField(fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createdAt;
}
