package com.example.permission.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 角色实体类
 * <p>
 * 对应数据库表 sys_role，用于存储系统中的角色信息。
 * 角色是权限分配的核心载体，通过角色-权限关联表实现权限分配，
 * 通过用户-角色关联表实现角色绑定。
 * 支持角色层级结构（通过parentId），以及数据权限范围控制（通过dataScope）。
 * </p>
 */
@Data
@TableName("sys_role")
public class Role implements Serializable {

    /** 序列化版本号 */
    private static final long serialVersionUID = 1L;

    /** 角色主键ID，自增长 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 租户ID，用于多租户数据隔离，插入时自动填充 */
    @TableField(fill = FieldFill.INSERT)
    private Long tenantId;

    /** 角色编码，全局唯一标识角色（如 "ADMIN"、"EDITOR"） */
    private String roleCode;

    /** 角色名称，用于界面展示（如 "管理员"、"编辑员"） */
    private String roleName;

    /** 角色描述，用于说明角色的职责和使用场景 */
    private String description;

    /** 父角色ID，用于构建角色层级结构（角色继承），顶级角色parentId为0或null */
    private Long parentId;

    /** 排序序号，值越小越靠前 */
    private Integer sort;

    /** 状态：0-禁用，1-启用 */
    private Integer status;

    /** 数据权限范围：1-全部数据，2-本部门数据，3-仅本人数据 */
    private Integer dataScope;

    /** 逻辑删除标志：0-未删除，1-已删除，使用MyBatis-Plus逻辑删除自动处理 */
    @TableLogic
    @TableField(fill = FieldFill.INSERT)
    private Integer deleted;

    /** 创建人ID，插入时自动填充 */
    @TableField(fill = FieldFill.INSERT)
    private Long createdBy;

    /** 创建时间，插入时自动填充 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /** 最后更新人ID，更新时自动填充 */
    @TableField(fill = FieldFill.UPDATE)
    private Long updatedBy;

    /** 最后更新时间，插入和更新时自动填充 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
