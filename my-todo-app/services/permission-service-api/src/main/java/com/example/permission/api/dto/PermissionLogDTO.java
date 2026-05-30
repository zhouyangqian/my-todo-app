package com.example.permission.api.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 权限审计日志DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PermissionLogDTO {
    private Long id;
    private Long tenantId;
    private Long userId;
    private String resource;
    private String action;
    private String permission;
    private Integer result;
    private String reason;
    private String ipAddress;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createdAt;
}
