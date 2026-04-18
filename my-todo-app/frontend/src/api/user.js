// api/user.js - 用户管理 API 接口模块
// 提供用户的增删改查、启用/禁用、地址管理等接口

import { get, post, put, del } from '@/utils/request'

/**
 * 分页查询用户列表
 * @param params 查询参数（分页、用户名、姓名、部门、状态等筛选条件）
 */
export function getUserPage(params) {
  return get('/users', params)
}

/**
 * 根据ID获取用户详情
 * @param id 用户ID
 */
export function getUser(id) {
  return get(`/users/${id}`)
}

/**
 * 创建新用户
 * @param data 用户信息
 */
export function createUser(data) {
  return post('/users', data)
}

/**
 * 更新用户信息
 * @param id 用户ID
 * @param data 需要更新的用户字段
 */
export function updateUser(id, data) {
  return put(`/users/${id}`, data)
}

/**
 * 删除用户
 * @param id 用户ID
 */
export function deleteUser(id) {
  return del(`/users/${id}`)
}

/**
 * 启用用户（将用户状态设置为启用）
 * @param id 用户ID
 */
export function enableUser(id) {
  return post(`/users/${id}/enable`)
}

/**
 * 禁用用户（将用户状态设置为禁用，禁止登录）
 * @param id 用户ID
 */
export function disableUser(id) {
  return post(`/users/${id}/disable`)
}

/**
 * 获取用户的所有收货地址
 * @param userId 用户ID
 */
export function getUserAddresses(userId) {
  return get(`/users/${userId}/addresses`)
}

/**
 * 为用户添加新的收货地址
 * @param userId 用户ID
 * @param data 地址信息
 */
export function addUserAddress(userId, data) {
  return post(`/users/${userId}/addresses`, data)
}

/**
 * 设置用户的默认收货地址
 * @param userId 用户ID
 * @param addressId 地址ID
 */
export function setDefaultAddress(userId, addressId) {
  return post(`/users/${userId}/addresses/${addressId}/default`)
}
