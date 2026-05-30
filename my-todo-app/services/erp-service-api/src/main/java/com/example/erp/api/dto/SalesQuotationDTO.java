package com.example.erp.api.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 销售报价单视图对象
 * 用于返回带有关联信息的销售报价单数据
 */
@Data
public class SalesQuotationDTO {

    /** 报价单ID */
    private Long id;

    /** 报价单编号 */
    private String quotationNo;

    /** 客户ID */
    private Long customerId;

    /** 客户名称 */
    private String customerName;

    /** 报价日期 */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private LocalDate quotationDate;

    /** 有效期至 */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private LocalDate validUntil;

    /** 总金额 */
    private BigDecimal totalAmount;

    /** 报价单状态 */
    private Integer status;

    /** 报价单状态文本 */
    private String statusText;

    /** 转订单ID */
    private Long convertedOrderId;

    /** 转订单编号 */
    private String convertedOrderNo;

    /** 备注 */
    private String remark;

    /** 报价明细列表 */
    private List<SalesQuotationItemDTO> items;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "GMT+8")
    private LocalDateTime createdAt;

    /** 更新时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "GMT+8")
    private LocalDateTime updatedAt;
}
