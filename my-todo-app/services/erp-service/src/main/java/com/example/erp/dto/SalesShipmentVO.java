package com.example.erp.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 销售出库单视图对象
 * 用于返回带有关联信息的销售出库单数据
 */
@Data
public class SalesShipmentVO {
    /** 出库单ID */
    private Long id;

    /** 出库单号 */
    private String shipmentNo;

    /** 订单ID */
    private Long orderId;

    /** 订单编号 */
    private String orderNo;

    /** 客户ID */
    private Long customerId;

    /** 客户名称 */
    private String customerName;

    /** 仓库ID */
    private Long warehouseId;

    /** 仓库名称 */
    private String warehouseName;

    /** 出库日期 */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private LocalDateTime shipmentDate;

    /** 出库总金额 */
    private BigDecimal totalAmount;

    /** 优惠金额 */
    private BigDecimal discountAmount;

    /** 实收金额 */
    private BigDecimal receivedAmount;

    /** 出库状态 */
    private Integer shipmentStatus;

    /** 出库状态文本 */
    private String shipmentStatusText;

    /** 备注 */
    private String remark;

    /** 出库明细列表 */
    private List<SalesShipmentItemVO> items;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "GMT+8")
    private LocalDateTime createdAt;

    /** 更新时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "GMT+8")
    private LocalDateTime updatedAt;

    /**
     * 销售出库单明细视图对象
     */
    @Data
    public static class SalesShipmentItemVO {
        /** 明细ID */
        private Long id;

        /** 订单明细ID */
        private Long orderItemId;

        /** 商品ID */
        private Long productId;

        /** 商品编码 */
        private String productCode;

        /** 商品名称 */
        private String productName;

        /** 规格 */
        private String specification;

        /** 单位 */
        private String unit;

        /** 出库数量 */
        private BigDecimal quantity;

        /** 单价 */
        private BigDecimal price;

        /** 折扣金额 */
        private BigDecimal discountAmount;

        /** 金额 */
        private BigDecimal amount;

        /** 备注 */
        private String remark;
    }
}
