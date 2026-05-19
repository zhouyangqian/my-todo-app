package com.example.erp.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 采购订单视图对象
 */
@Data
public class PurchaseOrderVO {
    private Long id;
    private String orderNo;
    private Long supplierId;
    private String supplierName;
    private Long warehouseId;
    private String warehouseName;

    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private LocalDateTime orderDate;

    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private LocalDateTime expectedDate;

    private BigDecimal totalAmount;
    private BigDecimal discountAmount;
    private BigDecimal paidAmount;
    private Integer orderStatus;
    private String orderStatusText;
    private Long approvedBy;
    private String approvedByName;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "GMT+8")
    private LocalDateTime approvedAt;

    private Long handlerId;
    private String handlerName;
    private String remark;
    private List<PurchaseOrderItemVO> items;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "GMT+8")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "GMT+8")
    private LocalDateTime updatedAt;

    @Data
    public static class PurchaseOrderItemVO {
        private Long id;
        private Long productId;
        private String productCode;
        private String productName;
        private String specification;
        private String unit;
        private BigDecimal quantity;
        private BigDecimal price;
        private BigDecimal discountAmount;
        private BigDecimal amount;
        private BigDecimal receivedQuantity;
        private BigDecimal receivedAmount;
        private String remark;

        /** 可入库数量 = 订单数量 - 已入库数量 */
        private BigDecimal receivableQuantity;
    }
}
