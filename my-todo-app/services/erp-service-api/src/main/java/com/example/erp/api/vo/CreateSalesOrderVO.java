package com.example.erp.api.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 销售订单创建请求
 */
@Data
public class CreateSalesOrderVO {

    /**
     * 客户ID
     */
    @NotNull(message = "客户ID不能为空")
    private Long customerId;

    /**
     * 仓库ID
     */
    @NotNull(message = "仓库ID不能为空")
    private Long warehouseId;

    /**
     * 预计发货日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private LocalDateTime expectedDate;

    /**
     * 销售员ID
     */
    private Long salesId;

    /**
     * 备注
     */
    private String remark;

    /**
     * 订单明细列表
     */
    @NotNull(message = "订单明细不能为空")
    private List<OrderItemRequest> items;

    /**
     * 订单明细请求
     */
    @Data
    public static class OrderItemRequest {
        /**
         * 商品ID
         */
        @NotNull(message = "商品ID不能为空")
        private Long productId;

        /**
         * 数量
         */
        @NotNull(message = "数量不能为空")
        @Positive(message = "数量必须大于0")
        private BigDecimal quantity;

        /**
         * 单价
         */
        @NotNull(message = "单价不能为空")
        @Positive(message = "单价必须大于0")
        private BigDecimal price;

        /**
         * 折扣金额
         */
        private BigDecimal discountAmount;

        /**
         * 备注
         */
        private String remark;
    }
}
