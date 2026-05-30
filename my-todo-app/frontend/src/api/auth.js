// api/auth.js - 认证相关 API 接口模块
// 提供用户登录、登出、Token 刷新、获取用户信息、修改密码等接口

import { get, post, put, del } from '@/utils/request'

/**
 * 用户登录
 * @param data 登录表单数据（用户名、密码、验证码）
 */
export function login(data) {
  return post('/auth/login', data)
}

/**
 * 用户登出
 * 清除服务端会话，使当前令牌失效
 */
export function logout() {
  return post('/auth/logout')
}

/**
 * 刷新Token
 * 使用 refreshToken 换取新的 accessToken，避免用户频繁重新登录
 * @param refreshToken 刷新令牌
 */
export function refreshToken(refreshToken) {
  return post('/auth/refresh', { refreshToken })
}

/**
 * 获取当前登录用户的信息和权限
 * 用于页面初始化时加载用户数据
 */
export function getUserInfo() {
  return get('/permissions/get-current-user-permissions')
}

/**
 * 修改密码
 * @param data 包含旧密码和新密码
 */
export function changePassword(data) {
  return post('/auth/change-password', data)
}

/**
 * 获取当前用户档案信息
 */
export function getProfile() {
  return get('/auth/profile')
}

/**
 * 管理员重置用户密码（无需旧密码）
 * @param {Object} data 包含 userId 和 newPassword
 */
export function resetPassword(data) {
  return post('/auth/reset-password', data)
}

/**
 * 管理员踢出用户（强制下线）
 * @param {number} userId 要踢出的用户ID
 */
export function kickUser(userId) {
  return post(`/auth/kick-user/${userId}`)
}

/**
 * 获取验证码
 * 返回验证码Key和Base64编码的验证码图片
 */
export function getCaptcha() {
  return get('/auth/captcha')
}

/**
 * 租户注册
 * @param data 租户注册表单数据
 */
export function registerTenant(data) {
  return post('/auth/tenant/register', data)
}

// ==================== 租户管理（管理员） ====================

/**
 * 获取租户列表
 * @param {Object} params 查询参数（page, size, tenantName, status）
 * @returns {Promise}
 */
export function getTenantList(params) {
  return get('/auth/tenant-management/list', params)
}

/**
 * 获取租户配额
 * @param {number} tenantId 租户ID
 * @returns {Promise}
 */
export function getTenantQuota(tenantId) {
  return get(`/auth/tenant-management/quota/${tenantId}`)
}

/**
 * 更新租户配额
 * @param {number} tenantId 租户ID
 * @param {Object} data 配额信息
 * @returns {Promise}
 */
export function updateTenantQuota(tenantId, data) {
  return put(`/auth/tenant-management/quota/${tenantId}`, data)
}

/**
 * 获取租户资源使用量
 * @param {number} tenantId 租户ID
 * @returns {Promise}
 */
export function getTenantUsage(tenantId) {
  return get(`/auth/tenant-management/usage/${tenantId}`)
}
