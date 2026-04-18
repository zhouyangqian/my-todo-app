// api/auth.js - 认证相关 API 接口模块
// 提供用户登录、登出、Token 刷新、获取用户信息、修改密码等接口

import { get, post } from '@/utils/request'

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
  return get('/permissions/user/me')
}

/**
 * 修改密码
 * @param data 包含旧密码和新密码
 */
export function changePassword(data) {
  return post('/auth/change-password', data)
}
