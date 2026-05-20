-- 财务服务扩展表: 成本核算 + 财务报表
-- Version: 1.0.0

USE `my_todo_finance`;

-- 成本核算配置表
CREATE TABLE IF NOT EXISTS `fin_cost_config` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '配置ID',
    `tenant_id` BIGINT NOT NULL DEFAULT 0 COMMENT '租户ID',
    `product_id` BIGINT NOT NULL COMMENT '商品ID',
    `cost_method` TINYINT NOT NULL DEFAULT 1 COMMENT '成本核算方法: 1-FIFO, 2-加权平均, 3-个别计价',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 0-停用, 1-启用',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '软删除',
    `created_by` BIGINT DEFAULT NULL COMMENT '创建人',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_by` BIGINT DEFAULT NULL COMMENT '更新人',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_product_tenant` (`product_id`, `tenant_id`, `deleted`),
    KEY `idx_tenant_id` (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='成本核算配置表';

-- 成本历史记录表
CREATE TABLE IF NOT EXISTS `fin_cost_history` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '记录ID',
    `tenant_id` BIGINT NOT NULL DEFAULT 0 COMMENT '租户ID',
    `product_id` BIGINT NOT NULL COMMENT '商品ID',
    `warehouse_id` BIGINT NOT NULL COMMENT '仓库ID',
    `cost_price` DECIMAL(18,4) NOT NULL COMMENT '成本单价',
    `quantity` DECIMAL(18,4) NOT NULL COMMENT '数量',
    `total_cost` DECIMAL(18,4) NOT NULL COMMENT '总成本',
    `biz_type` TINYINT NOT NULL COMMENT '业务类型: 1-采购入库, 2-销售出库, 3-调拨, 4-盘点调整',
    `biz_no` VARCHAR(50) DEFAULT NULL COMMENT '业务单号',
    `cost_method` TINYINT NOT NULL COMMENT '使用的成本方法',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '软删除',
    `created_by` BIGINT DEFAULT NULL COMMENT '创建人',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_tenant_product` (`tenant_id`, `product_id`),
    KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='成本历史记录表';

-- 毛利计算表
CREATE TABLE IF NOT EXISTS `fin_profit_calc` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '记录ID',
    `tenant_id` BIGINT NOT NULL DEFAULT 0 COMMENT '租户ID',
    `product_id` BIGINT NOT NULL COMMENT '商品ID',
    `sales_order_id` BIGINT DEFAULT NULL COMMENT '销售订单ID',
    `sales_quantity` DECIMAL(18,4) NOT NULL COMMENT '销售数量',
    `sales_amount` DECIMAL(18,2) NOT NULL COMMENT '销售金额',
    `cost_amount` DECIMAL(18,2) NOT NULL COMMENT '成本金额',
    `gross_profit` DECIMAL(18,2) NOT NULL COMMENT '毛利',
    `gross_profit_rate` DECIMAL(8,4) NOT NULL COMMENT '毛利率',
    `calc_date` DATE NOT NULL COMMENT '计算日期',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '软删除',
    `created_by` BIGINT DEFAULT NULL COMMENT '创建人',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_tenant_product` (`tenant_id`, `product_id`),
    KEY `idx_calc_date` (`calc_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='毛利计算表';

-- 财务报表缓存表
CREATE TABLE IF NOT EXISTS `fin_report` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '报表ID',
    `tenant_id` BIGINT NOT NULL DEFAULT 0 COMMENT '租户ID',
    `report_type` TINYINT NOT NULL COMMENT '报表类型: 1-资产负债表, 2-利润表, 3-现金流量表, 4-毛利分析',
    `report_period` VARCHAR(20) NOT NULL COMMENT '报表期间 (如 2026-01)',
    `start_date` DATE NOT NULL COMMENT '开始日期',
    `end_date` DATE NOT NULL COMMENT '结束日期',
    `report_data` JSON NOT NULL COMMENT '报表数据(JSON)',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 0-草稿, 1-已生成, 2-已锁定',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '软删除',
    `created_by` BIGINT DEFAULT NULL COMMENT '创建人',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_by` BIGINT DEFAULT NULL COMMENT '更新人',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_type_period_tenant` (`report_type`, `report_period`, `tenant_id`, `deleted`),
    KEY `idx_tenant_id` (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='财务报表缓存表';
