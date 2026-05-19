package com.example.erp.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * Dashboard统计数据VO
 */
@Data
public class DashboardVO {

    /** 今日销售额 */
    private BigDecimal todaySales;

    /** 本月销售额 */
    private BigDecimal monthSales;

    /** 今日采购额 */
    private BigDecimal todayPurchases;

    /** 本月采购额 */
    private BigDecimal monthPurchases;

    /** 库存商品种类数 */
    private Integer inventoryProductCount;

    /** 库存预警商品数 */
    private Integer alertProductCount;

    /** 待审核采购订单数 */
    private Integer pendingPurchaseOrders;

    /** 待审核销售订单数 */
    private Integer pendingSalesOrders;

    /** 销售趋势（近7天） */
    private List<TrendData> salesTrend;

    /** 采购趋势（近7天） */
    private List<TrendData> purchaseTrend;

    /** 销售排行（前10商品） */
    private List<RankingData> topProducts;

    /** 销售排行（前10客户） */
    private List<RankingData> topCustomers;

    @Data
    public static class TrendData {
        private String date;
        private BigDecimal amount;
    }

    @Data
    public static class RankingData {
        private String name;
        private BigDecimal amount;
        private Integer quantity;
    }
}
