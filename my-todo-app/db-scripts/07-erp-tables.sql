-- ERP服务数据库表结构
-- Version: 1.0.0

USE `my-todo-app-dev`;

-- 商品分类表
CREATE TABLE IF NOT EXISTS `erp_product_category` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '分类ID',
    `tenant_id` BIGINT NOT NULL DEFAULT 0 COMMENT '租户ID',
    `category_code` VARCHAR(50) NOT NULL COMMENT '分类编码',
    `category_name` VARCHAR(100) NOT NULL COMMENT '分类名称',
    `parent_id` BIGINT NOT NULL DEFAULT 0 COMMENT '父分类ID，顶级分类为0',
    `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序号',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 0-停用, 1-启用',
    `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '软删除',
    `created_by` BIGINT DEFAULT NULL COMMENT '创建人',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_by` BIGINT DEFAULT NULL COMMENT '更新人',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_category_code_tenant` (`category_code`, `tenant_id`, `deleted`),
    KEY `idx_tenant_id` (`tenant_id`),
    KEY `idx_parent_id` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品分类表';

-- 商品表
CREATE TABLE IF NOT EXISTS `erp_product` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '商品ID',
    `tenant_id` BIGINT NOT NULL DEFAULT 0 COMMENT '租户ID',
    `product_code` VARCHAR(50) NOT NULL COMMENT '商品编码',
    `product_name` VARCHAR(200) NOT NULL COMMENT '商品名称',
    `brand` VARCHAR(100) DEFAULT NULL COMMENT '品牌',
    `model` VARCHAR(100) DEFAULT NULL COMMENT '型号',
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
    `stock_min` DECIMAL(18,4) DEFAULT 0 COMMENT '库存下限',
    `stock_max` DECIMAL(18,4) DEFAULT 99999999 COMMENT '库存上限',
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

-- 采购订单明细表
CREATE TABLE IF NOT EXISTS `erp_purchase_order_item` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '明细ID',
    `tenant_id` BIGINT NOT NULL DEFAULT 0 COMMENT '租户ID',
    `order_id` BIGINT NOT NULL COMMENT '订单ID',
    `product_id` BIGINT NOT NULL COMMENT '商品ID',
    `product_code` VARCHAR(50) NOT NULL COMMENT '商品编码',
    `product_name` VARCHAR(200) NOT NULL COMMENT '商品名称',
    `specification` VARCHAR(100) DEFAULT NULL COMMENT '规格型号',
    `unit` VARCHAR(20) DEFAULT NULL COMMENT '单位',
    `quantity` DECIMAL(18,4) NOT NULL COMMENT '订单数量',
    `price` DECIMAL(18,4) NOT NULL COMMENT '单价',
    `discount_amount` DECIMAL(18,2) DEFAULT 0 COMMENT '行折扣金额',
    `amount` DECIMAL(18,2) NOT NULL COMMENT '行金额',
    `received_quantity` DECIMAL(18,4) DEFAULT 0 COMMENT '已入库数量',
    `received_amount` DECIMAL(18,2) DEFAULT 0 COMMENT '已入库金额',
    `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '软删除',
    `created_by` BIGINT DEFAULT NULL COMMENT '创建人',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_by` BIGINT DEFAULT NULL COMMENT '更新人',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_tenant_id` (`tenant_id`),
    KEY `idx_order_id` (`order_id`),
    KEY `idx_product_id` (`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='采购订单明细表';

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
    `delivered_amount` DECIMAL(18,2) DEFAULT 0 COMMENT '已发货金额',
    `delivered_quantity` DECIMAL(18,4) DEFAULT 0 COMMENT '已发货数量(汇总)',
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

-- 销售订单明细表
CREATE TABLE IF NOT EXISTS `erp_sales_order_item` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '明细ID',
    `tenant_id` BIGINT NOT NULL DEFAULT 0 COMMENT '租户ID',
    `order_id` BIGINT NOT NULL COMMENT '订单ID',
    `product_id` BIGINT NOT NULL COMMENT '商品ID',
    `product_code` VARCHAR(50) NOT NULL COMMENT '商品编码',
    `product_name` VARCHAR(200) NOT NULL COMMENT '商品名称',
    `specification` VARCHAR(100) DEFAULT NULL COMMENT '规格型号',
    `unit` VARCHAR(20) DEFAULT NULL COMMENT '单位',
    `quantity` DECIMAL(18,4) NOT NULL COMMENT '订单数量',
    `price` DECIMAL(18,4) NOT NULL COMMENT '单价',
    `discount_amount` DECIMAL(18,2) DEFAULT 0 COMMENT '行折扣金额',
    `amount` DECIMAL(18,2) NOT NULL COMMENT '行金额',
    `delivered_quantity` DECIMAL(18,4) DEFAULT 0 COMMENT '已发货数量',
    `delivered_amount` DECIMAL(18,2) DEFAULT 0 COMMENT '已发货金额',
    `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '软删除',
    `created_by` BIGINT DEFAULT NULL COMMENT '创建人',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_by` BIGINT DEFAULT NULL COMMENT '更新人',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_tenant_id` (`tenant_id`),
    KEY `idx_order_id` (`order_id`),
    KEY `idx_product_id` (`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='销售订单明细表';

-- 销售出库单表
CREATE TABLE IF NOT EXISTS `erp_sales_shipment` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '出库单ID',
    `tenant_id` BIGINT NOT NULL DEFAULT 0 COMMENT '租户ID',
    `shipment_no` VARCHAR(50) NOT NULL COMMENT '出库单号',
    `order_id` BIGINT NOT NULL COMMENT '订单ID',
    `order_no` VARCHAR(50) NOT NULL COMMENT '订单编号',
    `customer_id` BIGINT NOT NULL COMMENT '客户ID',
    `warehouse_id` BIGINT NOT NULL COMMENT '仓库ID',
    `shipment_date` DATETIME NOT NULL COMMENT '出库日期',
    `total_amount` DECIMAL(18,2) DEFAULT 0 COMMENT '出库总金额',
    `discount_amount` DECIMAL(18,2) DEFAULT 0 COMMENT '优惠金额',
    `received_amount` DECIMAL(18,2) DEFAULT 0 COMMENT '实收金额',
    `shipment_status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态: 0-草稿, 1-待审核, 2-已出库, 3-已取消',
    `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '软删除',
    `created_by` BIGINT DEFAULT NULL COMMENT '创建人',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_by` BIGINT DEFAULT NULL COMMENT '更新人',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_shipment_no_tenant` (`shipment_no`, `tenant_id`),
    KEY `idx_tenant_id` (`tenant_id`),
    KEY `idx_order_id` (`order_id`),
    KEY `idx_customer_id` (`customer_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='销售出库单表';

-- 库存盘点单表
CREATE TABLE IF NOT EXISTS `erp_inventory_check` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '盘点单ID',
    `tenant_id` BIGINT NOT NULL DEFAULT 0 COMMENT '租户ID',
    `check_no` VARCHAR(50) NOT NULL COMMENT '盘点单号',
    `warehouse_id` BIGINT NOT NULL COMMENT '仓库ID',
    `check_date` DATETIME NOT NULL COMMENT '盘点日期',
    `check_type` TINYINT NOT NULL DEFAULT 1 COMMENT '盘点类型: 1-全盘, 2-抽盘',
    `check_status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态: 0-草稿, 1-盘点中, 2-已完成, 3-已取消',
    `total_profit_qty` DECIMAL(18,4) DEFAULT 0 COMMENT '盘盈数量合计',
    `total_loss_qty` DECIMAL(18,4) DEFAULT 0 COMMENT '盘亏数量合计',
    `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '软删除',
    `created_by` BIGINT DEFAULT NULL COMMENT '创建人',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_by` BIGINT DEFAULT NULL COMMENT '更新人',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_check_no_tenant` (`check_no`, `tenant_id`),
    KEY `idx_tenant_id` (`tenant_id`),
    KEY `idx_warehouse_id` (`warehouse_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='库存盘点单表';

-- 库存盘点明细表
CREATE TABLE IF NOT EXISTS `erp_inventory_check_item` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '明细ID',
    `tenant_id` BIGINT NOT NULL DEFAULT 0 COMMENT '租户ID',
    `check_id` BIGINT NOT NULL COMMENT '盘点单ID',
    `product_id` BIGINT NOT NULL COMMENT '商品ID',
    `product_code` VARCHAR(50) NOT NULL COMMENT '商品编码',
    `product_name` VARCHAR(200) NOT NULL COMMENT '商品名称',
    `system_quantity` DECIMAL(18,4) NOT NULL DEFAULT 0 COMMENT '系统数量',
    `actual_quantity` DECIMAL(18,4) DEFAULT NULL COMMENT '实盘数量',
    `diff_quantity` DECIMAL(18,4) DEFAULT 0 COMMENT '差异数量',
    `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '软删除',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_check_id` (`check_id`),
    KEY `idx_product_id` (`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='库存盘点明细表';

-- 采购退货单表
CREATE TABLE IF NOT EXISTS `erp_purchase_return` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '退货单ID',
    `tenant_id` BIGINT NOT NULL DEFAULT 0 COMMENT '租户ID',
    `return_no` VARCHAR(50) NOT NULL COMMENT '退货单号',
    `order_id` BIGINT DEFAULT NULL COMMENT '原采购订单ID',
    `order_no` VARCHAR(50) DEFAULT NULL COMMENT '原订单编号',
    `supplier_id` BIGINT NOT NULL COMMENT '供应商ID',
    `warehouse_id` BIGINT NOT NULL COMMENT '仓库ID',
    `return_date` DATETIME NOT NULL COMMENT '退货日期',
    `total_amount` DECIMAL(18,2) DEFAULT 0 COMMENT '退货金额',
    `return_status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态: 0-草稿, 1-待审核, 2-已审核, 3-已取消',
    `reason` VARCHAR(500) DEFAULT NULL COMMENT '退货原因',
    `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '软删除',
    `created_by` BIGINT DEFAULT NULL COMMENT '创建人',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_by` BIGINT DEFAULT NULL COMMENT '更新人',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_return_no_tenant` (`return_no`, `tenant_id`),
    KEY `idx_tenant_id` (`tenant_id`),
    KEY `idx_order_id` (`order_id`),
    KEY `idx_supplier_id` (`supplier_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='采购退货单表';

-- 采购退货明细表
CREATE TABLE IF NOT EXISTS `erp_purchase_return_item` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '明细ID',
    `tenant_id` BIGINT NOT NULL DEFAULT 0 COMMENT '租户ID',
    `return_id` BIGINT NOT NULL COMMENT '退货单ID',
    `product_id` BIGINT NOT NULL COMMENT '商品ID',
    `product_code` VARCHAR(50) NOT NULL COMMENT '商品编码',
    `product_name` VARCHAR(200) NOT NULL COMMENT '商品名称',
    `quantity` DECIMAL(18,4) NOT NULL COMMENT '退货数量',
    `price` DECIMAL(18,4) NOT NULL COMMENT '单价',
    `amount` DECIMAL(18,2) NOT NULL COMMENT '金额',
    `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '软删除',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_return_id` (`return_id`),
    KEY `idx_product_id` (`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='采购退货明细表';

-- 销售退货单表
CREATE TABLE IF NOT EXISTS `erp_sales_return` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '退货单ID',
    `tenant_id` BIGINT NOT NULL DEFAULT 0 COMMENT '租户ID',
    `return_no` VARCHAR(50) NOT NULL COMMENT '退货单号',
    `order_id` BIGINT DEFAULT NULL COMMENT '原销售订单ID',
    `order_no` VARCHAR(50) DEFAULT NULL COMMENT '原订单编号',
    `customer_id` BIGINT NOT NULL COMMENT '客户ID',
    `warehouse_id` BIGINT NOT NULL COMMENT '仓库ID',
    `return_date` DATETIME NOT NULL COMMENT '退货日期',
    `total_amount` DECIMAL(18,2) DEFAULT 0 COMMENT '退货金额',
    `return_status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态: 0-草稿, 1-待审核, 2-已审核, 3-已取消',
    `reason` VARCHAR(500) DEFAULT NULL COMMENT '退货原因',
    `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '软删除',
    `created_by` BIGINT DEFAULT NULL COMMENT '创建人',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_by` BIGINT DEFAULT NULL COMMENT '更新人',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_return_no_tenant` (`return_no`, `tenant_id`),
    KEY `idx_tenant_id` (`tenant_id`),
    KEY `idx_order_id` (`order_id`),
    KEY `idx_customer_id` (`customer_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='销售退货单表';

-- 销售退货明细表
CREATE TABLE IF NOT EXISTS `erp_sales_return_item` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '明细ID',
    `tenant_id` BIGINT NOT NULL DEFAULT 0 COMMENT '租户ID',
    `return_id` BIGINT NOT NULL COMMENT '退货单ID',
    `product_id` BIGINT NOT NULL COMMENT '商品ID',
    `product_code` VARCHAR(50) NOT NULL COMMENT '商品编码',
    `product_name` VARCHAR(200) NOT NULL COMMENT '商品名称',
    `quantity` DECIMAL(18,4) NOT NULL COMMENT '退货数量',
    `price` DECIMAL(18,4) NOT NULL COMMENT '单价',
    `amount` DECIMAL(18,2) NOT NULL COMMENT '金额',
    `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '软删除',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_return_id` (`return_id`),
    KEY `idx_product_id` (`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='销售退货明细表';

-- 系统配置表
CREATE TABLE IF NOT EXISTS `erp_config` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '配置ID',
    `tenant_id` BIGINT NOT NULL DEFAULT 0 COMMENT '租户ID',
    `config_key` VARCHAR(100) NOT NULL COMMENT '配置键',
    `config_value` VARCHAR(500) DEFAULT NULL COMMENT '配置值',
    `config_name` VARCHAR(100) NOT NULL COMMENT '配置名称',
    `config_type` VARCHAR(50) NOT NULL COMMENT '配置类型: APPROVAL-审批规则, NUMBER-编号规则, BIZ-业务参数',
    `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '软删除',
    `created_by` BIGINT DEFAULT NULL COMMENT '创建人',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_by` BIGINT DEFAULT NULL COMMENT '更新人',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_config_key_tenant` (`config_key`, `tenant_id`, `deleted`),
    KEY `idx_tenant_id` (`tenant_id`),
    KEY `idx_config_type` (`config_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统配置表';

-- 商品价格表
CREATE TABLE IF NOT EXISTS `erp_product_price` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '价格ID',
    `tenant_id` BIGINT NOT NULL DEFAULT 0 COMMENT '租户ID',
    `product_id` BIGINT NOT NULL COMMENT '商品ID',
    `price_type` TINYINT NOT NULL COMMENT '价格类型: 1-成本价, 2-销售价, 3-批发价, 4-会员价, 5-促销价',
    `price` DECIMAL(18,4) NOT NULL COMMENT '价格',
    `min_quantity` DECIMAL(18,4) DEFAULT 1 COMMENT '最小数量（阶梯价）',
    `start_date` DATETIME DEFAULT NULL COMMENT '生效日期',
    `end_date` DATETIME DEFAULT NULL COMMENT '失效日期',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 0-停用, 1-启用',
    `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '软删除',
    `created_by` BIGINT DEFAULT NULL COMMENT '创建人',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_by` BIGINT DEFAULT NULL COMMENT '更新人',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_tenant_id` (`tenant_id`),
    KEY `idx_product_id` (`product_id`),
    KEY `idx_price_type` (`price_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品价格表';

-- 商品促销活动表
CREATE TABLE IF NOT EXISTS `erp_product_promotion` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '促销ID',
    `tenant_id` BIGINT NOT NULL DEFAULT 0 COMMENT '租户ID',
    `promotion_name` VARCHAR(200) NOT NULL COMMENT '促销名称',
    `promotion_type` TINYINT NOT NULL COMMENT '促销类型: 1-限时折扣, 2-满减, 3-买赠',
    `product_id` BIGINT DEFAULT NULL COMMENT '关联商品ID（NULL表示全场活动）',
    `discount_rate` DECIMAL(5,2) DEFAULT NULL COMMENT '折扣率（限时折扣用，如0.80表示8折）',
    `min_amount` DECIMAL(18,2) DEFAULT NULL COMMENT '满减门槛金额',
    `reduce_amount` DECIMAL(18,2) DEFAULT NULL COMMENT '满减优惠金额',
    `gift_product_id` BIGINT DEFAULT NULL COMMENT '赠品商品ID（买赠用）',
    `gift_quantity` INT DEFAULT NULL COMMENT '赠品数量（买赠用）',
    `start_date` DATETIME NOT NULL COMMENT '促销开始时间',
    `end_date` DATETIME NOT NULL COMMENT '促销结束时间',
    `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态: 0-未开始, 1-进行中, 2-已结束, 3-已停用',
    `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '软删除',
    `created_by` BIGINT DEFAULT NULL COMMENT '创建人',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_by` BIGINT DEFAULT NULL COMMENT '更新人',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_tenant_id` (`tenant_id`),
    KEY `idx_product_id` (`product_id`),
    KEY `idx_promotion_type` (`promotion_type`),
    KEY `idx_status` (`status`),
    KEY `idx_start_end_date` (`start_date`, `end_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品促销活动表';

-- 销售出库单明细表
CREATE TABLE IF NOT EXISTS `erp_sales_shipment_item` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '明细ID',
    `tenant_id` BIGINT NOT NULL DEFAULT 0 COMMENT '租户ID',
    `shipment_id` BIGINT NOT NULL COMMENT '出库单ID',
    `order_item_id` BIGINT NOT NULL COMMENT '订单明细ID',
    `product_id` BIGINT NOT NULL COMMENT '商品ID',
    `product_code` VARCHAR(50) NOT NULL COMMENT '商品编码',
    `product_name` VARCHAR(200) NOT NULL COMMENT '商品名称',
    `specification` VARCHAR(100) DEFAULT NULL COMMENT '规格型号',
    `unit` VARCHAR(20) DEFAULT NULL COMMENT '单位',
    `quantity` DECIMAL(18,4) NOT NULL COMMENT '出库数量',
    `price` DECIMAL(18,4) NOT NULL COMMENT '单价',
    `discount_amount` DECIMAL(18,2) DEFAULT 0 COMMENT '折扣金额',
    `amount` DECIMAL(18,2) NOT NULL COMMENT '金额',
    `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '软删除',
    `created_by` BIGINT DEFAULT NULL COMMENT '创建人',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_tenant_id` (`tenant_id`),
    KEY `idx_shipment_id` (`shipment_id`),
    KEY `idx_order_item_id` (`order_item_id`),
    KEY `idx_product_id` (`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='销售出库单明细表';
