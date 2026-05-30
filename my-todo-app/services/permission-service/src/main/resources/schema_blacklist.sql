-- sys_blacklist 黑名单管理表
-- 用于记录被拉黑的用户信息，支持黑名单管理功能

CREATE TABLE IF NOT EXISTS sys_blacklist (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id BIGINT NOT NULL DEFAULT 0,
    user_id BIGINT NOT NULL COMMENT '被拉黑用户ID',
    reason VARCHAR(500) COMMENT '拉黑原因',
    operator_id BIGINT COMMENT '操作人ID',
    operator_type VARCHAR(32) DEFAULT 'TENANT_ADMIN' COMMENT '操作人类型(TENANT_ADMIN/SYSTEM_ADMIN)',
    status INT NOT NULL DEFAULT 1 COMMENT '1=生效, 0=已解除',
    added_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    removed_at DATETIME DEFAULT NULL,
    removed_by BIGINT DEFAULT NULL,
    deleted INT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY idx_tenant_user (tenant_id, user_id),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='黑名单管理表';
