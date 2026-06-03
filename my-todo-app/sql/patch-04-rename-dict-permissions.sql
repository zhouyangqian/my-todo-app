-- ============================================================
-- Patch 04: 重命名字典管理权限码，统一为冒号分隔命名规范
-- 原因: 控制器 @RequiresPermission 注解与数据库 permission_code 不匹配
-- 旧格式：dict:errorDoc:categoryList (camelCase)
-- 新格式：dict:error-doc:category:list (全小写冒号分隔)
-- ============================================================

USE `my-todo-app-dev`;

-- ======================== 1. 菜单权限重命名 ========================

-- dict:apiMarket → dict:api-market
UPDATE `sys_permission` SET `permission_code` = 'dict:api-market' WHERE `tenant_id` = 1 AND `permission_code` = 'dict:apiMarket';

-- dict:thirdParty → dict:third-party
UPDATE `sys_permission` SET `permission_code` = 'dict:third-party' WHERE `tenant_id` = 1 AND `permission_code` = 'dict:thirdParty';

-- dict:errorDoc → dict:error-doc
UPDATE `sys_permission` SET `permission_code` = 'dict:error-doc' WHERE `tenant_id` = 1 AND `permission_code` = 'dict:errorDoc';

-- ======================== 2. 参数管理按钮重命名 ========================
-- dict:parameter:categoryList → dict:parameter:category:list
UPDATE `sys_permission` SET `permission_code` = 'dict:parameter:category:list'   WHERE `tenant_id` = 1 AND `permission_code` = 'dict:parameter:categoryList';
UPDATE `sys_permission` SET `permission_code` = 'dict:parameter:category:create' WHERE `tenant_id` = 1 AND `permission_code` = 'dict:parameter:categoryCreate';
UPDATE `sys_permission` SET `permission_code` = 'dict:parameter:category:update' WHERE `tenant_id` = 1 AND `permission_code` = 'dict:parameter:categoryUpdate';
UPDATE `sys_permission` SET `permission_code` = 'dict:parameter:category:delete' WHERE `tenant_id` = 1 AND `permission_code` = 'dict:parameter:categoryDelete';

-- dict:parameter:list/create/update/delete → dict:parameter:dictionary:list/create/update/delete
UPDATE `sys_permission` SET `permission_code` = 'dict:parameter:dictionary:list'   WHERE `tenant_id` = 1 AND `permission_code` = 'dict:parameter:list';
UPDATE `sys_permission` SET `permission_code` = 'dict:parameter:dictionary:create' WHERE `tenant_id` = 1 AND `permission_code` = 'dict:parameter:create';
UPDATE `sys_permission` SET `permission_code` = 'dict:parameter:dictionary:update' WHERE `tenant_id` = 1 AND `permission_code` = 'dict:parameter:update';
UPDATE `sys_permission` SET `permission_code` = 'dict:parameter:dictionary:delete' WHERE `tenant_id` = 1 AND `permission_code` = 'dict:parameter:delete';

-- ======================== 3. API市场按钮重命名 ========================
-- dict:apiMarket:* → dict:api-market:*
UPDATE `sys_permission` SET `permission_code` = 'dict:api-market:list'        WHERE `tenant_id` = 1 AND `permission_code` = 'dict:apiMarket:list';
UPDATE `sys_permission` SET `permission_code` = 'dict:api-market:detail'      WHERE `tenant_id` = 1 AND `permission_code` = 'dict:apiMarket:detail';
UPDATE `sys_permission` SET `permission_code` = 'dict:api-market:create'      WHERE `tenant_id` = 1 AND `permission_code` = 'dict:apiMarket:create';
UPDATE `sys_permission` SET `permission_code` = 'dict:api-market:update'      WHERE `tenant_id` = 1 AND `permission_code` = 'dict:apiMarket:update';
UPDATE `sys_permission` SET `permission_code` = 'dict:api-market:delete'      WHERE `tenant_id` = 1 AND `permission_code` = 'dict:apiMarket:delete';
UPDATE `sys_permission` SET `permission_code` = 'dict:api-market:subscribe'   WHERE `tenant_id` = 1 AND `permission_code` = 'dict:apiMarket:subscribe';
UPDATE `sys_permission` SET `permission_code` = 'dict:api-market:unsubscribe' WHERE `tenant_id` = 1 AND `permission_code` = 'dict:apiMarket:unsubscribe';

-- ======================== 4. 第三方API按钮重命名 ========================
-- dict:thirdParty:* → dict:third-party:*
UPDATE `sys_permission` SET `permission_code` = 'dict:third-party:list'        WHERE `tenant_id` = 1 AND `permission_code` = 'dict:thirdParty:list';
UPDATE `sys_permission` SET `permission_code` = 'dict:third-party:create'      WHERE `tenant_id` = 1 AND `permission_code` = 'dict:thirdParty:create';
UPDATE `sys_permission` SET `permission_code` = 'dict:third-party:update'      WHERE `tenant_id` = 1 AND `permission_code` = 'dict:thirdParty:update';
UPDATE `sys_permission` SET `permission_code` = 'dict:third-party:delete'      WHERE `tenant_id` = 1 AND `permission_code` = 'dict:thirdParty:delete';
UPDATE `sys_permission` SET `permission_code` = 'dict:third-party:call-logs'   WHERE `tenant_id` = 1 AND `permission_code` = 'dict:thirdParty:callLogs';
UPDATE `sys_permission` SET `permission_code` = 'dict:third-party:health-check' WHERE `tenant_id` = 1 AND `permission_code` = 'dict:thirdParty:healthCheck';

-- ======================== 5. 追踪管理按钮重命名 ========================
-- dict:trace:configList → dict:trace:config:list
UPDATE `sys_permission` SET `permission_code` = 'dict:trace:config:list'   WHERE `tenant_id` = 1 AND `permission_code` = 'dict:trace:configList';
UPDATE `sys_permission` SET `permission_code` = 'dict:trace:config:create' WHERE `tenant_id` = 1 AND `permission_code` = 'dict:trace:configCreate';
UPDATE `sys_permission` SET `permission_code` = 'dict:trace:config:update' WHERE `tenant_id` = 1 AND `permission_code` = 'dict:trace:configUpdate';
UPDATE `sys_permission` SET `permission_code` = 'dict:trace:config:delete' WHERE `tenant_id` = 1 AND `permission_code` = 'dict:trace:configDelete';

-- dict:trace:alertList → dict:trace:alert:list
UPDATE `sys_permission` SET `permission_code` = 'dict:trace:alert:list' WHERE `tenant_id` = 1 AND `permission_code` = 'dict:trace:alertList';

-- dict:trace:acknowledge → dict:trace:alert:acknowledge
UPDATE `sys_permission` SET `permission_code` = 'dict:trace:alert:acknowledge' WHERE `tenant_id` = 1 AND `permission_code` = 'dict:trace:acknowledge';

-- ======================== 6. 错误文档按钮重命名 ========================
-- dict:errorDoc:* → dict:error-doc:*
UPDATE `sys_permission` SET `permission_code` = 'dict:error-doc:category:list'   WHERE `tenant_id` = 1 AND `permission_code` = 'dict:errorDoc:categoryList';
UPDATE `sys_permission` SET `permission_code` = 'dict:error-doc:category:create' WHERE `tenant_id` = 1 AND `permission_code` = 'dict:errorDoc:categoryCreate';
UPDATE `sys_permission` SET `permission_code` = 'dict:error-doc:category:update' WHERE `tenant_id` = 1 AND `permission_code` = 'dict:errorDoc:categoryUpdate';
UPDATE `sys_permission` SET `permission_code` = 'dict:error-doc:category:delete' WHERE `tenant_id` = 1 AND `permission_code` = 'dict:errorDoc:categoryDelete';

-- dict:errorDoc:solutionList/Create → dict:error-doc:solution:list/create
UPDATE `sys_permission` SET `permission_code` = 'dict:error-doc:solution:list'   WHERE `tenant_id` = 1 AND `permission_code` = 'dict:errorDoc:solutionList';
UPDATE `sys_permission` SET `permission_code` = 'dict:error-doc:solution:create' WHERE `tenant_id` = 1 AND `permission_code` = 'dict:errorDoc:solutionCreate';

-- dict:errorDoc:search → dict:error-doc:search (prefix only)
UPDATE `sys_permission` SET `permission_code` = 'dict:error-doc:search' WHERE `tenant_id` = 1 AND `permission_code` = 'dict:errorDoc:search';

-- ======================== 7. 补充缺失的权限 ========================

-- 7.1 参数管理 -- 补充字典详情
INSERT IGNORE INTO `sys_permission` (`tenant_id`, `parent_id`, `permission_code`, `permission_name`, `permission_type`, `resource_path`, `http_method`, `sort`, `status`, `visible`, `created_at`)
SELECT 1, id, 'dict:parameter:dictionary:detail', '参数字典详情', 2, '/api/parameter-dictionaries/get-dictionary/{id}', 'GET', 6, 1, 1, NOW()
FROM `sys_permission` WHERE `tenant_id` = 1 AND `permission_code` = 'dict:parameter' LIMIT 1;

-- 7.2 参数管理 -- 补充参数项
INSERT IGNORE INTO `sys_permission` (`tenant_id`, `parent_id`, `permission_code`, `permission_name`, `permission_type`, `resource_path`, `http_method`, `sort`, `status`, `visible`, `created_at`)
SELECT 1, id, 'dict:parameter:item:list', '参数项列表', 2, '/api/parameter-items/get-item-page', 'GET', 10, 1, 1, NOW()
FROM `sys_permission` WHERE `tenant_id` = 1 AND `permission_code` = 'dict:parameter' LIMIT 1;

INSERT IGNORE INTO `sys_permission` (`tenant_id`, `parent_id`, `permission_code`, `permission_name`, `permission_type`, `resource_path`, `http_method`, `sort`, `status`, `visible`, `created_at`)
SELECT 1, id, 'dict:parameter:item:create', '新增参数项', 2, '/api/parameter-items/create-item', 'POST', 11, 1, 1, NOW()
FROM `sys_permission` WHERE `tenant_id` = 1 AND `permission_code` = 'dict:parameter' LIMIT 1;

INSERT IGNORE INTO `sys_permission` (`tenant_id`, `parent_id`, `permission_code`, `permission_name`, `permission_type`, `resource_path`, `http_method`, `sort`, `status`, `visible`, `created_at`)
SELECT 1, id, 'dict:parameter:item:update', '编辑参数项', 2, '/api/parameter-items/update-item/{id}', 'PUT', 12, 1, 1, NOW()
FROM `sys_permission` WHERE `tenant_id` = 1 AND `permission_code` = 'dict:parameter' LIMIT 1;

INSERT IGNORE INTO `sys_permission` (`tenant_id`, `parent_id`, `permission_code`, `permission_name`, `permission_type`, `resource_path`, `http_method`, `sort`, `status`, `visible`, `created_at`)
SELECT 1, id, 'dict:parameter:item:delete', '删除参数项', 2, '/api/parameter-items/delete-item/{id}', 'DELETE', 13, 1, 1, NOW()
FROM `sys_permission` WHERE `tenant_id` = 1 AND `permission_code` = 'dict:parameter' LIMIT 1;

-- 7.3 SaaS套餐 -- 补充套餐详情
INSERT IGNORE INTO `sys_permission` (`tenant_id`, `parent_id`, `permission_code`, `permission_name`, `permission_type`, `resource_path`, `http_method`, `sort`, `status`, `visible`, `created_at`)
SELECT 1, id, 'dict:package:detail', '套餐详情', 2, '/api/packages/{id}', 'GET', 2, 1, 1, NOW()
FROM `sys_permission` WHERE `tenant_id` = 1 AND `permission_code` = 'dict:package' LIMIT 1;

-- 7.4 营销活动 -- 补充活动详情
INSERT IGNORE INTO `sys_permission` (`tenant_id`, `parent_id`, `permission_code`, `permission_name`, `permission_type`, `resource_path`, `http_method`, `sort`, `status`, `visible`, `created_at`)
SELECT 1, id, 'dict:activity:detail', '活动详情', 2, '/api/activities/{id}', 'GET', 2, 1, 1, NOW()
FROM `sys_permission` WHERE `tenant_id` = 1 AND `permission_code` = 'dict:activity' LIMIT 1;

-- 7.5 追踪管理 -- 补充告警CRUD
INSERT IGNORE INTO `sys_permission` (`tenant_id`, `parent_id`, `permission_code`, `permission_name`, `permission_type`, `resource_path`, `http_method`, `sort`, `status`, `visible`, `created_at`)
SELECT 1, id, 'dict:trace:alert:create', '创建告警', 2, '/api/trace/alerts/create', 'POST', 5, 1, 1, NOW()
FROM `sys_permission` WHERE `tenant_id` = 1 AND `permission_code` = 'dict:trace' LIMIT 1;

INSERT IGNORE INTO `sys_permission` (`tenant_id`, `parent_id`, `permission_code`, `permission_name`, `permission_type`, `resource_path`, `http_method`, `sort`, `status`, `visible`, `created_at`)
SELECT 1, id, 'dict:trace:alert:update', '更新告警', 2, '/api/trace/alerts/update/{id}', 'PUT', 6, 1, 1, NOW()
FROM `sys_permission` WHERE `tenant_id` = 1 AND `permission_code` = 'dict:trace' LIMIT 1;

INSERT IGNORE INTO `sys_permission` (`tenant_id`, `parent_id`, `permission_code`, `permission_name`, `permission_type`, `resource_path`, `http_method`, `sort`, `status`, `visible`, `created_at`)
SELECT 1, id, 'dict:trace:alert:delete', '删除告警', 2, '/api/trace/alerts/delete/{id}', 'DELETE', 7, 1, 1, NOW()
FROM `sys_permission` WHERE `tenant_id` = 1 AND `permission_code` = 'dict:trace' LIMIT 1;

-- 7.6 字典项权限
INSERT IGNORE INTO `sys_permission` (`tenant_id`, `parent_id`, `permission_code`, `permission_name`, `permission_type`, `resource_path`, `http_method`, `sort`, `status`, `visible`, `created_at`)
SELECT 1, id, 'dict:item:list', '字典项列表', 2, '/api/dict/items/code/{dictCode}', 'GET', 6, 1, 1, NOW()
FROM `sys_permission` WHERE `tenant_id` = 1 AND `permission_code` = 'dict:type' LIMIT 1;

INSERT IGNORE INTO `sys_permission` (`tenant_id`, `parent_id`, `permission_code`, `permission_name`, `permission_type`, `resource_path`, `http_method`, `sort`, `status`, `visible`, `created_at`)
SELECT 1, id, 'dict:item:create', '新增字典项', 2, '/api/dict/items/add-dict-item', 'POST', 7, 1, 1, NOW()
FROM `sys_permission` WHERE `tenant_id` = 1 AND `permission_code` = 'dict:type' LIMIT 1;

INSERT IGNORE INTO `sys_permission` (`tenant_id`, `parent_id`, `permission_code`, `permission_name`, `permission_type`, `resource_path`, `http_method`, `sort`, `status`, `visible`, `created_at`)
SELECT 1, id, 'dict:item:update', '编辑字典项', 2, '/api/dict/items/update-dict-item/{id}', 'PUT', 8, 1, 1, NOW()
FROM `sys_permission` WHERE `tenant_id` = 1 AND `permission_code` = 'dict:type' LIMIT 1;

INSERT IGNORE INTO `sys_permission` (`tenant_id`, `parent_id`, `permission_code`, `permission_name`, `permission_type`, `resource_path`, `http_method`, `sort`, `status`, `visible`, `created_at`)
SELECT 1, id, 'dict:item:delete', '删除字典项', 2, '/api/dict/items/delete-dict-item/{id}', 'DELETE', 9, 1, 1, NOW()
FROM `sys_permission` WHERE `tenant_id` = 1 AND `permission_code` = 'dict:type' LIMIT 1;

-- ======================== 8. 修复系统配置按钮的资源路径 ========================
UPDATE `sys_permission` SET `resource_path` = '/api/dict/config/get-config-page'       WHERE `tenant_id` = 1 AND `permission_code` = 'dict:config:list';
UPDATE `sys_permission` SET `resource_path` = '/api/dict/config/get-config/{id}'        WHERE `tenant_id` = 1 AND `permission_code` = 'dict:config:detail';
UPDATE `sys_permission` SET `resource_path` = '/api/dict/config/create-config'          WHERE `tenant_id` = 1 AND `permission_code` = 'dict:config:create';
UPDATE `sys_permission` SET `resource_path` = '/api/dict/config/update-config/{id}'     WHERE `tenant_id` = 1 AND `permission_code` = 'dict:config:update';
UPDATE `sys_permission` SET `resource_path` = '/api/dict/config/delete-config/{id}'     WHERE `tenant_id` = 1 AND `permission_code` = 'dict:config:delete';

-- ======================== 9. 为超级管理员角色分配新增权限 ========================
INSERT IGNORE INTO `sys_role_permission` (`tenant_id`, `role_id`, `permission_id`, `created_at`)
SELECT 1, r.id, p.id, NOW()
FROM `sys_role` r
CROSS JOIN `sys_permission` p
WHERE r.`tenant_id` = 1
  AND r.`role_code` = 'SUPER_ADMIN'
  AND p.`tenant_id` = 1
  AND p.`permission_code` IN (
    'dict:parameter:dictionary:detail',
    'dict:parameter:item:list', 'dict:parameter:item:create', 'dict:parameter:item:update', 'dict:parameter:item:delete',
    'dict:package:detail',
    'dict:activity:detail',
    'dict:trace:alert:create', 'dict:trace:alert:update', 'dict:trace:alert:delete',
    'dict:item:list', 'dict:item:create', 'dict:item:update', 'dict:item:delete'
  );
