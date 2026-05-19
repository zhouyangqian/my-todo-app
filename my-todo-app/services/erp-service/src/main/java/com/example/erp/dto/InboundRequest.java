package com.example.erp.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 入库请求DTO
 */
@Data
public class InboundRequest {
    private Long warehouseId;
    private Long productId;
    private BigDecimal quantity;
    private BigDecimal costPrice;
    private String batchNo;
    private Integer bizType;
    private String bizNo;
    private Long bizId;
}
