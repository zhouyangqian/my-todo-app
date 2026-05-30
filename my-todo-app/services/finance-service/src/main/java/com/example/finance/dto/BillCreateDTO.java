package com.example.finance.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 账单创建请求DTO
 */
@Data
public class BillCreateDTO {

    /** 账单类型：1=应收(RECEIVABLE), 2=应付(PAYABLE) */
    @NotNull(message = "账单类型不能为空")
    private Integer billType;

    /** 方向：1=正数(正常), -1=红字(退货/冲销) */
    private Integer direction;

    /** 往来单位类型：1=客户, 2=供应商 */
    @NotNull(message = "往来单位类型不能为空")
    private Integer partnerType;

    /** 往来单位ID */
    @NotNull(message = "往来单位ID不能为空")
    private Long partnerId;

    /** 往来单位名称 */
    private String partnerName;

    /** 关联资金账户ID */
    private Long accountId;

    /** 币种编码，默认CNY */
    private String currency;

    /** 汇率，默认1.0 */
    private BigDecimal exchangeRate;

    /** 账单金额 */
    @NotNull(message = "账单金额不能为空")
    private BigDecimal amount;

    /** 账单日期 */
    @NotNull(message = "账单日期不能为空")
    private LocalDate billDate;

    /** 到期日期 */
    private LocalDate dueDate;

    /** 来源类型：PURCHASE/SALE/MANUAL */
    private String sourceType;

    /** 来源单据ID */
    private Long sourceId;

    /** 来源单据编号 */
    private String sourceNo;

    /** 备注 */
    private String remark;
}
