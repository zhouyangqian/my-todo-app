package com.example.finance.vo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * ERP结算请求VO
 * <p>
 * 采购/销售结算接口的统一请求体，用于接收 ERP 服务传递的订单结算数据。
 * </p>
 */
@Data
public class SettlementVO {

    /** 订单ID（采购订单ID或销售订单ID） */
    @NotNull(message = "订单ID不能为空")
    private Long orderId;

    /** 订单号 */
    @NotBlank(message = "订单号不能为空")
    private String orderNo;

    /** 交易对方ID（供应商ID或客户ID） */
    @NotNull(message = "交易对方ID不能为空")
    private Long counterpartyId;

    /** 结算金额 */
    @NotNull(message = "结算金额不能为空")
    private BigDecimal amount;
}
