package com.example.user.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.common.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

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
 *     <li>继承 BaseEntity - 包含主键、租户ID、软删除标记、创建/更新时间等通用字段</li>
 *     <li>@TableField - 指定字段的自动填充策略</li>
 * </ul>
 * </p>
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_user")
public class User extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 用户名（登录账号），在同一租户下必须唯一
     */
    @TableField("user_name")
    private String userName;

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
}
