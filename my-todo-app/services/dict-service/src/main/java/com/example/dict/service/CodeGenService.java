package com.example.dict.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.dict.dto.TableColumnDTO;
import com.example.dict.dto.TableInfoDTO;
import com.example.dict.entity.CodeTemplate;
import com.example.dict.entity.GenHistory;

import java.util.List;
import java.util.Map;

/**
 * 代码生成服务接口
 */
public interface CodeGenService extends IService<CodeTemplate> {

    List<TableInfoDTO> listTables();

    List<TableColumnDTO> listColumns(String schema, String tableName);

    Page<CodeTemplate> getTemplatePage(int page, int size, String templateName);

    List<CodeTemplate> listByType(String templateType);

    CodeTemplate createTemplate(CodeTemplate template);

    CodeTemplate updateTemplate(CodeTemplate template);

    void deleteTemplate(Long id);

    String generateCode(Long templateId, Long tenantId, String schema,
                        String tableName, String moduleName, String packageName,
                        String businessName, String genType,
                        Long userId, Map<String, String> params);

    Map<String, String> generateCodeBatch(List<String> templateTypes, Long tenantId,
                                           String schema, String tableName,
                                           String moduleName, String packageName,
                                           String businessName, Long userId,
                                           Map<String, String> params);

    Page<GenHistory> getHistoryPage(Long tenantId, int page, int size);

    GenHistory getHistory(Long id);
}
