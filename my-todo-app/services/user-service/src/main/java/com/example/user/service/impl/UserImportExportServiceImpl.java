package com.example.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.user.api.dto.ImportResultDTO;
import com.example.user.entity.User;
import com.example.user.service.AuditLogService;
import com.example.user.service.UserImportExportService;
import com.example.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * 用户导入导出业务逻辑服务实现类
 * <p>
 * 提供用户Excel导入模板下载、用户数据导入、用户数据导出等功能。
 * 使用 Apache POI 的 SXSSFWorkbook 处理大文件。
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserImportExportServiceImpl implements UserImportExportService {

    private final UserService userService;
    private final AuditLogService auditLogService;

    /** 导入最大行数 */
    private static final int MAX_IMPORT_ROWS = 1000;

    /** 导入默认密码 */
    private static final String DEFAULT_PASSWORD = "Abc@12345";

    /** 邮箱格式校验正则 */
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");

    /** 列头定义 */
    private static final String[] TEMPLATE_HEADERS = {"用户名", "邮箱", "电话", "真实姓名"};
    private static final String[] EXPORT_HEADERS = {"用户名", "邮箱", "电话", "真实姓名", "状态", "创建时间"};

    /**
     * 下载导入模板
     * <p>
     * 返回一个空Excel的字节数组，包含列头：用户名、邮箱、电话、真实姓名。
     * </p>
     *
     * @return Excel模板字节数组
     */
    @Override
    public byte[] downloadTemplate() {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("用户导入模板");

            // 创建标题行样式
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            // 写入列头
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < TEMPLATE_HEADERS.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(TEMPLATE_HEADERS[i]);
                cell.setCellStyle(headerStyle);
                // 设置列宽
                sheet.setColumnWidth(i, 20 * 256);
            }

            workbook.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            log.error("生成导入模板失败", e);
            throw new RuntimeException("生成导入模板失败", e);
        }
    }

    /**
     * 导入用户
     * <p>
     * 从Excel文件中解析用户数据，逐行验证后创建用户。
     * 验证规则：必填字段检查、邮箱格式校验、用户名重复检查、租户内用户名唯一性。
     * 导入用户使用默认密码，状态设为启用。
     * </p>
     *
     * @param file       上传的Excel文件
     * @param tenantId   租户ID
     * @param operatorId 操作人ID
     * @param ipAddress  操作人IP地址
     * @return 导入结果
     */
    @Transactional
    @Override
    public ImportResultDTO importUsers(MultipartFile file, Long tenantId, Long operatorId, String ipAddress) {
        // 校验文件
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("导入文件不能为空");
        }
        String filename = file.getOriginalFilename();
        if (filename == null || (!filename.endsWith(".xlsx") && !filename.endsWith(".xls"))) {
            throw new IllegalArgumentException("仅支持Excel文件（.xlsx或.xls）");
        }

        List<ImportResultDTO.ImportFailureItem> failures = new ArrayList<>();
        int successCount = 0;
        Set<String> importedUsernames = new HashSet<>();

        try (InputStream is = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(is)) {

            Sheet sheet = workbook.getSheetAt(0);
            int lastRowNum = sheet.getLastRowNum();

            // 校验行数限制（减去标题行）
            if (lastRowNum > MAX_IMPORT_ROWS) {
                throw new IllegalArgumentException("导入数据不能超过" + MAX_IMPORT_ROWS + "行");
            }

            for (int i = 1; i <= lastRowNum; i++) {
                Row row = sheet.getRow(i);
                if (row == null) {
                    continue;
                }

                String username = getCellStringValue(row, 0);
                String email = getCellStringValue(row, 1);
                String phone = getCellStringValue(row, 2);
                String realName = getCellStringValue(row, 3);

                // 逐行验证
                String validationError = validateImportRow(username, email, phone, realName, importedUsernames, tenantId, i + 1);
                if (validationError != null) {
                    failures.add(ImportResultDTO.ImportFailureItem.builder()
                            .rowNum(i + 1)
                            .username(username)
                            .reason(validationError)
                            .build());
                    continue;
                }

                // 创建用户
                try {
                    User user = new User();
                    user.setUserName(username.trim());
                    user.setEmail(email != null ? email.trim() : null);
                    user.setPhone(phone != null ? phone.trim() : null);
                    user.setRealName(realName != null ? realName.trim() : null);
                    user.setTenantId(tenantId);
                    user.setStatus(1);
                    user.setCreatedBy(operatorId);
                    user.setCreatedAt(LocalDateTime.now());
                    user.setUpdatedAt(LocalDateTime.now());
                    user.setDeleted(0);

                    userService.save(user);
                    importedUsernames.add(username.trim().toLowerCase());
                    successCount++;
                } catch (Exception e) {
                    log.error("导入用户失败: 行号={}, username={}", i + 1, username, e);
                    failures.add(ImportResultDTO.ImportFailureItem.builder()
                            .rowNum(i + 1)
                            .username(username)
                            .reason("创建用户失败: " + e.getMessage())
                            .build());
                }
            }

        } catch (IOException e) {
            log.error("读取导入文件失败", e);
            throw new RuntimeException("读取导入文件失败", e);
        }

        // 记录审计日志
        auditLogService.log(tenantId, operatorId, "IMPORT", "USER", null,
                "导入用户: 成功" + successCount + "个, 失败" + failures.size() + "个", ipAddress);

        return ImportResultDTO.builder()
                .successCount(successCount)
                .failCount(failures.size())
                .totalCount(successCount + failures.size())
                .failures(failures)
                .build();
    }

    /**
     * 导出用户列表
     * <p>
     * 根据筛选条件查询用户列表，导出为Excel文件。
     * 列包括：用户名、邮箱、电话、真实姓名、状态、创建时间。
     * </p>
     *
     * @param tenantId 租户ID
     * @param username 用户名（模糊查询，可选）
     * @param realName 真实姓名（模糊查询，可选）
     * @param status   用户状态（可选）
     * @return Excel文件字节数组
     */
    @Override
    public byte[] exportUsers(Long tenantId, String username, String realName, Integer status) {
        // 查询用户列表（不分页，导出全部匹配数据）
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getTenantId, tenantId)
               .eq(User::getDeleted, 0);
        if (username != null && !username.isEmpty()) {
            wrapper.like(User::getUserName, username);
        }
        if (realName != null && !realName.isEmpty()) {
            wrapper.like(User::getRealName, realName);
        }
        if (status != null) {
            wrapper.eq(User::getStatus, status);
        }
        wrapper.orderByDesc(User::getCreatedAt);
        List<User> users = userService.list(wrapper);

        // 使用 SXSSFWorkbook 处理大文件
        try (SXSSFWorkbook workbook = new SXSSFWorkbook(100);
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("用户列表");

            // 创建标题行样式
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            // 写入列头
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < EXPORT_HEADERS.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(EXPORT_HEADERS[i]);
                cell.setCellStyle(headerStyle);
            }

            // 写入数据行
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            for (int i = 0; i < users.size(); i++) {
                User user = users.get(i);
                Row row = sheet.createRow(i + 1);
                row.createCell(0).setCellValue(user.getUserName() != null ? user.getUserName() : "");
                row.createCell(1).setCellValue(user.getEmail() != null ? user.getEmail() : "");
                row.createCell(2).setCellValue(user.getPhone() != null ? user.getPhone() : "");
                row.createCell(3).setCellValue(user.getRealName() != null ? user.getRealName() : "");
                row.createCell(4).setCellValue(user.getStatus() != null && user.getStatus() == 1 ? "启用" : "禁用");
                row.createCell(5).setCellValue(user.getCreatedAt() != null ? user.getCreatedAt().format(formatter) : "");
            }

            workbook.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            log.error("导出用户列表失败", e);
            throw new RuntimeException("导出用户列表失败", e);
        }
    }

    /**
     * 校验导入行数据
     *
     * @return 错误信息，如果校验通过则返回 null
     */
    private String validateImportRow(String username, String email, String phone,
                                     String realName, Set<String> importedUsernames,
                                     Long tenantId, int rowNum) {
        // 必填字段检查
        if (username == null || username.trim().isEmpty()) {
            return "用户名不能为空";
        }
        if (username.trim().length() < 3 || username.trim().length() > 50) {
            return "用户名长度3-50";
        }

        // 邮箱格式校验
        if (email != null && !email.trim().isEmpty() && !EMAIL_PATTERN.matcher(email.trim()).matches()) {
            return "邮箱格式不正确";
        }

        // 本批次内用户名重复检查
        if (importedUsernames.contains(username.trim().toLowerCase())) {
            return "用户名在本批次中重复";
        }

        // 租户内用户名唯一性检查
        User existing = userService.getOne(
                new LambdaQueryWrapper<User>()
                        .eq(User::getTenantId, tenantId)
                        .eq(User::getUserName, username.trim())
                        .eq(User::getDeleted, 0)
        );
        if (existing != null) {
            return "用户名已存在";
        }

        return null;
    }

    /**
     * 获取单元格字符串值
     */
    private String getCellStringValue(Row row, int colIndex) {
        Cell cell = row.getCell(colIndex);
        if (cell == null) {
            return null;
        }
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                return String.valueOf((long) cell.getNumericCellValue());
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            default:
                return null;
        }
    }
}
