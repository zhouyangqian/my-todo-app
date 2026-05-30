-- =====================================================
-- 销售报价单模块数据库表结构
-- =====================================================

-- 销售报价单主表
CREATE TABLE IF NOT EXISTS `erp_sales_quotation` (
    `id`              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '报价单ID（主键，自增）',
    `tenant_id`       BIGINT       NOT NULL                COMMENT '租户ID，用于多租户数据隔离',
    `quotation_no`    VARCHAR(32)  NOT NULL                COMMENT '报价单编号，同一租户下唯一',
    `customer_id`     BIGINT       NOT NULL                COMMENT '客户ID，关联 erp_customer 表',
    `customer_name`   VARCHAR(100) DEFAULT NULL             COMMENT '客户名称，冗余存储便于查询显示',
    `quotation_date`  DATE         DEFAULT NULL             COMMENT '报价日期',
    `valid_until`     DATE         DEFAULT NULL             COMMENT '有效期至',
    `total_amount`    DECIMAL(16,2) DEFAULT 0.00            COMMENT '总金额',
    `status`          INT          NOT NULL DEFAULT 0       COMMENT '状态：0-草稿, 1-已发送, 2-已接受, 3-已拒绝, 4-已过期, 5-已转订单',
    `converted_order_id` BIGINT    DEFAULT NULL             COMMENT '转订单ID，关联 erp_sales_order 表',
    `remark`          VARCHAR(500) DEFAULT NULL             COMMENT '备注',
    `deleted`         INT          NOT NULL DEFAULT 0       COMMENT '软删除标记：0-未删除, 1-已删除',
    `created_by`      BIGINT       DEFAULT NULL             COMMENT '创建人ID',
    `created_at`      DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_by`      BIGINT       DEFAULT NULL             COMMENT '更新人ID',
    `updated_at`      DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_quotation_no_tenant` (`quotation_no`, `tenant_id`),
    KEY `idx_tenant_id` (`tenant_id`),
    KEY `idx_customer_id` (`customer_id`),
    KEY `idx_status` (`status`),
    KEY `idx_quotation_date` (`quotation_date`),
    KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='销售报价单主表';

-- 销售报价单明细表
CREATE TABLE IF NOT EXISTS `erp_sales_quotation_item` (
    `id`              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '明细ID（主键，自增）',
    `tenant_id`       BIGINT       NOT NULL                COMMENT '租户ID，用于多租户数据隔离',
    `quotation_id`    BIGINT       NOT NULL                COMMENT '报价单ID，关联 erp_sales_quotation 表',
    `product_id`      BIGINT       NOT NULL                COMMENT '商品ID，关联 erp_product 表',
    `product_name`    VARCHAR(100) DEFAULT NULL             COMMENT '商品名称',
    `product_code`    VARCHAR(50)  DEFAULT NULL             COMMENT '商品编码',
    `quantity`        DECIMAL(16,4) NOT NULL                COMMENT '报价数量',
    `unit_price`      DECIMAL(16,2) NOT NULL                COMMENT '单价',
    `discount_rate`   DECIMAL(5,2)  DEFAULT 100.00          COMMENT '折扣率（百分比，默认100表示无折扣）',
    `amount`          DECIMAL(16,2) DEFAULT 0.00            COMMENT '行金额 = 数量 * 单价 * 折扣率 / 100',
    `remark`          VARCHAR(500) DEFAULT NULL             COMMENT '备注',
    `deleted`         INT          NOT NULL DEFAULT 0       COMMENT '软删除标记：0-未删除, 1-已删除',
    `created_by`      BIGINT       DEFAULT NULL             COMMENT '创建人ID',
    `created_at`      DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_by`      BIGINT       DEFAULT NULL             COMMENT '更新人ID',
    `updated_at`      DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_tenant_id` (`tenant_id`),
    KEY `idx_quotation_id` (`quotation_id`),
    KEY `idx_product_id` (`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='销售报价单明细表';
