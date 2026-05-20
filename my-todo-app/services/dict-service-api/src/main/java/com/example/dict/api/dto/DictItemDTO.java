package com.example.dict.api.dto;

import lombok.Data;

/**
 * 字典项DTO（服务间传输）
 */
@Data
public class DictItemDTO {

    private Long id;

    private Long tenantId;

    private Long typeId;

    private String label;

    private String value;

    private Integer sort;

    private Integer status;
}
