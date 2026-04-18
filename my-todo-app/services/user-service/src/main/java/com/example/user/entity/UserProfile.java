package com.example.user.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户详细信息实体类
 * <p>
 * 对应数据库表 sys_user_profile，存储用户的扩展个人信息，
 * 包括个人资料（昵称、性别、生日等）和人事信息（部门、职位、入职/离职日期等）。
 * 与 User 实体是一对一关系，以 userId 作为主键。
 * 用于补充 User 实体中不包含的详细字段，实现信息的分表存储。
 * </p>
 */
@Data
@TableName("sys_user_profile")
public class UserProfile implements Serializable {

    /** 序列化版本号，用于保证序列化兼容性 */
    private static final long serialVersionUID = 1L;

    /**
     * 用户ID（主键，与 sys_user 表的 id 一一对应）
     */
    @TableId(type = IdType.INPUT)  // 主键策略为INPUT，由外部传入（即用户ID）
    private Long userId;

    /**
     * 租户ID，用于多租户数据隔离
     * <p>在插入记录时自动填充当前用户的租户ID</p>
     */
    @TableField(fill = FieldFill.INSERT)  // 插入时自动填充
    private Long tenantId;

    /**
     * 真实姓名
     */
    private String realName;

    /**
     * 用户昵称（可不同于用户名，用于显示）
     */
    private String nickname;

    /**
     * 头像URL地址
     */
    private String avatar;

    /**
     * 性别：0-未知，1-男，2-女
     */
    private Integer gender;

    /**
     * 出生日期
     */
    private LocalDateTime birthday;

    /**
     * 身份证号码（敏感信息，需加密存储）
     */
    private String idCard;

    /**
     * 所属部门ID，关联部门表
     */
    private Long deptId;

    /**
     * 职位名称（如：Java开发工程师、产品经理等）
     */
    private String position;

    /**
     * 入职日期
     */
    private LocalDateTime hireDate;

    /**
     * 离职日期（为空表示在职）
     */
    private LocalDateTime leaveDate;

    /**
     * 联系地址（文本格式的完整地址）
     */
    private String address;

    /**
     * 备注信息（管理员可填写的额外说明）
     */
    private String remark;

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
