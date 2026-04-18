// api/auth.ts - 认证相关 API 接口模块
// 提供用户登录、登出、Token 刷新、获取用户信息、修改密码等接口

import { get, post } from '@/utils/request'

// 登录请求参数
export interface LoginRequest {
  username: string        // 用户名
  password: string        // 密码
  captchaKey?: string     // 验证码缓存键（可选）
  captchaValue?: string   // 验证码输入值（可选）
}

// 登录响应数据
export interface LoginResponse {
  accessToken: string     // 访问令牌，用于接口鉴权
  refreshToken: string    // 刷新令牌，用于续期访问令牌
  tokenType: string       // 令牌类型（如 Bearer）
  expiresIn: number       // 令牌过期时间（秒）
  userInfo: {             // 当前登录用户基本信息
    userId: number        // 用户ID
    username: string      // 用户名
    email: string         // 邮箱
    realName: string      // 真实姓名
    avatar: string        // 头像URL
    tenantId: number      // 所属租户ID（多租户架构）
  }
}

// 用户权限响应数据
export interface UserPermissionResponse {
  userId: number          // 用户ID
  permissions: string[]   // 用户拥有的权限编码列表
  roles: string[]         // 用户拥有的角色编码列表
}

/**
 * 用户登录
 * @param data 登录表单数据（用户名、密码、验证码）
 * @returns 登录响应，包含令牌和用户信息
 */
export function login(data: LoginRequest): Promise<LoginResponse> {
  return post('/auth/login', data)
}

/**
 * 用户登出
 * 清除服务端会话，使当前令牌失效
 */
export function logout(): Promise<void> {
  return post('/auth/logout')
}

/**
 * 刷新Token
 * 使用 refreshToken 换取新的 accessToken，避免用户频繁重新登录
 * @param refreshToken 刷新令牌
 */
export function refreshToken(refreshToken: string): Promise<LoginResponse> {
  return post('/auth/refresh', { refreshToken })
}

/**
 * 获取当前登录用户的信息和权限
 * 用于页面初始化时加载用户数据
 */
export function getUserInfo(): Promise<UserPermissionResponse> {
  return get('/permissions/user/me')
}

/**
 * 修改密码
 * @param data 包含旧密码和新密码
 */
export function changePassword(data: { oldPassword: string; newPassword: string }): Promise<void> {
  return post('/auth/change-password', data)
}
