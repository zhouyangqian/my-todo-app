package com.example.inventory.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 库存视图对象
 * 用于返回带有关联信息的库存数据
 */
@Data
public class InventoryVO {
    private Long id;
    private Long warehouseId;
    private String warehouseName;
    private Long productId;
    private String productName;
    private String productSku;
    private String batchNo;
    private BigDecimal quantity;
    private BigDecimal lockedQuantity;
    private BigDecimal availableQuantity;
    private BigDecimal stockMin;
    private BigDecimal stockMax;
    private BigDecimal costPrice;
}
