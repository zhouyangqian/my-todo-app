package com.example.user.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户收货地址实体类
 * <p>
 * 对应数据库表 sys_user_address，存储用户的收货地址信息。
 * 每个用户可以有多个收货地址，但只能有一个默认地址。
 * 支持省/市/区三级地址划分，并记录收货人和联系电话。
 * 支持多租户数据隔离和软删除机制。
 * </p>
 */
@Data
@TableName("sys_user_address")
public class UserAddress implements Serializable {

    /** 序列化版本号，用于保证序列化兼容性 */
    private static final long serialVersionUID = 1L;

    /**
     * 地址ID（主键，数据库自增）
     */
    @TableId(type = IdType.AUTO)  // 主键策略为AUTO，由数据库自动递增生成
    private Long id;

    /**
     * 租户ID，用于多租户数据隔离
     * <p>在插入记录时自动填充当前用户的租户ID</p>
     */
    @TableField(fill = FieldFill.INSERT)  // 插入时自动填充
    private Long tenantId;

    /**
     * 所属用户ID，关联 sys_user 表的主键
     */
    private Long userId;

    /**
     * 收货人姓名
     */
    private String receiverName;

    /**
     * 收货人联系电话
     */
    private String receiverPhone;

    /**
     * 省份（如：广东省）
     */
    private String province;

    /**
     * 城市（如：深圳市）
     */
    private String city;

    /**
     * 区/县（如：南山区）
     */
    private String district;

    /**
     * 详细地址（街道、门牌号等具体地址信息）
     */
    private String detailAddress;

    /**
     * 是否为默认地址：0-否，1-是
     * <p>每个用户只能有一个默认地址，设置新的默认地址时会自动取消旧的</p>
     */
    private Integer isDefault;

    /**
     * 逻辑删除标志：0-未删除，1-已删除
     * <p>MyBatis-Plus 的 @TableLogic 注解会自动在查询时添加 deleted=0 条件</p>
     */
    @TableLogic  // 标记为逻辑删除字段
    @TableField(fill = FieldFill.INSERT)  // 插入时自动填充默认值0
    private Integer deleted;

    /**
     * 创建时间
     * <p>在插入记录时自动填充当前时间</p>
     */
    @TableField(fill = FieldFill.INSERT)  // 插入时自动填充
    private LocalDateTime createdAt;

    /**
     * 最后更新时间
     * <p>在插入和更新记录时自动填充当前时间</p>
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)  // 插入和更新时均自动填充
    private LocalDateTime updatedAt;
}
