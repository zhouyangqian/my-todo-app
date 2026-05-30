package com.example.erp.api.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 供应商对账单VO
 */
@Data
public class SupplierStatementDTO {

    /** 供应商ID */
    private Long supplierId;

    /** 供应商名称 */
    private String supplierName;

    /** 期初余额（应付） */
    private BigDecimal openingBalance;

    /** 本期采购金额 */
    private BigDecimal purchaseAmount;

    /** 本期付款金额 */
    private BigDecimal paymentAmount;

    /** 期末余额（应付） */
    private BigDecimal closingBalance;

    /** 对账明细列表 */
    private List<StatementItem> items;

    @Data
    public static class StatementItem {
        /** 单据日期 */
        private String date;
        /** 单据编号 */
        private String documentNo;
        /** 单据类型：purchase-采购订单, payment-付款 */
        private String type;
        /** 摘要说明 */
        private String description;
        /** 借方金额（增加应付） */
        private BigDecimal debit;
        /** 贷方金额（减少应付） */
        private BigDecimal credit;
        /** 余额 */
        private BigDecimal balance;
    }
}
