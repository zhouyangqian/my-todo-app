package com.example.dict.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 参数字典实体
 */
@Data
@TableName("sys_parameter_dictionary")
public class ParameterDictionary implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 参数ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 租户ID
     */
    @TableField(fill = FieldFill.INSERT)
    private Long tenantId;

    /**
     * 分类ID
     */
    private Long categoryId;

    /**
     * 参数名称
     */
    private String paramName;

    /**
     * 参数编码
     */
    private String paramCode;

    /**
     * 参数值(JSON支持)
     */
    private String paramValue;

    /**
     * 值类型(STRING/NUMBER/BOOLEAN/JSON)
     */
    private String valueType;

    /**
     * 描述
     */
    private String description;

    /**
     * 验证规则(正则或范围)
     */
    private String validationRule;

    /**
     * 排序
     */
    private Integer sortOrder;

    /**
     * 状态: 0-禁用, 1-启用
     */
    private Integer status;

    /**
     * 版本号(乐观锁)
     */
    @Version
    private Integer version;

    /**
     * 软删除
     */
    @TableLogic
    @TableField(fill = FieldFill.INSERT)
    private Integer deleted;

    /**
     * 创建人
     */
    @TableField(fill = FieldFill.INSERT)
    private Long createdBy;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private LocalDateTime createdAt;

    /**
     * 更新人
     */
    @TableField(fill = FieldFill.UPDATE)
    private Long updatedBy;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private LocalDateTime updatedAt;
}
