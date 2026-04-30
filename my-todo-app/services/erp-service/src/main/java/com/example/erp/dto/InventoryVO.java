package com.example.erp.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 库存视图对象
 * 用于返回带有关联信息的库存数据
 */
@Data
public class InventoryVO {
    /** 库存ID */
    private Long id;

    /** 仓库ID */
    private Long warehouseId;

    /** 仓库名称 */
    private String warehouseName;

    /** 商品ID */
    private Long productId;

    /** 商品名称 */
    private String productName;

    /** 商品编码 */
    private String productSku;

    /** 批次号 */
    private String batchNo;

    /** 库存数量 */
    private BigDecimal quantity;

    /** 锁定数量 */
    private BigDecimal lockedQuantity;

    /** 可用数量 */
    private BigDecimal availableQuantity;

    /** 库存下限 */
    private BigDecimal stockMin;

    /** 库存上限 */
    private BigDecimal stockMax;

    /** 成本价 */
    private BigDecimal costPrice;
}
