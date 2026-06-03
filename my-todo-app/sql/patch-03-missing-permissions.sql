-- ============================================================
-- Patch 03: 补充缺失的销售报价单权限 + 修复角色关联
-- 问题:
--   1. SalesQuotationController 有 9 个端点但 sys_permission 中无对应权限
--   2. patch-missing-permissions.sql 硬编码 role_id=1 未按 role_code 分配
-- ============================================================

USE `my-todo-app-dev`;

-- ======================== 1. 销售报价单菜单 ========================
INSERT IGNORE INTO `sys_permission` (`tenant_id`, `parent_id`, `permission_code`, `permission_name`, `permission_type`, `resource_path`, `http_method`, `icon`, `menu_path`, `component`, `sort`, `status`, `visible`, `created_at`)
SELECT 1, id, 'erp:salesQuotation', '销售报价', 1,
       '/api/erp/sales-quotations/get-sales-quotation-page', 'GET',
       'Document', '/erp/sales-quotation', 'erp/sales-quotation/index',
       7, 1, 1, NOW()
FROM `sys_permission` WHERE `tenant_id` = 1 AND `permission_code` = 'erp' LIMIT 1;

-- ======================== 2. 销售报价单按钮 ========================
SET @sq_menu = (SELECT id FROM `sys_permission` WHERE `tenant_id` = 1 AND `permission_code` = 'erp:salesQuotation' LIMIT 1);

INSERT IGNORE INTO `sys_permission` (`tenant_id`, `parent_id`, `permission_code`, `permission_name`, `permission_type`, `resource_path`, `http_method`, `sort`, `status`, `visible`, `created_at`)
VALUES
-- 基础CRUD
(1, @sq_menu, 'erp:salesQuotation:list',   '报价列表',   2, '/api/erp/sales-quotations/get-sales-quotation-page',        'GET',    1, 1, 1, NOW()),
(1, @sq_menu, 'erp:salesQuotation:detail', '报价详情',   2, '/api/erp/sales-quotations/get-sales-quotation/{id}',         'GET',    2, 1, 1, NOW()),
(1, @sq_menu, 'erp:salesQuotation:create', '创建报价',   2, '/api/erp/sales-quotations/create-sales-quotation',           'POST',   3, 1, 1, NOW()),
(1, @sq_menu, 'erp:salesQuotation:update', '编辑报价',   2, '/api/erp/sales-quotations/update-sales-quotation/{id}',      'PUT',    4, 1, 1, NOW()),
(1, @sq_menu, 'erp:salesQuotation:delete', '删除报价',   2, '/api/erp/sales-quotations/delete-sales-quotation/{id}',      'DELETE', 5, 1, 1, NOW()),
-- 业务流程
(1, @sq_menu, 'erp:salesQuotation:send',   '发送报价',   2, '/api/erp/sales-quotations/send/{id}',                        'POST',   6, 1, 1, NOW()),
(1, @sq_menu, 'erp:salesQuotation:accept', '接受报价',   2, '/api/erp/sales-quotations/accept/{id}',                      'POST',   7, 1, 1, NOW()),
(1, @sq_menu, 'erp:salesQuotation:reject', '拒绝报价',   2, '/api/erp/sales-quotations/reject/{id}',                      'POST',   8, 1, 1, NOW()),
(1, @sq_menu, 'erp:salesQuotation:toOrder','转销售订单', 2, '/api/erp/sales-quotations/to-sales-order/{id}',             'POST',   9, 1, 1, NOW());

-- ======================== 3. 为角色分配新权限 ========================
-- 注意: 使用 role_code 动态查询，避免硬编码 role_id

-- 超级管理员: 分配所有新增权限
INSERT IGNORE INTO `sys_role_permission` (`tenant_id`, `role_id`, `permission_id`, `created_at`)
SELECT 1, r.id, p.id, NOW()
FROM `sys_role` r
CROSS JOIN `sys_permission` p
WHERE r.`tenant_id` = 1
  AND r.`role_code` = 'SUPER_ADMIN'
  AND p.`tenant_id` = 1
  AND p.`permission_code` LIKE 'erp:salesQuotation%';

-- 管理员: 分配报价单权限（与销售订单权限一致）
INSERT IGNORE INTO `sys_role_permission` (`tenant_id`, `role_id`, `permission_id`, `created_at`)
SELECT 1, r.id, p.id, NOW()
FROM `sys_role` r
CROSS JOIN `sys_permission` p
WHERE r.`tenant_id` = 1
  AND r.`role_code` = 'ADMIN'
  AND p.`tenant_id` = 1
  AND p.`permission_code` LIKE 'erp:salesQuotation%';

-- ======================== 4. 修复 patch-missing-permissions 角色关联 ========================
-- 原补丁脚本硬编码 role_id=1，在 AUTO_INCREMENT 不一致时会出错
-- 以下按 role_code 重新分配 1814~1961 范围内的权限给 SUPER_ADMIN

INSERT IGNORE INTO `sys_role_permission` (`tenant_id`, `role_id`, `permission_id`, `created_at`)
SELECT 1, r.id, p.id, NOW()
FROM `sys_role` r
CROSS JOIN `sys_permission` p
WHERE r.`tenant_id` = 1
  AND r.`role_code` = 'SUPER_ADMIN'
  AND p.`tenant_id` = 1
  AND p.`id` BETWEEN 1814 AND 1961
  AND NOT EXISTS (
    SELECT 1 FROM `sys_role_permission` rp
    WHERE rp.`tenant_id` = 1
      AND rp.`role_id` = r.id
      AND rp.`permission_id` = p.id
  );
