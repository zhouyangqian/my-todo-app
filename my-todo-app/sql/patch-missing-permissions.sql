-- ============================================================
-- 补充缺失的权限记录
-- 对比前端路由 (router/index.js) 与 sys_permission 表，
-- 补充 26 个页面菜单权限 (type=1) 及对应的操作按钮权限 (type=2)
-- ============================================================

-- ----------------------------
-- 一、System 模块 - 菜单权限 (parent_id = 1573)
-- ----------------------------
INSERT INTO `sys_permission` VALUES (1814, 1, 1573, 'system:gateway', '网关监控', 1, '/api/gateway/service-statuses', 'GET', 'Monitor', '/system/gateway', 'system/gateway/index', 5, 1, 1, 0, NULL, NOW(), NULL, NOW());
INSERT INTO `sys_permission` VALUES (1815, 1, 1573, 'system:cache', '缓存管理', 1, '/api/gateway/cache/stats', 'GET', 'Coin', '/system/cache', 'system/cache/index', 6, 1, 1, 0, NULL, NOW(), NULL, NOW());
INSERT INTO `sys_permission` VALUES (1816, 1, 1573, 'system:canary', '金丝雀发布', 1, '/api/gateway/canary/config', 'GET', 'Promotion', '/system/canary', 'system/canary/index', 7, 1, 1, 0, NULL, NOW(), NULL, NOW());
INSERT INTO `sys_permission` VALUES (1817, 1, 1573, 'system:monitor', '实时监控', 1, '/api/gateway/metrics', 'GET', 'DataAnalysis', '/system/monitor', 'system/monitor/index', 8, 1, 1, 0, NULL, NOW(), NULL, NOW());
INSERT INTO `sys_permission` VALUES (1818, 1, 1573, 'system:dynamicPermission', '动态权限', 1, '/api/permissions/dynamic/tree', 'GET', 'Setting', '/system/permission-dynamic', 'system/permission-dynamic/index', 9, 1, 1, 0, NULL, NOW(), NULL, NOW());
INSERT INTO `sys_permission` VALUES (1819, 1, 1573, 'system:session', '会话管理', 1, '/api/permissions/sessions/online', 'GET', 'Connection', '/system/session', 'system/session/index', 10, 1, 1, 0, NULL, NOW(), NULL, NOW());
INSERT INTO `sys_permission` VALUES (1820, 1, 1573, 'system:blacklist', '黑名单管理', 1, '/api/permissions/blacklist', 'GET', 'Warning', '/system/blacklist', 'system/blacklist/index', 11, 1, 1, 0, NULL, NOW(), NULL, NOW());
INSERT INTO `sys_permission` VALUES (1821, 1, 1573, 'system:permissionTemplate', '权限模板', 1, '/api/permissions/templates/page', 'GET', 'Document', '/system/permission-template', 'system/permission-template/index', 12, 1, 1, 0, NULL, NOW(), NULL, NOW());
INSERT INTO `sys_permission` VALUES (1822, 1, 1573, 'system:dataRule', '数据权限', 1, '/api/permissions/data-rules/role/{roleId}', 'GET', 'Filter', '/system/data-rule', 'system/data-rule/index', 13, 1, 1, 0, NULL, NOW(), NULL, NOW());
INSERT INTO `sys_permission` VALUES (1823, 1, 1573, 'system:roleInheritance', '角色继承', 1, '/api/permissions/role-inheritance/tree/{roleId}', 'GET', 'Share', '/system/role-inheritance', 'system/role-inheritance/index', 14, 1, 1, 0, NULL, NOW(), NULL, NOW());
INSERT INTO `sys_permission` VALUES (1824, 1, 1573, 'system:menu', '菜单管理', 1, '/api/permissions/menus/tree', 'GET', 'Menu', '/system/menu', 'system/menu/index', 15, 1, 1, 0, NULL, NOW(), NULL, NOW());
INSERT INTO `sys_permission` VALUES (1825, 1, 1573, 'system:tenant', '租户管理', 1, '/api/tenants/page', 'GET', 'OfficeBuilding', '/system/tenant', 'system/tenant/index', 16, 1, 1, 0, NULL, NOW(), NULL, NOW());

-- ----------------------------
-- 二、Dict 模块 - 菜单权限 (parent_id = 1574)
-- ----------------------------
INSERT INTO `sys_permission` VALUES (1826, 1, 1574, 'dict:parameter', '参数管理', 1, '/api/parameter-dictionaries/get-dictionary-page', 'GET', 'Setting', '/dict/parameter', 'dict/parameter/index', 3, 1, 1, 0, NULL, NOW(), NULL, NOW());
INSERT INTO `sys_permission` VALUES (1827, 1, 1574, 'dict:apiMarket', 'API市场', 1, '/api/api-market/definitions/page', 'GET', 'Connection', '/dict/api-market', 'dict/api-market/index', 4, 1, 1, 0, NULL, NOW(), NULL, NOW());
INSERT INTO `sys_permission` VALUES (1828, 1, 1574, 'dict:thirdParty', '第三方API', 1, '/api/third-party/page', 'GET', 'Link', '/dict/third-party', 'dict/third-party/index', 5, 1, 1, 0, NULL, NOW(), NULL, NOW());
INSERT INTO `sys_permission` VALUES (1829, 1, 1574, 'dict:package', 'SaaS套餐', 1, '/api/packages/page', 'GET', 'Box', '/dict/package', 'dict/package/index', 6, 1, 1, 0, NULL, NOW(), NULL, NOW());
INSERT INTO `sys_permission` VALUES (1830, 1, 1574, 'dict:activity', '营销活动', 1, '/api/activities/page', 'GET', 'Present', '/dict/activity', 'dict/activity/index', 7, 1, 1, 0, NULL, NOW(), NULL, NOW());
INSERT INTO `sys_permission` VALUES (1831, 1, 1574, 'dict:trace', '追踪管理', 1, '/api/trace/configs/page', 'GET', 'View', '/dict/trace', 'dict/trace/index', 8, 1, 1, 0, NULL, NOW(), NULL, NOW());
INSERT INTO `sys_permission` VALUES (1832, 1, 1574, 'dict:codegen', '代码生成', 1, '/api/dict/codegen/template/page', 'GET', 'DocumentCopy', '/dict/codegen', 'dict/codegen/index', 9, 1, 1, 0, NULL, NOW(), NULL, NOW());
INSERT INTO `sys_permission` VALUES (1833, 1, 1574, 'dict:errorDoc', '错误文档', 1, '/api/dict/error-doc/category/page', 'GET', 'Warning', '/dict/error-doc', 'dict/error-doc/index', 10, 1, 1, 0, NULL, NOW(), NULL, NOW());

-- ----------------------------
-- 三、Finance 模块 - 菜单权限 (parent_id = 1576)
-- ----------------------------
INSERT INTO `sys_permission` VALUES (1834, 1, 1576, 'finance:invoice', '发票管理', 1, '/api/finance/invoices/get-invoice-page', 'GET', 'Document', '/finance/invoice', 'finance/invoice/index', 5, 1, 1, 0, NULL, NOW(), NULL, NOW());
INSERT INTO `sys_permission` VALUES (1835, 1, 1576, 'finance:cost', '成本核算', 1, '/api/finance/cost/get-config-page', 'GET', 'DataAnalysis', '/finance/cost', 'finance/cost/index', 6, 1, 1, 0, NULL, NOW(), NULL, NOW());
INSERT INTO `sys_permission` VALUES (1836, 1, 1576, 'finance:report', '财务报表', 1, '/api/finance/reports/get-report-list', 'GET', 'TrendCharts', '/finance/report', 'finance/report/index', 7, 1, 1, 0, NULL, NOW(), NULL, NOW());
INSERT INTO `sys_permission` VALUES (1837, 1, 1576, 'finance:bankReconciliation', '银行对账', 1, '/api/finance/bank-reconciliation/page', 'GET', 'Connection', '/finance/bank-reconciliation', 'finance/bank-reconciliation/index', 8, 1, 1, 0, NULL, NOW(), NULL, NOW());
INSERT INTO `sys_permission` VALUES (1838, 1, 1576, 'finance:budget', '预算管理', 1, '/api/finance/budgets/page', 'GET', 'Money', '/finance/budget', 'finance/budget/index', 9, 1, 1, 0, NULL, NOW(), NULL, NOW());

-- ----------------------------
-- 四、System 模块 - 按钮权限 (type=2)
-- ----------------------------

-- 网关监控 (parent: 1814)
INSERT INTO `sys_permission` VALUES (1839, 1, 1814, 'system:gateway:status', '服务状态', 2, '/api/gateway/service-statuses', 'GET', NULL, NULL, NULL, 1, 1, 1, 0, NULL, NOW(), NULL, NOW());
INSERT INTO `sys_permission` VALUES (1840, 1, 1814, 'system:gateway:cacheStats', '缓存统计', 2, '/api/gateway/cache/stats', 'GET', NULL, NULL, NULL, 2, 1, 1, 0, NULL, NOW(), NULL, NOW());
INSERT INTO `sys_permission` VALUES (1841, 1, 1814, 'system:gateway:evictCache', '清除缓存', 2, '/api/gateway/cache/evict', 'DELETE', NULL, NULL, NULL, 3, 1, 1, 0, NULL, NOW(), NULL, NOW());
INSERT INTO `sys_permission` VALUES (1842, 1, 1814, 'system:gateway:metrics', '监控指标', 2, '/api/gateway/metrics', 'GET', NULL, NULL, NULL, 4, 1, 1, 0, NULL, NOW(), NULL, NOW());
INSERT INTO `sys_permission` VALUES (1843, 1, 1814, 'system:gateway:dashboard', '仪表盘', 2, '/api/gateway/dashboard', 'GET', NULL, NULL, NULL, 5, 1, 1, 0, NULL, NOW(), NULL, NOW());

-- 缓存管理 (parent: 1815)
INSERT INTO `sys_permission` VALUES (1844, 1, 1815, 'system:cache:stats', '缓存统计', 2, '/api/gateway/cache/stats', 'GET', NULL, NULL, NULL, 1, 1, 1, 0, NULL, NOW(), NULL, NOW());
INSERT INTO `sys_permission` VALUES (1845, 1, 1815, 'system:cache:evict', '清除缓存', 2, '/api/gateway/cache/evict', 'DELETE', NULL, NULL, NULL, 2, 1, 1, 0, NULL, NOW(), NULL, NOW());

-- 金丝雀发布 (parent: 1816)
INSERT INTO `sys_permission` VALUES (1846, 1, 1816, 'system:canary:config', '金丝雀配置', 2, '/api/gateway/canary/config', 'GET', NULL, NULL, NULL, 1, 1, 1, 0, NULL, NOW(), NULL, NOW());
INSERT INTO `sys_permission` VALUES (1847, 1, 1816, 'system:canary:update', '更新配置', 2, '/api/gateway/canary/config', 'PUT', NULL, NULL, NULL, 2, 1, 1, 0, NULL, NOW(), NULL, NOW());

-- 实时监控 (parent: 1817)
INSERT INTO `sys_permission` VALUES (1848, 1, 1817, 'system:monitor:metrics', '监控指标', 2, '/api/gateway/metrics', 'GET', NULL, NULL, NULL, 1, 1, 1, 0, NULL, NOW(), NULL, NOW());
INSERT INTO `sys_permission` VALUES (1849, 1, 1817, 'system:monitor:slowApis', '慢接口', 2, '/api/gateway/slow-apis', 'GET', NULL, NULL, NULL, 2, 1, 1, 0, NULL, NOW(), NULL, NOW());
INSERT INTO `sys_permission` VALUES (1850, 1, 1817, 'system:monitor:errorApis', '错误接口', 2, '/api/gateway/error-apis', 'GET', NULL, NULL, NULL, 3, 1, 1, 0, NULL, NOW(), NULL, NOW());

-- 动态权限 (parent: 1818)
INSERT INTO `sys_permission` VALUES (1851, 1, 1818, 'system:dynamicPermission:tree', '权限树', 2, '/api/permissions/dynamic/tree', 'GET', NULL, NULL, NULL, 1, 1, 1, 0, NULL, NOW(), NULL, NOW());
INSERT INTO `sys_permission` VALUES (1852, 1, 1818, 'system:dynamicPermission:create', '新增动态权限', 2, '/api/permissions/dynamic/create', 'POST', NULL, NULL, NULL, 2, 1, 1, 0, NULL, NOW(), NULL, NOW());
INSERT INTO `sys_permission` VALUES (1853, 1, 1818, 'system:dynamicPermission:update', '编辑动态权限', 2, '/api/permissions/dynamic/update/{id}', 'PUT', NULL, NULL, NULL, 3, 1, 1, 0, NULL, NOW(), NULL, NOW());
INSERT INTO `sys_permission` VALUES (1854, 1, 1818, 'system:dynamicPermission:delete', '删除动态权限', 2, '/api/permissions/dynamic/delete/{id}', 'DELETE', NULL, NULL, NULL, 4, 1, 1, 0, NULL, NOW(), NULL, NOW());
INSERT INTO `sys_permission` VALUES (1855, 1, 1818, 'system:dynamicPermission:clearCache', '清除缓存', 2, '/api/permissions/dynamic/clear-cache', 'POST', NULL, NULL, NULL, 5, 1, 1, 0, NULL, NOW(), NULL, NOW());

-- 会话管理 (parent: 1819)
INSERT INTO `sys_permission` VALUES (1856, 1, 1819, 'system:session:list', '在线会话', 2, '/api/permissions/sessions/online', 'GET', NULL, NULL, NULL, 1, 1, 1, 0, NULL, NOW(), NULL, NOW());
INSERT INTO `sys_permission` VALUES (1857, 1, 1819, 'system:session:kick', '踢出会话', 2, '/api/permissions/sessions/kick/{sessionId}', 'POST', NULL, NULL, NULL, 2, 1, 1, 0, NULL, NOW(), NULL, NOW());
INSERT INTO `sys_permission` VALUES (1858, 1, 1819, 'system:session:kickUser', '踢出用户', 2, '/api/permissions/sessions/kick-user/{userId}', 'POST', NULL, NULL, NULL, 3, 1, 1, 0, NULL, NOW(), NULL, NOW());
INSERT INTO `sys_permission` VALUES (1859, 1, 1819, 'system:session:clean', '清理过期', 2, '/api/permissions/sessions/clean-expired', 'POST', NULL, NULL, NULL, 4, 1, 1, 0, NULL, NOW(), NULL, NOW());

-- 黑名单管理 (parent: 1820)
INSERT INTO `sys_permission` VALUES (1860, 1, 1820, 'system:blacklist:list', '黑名单列表', 2, '/api/permissions/blacklist', 'GET', NULL, NULL, NULL, 1, 1, 1, 0, NULL, NOW(), NULL, NOW());
INSERT INTO `sys_permission` VALUES (1861, 1, 1820, 'system:blacklist:create', '新增黑名单', 2, '/api/permissions/blacklist', 'POST', NULL, NULL, NULL, 2, 1, 1, 0, NULL, NOW(), NULL, NOW());
INSERT INTO `sys_permission` VALUES (1862, 1, 1820, 'system:blacklist:delete', '移除黑名单', 2, '/api/permissions/blacklist/{id}', 'DELETE', NULL, NULL, NULL, 3, 1, 1, 0, NULL, NOW(), NULL, NOW());
INSERT INTO `sys_permission` VALUES (1863, 1, 1820, 'system:blacklist:check', '检查黑名单', 2, '/api/permissions/blacklist/check', 'GET', NULL, NULL, NULL, 4, 1, 1, 0, NULL, NOW(), NULL, NOW());

-- 权限模板 (parent: 1821)
INSERT INTO `sys_permission` VALUES (1864, 1, 1821, 'system:permissionTemplate:list', '模板列表', 2, '/api/permissions/templates/page', 'GET', NULL, NULL, NULL, 1, 1, 1, 0, NULL, NOW(), NULL, NOW());
INSERT INTO `sys_permission` VALUES (1865, 1, 1821, 'system:permissionTemplate:detail', '模板详情', 2, '/api/permissions/templates/detail/{id}', 'GET', NULL, NULL, NULL, 2, 1, 1, 0, NULL, NOW(), NULL, NOW());
INSERT INTO `sys_permission` VALUES (1866, 1, 1821, 'system:permissionTemplate:create', '新增模板', 2, '/api/permissions/templates/create', 'POST', NULL, NULL, NULL, 3, 1, 1, 0, NULL, NOW(), NULL, NOW());
INSERT INTO `sys_permission` VALUES (1867, 1, 1821, 'system:permissionTemplate:update', '编辑模板', 2, '/api/permissions/templates/update/{id}', 'PUT', NULL, NULL, NULL, 4, 1, 1, 0, NULL, NOW(), NULL, NOW());
INSERT INTO `sys_permission` VALUES (1868, 1, 1821, 'system:permissionTemplate:delete', '删除模板', 2, '/api/permissions/templates/delete/{id}', 'DELETE', NULL, NULL, NULL, 5, 1, 1, 0, NULL, NOW(), NULL, NOW());
INSERT INTO `sys_permission` VALUES (1869, 1, 1821, 'system:permissionTemplate:apply', '应用模板', 2, '/api/permissions/templates/apply-to-role', 'POST', NULL, NULL, NULL, 6, 1, 1, 0, NULL, NOW(), NULL, NOW());

-- 数据权限 (parent: 1822)
INSERT INTO `sys_permission` VALUES (1870, 1, 1822, 'system:dataRule:list', '数据权限列表', 2, '/api/permissions/data-rules/role/{roleId}', 'GET', NULL, NULL, NULL, 1, 1, 1, 0, NULL, NOW(), NULL, NOW());
INSERT INTO `sys_permission` VALUES (1871, 1, 1822, 'system:dataRule:create', '新增数据权限', 2, '/api/permissions/data-rules/create', 'POST', NULL, NULL, NULL, 2, 1, 1, 0, NULL, NOW(), NULL, NOW());
INSERT INTO `sys_permission` VALUES (1872, 1, 1822, 'system:dataRule:update', '编辑数据权限', 2, '/api/permissions/data-rules/update/{id}', 'PUT', NULL, NULL, NULL, 3, 1, 1, 0, NULL, NOW(), NULL, NOW());
INSERT INTO `sys_permission` VALUES (1873, 1, 1822, 'system:dataRule:delete', '删除数据权限', 2, '/api/permissions/data-rules/delete/{id}', 'DELETE', NULL, NULL, NULL, 4, 1, 1, 0, NULL, NOW(), NULL, NOW());

-- 角色继承 (parent: 1823)
INSERT INTO `sys_permission` VALUES (1874, 1, 1823, 'system:roleInheritance:tree', '继承树', 2, '/api/permissions/role-inheritance/tree/{roleId}', 'GET', NULL, NULL, NULL, 1, 1, 1, 0, NULL, NOW(), NULL, NOW());
INSERT INTO `sys_permission` VALUES (1875, 1, 1823, 'system:roleInheritance:setParent', '设置父角色', 2, '/api/permissions/role-inheritance/set-parent', 'POST', NULL, NULL, NULL, 2, 1, 1, 0, NULL, NOW(), NULL, NOW());
INSERT INTO `sys_permission` VALUES (1876, 1, 1823, 'system:roleInheritance:removeParent', '移除父角色', 2, '/api/permissions/role-inheritance/remove-parent', 'DELETE', NULL, NULL, NULL, 3, 1, 1, 0, NULL, NOW(), NULL, NOW());
INSERT INTO `sys_permission` VALUES (1877, 1, 1823, 'system:roleInheritance:effectivePermissions', '有效权限', 2, '/api/permissions/role-inheritance/effective-permissions/{roleId}', 'GET', NULL, NULL, NULL, 4, 1, 1, 0, NULL, NOW(), NULL, NOW());

-- 菜单管理 (parent: 1824)
INSERT INTO `sys_permission` VALUES (1878, 1, 1824, 'system:menu:tree', '菜单树', 2, '/api/permissions/menus/tree', 'GET', NULL, NULL, NULL, 1, 1, 1, 0, NULL, NOW(), NULL, NOW());
INSERT INTO `sys_permission` VALUES (1879, 1, 1824, 'system:menu:create', '新增菜单', 2, '/api/permissions/menus/create', 'POST', NULL, NULL, NULL, 2, 1, 1, 0, NULL, NOW(), NULL, NOW());
INSERT INTO `sys_permission` VALUES (1880, 1, 1824, 'system:menu:update', '编辑菜单', 2, '/api/permissions/menus/update/{id}', 'PUT', NULL, NULL, NULL, 3, 1, 1, 0, NULL, NOW(), NULL, NOW());
INSERT INTO `sys_permission` VALUES (1881, 1, 1824, 'system:menu:delete', '删除菜单', 2, '/api/permissions/menus/delete/{id}', 'DELETE', NULL, NULL, NULL, 4, 1, 1, 0, NULL, NOW(), NULL, NOW());
INSERT INTO `sys_permission` VALUES (1882, 1, 1824, 'system:menu:assignRole', '分配角色菜单', 2, '/api/permissions/menus/assign-role', 'POST', NULL, NULL, NULL, 5, 1, 1, 0, NULL, NOW(), NULL, NOW());

-- 租户管理 (parent: 1825)
INSERT INTO `sys_permission` VALUES (1883, 1, 1825, 'system:tenant:list', '租户列表', 2, '/api/tenants/page', 'GET', NULL, NULL, NULL, 1, 1, 1, 0, NULL, NOW(), NULL, NOW());
INSERT INTO `sys_permission` VALUES (1884, 1, 1825, 'system:tenant:create', '新增租户', 2, '/api/tenants/create', 'POST', NULL, NULL, NULL, 2, 1, 1, 0, NULL, NOW(), NULL, NOW());
INSERT INTO `sys_permission` VALUES (1885, 1, 1825, 'system:tenant:update', '编辑租户', 2, '/api/tenants/update/{id}', 'PUT', NULL, NULL, NULL, 3, 1, 1, 0, NULL, NOW(), NULL, NOW());
INSERT INTO `sys_permission` VALUES (1886, 1, 1825, 'system:tenant:delete', '删除租户', 2, '/api/tenants/delete/{id}', 'DELETE', NULL, NULL, NULL, 4, 1, 1, 0, NULL, NOW(), NULL, NOW());

-- ----------------------------
-- 五、Dict 模块 - 按钮权限 (type=2)
-- ----------------------------

-- 参数管理 (parent: 1826)
INSERT INTO `sys_permission` VALUES (1887, 1, 1826, 'dict:parameter:categoryList', '分类列表', 2, '/api/parameter-categories/get-category-page', 'GET', NULL, NULL, NULL, 1, 1, 1, 0, NULL, NOW(), NULL, NOW());
INSERT INTO `sys_permission` VALUES (1888, 1, 1826, 'dict:parameter:categoryCreate', '新增分类', 2, '/api/parameter-categories/create-category', 'POST', NULL, NULL, NULL, 2, 1, 1, 0, NULL, NOW(), NULL, NOW());
INSERT INTO `sys_permission` VALUES (1889, 1, 1826, 'dict:parameter:categoryUpdate', '编辑分类', 2, '/api/parameter-categories/update-category/{id}', 'PUT', NULL, NULL, NULL, 3, 1, 1, 0, NULL, NOW(), NULL, NOW());
INSERT INTO `sys_permission` VALUES (1890, 1, 1826, 'dict:parameter:categoryDelete', '删除分类', 2, '/api/parameter-categories/delete-category/{id}', 'DELETE', NULL, NULL, NULL, 4, 1, 1, 0, NULL, NOW(), NULL, NOW());
INSERT INTO `sys_permission` VALUES (1891, 1, 1826, 'dict:parameter:list', '字典列表', 2, '/api/parameter-dictionaries/get-dictionary-page', 'GET', NULL, NULL, NULL, 5, 1, 1, 0, NULL, NOW(), NULL, NOW());
INSERT INTO `sys_permission` VALUES (1892, 1, 1826, 'dict:parameter:create', '新增字典', 2, '/api/parameter-dictionaries/create-dictionary', 'POST', NULL, NULL, NULL, 6, 1, 1, 0, NULL, NOW(), NULL, NOW());
INSERT INTO `sys_permission` VALUES (1893, 1, 1826, 'dict:parameter:update', '编辑字典', 2, '/api/parameter-dictionaries/update-dictionary/{id}', 'PUT', NULL, NULL, NULL, 7, 1, 1, 0, NULL, NOW(), NULL, NOW());
INSERT INTO `sys_permission` VALUES (1894, 1, 1826, 'dict:parameter:delete', '删除字典', 2, '/api/parameter-dictionaries/delete-dictionary/{id}', 'DELETE', NULL, NULL, NULL, 8, 1, 1, 0, NULL, NOW(), NULL, NOW());

-- API市场 (parent: 1827)
INSERT INTO `sys_permission` VALUES (1895, 1, 1827, 'dict:apiMarket:list', 'API列表', 2, '/api/api-market/definitions/page', 'GET', NULL, NULL, NULL, 1, 1, 1, 0, NULL, NOW(), NULL, NOW());
INSERT INTO `sys_permission` VALUES (1896, 1, 1827, 'dict:apiMarket:detail', 'API详情', 2, '/api/api-market/definitions/{id}', 'GET', NULL, NULL, NULL, 2, 1, 1, 0, NULL, NOW(), NULL, NOW());
INSERT INTO `sys_permission` VALUES (1897, 1, 1827, 'dict:apiMarket:create', '新增API', 2, '/api/api-market/definitions/create', 'POST', NULL, NULL, NULL, 3, 1, 1, 0, NULL, NOW(), NULL, NOW());
INSERT INTO `sys_permission` VALUES (1898, 1, 1827, 'dict:apiMarket:update', '编辑API', 2, '/api/api-market/definitions/update/{id}', 'PUT', NULL, NULL, NULL, 4, 1, 1, 0, NULL, NOW(), NULL, NOW());
INSERT INTO `sys_permission` VALUES (1899, 1, 1827, 'dict:apiMarket:delete', '删除API', 2, '/api/api-market/definitions/delete/{id}', 'DELETE', NULL, NULL, NULL, 5, 1, 1, 0, NULL, NOW(), NULL, NOW());
INSERT INTO `sys_permission` VALUES (1900, 1, 1827, 'dict:apiMarket:subscribe', '订阅API', 2, '/api/api-market/subscriptions/subscribe', 'POST', NULL, NULL, NULL, 6, 1, 1, 0, NULL, NOW(), NULL, NOW());
INSERT INTO `sys_permission` VALUES (1901, 1, 1827, 'dict:apiMarket:unsubscribe', '取消订阅', 2, '/api/api-market/subscriptions/{id}', 'DELETE', NULL, NULL, NULL, 7, 1, 1, 0, NULL, NOW(), NULL, NOW());

-- 第三方API (parent: 1828)
INSERT INTO `sys_permission` VALUES (1902, 1, 1828, 'dict:thirdParty:list', '第三方API列表', 2, '/api/third-party/page', 'GET', NULL, NULL, NULL, 1, 1, 1, 0, NULL, NOW(), NULL, NOW());
INSERT INTO `sys_permission` VALUES (1903, 1, 1828, 'dict:thirdParty:create', '新增第三方API', 2, '/api/third-party/create', 'POST', NULL, NULL, NULL, 2, 1, 1, 0, NULL, NOW(), NULL, NOW());
INSERT INTO `sys_permission` VALUES (1904, 1, 1828, 'dict:thirdParty:update', '编辑第三方API', 2, '/api/third-party/update/{id}', 'PUT', NULL, NULL, NULL, 3, 1, 1, 0, NULL, NOW(), NULL, NOW());
INSERT INTO `sys_permission` VALUES (1905, 1, 1828, 'dict:thirdParty:delete', '删除第三方API', 2, '/api/third-party/delete/{id}', 'DELETE', NULL, NULL, NULL, 4, 1, 1, 0, NULL, NOW(), NULL, NOW());
INSERT INTO `sys_permission` VALUES (1906, 1, 1828, 'dict:thirdParty:callLogs', '调用日志', 2, '/api/third-party/call-logs/page', 'GET', NULL, NULL, NULL, 5, 1, 1, 0, NULL, NOW(), NULL, NOW());
INSERT INTO `sys_permission` VALUES (1907, 1, 1828, 'dict:thirdParty:healthCheck', '健康检查', 2, '/api/third-party/health-check/{id}', 'GET', NULL, NULL, NULL, 6, 1, 1, 0, NULL, NOW(), NULL, NOW());

-- SaaS套餐 (parent: 1829)
INSERT INTO `sys_permission` VALUES (1908, 1, 1829, 'dict:package:list', '套餐列表', 2, '/api/packages/page', 'GET', NULL, NULL, NULL, 1, 1, 1, 0, NULL, NOW(), NULL, NOW());
INSERT INTO `sys_permission` VALUES (1909, 1, 1829, 'dict:package:create', '新增套餐', 2, '/api/packages/create', 'POST', NULL, NULL, NULL, 2, 1, 1, 0, NULL, NOW(), NULL, NOW());
INSERT INTO `sys_permission` VALUES (1910, 1, 1829, 'dict:package:update', '编辑套餐', 2, '/api/packages/update/{id}', 'PUT', NULL, NULL, NULL, 3, 1, 1, 0, NULL, NOW(), NULL, NOW());
INSERT INTO `sys_permission` VALUES (1911, 1, 1829, 'dict:package:delete', '删除套餐', 2, '/api/packages/delete/{id}', 'DELETE', NULL, NULL, NULL, 4, 1, 1, 0, NULL, NOW(), NULL, NOW());
INSERT INTO `sys_permission` VALUES (1912, 1, 1829, 'dict:package:subscribe', '订阅套餐', 2, '/api/packages/subscribe', 'POST', NULL, NULL, NULL, 5, 1, 1, 0, NULL, NOW(), NULL, NOW());

-- 营销活动 (parent: 1830)
INSERT INTO `sys_permission` VALUES (1913, 1, 1830, 'dict:activity:list', '活动列表', 2, '/api/activities/page', 'GET', NULL, NULL, NULL, 1, 1, 1, 0, NULL, NOW(), NULL, NOW());
INSERT INTO `sys_permission` VALUES (1914, 1, 1830, 'dict:activity:create', '新增活动', 2, '/api/activities/create', 'POST', NULL, NULL, NULL, 2, 1, 1, 0, NULL, NOW(), NULL, NOW());
INSERT INTO `sys_permission` VALUES (1915, 1, 1830, 'dict:activity:update', '编辑活动', 2, '/api/activities/update/{id}', 'PUT', NULL, NULL, NULL, 3, 1, 1, 0, NULL, NOW(), NULL, NOW());
INSERT INTO `sys_permission` VALUES (1916, 1, 1830, 'dict:activity:delete', '删除活动', 2, '/api/activities/delete/{id}', 'DELETE', NULL, NULL, NULL, 4, 1, 1, 0, NULL, NOW(), NULL, NOW());

-- 追踪管理 (parent: 1831)
INSERT INTO `sys_permission` VALUES (1917, 1, 1831, 'dict:trace:configList', '追踪配置列表', 2, '/api/trace/configs/page', 'GET', NULL, NULL, NULL, 1, 1, 1, 0, NULL, NOW(), NULL, NOW());
INSERT INTO `sys_permission` VALUES (1918, 1, 1831, 'dict:trace:configCreate', '新增追踪配置', 2, '/api/trace/configs/create', 'POST', NULL, NULL, NULL, 2, 1, 1, 0, NULL, NOW(), NULL, NOW());
INSERT INTO `sys_permission` VALUES (1919, 1, 1831, 'dict:trace:configUpdate', '编辑追踪配置', 2, '/api/trace/configs/update/{id}', 'PUT', NULL, NULL, NULL, 3, 1, 1, 0, NULL, NOW(), NULL, NOW());
INSERT INTO `sys_permission` VALUES (1920, 1, 1831, 'dict:trace:configDelete', '删除追踪配置', 2, '/api/trace/configs/delete/{id}', 'DELETE', NULL, NULL, NULL, 4, 1, 1, 0, NULL, NOW(), NULL, NOW());
INSERT INTO `sys_permission` VALUES (1921, 1, 1831, 'dict:trace:alertList', '告警列表', 2, '/api/trace/alerts/page', 'GET', NULL, NULL, NULL, 5, 1, 1, 0, NULL, NOW(), NULL, NOW());
INSERT INTO `sys_permission` VALUES (1922, 1, 1831, 'dict:trace:acknowledge', '确认告警', 2, '/api/trace/alerts/acknowledge/{id}', 'PUT', NULL, NULL, NULL, 6, 1, 1, 0, NULL, NOW(), NULL, NOW());

-- 代码生成 (parent: 1832)
INSERT INTO `sys_permission` VALUES (1923, 1, 1832, 'dict:codegen:list', '模板列表', 2, '/api/dict/codegen/template/page', 'GET', NULL, NULL, NULL, 1, 1, 1, 0, NULL, NOW(), NULL, NOW());
INSERT INTO `sys_permission` VALUES (1924, 1, 1832, 'dict:codegen:create', '新增模板', 2, '/api/dict/codegen/template', 'POST', NULL, NULL, NULL, 2, 1, 1, 0, NULL, NOW(), NULL, NOW());
INSERT INTO `sys_permission` VALUES (1925, 1, 1832, 'dict:codegen:update', '编辑模板', 2, '/api/dict/codegen/template/{id}', 'PUT', NULL, NULL, NULL, 3, 1, 1, 0, NULL, NOW(), NULL, NOW());
INSERT INTO `sys_permission` VALUES (1926, 1, 1832, 'dict:codegen:delete', '删除模板', 2, '/api/dict/codegen/template/{id}', 'DELETE', NULL, NULL, NULL, 4, 1, 1, 0, NULL, NOW(), NULL, NOW());
INSERT INTO `sys_permission` VALUES (1927, 1, 1832, 'dict:codegen:generate', '生成代码', 2, '/api/dict/codegen/generate', 'POST', NULL, NULL, NULL, 5, 1, 1, 0, NULL, NOW(), NULL, NOW());
INSERT INTO `sys_permission` VALUES (1928, 1, 1832, 'dict:codegen:history', '生成历史', 2, '/api/dict/codegen/history/page', 'GET', NULL, NULL, NULL, 6, 1, 1, 0, NULL, NOW(), NULL, NOW());
INSERT INTO `sys_permission` VALUES (1961, 1, 1832, 'dict:codegen:detail', '模板详情', 2, '/api/dict/codegen/template/{id}', 'GET', NULL, NULL, NULL, 7, 1, 1, 0, NULL, NOW(), NULL, NOW());

-- 错误文档 (parent: 1833)
INSERT INTO `sys_permission` VALUES (1929, 1, 1833, 'dict:errorDoc:categoryList', '分类列表', 2, '/api/dict/error-doc/category/page', 'GET', NULL, NULL, NULL, 1, 1, 1, 0, NULL, NOW(), NULL, NOW());
INSERT INTO `sys_permission` VALUES (1930, 1, 1833, 'dict:errorDoc:categoryCreate', '新增分类', 2, '/api/dict/error-doc/category', 'POST', NULL, NULL, NULL, 2, 1, 1, 0, NULL, NOW(), NULL, NOW());
INSERT INTO `sys_permission` VALUES (1931, 1, 1833, 'dict:errorDoc:categoryUpdate', '编辑分类', 2, '/api/dict/error-doc/category/{id}', 'PUT', NULL, NULL, NULL, 3, 1, 1, 0, NULL, NOW(), NULL, NOW());
INSERT INTO `sys_permission` VALUES (1932, 1, 1833, 'dict:errorDoc:categoryDelete', '删除分类', 2, '/api/dict/error-doc/category/{id}', 'DELETE', NULL, NULL, NULL, 4, 1, 1, 0, NULL, NOW(), NULL, NOW());
INSERT INTO `sys_permission` VALUES (1933, 1, 1833, 'dict:errorDoc:solutionList', '解决方案列表', 2, '/api/dict/error-doc/solution/page', 'GET', NULL, NULL, NULL, 5, 1, 1, 0, NULL, NOW(), NULL, NOW());
INSERT INTO `sys_permission` VALUES (1934, 1, 1833, 'dict:errorDoc:solutionCreate', '新增解决方案', 2, '/api/dict/error-doc/solution', 'POST', NULL, NULL, NULL, 6, 1, 1, 0, NULL, NOW(), NULL, NOW());
INSERT INTO `sys_permission` VALUES (1935, 1, 1833, 'dict:errorDoc:search', '搜索错误', 2, '/api/dict/error-doc/search', 'GET', NULL, NULL, NULL, 7, 1, 1, 0, NULL, NOW(), NULL, NOW());

-- ----------------------------
-- 六、Finance 模块 - 按钮权限 (type=2)
-- ----------------------------

-- 发票管理 (parent: 1834)
INSERT INTO `sys_permission` VALUES (1936, 1, 1834, 'finance:invoice:list', '发票列表', 2, '/api/finance/invoices/get-invoice-page', 'GET', NULL, NULL, NULL, 1, 1, 1, 0, NULL, NOW(), NULL, NOW());
INSERT INTO `sys_permission` VALUES (1937, 1, 1834, 'finance:invoice:detail', '发票详情', 2, '/api/finance/invoices/get-invoice/{id}', 'GET', NULL, NULL, NULL, 2, 1, 1, 0, NULL, NOW(), NULL, NOW());
INSERT INTO `sys_permission` VALUES (1938, 1, 1834, 'finance:invoice:create', '新增发票', 2, '/api/finance/invoices/create-invoice', 'POST', NULL, NULL, NULL, 3, 1, 1, 0, NULL, NOW(), NULL, NOW());
INSERT INTO `sys_permission` VALUES (1939, 1, 1834, 'finance:invoice:update', '编辑发票', 2, '/api/finance/invoices/update-invoice/{id}', 'PUT', NULL, NULL, NULL, 4, 1, 1, 0, NULL, NOW(), NULL, NOW());
INSERT INTO `sys_permission` VALUES (1940, 1, 1834, 'finance:invoice:delete', '删除发票', 2, '/api/finance/invoices/delete-invoice/{id}', 'DELETE', NULL, NULL, NULL, 5, 1, 1, 0, NULL, NOW(), NULL, NOW());
INSERT INTO `sys_permission` VALUES (1941, 1, 1834, 'finance:invoice:void', '作废发票', 2, '/api/finance/invoices/void-invoice/{id}', 'POST', NULL, NULL, NULL, 6, 1, 1, 0, NULL, NOW(), NULL, NOW());

-- 成本核算 (parent: 1835)
INSERT INTO `sys_permission` VALUES (1942, 1, 1835, 'finance:cost:list', '成本配置列表', 2, '/api/finance/cost/get-config-page', 'GET', NULL, NULL, NULL, 1, 1, 1, 0, NULL, NOW(), NULL, NOW());
INSERT INTO `sys_permission` VALUES (1943, 1, 1835, 'finance:cost:update', '设置成本方法', 2, '/api/finance/cost/set-cost-method', 'POST', NULL, NULL, NULL, 2, 1, 1, 0, NULL, NOW(), NULL, NOW());
INSERT INTO `sys_permission` VALUES (1944, 1, 1835, 'finance:cost:calculate', '计算出库成本', 2, '/api/finance/cost/calculate-outbound-cost', 'GET', NULL, NULL, NULL, 3, 1, 1, 0, NULL, NOW(), NULL, NOW());
INSERT INTO `sys_permission` VALUES (1945, 1, 1835, 'finance:cost:history', '成本历史', 2, '/api/finance/cost/get-cost-history-page', 'GET', NULL, NULL, NULL, 4, 1, 1, 0, NULL, NOW(), NULL, NOW());

-- 财务报表 (parent: 1836)
INSERT INTO `sys_permission` VALUES (1946, 1, 1836, 'finance:report:list', '报表列表', 2, '/api/finance/reports/get-report-list', 'GET', NULL, NULL, NULL, 1, 1, 1, 0, NULL, NOW(), NULL, NOW());
INSERT INTO `sys_permission` VALUES (1947, 1, 1836, 'finance:report:generate', '生成报表', 2, '/api/finance/reports/generate-income-statement', 'POST', NULL, NULL, NULL, 2, 1, 1, 0, NULL, NOW(), NULL, NOW());
INSERT INTO `sys_permission` VALUES (1948, 1, 1836, 'finance:report:lock', '锁定报表', 2, '/api/finance/reports/lock-report/{id}', 'POST', NULL, NULL, NULL, 3, 1, 1, 0, NULL, NOW(), NULL, NOW());
INSERT INTO `sys_permission` VALUES (1949, 1, 1836, 'finance:report:export', '导出报表', 2, '/api/finance/reports/{id}/export', 'GET', NULL, NULL, NULL, 4, 1, 1, 0, NULL, NOW(), NULL, NOW());

-- 银行对账 (parent: 1837)
INSERT INTO `sys_permission` VALUES (1950, 1, 1837, 'finance:bankReconciliation:list', '对账列表', 2, '/api/finance/bank-reconciliation/page', 'GET', NULL, NULL, NULL, 1, 1, 1, 0, NULL, NOW(), NULL, NOW());
INSERT INTO `sys_permission` VALUES (1951, 1, 1837, 'finance:bankReconciliation:import', '导入银行对账单', 2, '/api/finance/bank-reconciliation/import', 'POST', NULL, NULL, NULL, 2, 1, 1, 0, NULL, NOW(), NULL, NOW());
INSERT INTO `sys_permission` VALUES (1952, 1, 1837, 'finance:bankReconciliation:autoMatch', '自动匹配', 2, '/api/finance/bank-reconciliation/auto-match/{id}', 'POST', NULL, NULL, NULL, 3, 1, 1, 0, NULL, NOW(), NULL, NOW());
INSERT INTO `sys_permission` VALUES (1953, 1, 1837, 'finance:bankReconciliation:manualMatch', '手动匹配', 2, '/api/finance/bank-reconciliation/manual-match', 'POST', NULL, NULL, NULL, 4, 1, 1, 0, NULL, NOW(), NULL, NOW());
INSERT INTO `sys_permission` VALUES (1954, 1, 1837, 'finance:bankReconciliation:unmatch', '取消匹配', 2, '/api/finance/bank-reconciliation/unmatch/{bankRecordId}', 'POST', NULL, NULL, NULL, 5, 1, 1, 0, NULL, NOW(), NULL, NOW());

-- 预算管理 (parent: 1838)
INSERT INTO `sys_permission` VALUES (1955, 1, 1838, 'finance:budget:list', '预算列表', 2, '/api/finance/budgets/page', 'GET', NULL, NULL, NULL, 1, 1, 1, 0, NULL, NOW(), NULL, NOW());
INSERT INTO `sys_permission` VALUES (1956, 1, 1838, 'finance:budget:create', '新增预算', 2, '/api/finance/budgets/create', 'POST', NULL, NULL, NULL, 2, 1, 1, 0, NULL, NOW(), NULL, NOW());
INSERT INTO `sys_permission` VALUES (1957, 1, 1838, 'finance:budget:update', '编辑预算', 2, '/api/finance/budgets/update/{id}', 'PUT', NULL, NULL, NULL, 3, 1, 1, 0, NULL, NOW(), NULL, NOW());
INSERT INTO `sys_permission` VALUES (1958, 1, 1838, 'finance:budget:approve', '审批预算', 2, '/api/finance/budgets/approve/{id}', 'POST', NULL, NULL, NULL, 4, 1, 1, 0, NULL, NOW(), NULL, NOW());
INSERT INTO `sys_permission` VALUES (1959, 1, 1838, 'finance:budget:delete', '删除预算', 2, '/api/finance/budgets/delete/{id}', 'DELETE', NULL, NULL, NULL, 5, 1, 1, 0, NULL, NOW(), NULL, NOW());
INSERT INTO `sys_permission` VALUES (1960, 1, 1838, 'finance:budget:execution', '预算执行', 2, '/api/finance/budgets/execution/{id}', 'GET', NULL, NULL, NULL, 6, 1, 1, 0, NULL, NOW(), NULL, NOW());

-- ----------------------------
-- 七、同步更新 sys_role_permission 为管理员角色分配新增权限
-- 假设管理员角色 ID = 1，以下为所有新增权限的分配
-- ----------------------------
INSERT INTO `sys_role_permission` (`role_id`, `permission_id`) SELECT 1, id FROM `sys_permission` WHERE id BETWEEN 1814 AND 1961;
