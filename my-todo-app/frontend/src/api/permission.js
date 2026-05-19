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
