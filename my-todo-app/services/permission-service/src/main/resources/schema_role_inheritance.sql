-- 角色继承关系表
-- 用于定义角色之间的继承关系，子角色自动获得父角色的所有权限
CREATE TABLE IF NOT EXISTS sys_role_inheritance (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id BIGINT NOT NULL DEFAULT 0 COMMENT '租户ID',
    child_role_id BIGINT NOT NULL COMMENT '子角色ID',
    parent_role_id BIGINT NOT NULL COMMENT '父角色ID',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE KEY uk_child_parent (child_role_id, parent_role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色继承关系表';
