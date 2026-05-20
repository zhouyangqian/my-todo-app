package com.example.erp.api.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 销售出库单创建请求
 */
@Data
public class CreateShipmentVO {

    /**
     * 订单ID
     */
    @NotNull(message = "订单ID不能为空")
    private Long orderId;

    /**
     * 出库日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private LocalDateTime shipmentDate;

    /**
     * 备注
     */
    private String remark;

    /**
     * 出库明细列表
     */
    @NotEmpty(message = "出库明细不能为空")
    private List<ShipmentItemRequest> items;

    /**
     * 出库明细请求
     */
    @Data
    public static class ShipmentItemRequest {
        /**
         * 订单明细ID
         */
        @NotNull(message = "订单明细ID不能为空")
        private Long orderItemId;

        /**
         * 出库数量
         */
        @NotNull(message = "出库数量不能为空")
        @Positive(message = "出库数量必须大于0")
        private BigDecimal quantity;

        /**
         * 备注
         */
        private String remark;
    }
}
