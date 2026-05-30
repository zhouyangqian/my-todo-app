package com.example.user.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户角色关联实体
 * <p>
 * 对应数据库表 sys_user_role，记录用户与角色的多对多关联关系。
 * 不继承 BaseEntity，使用简单的自增主键。
 * </p>
 */
@Data
@TableName("sys_user_role")
public class UserRole implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField(fill = FieldFill.INSERT)
    private Long tenantId;

    private Long userId;

    private Long roleId;

    private LocalDateTime createdAt;
}
