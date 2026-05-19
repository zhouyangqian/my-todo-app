package com.example.erp.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 采购报表VO
 */
@Data
public class PurchaseReportVO {

    /** 汇总数据 */
    private Summary summary;

    /** 按商品统计 */
    private List<ProductStat> productStats;

    /** 按供应商统计 */
    private List<SupplierStat> supplierStats;

    /** 按日期统计 */
    private List<DailyStat> dailyStats;

    @Data
    public static class Summary {
        private BigDecimal totalAmount;
        private Integer totalOrders;
        private Integer totalQuantity;
        private BigDecimal avgOrderAmount;
    }

    @Data
    public static class ProductStat {
        private Long productId;
        private String productName;
        private String productCode;
        private Integer quantity;
        private BigDecimal amount;
    }

    @Data
    public static class SupplierStat {
        private Long supplierId;
        private String supplierName;
        private Integer orderCount;
        private BigDecimal amount;
    }

    @Data
    public static class DailyStat {
        private String date;
        private Integer orderCount;
        private BigDecimal amount;
    }
}
