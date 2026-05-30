package com.example.erp.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 客户实体类
 * <p>
 * 对应数据库表 erp_customer，用于管理ERP系统中的客户基础信息。
 * 支持企业客户和个人客户两种类型，记录客户的联系方式、银行账户信息、
 * 税务信息以及信用额度和结算方式等，是销售业务的核心主数据。
 * 支持多租户隔离（tenantId）、软删除（deleted）和自动填充审计字段。
 * </p>
 *
 * @author ERP系统
 * @since 1.0
 */
@Data
@TableName("erp_customer")
public class Customer implements Serializable {

    /** 序列化版本号 */
    private static final long serialVersionUID = 1L;

    /**
     * 客户ID（主键，自增）
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 租户ID，用于多租户数据隔离，插入时自动填充
     */
    @TableField(fill = FieldFill.INSERT)
    private Long tenantId;

    /**
     * 客户编码，唯一标识客户的编号，同一租户下不可重复
     */
    private String customerCode;

    /**
     * 客户名称，客户的全称，用于显示和搜索
     */
    private String customerName;

    /**
     * 客户类型：1-企业（公司客户），2-个人（个人客户）。不同类型影响开票和结算方式
     */
    private Integer customerType;

    /**
     * 联系人，客户方的主要对接联系人姓名
     */
    private String contactPerson;

    /**
     * 联系电话，客户联系人的电话号码
     */
    private String phone;

    /**
     * 邮箱，客户联系人的电子邮箱地址
     */
    private String email;

    /**
     * 地址，客户的经营场所或收货地址
     */
    private String address;

    /**
     * 开户银行，客户的银行开户行名称，用于收款时确认
     */
    private String bankName;

    /**
     * 银行账号，客户的银行账户号码
     */
    private String bankAccount;

    /**
     * 税号，客户的纳税人识别号，用于开具销售发票
     */
    private String taxNo;

    /**
     * 信用额度，客户可赊账的最大金额。超出此额度的订单需审批或现结
     */
    private BigDecimal creditLimit;

    /**
     * 当前欠款，客户当前未结算的销售金额
     */
    private BigDecimal currentDebt;

    /**
     * 状态：0-停用，1-启用。停用的客户不可用于销售业务
     */
    private Integer status;

    /**
     * 结算方式：1-现结（货到付款），2-月结（每月结算一次），3-账期（约定天数后收款）
     */
    private Integer settlementType;

    /**
     * 账期天数，当结算方式为"账期"时生效，表示从发货到收款的天数
     */
    private Integer creditDays;

    /**
     * 备注，用于记录客户的补充说明信息
     */
    private String remark;

    /**
     * 软删除标记：0-未删除，1-已删除。使用逻辑删除避免数据物理丢失
     */
    @TableLogic
    @TableField(fill = FieldFill.INSERT)
    private Integer deleted;

    /**
     * 创建人ID，记录客户的创建者，插入时自动填充
     */
    @TableField(fill = FieldFill.INSERT)
    private Long createdBy;

    /**
     * 创建时间，记录客户的创建时间戳，插入时自动填充
     */
    @TableField(fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private LocalDateTime createdAt;

    /**
     * 更新人ID，记录最近一次修改者，更新时自动填充
     */
    @TableField(fill = FieldFill.UPDATE)
    private Long updatedBy;

    /**
     * 更新时间，记录最近一次修改的时间戳，插入和更新时自动填充
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private LocalDateTime updatedAt;
}
