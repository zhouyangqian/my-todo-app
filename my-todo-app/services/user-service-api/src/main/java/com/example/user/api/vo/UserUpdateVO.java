package com.example.user.api.vo;

import lombok.Data;

/**
 * 更新用户请求VO
 */
@Data
public class UserUpdateVO {

    private String email;

    private String phone;

    private String realName;

    private String avatar;

    /** 用户状态: 0-禁用, 1-正常 */
    private Integer status;
}
