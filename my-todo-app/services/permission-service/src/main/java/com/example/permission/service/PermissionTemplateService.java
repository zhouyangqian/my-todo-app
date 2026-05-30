package com.example.permission.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.permission.entity.PermissionTemplate;

/**
 * 权限模板管理服务接口
 */
public interface PermissionTemplateService extends IService<PermissionTemplate> {

    PermissionTemplate createTemplate(PermissionTemplate template);

    PermissionTemplate updateTemplate(PermissionTemplate template);

    void deleteTemplate(Long id);

    Page<PermissionTemplate> getTemplatePage(Long tenantId, int page, int size);

    PermissionTemplate getTemplateDetail(Long id);

    void applyTemplateToRole(Long templateId, Long roleId, Long tenantId, Long operatorId);

    void initSystemTemplates();
}
