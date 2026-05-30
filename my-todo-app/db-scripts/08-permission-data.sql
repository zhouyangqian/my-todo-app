-- 权限数据脚本（基于真实后端接口）
-- 接口地址格式：/完整路径/方法名

USE `my-todo-app-dev`;

-- 清空现有权限数据
DELETE FROM `sys_role_permission` WHERE `tenant_id` = 1;
DELETE FROM `sys_permission` WHERE `tenant_id` = 1;
DELETE FROM `sys_role` WHERE `tenant_id` = 1;

-- ==================== 1. 插入角色 ====================
INSERT INTO `sys_role` (`tenant_id`, `role_code`, `role_name`, `description`, `sort`, `status`, `data_scope`, `created_at`)
VALUES
(1, 'SUPER_ADMIN', '超级管理员', '拥有全部系统权限', 1, 1, 1, NOW()),
(1, 'ADMIN', '管理员', '拥有管理权限', 2, 1, 1, NOW()),
(1, 'USER', '普通用户', '基础用户权限', 3, 1, 3, NOW());

-- ==================== 2. 插入顶级菜单 ====================
INSERT INTO `sys_permission` (`tenant_id`, `parent_id`, `permission_code`, `permission_name`, `permission_type`, `resource_path`, `http_method`, `icon`, `menu_path`, `component`, `sort`, `status`, `visible`, `created_at`)
VALUES
(1, 0, 'system', '系统管理', 1, '/system', 'GET', 'Setting', 'system', NULL, 1, 1, 1, NOW()),
(1, 0, 'dict', '字典管理', 1, '/dict', 'GET', 'Collection', 'dict', NULL, 2, 1, 1, NOW()),
(1, 0, 'erp', 'ERP管理', 1, '/erp', 'GET', 'ShoppingCart', 'erp', NULL, 3, 1, 1, NOW()),
(1, 0, 'finance', '财务管理', 1, '/finance', 'GET', 'Money', 'finance', NULL, 4, 1, 1, NOW());

-- ==================== 3. 插入系统管理子菜单和按钮 ====================
-- 系统管理子菜单
INSERT INTO `sys_permission` (`tenant_id`, `parent_id`, `permission_code`, `permission_name`, `permission_type`, `resource_path`, `http_method`, `icon`, `menu_path`, `component`, `sort`, `status`, `visible`, `created_at`)
SELECT 1, (SELECT id FROM sys_permission WHERE tenant_id=1 AND permission_code='system' LIMIT 1),
       'system:user', '用户管理', 1, '/api/users/get-user-page', 'GET', 'User', '/system/user', 'system/user/index', 1, 1, 1, NOW()
UNION ALL SELECT 1, (SELECT id FROM sys_permission WHERE tenant_id=1 AND permission_code='system' LIMIT 1),
       'system:role', '角色管理', 1, '/api/roles/get-role-list', 'GET', 'UserFilled', '/system/role', 'system/role/index', 2, 1, 1, NOW()
UNION ALL SELECT 1, (SELECT id FROM sys_permission WHERE tenant_id=1 AND permission_code='system' LIMIT 1),
       'system:permission', '权限管理', 1, '/api/permissions/get-current-user-permissions', 'GET', 'Lock', '/system/permission', 'system/permission/index', 3, 1, 1, NOW()
UNION ALL SELECT 1, (SELECT id FROM sys_permission WHERE tenant_id=1 AND permission_code='system' LIMIT 1),
       'system:dept', '部门管理', 1, '/api/system/dept/get-department-tree', 'GET', 'OfficeBuilding', '/system/dept', 'system/dept/index', 4, 1, 1, NOW();

-- 用户管理按钮
INSERT INTO `sys_permission` (`tenant_id`, `parent_id`, `permission_code`, `permission_name`, `permission_type`, `resource_path`, `http_method`, `sort`, `status`, `visible`, `created_at`)
SELECT 1, (SELECT id FROM sys_permission WHERE tenant_id=1 AND permission_code='system:user' LIMIT 1),
       'system:user:list', '用户列表', 2, '/api/users/get-user-page', 'GET', 1, 1, 1, NOW()
UNION ALL SELECT 1, (SELECT id FROM sys_permission WHERE tenant_id=1 AND permission_code='system:user' LIMIT 1),
       'system:user:detail', '用户详情', 2, '/api/users/get-user/{id}', 'GET', 2, 1, 1, NOW()
UNION ALL SELECT 1, (SELECT id FROM sys_permission WHERE tenant_id=1 AND permission_code='system:user' LIMIT 1),
       'system:user:create', '新增用户', 2, '/api/users/create-user', 'POST', 3, 1, 1, NOW()
UNION ALL SELECT 1, (SELECT id FROM sys_permission WHERE tenant_id=1 AND permission_code='system:user' LIMIT 1),
       'system:user:update', '编辑用户', 2, '/api/users/update-user/{id}', 'PUT', 4, 1, 1, NOW()
UNION ALL SELECT 1, (SELECT id FROM sys_permission WHERE tenant_id=1 AND permission_code='system:user' LIMIT 1),
       'system:user:delete', '删除用户', 2, '/api/users/delete-user/{id}', 'DELETE', 5, 1, 1, NOW()
UNION ALL SELECT 1, (SELECT id FROM sys_permission WHERE tenant_id=1 AND permission_code='system:user' LIMIT 1),
       'system:user:enable', '启用用户', 2, '/api/users/enable-user/{id}', 'POST', 6, 1, 1, NOW()
UNION ALL SELECT 1, (SELECT id FROM sys_permission WHERE tenant_id=1 AND permission_code='system:user' LIMIT 1),
       'system:user:disable', '禁用用户', 2, '/api/users/disable-user/{id}', 'POST', 7, 1, 1, NOW()
UNION ALL SELECT 1, (SELECT id FROM sys_permission WHERE tenant_id=1 AND permission_code='system:user' LIMIT 1),
       'system:user:kick', '踢出用户', 2, '/api/auth/kick-user/{id}', 'POST', 8, 1, 1, NOW();

-- 角色管理按钮
INSERT INTO `sys_permission` (`tenant_id`, `parent_id`, `permission_code`, `permission_name`, `permission_type`, `resource_path`, `http_method`, `sort`, `status`, `visible`, `created_at`)
SELECT 1, (SELECT id FROM sys_permission WHERE tenant_id=1 AND permission_code='system:role' LIMIT 1),
       'system:role:list', '角色列表', 2, '/api/roles/get-role-list', 'GET', 1, 1, 1, NOW()
UNION ALL SELECT 1, (SELECT id FROM sys_permission WHERE tenant_id=1 AND permission_code='system:role' LIMIT 1),
       'system:role:detail', '角色详情', 2, '/api/roles/get-role/{id}', 'GET', 2, 1, 1, NOW()
UNION ALL SELECT 1, (SELECT id FROM sys_permission WHERE tenant_id=1 AND permission_code='system:role' LIMIT 1),
       'system:role:create', '新增角色', 2, '/api/roles/create-role', 'POST', 3, 1, 1, NOW()
UNION ALL SELECT 1, (SELECT id FROM sys_permission WHERE tenant_id=1 AND permission_code='system:role' LIMIT 1),
       'system:role:update', '编辑角色', 2, '/api/roles/update-role/{id}', 'PUT', 4, 1, 1, NOW()
UNION ALL SELECT 1, (SELECT id FROM sys_permission WHERE tenant_id=1 AND permission_code='system:role' LIMIT 1),
       'system:role:delete', '删除角色', 2, '/api/roles/delete-role/{id}', 'DELETE', 5, 1, 1, NOW()
UNION ALL SELECT 1, (SELECT id FROM sys_permission WHERE tenant_id=1 AND permission_code='system:role' LIMIT 1),
       'system:role:assignToUser', '分配角色', 2, '/api/roles/assign-to-user', 'POST', 6, 1, 1, NOW()
UNION ALL SELECT 1, (SELECT id FROM sys_permission WHERE tenant_id=1 AND permission_code='system:role' LIMIT 1),
       'system:role:assignPerm', '分配权限', 2, '/api/roles/assign-to-user', 'POST', 7, 1, 1, NOW();

-- 权限管理按钮
INSERT INTO `sys_permission` (`tenant_id`, `parent_id`, `permission_code`, `permission_name`, `permission_type`, `resource_path`, `http_method`, `sort`, `status`, `visible`, `created_at`)
SELECT 1, (SELECT id FROM sys_permission WHERE tenant_id=1 AND permission_code='system:permission' LIMIT 1),
       'system:permission:list', '权限列表', 2, '/api/permissions/get-current-user-permissions', 'GET', 1, 1, 1, NOW()
UNION ALL SELECT 1, (SELECT id FROM sys_permission WHERE tenant_id=1 AND permission_code='system:permission' LIMIT 1),
       'system:permission:detail', '权限详情', 2, '/api/permissions/get-user-permissions/{userId}', 'GET', 2, 1, 1, NOW()
UNION ALL SELECT 1, (SELECT id FROM sys_permission WHERE tenant_id=1 AND permission_code='system:permission' LIMIT 1),
       'system:permission:create', '新增权限', 2, '/api/permissions/create-permission', 'POST', 3, 1, 1, NOW()
UNION ALL SELECT 1, (SELECT id FROM sys_permission WHERE tenant_id=1 AND permission_code='system:permission' LIMIT 1),
       'system:permission:update', '编辑权限', 2, '/api/permissions/update-permission/{id}', 'PUT', 4, 1, 1, NOW()
UNION ALL SELECT 1, (SELECT id FROM sys_permission WHERE tenant_id=1 AND permission_code='system:permission' LIMIT 1),
       'system:permission:delete', '删除权限', 2, '/api/permissions/delete-permission/{id}', 'DELETE', 5, 1, 1, NOW();

-- 部门管理按钮
INSERT INTO `sys_permission` (`tenant_id`, `parent_id`, `permission_code`, `permission_name`, `permission_type`, `resource_path`, `http_method`, `sort`, `status`, `visible`, `created_at`)
SELECT 1, (SELECT id FROM sys_permission WHERE tenant_id=1 AND permission_code='system:dept' LIMIT 1),
       'system:dept:list', '部门列表', 2, '/api/system/dept/get-department-tree', 'GET', 1, 1, 1, NOW()
UNION ALL SELECT 1, (SELECT id FROM sys_permission WHERE tenant_id=1 AND permission_code='system:dept' LIMIT 1),
       'system:dept:detail', '部门详情', 2, '/api/system/dept/get-department-tree', 'GET', 2, 1, 1, NOW()
UNION ALL SELECT 1, (SELECT id FROM sys_permission WHERE tenant_id=1 AND permission_code='system:dept' LIMIT 1),
       'system:dept:create', '新增部门', 2, '/api/system/dept/create-department', 'POST', 3, 1, 1, NOW()
UNION ALL SELECT 1, (SELECT id FROM sys_permission WHERE tenant_id=1 AND permission_code='system:dept' LIMIT 1),
       'system:dept:update', '编辑部门', 2, '/api/system/dept/update-department/{id}', 'PUT', 4, 1, 1, NOW()
UNION ALL SELECT 1, (SELECT id FROM sys_permission WHERE tenant_id=1 AND permission_code='system:dept' LIMIT 1),
       'system:dept:delete', '删除部门', 2, '/api/system/dept/delete-department/{id}', 'DELETE', 5, 1, 1, NOW();

-- ==================== 4. 插入字典管理子菜单和按钮 ====================
-- 字典管理子菜单
INSERT INTO `sys_permission` (`tenant_id`, `parent_id`, `permission_code`, `permission_name`, `permission_type`, `resource_path`, `http_method`, `icon`, `menu_path`, `component`, `sort`, `status`, `visible`, `created_at`)
SELECT 1, (SELECT id FROM sys_permission WHERE tenant_id=1 AND permission_code='dict' LIMIT 1),
       'dict:type', '字典类型', 1, '/api/dict/types/get-dict-type-page', 'GET', 'Files', '/dict/type', 'dict/type/index', 1, 1, 1, NOW()
UNION ALL SELECT 1, (SELECT id FROM sys_permission WHERE tenant_id=1 AND permission_code='dict' LIMIT 1),
       'dict:config', '系统配置', 1, '/api/dict/types/get-dict-type-page', 'GET', 'Tools', '/dict/config', 'dict/config/index', 2, 1, 1, NOW();

-- 字典类型按钮
INSERT INTO `sys_permission` (`tenant_id`, `parent_id`, `permission_code`, `permission_name`, `permission_type`, `resource_path`, `http_method`, `sort`, `status`, `visible`, `created_at`)
SELECT 1, (SELECT id FROM sys_permission WHERE tenant_id=1 AND permission_code='dict:type' LIMIT 1),
       'dict:type:list', '字典类型列表', 2, '/api/dict/types/get-dict-type-page', 'GET', 1, 1, 1, NOW()
UNION ALL SELECT 1, (SELECT id FROM sys_permission WHERE tenant_id=1 AND permission_code='dict:type' LIMIT 1),
       'dict:type:detail', '字典类型详情', 2, '/api/dict/types/get-dict-type/{id}', 'GET', 2, 1, 1, NOW()
UNION ALL SELECT 1, (SELECT id FROM sys_permission WHERE tenant_id=1 AND permission_code='dict:type' LIMIT 1),
       'dict:type:create', '新增字典类型', 2, '/api/dict/types/create-dict-type', 'POST', 3, 1, 1, NOW()
UNION ALL SELECT 1, (SELECT id FROM sys_permission WHERE tenant_id=1 AND permission_code='dict:type' LIMIT 1),
       'dict:type:update', '编辑字典类型', 2, '/api/dict/types/update-dict-type/{id}', 'PUT', 4, 1, 1, NOW()
UNION ALL SELECT 1, (SELECT id FROM sys_permission WHERE tenant_id=1 AND permission_code='dict:type' LIMIT 1),
       'dict:type:delete', '删除字典类型', 2, '/api/dict/types/delete-dict-type/{id}', 'DELETE', 5, 1, 1, NOW();

-- 系统配置按钮
INSERT INTO `sys_permission` (`tenant_id`, `parent_id`, `permission_code`, `permission_name`, `permission_type`, `resource_path`, `http_method`, `sort`, `status`, `visible`, `created_at`)
SELECT 1, (SELECT id FROM sys_permission WHERE tenant_id=1 AND permission_code='dict:config' LIMIT 1),
       'dict:config:list', '配置列表', 2, '/api/dict/types/get-dict-type-page', 'GET', 1, 1, 1, NOW()
UNION ALL SELECT 1, (SELECT id FROM sys_permission WHERE tenant_id=1 AND permission_code='dict:config' LIMIT 1),
       'dict:config:detail', '配置详情', 2, '/api/dict/types/get-dict-type/{id}', 'GET', 2, 1, 1, NOW()
UNION ALL SELECT 1, (SELECT id FROM sys_permission WHERE tenant_id=1 AND permission_code='dict:config' LIMIT 1),
       'dict:config:create', '新增配置', 2, '/api/dict/types/create-dict-type', 'POST', 3, 1, 1, NOW()
UNION ALL SELECT 1, (SELECT id FROM sys_permission WHERE tenant_id=1 AND permission_code='dict:config' LIMIT 1),
       'dict:config:update', '编辑配置', 2, '/api/dict/types/update-dict-type/{id}', 'PUT', 4, 1, 1, NOW()
UNION ALL SELECT 1, (SELECT id FROM sys_permission WHERE tenant_id=1 AND permission_code='dict:config' LIMIT 1),
       'dict:config:delete', '删除配置', 2, '/api/dict/types/delete-dict-type/{id}', 'DELETE', 5, 1, 1, NOW();

-- ==================== 5. 插入ERP管理子菜单和按钮 ====================
-- ERP管理子菜单
INSERT INTO `sys_permission` (`tenant_id`, `parent_id`, `permission_code`, `permission_name`, `permission_type`, `resource_path`, `http_method`, `icon`, `menu_path`, `component`, `sort`, `status`, `visible`, `created_at`)
SELECT 1, (SELECT id FROM sys_permission WHERE tenant_id=1 AND permission_code='erp' LIMIT 1),
       'erp:productCategory', '商品分类', 1, '/api/erp/product-categories/get-category-page', 'GET', 'Folder', '/erp/product-category', 'erp/product-category/index', 0, 1, 1, NOW()
UNION ALL SELECT 1, (SELECT id FROM sys_permission WHERE tenant_id=1 AND permission_code='erp' LIMIT 1),
       'erp:product', '商品管理', 1, '/api/erp/products/get-product-page', 'GET', 'Goods', '/erp/product', 'erp/product/index', 1, 1, 1, NOW()
UNION ALL SELECT 1, (SELECT id FROM sys_permission WHERE tenant_id=1 AND permission_code='erp' LIMIT 1),
       'erp:warehouse', '仓库管理', 1, '/api/erp/warehouses/get-warehouse-page', 'GET', 'House', '/erp/warehouse', 'erp/warehouse/index', 2, 1, 1, NOW()
UNION ALL SELECT 1, (SELECT id FROM sys_permission WHERE tenant_id=1 AND permission_code='erp' LIMIT 1),
       'erp:inventory', '库存管理', 1, '/api/erp/inventory/get-inventory-page', 'GET', 'Box', '/erp/inventory', 'erp/inventory/index', 3, 1, 1, NOW()
UNION ALL SELECT 1, (SELECT id FROM sys_permission WHERE tenant_id=1 AND permission_code='erp' LIMIT 1),
       'erp:inventoryFlow', '库存流水', 1, '/api/erp/inventory/get-flow-page', 'GET', 'List', '/erp/inventory-flow', 'erp/inventory-flow/index', 4, 1, 1, NOW()
UNION ALL SELECT 1, (SELECT id FROM sys_permission WHERE tenant_id=1 AND permission_code='erp' LIMIT 1),
       'erp:supplier', '供应商管理', 1, '/api/erp/suppliers/get-supplier-page', 'GET', 'Van', '/erp/supplier', 'erp/supplier/index', 5, 1, 1, NOW()
UNION ALL SELECT 1, (SELECT id FROM sys_permission WHERE tenant_id=1 AND permission_code='erp' LIMIT 1),
       'erp:customer', '客户管理', 1, '/api/erp/customers/get-customer-page', 'GET', 'Avatar', '/erp/customer', 'erp/customer/index', 6, 1, 1, NOW()
UNION ALL SELECT 1, (SELECT id FROM sys_permission WHERE tenant_id=1 AND permission_code='erp' LIMIT 1),
       'erp:salesOrder', '销售订单', 1, '/api/erp/sales-orders/get-sales-order-page', 'GET', 'ShoppingCart', '/erp/sales-order', 'erp/sales-order/index', 7, 1, 1, NOW()
UNION ALL SELECT 1, (SELECT id FROM sys_permission WHERE tenant_id=1 AND permission_code='erp' LIMIT 1),
       'erp:inventoryCheck', '库存盘点', 1, '/api/erp/inventory-checks/get-check-page', 'GET', 'Document', '/erp/inventory-check', 'erp/inventory-check/index', 5, 1, 1, NOW()
UNION ALL SELECT 1, (SELECT id FROM sys_permission WHERE tenant_id=1 AND permission_code='erp' LIMIT 1),
       'erp:inventoryAlert', '库存预警', 1, '/api/erp/inventory/get-alert-inventories', 'GET', 'Warning', '/erp/inventory-alert', 'erp/inventory-alert/index', 6, 1, 1, NOW()
UNION ALL SELECT 1, (SELECT id FROM sys_permission WHERE tenant_id=1 AND permission_code='erp' LIMIT 1),
       'erp:purchaseOrder', '采购订单', 1, '/api/erp/purchase-orders/get-purchase-order-page', 'GET', 'ShoppingCartFull', '/erp/purchase-order', 'erp/purchase-order/index', 7, 1, 1, NOW()
UNION ALL SELECT 1, (SELECT id FROM sys_permission WHERE tenant_id=1 AND permission_code='erp' LIMIT 1),
       'erp:purchaseReturn', '采购退货', 1, '/api/erp/purchase-returns/get-return-page', 'GET', 'RefreshLeft', '/erp/purchase-return', 'erp/purchase-return/index', 8, 1, 1, NOW()
UNION ALL SELECT 1, (SELECT id FROM sys_permission WHERE tenant_id=1 AND permission_code='erp' LIMIT 1),
       'erp:salesReturn', '销售退货', 1, '/api/erp/sales-returns/get-return-page', 'GET', 'RefreshRight', '/erp/sales-return', 'erp/sales-return/index', 10, 1, 1, NOW()
UNION ALL SELECT 1, (SELECT id FROM sys_permission WHERE tenant_id=1 AND permission_code='erp' LIMIT 1),
       'erp:productPrice', '商品价格', 1, '/api/erp/product-prices/get-price-page', 'GET', 'PriceTag', '/erp/product-price', 'erp/product-price/index', 2, 1, 1, NOW()
UNION ALL SELECT 1, (SELECT id FROM sys_permission WHERE tenant_id=1 AND permission_code='erp' LIMIT 1),
       'erp:productPromotion', '商品促销', 1, '/api/erp/product-promotions/get-promotion-page', 'GET', 'Present', '/erp/product-promotion', 'erp/product-promotion/index', 3, 1, 1, NOW()
UNION ALL SELECT 1, (SELECT id FROM sys_permission WHERE tenant_id=1 AND permission_code='erp' LIMIT 1),
       'erp:salesShipment', '销售出库', 1, '/api/erp/sales-shipments/get-sales-shipment-page', 'GET', 'Van', '/erp/sales-shipment', 'erp/sales-shipment/index', 8, 1, 1, NOW()
UNION ALL SELECT 1, (SELECT id FROM sys_permission WHERE tenant_id=1 AND permission_code='erp' LIMIT 1),
       'erp:report', '报表统计', 1, '/api/erp/reports/dashboard', 'GET', 'DataAnalysis', '/erp/report', 'erp/report/index', 9, 1, 1, NOW()
UNION ALL SELECT 1, (SELECT id FROM sys_permission WHERE tenant_id=1 AND permission_code='erp' LIMIT 1),
       'erp:config', '系统配置', 1, '/api/erp/configs/get-config-page', 'GET', 'Setting', '/erp/config', 'erp/config/index', 10, 1, 1, NOW();

-- 商品分类按钮
INSERT INTO `sys_permission` (`tenant_id`, `parent_id`, `permission_code`, `permission_name`, `permission_type`, `resource_path`, `http_method`, `sort`, `status`, `visible`, `created_at`)
SELECT 1, (SELECT id FROM sys_permission WHERE tenant_id=1 AND permission_code='erp:productCategory' LIMIT 1),
       'erp:productCategory:list', '分类列表', 2, '/api/erp/product-categories/get-category-page', 'GET', 1, 1, 1, NOW()
UNION ALL SELECT 1, (SELECT id FROM sys_permission WHERE tenant_id=1 AND permission_code='erp:productCategory' LIMIT 1),
       'erp:productCategory:detail', '分类详情', 2, '/api/erp/product-categories/get-category/{id}', 'GET', 2, 1, 1, NOW()
UNION ALL SELECT 1, (SELECT id FROM sys_permission WHERE tenant_id=1 AND permission_code='erp:productCategory' LIMIT 1),
       'erp:productCategory:create', '新增分类', 2, '/api/erp/product-categories/create-category', 'POST', 3, 1, 1, NOW()
UNION ALL SELECT 1, (SELECT id FROM sys_permission WHERE tenant_id=1 AND permission_code='erp:productCategory' LIMIT 1),
       'erp:productCategory:update', '编辑分类', 2, '/api/erp/product-categories/update-category/{id}', 'PUT', 4, 1, 1, NOW()
UNION ALL SELECT 1, (SELECT id FROM sys_permission WHERE tenant_id=1 AND permission_code='erp:productCategory' LIMIT 1),
       'erp:productCategory:delete', '删除分类', 2, '/api/erp/product-categories/delete-category/{id}', 'DELETE', 5, 1, 1, NOW();

-- 库存盘点按钮
INSERT INTO `sys_permission` (`tenant_id`, `parent_id`, `permission_code`, `permission_name`, `permission_type`, `resource_path`, `http_method`, `sort`, `status`, `visible`, `created_at`)
SELECT 1, (SELECT id FROM sys_permission WHERE tenant_id=1 AND permission_code='erp:inventoryCheck' LIMIT 1),
       'erp:inventoryCheck:list', '盘点列表', 2, '/api/erp/inventory-checks/get-check-page', 'GET', 1, 1, 1, NOW()
UNION ALL SELECT 1, (SELECT id FROM sys_permission WHERE tenant_id=1 AND permission_code='erp:inventoryCheck' LIMIT 1),
       'erp:inventoryCheck:create', '新建盘点', 2, '/api/erp/inventory-checks/create-check', 'POST', 2, 1, 1, NOW()
UNION ALL SELECT 1, (SELECT id FROM sys_permission WHERE tenant_id=1 AND permission_code='erp:inventoryCheck' LIMIT 1),
       'erp:inventoryCheck:submit', '提交结果', 2, '/api/erp/inventory-checks/submit-check/{id}', 'POST', 3, 1, 1, NOW()
UNION ALL SELECT 1, (SELECT id FROM sys_permission WHERE tenant_id=1 AND permission_code='erp:inventoryCheck' LIMIT 1),
       'erp:inventoryCheck:cancel', '取消盘点', 2, '/api/erp/inventory-checks/cancel-check/{id}', 'POST', 4, 1, 1, NOW();

-- 库存预警按钮
INSERT INTO `sys_permission` (`tenant_id`, `parent_id`, `permission_code`, `permission_name`, `permission_type`, `resource_path`, `http_method`, `sort`, `status`, `visible`, `created_at`)
SELECT 1, (SELECT id FROM sys_permission WHERE tenant_id=1 AND permission_code='erp:inventoryAlert' LIMIT 1),
       'erp:inventoryAlert:list', '预警列表', 2, '/api/erp/inventory/get-alert-inventories', 'GET', 1, 1, 1, NOW();

-- 采购退货按钮
INSERT INTO `sys_permission` (`tenant_id`, `parent_id`, `permission_code`, `permission_name`, `permission_type`, `resource_path`, `http_method`, `sort`, `status`, `visible`, `created_at`)
SELECT 1, (SELECT id FROM sys_permission WHERE tenant_id=1 AND permission_code='erp:purchaseReturn' LIMIT 1),
       'erp:purchaseReturn:list', '退货列表', 2, '/api/erp/purchase-returns/get-return-page', 'GET', 1, 1, 1, NOW()
UNION ALL SELECT 1, (SELECT id FROM sys_permission WHERE tenant_id=1 AND permission_code='erp:purchaseReturn' LIMIT 1),
       'erp:purchaseReturn:create', '新建退货', 2, '/api/erp/purchase-returns/create-return', 'POST', 2, 1, 1, NOW()
UNION ALL SELECT 1, (SELECT id FROM sys_permission WHERE tenant_id=1 AND permission_code='erp:purchaseReturn' LIMIT 1),
       'erp:purchaseReturn:submit', '提交审核', 2, '/api/erp/purchase-returns/submit-for-approval/{id}', 'POST', 3, 1, 1, NOW()
UNION ALL SELECT 1, (SELECT id FROM sys_permission WHERE tenant_id=1 AND permission_code='erp:purchaseReturn' LIMIT 1),
       'erp:purchaseReturn:approve', '审核退货', 2, '/api/erp/purchase-returns/approve-return/{id}', 'POST', 4, 1, 1, NOW()
UNION ALL SELECT 1, (SELECT id FROM sys_permission WHERE tenant_id=1 AND permission_code='erp:purchaseReturn' LIMIT 1),
       'erp:purchaseReturn:cancel', '取消退货', 2, '/api/erp/purchase-returns/cancel-return/{id}', 'POST', 5, 1, 1, NOW();

-- 销售退货按钮
INSERT INTO `sys_permission` (`tenant_id`, `parent_id`, `permission_code`, `permission_name`, `permission_type`, `resource_path`, `http_method`, `sort`, `status`, `visible`, `created_at`)
SELECT 1, (SELECT id FROM sys_permission WHERE tenant_id=1 AND permission_code='erp:salesReturn' LIMIT 1),
       'erp:salesReturn:list', '退货列表', 2, '/api/erp/sales-returns/get-return-page', 'GET', 1, 1, 1, NOW()
UNION ALL SELECT 1, (SELECT id FROM sys_permission WHERE tenant_id=1 AND permission_code='erp:salesReturn' LIMIT 1),
       'erp:salesReturn:create', '新建退货', 2, '/api/erp/sales-returns/create-return', 'POST', 2, 1, 1, NOW()
UNION ALL SELECT 1, (SELECT id FROM sys_permission WHERE tenant_id=1 AND permission_code='erp:salesReturn' LIMIT 1),
       'erp:salesReturn:submit', '提交审核', 2, '/api/erp/sales-returns/submit-for-approval/{id}', 'POST', 3, 1, 1, NOW()
UNION ALL SELECT 1, (SELECT id FROM sys_permission WHERE tenant_id=1 AND permission_code='erp:salesReturn' LIMIT 1),
       'erp:salesReturn:approve', '审核退货', 2, '/api/erp/sales-returns/approve-return/{id}', 'POST', 4, 1, 1, NOW()
UNION ALL SELECT 1, (SELECT id FROM sys_permission WHERE tenant_id=1 AND permission_code='erp:salesReturn' LIMIT 1),
       'erp:salesReturn:cancel', '取消退货', 2, '/api/erp/sales-returns/cancel-return/{id}', 'POST', 5, 1, 1, NOW();

-- 商品价格按钮
INSERT INTO `sys_permission` (`tenant_id`, `parent_id`, `permission_code`, `permission_name`, `permission_type`, `resource_path`, `http_method`, `sort`, `status`, `visible`, `created_at`)
SELECT 1, (SELECT id FROM sys_permission WHERE tenant_id=1 AND permission_code='erp:productPrice' LIMIT 1),
       'erp:productPrice:list', '价格列表', 2, '/api/erp/product-prices/get-price-page', 'GET', 1, 1, 1, NOW()
UNION ALL SELECT 1, (SELECT id FROM sys_permission WHERE tenant_id=1 AND permission_code='erp:productPrice' LIMIT 1),
       'erp:productPrice:create', '新增价格', 2, '/api/erp/product-prices/create-price', 'POST', 2, 1, 1, NOW()
UNION ALL SELECT 1, (SELECT id FROM sys_permission WHERE tenant_id=1 AND permission_code='erp:productPrice' LIMIT 1),
       'erp:productPrice:update', '编辑价格', 2, '/api/erp/product-prices/update-price/{id}', 'PUT', 3, 1, 1, NOW()
UNION ALL SELECT 1, (SELECT id FROM sys_permission WHERE tenant_id=1 AND permission_code='erp:productPrice' LIMIT 1),
       'erp:productPrice:delete', '删除价格', 2, '/api/erp/product-prices/delete-price/{id}', 'DELETE', 4, 1, 1, NOW();

-- 商品促销按钮
INSERT INTO `sys_permission` (`tenant_id`, `parent_id`, `permission_code`, `permission_name`, `permission_type`, `resource_path`, `http_method`, `sort`, `status`, `visible`, `created_at`)
SELECT 1, (SELECT id FROM sys_permission WHERE tenant_id=1 AND permission_code='erp:productPromotion' LIMIT 1),
       'erp:productPromotion:list', '促销列表', 2, '/api/erp/product-promotions/get-promotion-page', 'GET', 1, 1, 1, NOW()
UNION ALL SELECT 1, (SELECT id FROM sys_permission WHERE tenant_id=1 AND permission_code='erp:productPromotion' LIMIT 1),
       'erp:productPromotion:create', '新增促销', 2, '/api/erp/product-promotions/create-promotion', 'POST', 2, 1, 1, NOW()
UNION ALL SELECT 1, (SELECT id FROM sys_permission WHERE tenant_id=1 AND permission_code='erp:productPromotion' LIMIT 1),
       'erp:productPromotion:update', '编辑促销', 2, '/api/erp/product-promotions/update-promotion/{id}', 'PUT', 3, 1, 1, NOW()
UNION ALL SELECT 1, (SELECT id FROM sys_permission WHERE tenant_id=1 AND permission_code='erp:productPromotion' LIMIT 1),
       'erp:productPromotion:delete', '删除促销', 2, '/api/erp/product-promotions/delete-promotion/{id}', 'DELETE', 4, 1, 1, NOW()
UNION ALL SELECT 1, (SELECT id FROM sys_permission WHERE tenant_id=1 AND permission_code='erp:productPromotion' LIMIT 1),
       'erp:productPromotion:enable', '启用促销', 2, '/api/erp/product-promotions/enable-promotion/{id}', 'POST', 5, 1, 1, NOW()
UNION ALL SELECT 1, (SELECT id FROM sys_permission WHERE tenant_id=1 AND permission_code='erp:productPromotion' LIMIT 1),
       'erp:productPromotion:disable', '停用促销', 2, '/api/erp/product-promotions/disable-promotion/{id}', 'POST', 6, 1, 1, NOW();

-- 商品管理按钮
INSERT INTO `sys_permission` (`tenant_id`, `parent_id`, `permission_code`, `permission_name`, `permission_type`, `resource_path`, `http_method`, `sort`, `status`, `visible`, `created_at`)
SELECT 1, (SELECT id FROM sys_permission WHERE tenant_id=1 AND permission_code='erp:product' LIMIT 1),
       'erp:product:list', '商品列表', 2, '/api/erp/products/get-product-page', 'GET', 1, 1, 1, NOW()
UNION ALL SELECT 1, (SELECT id FROM sys_permission WHERE tenant_id=1 AND permission_code='erp:product' LIMIT 1),
       'erp:product:detail', '商品详情', 2, '/api/erp/products/get-product/{id}', 'GET', 2, 1, 1, NOW()
UNION ALL SELECT 1, (SELECT id FROM sys_permission WHERE tenant_id=1 AND permission_code='erp:product' LIMIT 1),
       'erp:product:create', '新增商品', 2, '/api/erp/products/create-product', 'POST', 3, 1, 1, NOW()
UNION ALL SELECT 1, (SELECT id FROM sys_permission WHERE tenant_id=1 AND permission_code='erp:product' LIMIT 1),
       'erp:product:update', '编辑商品', 2, '/api/erp/products/update-product/{id}', 'PUT', 4, 1, 1, NOW()
UNION ALL SELECT 1, (SELECT id FROM sys_permission WHERE tenant_id=1 AND permission_code='erp:product' LIMIT 1),
       'erp:product:delete', '删除商品', 2, '/api/erp/products/delete-product/{id}', 'DELETE', 5, 1, 1, NOW();

-- 仓库管理按钮
INSERT INTO `sys_permission` (`tenant_id`, `parent_id`, `permission_code`, `permission_name`, `permission_type`, `resource_path`, `http_method`, `sort`, `status`, `visible`, `created_at`)
SELECT 1, (SELECT id FROM sys_permission WHERE tenant_id=1 AND permission_code='erp:warehouse' LIMIT 1),
       'erp:warehouse:list', '仓库列表', 2, '/api/erp/warehouses/get-warehouse-page', 'GET', 1, 1, 1, NOW()
UNION ALL SELECT 1, (SELECT id FROM sys_permission WHERE tenant_id=1 AND permission_code='erp:warehouse' LIMIT 1),
       'erp:warehouse:detail', '仓库详情', 2, '/api/erp/warehouses/get-warehouse/{id}', 'GET', 2, 1, 1, NOW()
UNION ALL SELECT 1, (SELECT id FROM sys_permission WHERE tenant_id=1 AND permission_code='erp:warehouse' LIMIT 1),
       'erp:warehouse:create', '新增仓库', 2, '/api/erp/warehouses/create-warehouse', 'POST', 3, 1, 1, NOW()
UNION ALL SELECT 1, (SELECT id FROM sys_permission WHERE tenant_id=1 AND permission_code='erp:warehouse' LIMIT 1),
       'erp:warehouse:update', '编辑仓库', 2, '/api/erp/warehouses/update-warehouse/{id}', 'PUT', 4, 1, 1, NOW()
UNION ALL SELECT 1, (SELECT id FROM sys_permission WHERE tenant_id=1 AND permission_code='erp:warehouse' LIMIT 1),
       'erp:warehouse:delete', '删除仓库', 2, '/api/erp/warehouses/delete-warehouse/{id}', 'DELETE', 5, 1, 1, NOW()
UNION ALL SELECT 1, (SELECT id FROM sys_permission WHERE tenant_id=1 AND permission_code='erp:warehouse' LIMIT 1),
       'erp:warehouse:setDefault', '设为默认', 2, '/api/erp/warehouses/get-default-warehouse', 'GET', 6, 1, 1, NOW();

-- 库存管理按钮
INSERT INTO `sys_permission` (`tenant_id`, `parent_id`, `permission_code`, `permission_name`, `permission_type`, `resource_path`, `http_method`, `sort`, `status`, `visible`, `created_at`)
SELECT 1, (SELECT id FROM sys_permission WHERE tenant_id=1 AND permission_code='erp:inventory' LIMIT 1),
       'erp:inventory:list', '库存列表', 2, '/api/erp/inventory/get-inventory-page', 'GET', 1, 1, 1, NOW()
UNION ALL SELECT 1, (SELECT id FROM sys_permission WHERE tenant_id=1 AND permission_code='erp:inventory' LIMIT 1),
       'erp:inventory:query', '查询库存', 2, '/api/erp/inventory/get-inventory', 'GET', 2, 1, 1, NOW()
UNION ALL SELECT 1, (SELECT id FROM sys_permission WHERE tenant_id=1 AND permission_code='erp:inventory' LIMIT 1),
       'erp:inventory:inbound', '入库操作', 2, '/api/erp/inventory/inbound', 'POST', 3, 1, 1, NOW()
UNION ALL SELECT 1, (SELECT id FROM sys_permission WHERE tenant_id=1 AND permission_code='erp:inventory' LIMIT 1),
       'erp:inventory:outbound', '出库操作', 2, '/api/erp/inventory/outbound', 'POST', 4, 1, 1, NOW()
UNION ALL SELECT 1, (SELECT id FROM sys_permission WHERE tenant_id=1 AND permission_code='erp:inventory' LIMIT 1),
       'erp:inventory:alert', '库存预警', 2, '/api/erp/inventory/get-alert-inventories', 'GET', 5, 1, 1, NOW();

-- 供应商管理按钮
INSERT INTO `sys_permission` (`tenant_id`, `parent_id`, `permission_code`, `permission_name`, `permission_type`, `resource_path`, `http_method`, `sort`, `status`, `visible`, `created_at`)
SELECT 1, (SELECT id FROM sys_permission WHERE tenant_id=1 AND permission_code='erp:supplier' LIMIT 1),
       'erp:supplier:list', '供应商列表', 2, '/api/erp/suppliers/get-supplier-page', 'GET', 1, 1, 1, NOW()
UNION ALL SELECT 1, (SELECT id FROM sys_permission WHERE tenant_id=1 AND permission_code='erp:supplier' LIMIT 1),
       'erp:supplier:detail', '供应商详情', 2, '/api/erp/suppliers/get-supplier/{id}', 'GET', 2, 1, 1, NOW()
UNION ALL SELECT 1, (SELECT id FROM sys_permission WHERE tenant_id=1 AND permission_code='erp:supplier' LIMIT 1),
       'erp:supplier:create', '新增供应商', 2, '/api/erp/suppliers/create-supplier', 'POST', 3, 1, 1, NOW()
UNION ALL SELECT 1, (SELECT id FROM sys_permission WHERE tenant_id=1 AND permission_code='erp:supplier' LIMIT 1),
       'erp:supplier:update', '编辑供应商', 2, '/api/erp/suppliers/update-supplier/{id}', 'PUT', 4, 1, 1, NOW()
UNION ALL SELECT 1, (SELECT id FROM sys_permission WHERE tenant_id=1 AND permission_code='erp:supplier' LIMIT 1),
       'erp:supplier:delete', '删除供应商', 2, '/api/erp/suppliers/delete-supplier/{id}', 'DELETE', 5, 1, 1, NOW();

-- 客户管理按钮
INSERT INTO `sys_permission` (`tenant_id`, `parent_id`, `permission_code`, `permission_name`, `permission_type`, `resource_path`, `http_method`, `sort`, `status`, `visible`, `created_at`)
SELECT 1, (SELECT id FROM sys_permission WHERE tenant_id=1 AND permission_code='erp:customer' LIMIT 1),
       'erp:customer:list', '客户列表', 2, '/api/erp/customers/get-customer-page', 'GET', 1, 1, 1, NOW()
UNION ALL SELECT 1, (SELECT id FROM sys_permission WHERE tenant_id=1 AND permission_code='erp:customer' LIMIT 1),
       'erp:customer:detail', '客户详情', 2, '/api/erp/customers/get-customer/{id}', 'GET', 2, 1, 1, NOW()
UNION ALL SELECT 1, (SELECT id FROM sys_permission WHERE tenant_id=1 AND permission_code='erp:customer' LIMIT 1),
       'erp:customer:create', '新增客户', 2, '/api/erp/customers/create-customer', 'POST', 3, 1, 1, NOW()
UNION ALL SELECT 1, (SELECT id FROM sys_permission WHERE tenant_id=1 AND permission_code='erp:customer' LIMIT 1),
       'erp:customer:update', '编辑客户', 2, '/api/erp/customers/update-customer/{id}', 'PUT', 4, 1, 1, NOW()
UNION ALL SELECT 1, (SELECT id FROM sys_permission WHERE tenant_id=1 AND permission_code='erp:customer' LIMIT 1),
       'erp:customer:delete', '删除客户', 2, '/api/erp/customers/delete-customer/{id}', 'DELETE', 5, 1, 1, NOW();

-- 采购订单按钮
INSERT INTO `sys_permission` (`tenant_id`, `parent_id`, `permission_code`, `permission_name`, `permission_type`, `resource_path`, `http_method`, `sort`, `status`, `visible`, `created_at`)
SELECT 1, (SELECT id FROM sys_permission WHERE tenant_id=1 AND permission_code='erp:purchaseOrder' LIMIT 1),
       'erp:purchaseOrder:list', '订单列表', 2, '/api/erp/purchase-orders/get-purchase-order-page', 'GET', 1, 1, 1, NOW()
UNION ALL SELECT 1, (SELECT id FROM sys_permission WHERE tenant_id=1 AND permission_code='erp:purchaseOrder' LIMIT 1),
       'erp:purchaseOrder:detail', '订单详情', 2, '/api/erp/purchase-orders/get-purchase-order/{id}', 'GET', 2, 1, 1, NOW()
UNION ALL SELECT 1, (SELECT id FROM sys_permission WHERE tenant_id=1 AND permission_code='erp:purchaseOrder' LIMIT 1),
       'erp:purchaseOrder:create', '新建订单', 2, '/api/erp/purchase-orders/create-purchase-order', 'POST', 3, 1, 1, NOW()
UNION ALL SELECT 1, (SELECT id FROM sys_permission WHERE tenant_id=1 AND permission_code='erp:purchaseOrder' LIMIT 1),
       'erp:purchaseOrder:update', '编辑订单', 2, '/api/erp/purchase-orders/update-purchase-order/{id}', 'PUT', 4, 1, 1, NOW()
UNION ALL SELECT 1, (SELECT id FROM sys_permission WHERE tenant_id=1 AND permission_code='erp:purchaseOrder' LIMIT 1),
       'erp:purchaseOrder:submit', '提交审核', 2, '/api/erp/purchase-orders/submit-for-approval/{id}', 'POST', 5, 1, 1, NOW()
UNION ALL SELECT 1, (SELECT id FROM sys_permission WHERE tenant_id=1 AND permission_code='erp:purchaseOrder' LIMIT 1),
       'erp:purchaseOrder:approve', '审核订单', 2, '/api/erp/purchase-orders/approve-order/{id}', 'POST', 6, 1, 1, NOW()
UNION ALL SELECT 1, (SELECT id FROM sys_permission WHERE tenant_id=1 AND permission_code='erp:purchaseOrder' LIMIT 1),
       'erp:purchaseOrder:cancel', '取消订单', 2, '/api/erp/purchase-orders/cancel-order/{id}', 'POST', 7, 1, 1, NOW();

-- 销售订单按钮
INSERT INTO `sys_permission` (`tenant_id`, `parent_id`, `permission_code`, `permission_name`, `permission_type`, `resource_path`, `http_method`, `sort`, `status`, `visible`, `created_at`)
SELECT 1, (SELECT id FROM sys_permission WHERE tenant_id=1 AND permission_code='erp:salesOrder' LIMIT 1),
       'erp:salesOrder:list', '订单列表', 2, '/api/erp/sales-orders/get-sales-order-page', 'GET', 1, 1, 1, NOW()
UNION ALL SELECT 1, (SELECT id FROM sys_permission WHERE tenant_id=1 AND permission_code='erp:salesOrder' LIMIT 1),
       'erp:salesOrder:detail', '订单详情', 2, '/api/erp/sales-orders/get-sales-order/{id}', 'GET', 2, 1, 1, NOW()
UNION ALL SELECT 1, (SELECT id FROM sys_permission WHERE tenant_id=1 AND permission_code='erp:salesOrder' LIMIT 1),
       'erp:salesOrder:create', '新建订单', 2, '/api/erp/sales-orders/create-sales-order', 'POST', 3, 1, 1, NOW()
UNION ALL SELECT 1, (SELECT id FROM sys_permission WHERE tenant_id=1 AND permission_code='erp:salesOrder' LIMIT 1),
       'erp:salesOrder:update', '编辑订单', 2, '/api/erp/sales-orders/update-sales-order/{id}', 'PUT', 4, 1, 1, NOW()
UNION ALL SELECT 1, (SELECT id FROM sys_permission WHERE tenant_id=1 AND permission_code='erp:salesOrder' LIMIT 1),
       'erp:salesOrder:submit', '提交审核', 2, '/api/erp/sales-orders/submit-for-approval/{id}', 'POST', 5, 1, 1, NOW()
UNION ALL SELECT 1, (SELECT id FROM sys_permission WHERE tenant_id=1 AND permission_code='erp:salesOrder' LIMIT 1),
       'erp:salesOrder:approve', '审核订单', 2, '/api/erp/sales-orders/approve-order/{id}', 'POST', 6, 1, 1, NOW()
UNION ALL SELECT 1, (SELECT id FROM sys_permission WHERE tenant_id=1 AND permission_code='erp:salesOrder' LIMIT 1),
       'erp:salesOrder:cancel', '取消订单', 2, '/api/erp/sales-orders/cancel-order/{id}', 'POST', 7, 1, 1, NOW();

-- 销售出库按钮
INSERT INTO `sys_permission` (`tenant_id`, `parent_id`, `permission_code`, `permission_name`, `permission_type`, `resource_path`, `http_method`, `sort`, `status`, `visible`, `created_at`)
SELECT 1, (SELECT id FROM sys_permission WHERE tenant_id=1 AND permission_code='erp:salesShipment' LIMIT 1),
       'erp:salesShipment:list', '出库单列表', 2, '/api/erp/sales-shipments/get-sales-shipment-page', 'GET', 1, 1, 1, NOW()
UNION ALL SELECT 1, (SELECT id FROM sys_permission WHERE tenant_id=1 AND permission_code='erp:salesShipment' LIMIT 1),
       'erp:salesShipment:detail', '出库单详情', 2, '/api/erp/sales-shipments/get-sales-shipment/{id}', 'GET', 2, 1, 1, NOW()
UNION ALL SELECT 1, (SELECT id FROM sys_permission WHERE tenant_id=1 AND permission_code='erp:salesShipment' LIMIT 1),
       'erp:salesShipment:shippable', '可发货商品', 2, '/api/erp/sales-shipments/get-shippable-items/{orderId}', 'GET', 3, 1, 1, NOW()
UNION ALL SELECT 1, (SELECT id FROM sys_permission WHERE tenant_id=1 AND permission_code='erp:salesShipment' LIMIT 1),
       'erp:salesShipment:create', '新建出库单', 2, '/api/erp/sales-shipments/create-sales-shipment', 'POST', 4, 1, 1, NOW()
UNION ALL SELECT 1, (SELECT id FROM sys_permission WHERE tenant_id=1 AND permission_code='erp:salesShipment' LIMIT 1),
       'erp:salesShipment:approve', '审核出库', 2, '/api/erp/sales-shipments/approve-shipment/{id}', 'POST', 5, 1, 1, NOW()
UNION ALL SELECT 1, (SELECT id FROM sys_permission WHERE tenant_id=1 AND permission_code='erp:salesShipment' LIMIT 1),
       'erp:salesShipment:cancel', '取消出库', 2, '/api/erp/sales-shipments/cancel-shipment/{id}', 'POST', 6, 1, 1, NOW();

-- ==================== 6. 插入财务管理子菜单和按钮 ====================
-- 财务管理子菜单
INSERT INTO `sys_permission` (`tenant_id`, `parent_id`, `permission_code`, `permission_name`, `permission_type`, `resource_path`, `http_method`, `icon`, `menu_path`, `component`, `sort`, `status`, `visible`, `created_at`)
SELECT 1, (SELECT id FROM sys_permission WHERE tenant_id=1 AND permission_code='finance' LIMIT 1),
       'finance:receivable', '应收账款', 1, '/api/finance/receivables/get-receivable-page', 'GET', 'CreditCard', '/finance/receivable', 'finance/receivable/index', 1, 1, 1, NOW()
UNION ALL SELECT 1, (SELECT id FROM sys_permission WHERE tenant_id=1 AND permission_code='finance' LIMIT 1),
       'finance:payable', '应付账款', 1, '/api/finance/payables/get-payable-page', 'GET', 'Wallet', '/finance/payable', 'finance/payable/index', 2, 1, 1, NOW()
UNION ALL SELECT 1, (SELECT id FROM sys_permission WHERE tenant_id=1 AND permission_code='finance' LIMIT 1),
       'finance:record', '收支记录', 1, '/api/finance/records/get-record-page', 'GET', 'Tickets', '/finance/record', 'finance/record/index', 3, 1, 1, NOW()
UNION ALL SELECT 1, (SELECT id FROM sys_permission WHERE tenant_id=1 AND permission_code='finance' LIMIT 1),
       'finance:bankAccount', '银行账户', 1, '/api/finance/bank-accounts/get-bank-account-page', 'GET', 'Postcard', '/finance/bank-account', 'finance/bank-account/index', 4, 1, 1, NOW();

-- 应收账款按钮
INSERT INTO `sys_permission` (`tenant_id`, `parent_id`, `permission_code`, `permission_name`, `permission_type`, `resource_path`, `http_method`, `sort`, `status`, `visible`, `created_at`)
SELECT 1, (SELECT id FROM sys_permission WHERE tenant_id=1 AND permission_code='finance:receivable' LIMIT 1),
       'finance:receivable:list', '应收列表', 2, '/api/finance/receivables/get-receivable-page', 'GET', 1, 1, 1, NOW()
UNION ALL SELECT 1, (SELECT id FROM sys_permission WHERE tenant_id=1 AND permission_code='finance:receivable' LIMIT 1),
       'finance:receivable:create', '新增应收', 2, '/api/finance/receivables/create-receivable', 'POST', 2, 1, 1, NOW()
UNION ALL SELECT 1, (SELECT id FROM sys_permission WHERE tenant_id=1 AND permission_code='finance:receivable' LIMIT 1),
       'finance:receivable:receive', '收款确认', 2, '/api/finance/receivables/receive-payment/{id}', 'POST', 3, 1, 1, NOW()
UNION ALL SELECT 1, (SELECT id FROM sys_permission WHERE tenant_id=1 AND permission_code='finance:receivable' LIMIT 1),
       'finance:receivable:overdue', '逾期应收', 2, '/api/finance/receivables/get-overdue-receivables', 'GET', 4, 1, 1, NOW();

-- 应付账款按钮
INSERT INTO `sys_permission` (`tenant_id`, `parent_id`, `permission_code`, `permission_name`, `permission_type`, `resource_path`, `http_method`, `sort`, `status`, `visible`, `created_at`)
SELECT 1, (SELECT id FROM sys_permission WHERE tenant_id=1 AND permission_code='finance:payable' LIMIT 1),
       'finance:payable:list', '应付列表', 2, '/api/finance/payables/get-payable-page', 'GET', 1, 1, 1, NOW()
UNION ALL SELECT 1, (SELECT id FROM sys_permission WHERE tenant_id=1 AND permission_code='finance:payable' LIMIT 1),
       'finance:payable:create', '新增应付', 2, '/api/finance/payables/create-payable', 'POST', 2, 1, 1, NOW()
UNION ALL SELECT 1, (SELECT id FROM sys_permission WHERE tenant_id=1 AND permission_code='finance:payable' LIMIT 1),
       'finance:payable:pay', '付款确认', 2, '/api/finance/payables/make-payment/{id}', 'POST', 3, 1, 1, NOW()
UNION ALL SELECT 1, (SELECT id FROM sys_permission WHERE tenant_id=1 AND permission_code='finance:payable' LIMIT 1),
       'finance:payable:overdue', '逾期应付', 2, '/api/finance/payables/get-overdue-payables', 'GET', 4, 1, 1, NOW();

-- 收支记录按钮
INSERT INTO `sys_permission` (`tenant_id`, `parent_id`, `permission_code`, `permission_name`, `permission_type`, `resource_path`, `http_method`, `sort`, `status`, `visible`, `created_at`)
SELECT 1, (SELECT id FROM sys_permission WHERE tenant_id=1 AND permission_code='finance:record' LIMIT 1),
       'finance:record:list', '记录列表', 2, '/api/finance/records/get-record-page', 'GET', 1, 1, 1, NOW()
UNION ALL SELECT 1, (SELECT id FROM sys_permission WHERE tenant_id=1 AND permission_code='finance:record' LIMIT 1),
       'finance:record:create', '新增记录', 2, '/api/finance/records/create-record', 'POST', 2, 1, 1, NOW()
UNION ALL SELECT 1, (SELECT id FROM sys_permission WHERE tenant_id=1 AND permission_code='finance:record' LIMIT 1),
       'finance:record:approve', '审核记录', 2, '/api/finance/records/approve-record/{id}', 'POST', 3, 1, 1, NOW()
UNION ALL SELECT 1, (SELECT id FROM sys_permission WHERE tenant_id=1 AND permission_code='finance:record' LIMIT 1),
       'finance:record:cancel', '取消记录', 2, '/api/finance/records/cancel-record/{id}', 'POST', 4, 1, 1, NOW();

-- 银行账户按钮
INSERT INTO `sys_permission` (`tenant_id`, `parent_id`, `permission_code`, `permission_name`, `permission_type`, `resource_path`, `http_method`, `sort`, `status`, `visible`, `created_at`)
SELECT 1, (SELECT id FROM sys_permission WHERE tenant_id=1 AND permission_code='finance:bankAccount' LIMIT 1),
       'finance:bankAccount:list', '账户列表', 2, '/api/finance/bank-accounts/get-bank-account-page', 'GET', 1, 1, 1, NOW()
UNION ALL SELECT 1, (SELECT id FROM sys_permission WHERE tenant_id=1 AND permission_code='finance:bankAccount' LIMIT 1),
       'finance:bankAccount:all', '所有账户', 2, '/api/finance/bank-accounts/get-all-bank-accounts', 'GET', 2, 1, 1, NOW()
UNION ALL SELECT 1, (SELECT id FROM sys_permission WHERE tenant_id=1 AND permission_code='finance:bankAccount' LIMIT 1),
       'finance:bankAccount:create', '新增账户', 2, '/api/finance/bank-accounts/create-bank-account', 'POST', 3, 1, 1, NOW()
UNION ALL SELECT 1, (SELECT id FROM sys_permission WHERE tenant_id=1 AND permission_code='finance:bankAccount' LIMIT 1),
       'finance:bankAccount:update', '编辑账户', 2, '/api/finance/bank-accounts/update-bank-account/{id}', 'PUT', 4, 1, 1, NOW()
UNION ALL SELECT 1, (SELECT id FROM sys_permission WHERE tenant_id=1 AND permission_code='finance:bankAccount' LIMIT 1),
       'finance:bankAccount:delete', '删除账户', 2, '/api/finance/bank-accounts/delete-bank-account/{id}', 'DELETE', 5, 1, 1, NOW();

-- 报表统计按钮
INSERT INTO `sys_permission` (`tenant_id`, `parent_id`, `permission_code`, `permission_name`, `permission_type`, `resource_path`, `http_method`, `sort`, `status`, `visible`, `created_at`)
SELECT 1, (SELECT id FROM sys_permission WHERE tenant_id=1 AND permission_code='erp:report' LIMIT 1),
       'erp:report:dashboard', 'Dashboard', 2, '/api/erp/reports/dashboard', 'GET', 1, 1, 1, NOW()
UNION ALL SELECT 1, (SELECT id FROM sys_permission WHERE tenant_id=1 AND permission_code='erp:report' LIMIT 1),
       'erp:report:sales', '销售报表', 2, '/api/erp/reports/sales', 'GET', 2, 1, 1, NOW()
UNION ALL SELECT 1, (SELECT id FROM sys_permission WHERE tenant_id=1 AND permission_code='erp:report' LIMIT 1),
       'erp:report:purchase', '采购报表', 2, '/api/erp/reports/purchase', 'GET', 3, 1, 1, NOW()
UNION ALL SELECT 1, (SELECT id FROM sys_permission WHERE tenant_id=1 AND permission_code='erp:report' LIMIT 1),
       'erp:report:inventory', '库存报表', 2, '/api/erp/reports/inventory', 'GET', 4, 1, 1, NOW();

-- 系统配置按钮
INSERT INTO `sys_permission` (`tenant_id`, `parent_id`, `permission_code`, `permission_name`, `permission_type`, `resource_path`, `http_method`, `sort`, `status`, `visible`, `created_at`)
SELECT 1, (SELECT id FROM sys_permission WHERE tenant_id=1 AND permission_code='erp:config' LIMIT 1),
       'erp:config:list', '配置列表', 2, '/api/erp/configs/get-config-page', 'GET', 1, 1, 1, NOW()
UNION ALL SELECT 1, (SELECT id FROM sys_permission WHERE tenant_id=1 AND permission_code='erp:config' LIMIT 1),
       'erp:config:create', '新增配置', 2, '/api/erp/configs/create-config', 'POST', 2, 1, 1, NOW()
UNION ALL SELECT 1, (SELECT id FROM sys_permission WHERE tenant_id=1 AND permission_code='erp:config' LIMIT 1),
       'erp:config:update', '编辑配置', 2, '/api/erp/configs/update-config/{id}', 'PUT', 3, 1, 1, NOW()
UNION ALL SELECT 1, (SELECT id FROM sys_permission WHERE tenant_id=1 AND permission_code='erp:config' LIMIT 1),
       'erp:config:delete', '删除配置', 2, '/api/erp/configs/delete-config/{id}', 'DELETE', 4, 1, 1, NOW()
UNION ALL SELECT 1, (SELECT id FROM sys_permission WHERE tenant_id=1 AND permission_code='erp:config' LIMIT 1),
       'erp:config:batchUpdate', '批量更新', 2, '/api/erp/configs/batch-update', 'POST', 5, 1, 1, NOW();

-- ==================== 7. 分配角色权限 ====================
-- 为超级管理员分配所有权限（动态查询 role_id，避免 AUTO_INCREMENT 不一致）
INSERT INTO `sys_role_permission` (`tenant_id`, `role_id`, `permission_id`, `created_at`)
SELECT 1, (SELECT id FROM `sys_role` WHERE `role_code` = 'SUPER_ADMIN' AND `tenant_id` = 1 LIMIT 1), id, NOW()
FROM `sys_permission` WHERE `tenant_id` = 1;

-- 为管理员分配部分权限（排除权限管理）
INSERT INTO `sys_role_permission` (`tenant_id`, `role_id`, `permission_id`, `created_at`)
SELECT 1, (SELECT id FROM `sys_role` WHERE `role_code` = 'ADMIN' AND `tenant_id` = 1 LIMIT 1), id, NOW()
FROM `sys_permission`
WHERE `tenant_id` = 1 AND `permission_code` NOT LIKE 'system:permission%';

-- 为普通用户分配基础权限（仅菜单查看）
INSERT INTO `sys_role_permission` (`tenant_id`, `role_id`, `permission_id`, `created_at`)
SELECT 1, (SELECT id FROM `sys_role` WHERE `role_code` = 'USER' AND `tenant_id` = 1 LIMIT 1), id, NOW()
FROM `sys_permission`
WHERE `tenant_id` = 1 AND `permission_type` = 1;

-- ==================== 8. 分配用户角色 ====================
-- 将 admin 用户(user_id=1)绑定到 SUPER_ADMIN 角色
INSERT INTO `sys_user_role` (`tenant_id`, `user_id`, `role_id`, `created_by`, `created_at`)
SELECT 1, 1, id, 1, NOW()
FROM `sys_role` WHERE `role_code` = 'SUPER_ADMIN' AND `tenant_id` = 1 LIMIT 1;
