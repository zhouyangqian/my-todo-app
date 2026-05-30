package com.example.erp.api.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 销售报价单创建请求
 */
@Data
public class CreateSalesQuotationVO {

    /**
     * 客户ID
     */
    @NotNull(message = "客户ID不能为空")
    private Long customerId;

    /**
     * 报价日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private LocalDate quotationDate;

    /**
     * 有效期至
     */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private LocalDate validUntil;

    /**
     * 备注
     */
    private String remark;

    /**
     * 报价明细列表
     */
    @NotNull(message = "报价明细不能为空")
    private List<QuotationItemRequest> items;

    /**
     * 报价明细请求
     */
    @Data
    public static class QuotationItemRequest {
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
        private BigDecimal unitPrice;

        /**
         * 折扣率（百分比，默认100表示无折扣）
         */
        private BigDecimal discountRate;

        /**
         * 备注
         */
        private String remark;
    }
}
