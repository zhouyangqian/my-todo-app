package com.example.finance.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 应收账款实体类
 * <p>
 * 对应数据库表：fin_account_receivable
 * 用于记录企业因销售商品或提供劳务而应向客户收取的款项。
 * 支持多租户隔离、软删除，以及按客户、金额、结算状态进行管理。
 * </p>
 * <p>
 * 主要业务场景：
 * <ul>
 *   <li>销售订单确认后自动生成应收记录</li>
 *   <li>客户付款时更新已收金额和未收金额</li>
 *   <li>逾期应收账款统计与催收管理</li>
 * </ul>
 * </p>
 *
 * @author finance-team
 * @since 1.0
 */
@Data
@TableName("fin_account_receivable")
public class AccountReceivable implements Serializable {

    /** 序列化版本号，用于保证序列化兼容性 */
    private static final long serialVersionUID = 1L;

    /** 应收记录主键ID，自增长 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 租户ID，用于多租户数据隔离，插入时自动填充 */
    @TableField(fill = FieldFill.INSERT)
    private Long tenantId;

    /** 业务单号，关联销售订单等业务单据的唯一编号 */
    private String bizNo;

    /** 客户ID，关联客户信息表，标识欠款客户 */
    private Long customerId;

    /** 应收金额（总金额），使用BigDecimal保证财务精度 */
    private BigDecimal amount;

    /** 已收金额，累计已收回的款项金额 */
    private BigDecimal receivedAmount;

    /** 未收金额，尚未收回的款项金额（= 应收金额 - 已收金额） */
    private BigDecimal unreceivedAmount;

    /** 币种编码，如 CNY(人民币)、USD(美元) 等 */
    private String currency;

    /** 业务日期，即销售或服务发生的实际日期 */
    private LocalDateTime bizDate;

    /** 应收日期（到期日期），超过此日期未收款则视为逾期 */
    private LocalDateTime dueDate;

    /** 结算状态：0-未结算（全额未收），1-部分结算（已收部分），2-已结算（全额已收） */
    private Integer status;

    /** 备注信息，用于补充说明该笔应收账款的相关信息 */
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
