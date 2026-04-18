package com.example.permission.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 部门实体类
 * <p>
 * 对应数据库表 sys_department，用于存储组织架构中的部门信息。
 * 支持树形结构的部门层级，通过 parentId 实现父子关系。
 * 支持多租户隔离，通过 tenantId 区分不同租户的部门数据。
 * </p>
 */
@Data
@TableName("sys_department")
public class Department implements Serializable {

    /** 序列化版本号 */
    private static final long serialVersionUID = 1L;

    /** 部门主键ID，自增长 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 租户ID，用于多租户数据隔离，插入时自动填充 */
    @TableField(fill = FieldFill.INSERT)
    private Long tenantId;

    /** 父部门ID，用于构建部门树形结构，顶级部门的parentId为0或null */
    private Long parentId;

    /** 部门编码，唯一标识部门的业务编码 */
    private String deptCode;

    /** 部门名称 */
    private String deptName;

    /** 部门完整路径（例如：/总公司/分公司/技术部），用于快速定位部门在树中的位置 */
    private String fullPath;

    /** 部门层级深度（树形结构中的深度，从0或1开始） */
    private Integer level;

    /** 部门负责人用户ID，关联用户表 */
    private Long leaderId;

    /** 部门联系电话 */
    private String phone;

    /** 部门联系邮箱 */
    private String email;

    /** 排序序号，值越小越靠前 */
    private Integer sort;

    /** 状态：0-禁用，1-启用 */
    private Integer status;

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
