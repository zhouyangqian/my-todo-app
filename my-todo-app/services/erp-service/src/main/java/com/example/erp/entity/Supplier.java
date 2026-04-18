package com.example.erp.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 供应商实体类
 * <p>
 * 对应数据库表 erp_supplier，用于管理ERP系统中的供应商基础信息。
 * 记录供应商的联系方式、银行账户信息、税务信息以及结算方式等，
 * 支持多种结算方式（现结/月结/账期），是采购业务的核心主数据。
 * 支持多租户隔离（tenantId）、软删除（deleted）和自动填充审计字段。
 * </p>
 *
 * @author ERP系统
 * @since 1.0
 */
@Data
@TableName("erp_supplier")
public class Supplier implements Serializable {

    /** 序列化版本号 */
    private static final long serialVersionUID = 1L;

    /**
     * 供应商ID（主键，自增）
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 租户ID，用于多租户数据隔离，插入时自动填充
     */
    @TableField(fill = FieldFill.INSERT)
    private Long tenantId;

    /**
     * 供应商编码，唯一标识供应商的编号，同一租户下不可重复
     */
    private String supplierCode;

    /**
     * 供应商名称，供应商的全称，用于显示和搜索
     */
    private String supplierName;

    /**
     * 联系人，供应商方的主要对接联系人姓名
     */
    private String contactPerson;

    /**
     * 联系电话，供应商联系人的电话号码
     */
    private String phone;

    /**
     * 邮箱，供应商联系人的电子邮箱地址
     */
    private String email;

    /**
     * 地址，供应商的经营场所或办公地址
     */
    private String address;

    /**
     * 开户银行，供应商的银行开户行名称，用于付款时填写
     */
    private String bankName;

    /**
     * 银行账号，供应商的银行账户号码，用于付款转账
     */
    private String bankAccount;

    /**
     * 税号，供应商的纳税人识别号，用于开具发票
     */
    private String taxNo;

    /**
     * 状态：0-停用，1-启用。停用的供应商不可用于采购业务
     */
    private Integer status;

    /**
     * 结算方式：1-现结（货到付款），2-月结（每月结算一次），3-账期（约定天数后付款）
     */
    private Integer settlementType;

    /**
     * 账期天数，当结算方式为"账期"时生效，表示从收货到付款的天数
     */
    private Integer creditDays;

    /**
     * 备注，用于记录供应商的补充说明信息
     */
    private String remark;

    /**
     * 软删除标记：0-未删除，1-已删除。使用逻辑删除避免数据物理丢失
     */
    @TableLogic
    @TableField(fill = FieldFill.INSERT)
    private Integer deleted;

    /**
     * 创建人ID，记录供应商的创建者，插入时自动填充
     */
    @TableField(fill = FieldFill.INSERT)
    private Long createdBy;

    /**
     * 创建时间，记录供应商的创建时间戳，插入时自动填充
     */
    @TableField(fill = FieldFill.INSERT)
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
    private LocalDateTime updatedAt;
}
