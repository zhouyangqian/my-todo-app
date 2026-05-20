package com.example.user.api.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户信息DTO（服务间传输）
 */
@Data
public class UserDTO {

    private Long id;

    private Long tenantId;

    private String username;

    private String email;

    private String phone;

    private String realName;

    private String avatar;

    /** 用户状态: 0-禁用, 1-正常 */
    private Integer status;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime lastLoginTime;

    private String lastLoginIp;
}
