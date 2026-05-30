// api/user.js - 用户管理 API 接口模块
// 提供用户的增删改查、启用/禁用、地址管理等接口

import { get, post, put, del } from '@/utils/request'

/**
 * 分页查询用户列表
 * @param {Object} params 查询参数（分页、用户名、姓名、部门、状态等筛选条件）
 * @returns {Promise}
 */
export function getUserPage(params) {
  return get('/users/get-user-page', params)
}

/**
 * 根据ID获取用户详情
 * @param {string} id 用户ID
 * @returns {Promise}
 */
export function getUser(id) {
  return get(`/users/get-user/${id}`)
}

/**
 * 创建新用户
 * @param {Object} data 用户信息
 * @returns {Promise}
 */
export function createUser(data) {
  return post('/users/create-user', data)
}

/**
 * 更新用户信息
 * @param {string} id 用户ID
 * @param {Object} data 需要更新的用户字段
 * @returns {Promise}
 */
export function updateUser(id, data) {
  return put(`/users/update-user/${id}`, data)
}

/**
 * 删除用户
 * @param {string} id 用户ID
 * @returns {Promise}
 */
export function deleteUser(id) {
  return del(`/users/delete-user/${id}`)
}

/**
 * 启用用户（将用户状态设置为启用）
 * @param {string} id 用户ID
 * @returns {Promise}
 */
export function enableUser(id) {
  return post(`/users/enable-user/${id}`)
}

/**
 * 禁用用户（将用户状态设置为禁用，禁止登录）
 * @param {string} id 用户ID
 * @returns {Promise}
 */
export function disableUser(id) {
  return post(`/users/disable-user/${id}`)
}

/**
 * 获取用户的所有收货地址
 * @param {string} userId 用户ID
 * @returns {Promise}
 */
export function getUserAddresses(userId) {
  return get(`/users/${userId}/addresses`)
}

/**
 * 为用户添加新的收货地址
 * @param {string} userId 用户ID
 * @param {Object} data 地址信息
 * @returns {Promise}
 */
export function addUserAddress(userId, data) {
  return post(`/users/${userId}/addresses`, data)
}

/**
 * 设置用户的默认收货地址
 * @param {string} userId 用户ID
 * @param {string} addressId 地址ID
 * @returns {Promise}
 */
export function setDefaultAddress(userId, addressId) {
  return post(`/users/${userId}/addresses/${addressId}/default`)
}

// ===== 角色管理 =====

/**
 * 分页查询角色列表
 * @param {Object} params 查询参数（分页、角色名称等筛选条件）
 * @returns {Promise}
 */
export function getRolePage(params) {
  return get('/users/roles/get-role-page', params)
}

/**
 * 创建角色
 * @param {Object} data 角色信息
 * @returns {Promise}
 */
export function createRole(data) {
  return post('/users/roles/create-role', data)
}

/**
 * 更新角色
 * @param {string} id 角色ID
 * @param {Object} data 角色信息
 * @returns {Promise}
 */
export function updateRole(id, data) {
  return put(`/users/roles/update-role/${id}`, data)
}

/**
 * 删除角色
 * @param {string} id 角色ID
 * @returns {Promise}
 */
export function deleteRole(id) {
  return del(`/users/roles/delete-role/${id}`)
}

/**
 * 为用户分配角色
 * @param {Object} data { userId, roleIds }
 * @returns {Promise}
 */
export function assignRoles(data) {
  return post('/users/roles/assign-roles', data)
}

/**
 * 移除用户角色
 * @param {Object} data { userId, roleIds }
 * @returns {Promise}
 */
export function removeRoles(data) {
  return del('/users/roles/remove-roles', data)
}

/**
 * 查询用户角色列表
 * @param {string} userId 用户ID
 * @returns {Promise}
 */
export function getUserRoles(userId) {
  return get(`/users/roles/get-user-roles/${userId}`)
}

// ===== 用户权限 =====

/**
 * 移除用户直接权限
 * @param {Object} data { userId, permissionIds }
 * @returns {Promise}
 */
export function removeDirectPermissions(data) {
  return del('/users/permissions/remove-direct', data)
}

// ===== 审计日志 =====

/**
 * 分页查询审计日志
 * @param {Object} params 查询参数（分页、操作类型、操作人等筛选条件）
 * @returns {Promise}
 */
export function getAuditLogPage(params) {
  return get('/users/audit-logs', params)
}

// ===== 批量操作 =====

/**
 * 批量禁用用户
 * @param {Object} data { userIds: Long[] }
 * @returns {Promise}
 */
export function batchDisableUsers(data) {
  return post('/users/batch/disable', data)
}

/**
 * 批量删除用户
 * @param {Object} data { userIds: Long[] }
 * @returns {Promise}
 */
export function batchDeleteUsers(data) {
  return post('/users/batch/delete', data)
}

/**
 * 批量分配角色
 * @param {Object} data { userIds: Long[], roleIds: Long[] }
 * @returns {Promise}
 */
export function batchAssignRoles(data) {
  return post('/users/batch/assign-roles', data)
}

// ===== 导入导出 =====

/**
 * 下载用户导入模板
 * @returns {Promise}
 */
export function downloadImportTemplate() {
  return get('/users/import-export/template', {}, { responseType: 'blob' })
}

/**
 * 导入用户
 * @param {File} file Excel文件
 * @returns {Promise}
 */
export function importUsers(file) {
  const formData = new FormData()
  formData.append('file', file)
  return post('/users/import-export/import', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

/**
 * 导出用户列表
 * @param {Object} params 筛选参数（username, realName, status）
 * @returns {Promise}
 */
export function exportUsers(params) {
  return get('/users/import-export/export', params, { responseType: 'blob' })
}
