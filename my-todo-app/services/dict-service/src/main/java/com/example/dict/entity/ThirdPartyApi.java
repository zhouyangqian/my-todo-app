package com.example.dict.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 第三方API实体
 */
@Data
@TableName("third_party_api")
public class ThirdPartyApi implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 租户ID */
    @TableField(value = "tenant_id", fill = FieldFill.INSERT)
    private Long tenantId;

    /** API名称 */
    private String apiName;

    /** API地址 */
    private String apiUrl;

    /** 请求方法 */
    private String apiMethod;

    /** 认证类型: NONE/API_KEY/OAUTH2/BASIC */
    private String authType;

    /** API Key Header */
    private String apiKeyHeader;

    /** API Key Value */
    private String apiKeyValue;

    /** 描述 */
    private String description;

    /** 超时时间(ms) */
    private Integer timeoutMs;

    /** 重试次数 */
    private Integer retryCount;

    /** 状态 */
    private Integer status;

    @TableLogic
    @TableField(fill = FieldFill.INSERT)
    private Integer deleted;

    /** 创建人 */
    @TableField(fill = FieldFill.INSERT)
    private Long createdBy;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createdAt;

    /** 更新人 */
    @TableField(fill = FieldFill.UPDATE)
    private Long updatedBy;

    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime updatedAt;
}
