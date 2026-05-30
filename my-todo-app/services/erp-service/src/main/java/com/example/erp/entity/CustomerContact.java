package com.example.erp.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 客户联系人实体类
 * <p>
 * 对应数据库表 customer_contact，用于管理客户的多个联系人信息。
 * 支持设置默认联系人，记录联系人的职位、电话、邮箱等信息。
 * 支持多租户隔离（tenantId）、软删除（deleted）和自动填充审计字段。
 * </p>
 *
 * @author ERP系统
 * @since 1.0
 */
@Data
@TableName("customer_contact")
public class CustomerContact implements Serializable {

    /** 序列化版本号 */
    private static final long serialVersionUID = 1L;

    /**
     * 联系人ID（主键，自增）
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 租户ID，用于多租户数据隔离，插入时自动填充
     */
    @TableField(fill = FieldFill.INSERT)
    private Long tenantId;

    /**
     * 客户ID，关联客户表
     */
    private Long customerId;

    /**
     * 联系人姓名
     */
    private String name;

    /**
     * 职位
     */
    private String position;

    /**
     * 联系电话
     */
    private String phone;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 是否默认联系人：0-否，1-是。每个客户只能有一个默认联系人
     */
    private Integer isDefault;

    /**
     * 备注
     */
    private String remark;

    /**
     * 软删除标记：0-未删除，1-已删除
     */
    @TableLogic
    @TableField(fill = FieldFill.INSERT)
    private Integer deleted;

    /**
     * 创建人ID，插入时自动填充
     */
    @TableField(fill = FieldFill.INSERT)
    private Long createdBy;

    /**
     * 创建时间，插入时自动填充
     */
    @TableField(fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private LocalDateTime createdAt;

    /**
     * 更新人ID，更新时自动填充
     */
    @TableField(fill = FieldFill.UPDATE)
    private Long updatedBy;

    /**
     * 更新时间，插入和更新时自动填充
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private LocalDateTime updatedAt;
}
