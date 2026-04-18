-- Auth Service Database Schema
-- Version: 1.0.0
-- Date: 2026-04-07

CREATE DATABASE IF NOT EXISTS my_todo_auth DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE my_todo_auth;

-- User Table
CREATE TABLE IF NOT EXISTS `user` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'User ID',
    `tenant_id` BIGINT NOT NULL DEFAULT 0 COMMENT 'Tenant ID',
    `username` VARCHAR(50) NOT NULL COMMENT 'Username',
    `password` VARCHAR(255) NOT NULL COMMENT 'Password (BCrypt)',
    `email` VARCHAR(100) DEFAULT NULL COMMENT 'Email',
    `phone` VARCHAR(20) DEFAULT NULL COMMENT 'Phone number',
    `real_name` VARCHAR(50) DEFAULT NULL COMMENT 'Real name',
    `avatar` VARCHAR(255) DEFAULT NULL COMMENT 'Avatar URL',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT 'Status: 0-disabled, 1-enabled',
    `locked` TINYINT NOT NULL DEFAULT 0 COMMENT 'Locked: 0-unlocked, 1-locked',
    `locked_until` DATETIME DEFAULT NULL COMMENT 'Locked until time',
    `login_fail_count` INT NOT NULL DEFAULT 0 COMMENT 'Login fail count',
    `last_login_time` DATETIME DEFAULT NULL COMMENT 'Last login time',
    `last_login_ip` VARCHAR(50) DEFAULT NULL COMMENT 'Last login IP',
    `password_changed_at` DATETIME DEFAULT NULL COMMENT 'Password changed time',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT 'Soft delete: 0-not deleted, 1-deleted',
    `created_by` BIGINT DEFAULT NULL COMMENT 'Created by user ID',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Created time',
    `updated_by` BIGINT DEFAULT NULL COMMENT 'Updated by user ID',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Updated time',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username_tenant` (`username`, `tenant_id`),
    UNIQUE KEY `uk_email_tenant` (`email`, `tenant_id`),
    KEY `idx_tenant_id` (`tenant_id`),
    KEY `idx_status` (`status`),
    KEY `idx_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='User Table';

-- Login Session Table
CREATE TABLE IF NOT EXISTS `login_session` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'Session ID',
    `tenant_id` BIGINT NOT NULL DEFAULT 0 COMMENT 'Tenant ID',
    `user_id` BIGINT NOT NULL COMMENT 'User ID',
    `token_id` VARCHAR(64) NOT NULL COMMENT 'JWT Token ID',
    `device_type` VARCHAR(20) DEFAULT 'web' COMMENT 'Device type: web, mobile, desktop',
    `device_info` VARCHAR(255) DEFAULT NULL COMMENT 'Device info (User-Agent)',
    `ip_address` VARCHAR(50) DEFAULT NULL COMMENT 'IP address',
    `login_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Login time',
    `expire_time` DATETIME NOT NULL COMMENT 'Token expire time',
    `logout_time` DATETIME DEFAULT NULL COMMENT 'Logout time',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT 'Status: 0-logout, 1-active',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Created time',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Updated time',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_token_id` (`token_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_tenant_id` (`tenant_id`),
    KEY `idx_expire_time` (`expire_time`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Login Session Table';

-- Refresh Token Table
CREATE TABLE IF NOT EXISTS `refresh_token` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'Token ID',
    `tenant_id` BIGINT NOT NULL DEFAULT 0 COMMENT 'Tenant ID',
    `user_id` BIGINT NOT NULL COMMENT 'User ID',
    `token_hash` VARCHAR(128) NOT NULL COMMENT 'Refresh token hash (SHA-256)',
    `session_id` BIGINT DEFAULT NULL COMMENT 'Associated session ID',
    `expire_time` DATETIME NOT NULL COMMENT 'Token expire time',
    `revoked` TINYINT NOT NULL DEFAULT 0 COMMENT 'Revoked: 0-valid, 1-revoked',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Created time',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_token_hash` (`token_hash`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_tenant_id` (`tenant_id`),
    KEY `idx_expire_time` (`expire_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Refresh Token Table';

-- Login Log Table
CREATE TABLE IF NOT EXISTS `login_log` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'Log ID',
    `tenant_id` BIGINT NOT NULL DEFAULT 0 COMMENT 'Tenant ID',
    `user_id` BIGINT DEFAULT NULL COMMENT 'User ID (null if login failed)',
    `username` VARCHAR(50) DEFAULT NULL COMMENT 'Username attempted',
    `login_type` TINYINT NOT NULL DEFAULT 1 COMMENT 'Login type: 1-password, 2-sms, 3-social',
    `login_status` TINYINT NOT NULL COMMENT 'Status: 0-fail, 1-success',
    `fail_reason` VARCHAR(100) DEFAULT NULL COMMENT 'Fail reason',
    `ip_address` VARCHAR(50) DEFAULT NULL COMMENT 'IP address',
    `device_info` VARCHAR(255) DEFAULT NULL COMMENT 'Device info',
    `login_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Login time',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_tenant_id` (`tenant_id`),
    KEY `idx_login_time` (`login_time`),
    KEY `idx_login_status` (`login_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Login Log Table';

-- Captcha Table
CREATE TABLE IF NOT EXISTS `captcha` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'Captcha ID',
    `captcha_key` VARCHAR(64) NOT NULL COMMENT 'Captcha key',
    `captcha_value` VARCHAR(10) NOT NULL COMMENT 'Captcha value',
    `captcha_type` TINYINT NOT NULL DEFAULT 1 COMMENT 'Type: 1-image, 2-sms, 3-email',
    `expire_time` DATETIME NOT NULL COMMENT 'Expire time',
    `used` TINYINT NOT NULL DEFAULT 0 COMMENT 'Used: 0-unused, 1-used',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Created time',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_captcha_key` (`captcha_key`),
    KEY `idx_expire_time` (`expire_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Captcha Table';

-- Password History Table (for password policy)
CREATE TABLE IF NOT EXISTS `password_history` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'History ID',
    `tenant_id` BIGINT NOT NULL DEFAULT 0 COMMENT 'Tenant ID',
    `user_id` BIGINT NOT NULL COMMENT 'User ID',
    `password_hash` VARCHAR(255) NOT NULL COMMENT 'Password hash',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Created time',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_tenant_id` (`tenant_id`),
    KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Password History Table';

-- Social Account Binding Table
CREATE TABLE IF NOT EXISTS `social_account` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'Social Account ID',
    `tenant_id` BIGINT NOT NULL DEFAULT 0 COMMENT 'Tenant ID',
    `user_id` BIGINT NOT NULL COMMENT 'User ID',
    `provider` VARCHAR(20) NOT NULL COMMENT 'Provider: wechat, alipay, dingtalk',
    `provider_user_id` VARCHAR(100) NOT NULL COMMENT 'Provider user ID',
    `union_id` VARCHAR(100) DEFAULT NULL COMMENT 'Union ID',
    `nickname` VARCHAR(100) DEFAULT NULL COMMENT 'Nickname',
    `avatar` VARCHAR(255) DEFAULT NULL COMMENT 'Avatar',
    `extra_data` TEXT DEFAULT NULL COMMENT 'Extra data (JSON)',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Created time',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Updated time',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_provider_user` (`provider`, `provider_user_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_tenant_id` (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Social Account Table';

-- Insert default admin user (password: admin123)
-- BCrypt hash generated with strength 10
INSERT INTO `user` (`tenant_id`, `username`, `password`, `email`, `real_name`, `status`, `created_at`)
VALUES (1, 'admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt9VJ.S', 'admin@example.com', 'System Admin', 1, NOW());

-- Create indexes for performance
CREATE INDEX idx_user_deleted_status ON `user` (deleted, status);
CREATE INDEX idx_session_user_status ON `login_session` (user_id, status);
CREATE INDEX idx_token_user_revoked ON `refresh_token` (user_id, revoked);
