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
