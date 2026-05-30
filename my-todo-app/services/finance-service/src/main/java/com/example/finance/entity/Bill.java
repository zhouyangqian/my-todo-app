package com.example.finance.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.example.common.core.entity.BaseEntity;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 统一账单实体类
 * <p>
 * 对应数据库表：fin_bill
 * 统一管理应收账款和应付账款，支持完整的账单生命周期：
 * 草稿 -> 待审核 -> 已审核 -> 部分收付 -> 已完成（或已取消）。
 * 支持红字冲销（direction=-1）处理退货场景。
 * </p>
 *
 * @author finance-team
 * @since 1.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("fin_bill")
public class Bill extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 账单编号，唯一标识 */
    private String billNo;

    /** 账单类型：1=应收(RECEIVABLE), 2=应付(PAYABLE) */
    private Integer billType;

    /** 方向：1=正数(正常), -1=红字(退货/冲销) */
    private Integer direction;

    /** 往来单位类型：1=客户, 2=供应商 */
    private Integer partnerType;

    /** 往来单位ID */
    private Long partnerId;

    /** 往来单位名称 */
    private String partnerName;

    /** 关联资金账户ID */
    private Long accountId;

    /** 币种编码，默认CNY */
    private String currency;

    /** 汇率，默认1.0 */
    private BigDecimal exchangeRate;

    /** 账单金额 */
    private BigDecimal amount;

    /** 本位币金额（= amount * exchangeRate） */
    private BigDecimal baseAmount;

    /** 已收/已付金额，默认0 */
    private BigDecimal paidAmount;

    /** 账单日期 */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private LocalDate billDate;

    /** 到期日期 */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private LocalDate dueDate;

    /** 状态：0=草稿, 1=待审核, 2=已审核, 3=部分收付, 4=已完成, 5=已取消 */
    private Integer status;

    /** 来源类型：PURCHASE/SALE/MANUAL */
    private String sourceType;

    /** 来源单据ID */
    private Long sourceId;

    /** 来源单据编号 */
    private String sourceNo;

    /** 审核状态：0=无, 1=待审核, 2=已通过, 3=已驳回 */
    private Integer auditStatus;

    /** 审核人ID */
    private Long auditBy;

    /** 审核时间 */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private LocalDateTime auditAt;

    /** 审核备注 */
    private String auditRemark;

    /** 备注 */
    private String remark;
}
