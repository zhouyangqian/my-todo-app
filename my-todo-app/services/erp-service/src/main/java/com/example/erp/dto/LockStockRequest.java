package com.example.erp.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 锁定库存请求DTO
 */
@Data
public class LockStockRequest {
    private Long warehouseId;
    private Long productId;
    private BigDecimal quantity;
}
