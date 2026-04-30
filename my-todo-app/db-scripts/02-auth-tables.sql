-- 认证服务数据库表结构
-- Version: 1.0.0

USE `my-todo-app-dev`;

-- 用户表
CREATE TABLE IF NOT EXISTS `user` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    `tenant_id` BIGINT NOT NULL DEFAULT 0 COMMENT '租户ID',
    `username` VARCHAR(50) NOT NULL COMMENT '用户名',
    `password` VARCHAR(255) NOT NULL COMMENT '密码(BCrypt加密)',
    `email` VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
    `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号',
    `real_name` VARCHAR(50) DEFAULT NULL COMMENT '真实姓名',
    `avatar` VARCHAR(255) DEFAULT NULL COMMENT '头像URL',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 0-禁用, 1-启用',
    `locked` TINYINT NOT NULL DEFAULT 0 COMMENT '锁定状态: 0-未锁定, 1-已锁定',
    `locked_until` DATETIME DEFAULT NULL COMMENT '锁定到期时间',
    `login_fail_count` INT NOT NULL DEFAULT 0 COMMENT '登录失败次数',
    `last_login_time` DATETIME DEFAULT NULL COMMENT '最后登录时间',
    `last_login_ip` VARCHAR(50) DEFAULT NULL COMMENT '最后登录IP',
    `password_changed_at` DATETIME DEFAULT NULL COMMENT '密码修改时间',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '软删除: 0-未删除, 1-已删除',
    `created_by` BIGINT DEFAULT NULL COMMENT '创建人ID',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_by` BIGINT DEFAULT NULL COMMENT '更新人ID',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username_tenant` (`username`, `tenant_id`),
    UNIQUE KEY `uk_email_tenant` (`email`, `tenant_id`),
    KEY `idx_tenant_id` (`tenant_id`),
    KEY `idx_status` (`status`),
    KEY `idx_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- 登录会话表
CREATE TABLE IF NOT EXISTS `login_session` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '会话ID',
    `tenant_id` BIGINT NOT NULL DEFAULT 0 COMMENT '租户ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `token_id` VARCHAR(64) NOT NULL COMMENT 'JWT令牌ID',
    `device_type` VARCHAR(20) DEFAULT 'web' COMMENT '设备类型: web, mobile, desktop',
    `device_info` VARCHAR(255) DEFAULT NULL COMMENT '设备信息(User-Agent)',
    `ip_address` VARCHAR(50) DEFAULT NULL COMMENT 'IP地址',
    `login_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '登录时间',
    `expire_time` DATETIME NOT NULL COMMENT '令牌过期时间',
    `logout_time` DATETIME DEFAULT NULL COMMENT '登出时间',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 0-已登出, 1-活跃',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_token_id` (`token_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_tenant_id` (`tenant_id`),
    KEY `idx_expire_time` (`expire_time`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='登录会话表';

-- 刷新令牌表
CREATE TABLE IF NOT EXISTS `refresh_token` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '令牌ID',
    `tenant_id` BIGINT NOT NULL DEFAULT 0 COMMENT '租户ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `token_hash` VARCHAR(128) NOT NULL COMMENT '刷新令牌哈希值(SHA-256)',
    `session_id` BIGINT DEFAULT NULL COMMENT '关联会话ID',
    `expire_time` DATETIME NOT NULL COMMENT '令牌过期时间',
    `revoked` TINYINT NOT NULL DEFAULT 0 COMMENT '已吊销: 0-有效, 1-已吊销',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_token_hash` (`token_hash`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_tenant_id` (`tenant_id`),
    KEY `idx_expire_time` (`expire_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='刷新令牌表';

-- 登录日志表
CREATE TABLE IF NOT EXISTS `login_log` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '日志ID',
    `tenant_id` BIGINT NOT NULL DEFAULT 0 COMMENT '租户ID',
    `user_id` BIGINT DEFAULT NULL COMMENT '用户ID(登录失败时为null)',
    `username` VARCHAR(50) DEFAULT NULL COMMENT '尝试登录的用户名',
    `login_type` TINYINT NOT NULL DEFAULT 1 COMMENT '登录类型: 1-密码登录, 2-短信登录, 3-社交登录',
    `login_status` TINYINT NOT NULL COMMENT '状态: 0-失败, 1-成功',
    `fail_reason` VARCHAR(100) DEFAULT NULL COMMENT '失败原因',
    `ip_address` VARCHAR(50) DEFAULT NULL COMMENT 'IP地址',
    `device_info` VARCHAR(255) DEFAULT NULL COMMENT '设备信息',
    `login_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '登录时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_tenant_id` (`tenant_id`),
    KEY `idx_login_time` (`login_time`),
    KEY `idx_login_status` (`login_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='登录日志表';

-- 验证码表
CREATE TABLE IF NOT EXISTS `captcha` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '验证码ID',
    `captcha_key` VARCHAR(64) NOT NULL COMMENT '验证码键',
    `captcha_value` VARCHAR(10) NOT NULL COMMENT '验证码值',
    `captcha_type` TINYINT NOT NULL DEFAULT 1 COMMENT '类型: 1-图片验证码, 2-短信验证码, 3-邮件验证码',
    `expire_time` DATETIME NOT NULL COMMENT '过期时间',
    `used` TINYINT NOT NULL DEFAULT 0 COMMENT '已使用: 0-未使用, 1-已使用',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_captcha_key` (`captcha_key`),
    KEY `idx_expire_time` (`expire_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='验证码表';

-- 密码历史表
CREATE TABLE IF NOT EXISTS `password_history` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '历史ID',
    `tenant_id` BIGINT NOT NULL DEFAULT 0 COMMENT '租户ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `password_hash` VARCHAR(255) NOT NULL COMMENT '密码哈希值',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_tenant_id` (`tenant_id`),
    KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='密码历史表';

-- 社交账号绑定表
CREATE TABLE IF NOT EXISTS `social_account` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '社交账号ID',
    `tenant_id` BIGINT NOT NULL DEFAULT 0 COMMENT '租户ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `provider` VARCHAR(20) NOT NULL COMMENT '提供商: wechat, alipay, dingtalk',
    `provider_user_id` VARCHAR(100) NOT NULL COMMENT '提供商用户ID',
    `union_id` VARCHAR(100) DEFAULT NULL COMMENT '统一ID',
    `nickname` VARCHAR(100) DEFAULT NULL COMMENT '昵称',
    `avatar` VARCHAR(255) DEFAULT NULL COMMENT '头像',
    `extra_data` TEXT DEFAULT NULL COMMENT '扩展数据(JSON格式)',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_provider_user` (`provider`, `provider_user_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_tenant_id` (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='社交账号绑定表';

-- 插入默认管理员用户(密码: admin123)
-- BCrypt哈希值,强度10,已验证
INSERT INTO `user` (`tenant_id`, `username`, `password`, `email`, `real_name`, `status`, `created_at`)
VALUES (1, 'admin', '$2a$10$FaVniNYzRkROPg/PlCLKJOQKLE1jGcDJZJYE7ryXfOFOMdIrfbN7u', 'admin@example.com', '系统管理员', 1, NOW());

-- 创建性能索引
CREATE INDEX idx_user_deleted_status ON `user` (deleted, status);
CREATE INDEX idx_session_user_status ON `login_session` (user_id, status);
CREATE INDEX idx_token_user_revoked ON `refresh_token` (user_id, revoked);
