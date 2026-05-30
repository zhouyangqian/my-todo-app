package com.example.dict.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.common.core.annotation.RequiresPermission;
import com.example.common.core.result.ApiResponse;
import com.example.common.core.result.PageResult;
import com.example.dict.dto.TableColumnDTO;
import com.example.dict.dto.TableInfoDTO;
import com.example.dict.entity.CodeTemplate;
import com.example.dict.entity.GenHistory;
import com.example.dict.service.CodeGenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 代码生成管理控制器
 */
@Tag(name = "代码生成", description = "代码模板CRUD、代码生成、生成历史API")
@RestController
@RequestMapping("/api/dict/codegen")
@RequiredArgsConstructor
public class CodeGenController {

    private final CodeGenService codeGenService;

    // ==================== 数据库元数据 ====================

    @RequiresPermission(code = "dict:codegen:list", name = "查询数据库表列表")
    @Operation(summary = "查询数据库表列表")
    @GetMapping("/tables")
    public ApiResponse<List<TableInfoDTO>> listTables() {
        return ApiResponse.success(codeGenService.listTables());
    }

    @RequiresPermission(code = "dict:codegen:list", name = "查询表字段信息")
    @Operation(summary = "查询表字段信息")
    @GetMapping("/columns")
    public ApiResponse<List<TableColumnDTO>> listColumns(
            @RequestParam String schema,
            @RequestParam String tableName) {
        return ApiResponse.success(codeGenService.listColumns(schema, tableName));
    }

    // ==================== 模板管理 ====================

    @RequiresPermission(code = "dict:codegen:list", name = "查询模板列表")
    @Operation(summary = "分页查询模板")
    @GetMapping("/template/page")
    public ApiResponse<PageResult<CodeTemplate>> getTemplatePage(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String templateName) {
        Page<CodeTemplate> result = codeGenService.getTemplatePage(page, size, templateName);
        PageResult<CodeTemplate> pageResult = PageResult.of(
                result.getRecords(), result.getTotal(), result.getCurrent(), result.getSize());
        return ApiResponse.success(pageResult);
    }

    @RequiresPermission(code = "dict:codegen:list", name = "按类型查询模板")
    @Operation(summary = "按类型查询模板列表")
    @GetMapping("/template/type/{templateType}")
    public ApiResponse<List<CodeTemplate>> listByType(@PathVariable String templateType) {
        return ApiResponse.success(codeGenService.listByType(templateType));
    }

    @RequiresPermission(code = "dict:codegen:detail", name = "查询模板详情")
    @Operation(summary = "获取模板详情")
    @GetMapping("/template/{id}")
    public ApiResponse<CodeTemplate> getTemplate(@PathVariable Long id) {
        return ApiResponse.success(codeGenService.getById(id));
    }

    @RequiresPermission(code = "dict:codegen:create", name = "创建模板")
    @Operation(summary = "创建模板")
    @PostMapping("/template")
    public ApiResponse<CodeTemplate> createTemplate(@RequestBody CodeTemplate template) {
        return ApiResponse.success(codeGenService.createTemplate(template));
    }

    @RequiresPermission(code = "dict:codegen:update", name = "更新模板")
    @Operation(summary = "更新模板")
    @PutMapping("/template/{id}")
    public ApiResponse<CodeTemplate> updateTemplate(
            @PathVariable Long id,
            @RequestBody CodeTemplate template) {
        template.setId(id);
        return ApiResponse.success(codeGenService.updateTemplate(template));
    }

    @RequiresPermission(code = "dict:codegen:delete", name = "删除模板")
    @Operation(summary = "删除模板")
    @DeleteMapping("/template/{id}")
    public ApiResponse<Void> deleteTemplate(@PathVariable Long id) {
        codeGenService.deleteTemplate(id);
        return ApiResponse.success();
    }

    // ==================== 代码生成 ====================

    @RequiresPermission(code = "dict:codegen:generate", name = "生成代码")
    @Operation(summary = "生成代码")
    @PostMapping("/generate")
    public ApiResponse<String> generateCode(
            @RequestHeader("X-Tenant-Id") Long tenantId,
            @RequestHeader("X-User-Id") Long userId,
            @RequestBody Map<String, Object> body) {
        Long templateId = Long.valueOf(body.get("templateId").toString());
        String schema = (String) body.get("schema");
        String tableName = (String) body.get("tableName");
        String moduleName = (String) body.get("moduleName");
        String packageName = (String) body.get("packageName");
        String businessName = (String) body.get("businessName");
        String genType = (String) body.getOrDefault("genType", "ALL");
        @SuppressWarnings("unchecked")
        Map<String, String> params = (Map<String, String>) body.get("params");

        String content = codeGenService.generateCode(
                templateId, tenantId, schema, tableName, moduleName,
                packageName, businessName, genType, userId, params);
        return ApiResponse.success(content);
    }

    @RequiresPermission(code = "dict:codegen:generate", name = "批量生成代码")
    @Operation(summary = "批量生成代码（多个模板类型）")
    @PostMapping("/generate/batch")
    public ApiResponse<Map<String, String>> generateCodeBatch(
            @RequestHeader("X-Tenant-Id") Long tenantId,
            @RequestHeader("X-User-Id") Long userId,
            @RequestBody Map<String, Object> body) {
        String schema = (String) body.get("schema");
        String tableName = (String) body.get("tableName");
        String moduleName = (String) body.get("moduleName");
        String packageName = (String) body.get("packageName");
        String businessName = (String) body.get("businessName");
        @SuppressWarnings("unchecked")
        List<String> templateTypes = (List<String>) body.get("templateTypes");
        @SuppressWarnings("unchecked")
        Map<String, String> params = (Map<String, String>) body.get("params");

        Map<String, String> results = codeGenService.generateCodeBatch(
                templateTypes, tenantId, schema, tableName,
                moduleName, packageName, businessName, userId, params);
        return ApiResponse.success(results);
    }

    // ==================== 生成历史 ====================

    @RequiresPermission(code = "dict:codegen:history", name = "查询生成历史")
    @Operation(summary = "分页查询生成历史")
    @GetMapping("/history/page")
    public ApiResponse<PageResult<GenHistory>> getHistoryPage(
            @RequestHeader("X-Tenant-Id") Long tenantId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<GenHistory> result = codeGenService.getHistoryPage(tenantId, page, size);
        PageResult<GenHistory> pageResult = PageResult.of(
                result.getRecords(), result.getTotal(), result.getCurrent(), result.getSize());
        return ApiResponse.success(pageResult);
    }

    @RequiresPermission(code = "dict:codegen:history", name = "查询生成历史详情")
    @Operation(summary = "获取生成历史详情")
    @GetMapping("/history/{id}")
    public ApiResponse<GenHistory> getHistory(@PathVariable Long id) {
        return ApiResponse.success(codeGenService.getHistory(id));
    }
}
