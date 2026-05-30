-- 权限审计日志表
CREATE TABLE IF NOT EXISTS sys_permission_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id BIGINT NOT NULL DEFAULT 0 COMMENT '租户ID',
    user_id BIGINT DEFAULT NULL COMMENT '被操作用户ID',
    resource VARCHAR(128) DEFAULT NULL COMMENT '资源标识',
    action VARCHAR(64) DEFAULT NULL COMMENT '操作类型',
    permission VARCHAR(128) DEFAULT NULL COMMENT '权限编码',
    result INT NOT NULL DEFAULT 0 COMMENT '结果(0拒绝 1通过)',
    reason VARCHAR(500) DEFAULT NULL COMMENT '原因',
    ip_address VARCHAR(64) DEFAULT NULL COMMENT 'IP地址',
    deleted INT NOT NULL DEFAULT 0 COMMENT '软删除',
    created_by BIGINT DEFAULT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT DEFAULT NULL,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY idx_tenant_id (tenant_id),
    KEY idx_user_id (user_id),
    KEY idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='权限审计日志表';
