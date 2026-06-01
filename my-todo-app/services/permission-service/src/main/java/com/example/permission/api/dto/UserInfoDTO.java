package com.example.permission.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserInfoDTO {

    private Long userId;
    private String username;
    private String email;
    private String realName;
    private String phone;
    private String avatar;
    private Long tenantId;
}
