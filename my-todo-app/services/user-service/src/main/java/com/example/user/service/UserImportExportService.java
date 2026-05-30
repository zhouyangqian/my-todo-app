package com.example.user.service;

import com.example.user.api.dto.ImportResultDTO;
import org.springframework.web.multipart.MultipartFile;

/**
 * 用户导入导出业务逻辑服务接口
 */
public interface UserImportExportService {

    byte[] downloadTemplate();

    ImportResultDTO importUsers(MultipartFile file, Long tenantId, Long operatorId, String ipAddress);

    byte[] exportUsers(Long tenantId, String username, String realName, Integer status);
}
