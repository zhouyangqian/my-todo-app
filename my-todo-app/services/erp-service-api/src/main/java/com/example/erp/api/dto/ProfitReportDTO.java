package com.example.erp.api.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 利润分析报表VO
 */
@Data
public class ProfitReportDTO {

    /** 汇总数据 */
    private Summary summary;

    /** 按商品统计的利润明细 */
    private List<ProductProfit> productProfits;

    /** 按日期统计的利润明细 */
    private List<DailyProfit> dailyProfits;

    @Data
    public static class Summary {
        /** 销售收入总额 */
        private BigDecimal totalRevenue;
        /** 采购成本总额 */
        private BigDecimal totalCost;
        /** 毛利润 */
        private BigDecimal totalProfit;
        /** 毛利率 */
        private BigDecimal profitMargin;
    }

    @Data
    public static class ProductProfit {
        /** 商品ID */
        private Long productId;
        /** 商品名称 */
        private String productName;
        /** 商品编码 */
        private String productCode;
        /** 销售数量 */
        private BigDecimal quantity;
        /** 销售金额 */
        private BigDecimal revenue;
        /** 成本金额 */
        private BigDecimal cost;
        /** 毛利润 */
        private BigDecimal profit;
        /** 毛利率 */
        private BigDecimal profitMargin;
    }

    @Data
    public static class DailyProfit {
        /** 日期 */
        private String date;
        /** 销售收入 */
        private BigDecimal revenue;
        /** 采购成本 */
        private BigDecimal cost;
        /** 毛利润 */
        private BigDecimal profit;
    }
}
