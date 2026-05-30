-- Initialize databases for My Todo App

-- Nacos database
CREATE DATABASE IF NOT EXISTS nacos DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Auth service database
CREATE DATABASE IF NOT EXISTS my_todo_auth DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- User service database
CREATE DATABASE IF NOT EXISTS my_todo_user DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Permission service database
CREATE DATABASE IF NOT EXISTS my_todo_permission DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Dict service database
CREATE DATABASE IF NOT EXISTS my_todo_dict DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- ERP service database
CREATE DATABASE IF NOT EXISTS my_todo_erp DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Finance service database
CREATE DATABASE IF NOT EXISTS my_todo_finance DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Inventory service database
CREATE DATABASE IF NOT EXISTS my_todo_inventory DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE my_todo_inventory;

-- 库存表
CREATE TABLE IF NOT EXISTS erp_inventory (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id BIGINT NOT NULL DEFAULT 0 COMMENT '租户ID',
    warehouse_id BIGINT NOT NULL COMMENT '仓库ID',
    product_id BIGINT NOT NULL COMMENT '商品ID',
    batch_no VARCHAR(64) DEFAULT NULL COMMENT '批次号',
    quantity DECIMAL(18,4) NOT NULL DEFAULT 0 COMMENT '库存数量',
    locked_quantity DECIMAL(18,4) NOT NULL DEFAULT 0 COMMENT '锁定数量',
    available_quantity DECIMAL(18,4) NOT NULL DEFAULT 0 COMMENT '可用数量',
    stock_min DECIMAL(18,4) DEFAULT NULL COMMENT '最低库存',
    stock_max DECIMAL(18,4) DEFAULT NULL COMMENT '最高库存',
    cost_price DECIMAL(18,4) DEFAULT NULL COMMENT '成本价',
    production_date DATE DEFAULT NULL COMMENT '生产日期',
    expiry_date DATE DEFAULT NULL COMMENT '过期日期',
    status INT NOT NULL DEFAULT 1 COMMENT '状态',
    version INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_tenant_product_warehouse (tenant_id, product_id, warehouse_id, batch_no),
    KEY idx_tenant_id (tenant_id),
    KEY idx_product_id (product_id),
    KEY idx_warehouse_id (warehouse_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='库存表';

-- 库存流水表
CREATE TABLE IF NOT EXISTS erp_inventory_flow (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id BIGINT NOT NULL DEFAULT 0 COMMENT '租户ID',
    warehouse_id BIGINT NOT NULL COMMENT '仓库ID',
    product_id BIGINT NOT NULL COMMENT '商品ID',
    biz_type INT NOT NULL COMMENT '业务类型(1采购入库 2销售出库 3调拨入 4调拨出 5盘盈 6盘亏)',
    biz_no VARCHAR(64) DEFAULT NULL COMMENT '业务单号',
    biz_id BIGINT DEFAULT NULL COMMENT '业务单ID',
    quantity DECIMAL(18,4) NOT NULL COMMENT '变动数量(正=入,负=出)',
    before_quantity DECIMAL(18,4) NOT NULL DEFAULT 0 COMMENT '变动前数量',
    after_quantity DECIMAL(18,4) NOT NULL DEFAULT 0 COMMENT '变动后数量',
    cost_price DECIMAL(18,4) DEFAULT NULL COMMENT '成本价',
    batch_no VARCHAR(64) DEFAULT NULL COMMENT '批次号',
    remark VARCHAR(500) DEFAULT NULL COMMENT '备注',
    operator_id BIGINT DEFAULT NULL COMMENT '操作人ID',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    KEY idx_tenant_id (tenant_id),
    KEY idx_product_id (product_id),
    KEY idx_warehouse_id (warehouse_id),
    KEY idx_biz_type (biz_type),
    KEY idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='库存流水表';

-- 库存盘点单表
CREATE TABLE IF NOT EXISTS erp_inventory_check (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id BIGINT NOT NULL DEFAULT 0 COMMENT '租户ID',
    check_no VARCHAR(64) NOT NULL COMMENT '盘点单号',
    warehouse_id BIGINT NOT NULL COMMENT '仓库ID',
    check_date DATE NOT NULL COMMENT '盘点日期',
    check_type INT NOT NULL DEFAULT 1 COMMENT '盘点类型(1全盘 2抽盘)',
    check_status INT NOT NULL DEFAULT 0 COMMENT '盘点状态(0草稿 1进行中 2已完成 3已取消)',
    total_profit_qty DECIMAL(18,4) DEFAULT 0 COMMENT '盘盈数量合计',
    total_loss_qty DECIMAL(18,4) DEFAULT 0 COMMENT '盘亏数量合计',
    remark VARCHAR(500) DEFAULT NULL COMMENT '备注',
    deleted INT NOT NULL DEFAULT 0 COMMENT '软删除标记',
    created_by BIGINT DEFAULT NULL COMMENT '创建人',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_by BIGINT DEFAULT NULL COMMENT '更新人',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    KEY idx_tenant_id (tenant_id),
    KEY idx_warehouse_id (warehouse_id),
    KEY idx_check_no (check_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='库存盘点单表';

-- 库存盘点明细表
CREATE TABLE IF NOT EXISTS erp_inventory_check_item (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id BIGINT NOT NULL DEFAULT 0 COMMENT '租户ID',
    check_id BIGINT NOT NULL COMMENT '盘点单ID',
    product_id BIGINT NOT NULL COMMENT '商品ID',
    product_code VARCHAR(64) DEFAULT NULL COMMENT '商品编码',
    product_name VARCHAR(128) DEFAULT NULL COMMENT '商品名称',
    system_quantity DECIMAL(18,4) NOT NULL DEFAULT 0 COMMENT '系统数量',
    actual_quantity DECIMAL(18,4) NOT NULL DEFAULT 0 COMMENT '实盘数量',
    diff_quantity DECIMAL(18,4) NOT NULL DEFAULT 0 COMMENT '差异数量',
    remark VARCHAR(500) DEFAULT NULL COMMENT '备注',
    deleted INT NOT NULL DEFAULT 0 COMMENT '软删除标记',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    KEY idx_tenant_id (tenant_id),
    KEY idx_check_id (check_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='库存盘点明细表';

USE my_todo_auth;

-- 验证码表
CREATE TABLE IF NOT EXISTS sys_captcha (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    captcha_key VARCHAR(128) NOT NULL UNIQUE COMMENT '验证码Key',
    code_hash VARCHAR(128) NOT NULL COMMENT 'SHA-256哈希',
    user_id BIGINT DEFAULT NULL COMMENT '用户ID(可选)',
    expiry_time DATETIME NOT NULL COMMENT '过期时间',
    used INT NOT NULL DEFAULT 0 COMMENT '是否已使用',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Grant privileges
GRANT ALL PRIVILEGES ON nacos.* TO 'root'@'%';
GRANT ALL PRIVILEGES ON my_todo_auth.* TO 'root'@'%';
GRANT ALL PRIVILEGES ON my_todo_user.* TO 'root'@'%';
GRANT ALL PRIVILEGES ON my_todo_permission.* TO 'root'@'%';
GRANT ALL PRIVILEGES ON my_todo_dict.* TO 'root'@'%';
GRANT ALL PRIVILEGES ON my_todo_erp.* TO 'root'@'%';
GRANT ALL PRIVILEGES ON my_todo_finance.* TO 'root'@'%';
GRANT ALL PRIVILEGES ON my_todo_inventory.* TO 'root'@'%';

FLUSH PRIVILEGES;
