package com.example.common.permission.handler;

import com.example.common.core.annotation.DataPermission;
import com.example.common.permission.aspect.DataPermissionAspect;

import java.util.ArrayList;
import java.util.List;

/**
 * 数据权限SQL构建器
 * <p>
 * 根据数据权限上下文构建SQL WHERE条件片段，
 * 供 MyBatis-Plus 拦截器在 SQL 执行时自动添加数据范围过滤。
 * </p>
 */
public class DataScopeSqlBuilder {

    /**
     * 构建数据范围SQL条件
     *
     * @param tableAlias 表别名（如 "t" 或 "u"），可为null
     * @param deptField  部门字段名（如 "dept_id"）
     * @param userField  用户字段名（如 "created_by"）
     * @param scope      数据范围类型
     * @param userId     当前用户ID
     * @param deptId     当前用户部门ID
     * @param subDeptIds 子部门ID列表（用于DEPT_AND_SUB）
     * @param projectIds 项目ID列表（用于PROJECT）
     * @return SQL WHERE条件片段（不含WHERE关键字），返回null表示不过滤
     */
    public static String buildScopeSql(String tableAlias, String deptField, String userField,
                                       DataPermission.DataScope scope, Long userId, Long deptId,
                                       List<Long> subDeptIds, List<Long> projectIds) {
        if (scope == null || scope == DataPermission.DataScope.ALL) {
            return null;
        }

        String prefix = (tableAlias != null && !tableAlias.isEmpty()) ? tableAlias + "." : "";
        String deptCol = prefix + deptField;
        String userCol = prefix + userField;
        String projectCol = prefix + "project_id";

        switch (scope) {
            case SELF:
                if (userId == null) {
                    return userCol + " = -1";
                }
                return userCol + " = " + userId;

            case DEPT:
                if (deptId == null) {
                    return deptCol + " = -1";
                }
                return deptCol + " = " + deptId;

            case DEPT_AND_SUB:
                if (deptId == null) {
                    return deptCol + " = -1";
                }
                List<Long> deptIds = new ArrayList<>();
                deptIds.add(deptId);
                if (subDeptIds != null && !subDeptIds.isEmpty()) {
                    deptIds.addAll(subDeptIds);
                }
                StringBuilder sb = new StringBuilder();
                sb.append(deptCol).append(" IN (");
                for (int i = 0; i < deptIds.size(); i++) {
                    if (i > 0) sb.append(",");
                    sb.append(deptIds.get(i));
                }
                sb.append(")");
                return sb.toString();

            case PROJECT:
                if (projectIds == null || projectIds.isEmpty()) {
                    return projectCol + " = -1";
                }
                StringBuilder projectSb = new StringBuilder();
                projectSb.append(projectCol).append(" IN (");
                for (int i = 0; i < projectIds.size(); i++) {
                    if (i > 0) projectSb.append(",");
                    projectSb.append(projectIds.get(i));
                }
                projectSb.append(")");
                return projectSb.toString();

            default:
                return null;
        }
    }

    /**
     * 从当前ThreadLocal上下文构建SQL条件
     *
     * @param tableAlias 表别名
     * @param subDeptIds 子部门ID列表
     * @return SQL WHERE条件片段，返回null表示不过滤
     */
    public static String buildScopeSqlFromContext(String tableAlias, List<Long> subDeptIds) {
        return buildScopeSqlFromContext(tableAlias, subDeptIds, null);
    }

    /**
     * 从当前ThreadLocal上下文构建SQL条件（支持项目ID列表）
     *
     * @param tableAlias 表别名
     * @param subDeptIds 子部门ID列表
     * @param projectIds 项目ID列表（用于PROJECT范围）
     * @return SQL WHERE条件片段，返回null表示不过滤
     */
    public static String buildScopeSqlFromContext(String tableAlias, List<Long> subDeptIds, List<Long> projectIds) {
        DataPermissionAspect.DataPermissionContext context = DataPermissionAspect.getContext();
        if (context == null) {
            return null;
        }

        return buildScopeSql(
                tableAlias,
                context.getDeptField() != null ? context.getDeptField() : "dept_id",
                context.getUserField() != null ? context.getUserField() : "created_by",
                context.getDataScope(),
                context.getUserId(),
                context.getDeptId(),
                subDeptIds,
                projectIds
        );
    }
}
