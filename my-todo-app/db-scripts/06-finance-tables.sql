-- 财务服务数据库表结构
-- Version: 1.0.0

USE `my-todo-app-dev`;

-- 银行账户表
CREATE TABLE IF NOT EXISTS `fin_bank_account` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '账户ID',
    `tenant_id` BIGINT NOT NULL DEFAULT 0 COMMENT '租户ID',
    `account_code` VARCHAR(50) NOT NULL COMMENT '账户编码',
    `account_name` VARCHAR(100) NOT NULL COMMENT '账户名称',
    `account_type` TINYINT NOT NULL DEFAULT 1 COMMENT '账户类型: 1-现金账户, 2-银行账户, 3-支付宝, 4-微信',
    `bank_name` VARCHAR(100) DEFAULT NULL COMMENT '开户银行',
    `bank_account_no` VARCHAR(50) DEFAULT NULL COMMENT '银行账号',
    `balance` DECIMAL(18,2) NOT NULL DEFAULT 0 COMMENT '账户余额',
    `currency` VARCHAR(10) NOT NULL DEFAULT 'CNY' COMMENT '币种',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 0-停用, 1-启用',
    `is_default` TINYINT NOT NULL DEFAULT 0 COMMENT '是否默认账户',
    `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '软删除',
    `created_by` BIGINT DEFAULT NULL COMMENT '创建人',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_by` BIGINT DEFAULT NULL COMMENT '更新人',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_account_code_tenant` (`account_code`, `tenant_id`, `deleted`),
    KEY `idx_tenant_id` (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='银行账户表';

-- 应收账款表
CREATE TABLE IF NOT EXISTS `fin_account_receivable` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '应收ID',
    `tenant_id` BIGINT NOT NULL DEFAULT 0 COMMENT '租户ID',
    `biz_no` VARCHAR(50) NOT NULL COMMENT '业务单号',
    `customer_id` BIGINT NOT NULL COMMENT '客户ID',
    `amount` DECIMAL(18,2) NOT NULL COMMENT '应收金额',
    `received_amount` DECIMAL(18,2) NOT NULL DEFAULT 0 COMMENT '已收金额',
    `unreceived_amount` DECIMAL(18,2) NOT NULL COMMENT '未收金额',
    `currency` VARCHAR(10) NOT NULL DEFAULT 'CNY' COMMENT '币种',
    `biz_date` DATETIME NOT NULL COMMENT '业务日期',
    `due_date` DATETIME DEFAULT NULL COMMENT '应收日期',
    `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态: 0-未结算, 1-部分结算, 2-已结算',
    `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '软删除',
    `created_by` BIGINT DEFAULT NULL COMMENT '创建人',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_by` BIGINT DEFAULT NULL COMMENT '更新人',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_tenant_id` (`tenant_id`),
    KEY `idx_customer_id` (`customer_id`),
    KEY `idx_biz_no` (`biz_no`),
    KEY `idx_status` (`status`),
    KEY `idx_due_date` (`due_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='应收账款表';

-- 应付账款表
CREATE TABLE IF NOT EXISTS `fin_account_payable` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '应付ID',
    `tenant_id` BIGINT NOT NULL DEFAULT 0 COMMENT '租户ID',
    `biz_no` VARCHAR(50) NOT NULL COMMENT '业务单号',
    `supplier_id` BIGINT NOT NULL COMMENT '供应商ID',
    `amount` DECIMAL(18,2) NOT NULL COMMENT '应付金额',
    `paid_amount` DECIMAL(18,2) NOT NULL DEFAULT 0 COMMENT '已付金额',
    `unpaid_amount` DECIMAL(18,2) NOT NULL COMMENT '未付金额',
    `currency` VARCHAR(10) NOT NULL DEFAULT 'CNY' COMMENT '币种',
    `biz_date` DATETIME NOT NULL COMMENT '业务日期',
    `due_date` DATETIME DEFAULT NULL COMMENT '应付日期',
    `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态: 0-未结算, 1-部分结算, 2-已结算',
    `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '软删除',
    `created_by` BIGINT DEFAULT NULL COMMENT '创建人',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_by` BIGINT DEFAULT NULL COMMENT '更新人',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_tenant_id` (`tenant_id`),
    KEY `idx_supplier_id` (`supplier_id`),
    KEY `idx_biz_no` (`biz_no`),
    KEY `idx_status` (`status`),
    KEY `idx_due_date` (`due_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='应付账款表';

-- 收支记录表
CREATE TABLE IF NOT EXISTS `fin_payment_record` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '记录ID',
    `tenant_id` BIGINT NOT NULL DEFAULT 0 COMMENT '租户ID',
    `record_no` VARCHAR(50) NOT NULL COMMENT '单据编号',
    `record_type` TINYINT NOT NULL COMMENT '收支类型: 1-收入, 2-支出',
    `biz_type` TINYINT NOT NULL COMMENT '业务类型: 1-销售收款, 2-采购付款, 3-退款, 4-其他收入, 5-其他支出',
    `biz_id` BIGINT DEFAULT NULL COMMENT '关联业务ID',
    `partner_id` BIGINT DEFAULT NULL COMMENT '客户/供应商ID',
    `amount` DECIMAL(18,2) NOT NULL COMMENT '金额',
    `currency` VARCHAR(10) NOT NULL DEFAULT 'CNY' COMMENT '币种',
    `payment_method` TINYINT NOT NULL DEFAULT 1 COMMENT '支付方式: 1-现金, 2-银行转账, 3-支付宝, 4-微信, 5-支票',
    `bank_account_id` BIGINT DEFAULT NULL COMMENT '银行账户ID',
    `transaction_date` DATETIME NOT NULL COMMENT '交易日期',
    `handler_id` BIGINT DEFAULT NULL COMMENT '经手人ID',
    `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态: 0-待审核, 1-已审核, 2-已取消',
    `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '软删除',
    `created_by` BIGINT DEFAULT NULL COMMENT '创建人',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_by` BIGINT DEFAULT NULL COMMENT '更新人',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_record_no_tenant` (`record_no`, `tenant_id`),
    KEY `idx_tenant_id` (`tenant_id`),
    KEY `idx_record_type` (`record_type`),
    KEY `idx_biz_type` (`biz_type`),
    KEY `idx_transaction_date` (`transaction_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='收支记录表';

-- 发票表
CREATE TABLE IF NOT EXISTS `fin_invoice` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '发票ID',
    `tenant_id` BIGINT NOT NULL DEFAULT 0 COMMENT '租户ID',
    `invoice_no` VARCHAR(50) NOT NULL COMMENT '发票编号',
    `invoice_type` TINYINT NOT NULL DEFAULT 1 COMMENT '发票类型: 1-增值税专用发票, 2-增值税普通发票, 3-电子发票',
    `invoice_direction` TINYINT NOT NULL COMMENT '发票方向: 1-开票(销售), 2-收票(采购)',
    `biz_id` BIGINT DEFAULT NULL COMMENT '关联业务ID',
    `partner_id` BIGINT DEFAULT NULL COMMENT '客户/供应商ID',
    `invoice_date` DATETIME NOT NULL COMMENT '开票日期',
    `amount_without_tax` DECIMAL(18,2) NOT NULL COMMENT '不含税金额',
    `tax_amount` DECIMAL(18,2) NOT NULL DEFAULT 0 COMMENT '税额',
    `total_amount` DECIMAL(18,2) NOT NULL COMMENT '价税合计',
    `tax_rate` DECIMAL(5,2) DEFAULT 13 COMMENT '税率',
    `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态: 0-待开票, 1-已开票, 2-已作废',
    `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '软删除',
    `created_by` BIGINT DEFAULT NULL COMMENT '创建人',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_by` BIGINT DEFAULT NULL COMMENT '更新人',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_invoice_no_tenant` (`invoice_no`, `tenant_id`),
    KEY `idx_tenant_id` (`tenant_id`),
    KEY `idx_invoice_direction` (`invoice_direction`),
    KEY `idx_partner_id` (`partner_id`),
    KEY `idx_invoice_date` (`invoice_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='发票表';

-- 插入默认银行账户
INSERT INTO `fin_bank_account` (`tenant_id`, `account_code`, `account_name`, `account_type`, `bank_name`, `bank_account_no`, `balance`, `status`, `is_default`, `created_at`)
VALUES
(1, 'CASH001', '现金账户', 1, NULL, NULL, 0, 1, 1, NOW()),
(1, 'BANK001', '基本户', 2, '中国银行', '6210000000000000000', 0, 1, 0, NOW());
