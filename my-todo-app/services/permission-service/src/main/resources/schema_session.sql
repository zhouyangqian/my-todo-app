-- sys_session 会话管理表
-- 用于记录用户登录会话信息，支持在线用户查看、会话踢出等功能

CREATE TABLE IF NOT EXISTS sys_session (
    id VARCHAR(64) PRIMARY KEY COMMENT '会话ID（UUID）',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    tenant_id BIGINT NOT NULL DEFAULT 0 COMMENT '租户ID',
    device_info VARCHAR(256) COMMENT '设备信息',
    ip_address VARCHAR(64) COMMENT 'IP地址',
    user_agent VARCHAR(512) COMMENT '浏览器User-Agent',
    status INT NOT NULL DEFAULT 1 COMMENT '会话状态：1=活跃，0=已踢出',
    login_time DATETIME NOT NULL COMMENT '登录时间',
    last_active DATETIME COMMENT '最后活跃时间',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    KEY idx_user_id (user_id),
    KEY idx_tenant_id (tenant_id),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='会话管理表';
