package com.example.permission.api.vo;

import lombok.Data;

@Data
public class BlacklistCreateVO {

    private Long userId;

    private String reason;

    private String operatorType;
}
