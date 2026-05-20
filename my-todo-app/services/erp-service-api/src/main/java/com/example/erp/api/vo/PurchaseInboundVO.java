package com.example.erp.api.vo;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 采购入库请求
 */
@Data
public class PurchaseInboundVO {

    /** 入库明细列表 */
    @NotNull(message = "入库明细不能为空")
    private List<InboundItemRequest> items;

    /** 备注 */
    private String remark;

    @Data
    public static class InboundItemRequest {
        /** 订单明细ID */
        @NotNull(message = "订单明细ID不能为空")
        private Long itemId;

        /** 实收数量 */
        @NotNull(message = "实收数量不能为空")
        private BigDecimal quantity;
    }
}
