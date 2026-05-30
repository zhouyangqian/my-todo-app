-- 角色表
CREATE TABLE IF NOT EXISTS sys_role (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id BIGINT NOT NULL DEFAULT 0,
    role_name VARCHAR(64) NOT NULL COMMENT '角色名称',
    role_code VARCHAR(64) NOT NULL COMMENT '角色编码',
    description VARCHAR(256) DEFAULT NULL,
    status INT NOT NULL DEFAULT 1 COMMENT '0禁用 1启用',
    is_system INT NOT NULL DEFAULT 0 COMMENT '是否系统角色',
    deleted INT NOT NULL DEFAULT 0,
    created_by BIGINT DEFAULT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT DEFAULT NULL,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_tenant_role_code (tenant_id, role_code)
);

-- 用户角色关联表
CREATE TABLE IF NOT EXISTS sys_user_role (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id BIGINT NOT NULL DEFAULT 0,
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_role (user_id, role_id, tenant_id)
);

-- 用户直接权限表
CREATE TABLE IF NOT EXISTS sys_user_permission (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id BIGINT NOT NULL DEFAULT 0,
    user_id BIGINT NOT NULL,
    permission_id BIGINT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_permission (user_id, permission_id, tenant_id)
);

-- 审计日志表
CREATE TABLE IF NOT EXISTS sys_audit_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id BIGINT NOT NULL DEFAULT 0,
    operator_id BIGINT DEFAULT NULL COMMENT '操作人ID',
    operation_type VARCHAR(64) NOT NULL COMMENT '操作类型',
    target_type VARCHAR(64) NOT NULL COMMENT '目标类型',
    target_id BIGINT DEFAULT NULL COMMENT '目标ID',
    result INT NOT NULL DEFAULT 1 COMMENT '0失败 1成功',
    ip_address VARCHAR(64) DEFAULT NULL,
    detail VARCHAR(1000) DEFAULT NULL COMMENT '操作详情',
    deleted INT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    KEY idx_tenant_id (tenant_id),
    KEY idx_operator_id (operator_id),
    KEY idx_created_at (created_at)
);
