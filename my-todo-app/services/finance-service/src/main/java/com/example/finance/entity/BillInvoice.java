package com.example.finance.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.example.common.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 账单-发票关联实体类
 * <p>
 * 对应数据库表：fin_bill_invoice
 * 用于记录账单与发票之间的多对多关联关系，
 * 以及每条关联对应的金额。
 * </p>
 *
 * @author finance-team
 * @since 1.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("fin_bill_invoice")
public class BillInvoice extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 账单ID */
    private Long billId;

    /** 发票ID */
    private Long invoiceId;

    /** 关联金额 */
    private BigDecimal relatedAmount;
}
