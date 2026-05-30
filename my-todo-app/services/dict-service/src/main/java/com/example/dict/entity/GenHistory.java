package com.example.dict.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 代码生成历史实体
 */
@Data
@TableName("gen_history")
public class GenHistory implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 租户ID */
    private Long tenantId;

    /** 表名 */
    private String tableName;

    /** 模块名 */
    private String moduleName;

    /** 包名 */
    private String packageName;

    /** 生成类型 */
    private String genType;

    /** 生成内容 */
    private String genContent;

    /** 创建人 */
    private Long createdBy;

    @TableField(fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createdAt;
}
