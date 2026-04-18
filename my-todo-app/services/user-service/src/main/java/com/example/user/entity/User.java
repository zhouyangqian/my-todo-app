package com.example.user.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户实体类（本地存储，数据从认证服务同步）
 * <p>
 * 对应数据库表 sys_user，存储用户的基本信息，包括账号、联系方式、
 * 部门归属和状态等。支持多租户数据隔离和软删除机制。
 * 该实体的用户数据由认证服务（auth-service）同步过来，本地只做读取和管理。
 * </p>
 * <p>
 * 使用 MyBatis-Plus 注解实现 ORM 映射：
 * <ul>
 *     <li>@TableName - 指定对应的数据库表名</li>
 *     <li>@TableId - 指定主键及其生成策略（INPUT 表示由外部输入）</li>
 *     <li>@TableField - 指定字段的自动填充策略</li>
 *     <li>@TableLogic - 标记逻辑删除字段</li>
 * </ul>
 * </p>
 */
@Data
@TableName("sys_user")
public class User implements Serializable {

    /** 序列化版本号，用于保证序列化兼容性 */
    private static final long serialVersionUID = 1L;

    /**
     * 用户ID（主键，由认证服务生成后同步过来）
     */
    @TableId(type = IdType.INPUT)  // 主键策略为INPUT，表示ID由外部传入，不自增
    private Long id;

    /**
     * 租户ID，用于多租户数据隔离
     * <p>在插入记录时自动填充当前用户的租户ID</p>
     */
    @TableField(fill = FieldFill.INSERT)  // 插入时自动填充
    private Long tenantId;

    /**
     * 用户名（登录账号），在同一租户下必须唯一
     */
    private String username;

    /**
     * 邮箱地址
     */
    private String email;

    /**
     * 手机号码
     */
    private String phone;

    /**
     * 真实姓名
     */
    private String realName;

    /**
     * 头像URL地址
     */
    private String avatar;

    /**
     * 所属部门ID，关联部门表
     */
    private Long deptId;

    /**
     * 用户状态：0-禁用（无法登录），1-启用（正常使用）
     */
    private Integer status;

    /**
     * 逻辑删除标志：0-未删除，1-已删除
     * <p>MyBatis-Plus 的 @TableLogic 注解会自动在查询时添加 deleted=0 条件</p>
     */
    @TableLogic  // 标记为逻辑删除字段，查询时自动过滤已删除记录
    @TableField(fill = FieldFill.INSERT)  // 插入时自动填充默认值0
    private Integer deleted;

    /**
     * 创建人ID，记录是谁创建了该用户
     * <p>在插入记录时自动填充当前操作用户ID</p>
     */
    @TableField(fill = FieldFill.INSERT)  // 插入时自动填充
    private Long createdBy;

    /**
     * 创建时间
     * <p>在插入记录时自动填充当前时间</p>
     */
    @TableField(fill = FieldFill.INSERT)  // 插入时自动填充
    private LocalDateTime createdAt;

    /**
     * 最后更新人ID，记录是谁最后修改了该用户信息
     * <p>在更新记录时自动填充当前操作用户ID</p>
     */
    @TableField(fill = FieldFill.UPDATE)  // 更新时自动填充
    private Long updatedBy;

    /**
     * 最后更新时间
     * <p>在插入和更新记录时自动填充当前时间</p>
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)  // 插入和更新时均自动填充
    private LocalDateTime updatedAt;
}
