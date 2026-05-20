package com.example.inventory.api.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 库存流数据传输对象
 */
@Data
public class InventoryFlowDTO {
    private Long id;
    private Long warehouseId;
    private String warehouseName;
    private Long productId;
    private String productName;
    private Integer bizType;
    private String bizTypeText;
    private String bizNo;
    private Long bizId;
    private BigDecimal quantity;
    private BigDecimal beforeQuantity;
    private BigDecimal afterQuantity;
    private BigDecimal costPrice;
    private String batchNo;
    private String remark;
    private Long operatorId;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "GMT+8")
    private LocalDateTime createdAt;
}
