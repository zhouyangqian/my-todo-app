-- ERP服务数据库脚本
-- Version: 1.0.0

CREATE DATABASE IF NOT EXISTS my_todo_erp DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE my_todo_erp;

-- 商品表
CREATE TABLE IF NOT EXISTS `erp_product` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '商品ID',
    `tenant_id` BIGINT NOT NULL DEFAULT 0 COMMENT '租户ID',
    `product_code` VARCHAR(50) NOT NULL COMMENT '商品编码',
    `product_name` VARCHAR(200) NOT NULL COMMENT '商品名称',
    `category_id` BIGINT DEFAULT NULL COMMENT '分类ID',
    `barcode` VARCHAR(50) DEFAULT NULL COMMENT '条码',
    `specification` VARCHAR(100) DEFAULT NULL COMMENT '规格',
    `unit` VARCHAR(20) DEFAULT NULL COMMENT '单位',
    `cost_price` DECIMAL(18,4) DEFAULT 0 COMMENT '成本价',
    `sale_price` DECIMAL(18,4) DEFAULT 0 COMMENT '销售价',
    `min_price` DECIMAL(18,4) DEFAULT 0 COMMENT '最低售价',
    `stock_quantity` DECIMAL(18,4) DEFAULT 0 COMMENT '库存数量',
    `stock_min` DECIMAL(18,4) DEFAULT 0 COMMENT '库存预警下限',
    `stock_max` DECIMAL(18,4) DEFAULT 0 COMMENT '库存预警上限',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 0-停用, 1-启用',
    `image_url` VARCHAR(500) DEFAULT NULL COMMENT '商品图片',
    `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '软删除',
    `created_by` BIGINT DEFAULT NULL COMMENT '创建人',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_by` BIGINT DEFAULT NULL COMMENT '更新人',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_product_code_tenant` (`product_code`, `tenant_id`, `deleted`),
    KEY `idx_tenant_id` (`tenant_id`),
    KEY `idx_category_id` (`category_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品表';

-- 仓库表
CREATE TABLE IF NOT EXISTS `erp_warehouse` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '仓库ID',
    `tenant_id` BIGINT NOT NULL DEFAULT 0 COMMENT '租户ID',
    `warehouse_code` VARCHAR(50) NOT NULL COMMENT '仓库编码',
    `warehouse_name` VARCHAR(100) NOT NULL COMMENT '仓库名称',
    `warehouse_type` TINYINT NOT NULL DEFAULT 1 COMMENT '仓库类型: 1-普通仓, 2-门店仓, 3-虚拟仓',
    `manager_id` BIGINT DEFAULT NULL COMMENT '负责人ID',
    `phone` VARCHAR(20) DEFAULT NULL COMMENT '联系电话',
    `address` VARCHAR(255) DEFAULT NULL COMMENT '地址',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 0-停用, 1-启用',
    `is_default` TINYINT NOT NULL DEFAULT 0 COMMENT '是否默认仓库',
    `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '软删除',
    `created_by` BIGINT DEFAULT NULL COMMENT '创建人',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_by` BIGINT DEFAULT NULL COMMENT '更新人',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_warehouse_code_tenant` (`warehouse_code`, `tenant_id`, `deleted`),
    KEY `idx_tenant_id` (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='仓库表';

-- 库存表
CREATE TABLE IF NOT EXISTS `erp_inventory` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '库存ID',
    `tenant_id` BIGINT NOT NULL DEFAULT 0 COMMENT '租户ID',
    `warehouse_id` BIGINT NOT NULL COMMENT '仓库ID',
    `product_id` BIGINT NOT NULL COMMENT '商品ID',
    `batch_no` VARCHAR(50) DEFAULT NULL COMMENT '批次号',
    `quantity` DECIMAL(18,4) NOT NULL DEFAULT 0 COMMENT '库存数量',
    `locked_quantity` DECIMAL(18,4) NOT NULL DEFAULT 0 COMMENT '锁定数量',
    `available_quantity` DECIMAL(18,4) NOT NULL DEFAULT 0 COMMENT '可用数量',
    `cost_price` DECIMAL(18,4) DEFAULT NULL COMMENT '成本价',
    `production_date` DATETIME DEFAULT NULL COMMENT '生产日期',
    `expiry_date` DATETIME DEFAULT NULL COMMENT '过期日期',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 0-禁用, 1-正常',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `version` INT NOT NULL DEFAULT 0 COMMENT '版本号(乐观锁)',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_warehouse_product` (`warehouse_id`, `product_id`),
    KEY `idx_tenant_id` (`tenant_id`),
    KEY `idx_product_id` (`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='库存表';

-- 供应商表
CREATE TABLE IF NOT EXISTS `erp_supplier` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '供应商ID',
    `tenant_id` BIGINT NOT NULL DEFAULT 0 COMMENT '租户ID',
    `supplier_code` VARCHAR(50) NOT NULL COMMENT '供应商编码',
    `supplier_name` VARCHAR(200) NOT NULL COMMENT '供应商名称',
    `contact_person` VARCHAR(50) DEFAULT NULL COMMENT '联系人',
    `phone` VARCHAR(20) DEFAULT NULL COMMENT '联系电话',
    `email` VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
    `address` VARCHAR(255) DEFAULT NULL COMMENT '地址',
    `bank_name` VARCHAR(100) DEFAULT NULL COMMENT '开户银行',
    `bank_account` VARCHAR(50) DEFAULT NULL COMMENT '银行账号',
    `tax_no` VARCHAR(50) DEFAULT NULL COMMENT '税号',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 0-停用, 1-启用',
    `settlement_type` TINYINT NOT NULL DEFAULT 1 COMMENT '结算方式: 1-现结, 2-月结, 3-账期',
    `credit_days` INT DEFAULT 30 COMMENT '账期天数',
    `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '软删除',
    `created_by` BIGINT DEFAULT NULL COMMENT '创建人',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_by` BIGINT DEFAULT NULL COMMENT '更新人',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_supplier_code_tenant` (`supplier_code`, `tenant_id`, `deleted`),
    KEY `idx_tenant_id` (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='供应商表';

-- 客户表
CREATE TABLE IF NOT EXISTS `erp_customer` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '客户ID',
    `tenant_id` BIGINT NOT NULL DEFAULT 0 COMMENT '租户ID',
    `customer_code` VARCHAR(50) NOT NULL COMMENT '客户编码',
    `customer_name` VARCHAR(200) NOT NULL COMMENT '客户名称',
    `customer_type` TINYINT NOT NULL DEFAULT 1 COMMENT '客户类型: 1-企业, 2-个人',
    `contact_person` VARCHAR(50) DEFAULT NULL COMMENT '联系人',
    `phone` VARCHAR(20) DEFAULT NULL COMMENT '联系电话',
    `email` VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
    `address` VARCHAR(255) DEFAULT NULL COMMENT '地址',
    `bank_name` VARCHAR(100) DEFAULT NULL COMMENT '开户银行',
    `bank_account` VARCHAR(50) DEFAULT NULL COMMENT '银行账号',
    `tax_no` VARCHAR(50) DEFAULT NULL COMMENT '税号',
    `credit_limit` DECIMAL(18,2) DEFAULT 0 COMMENT '信用额度',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 0-停用, 1-启用',
    `settlement_type` TINYINT NOT NULL DEFAULT 1 COMMENT '结算方式: 1-现结, 2-月结, 3-账期',
    `credit_days` INT DEFAULT 30 COMMENT '账期天数',
    `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '软删除',
    `created_by` BIGINT DEFAULT NULL COMMENT '创建人',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_by` BIGINT DEFAULT NULL COMMENT '更新人',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_customer_code_tenant` (`customer_code`, `tenant_id`, `deleted`),
    KEY `idx_tenant_id` (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='客户表';

-- 采购订单表
CREATE TABLE IF NOT EXISTS `erp_purchase_order` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '订单ID',
    `tenant_id` BIGINT NOT NULL DEFAULT 0 COMMENT '租户ID',
    `order_no` VARCHAR(50) NOT NULL COMMENT '订单编号',
    `supplier_id` BIGINT NOT NULL COMMENT '供应商ID',
    `warehouse_id` BIGINT NOT NULL COMMENT '仓库ID',
    `order_date` DATETIME NOT NULL COMMENT '订单日期',
    `expected_date` DATETIME DEFAULT NULL COMMENT '预计到货日期',
    `total_amount` DECIMAL(18,2) DEFAULT 0 COMMENT '订单金额',
    `discount_amount` DECIMAL(18,2) DEFAULT 0 COMMENT '优惠金额',
    `paid_amount` DECIMAL(18,2) DEFAULT 0 COMMENT '实付金额',
    `order_status` TINYINT NOT NULL DEFAULT 0 COMMENT '订单状态: 0-草稿, 1-待审核, 2-已审核, 3-已入库, 4-已完成, 5-已取消',
    `approved_by` BIGINT DEFAULT NULL COMMENT '审核人ID',
    `approved_at` DATETIME DEFAULT NULL COMMENT '审核时间',
    `handler_id` BIGINT DEFAULT NULL COMMENT '经手人ID',
    `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '软删除',
    `created_by` BIGINT DEFAULT NULL COMMENT '创建人',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_by` BIGINT DEFAULT NULL COMMENT '更新人',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_order_no_tenant` (`order_no`, `tenant_id`),
    KEY `idx_tenant_id` (`tenant_id`),
    KEY `idx_supplier_id` (`supplier_id`),
    KEY `idx_order_status` (`order_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='采购订单表';

-- 销售订单表
CREATE TABLE IF NOT EXISTS `erp_sales_order` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '订单ID',
    `tenant_id` BIGINT NOT NULL DEFAULT 0 COMMENT '租户ID',
    `order_no` VARCHAR(50) NOT NULL COMMENT '订单编号',
    `customer_id` BIGINT NOT NULL COMMENT '客户ID',
    `warehouse_id` BIGINT NOT NULL COMMENT '仓库ID',
    `order_date` DATETIME NOT NULL COMMENT '订单日期',
    `expected_date` DATETIME DEFAULT NULL COMMENT '预计发货日期',
    `total_amount` DECIMAL(18,2) DEFAULT 0 COMMENT '订单金额',
    `discount_amount` DECIMAL(18,2) DEFAULT 0 COMMENT '优惠金额',
    `received_amount` DECIMAL(18,2) DEFAULT 0 COMMENT '实收金额',
    `order_status` TINYINT NOT NULL DEFAULT 0 COMMENT '订单状态: 0-草稿, 1-待审核, 2-已审核, 3-已出库, 4-已完成, 5-已取消',
    `approved_by` BIGINT DEFAULT NULL COMMENT '审核人ID',
    `approved_at` DATETIME DEFAULT NULL COMMENT '审核时间',
    `sales_id` BIGINT DEFAULT NULL COMMENT '销售员ID',
    `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '软删除',
    `created_by` BIGINT DEFAULT NULL COMMENT '创建人',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_by` BIGINT DEFAULT NULL COMMENT '更新人',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_order_no_tenant` (`order_no`, `tenant_id`),
    KEY `idx_tenant_id` (`tenant_id`),
    KEY `idx_customer_id` (`customer_id`),
    KEY `idx_order_status` (`order_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='销售订单表';

-- 库存流水表
CREATE TABLE IF NOT EXISTS `erp_inventory_flow` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '流水ID',
    `tenant_id` BIGINT NOT NULL DEFAULT 0 COMMENT '租户ID',
    `warehouse_id` BIGINT NOT NULL COMMENT '仓库ID',
    `product_id` BIGINT NOT NULL COMMENT '商品ID',
    `biz_type` TINYINT NOT NULL COMMENT '业务类型: 1-采购入库, 2-销售出库, 3-调拨入库, 4-调拨出库, 5-盘盈, 6-盘亏, 7-退货入库, 8-退货出库',
    `biz_no` VARCHAR(50) DEFAULT NULL COMMENT '业务单号',
    `biz_id` BIGINT DEFAULT NULL COMMENT '业务ID',
    `quantity` DECIMAL(18,4) NOT NULL COMMENT '变动数量(正数入库,负数出库)',
    `before_quantity` DECIMAL(18,4) NOT NULL COMMENT '变动前数量',
    `after_quantity` DECIMAL(18,4) NOT NULL COMMENT '变动后数量',
    `cost_price` DECIMAL(18,4) DEFAULT NULL COMMENT '成本价',
    `batch_no` VARCHAR(50) DEFAULT NULL COMMENT '批次号',
    `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
    `operator_id` BIGINT DEFAULT NULL COMMENT '操作人ID',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_tenant_id` (`tenant_id`),
    KEY `idx_warehouse_product` (`warehouse_id`, `product_id`),
    KEY `idx_biz_no` (`biz_no`),
    KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='库存流水表';

-- 插入默认仓库
INSERT INTO `erp_warehouse` (`tenant_id`, `warehouse_code`, `warehouse_name`, `warehouse_type`, `status`, `is_default`, `created_at`)
VALUES (1, 'WH001', '主仓库', 1, 1, 1, NOW());
