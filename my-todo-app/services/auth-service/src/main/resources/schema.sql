-- 租户表
CREATE TABLE IF NOT EXISTS sys_tenant (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_name VARCHAR(128) NOT NULL COMMENT '租户名称',
    tenant_code VARCHAR(64) NOT NULL UNIQUE COMMENT '租户编码',
    status INT NOT NULL DEFAULT 1 COMMENT '状态(0禁用 1正常 2过期)',
    user_limit INT NOT NULL DEFAULT 5 COMMENT '用户数量限制',
    contact_name VARCHAR(64) DEFAULT NULL COMMENT '联系人',
    contact_email VARCHAR(128) DEFAULT NULL COMMENT '联系邮箱',
    contact_phone VARCHAR(32) DEFAULT NULL COMMENT '联系电话',
    expire_time DATETIME DEFAULT NULL COMMENT '过期时间',
    deleted INT NOT NULL DEFAULT 0 COMMENT '软删除',
    created_by BIGINT DEFAULT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT DEFAULT NULL,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Token黑名单表
CREATE TABLE IF NOT EXISTS sys_token_blacklist (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    token_prefix VARCHAR(64) NOT NULL COMMENT 'Token前缀(前32字符)',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    tenant_id BIGINT NOT NULL DEFAULT 0,
    expiry_time DATETIME NOT NULL COMMENT '过期时间',
    reason VARCHAR(32) NOT NULL DEFAULT 'LOGOUT' COMMENT '原因(LOGOUT/REFRESH/FORCED)',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    KEY idx_token_prefix (token_prefix),
    KEY idx_user_id (user_id),
    KEY idx_expiry_time (expiry_time)
);

-- 验证码表
CREATE TABLE IF NOT EXISTS sys_captcha (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    captcha_key VARCHAR(128) NOT NULL UNIQUE COMMENT '验证码Key',
    code_hash VARCHAR(128) NOT NULL COMMENT 'SHA-256哈希',
    user_id BIGINT DEFAULT NULL COMMENT '用户ID(可选)',
    expiry_time DATETIME NOT NULL COMMENT '过期时间',
    used INT NOT NULL DEFAULT 0 COMMENT '是否已使用',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);
