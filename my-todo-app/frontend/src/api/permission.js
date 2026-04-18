// api/permission.js - 权限与角色管理 API 接口模块
// 提供权限树、角色的增删改查、角色权限分配、用户角色分配等接口

import { get, post, put, del } from '@/utils/request'

/**
 * 获取完整的权限树（树形结构，用于权限分配和菜单渲染）
 */
export function getPermissionTree() {
  return get('/permissions/tree')
}

/**
 * 创建新的权限节点
 * @param data 权限信息
 */
export function createPermission(data) {
  return post('/permissions', data)
}

/**
 * 更新权限节点信息
 * @param id 权限ID
 * @param data 需要更新的权限字段
 */
export function updatePermission(id, data) {
  return put(`/permissions/${id}`, data)
}

/**
 * 删除权限节点
 * @param id 权限ID
 */
export function deletePermission(id) {
  return del(`/permissions/${id}`)
}

/**
 * 获取角色列表（支持按名称、编码、状态筛选）
 * @param params 可选的筛选参数
 */
export function getRoleList(params) {
  return get('/roles', params)
}

/**
 * 根据ID获取角色详情
 * @param id 角色ID
 */
export function getRole(id) {
  return get(`/roles/${id}`)
}

/**
 * 创建新角色
 * @param data 角色信息
 */
export function createRole(data) {
  return post('/roles', data)
}

/**
 * 更新角色信息
 * @param id 角色ID
 * @param data 需要更新的角色字段
 */
export function updateRole(id, data) {
  return put(`/roles/${id}`, data)
}

/**
 * 删除角色
 * @param id 角色ID
 */
export function deleteRole(id) {
  return del(`/roles/${id}`)
}

/**
 * 获取指定角色已分配的权限ID列表
 * @param roleId 角色ID
 */
export function getRolePermissions(roleId) {
  return get(`/roles/${roleId}/permissions`)
}

/**
 * 为角色分配权限（覆盖式分配，传入的权限ID列表会完全替换原有权限）
 * @param roleId 角色ID
 * @param permissionIds 要分配的权限ID数组
 */
export function assignRolePermissions(roleId, permissionIds) {
  return post(`/roles/${roleId}/permissions`, { permissionIds })
}

/**
 * 获取指定用户已分配的角色ID列表
 * @param userId 用户ID
 */
export function getUserRoles(userId) {
  return get(`/users/${userId}/roles`)
}

/**
 * 为用户分配角色（覆盖式分配）
 * @param userId 用户ID
 * @param roleIds 要分配的角色ID数组
 */
export function assignUserRoles(userId, roleIds) {
  return post(`/users/${userId}/roles`, { roleIds })
}
