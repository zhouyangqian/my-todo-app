package com.example.permission.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserPermissionDTO {

    private UserInfoDTO userInfo;
    private Long userId;
    private Set<String> permissions;
    private Set<String> roles;
}
