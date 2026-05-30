// api/permission.js - 权限与角色管理 API 接口模块
// 提供权限树、角色的增删改查、角色权限分配、用户角色分配等接口

import { get, post, put, del } from '@/utils/request'

/**
 * 获取完整的权限树（树形结构，用于权限分配和菜单渲染）
 * @returns {Promise}
 */
export function getPermissionTree() {
  return get('/permissions/get-permission-tree')
}

/**
 * 创建新的权限节点
 * @param {Object} data 权限信息
 * @returns {Promise}
 */
export function createPermission(data) {
  return post('/permissions/create-permission', data)
}

/**
 * 更新权限节点信息
 * @param {string} id 权限ID
 * @param {Object} data 需要更新的权限字段
 * @returns {Promise}
 */
export function updatePermission(id, data) {
  return put(`/permissions/update-permission/${id}`, data)
}

/**
 * 删除权限节点
 * @param {string} id 权限ID
 * @returns {Promise}
 */
export function deletePermission(id) {
  return del(`/permissions/delete-permission/${id}`)
}

/**
 * 获取角色列表（支持按名称、编码、状态筛选）
 * @param {Object} params 可选的筛选参数
 * @returns {Promise}
 */
export function getRoleList(params) {
  return get('/roles/get-role-list', params)
}

/**
 * 根据ID获取角色详情
 * @param {string} id 角色ID
 * @returns {Promise}
 */
export function getRole(id) {
  return get(`/roles/get-role/${id}`)
}

/**
 * 创建新角色
 * @param {Object} data 角色信息
 * @returns {Promise}
 */
export function createRole(data) {
  return post('/roles/create-role', data)
}

/**
 * 更新角色信息
 * @param {string} id 角色ID
 * @param {Object} data 需要更新的角色字段
 * @returns {Promise}
 */
export function updateRole(id, data) {
  return put(`/roles/update-role/${id}`, data)
}

/**
 * 删除角色
 * @param {string} id 角色ID
 * @returns {Promise}
 */
export function deleteRole(id) {
  return del(`/roles/delete-role/${id}`)
}

/**
 * 获取指定角色已分配的权限ID列表
 * @param {string} roleId 角色ID
 * @returns {Promise}
 */
export function getRolePermissions(roleId) {
  return get(`/roles/get-role-permissions/${roleId}`)
}

/**
 * 为角色分配权限（覆盖式分配，传入的权限ID列表会完全替换原有权限）
 * @param {string} roleId 角色ID
 * @param {string[]} permissionIds 要分配的权限ID数组
 * @returns {Promise}
 */
export function assignRolePermissions(roleId, permissionIds) {
  return post(`/roles/${roleId}/permissions`, { permissionIds })
}

/**
 * 获取指定用户已分配的角色ID列表
 * @param {string} userId 用户ID
 * @returns {Promise}
 */
export function getUserRoles(userId) {
  return get(`/roles/get-roles-by-user/${userId}`)
}

/**
 * 为用户分配角色（覆盖式分配）
 * @param {string} userId 用户ID
 * @param {string[]} roleIds 要分配的角色ID数组
 * @returns {Promise}
 */
export function assignUserRoles(userId, roleIds) {
  return post('/roles/assign-to-user', { userId, roleIds })
}

/**
 * 清除指定用户的权限缓存
 * 权限或角色变更后调用，确保下次查询获取最新数据
 * @param {number} userId 用户ID
 * @returns {Promise}
 */
export function clearUserPermissionCache(userId) {
  return del(`/permissions/clear-user-cache/${userId}`)
}

// ==================== 动态权限管理 ====================

/**
 * 动态创建权限
 * @param {Object} data 权限信息（code, name, description, parentId, level）
 * @returns {Promise}
 */
export function createDynamicPermission(data) {
  return post('/permissions/dynamic/create', data)
}

/**
 * 动态更新权限
 * @param {string} id 权限ID
 * @param {Object} data 更新字段（name, description）
 * @returns {Promise}
 */
export function updateDynamicPermission(id, data) {
  return put(`/permissions/dynamic/update/${id}`, data)
}

/**
 * 动态删除权限
 * @param {string} id 权限ID
 * @returns {Promise}
 */
export function deleteDynamicPermission(id) {
  return del(`/permissions/dynamic/delete/${id}`)
}

/**
 * 获取动态权限树
 * @returns {Promise}
 */
export function getDynamicPermissionTree() {
  return get('/permissions/dynamic/tree')
}

/**
 * 清除权限缓存
 * @returns {Promise}
 */
export function clearPermissionCache() {
  return post('/permissions/dynamic/clear-cache')
}

// ==================== 会话管理 ====================

/**
 * 获取在线用户列表
 * @returns {Promise}
 */
export function getOnlineSessions() {
  return get('/permissions/sessions/online')
}

/**
 * 踢出指定会话
 * @param {string} sessionId 会话ID
 * @returns {Promise}
 */
export function kickSession(sessionId) {
  return post(`/permissions/sessions/kick/${sessionId}`)
}

/**
 * 踢出用户所有会话
 * @param {number} userId 用户ID
 * @returns {Promise}
 */
export function kickUserSessions(userId) {
  return post(`/permissions/sessions/kick-user/${userId}`)
}

/**
 * 清理过期会话
 * @returns {Promise}
 */
export function cleanExpiredSessions() {
  return post('/permissions/sessions/clean-expired')
}

// ==================== 黑名单管理 ====================

/**
 * 分页查询黑名单列表
 * @param {Object} params 查询参数（page, size, status）
 * @returns {Promise}
 */
export function getBlacklistPage(params) {
  return get('/permissions/blacklist', params)
}

/**
 * 加入黑名单
 * @param {Object} data 拉黑参数（userId, reason, operatorType）
 * @returns {Promise}
 */
export function addToBlacklist(data) {
  return post('/permissions/blacklist', data)
}

/**
 * 解除黑名单
 * @param {number} id 黑名单记录ID
 * @returns {Promise}
 */
export function removeFromBlacklist(id) {
  return del(`/permissions/blacklist/${id}`)
}

/**
 * 检查用户是否在黑名单中
 * @param {number} userId 用户ID
 * @returns {Promise}
 */
export function checkBlacklist(userId) {
  return get('/permissions/blacklist/check', { userId })
}

// ==================== 权限模板管理 ====================

/**
 * 分页查询权限模板
 * @param {Object} params 查询参数（page, size）
 * @returns {Promise}
 */
export function getTemplatePage(params) {
  return get('/permissions/templates/page', params)
}

/**
 * 获取权限模板详情
 * @param {number} id 模板ID
 * @returns {Promise}
 */
export function getTemplateDetail(id) {
  return get(`/permissions/templates/detail/${id}`)
}

/**
 * 创建权限模板
 * @param {Object} data 模板信息
 * @returns {Promise}
 */
export function createTemplate(data) {
  return post('/permissions/templates/create', data)
}

/**
 * 更新权限模板
 * @param {number} id 模板ID
 * @param {Object} data 模板信息
 * @returns {Promise}
 */
export function updateTemplate(id, data) {
  return put(`/permissions/templates/update/${id}`, data)
}

/**
 * 删除权限模板
 * @param {number} id 模板ID
 * @returns {Promise}
 */
export function deleteTemplate(id) {
  return del(`/permissions/templates/delete/${id}`)
}

/**
 * 将模板应用到角色
 * @param {Object} data 包含 templateId 和 roleId
 * @returns {Promise}
 */
export function applyTemplateToRole(data) {
  return post('/permissions/templates/apply-to-role', data)
}

// ==================== 数据权限规则管理 ====================

/**
 * 获取指定角色的数据权限规则列表
 * @param {number} roleId 角色ID
 * @returns {Promise}
 */
export function getDataRulesByRole(roleId) {
  return get(`/permissions/data-rules/role/${roleId}`)
}

/**
 * 创建数据权限规则
 * @param {Object} data 规则信息
 * @returns {Promise}
 */
export function createDataRule(data) {
  return post('/permissions/data-rules/create', data)
}

/**
 * 更新数据权限规则
 * @param {number} id 规则ID
 * @param {Object} data 规则信息
 * @returns {Promise}
 */
export function updateDataRule(id, data) {
  return put(`/permissions/data-rules/update/${id}`, data)
}

/**
 * 删除数据权限规则
 * @param {number} id 规则ID
 * @returns {Promise}
 */
export function deleteDataRule(id) {
  return del(`/permissions/data-rules/delete/${id}`)
}

// ==================== 菜单管理 ====================

/**
 * 获取菜单树
 * @returns {Promise}
 */
export function getMenuTree() {
  return get('/permissions/menus/tree')
}

/**
 * 获取角色的菜单树
 * @param {number} roleId 角色ID
 * @returns {Promise}
 */
export function getRoleMenuTree(roleId) {
  return get(`/permissions/menus/role-tree/${roleId}`)
}

/**
 * 创建菜单
 * @param {Object} data 菜单信息
 * @returns {Promise}
 */
export function createMenu(data) {
  return post('/permissions/menus/create', data)
}

/**
 * 更新菜单
 * @param {number} id 菜单ID
 * @param {Object} data 菜单信息
 * @returns {Promise}
 */
export function updateMenu(id, data) {
  return put(`/permissions/menus/update/${id}`, data)
}

/**
 * 删除菜单
 * @param {number} id 菜单ID
 * @returns {Promise}
 */
export function deleteMenu(id) {
  return del(`/permissions/menus/delete/${id}`)
}

/**
 * 分配菜单给角色
 * @param {Object} data { roleId, menuIds }
 * @returns {Promise}
 */
export function assignMenusToRole(data) {
  return post('/permissions/menus/assign-role', data)
}

/**
 * 获取当前用户菜单
 * @returns {Promise}
 */
export function getUserMenus() {
  return get('/permissions/menus/user-menus')
}

// ==================== 角色继承管理 ====================

/**
 * 设置角色继承关系
 * @param {Object} data { childRoleId, parentRoleId }
 * @returns {Promise}
 */
export function setRoleParent(data) {
  return post('/permissions/role-inheritance/set-parent', data)
}

/**
 * 移除角色继承关系
 * @param {Object} data { childRoleId, parentRoleId }
 * @returns {Promise}
 */
export function removeRoleParent(data) {
  return del('/permissions/role-inheritance/remove-parent', data)
}

/**
 * 获取指定角色的所有父角色
 * @param {number} roleId 角色ID
 * @returns {Promise}
 */
export function getParentRoles(roleId) {
  return get(`/permissions/role-inheritance/parents/${roleId}`)
}

/**
 * 获取继承的权限（仅继承部分，不含自身权限）
 * @param {number} roleId 角色ID
 * @returns {Promise}
 */
export function getInheritedPermissions(roleId) {
  return get(`/permissions/role-inheritance/inherited-permissions/${roleId}`)
}

/**
 * 获取所有有效权限（自身权限 + 继承权限）
 * @param {number} roleId 角色ID
 * @returns {Promise}
 */
export function getEffectivePermissions(roleId) {
  return get(`/permissions/role-inheritance/effective-permissions/${roleId}`)
}

/**
 * 获取继承树
 * @param {number} roleId 角色ID
 * @returns {Promise}
 */
export function getInheritanceTree(roleId) {
  return get(`/permissions/role-inheritance/tree/${roleId}`)
}
