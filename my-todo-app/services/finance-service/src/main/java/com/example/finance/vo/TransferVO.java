package com.example.finance.vo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 账户转账请求VO
 * <p>
 * 用于账户间转账接口的请求体，支持指定转出账户、转入账户、金额、币种和汇率。
 * 转账会生成两条收支记录：转出账户的EXPENSE记录和转入账户的INCOME记录，通过relatedTransId关联。
 * </p>
 */
@Data
public class TransferVO {

    /** 转出账户ID */
    @NotNull(message = "转出账户ID不能为空")
    private Long fromAccountId;

    /** 转入账户ID */
    @NotNull(message = "转入账户ID不能为空")
    private Long toAccountId;

    /** 转账金额 */
    @NotNull(message = "转账金额不能为空")
    private BigDecimal amount;

    /** 币种编码，默认CNY */
    @NotBlank(message = "币种不能为空")
    private String currency;

    /** 汇率，默认1.0 */
    private BigDecimal exchangeRate;

    /** 备注 */
    private String remark;
}
