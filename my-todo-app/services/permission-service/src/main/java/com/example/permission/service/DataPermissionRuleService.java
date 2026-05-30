package com.example.permission.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.permission.entity.DataPermissionRule;

import java.util.List;

/**
 * 数据权限规则服务接口
 */
public interface DataPermissionRuleService extends IService<DataPermissionRule> {

    DataPermissionRule createRule(DataPermissionRule rule);

    DataPermissionRule updateRule(DataPermissionRule rule);

    void deleteRule(Long id);

    List<DataPermissionRule> getRulesByRoleId(Long roleId);

    List<DataPermissionRule> getRulesByRoleIds(List<Long> roleIds);
}
