package com.example.erp.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 仓库实体类
 * <p>
 * 对应数据库表 erp_warehouse，用于管理ERP系统中的仓库基础信息。
 * 支持多种仓库类型（普通仓、门店仓、虚拟仓），可设置默认仓库，
 * 每个租户可拥有多个仓库，通过负责人管理仓库日常运营。
 * 支持多租户隔离（tenantId）、软删除（deleted）和自动填充审计字段。
 * </p>
 *
 * @author ERP系统
 * @since 1.0
 */
@Data
@TableName("erp_warehouse")
public class Warehouse implements Serializable {

    /** 序列化版本号 */
    private static final long serialVersionUID = 1L;

    /**
     * 仓库ID（主键，自增）
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 租户ID，用于多租户数据隔离，插入时自动填充
     */
    @TableField(fill = FieldFill.INSERT)
    private Long tenantId;

    /**
     * 仓库编码，唯一标识仓库的编号，同一租户下不可重复
     */
    private String warehouseCode;

    /**
     * 仓库名称，用于显示和搜索
     */
    private String warehouseName;

    /**
     * 仓库类型：1-普通仓（常规存储仓库），2-门店仓（门店附属仓库），3-虚拟仓（逻辑仓库，用于特殊业务场景）
     */
    private Integer warehouseType;

    /**
     * 负责人ID，仓库管理员的用户ID，关联用户表
     */
    private Long managerId;

    /**
     * 联系电话，仓库的联系电话，方便物流和业务沟通
     */
    private String phone;

    /**
     * 地址，仓库的物理地址
     */
    private String address;

    /**
     * 状态：0-停用，1-启用。停用的仓库不可用于出入库操作
     */
    private Integer status;

    /**
     * 是否默认仓库：0-否，1-是。每个租户只能有一个默认仓库，新建仓库时若设为默认则自动清除其他默认标记
     */
    private Integer isDefault;

    /**
     * 备注，用于记录仓库的补充说明信息
     */
    private String remark;

    /**
     * 软删除标记：0-未删除，1-已删除。使用逻辑删除避免数据物理丢失
     */
    @TableLogic
    @TableField(fill = FieldFill.INSERT)
    private Integer deleted;

    /**
     * 创建人ID，记录仓库的创建者，插入时自动填充
     */
    @TableField(fill = FieldFill.INSERT)
    private Long createdBy;

    /**
     * 创建时间，记录仓库的创建时间戳，插入时自动填充
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
