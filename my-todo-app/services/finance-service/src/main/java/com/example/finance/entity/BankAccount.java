package com.example.finance.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 银行账户实体类
 * <p>
 * 对应数据库表：fin_bank_account
 * 用于管理企业的各类资金账户，包括现金账户、银行账户、支付宝、微信等。
 * 每个租户可拥有多个账户，但只能设置一个默认账户。
 * 收支记录审核通过后会自动更新对应账户的余额。
 * </p>
 * <p>
 * 主要业务场景：
 * <ul>
 *   <li>新增和管理企业的资金账户信息</li>
 *   <li>收支审核时自动调整账户余额</li>
 *   <li>设置默认账户用于快速操作</li>
 * </ul>
 * </p>
 *
 * @author finance-team
 * @since 1.0
 */
@Data
@TableName("fin_bank_account")
public class BankAccount implements Serializable {

    /** 序列化版本号，用于保证序列化兼容性 */
    private static final long serialVersionUID = 1L;

    /** 账户主键ID，自增长 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 租户ID，用于多租户数据隔离，插入时自动填充 */
    @TableField(fill = FieldFill.INSERT)
    private Long tenantId;

    /** 账户编码，唯一标识该账户的编号（如 BK001），在创建时校验唯一性 */
    private String accountCode;

    /** 账户名称，便于识别的账户描述名称（如"中国银行基本户"） */
    private String accountName;

    /** 账户类型：1-现金账户, 2-银行账户, 3-支付宝, 4-微信 */
    private Integer accountType;

    /** 开户银行名称（如"中国银行"、"工商银行"等），仅银行账户类型时有效 */
    private String bankName;

    /** 银行账号，实际银行卡号或支付账号 */
    private String bankAccountNo;

    /** 账户余额，使用BigDecimal保证财务精度，创建时初始化为0 */
    private BigDecimal balance;

    /** 币种编码，如 CNY(人民币)、USD(美元) 等 */
    private String currency;

    /** 账户状态：0-停用（不可用于交易），1-启用（正常使用中） */
    private Integer status;

    /** 是否为默认账户：0-否, 1-是。每个租户仅允许一个默认账户，设置新默认时会自动清除旧的 */
    private Integer isDefault;

    /** 备注信息，用于补充说明该账户的相关信息 */
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
