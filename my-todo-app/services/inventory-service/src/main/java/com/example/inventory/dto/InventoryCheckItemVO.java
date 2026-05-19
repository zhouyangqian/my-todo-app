package com.example.inventory.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 盘点明细视图对象
 */
@Data
public class InventoryCheckItemVO {
    private Long id;
    private Long checkId;
    private Long productId;
    private String productCode;
    private String productName;
    private BigDecimal systemQuantity;
    private BigDecimal actualQuantity;
    private BigDecimal diffQuantity;
    private String remark;
}
