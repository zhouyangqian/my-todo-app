package com.example.user.api.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * 批量操作结果DTO
 */
@Data
@Builder
public class BatchResultDTO {

    private int successCount;
    private int failCount;
    private List<BatchFailureItem> failures;

    @Data
    @Builder
    public static class BatchFailureItem {
        private Long userId;
        private String reason;
    }
}
