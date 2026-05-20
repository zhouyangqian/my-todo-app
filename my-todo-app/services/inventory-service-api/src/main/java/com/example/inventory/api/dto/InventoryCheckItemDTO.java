package com.example.inventory.api.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 盘点明细数据传输对象
 */
@Data
public class InventoryCheckItemDTO {
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
