package com.example.permission.builder;

import com.example.permission.entity.DataPermissionRule;
import com.example.permission.service.DataPermissionRuleService;
import com.example.permission.service.DepartmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 数据权限 SQL 构建器
 * <p>
 * 根据 DataPermissionRule 实体构建 SQL WHERE 条件片段。
 * 支持的数据范围：
 * <ul>
 *   <li>ALL - 查看所有数据，不追加条件</li>
 *   <li>DEPT - 本部门数据，dept_id = 当前用户部门ID</li>
 *   <li>DEPT_AND_SUB - 本部门及子部门数据，dept_id IN (部门ID列表)</li>
 *   <li>CUSTOM - 自定义条件（预留扩展）</li>
 * </ul>
 * </p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataScopeSqlBuilder {

    private final DataPermissionRuleService dataPermissionRuleService;

    private final DepartmentService departmentService;

    /**
     * 根据用户角色构建数据范围 SQL 条件
     * <p>
     * 查询用户所有角色的数据权限规则，合并计算最终的 SQL 条件。
     * 多条规则之间使用 OR 连接（取并集，宽松策略）。
     * </p>
     *
     * @param userId     用户ID（用于 SELF 范围）
     * @param roleIds    用户拥有的角色ID列表
     * @param tableAlias 表别名（如 "t"），可为空
     * @param deptId     当前用户的部门ID（用于 DEPT/DEPT_AND_SUB 范围）
     * @return SQL WHERE 条件片段（不含 WHERE 关键字），返回 null 表示不过滤
     */
    public String buildScopeSql(Long userId, List<Long> roleIds, String tableAlias, Long deptId) {
        if (roleIds == null || roleIds.isEmpty()) {
            return null;
        }

        List<DataPermissionRule> rules = dataPermissionRuleService.getRulesByRoleIds(roleIds);
        if (rules.isEmpty()) {
            return null;
        }

        String prefix = (tableAlias != null && !tableAlias.isEmpty()) ? tableAlias + "." : "";
        List<String> conditions = new ArrayList<>();

        for (DataPermissionRule rule : rules) {
            String condition = buildRuleCondition(rule, prefix, userId, deptId);
            if (condition != null && !condition.isEmpty()) {
                conditions.add(condition);
            }
        }

        if (conditions.isEmpty()) {
            return null;
        }

        // 如果只有一条条件，直接返回
        if (conditions.size() == 1) {
            return conditions.get(0);
        }

        // 多条规则用 OR 连接
        StringBuilder sb = new StringBuilder();
        sb.append("(");
        for (int i = 0; i < conditions.size(); i++) {
            if (i > 0) {
                sb.append(" OR ");
            }
            sb.append(conditions.get(i));
        }
        sb.append(")");
        return sb.toString();
    }

    /**
     * 根据单个角色构建数据范围 SQL 条件
     *
     * @param userId     用户ID
     * @param roleId     角色ID
     * @param tableAlias 表别名
     * @return SQL WHERE 条件片段，返回 null 表示不过滤
     */
    public String buildScopeSql(Long userId, Long roleId, String tableAlias) {
        if (roleId == null) {
            return null;
        }
        return buildScopeSql(userId, List.of(roleId), tableAlias, null);
    }

    /**
     * 根据单个角色构建数据范围 SQL 条件（指定部门ID）
     *
     * @param userId     用户ID
     * @param roleId     角色ID
     * @param tableAlias 表别名
     * @param deptId     部门ID
     * @return SQL WHERE 条件片段，返回 null 表示不过滤
     */
    public String buildScopeSql(Long userId, Long roleId, String tableAlias, Long deptId) {
        if (roleId == null) {
            return null;
        }
        return buildScopeSql(userId, List.of(roleId), tableAlias, deptId);
    }

    /**
     * 根据单条规则构建 SQL 条件
     */
    private String buildRuleCondition(DataPermissionRule rule, String prefix, Long userId, Long deptId) {
        String scopeType = rule.getScopeType();
        if (scopeType == null) {
            return null;
        }

        String deptColumn = prefix + (rule.getDeptColumn() != null ? rule.getDeptColumn() : "dept_id");
        String userColumn = prefix + (rule.getUserColumn() != null ? rule.getUserColumn() : "created_by");

        switch (scopeType.toUpperCase()) {
            case "ALL":
                // 全部数据，不追加条件
                return null;

            case "DEPT":
                if (deptId == null) {
                    return deptColumn + " = -1";
                }
                return deptColumn + " = " + deptId;

            case "DEPT_AND_SUB":
                return buildDeptAndSubCondition(deptId, deptColumn);

            case "SELF":
                if (userId == null) {
                    return userColumn + " = -1";
                }
                return userColumn + " = " + userId;

            case "CUSTOM":
                // 自定义条件，预留扩展
                log.debug("自定义数据权限范围暂未实现: ruleId={}", rule.getId());
                return null;

            default:
                log.warn("未知的数据权限范围类型: {}", scopeType);
                return null;
        }
    }

    /**
     * 构建 DEPT_AND_SUB 条件：本部门及子部门
     * <p>
     * 查询部门树获取所有子部门ID，生成 IN 条件
     * </p>
     */
    private String buildDeptAndSubCondition(Long deptId, String deptColumn) {
        if (deptId == null) {
            return deptColumn + " = -1";
        }

        List<Long> deptIds = new ArrayList<>();
        deptIds.add(deptId);

        // 递归收集子部门ID
        collectSubDeptIds(deptId, deptIds);

        StringBuilder sb = new StringBuilder();
        sb.append(deptColumn).append(" IN (");
        for (int i = 0; i < deptIds.size(); i++) {
            if (i > 0) {
                sb.append(",");
            }
            sb.append(deptIds.get(i));
        }
        sb.append(")");
        return sb.toString();
    }

    /**
     * 递归收集子部门ID
     */
    private void collectSubDeptIds(Long parentId, List<Long> deptIds) {
        List<com.example.permission.entity.Department> children =
                departmentService.list(
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<com.example.permission.entity.Department>()
                        .eq(com.example.permission.entity.Department::getParentId, parentId)
                        .eq(com.example.permission.entity.Department::getDeleted, 0)
                );
        for (com.example.permission.entity.Department child : children) {
            deptIds.add(child.getId());
            collectSubDeptIds(child.getId(), deptIds);
        }
    }
}
