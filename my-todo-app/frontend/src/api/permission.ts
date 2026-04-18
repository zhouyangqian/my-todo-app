// api/permission.ts - 权限与角色管理 API 接口模块
// 提供权限树、角色的增删改查、角色权限分配、用户角色分配等接口

import { get, post, put, del } from '@/utils/request'

// 权限节点类型定义（树形结构）
export interface Permission {
  id: number              // 权限唯一标识
  tenantId: number        // 所属租户ID
  parentId: number        // 父级权限ID（0 表示顶级）
  name: string            // 权限名称
  code: string            // 权限编码（如 system:user:add）
  type: number            // 权限类型：1-菜单, 2-按钮, 3-接口
  path: string            // 菜单路径或接口路径
  icon: string            // 菜单图标名称
  sort: number            // 排序序号
  status: number          // 状态：1-启用，0-禁用
  children?: Permission[] // 子权限列表（树形递归）
}

// 角色类型定义
export interface Role {
  id: number              // 角色唯一标识
  tenantId: number        // 所属租户ID
  name: string            // 角色名称（如 管理员、普通用户）
  code: string            // 角色编码（如 admin、user）
  description: string     // 角色描述
  status: number          // 状态：1-启用，0-禁用
  createdAt: string       // 创建时间
}

// 用户-角色关联
export interface UserRole {
  userId: number          // 用户ID
  roleId: number          // 角色ID
}

// 角色-权限关联
export interface RolePermission {
  roleId: number          // 角色ID
  permissionId: number    // 权限ID
}

// 用户权限信息响应
export interface UserPermissionResponse {
  userId: number          // 用户ID
  permissions: string[]   // 用户拥有的权限编码列表
  roles: string[]         // 用户拥有的角色编码列表
}

/**
 * 获取完整的权限树（树形结构，用于权限分配和菜单渲染）
 */
export function getPermissionTree(): Promise<Permission[]> {
  return get('/permissions/tree')
}

/**
 * 创建新的权限节点
 * @param data 权限信息
 */
export function createPermission(data: Partial<Permission>): Promise<Permission> {
  return post('/permissions', data)
}

/**
 * 更新权限节点信息
 * @param id 权限ID
 * @param data 需要更新的权限字段
 */
export function updatePermission(id: number, data: Partial<Permission>): Promise<Permission> {
  return put(`/permissions/${id}`, data)
}

/**
 * 删除权限节点
 * @param id 权限ID
 */
export function deletePermission(id: number): Promise<void> {
  return del(`/permissions/${id}`)
}

/**
 * 获取角色列表（支持按名称、编码、状态筛选）
 * @param params 可选的筛选参数
 */
export function getRoleList(params?: {
  name?: string
  code?: string
  status?: number
}): Promise<Role[]> {
  return get('/roles', params)
}

/**
 * 根据ID获取角色详情
 * @param id 角色ID
 */
export function getRole(id: number): Promise<Role> {
  return get(`/roles/${id}`)
}

/**
 * 创建新角色
 * @param data 角色信息
 */
export function createRole(data: Partial<Role>): Promise<Role> {
  return post('/roles', data)
}

/**
 * 更新角色信息
 * @param id 角色ID
 * @param data 需要更新的角色字段
 */
export function updateRole(id: number, data: Partial<Role>): Promise<Role> {
  return put(`/roles/${id}`, data)
}

/**
 * 删除角色
 * @param id 角色ID
 */
export function deleteRole(id: number): Promise<void> {
  return del(`/roles/${id}`)
}

/**
 * 获取指定角色已分配的权限ID列表
 * @param roleId 角色ID
 * @returns 权限ID数组
 */
export function getRolePermissions(roleId: number): Promise<number[]> {
  return get(`/roles/${roleId}/permissions`)
}

/**
 * 为角色分配权限（覆盖式分配，传入的权限ID列表会完全替换原有权限）
 * @param roleId 角色ID
 * @param permissionIds 要分配的权限ID数组
 */
export function assignRolePermissions(roleId: number, permissionIds: number[]): Promise<void> {
  return post(`/roles/${roleId}/permissions`, { permissionIds })
}

/**
 * 获取指定用户已分配的角色ID列表
 * @param userId 用户ID
 * @returns 角色ID数组
 */
export function getUserRoles(userId: number): Promise<number[]> {
  return get(`/users/${userId}/roles`)
}

/**
 * 为用户分配角色（覆盖式分配）
 * @param userId 用户ID
 * @param roleIds 要分配的角色ID数组
 */
export function assignUserRoles(userId: number, roleIds: number[]): Promise<void> {
  return post(`/users/${userId}/roles`, { roleIds })
}
