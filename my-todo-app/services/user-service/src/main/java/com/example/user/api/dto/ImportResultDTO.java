package com.example.user.api.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * 用户导入结果DTO
 */
@Data
@Builder
public class ImportResultDTO {

    private int totalCount;
    private int successCount;
    private int failCount;
    private List<ImportFailureItem> failures;

    @Data
    @Builder
    public static class ImportFailureItem {
        private int rowNum;
        private String username;
        private String reason;
    }
}
