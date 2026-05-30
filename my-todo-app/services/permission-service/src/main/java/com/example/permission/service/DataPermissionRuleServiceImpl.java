package com.example.permission.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.permission.entity.DataPermissionRule;
import com.example.permission.mapper.DataPermissionRuleMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 数据权限规则服务实现类
 * <p>
 * 继承 MyBatis-Plus 的 ServiceImpl，提供数据权限规则的增删改查业务逻辑：
 * - 创建、更新、删除数据权限规则
 * - 按角色ID查询单个或多个角色的数据权限规则（支持OR逻辑）
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DataPermissionRuleServiceImpl extends ServiceImpl<DataPermissionRuleMapper, DataPermissionRule> implements DataPermissionRuleService {

    /**
     * 创建数据权限规则
     *
     * @param rule 规则实体对象
     * @return 创建成功的规则对象
     */
    @Override
    @Transactional
    public DataPermissionRule createRule(DataPermissionRule rule) {
        save(rule);
        log.info("已创建数据权限规则: roleId={}, ruleName={}", rule.getRoleId(), rule.getRuleName());
        return rule;
    }

    /**
     * 更新数据权限规则
     *
     * @param rule 规则实体对象（包含待更新的字段和规则ID）
     * @return 更新后的规则对象
     */
    @Override
    @Transactional
    public DataPermissionRule updateRule(DataPermissionRule rule) {
        updateById(rule);
        log.info("已更新数据权限规则: id={}", rule.getId());
        return rule;
    }

    /**
     * 删除数据权限规则（逻辑删除）
     *
     * @param id 规则ID
     */
    @Override
    @Transactional
    public void deleteRule(Long id) {
        removeById(id);
        log.info("已删除数据权限规则: id={}", id);
    }

    /**
     * 获取指定角色的数据权限规则列表
     *
     * @param roleId 角色ID
     * @return 该角色的数据权限规则列表
     */
    @Override
    public List<DataPermissionRule> getRulesByRoleId(Long roleId) {
        return list(
            new LambdaQueryWrapper<DataPermissionRule>()
                .eq(DataPermissionRule::getRoleId, roleId)
                .orderByAsc(DataPermissionRule::getSortOrder)
        );
    }

    /**
     * 获取多个角色的数据权限规则（OR逻辑）
     * <p>
     * 查询多个角色关联的所有数据权限规则，用于合并计算用户的有效数据权限。
     * </p>
     *
     * @param roleIds 角色ID列表
     * @return 所有关联角色的数据权限规则列表
     */
    @Override
    public List<DataPermissionRule> getRulesByRoleIds(List<Long> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) {
            return List.of();
        }
        return list(
            new LambdaQueryWrapper<DataPermissionRule>()
                .in(DataPermissionRule::getRoleId, roleIds)
                .orderByAsc(DataPermissionRule::getSortOrder)
        );
    }
}
