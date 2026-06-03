-- ============================================================
-- 综合补丁脚本: 一键执行所有修复
-- 包含:
--   patch-01: Gateway 3 张缺失表
--   patch-02: 90 张表缺失的表级 COMMENT
--   patch-03: 销售报价单权限 + 角色关联修复
--
-- 执行方式: mysql -u root -p < sql/patch-all.sql
-- ============================================================

USE `my-todo-app-dev`;

-- ================================================================
-- Part 1: Gateway 3 张缺失表
-- ================================================================

CREATE TABLE IF NOT EXISTS `gateway_route` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `tenant_id` BIGINT NOT NULL DEFAULT 0 COMMENT '租户ID',
    `route_id` VARCHAR(100) NOT NULL COMMENT '路由ID',
    `uri` VARCHAR(255) NOT NULL COMMENT '目标URI',
    `predicates` TEXT COMMENT '断言配置(JSON)',
    `filters` TEXT COMMENT '过滤器配置(JSON)',
    `order_num` INT DEFAULT 0 COMMENT '排序',
    `enabled` TINYINT NOT NULL DEFAULT 1 COMMENT '0=禁用 1=启用',
    `remark` VARCHAR(500) COMMENT '备注',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '软删除',
    `created_by` BIGINT COMMENT '创建人',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_by` BIGINT COMMENT '更新人',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_route_id` (`route_id`),
    KEY `idx_tenant_id` (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='网关路由配置表';

CREATE TABLE IF NOT EXISTS `gateway_rate_limit` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `tenant_id` BIGINT NOT NULL DEFAULT 0 COMMENT '租户ID',
    `route_id` VARCHAR(100) NOT NULL COMMENT '路由ID',
    `limit_dimension` VARCHAR(20) NOT NULL DEFAULT 'IP' COMMENT '限流维度(IP/USER/TENANT)',
    `max_requests` INT NOT NULL DEFAULT 100 COMMENT '最大请求数',
    `window_seconds` INT NOT NULL DEFAULT 60 COMMENT '时间窗口(秒)',
    `enabled` TINYINT NOT NULL DEFAULT 1 COMMENT '0=禁用 1=启用',
    `remark` VARCHAR(500) COMMENT '备注',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '软删除',
    `created_by` BIGINT COMMENT '创建人',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_by` BIGINT COMMENT '更新人',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_tenant_route` (`tenant_id`, `route_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='网关限流配置表';

CREATE TABLE IF NOT EXISTS `gateway_circuit_breaker` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `tenant_id` BIGINT NOT NULL DEFAULT 0 COMMENT '租户ID',
    `route_id` VARCHAR(100) NOT NULL COMMENT '路由ID',
    `failure_threshold` INT NOT NULL DEFAULT 5 COMMENT '失败阈值',
    `cooldown_seconds` INT NOT NULL DEFAULT 60 COMMENT '冷却时间(秒)',
    `half_open_max` INT DEFAULT 3 COMMENT '半开状态最大请求数',
    `state` VARCHAR(20) NOT NULL DEFAULT 'CLOSED' COMMENT '状态(CLOSED/OPEN/HALF_OPEN)',
    `enabled` TINYINT NOT NULL DEFAULT 1 COMMENT '0=禁用 1=启用',
    `remark` VARCHAR(500) COMMENT '备注',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '软删除',
    `created_by` BIGINT COMMENT '创建人',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_by` BIGINT COMMENT '更新人',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_tenant_route` (`tenant_id`, `route_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='网关熔断配置表';

-- ================================================================
-- Part 2: 90 张表缺失的表级 COMMENT
-- ================================================================

-- Auth 模块
ALTER TABLE `login_session`       COMMENT '登录会话表';
ALTER TABLE `refresh_token`       COMMENT '刷新令牌表';
ALTER TABLE `login_log`           COMMENT '登录日志表';
ALTER TABLE `captcha`             COMMENT '验证码表';
ALTER TABLE `password_history`    COMMENT '密码历史表';
ALTER TABLE `social_account`      COMMENT '社交账号绑定表';

-- User 模块
ALTER TABLE `sys_user`            COMMENT '用户表';
ALTER TABLE `sys_user_profile`    COMMENT '用户详情表';
ALTER TABLE `sys_user_address`    COMMENT '用户地址表';
ALTER TABLE `sys_user_role`       COMMENT '用户角色关联表';

-- Permission 模块
ALTER TABLE `sys_role`            COMMENT '角色表';
ALTER TABLE `sys_permission`      COMMENT '权限表';
ALTER TABLE `sys_role_permission` COMMENT '角色权限关联表';
ALTER TABLE `sys_department`      COMMENT '部门表';
ALTER TABLE `sys_menu`            COMMENT '菜单管理表';
ALTER TABLE `sys_role_menu`       COMMENT '角色菜单关联表';

-- Dict 模块
ALTER TABLE `sys_dict_type`       COMMENT '字典类型表';
ALTER TABLE `sys_dict_item`       COMMENT '字典项表';
ALTER TABLE `sys_config`          COMMENT '系统配置表';

-- 参数字典
ALTER TABLE `sys_parameter_category`   COMMENT '参数分类表';
ALTER TABLE `sys_parameter_dictionary` COMMENT '参数字典表';
ALTER TABLE `sys_parameter_item`       COMMENT '参数项表';

-- API 市场
ALTER TABLE `api_definition`      COMMENT 'API定义表';
ALTER TABLE `api_subscription`    COMMENT 'API订阅表';
ALTER TABLE `api_usage_record`    COMMENT 'API使用记录表';

-- 第三方API
ALTER TABLE `third_party_api`     COMMENT '第三方API配置表';
ALTER TABLE `third_party_call_log` COMMENT '第三方API调用日志表';

-- 链路追踪
ALTER TABLE `trace_config`        COMMENT '链路追踪配置表';
ALTER TABLE `trace_alert`         COMMENT '链路追踪告警表';

-- 代码生成
ALTER TABLE `code_template`       COMMENT '代码模板表';
ALTER TABLE `gen_history`         COMMENT '代码生成历史表';

-- 错误文档
ALTER TABLE `error_category`      COMMENT '错误分类表';
ALTER TABLE `error_solution`      COMMENT '错误解决方案表';

-- SaaS 套餐
ALTER TABLE `saas_package`        COMMENT 'SaaS套餐表';
ALTER TABLE `tenant_subscription` COMMENT '租户订阅表';
ALTER TABLE `marketing_activity`  COMMENT '营销活动表';
ALTER TABLE `activity_participation` COMMENT '活动参与记录表';

-- Auth 扩展
ALTER TABLE `sys_tenant`          COMMENT '租户信息表';
ALTER TABLE `sys_token_blacklist` COMMENT 'Token黑名单表';
ALTER TABLE `sys_captcha`         COMMENT '验证码表(新)';
ALTER TABLE `sys_tenant_quota`    COMMENT '租户配额表';
ALTER TABLE `sys_tenant_resource_usage` COMMENT '租户资源使用表';

-- Permission 扩展
ALTER TABLE `sys_user_permission`     COMMENT '用户权限关联表';
ALTER TABLE `sys_audit_log`           COMMENT '审计日志表';
ALTER TABLE `sys_permission_log`      COMMENT '权限审计日志表';
ALTER TABLE `sys_session`             COMMENT '会话管理表';
ALTER TABLE `sys_blacklist`           COMMENT '黑名单管理表';
ALTER TABLE `sys_permission_template` COMMENT '权限模板表';
ALTER TABLE `sys_data_permission_rule` COMMENT '数据权限规则表';
ALTER TABLE `sys_role_inheritance`    COMMENT '角色继承关系表';

-- ERP 模块
ALTER TABLE `erp_product_category`     COMMENT '商品分类表';
ALTER TABLE `erp_product`              COMMENT '商品表';
ALTER TABLE `erp_warehouse`            COMMENT '仓库表';
ALTER TABLE `erp_inventory`            COMMENT '库存表';
ALTER TABLE `erp_supplier`             COMMENT '供应商表';
ALTER TABLE `erp_customer`             COMMENT '客户表';
ALTER TABLE `erp_purchase_order`       COMMENT '采购订单表';
ALTER TABLE `erp_purchase_order_item`  COMMENT '采购订单明细表';
ALTER TABLE `erp_purchase_return`      COMMENT '采购退货表';
ALTER TABLE `erp_purchase_return_item` COMMENT '采购退货明细表';
ALTER TABLE `erp_sales_order`          COMMENT '销售订单表';
ALTER TABLE `erp_sales_order_item`     COMMENT '销售订单明细表';
ALTER TABLE `erp_sales_quotation`      COMMENT '销售报价单主表';
ALTER TABLE `erp_sales_quotation_item` COMMENT '销售报价单明细表';
ALTER TABLE `erp_sales_return`         COMMENT '销售退货表';
ALTER TABLE `erp_sales_return_item`    COMMENT '销售退货明细表';
ALTER TABLE `erp_sales_shipment`       COMMENT '销售出库单表';
ALTER TABLE `erp_sales_shipment_item`  COMMENT '销售出库单明细表';
ALTER TABLE `erp_inventory_flow`       COMMENT '库存流水表';
ALTER TABLE `erp_inventory_check`      COMMENT '库存盘点表';
ALTER TABLE `erp_inventory_check_item` COMMENT '库存盘点明细表';
ALTER TABLE `erp_product_price`        COMMENT '商品价格表';
ALTER TABLE `erp_product_promotion`    COMMENT '商品促销表';
ALTER TABLE `erp_config`               COMMENT 'ERP系统配置表';
ALTER TABLE `supplier_contact`         COMMENT '供应商联系人表';
ALTER TABLE `customer_contact`         COMMENT '客户联系人表';

-- Finance 模块
ALTER TABLE `fin_bank_account`       COMMENT '银行账户表';
ALTER TABLE `fin_account_receivable` COMMENT '应收账款表';
ALTER TABLE `fin_account_payable`    COMMENT '应付账款表';
ALTER TABLE `fin_payment_record`     COMMENT '收支记录表';
ALTER TABLE `fin_invoice`            COMMENT '发票表';
ALTER TABLE `fin_cost_config`        COMMENT '成本核算配置表';
ALTER TABLE `fin_cost_history`       COMMENT '成本核算历史表';
ALTER TABLE `fin_profit_calc`        COMMENT '毛利计算表';
ALTER TABLE `fin_report`             COMMENT '财务报表表';
ALTER TABLE `fin_bank_record`        COMMENT '银行对账单记录表';
ALTER TABLE `fin_bank_reconciliation` COMMENT '银行对账记录表';
ALTER TABLE `fin_bill`               COMMENT '统一账单表';
ALTER TABLE `fin_bill_invoice`       COMMENT '账单-发票关联表';
ALTER TABLE `fin_budget`             COMMENT '预算管理表';

-- Gateway 模块
ALTER TABLE `gateway_route`           COMMENT '网关路由配置表';
ALTER TABLE `gateway_rate_limit`      COMMENT '网关限流配置表';
ALTER TABLE `gateway_circuit_breaker` COMMENT '网关熔断配置表';

-- ================================================================
-- Part 3: 销售报价单权限 + 角色关联修复
-- ================================================================

-- 3a. 销售报价单菜单
INSERT IGNORE INTO `sys_permission` (`tenant_id`, `parent_id`, `permission_code`, `permission_name`, `permission_type`, `resource_path`, `http_method`, `icon`, `menu_path`, `component`, `sort`, `status`, `visible`, `created_at`)
SELECT 1, id, 'erp:salesQuotation', '销售报价', 1,
       '/api/erp/sales-quotations/get-sales-quotation-page', 'GET',
       'Document', '/erp/sales-quotation', 'erp/sales-quotation/index',
       7, 1, 1, NOW()
FROM `sys_permission` WHERE `tenant_id` = 1 AND `permission_code` = 'erp' LIMIT 1;

-- 3b. 销售报价单按钮
SET @sq_menu = (SELECT id FROM `sys_permission` WHERE `tenant_id` = 1 AND `permission_code` = 'erp:salesQuotation' LIMIT 1);

INSERT IGNORE INTO `sys_permission` (`tenant_id`, `parent_id`, `permission_code`, `permission_name`, `permission_type`, `resource_path`, `http_method`, `sort`, `status`, `visible`, `created_at`)
VALUES
(1, @sq_menu, 'erp:salesQuotation:list',   '报价列表',   2, '/api/erp/sales-quotations/get-sales-quotation-page',        'GET',    1, 1, 1, NOW()),
(1, @sq_menu, 'erp:salesQuotation:detail', '报价详情',   2, '/api/erp/sales-quotations/get-sales-quotation/{id}',         'GET',    2, 1, 1, NOW()),
(1, @sq_menu, 'erp:salesQuotation:create', '创建报价',   2, '/api/erp/sales-quotations/create-sales-quotation',           'POST',   3, 1, 1, NOW()),
(1, @sq_menu, 'erp:salesQuotation:update', '编辑报价',   2, '/api/erp/sales-quotations/update-sales-quotation/{id}',      'PUT',    4, 1, 1, NOW()),
(1, @sq_menu, 'erp:salesQuotation:delete', '删除报价',   2, '/api/erp/sales-quotations/delete-sales-quotation/{id}',      'DELETE', 5, 1, 1, NOW()),
(1, @sq_menu, 'erp:salesQuotation:send',   '发送报价',   2, '/api/erp/sales-quotations/send/{id}',                        'POST',   6, 1, 1, NOW()),
(1, @sq_menu, 'erp:salesQuotation:accept', '接受报价',   2, '/api/erp/sales-quotations/accept/{id}',                      'POST',   7, 1, 1, NOW()),
(1, @sq_menu, 'erp:salesQuotation:reject', '拒绝报价',   2, '/api/erp/sales-quotations/reject/{id}',                      'POST',   8, 1, 1, NOW()),
(1, @sq_menu, 'erp:salesQuotation:toOrder','转销售订单', 2, '/api/erp/sales-quotations/to-sales-order/{id}',             'POST',   9, 1, 1, NOW());

-- 3c. 角色分配（按 role_code 动态查询，避免硬编码 role_id）
INSERT IGNORE INTO `sys_role_permission` (`tenant_id`, `role_id`, `permission_id`, `created_at`)
SELECT 1, r.id, p.id, NOW()
FROM `sys_role` r
CROSS JOIN `sys_permission` p
WHERE r.`tenant_id` = 1 AND r.`role_code` = 'SUPER_ADMIN'
  AND p.`tenant_id` = 1 AND p.`permission_code` LIKE 'erp:salesQuotation%';

INSERT IGNORE INTO `sys_role_permission` (`tenant_id`, `role_id`, `permission_id`, `created_at`)
SELECT 1, r.id, p.id, NOW()
FROM `sys_role` r
CROSS JOIN `sys_permission` p
WHERE r.`tenant_id` = 1 AND r.`role_code` = 'ADMIN'
  AND p.`tenant_id` = 1 AND p.`permission_code` LIKE 'erp:salesQuotation%';

-- 3d. 修复 patch-missing-permissions 角色关联（按 role_code 重新分配 1814~1961）
INSERT IGNORE INTO `sys_role_permission` (`tenant_id`, `role_id`, `permission_id`, `created_at`)
SELECT 1, r.id, p.id, NOW()
FROM `sys_role` r
CROSS JOIN `sys_permission` p
WHERE r.`tenant_id` = 1 AND r.`role_code` = 'SUPER_ADMIN'
  AND p.`tenant_id` = 1 AND p.`id` BETWEEN 1814 AND 1961
  AND NOT EXISTS (
    SELECT 1 FROM `sys_role_permission` rp
    WHERE rp.`tenant_id` = 1 AND rp.`role_id` = r.id AND rp.`permission_id` = p.id
  );

-- ================================================================
-- 验证
-- ================================================================
SELECT 'Gateway tables (expect 3):' AS check_item, COUNT(*) AS result
FROM information_schema.TABLES
WHERE TABLE_SCHEMA = 'my-todo-app-dev' AND TABLE_NAME LIKE 'gateway_%';

SELECT 'Tables without comment (expect 0):' AS check_item, COUNT(*) AS result
FROM information_schema.TABLES
WHERE TABLE_SCHEMA = 'my-todo-app-dev' AND TABLE_COMMENT = '';

SELECT 'Quotation permissions (expect 10):' AS check_item, COUNT(*) AS result
FROM `sys_permission`
WHERE `tenant_id` = 1 AND `permission_code` LIKE 'erp:salesQuotation%';
