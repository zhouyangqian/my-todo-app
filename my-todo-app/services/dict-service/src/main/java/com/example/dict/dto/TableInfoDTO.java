package com.example.dict.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 数据库表信息DTO（从INFORMATION_SCHEMA.TABLES查询）
 */
@Data
public class TableInfoDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 表名 */
    private String tableName;

    /** 表注释 */
    private String tableComment;

    /** 所属数据库 */
    private String tableSchema;
}
