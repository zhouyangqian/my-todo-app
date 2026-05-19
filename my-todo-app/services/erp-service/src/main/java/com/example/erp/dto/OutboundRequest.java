package com.example.erp.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 出库请求DTO
 */
@Data
public class OutboundRequest {
    private Long warehouseId;
    private Long productId;
    private BigDecimal quantity;
    private Integer bizType;
    private String bizNo;
    private Long bizId;
}
