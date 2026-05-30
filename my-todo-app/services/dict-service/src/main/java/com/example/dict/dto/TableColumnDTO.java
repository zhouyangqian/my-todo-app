package com.example.dict.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 数据库字段信息DTO（从INFORMATION_SCHEMA.COLUMNS查询）
 */
@Data
public class TableColumnDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 列名 */
    private String columnName;

    /** 数据类型（MySQL简短类型：varchar, bigint等） */
    private String dataType;

    /** 列类型（完整类型：varchar(128), bigint(20)） */
    private String columnType;

    /** 列注释 */
    private String columnComment;

    /** 是否可空（YES/NO） */
    private String isNullable;

    /** 键类型（PRI/UNI/MUL/空） */
    private String columnKey;

    /** 默认值 */
    private String columnDefault;

    /** Java字段名（驼峰，服务端计算） */
    private String fieldName;

    /** Java类型（服务端计算） */
    private String javaType;
}
