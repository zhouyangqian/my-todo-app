package com.example.inventory.api.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 库存报表DTO
 */
@Data
public class InventoryReportDTO {

    private Summary summary;
    private List<InventoryDetail> details;
    private List<AgingStat> agingStats;

    @Data
    public static class Summary {
        private Integer totalProducts;
        private BigDecimal totalQuantity;
        private BigDecimal totalValue;
        private Integer alertCount;
    }

    @Data
    public static class InventoryDetail {
        private Long warehouseId;
        private String warehouseName;
        private Long productId;
        private String productName;
        private String productCode;
        private BigDecimal quantity;
        private BigDecimal costPrice;
        private BigDecimal totalValue;
        private BigDecimal stockMin;
        private BigDecimal stockMax;
    }

    @Data
    public static class AgingStat {
        private String agingRange;
        private Integer productCount;
        private BigDecimal quantity;
        private BigDecimal value;
    }
}
