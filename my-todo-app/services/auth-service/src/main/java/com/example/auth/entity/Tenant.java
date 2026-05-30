package com.example.auth.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 租户实体类
 * <p>
 * 对应 sys_tenant 表，记录租户基本信息、状态和配额等。
 * 使用独立字段，不继承 BaseEntity。
 * </p>
 */
@Data
@TableName("sys_tenant")
public class Tenant implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 租户ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 租户名称 */
    private String tenantName;

    /** 租户编码（唯一） */
    private String tenantCode;

    /** 状态：0-禁用，1-正常，2-过期 */
    private Integer status;

    /** 用户数量限制 */
    private Integer userLimit;

    /** 联系人 */
    private String contactName;

    /** 联系邮箱 */
    private String contactEmail;

    /** 联系电话 */
    private String contactPhone;

    /** 过期时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime expireTime;

    /** 软删除：0-未删除，1-已删除 */
    @TableLogic
    private Integer deleted;

    /** 创建人 */
    private Long createdBy;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createdAt;

    /** 更新人 */
    private Long updatedBy;

    /** 更新时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime updatedAt;
}
