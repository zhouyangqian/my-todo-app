package com.example.finance.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 收支记录实体类
 * <p>
 * 对应数据库表：fin_payment_record
 * 用于记录企业所有的资金收支明细，包括销售收款、采购付款、退款等。
 * 每笔收支记录需经过审核流程，审核通过后自动更新关联银行账户余额。
 * 支持多种支付方式（现金、银行转账、支付宝、微信、支票）。
 * </p>
 * <p>
 * 主要业务场景：
 * <ul>
 *   <li>应收账款收款时生成收入记录</li>
 *   <li>应付账款付款时生成支出记录</li>
 *   <li>退款、其他收支的记录与审核</li>
 * </ul>
 * </p>
 *
 * @author finance-team
 * @since 1.0
 */
@Data
@TableName("fin_payment_record")
public class PaymentRecord implements Serializable {

    /** 序列化版本号，用于保证序列化兼容性 */
    private static final long serialVersionUID = 1L;

    /** 收支记录主键ID，自增长 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 租户ID，用于多租户数据隔离，插入时自动填充 */
    @TableField(fill = FieldFill.INSERT)
    private Long tenantId;

    /** 单据编号，系统自动生成的唯一编号（如SK20260101XXXXXX或FK20260101XXXXXX） */
    private String recordNo;

    /** 收支类型：1-收入（资金流入），2-支出（资金流出） */
    private Integer recordType;

    /** 业务类型：1-销售收款, 2-采购付款, 3-退款, 4-其他收入, 5-其他支出 */
    private Integer bizType;

    /** 关联业务ID，如销售订单ID、采购订单ID等，用于追溯来源单据 */
    private Long bizId;

    /** 客户或供应商ID，标识交易的对方，根据业务类型区分含义 */
    private Long partnerId;

    /** 收支金额，使用BigDecimal保证财务精度 */
    private BigDecimal amount;

    /** 币种编码，如 CNY(人民币)、USD(美元) 等 */
    private String currency;

    /** 支付方式：1-现金, 2-银行转账, 3-支付宝, 4-微信, 5-支票 */
    private Integer paymentMethod;

    /** 银行账户ID，关联银行账户表，标识资金收付的具体账户 */
    private Long bankAccountId;

    /** 交易日期，即资金实际收付发生的日期 */
    private LocalDateTime transactionDate;

    /** 经手人ID，记录实际办理该笔收支操作的人员 */
    private Long handlerId;

    /** 审核状态：0-待审核（刚创建），1-已审核（审核通过），2-已取消（作废） */
    private Integer status;

    /** 备注信息，用于补充说明该笔收支的相关信息 */
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
    private LocalDateTime createdAt;

    /** 更新人ID，记录最后一次修改该数据的操作者，更新时自动填充 */
    @TableField(fill = FieldFill.UPDATE)
    private Long updatedBy;

    /** 更新时间，记录数据最后一次修改的时间戳，插入和更新时均自动填充 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
