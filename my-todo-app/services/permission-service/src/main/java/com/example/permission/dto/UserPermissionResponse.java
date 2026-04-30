package com.example.permission.dto;

import lombok.Data;

import java.util.Set;

/**
 * 用户权限响应DTO
 * <p>
 * 用于返回指定用户的权限信息，包含：
 * - 用户基本信息
 * - 该用户拥有的所有权限编码集合
 * - 该用户拥有的所有角色编码集合
 * </p>
 */
@Data
public class UserPermissionResponse {

    /** 用户基本信息 */
    private UserInfo userInfo;

    /** 用户拥有的权限编码集合（如 "user:create", "user:delete"） */
    private Set<String> permissions;

    /** 用户拥有的角色编码集合（如 "ADMIN", "EDITOR"） */
    private Set<String> roles;

    /** 用户ID（保留用于兼容旧接口） */
    private Long userId;
}
