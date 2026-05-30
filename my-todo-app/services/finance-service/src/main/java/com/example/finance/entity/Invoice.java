package com.example.finance.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 发票实体类
 * <p>
 * 对应数据库表：fin_invoice
 * 用于管理企业的发票信息，支持增值税专用发票、增值税普通发票和电子发票三种类型。
 * 发票方向分为开票（销售方向客户开具）和收票（采购方从供应商接收）。
 * 包含不含税金额、税额、价税合计和税率等完整的税务信息。
 * </p>
 * <p>
 * 主要业务场景：
 * <ul>
 *   <li>销售业务向客户开具发票（开票方向）</li>
 *   <li>采购业务接收供应商发票（收票方向）</li>
 *   <li>发票作废与税务信息管理</li>
 * </ul>
 * </p>
 *
 * @author finance-team
 * @since 1.0
 */
@Data
@TableName("fin_invoice")
public class Invoice implements Serializable {

    /** 序列化版本号，用于保证序列化兼容性 */
    private static final long serialVersionUID = 1L;

    /** 发票主键ID，自增长 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 租户ID，用于多租户数据隔离，插入时自动填充 */
    @TableField(fill = FieldFill.INSERT)
    private Long tenantId;

    /** 发票编号，发票的唯一编号（如FP20260101XXXXXX） */
    private String invoiceNo;

    /** 发票代码，税务机关分配的发票代码 */
    private String invoiceCode;

    /** 发票类型：1-增值税专用发票（可抵扣进项税），2-增值税普通发票，3-电子发票 */
    private Integer invoiceType;

    /** 发票方向：1-开票（销售方向客户开具），2-收票（采购方从供应商接收） */
    private Integer invoiceDirection;

    /** 关联业务ID，如销售订单ID、采购订单ID等，用于追溯来源单据 */
    private Long bizId;

    /** 客户或供应商ID，标识发票的对方单位，根据发票方向区分含义 */
    private Long partnerId;

    /** 往来单位类型：1-客户, 2-供应商 */
    private Integer partnerType;

    /** 往来单位名称 */
    private String partnerName;

    /** 开票日期，即发票上注明的开具日期 */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private LocalDateTime invoiceDate;

    /** 币种编码，默认CNY（人民币） */
    private String currency;

    /** 不含税金额（税前金额），使用BigDecimal保证财务精度 */
    private BigDecimal amountWithoutTax;

    /** 税额，即按税率计算的增值税金额 */
    private BigDecimal taxAmount;

    /** 价税合计（含税总额），= 不含税金额 + 税额 */
    private BigDecimal totalAmount;

    /** 税率，如13%、9%、6%等，以小数形式存储（如0.13表示13%） */
    private BigDecimal taxRate;

    /** 发票状态：0-待开票（尚未开具），1-已开票（已正常开具），2-已作废（已作废处理） */
    private Integer status;

    /** 作废原因 */
    private String voidReason;

    /** 作废操作人ID */
    private Long voidBy;

    /** 作废时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime voidAt;

    /** 备注信息，用于补充说明该张发票的相关信息 */
    private String remark;

    /** 软删除标记：0-未删除，1-已删除。配合@TableLogic实现逻辑删除 */
    @TableLogic
    @TableField(fill = FieldFill.INSERT)
    private Integer deleted;

    /** 创建人ID，记录该条数据的创建者，插入时自动填充 */
    @TableField(fill = FieldFill.INSERT)
    private Long createdBy;

    /** 创建时间，记录数据创建的时间戳，插入时自动填充 */
    @TableField(fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private LocalDateTime createdAt;

    /** 更新人ID，记录最后一次修改该数据的操作者，更新时自动填充 */
    @TableField(fill = FieldFill.UPDATE)
    private Long updatedBy;

    /** 更新时间，记录数据最后一次修改的时间戳，插入和更新时均自动填充 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private LocalDateTime updatedAt;
}
