package com.example.finance.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 发票创建请求DTO
 */
@Data
public class InvoiceCreateDTO {

    /** 发票代码 */
    private String invoiceCode;

    /** 发票类型：1-增值税专用发票, 2-增值税普通发票, 3-电子发票 */
    @NotNull(message = "发票类型不能为空")
    private Integer invoiceType;

    /** 发票方向：1-开票, 2-收票 */
    @NotNull(message = "发票方向不能为空")
    private Integer invoiceDirection;

    /** 关联业务ID */
    private Long bizId;

    /** 客户或供应商ID */
    @NotNull(message = "往来单位ID不能为空")
    private Long partnerId;

    /** 往来单位类型：1-客户, 2-供应商 */
    private Integer partnerType;

    /** 往来单位名称 */
    private String partnerName;

    /** 开票日期 */
    private LocalDateTime invoiceDate;

    /** 币种编码 */
    private String currency;

    /** 不含税金额 */
    @NotNull(message = "不含税金额不能为空")
    private BigDecimal amountWithoutTax;

    /** 税额 */
    private BigDecimal taxAmount;

    /** 价税合计 */
    @NotNull(message = "价税合计不能为空")
    private BigDecimal totalAmount;

    /** 税率 */
    private BigDecimal taxRate;

    /** 备注 */
    private String remark;
}
