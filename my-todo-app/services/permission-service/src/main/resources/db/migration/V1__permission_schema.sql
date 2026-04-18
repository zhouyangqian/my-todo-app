-- Permission Service Database Schema
-- Version: 1.0.0

CREATE DATABASE IF NOT EXISTS my_todo_permission DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE my_todo_permission;

-- Role Table
CREATE TABLE IF NOT EXISTS `sys_role` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'Role ID',
    `tenant_id` BIGINT NOT NULL DEFAULT 0 COMMENT 'Tenant ID',
    `role_code` VARCHAR(50) NOT NULL COMMENT 'Role code',
    `role_name` VARCHAR(100) NOT NULL COMMENT 'Role name',
    `description` VARCHAR(255) DEFAULT NULL COMMENT 'Description',
    `parent_id` BIGINT DEFAULT 0 COMMENT 'Parent role ID',
    `sort` INT NOT NULL DEFAULT 0 COMMENT 'Sort order',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT 'Status: 0-disabled, 1-enabled',
    `data_scope` TINYINT NOT NULL DEFAULT 1 COMMENT 'Data scope: 1-all, 2-department, 3-self',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT 'Soft delete',
    `created_by` BIGINT DEFAULT NULL COMMENT 'Created by',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Created time',
    `updated_by` BIGINT DEFAULT NULL COMMENT 'Updated by',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Updated time',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_role_code_tenant` (`role_code`, `tenant_id`, `deleted`),
    KEY `idx_tenant_id` (`tenant_id`),
    KEY `idx_parent_id` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Role Table';

-- Permission Table
CREATE TABLE IF NOT EXISTS `sys_permission` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'Permission ID',
    `tenant_id` BIGINT NOT NULL DEFAULT 0 COMMENT 'Tenant ID',
    `parent_id` BIGINT DEFAULT 0 COMMENT 'Parent permission ID',
    `permission_code` VARCHAR(100) NOT NULL COMMENT 'Permission code',
    `permission_name` VARCHAR(100) NOT NULL COMMENT 'Permission name',
    `permission_type` TINYINT NOT NULL DEFAULT 1 COMMENT 'Type: 1-menu, 2-button, 3-api',
    `resource_path` VARCHAR(255) DEFAULT NULL COMMENT 'Resource path',
    `http_method` VARCHAR(10) DEFAULT NULL COMMENT 'HTTP method',
    `icon` VARCHAR(100) DEFAULT NULL COMMENT 'Icon',
    `menu_path` VARCHAR(255) DEFAULT NULL COMMENT 'Menu path',
    `component` VARCHAR(255) DEFAULT NULL COMMENT 'Component path',
    `sort` INT NOT NULL DEFAULT 0 COMMENT 'Sort order',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT 'Status: 0-disabled, 1-enabled',
    `visible` TINYINT NOT NULL DEFAULT 1 COMMENT 'Visible in menu: 0-no, 1-yes',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT 'Soft delete',
    `created_by` BIGINT DEFAULT NULL COMMENT 'Created by',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Created time',
    `updated_by` BIGINT DEFAULT NULL COMMENT 'Updated by',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Updated time',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_permission_code_tenant` (`permission_code`, `tenant_id`, `deleted`),
    KEY `idx_tenant_id` (`tenant_id`),
    KEY `idx_parent_id` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Permission Table';

-- User Role Association Table
CREATE TABLE IF NOT EXISTS `sys_user_role` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `tenant_id` BIGINT NOT NULL DEFAULT 0 COMMENT 'Tenant ID',
    `user_id` BIGINT NOT NULL COMMENT 'User ID',
    `role_id` BIGINT NOT NULL COMMENT 'Role ID',
    `created_by` BIGINT DEFAULT NULL COMMENT 'Created by',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Created time',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_role` (`user_id`, `role_id`),
    KEY `idx_tenant_id` (`tenant_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_role_id` (`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='User Role Association Table';

-- Role Permission Association Table
CREATE TABLE IF NOT EXISTS `sys_role_permission` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `tenant_id` BIGINT NOT NULL DEFAULT 0 COMMENT 'Tenant ID',
    `role_id` BIGINT NOT NULL COMMENT 'Role ID',
    `permission_id` BIGINT NOT NULL COMMENT 'Permission ID',
    `created_by` BIGINT DEFAULT NULL COMMENT 'Created by',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Created time',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_role_permission` (`role_id`, `permission_id`),
    KEY `idx_tenant_id` (`tenant_id`),
    KEY `idx_role_id` (`role_id`),
    KEY `idx_permission_id` (`permission_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Role Permission Association Table';

-- Department Table
CREATE TABLE IF NOT EXISTS `sys_department` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'Department ID',
    `tenant_id` BIGINT NOT NULL DEFAULT 0 COMMENT 'Tenant ID',
    `parent_id` BIGINT DEFAULT 0 COMMENT 'Parent department ID',
    `dept_code` VARCHAR(50) NOT NULL COMMENT 'Department code',
    `dept_name` VARCHAR(100) NOT NULL COMMENT 'Department name',
    `full_path` VARCHAR(500) DEFAULT NULL COMMENT 'Full path',
    `level` INT NOT NULL DEFAULT 1 COMMENT 'Level',
    `leader_id` BIGINT DEFAULT NULL COMMENT 'Leader user ID',
    `phone` VARCHAR(20) DEFAULT NULL COMMENT 'Phone',
    `email` VARCHAR(100) DEFAULT NULL COMMENT 'Email',
    `sort` INT NOT NULL DEFAULT 0 COMMENT 'Sort order',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT 'Status: 0-disabled, 1-enabled',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT 'Soft delete',
    `created_by` BIGINT DEFAULT NULL COMMENT 'Created by',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Created time',
    `updated_by` BIGINT DEFAULT NULL COMMENT 'Updated by',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Updated time',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_dept_code_tenant` (`dept_code`, `tenant_id`, `deleted`),
    KEY `idx_tenant_id` (`tenant_id`),
    KEY `idx_parent_id` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Department Table';

-- Insert default roles
INSERT INTO `sys_role` (`tenant_id`, `role_code`, `role_name`, `description`, `sort`, `status`, `data_scope`, `created_at`)
VALUES
(1, 'SUPER_ADMIN', 'Super Administrator', 'Full system access', 1, 1, 1, NOW()),
(1, 'ADMIN', 'Administrator', 'Admin access', 2, 1, 1, NOW()),
(1, 'USER', 'Normal User', 'Basic user access', 3, 1, 3, NOW());

-- Insert default permissions
INSERT INTO `sys_permission` (`tenant_id`, `parent_id`, `permission_code`, `permission_name`, `permission_type`, `menu_path`, `component`, `sort`, `status`, `visible`, `created_at`)
VALUES
(1, 0, 'system', 'System Management', 1, '/system', 'Layout', 1, 1, 1, NOW()),
(1, 1, 'system:user', 'User Management', 1, '/system/user', 'system/user/index', 1, 1, 1, NOW()),
(1, 1, 'system:role', 'Role Management', 1, '/system/role', 'system/role/index', 2, 1, 1, NOW()),
(1, 1, 'system:permission', 'Permission Management', 1, '/system/permission', 'system/permission/index', 3, 1, 1, NOW()),
(1, 1, 'system:dept', 'Department Management', 1, '/system/dept', 'system/dept/index', 4, 1, 1, NOW()),
(1, 0, 'erp', 'ERP Management', 1, '/erp', 'Layout', 2, 1, 1, NOW()),
(1, 6, 'erp:purchase', 'Purchase Management', 1, '/erp/purchase', 'erp/purchase/index', 1, 1, 1, NOW()),
(1, 6, 'erp:sales', 'Sales Management', 1, '/erp/sales', 'erp/sales/index', 2, 1, 1, NOW()),
(1, 6, 'erp:inventory', 'Inventory Management', 1, '/erp/inventory', 'erp/inventory/index', 3, 1, 1, NOW()),
(1, 0, 'finance', 'Finance Management', 1, '/finance', 'Layout', 3, 1, 1, NOW());

-- Assign all permissions to super admin
INSERT INTO `sys_role_permission` (`tenant_id`, `role_id`, `permission_id`, `created_at`)
SELECT 1, 1, id, NOW() FROM `sys_permission`;
