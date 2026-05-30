package com.example.user.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户响应DTO（不含password，含roleNames）
 */
@Data
public class UserVO {

    private Long id;

    private Long tenantId;

    private String username;

    private String email;

    private String phone;

    private String realName;

    private String avatar;

    private Long departmentId;

    /** 用户状态: 0-禁用, 1-正常 */
    private Integer status;

    /** 用户拥有的角色名称列表 */
    private List<String> roleNames;

    private Long createdBy;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createdAt;

    private Long updatedBy;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime updatedAt;
}
