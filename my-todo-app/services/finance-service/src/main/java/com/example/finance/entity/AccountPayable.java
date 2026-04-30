package com.example.finance.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 应付账款实体类
 * <p>
 * 对应数据库表：fin_account_payable
 * 用于记录企业因采购商品或接受劳务而应向供应商支付的款项。
 * 支持多租户隔离、软删除，以及按供应商、金额、结算状态进行管理。
 * </p>
 * <p>
 * 主要业务场景：
 * <ul>
 *   <li>采购订单确认后自动生成应付记录</li>
 *   <li>向供应商付款时更新已付金额和未付金额</li>
 *   <li>逾期应付账款统计与付款提醒</li>
 * </ul>
 * </p>
 *
 * @author finance-team
 * @since 1.0
 */
@Data
@TableName("fin_account_payable")
public class AccountPayable implements Serializable {

    /** 序列化版本号，用于保证序列化兼容性 */
    private static final long serialVersionUID = 1L;

    /** 应付记录主键ID，自增长 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 租户ID，用于多租户数据隔离，插入时自动填充 */
    @TableField(fill = FieldFill.INSERT)
    private Long tenantId;

    /** 业务单号，关联采购订单等业务单据的唯一编号 */
    private String bizNo;

    /** 供应商ID，关联供应商信息表，标识收款方 */
    private Long supplierId;

    /** 应付金额（总金额），使用BigDecimal保证财务精度 */
    private BigDecimal amount;

    /** 已付金额，累计已支付给供应商的款项金额 */
    private BigDecimal paidAmount;

    /** 未付金额，尚未支付的款项金额（= 应付金额 - 已付金额） */
    private BigDecimal unpaidAmount;

    /** 币种编码，如 CNY(人民币)、USD(美元) 等 */
    private String currency;

    /** 业务日期，即采购或服务发生的实际日期 */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private LocalDateTime bizDate;

    /** 应付日期（到期日期），超过此日期未付款则视为逾期 */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private LocalDateTime dueDate;

    /** 结算状态：0-未结算（全额未付），1-部分结算（已付部分），2-已结算（全额已付） */
    private Integer status;

    /** 备注信息，用于补充说明该笔应付账款的相关信息 */
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
