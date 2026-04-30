package com.example.common.mybatis.config;

import com.baomidou.mybatisplus.extension.plugins.handler.TenantLineHandler;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.NullValue;

/**
 * 多租户处理器
 * <p>
 * 实现 MyBatis-Plus 的 TenantLineHandler 接口，在 SQL 执行时自动注入租户ID条件，
 * 实现多租户数据隔离。
 * </p>
 * <p>
 * 工作原理：
 * <ul>
 *   <li>在执行 SQL 查询、更新、删除时，自动在 WHERE 子句中添加 tenant_id = ?</li>
 *   <li>支持从 ThreadLocal、JWT Token、请求头等上下文中获取当前租户ID</li>
 *   <li>可配置租户字段名和忽略某些表（如系统表）</li>
 * </ul>
 * </p>
 */
public class MultiTenantHandler implements TenantLineHandler {

    /**
     * 获取当前租户ID
     * <p>
     * 优先级顺序：
     * <ol>
     *   <li>从 TenantContext 获取（推荐，用于服务间调用）</li>
     *   <li>从 SecurityContext 获取（用于 Web 请求）</li>
     *   <li>从请求头获取（用于服务间调用）</li>
     * </ol>
     * </p>
     *
     * @return 当前租户ID，如果无法获取则返回 null（不注入租户条件）
     */
    @Override
    public Expression getTenantId() {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId != null) {
            return new LongValue(tenantId);
        }
        // 返回 null 表示不注入租户条件（适用于未登录的场景）
        return null;
    }

    /**
     * 获取租户字段名
     * <p>
     * 默认使用 "tenant_id" 作为租户字段名。
     * 如果某些表使用不同的字段名（如 company_id），可以通过此方法定制。
     * </p>
     *
     * @return 租户字段名
     */
    @Override
    public String getTenantIdColumn() {
        return "tenant_id";
    }

    /**
     * 判断是否忽略该表
     * <p>
     * 某些系统表不需要租户隔离（如字典表、配置表、菜单表等），
     * 可以在这里配置忽略。
     * </p>
     * <p>
     * 常见需要忽略的表：
     * <ul>
     *   <li>sys_dict - 字典表（所有租户共享）</li>
     *   <li>sys_config - 系统配置表</li>
     *   <li>sys_menu - 菜单表（根据租户动态过滤，不在 SQL 层隔离）</li>
     *   <li>gateway_route - 网关路由配置（系统级）</li>
     * </ul>
     * </p>
     *
     * @param tableName 表名
     * @return true 表示忽略该表（不注入租户条件），false 表示不忽略
     */
    @Override
    public boolean ignoreTable(String tableName) {
        // 系统表和认证相关表不需要租户隔离
        return tableName.startsWith("sys_")
            || tableName.startsWith("gateway_")
            || tableName.equals("user")              // 用户表（认证服务）
            || tableName.equals("login_session")      // 登录会话表
            || tableName.equals("refresh_token")      // 刷新令牌表
            || tableName.equals("login_log")          // 登录日志表
            || tableName.equals("captcha")            // 验证码表
            || tableName.equals("password_history")   // 密码历史表
            || tableName.equals("social_account")     // 社交账号表
            || tableName.equals("databasechangelog")
            || tableName.equals("databasechangeloglock");
    }
}

