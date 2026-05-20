package com.example.erp.api.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 采购订单视图对象
 */
@Data
public class PurchaseOrderDTO {
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
    private List<PurchaseOrderItemDTO> items;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "GMT+8")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "GMT+8")
    private LocalDateTime updatedAt;

}
