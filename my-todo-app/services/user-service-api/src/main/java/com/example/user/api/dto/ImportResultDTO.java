package com.example.user.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 导入结果DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImportResultDTO {

    /** 成功数量 */
    private int successCount;

    /** 失败数量 */
    private int failCount;

    /** 总行数 */
    private int totalCount;

    /** 失败明细列表 */
    private List<ImportFailureItem> failures;

    /**
     * 导入失败项
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ImportFailureItem {

        /** 行号 */
        private int rowNum;

        /** 用户名 */
        private String username;

        /** 失败原因 */
        private String reason;
    }
}
