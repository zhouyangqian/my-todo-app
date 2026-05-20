package com.example.erp.api.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 销售订单视图对象
 * 用于返回带有关联信息的销售订单数据
 */
@Data
public class SalesOrderDTO {
    /** 订单ID */
    private Long id;

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

    /** 订单日期 */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private LocalDateTime orderDate;

    /** 预计发货日期 */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private LocalDateTime expectedDate;

    /** 订单金额 */
    private BigDecimal totalAmount;

    /** 优惠金额 */
    private BigDecimal discountAmount;

    /** 实收金额 */
    private BigDecimal receivedAmount;

    /** 已发货金额 */
    private BigDecimal deliveredAmount;

    /** 已发货数量 */
    private BigDecimal deliveredQuantity;

    /** 订单状态 */
    private Integer orderStatus;

    /** 订单状态文本 */
    private String orderStatusText;

    /** 审核人ID */
    private Long approvedBy;

    /** 审核人姓名 */
    private String approvedByName;

    /** 审核时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "GMT+8")
    private LocalDateTime approvedAt;

    /** 销售员ID */
    private Long salesId;

    /** 销售员姓名 */
    private String salesName;

    /** 备注 */
    private String remark;

    /** 订单明细列表 */
    private List<SalesOrderItemDTO> items;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "GMT+8")
    private LocalDateTime createdAt;

    /** 更新时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "GMT+8")
    private LocalDateTime updatedAt;

}
