package com.example.erp.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 商品实体类
 * <p>
 * 对应数据库表 erp_product，用于管理ERP系统中的商品基础信息。
 * 包含商品的编码、名称、分类、价格体系（成本价/销售价/最低售价）、
 * 库存预警上下限、状态等核心字段。
 * 支持多租户隔离（tenantId）、软删除（deleted）和自动填充审计字段。
 * </p>
 *
 * @author ERP系统
 * @since 1.0
 */
@Data
@TableName("erp_product")
public class Product implements Serializable {

    /** 序列化版本号 */
    private static final long serialVersionUID = 1L;

    /**
     * 商品ID（主键，自增）
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 租户ID，用于多租户数据隔离，插入时自动填充
     */
    @TableField(fill = FieldFill.INSERT)
    private Long tenantId;

    /**
     * 商品编码，唯一标识商品的编号，同一租户下不可重复
     */
    private String productCode;

    /**
     * 商品编码（兼容前端字段名）
     */
    @TableField(exist = false)
    private String sku;

    /**
     * 商品名称，用于显示和搜索
     */
    private String productName;

    /**
     * 商品名称（兼容前端字段名）
     */
    @TableField(exist = false)
    private String name;

    /**
     * 品牌
     */
    private String brand;

    /**
     * 型号
     */
    private String model;

    /**
     * 商品分类ID，关联商品分类表，用于商品分类管理
     */
    private Long categoryId;

    /**
     * 条码，商品的国际条形码或自定义条码，用于扫码识别
     */
    private String barcode;

    /**
     * 规格，描述商品的规格型号，如"500ml"、"大号"等
     */
    private String specification;

    /**
     * 单位，商品的计量单位，如"个"、"箱"、"千克"等
     */
    private String unit;

    /**
     * 成本价，商品的采购成本价格，用于利润计算
     */
    private BigDecimal costPrice;

    /**
     * 销售价，商品的标准销售价格
     */
    private BigDecimal salePrice;

    /**
     * 最低售价，商品允许的最低销售价格，低于此价格时系统预警
     */
    private BigDecimal minPrice;

    /**
     * 库存数量，商品当前的库存总量，创建时默认为0
     */
    private BigDecimal stockQuantity;

    /**
     * 库存预警下限，当库存数量低于此值时触发低库存预警
     */
    private BigDecimal stockMin;

    /**
     * 库存预警上限，当库存数量高于此值时触发高库存预警
     */
    private BigDecimal stockMax;

    /**
     * 状态：0-停用，1-启用。停用的商品不可用于业务操作
     */
    private Integer status;

    /**
     * 商品图片URL，存储商品图片的访问地址
     */
    private String imageUrl;

    /**
     * 描述
     */
    @TableField("remark")
    private String description;

    /**
     * 软删除标记：0-未删除，1-已删除。使用逻辑删除避免数据物理丢失
     */
    @TableLogic
    @TableField(fill = FieldFill.INSERT)
    private Integer deleted;

    /**
     * 创建人ID，记录商品的创建者，插入时自动填充
     */
    @TableField(fill = FieldFill.INSERT)
    private Long createdBy;

    /**
     * 创建时间，记录商品的创建时间戳，插入时自动填充
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
