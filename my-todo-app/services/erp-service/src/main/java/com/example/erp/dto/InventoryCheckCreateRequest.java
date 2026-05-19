package com.example.erp.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 创建盘点单请求DTO
 */
@Data
public class InventoryCheckCreateRequest {
    private Long warehouseId;
    private Integer checkType;
    private LocalDateTime checkDate;
    private String remark;
    private List<ProductInfo> products;

    @Data
    public static class ProductInfo {
        private Long productId;
        private String productCode;
        private String productName;
    }
}
