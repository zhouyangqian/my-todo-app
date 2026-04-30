package com.example.erp.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 库存实体类
 * <p>
 * 对应数据库表 erp_inventory，用于管理ERP系统中各仓库的商品库存信息。
 * 记录商品在不同仓库中的实际库存数量、锁定数量（已下单未出库）和可用数量，
 * 支持批次管理、成本价跟踪、保质期管理（生产日期/过期日期），
 * 使用乐观锁（version字段）防止并发修改导致的库存超卖问题。
 * </p>
 *
 * @author ERP系统
 * @since 1.0
 */
@Data
@TableName("erp_inventory")
public class Inventory implements Serializable {

    /** 序列化版本号 */
    private static final long serialVersionUID = 1L;

    /**
     * 库存ID（主键，自增）
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 租户ID，用于多租户数据隔离，插入时自动填充
     */
    @TableField(fill = FieldFill.INSERT)
    private Long tenantId;

    /**
     * 仓库ID，关联 erp_warehouse 表，标识库存所在的仓库
     */
    private Long warehouseId;

    /**
     * 商品ID，关联 erp_product 表，标识库存对应的商品
     */
    private Long productId;

    /**
     * 批次号，用于商品的批次管理，可追溯同一批次商品的出入库记录
     */
    private String batchNo;

    /**
     * 库存数量，商品在仓库中的实际总数量
     */
    private BigDecimal quantity;

    /**
     * 锁定数量，已下单但尚未出库的数量。可用数量 = 库存数量 - 锁定数量
     */
    private BigDecimal lockedQuantity;

    /**
     * 可用数量，可供新订单使用的库存数量，等于库存数量减去锁定数量
     */
    private BigDecimal availableQuantity;

    /**
     * 库存下限，低于此值触发库存不足预警
     */
    private BigDecimal stockMin;

    /**
     * 库存上限，高于此值触发库存积压预警
     */
    private BigDecimal stockMax;

    /**
     * 成本价，当前批次商品的单位成本价格，用于成本核算
     */
    private BigDecimal costPrice;

    /**
     * 生产日期，商品的生产日期，用于保质期管理
     */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private LocalDateTime productionDate;

    /**
     * 过期日期，商品的有效期限，用于临期预警和过期商品处理
     */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private LocalDateTime expiryDate;

    /**
     * 状态：0-禁用，1-正常。禁用的库存记录不参与业务操作
     */
    private Integer status;

    /**
     * 创建时间，记录库存记录的创建时间戳，插入时自动填充
     */
    @TableField(fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private LocalDateTime createdAt;

    /**
     * 更新时间，记录库存记录最近一次修改的时间戳，插入和更新时自动填充
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private LocalDateTime updatedAt;

    /**
     * 版本号，乐观锁字段。每次更新库存时版本号自增，防止并发修改导致的数据不一致
     */
    @Version
    private Integer version;
}
