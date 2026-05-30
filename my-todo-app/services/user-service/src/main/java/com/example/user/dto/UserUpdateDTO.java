package com.example.user.dto;

import lombok.Data;

/**
 * 更新用户请求DTO
 */
@Data
public class UserUpdateDTO {

    private String email;

    private String phone;

    private String realName;

    private String avatar;

    private Long departmentId;

    /** 用户状态: 0-禁用, 1-正常 */
    private Integer status;
}
