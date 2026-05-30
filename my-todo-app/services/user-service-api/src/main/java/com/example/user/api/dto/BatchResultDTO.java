package com.example.user.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 批量操作结果DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatchResultDTO {

    /** 成功数量 */
    private int successCount;

    /** 失败数量 */
    private int failCount;

    /** 失败明细列表 */
    private List<BatchFailureItem> failures;

    /**
     * 批量操作失败项
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BatchFailureItem {

        /** 失败的用户ID */
        private Long userId;

        /** 失败原因 */
        private String reason;
    }
}
