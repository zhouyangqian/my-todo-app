package com.example.erp.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 采购订单创建请求
 */
@Data
public class PurchaseOrderCreateRequest {

    /** 供应商ID */
    @NotNull(message = "供应商ID不能为空")
    private Long supplierId;

    /** 仓库ID */
    @NotNull(message = "仓库ID不能为空")
    private Long warehouseId;

    /** 预计到货日期 */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private LocalDateTime expectedDate;

    /** 经手人ID */
    private Long handlerId;

    /** 备注 */
    private String remark;

    /** 订单明细列表 */
    @NotNull(message = "订单明细不能为空")
    private List<OrderItemRequest> items;

    @Data
    public static class OrderItemRequest {
        /** 商品ID */
        @NotNull(message = "商品ID不能为空")
        private Long productId;

        /** 数量 */
        @NotNull(message = "数量不能为空")
        @Positive(message = "数量必须大于0")
        private BigDecimal quantity;

        /** 单价 */
        @NotNull(message = "单价不能为空")
        @Positive(message = "单价必须大于0")
        private BigDecimal price;

        /** 折扣金额 */
        private BigDecimal discountAmount;

        /** 备注 */
        private String remark;
    }
}
