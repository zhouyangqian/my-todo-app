package com.example.dict.api.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 字典类型DTO（服务间传输）
 */
@Data
public class DictTypeDTO {

    private Long id;

    private Long tenantId;

    private String code;

    private String name;

    private String description;

    private Integer status;

    private LocalDateTime createdAt;
}
