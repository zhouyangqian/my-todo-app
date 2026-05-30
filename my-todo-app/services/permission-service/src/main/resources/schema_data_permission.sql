-- 数据权限规则表
-- 用于定义角色的数据权限过滤规则，支持按部门、个人、项目等维度过滤
CREATE TABLE IF NOT EXISTS sys_data_permission_rule (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id BIGINT NOT NULL DEFAULT 0,
    role_id BIGINT NOT NULL COMMENT '关联角色ID',
    rule_name VARCHAR(128) NOT NULL COMMENT '规则名称',
    scope_type VARCHAR(32) NOT NULL COMMENT 'ALL/DEPT/SELF/PROJECT/DEPT_AND_SUB',
    table_name VARCHAR(128) COMMENT '目标表名(空=所有表)',
    dept_column VARCHAR(64) DEFAULT 'dept_id' COMMENT '部门字段名',
    user_column VARCHAR(64) DEFAULT 'created_by' COMMENT '用户字段名',
    sort_order INT NOT NULL DEFAULT 0 COMMENT '排序',
    deleted INT NOT NULL DEFAULT 0 COMMENT '逻辑删除标志',
    created_by BIGINT DEFAULT NULL COMMENT '创建人ID',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_by BIGINT DEFAULT NULL COMMENT '更新人ID',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    KEY idx_role_id (role_id),
    KEY idx_tenant_id (tenant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数据权限规则表';
