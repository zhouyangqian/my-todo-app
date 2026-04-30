// api/system.js - 系统管理 API 接口模块
// 提供部门管理等系统相关功能接口

import { get, post, put, del } from '@/utils/request'

// ============ 部门管理 API ============

/**
 * 获取部门树
 */
export function getDeptTree() {
  return get('/system/dept/get-department-tree')
}

/**
 * 创建部门
 * @param data 部门信息
 */
export function createDept(data) {
  return post('/system/dept/create-department', data)
}

/**
 * 更新部门
 * @param id 部门ID
 * @param data 部门信息
 */
export function updateDept(id, data) {
  return put(`/system/dept/update-department/${id}`, data)
}

/**
 * 删除部门
 * @param id 部门ID
 */
export function deleteDept(id) {
  return del(`/system/dept/delete-department/${id}`)
}
