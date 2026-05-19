package com.example.erp.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 解锁库存请求DTO
 */
@Data
public class UnlockStockRequest {
    private Long warehouseId;
    private Long productId;
    private BigDecimal quantity;
}
