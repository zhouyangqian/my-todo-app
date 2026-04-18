-- 字典服务数据库脚本
-- Version: 1.0.0

CREATE DATABASE IF NOT EXISTS my_todo_dict DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE my_todo_dict;

-- 字典类型表
CREATE TABLE IF NOT EXISTS `sys_dict_type` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '字典类型ID',
    `tenant_id` BIGINT NOT NULL DEFAULT 0 COMMENT '租户ID',
    `dict_code` VARCHAR(50) NOT NULL COMMENT '字典类型编码',
    `dict_name` VARCHAR(100) NOT NULL COMMENT '字典类型名称',
    `description` VARCHAR(255) DEFAULT NULL COMMENT '描述',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 0-禁用, 1-启用',
    `is_system` TINYINT NOT NULL DEFAULT 0 COMMENT '是否系统内置: 0-否, 1-是',
    `sort` INT NOT NULL DEFAULT 0 COMMENT '排序',
    `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '软删除',
    `created_by` BIGINT DEFAULT NULL COMMENT '创建人',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_by` BIGINT DEFAULT NULL COMMENT '更新人',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_dict_code_tenant` (`dict_code`, `tenant_id`, `deleted`),
    KEY `idx_tenant_id` (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='字典类型表';

-- 字典项表
CREATE TABLE IF NOT EXISTS `sys_dict_item` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '字典项ID',
    `tenant_id` BIGINT NOT NULL DEFAULT 0 COMMENT '租户ID',
    `dict_type_id` BIGINT NOT NULL COMMENT '字典类型ID',
    `item_code` VARCHAR(50) NOT NULL COMMENT '字典项编码',
    `item_name` VARCHAR(100) NOT NULL COMMENT '字典项名称',
    `item_value` VARCHAR(255) DEFAULT NULL COMMENT '字典项值',
    `ext_value1` VARCHAR(255) DEFAULT NULL COMMENT '扩展值1',
    `ext_value2` VARCHAR(255) DEFAULT NULL COMMENT '扩展值2',
    `ext_value3` VARCHAR(255) DEFAULT NULL COMMENT '扩展值3',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 0-禁用, 1-启用',
    `is_default` TINYINT NOT NULL DEFAULT 0 COMMENT '是否默认: 0-否, 1-是',
    `sort` INT NOT NULL DEFAULT 0 COMMENT '排序',
    `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '软删除',
    `created_by` BIGINT DEFAULT NULL COMMENT '创建人',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_by` BIGINT DEFAULT NULL COMMENT '更新人',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_tenant_id` (`tenant_id`),
    KEY `idx_dict_type_id` (`dict_type_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='字典项表';

-- 系统配置表
CREATE TABLE IF NOT EXISTS `sys_config` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '配置ID',
    `tenant_id` BIGINT NOT NULL DEFAULT 0 COMMENT '租户ID',
    `config_code` VARCHAR(50) NOT NULL COMMENT '配置编码',
    `config_name` VARCHAR(100) NOT NULL COMMENT '配置名称',
    `config_value` TEXT COMMENT '配置值',
    `config_type` TINYINT NOT NULL DEFAULT 1 COMMENT '配置类型: 1-系统, 2-业务',
    `description` VARCHAR(255) DEFAULT NULL COMMENT '描述',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 0-禁用, 1-启用',
    `is_system` TINYINT NOT NULL DEFAULT 0 COMMENT '是否系统内置: 0-否, 1-是',
    `sort` INT NOT NULL DEFAULT 0 COMMENT '排序',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '软删除',
    `created_by` BIGINT DEFAULT NULL COMMENT '创建人',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_by` BIGINT DEFAULT NULL COMMENT '更新人',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_config_code_tenant` (`config_code`, `tenant_id`, `deleted`),
    KEY `idx_tenant_id` (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统配置表';

-- 插入默认字典类型
INSERT INTO `sys_dict_type` (`tenant_id`, `dict_code`, `dict_name`, `description`, `is_system`, `sort`, `created_at`)
VALUES
(1, 'gender', '性别', '用户性别', 1, 1, NOW()),
(1, 'status', '状态', '通用状态', 1, 2, NOW()),
(1, 'yes_no', '是否', '是否选项', 1, 3, NOW()),
(1, 'order_status', '订单状态', '订单状态', 1, 4, NOW()),
(1, 'payment_status', '支付状态', '支付状态', 1, 5, NOW());

-- 插入默认字典项
INSERT INTO `sys_dict_item` (`tenant_id`, `dict_type_id`, `item_code`, `item_name`, `item_value`, `is_default`, `sort`, `created_at`)
SELECT 1, id, 'male', '男', '1', 0, 1, NOW() FROM `sys_dict_type` WHERE `dict_code` = 'gender'
UNION ALL
SELECT 1, id, 'female', '女', '2', 0, 2, NOW() FROM `sys_dict_type` WHERE `dict_code` = 'gender'
UNION ALL
SELECT 1, id, 'unknown', '未知', '0', 1, 3, NOW() FROM `sys_dict_type` WHERE `dict_code` = 'gender';

INSERT INTO `sys_dict_item` (`tenant_id`, `dict_type_id`, `item_code`, `item_name`, `item_value`, `is_default`, `sort`, `created_at`)
SELECT 1, id, 'enabled', '启用', '1', 1, 1, NOW() FROM `sys_dict_type` WHERE `dict_code` = 'status'
UNION ALL
SELECT 1, id, 'disabled', '禁用', '0', 0, 2, NOW() FROM `sys_dict_type` WHERE `dict_code` = 'status';

INSERT INTO `sys_dict_item` (`tenant_id`, `dict_type_id`, `item_code`, `item_name`, `item_value`, `is_default`, `sort`, `created_at`)
SELECT 1, id, 'yes', '是', '1', 0, 1, NOW() FROM `sys_dict_type` WHERE `dict_code` = 'yes_no'
UNION ALL
SELECT 1, id, 'no', '否', '0', 1, 2, NOW() FROM `sys_dict_type` WHERE `dict_code` = 'yes_no';

-- 插入默认系统配置
INSERT INTO `sys_config` (`tenant_id`, `config_code`, `config_name`, `config_value`, `config_type`, `description`, `is_system`, `sort`, `created_at`)
VALUES
(1, 'system.name', '系统名称', 'My Todo App', 1, '系统显示名称', 1, 1, NOW()),
(1, 'system.logo', '系统Logo', '/logo.png', 1, '系统Logo路径', 1, 2, NOW()),
(1, 'login.max_attempts', '最大登录尝试次数', '5', 1, '登录失败最大次数', 1, 3, NOW()),
(1, 'login.lock_duration', '账户锁定时长(分钟)', '30', 1, '账户锁定时长', 1, 4, NOW()),
(1, 'password.min_length', '密码最小长度', '8', 1, '密码最小长度要求', 1, 5, NOW());
