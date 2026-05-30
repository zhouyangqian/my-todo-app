-- 供应商联系人表
CREATE TABLE IF NOT EXISTS `supplier_contact` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '联系人ID（主键，自增）',
    `tenant_id` BIGINT NOT NULL COMMENT '租户ID',
    `supplier_id` BIGINT NOT NULL COMMENT '供应商ID',
    `name` VARCHAR(100) NOT NULL COMMENT '联系人姓名',
    `position` VARCHAR(100) DEFAULT NULL COMMENT '职位',
    `phone` VARCHAR(50) DEFAULT NULL COMMENT '联系电话',
    `email` VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
    `is_default` INT NOT NULL DEFAULT 0 COMMENT '是否默认联系人：0-否，1-是',
    `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
    `deleted` INT NOT NULL DEFAULT 0 COMMENT '软删除标记：0-未删除，1-已删除',
    `created_by` BIGINT DEFAULT NULL COMMENT '创建人ID',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_by` BIGINT DEFAULT NULL COMMENT '更新人ID',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_supplier_contact_supplier_id` (`supplier_id`),
    KEY `idx_supplier_contact_tenant_id` (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='供应商联系人表';

-- 客户联系人表
CREATE TABLE IF NOT EXISTS `customer_contact` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '联系人ID（主键，自增）',
    `tenant_id` BIGINT NOT NULL COMMENT '租户ID',
    `customer_id` BIGINT NOT NULL COMMENT '客户ID',
    `name` VARCHAR(100) NOT NULL COMMENT '联系人姓名',
    `position` VARCHAR(100) DEFAULT NULL COMMENT '职位',
    `phone` VARCHAR(50) DEFAULT NULL COMMENT '联系电话',
    `email` VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
    `is_default` INT NOT NULL DEFAULT 0 COMMENT '是否默认联系人：0-否，1-是',
    `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
    `deleted` INT NOT NULL DEFAULT 0 COMMENT '软删除标记：0-未删除，1-已删除',
    `created_by` BIGINT DEFAULT NULL COMMENT '创建人ID',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_by` BIGINT DEFAULT NULL COMMENT '更新人ID',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_customer_contact_customer_id` (`customer_id`),
    KEY `idx_customer_contact_tenant_id` (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='客户联系人表';

-- 供应商表增加信用额度和当前欠款字段
ALTER TABLE `erp_supplier`
    ADD COLUMN `credit_limit` DECIMAL(18,2) NOT NULL DEFAULT 0.00 COMMENT '信用额度，供应商可赊账的最大金额' AFTER `credit_days`,
    ADD COLUMN `current_debt` DECIMAL(18,2) NOT NULL DEFAULT 0.00 COMMENT '当前欠款，供应商当前未结算的采购金额' AFTER `credit_limit`;

-- 客户表增加当前欠款字段
ALTER TABLE `erp_customer`
    ADD COLUMN `current_debt` DECIMAL(18,2) NOT NULL DEFAULT 0.00 COMMENT '当前欠款，客户当前未结算的销售金额' AFTER `credit_limit`;
