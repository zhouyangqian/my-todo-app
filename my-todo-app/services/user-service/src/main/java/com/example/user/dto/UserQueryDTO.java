package com.example.user.dto;

import lombok.Data;

/**
 * 查询用户请求DTO（分页参数 + 筛选条件）
 */
@Data
public class UserQueryDTO {

    /** 当前页码，默认1 */
    private Integer page = 1;

    /** 每页记录数，默认10 */
    private Integer size = 10;

    /** 用户名（模糊查询） */
    private String username;

    /** 真实姓名（模糊查询） */
    private String realName;

    /** 部门ID（精确匹配） */
    private Long departmentId;

    /** 用户状态: 0-禁用, 1-正常 */
    private Integer status;
}
