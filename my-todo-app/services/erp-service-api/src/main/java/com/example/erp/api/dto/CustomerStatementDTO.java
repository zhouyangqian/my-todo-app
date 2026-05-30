package com.example.erp.api.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 客户对账单VO
 */
@Data
public class CustomerStatementDTO {

    /** 客户ID */
    private Long customerId;

    /** 客户名称 */
    private String customerName;

    /** 期初余额（应收） */
    private BigDecimal openingBalance;

    /** 本期销售金额 */
    private BigDecimal salesAmount;

    /** 本期收款金额 */
    private BigDecimal receiptAmount;

    /** 期末余额（应收） */
    private BigDecimal closingBalance;

    /** 对账明细列表 */
    private List<StatementItem> items;

    @Data
    public static class StatementItem {
        /** 单据日期 */
        private String date;
        /** 单据编号 */
        private String documentNo;
        /** 单据类型：sales-销售订单, receipt-收款 */
        private String type;
        /** 摘要说明 */
        private String description;
        /** 借方金额（增加应收） */
        private BigDecimal debit;
        /** 贷方金额（减少应收） */
        private BigDecimal credit;
        /** 余额 */
        private BigDecimal balance;
    }
}
