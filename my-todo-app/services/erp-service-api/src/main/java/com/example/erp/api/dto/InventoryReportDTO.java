package com.example.erp.api.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 库存报表DTO
 */
@Data
public class InventoryReportDTO {

    /** 汇总数据 */
    private Summary summary;

    /** 库存明细 */
    private List<InventoryDetail> details;

    /** 库龄分析 */
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
        private Long productId;
        private String warehouseName;
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
