package com.example.permission.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 数据权限规则实体类
 * <p>
 * 对应数据库表 sys_data_permission_rule，用于定义角色的数据权限过滤规则。
 * 每个角色可以配置多条规则（OR逻辑），支持按部门、个人、项目等维度过滤数据。
 * </p>
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("sys_data_permission_rule")
public class DataPermissionRule implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 规则主键ID，自增长 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 租户ID，用于多租户数据隔离 */
    @TableField(fill = FieldFill.INSERT)
    private Long tenantId;

    /** 关联角色ID */
    private Long roleId;

    /** 规则名称 */
    private String ruleName;

    /** 数据范围类型：ALL/DEPT/SELF/PROJECT/DEPT_AND_SUB */
    private String scopeType;

    /** 目标表名（为空则适用于所有表） */
    private String tableName;

    /** 部门字段名，默认 dept_id */
    private String deptColumn;

    /** 用户字段名，默认 created_by */
    private String userColumn;

    /** 排序序号 */
    private Integer sortOrder;

    /** 逻辑删除标志：0-未删除，1-已删除 */
    @TableLogic
    @TableField(fill = FieldFill.INSERT)
    private Integer deleted;

    /** 创建人ID */
    @TableField(fill = FieldFill.INSERT)
    private Long createdBy;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private LocalDateTime createdAt;

    /** 更新人ID */
    @TableField(fill = FieldFill.UPDATE)
    private Long updatedBy;

    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private LocalDateTime updatedAt;
}
