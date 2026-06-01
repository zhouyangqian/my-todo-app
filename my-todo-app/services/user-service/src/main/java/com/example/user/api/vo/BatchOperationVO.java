package com.example.user.api.vo;

import lombok.Data;

import java.util.List;

/**
 * 批量操作请求VO
 */
@Data
public class BatchOperationVO {

    private List<Long> userIds;
    private List<Long> roleIds;
}
