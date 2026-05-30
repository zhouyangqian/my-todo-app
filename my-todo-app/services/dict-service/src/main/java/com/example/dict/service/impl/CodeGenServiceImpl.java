package com.example.dict.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.dict.dto.TableColumnDTO;
import com.example.dict.dto.TableInfoDTO;
import com.example.dict.entity.CodeTemplate;
import com.example.dict.entity.GenHistory;
import com.example.dict.mapper.CodeTemplateMapper;
import com.example.dict.mapper.GenHistoryMapper;
import com.example.dict.mapper.TableMetadataMapper;
import com.example.dict.service.CodeGenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 代码生成服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CodeGenServiceImpl extends ServiceImpl<CodeTemplateMapper, CodeTemplate> implements CodeGenService {

    private final GenHistoryMapper genHistoryMapper;
    private final TableMetadataMapper tableMetadataMapper;

    /** 业务数据库列表 */
    private static final List<String> BUSINESS_SCHEMAS = List.of(
            "my_todo_auth", "my_todo_user", "my_todo_permission",
            "my_todo_dict", "my_todo_erp", "my_todo_finance", "my_todo_inventory"
    );

    // ==================== 数据库元数据 ====================

    @Override
    public List<TableInfoDTO> listTables() {
        return tableMetadataMapper.listTables(BUSINESS_SCHEMAS);
    }

    @Override
    public List<TableColumnDTO> listColumns(String schema, String tableName) {
        List<TableColumnDTO> columns = tableMetadataMapper.listColumns(schema, tableName);
        for (TableColumnDTO col : columns) {
            col.setFieldName(toCamelCase(col.getColumnName()));
            col.setJavaType(mapJavaType(col.getDataType()));
        }
        return columns;
    }

    // ==================== 模板管理 ====================

    @Override
    public Page<CodeTemplate> getTemplatePage(int page, int size, String templateName) {
        LambdaQueryWrapper<CodeTemplate> wrapper = new LambdaQueryWrapper<>();
        if (templateName != null && !templateName.isEmpty()) {
            wrapper.like(CodeTemplate::getTemplateName, templateName);
        }
        wrapper.orderByDesc(CodeTemplate::getCreatedAt);
        return page(new Page<>(page, size), wrapper);
    }

    @Override
    public List<CodeTemplate> listByType(String templateType) {
        return list(new LambdaQueryWrapper<CodeTemplate>()
                .eq(CodeTemplate::getTemplateType, templateType)
                .orderByAsc(CodeTemplate::getTemplateName));
    }

    @Override
    @Transactional
    public CodeTemplate createTemplate(CodeTemplate template) {
        save(template);
        return template;
    }

    @Override
    @Transactional
    public CodeTemplate updateTemplate(CodeTemplate template) {
        updateById(template);
        return template;
    }

    @Override
    @Transactional
    public void deleteTemplate(Long id) {
        removeById(id);
    }

    // ==================== 代码生成 ====================

    @Override
    @Transactional
    public String generateCode(Long templateId, Long tenantId, String schema,
                                String tableName, String moduleName, String packageName,
                                String businessName, String genType,
                                Long userId, Map<String, String> params) {
        CodeTemplate template = getById(templateId);
        if (template == null) {
            throw new IllegalArgumentException("模板不存在: " + templateId);
        }

        String content = generateCodeInternal(template, tenantId, schema, tableName,
                moduleName, packageName, businessName, userId, params);

        // 保存生成历史
        GenHistory history = new GenHistory();
        history.setTenantId(tenantId);
        history.setTableName(tableName);
        history.setModuleName(moduleName);
        history.setPackageName(packageName);
        history.setGenType(genType);
        history.setGenContent(content);
        history.setCreatedBy(userId);
        genHistoryMapper.insert(history);

        log.info("代码生成完成: templateId={}, tableName={}", templateId, tableName);
        return content;
    }

    @Override
    @Transactional
    public Map<String, String> generateCodeBatch(List<String> templateTypes, Long tenantId,
                                                  String schema, String tableName,
                                                  String moduleName, String packageName,
                                                  String businessName, Long userId,
                                                  Map<String, String> params) {
        Map<String, String> results = new LinkedHashMap<>();
        for (String type : templateTypes) {
            List<CodeTemplate> templates = listByType(type);
            if (!templates.isEmpty()) {
                CodeTemplate template = templates.get(0);
                String content = generateCodeInternal(template, tenantId, schema, tableName,
                        moduleName, packageName, businessName, userId, params);
                results.put(type, content);
            }
        }

        // 保存一条批量生成历史
        if (!results.isEmpty()) {
            GenHistory history = new GenHistory();
            history.setTenantId(tenantId);
            history.setTableName(tableName);
            history.setModuleName(moduleName);
            history.setPackageName(packageName);
            history.setGenType("BATCH");
            history.setGenContent(String.join("\n\n// ====================\n\n", results.values()));
            history.setCreatedBy(userId);
            genHistoryMapper.insert(history);
        }

        log.info("批量代码生成完成: types={}, tableName={}", templateTypes, tableName);
        return results;
    }

    /**
     * 内部代码生成逻辑
     */
    private String generateCodeInternal(CodeTemplate template, Long tenantId, String schema,
                                         String tableName, String moduleName, String packageName,
                                         String businessName, Long userId,
                                         Map<String, String> params) {
        // 查询字段元数据
        List<TableColumnDTO> columns = Collections.emptyList();
        if (schema != null && !schema.isEmpty() && tableName != null && !tableName.isEmpty()) {
            columns = listColumns(schema, tableName);
        }

        // 处理foreach块（字段循环）
        String content = template.getTemplateContent();
        content = expandForeachBlocks(content, columns);

        // 构建标量替换参数
        Map<String, String> allParams = new HashMap<>();
        allParams.put("${tableName}", tableName != null ? tableName : "");
        allParams.put("${moduleName}", moduleName != null ? moduleName : "");
        allParams.put("${packageName}", packageName != null ? packageName : "");
        allParams.put("${entityName}", toCamelCase(tableName));
        allParams.put("${EntityName}", toPascalCase(tableName));
        allParams.put("${businessName}", businessName != null ? businessName : "");
        if (params != null) {
            allParams.putAll(params);
        }

        // 执行标量替换
        for (Map.Entry<String, String> entry : allParams.entrySet()) {
            if (entry.getKey() != null && entry.getValue() != null) {
                content = content.replace(entry.getKey(), entry.getValue());
            }
        }

        return content;
    }

    /**
     * 展开 ${foreach column in columns}...${endfor} 块
     */
    private String expandForeachBlocks(String template, List<TableColumnDTO> columns) {
        if (columns == null || columns.isEmpty()) {
            // 没有字段数据时，移除foreach块
            return template.replaceAll("\\$\\{foreach\\s+column\\s+in\\s+columns}.*?\\$\\{endfor}", "");
        }

        Pattern pattern = Pattern.compile(
                "\\$\\{foreach\\s+column\\s+in\\s+columns}(.*?)\\$\\{endfor}",
                Pattern.DOTALL);
        Matcher matcher = pattern.matcher(template);
        StringBuilder result = new StringBuilder();
        while (matcher.find()) {
            String blockBody = matcher.group(1);
            StringBuilder expanded = new StringBuilder();
            for (TableColumnDTO col : columns) {
                String colContent = blockBody
                        .replace("${column.fieldName}", nvl(col.getFieldName()))
                        .replace("${column.javaType}", nvl(col.getJavaType()))
                        .replace("${column.columnName}", nvl(col.getColumnName()))
                        .replace("${column.columnComment}", nvl(col.getColumnComment()))
                        .replace("${column.columnKey}", nvl(col.getColumnKey()))
                        .replace("${column.isNullable}", nvl(col.getIsNullable()))
                        .replace("${column.dataType}", nvl(col.getDataType()))
                        .replace("${column.columnType}", nvl(col.getColumnType()));
                expanded.append(colContent);
            }
            matcher.appendReplacement(result, Matcher.quoteReplacement(expanded.toString()));
        }
        matcher.appendTail(result);
        return result.toString();
    }

    // ==================== 生成历史 ====================

    @Override
    public Page<GenHistory> getHistoryPage(Long tenantId, int page, int size) {
        LambdaQueryWrapper<GenHistory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(GenHistory::getTenantId, tenantId)
               .orderByDesc(GenHistory::getCreatedAt);
        return genHistoryMapper.selectPage(new Page<>(page, size), wrapper);
    }

    @Override
    public GenHistory getHistory(Long id) {
        return genHistoryMapper.selectById(id);
    }

    // ==================== 工具方法 ====================

    /** MySQL数据类型 -> Java类型映射 */
    private String mapJavaType(String dataType) {
        if (dataType == null) return "String";
        return switch (dataType.toLowerCase()) {
            case "bigint" -> "Long";
            case "int", "tinyint", "smallint", "mediumint" -> "Integer";
            case "decimal", "numeric" -> "BigDecimal";
            case "datetime", "timestamp" -> "LocalDateTime";
            case "date" -> "LocalDate";
            case "float" -> "Float";
            case "double" -> "Double";
            case "bit", "boolean" -> "Boolean";
            default -> "String"; // varchar, char, text, longtext, enum 等
        };
    }

    /** 表名转小驼峰: sys_user -> sysUser */
    private String toCamelCase(String name) {
        if (name == null || name.isEmpty()) return "";
        StringBuilder result = new StringBuilder();
        String[] parts = name.split("_");
        for (int i = 0; i < parts.length; i++) {
            if (i == 0) {
                result.append(parts[i].toLowerCase());
            } else {
                result.append(Character.toUpperCase(parts[i].charAt(0)));
                result.append(parts[i].substring(1).toLowerCase());
            }
        }
        return result.toString();
    }

    /** 表名转大驼峰: sys_user -> SysUser */
    private String toPascalCase(String name) {
        if (name == null || name.isEmpty()) return "";
        StringBuilder result = new StringBuilder();
        String[] parts = name.split("_");
        for (String part : parts) {
            result.append(Character.toUpperCase(part.charAt(0)));
            result.append(part.substring(1).toLowerCase());
        }
        return result.toString();
    }

    private String nvl(String value) {
        return value != null ? value : "";
    }
}
