package com.example.user.controller;

import com.example.common.core.annotation.RequiresPermission;
import com.example.common.core.result.ApiResponse;
import com.example.user.api.dto.ImportResultDTO;
import com.example.user.service.UserImportExportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 用户导入导出控制器
 * <p>
 * 提供用户Excel导入模板下载、用户数据导入、用户数据导出等接口。
 * </p>
 */
@Tag(name = "用户导入导出", description = "用户Excel导入导出API")
@RestController
@RequestMapping("/api/users/import-export")
@RequiredArgsConstructor
public class UserImportExportController {

    private final UserImportExportService userImportExportService;

    /**
     * 下载导入模板
     */
    @RequiresPermission(code = "system:user:import", name = "导入用户")
    @Operation(summary = "下载导入模板")
    @GetMapping("/template")
    public ResponseEntity<byte[]> downloadTemplate() {
        byte[] data = userImportExportService.downloadTemplate();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=user_import_template.xlsx")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(data);
    }

    /**
     * 导入用户
     */
    @RequiresPermission(code = "system:user:import", name = "导入用户")
    @Operation(summary = "导入用户")
    @PostMapping("/import")
    public ApiResponse<ImportResultDTO> importUsers(
            @RequestParam("file") MultipartFile file,
            @RequestHeader("X-Tenant-Id") Long tenantId,
            @RequestHeader("X-User-Id") Long operatorId,
            @RequestHeader(value = "X-Forwarded-For", required = false) String ipAddress) {
        ImportResultDTO result = userImportExportService.importUsers(file, tenantId, operatorId, ipAddress);
        return ApiResponse.success(result);
    }

    /**
     * 导出用户列表
     */
    @RequiresPermission(code = "system:user:export", name = "导出用户")
    @Operation(summary = "导出用户列表")
    @GetMapping("/export")
    public ResponseEntity<byte[]> exportUsers(
            @RequestHeader("X-Tenant-Id") Long tenantId,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String realName,
            @RequestParam(required = false) Integer status) {
        byte[] data = userImportExportService.exportUsers(tenantId, username, realName, status);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=users_export.xlsx")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(data);
    }
}
