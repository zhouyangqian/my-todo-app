package com.example.common.mybatis.interceptor;

import com.baomidou.mybatisplus.extension.plugins.handler.DataPermissionHandler;
import com.example.common.permission.aspect.DataPermissionAspect;
import com.example.common.permission.aspect.DataPermissionAspect.DataPermissionContext;
import com.example.common.permission.handler.DataScopeSqlBuilder;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.expression.operators.relational.InExpression;
import net.sf.jsqlparser.schema.Column;

/**
 * 数据权限处理器（MyBatis-Plus DataPermissionHandler 实现）
 * <p>
 * 配合 MyBatis-Plus 内置的 DataPermissionInterceptor（InnerInterceptor）使用。
 * 工作流程：
 * <ol>
 *   <li>Controller 方法标注 @DataPermission 注解</li>
 *   <li>DataPermissionAspect 在方法执行前将 DataPermissionContext 存入 ThreadLocal</li>
 *   <li>MyBatis 执行查询时，MP 内置 DataPermissionInterceptor 调用本处理器的 getSqlSegment 方法</li>
 *   <li>本处理器从 ThreadLocal 读取上下文，使用 DataScopeSqlBuilder 生成 WHERE 条件</li>
 *   <li>DataPermissionAspect 在方法返回后清除 ThreadLocal</li>
 * </ol>
 * </p>
 */
@Slf4j
public class DataPermissionInterceptor implements DataPermissionHandler {

    /**
     * 获取数据权限的 SQL WHERE 条件表达式
     * <p>
     * 从 ThreadLocal 中读取 DataPermissionAspect 设置的上下文，
     * 如果上下文不存在（方法未标注 @DataPermission），返回原始 where 不追加条件。
     * </p>
     *
     * @param where         原始 SQL 的 WHERE 条件表达式
     * @param whereSegment  MyBatis MappedStatement ID（用于定位 SQL）
     * @return 追加数据权限条件后的 WHERE 表达式
     */
    @Override
    public Expression getSqlSegment(Expression where, String whereSegment) {
        DataPermissionContext context = DataPermissionAspect.getContext();
        if (context == null) {
            return where;
        }

        // 使用 DataScopeSqlBuilder 构建条件 SQL（tableAlias 为 null，条件不带表别名前缀）
        String scopeSql = DataScopeSqlBuilder.buildScopeSqlFromContext(null, null);
        if (scopeSql == null || scopeSql.isEmpty()) {
            return where;
        }

        log.debug("数据权限拦截器追加条件: {}, MappedStatement: {}", scopeSql, whereSegment);

        // 将条件字符串解析为 JSqlParser Expression
        Expression scopeExpression = parseExpression(scopeSql);

        if (where == null) {
            return scopeExpression;
        }

        // 用 AND 连接原始条件和数据权限条件
        return new AndExpression(where, scopeExpression);
    }

    /**
     * 将 SQL 条件字符串解析为 JSqlParser Expression
     * <p>
     * 支持 "column = value" 和 "column IN (v1,v2,...)" 两种格式。
     * </p>
     */
    private Expression parseExpression(String sql) {
        if (sql.toUpperCase().contains(" IN ")) {
            return parseInExpression(sql);
        }
        return parseEqualsExpression(sql);
    }

    /**
     * 解析 IN 表达式
     * <p>
     * 格式: "column IN (v1,v2,...)" 或 "alias.column IN (v1,v2,...)"
     * </p>
     */
    private Expression parseInExpression(String sql) {
        int inIndex = sql.toUpperCase().indexOf(" IN ");
        String columnPart = sql.substring(0, inIndex).trim();
        String valuesPart = sql.substring(inIndex + 4).trim();

        // 去掉外层括号
        if (valuesPart.startsWith("(") && valuesPart.endsWith(")")) {
            valuesPart = valuesPart.substring(1, valuesPart.length() - 1);
        }

        // Column(String) 构造器会自动处理 "alias.column" 格式的拆分
        Column column = new Column(columnPart);

        String[] valueStrs = valuesPart.split(",");
        java.util.List<Expression> expressions = new java.util.ArrayList<>();
        for (String val : valueStrs) {
            expressions.add(new LongValue(Long.parseLong(val.trim())));
        }
        ExpressionList expressionList = new ExpressionList(expressions);

        return new InExpression(column, expressionList);
    }

    /**
     * 解析等值表达式
     * <p>
     * 格式: "column = value" 或 "alias.column = value"
     * </p>
     */
    private Expression parseEqualsExpression(String sql) {
        String[] parts = sql.split("=");
        if (parts.length != 2) {
            log.warn("无法解析数据权限条件: {}", sql);
            return new StringValue(sql);
        }

        String columnPart = parts[0].trim();
        String valuePart = parts[1].trim();

        // Column(String) 构造器会自动处理 "alias.column" 格式的拆分
        Column column = new Column(columnPart);
        LongValue value = new LongValue(Long.parseLong(valuePart));

        return new EqualsTo(column, value);
    }
}
