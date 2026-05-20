package com.example.dict.api.dto;

import lombok.Data;

/**
 * 系统配置DTO（服务间传输）
 */
@Data
public class ConfigDTO {

    private Long id;

    private Long tenantId;

    private String configKey;

    private String configValue;

    private String configName;

    private String description;

    private Integer status;
}
