// stores/user.js - 用户状态管理模块（Pinia Store）
// 管理用户登录状态、Token、用户信息、权限和角色等全局状态

import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { login, logout, getUserInfo } from '@/api/auth'
import router from '@/router'

/**
 * 用户状态管理 Store（使用 Composition API 风格）
 * 管理当前登录用户的所有状态和操作
 */
export const useUserStore = defineStore('user', () => {
  // ===== 状态定义 =====

  // 访问令牌，页面刷新时从 localStorage 恢复
  const token = ref(localStorage.getItem('token') || '')
  // 刷新令牌，用于令牌续期
  const refreshToken = ref(localStorage.getItem('refreshToken') || '')
  // 当前登录用户信息
  const userInfo = ref(null)
  // 用户拥有的权限编码列表
  const permissions = ref([])
  // 用户拥有的角色编码列表
  const roles = ref([])

  // ===== 计算属性 =====

  // 是否已登录（通过 token 是否存在判断）
  const isLoggedIn = computed(() => !!token.value)

  // ===== 操作方法 =====

  /**
   * 用户登录操作
   * 1. 调用登录接口获取令牌
   * 2. 将令牌持久化到 localStorage
   * 3. 从登录响应中保存用户基本信息，并获取权限
   * 4. 跳转到工作台页面
   * @param username 用户名
   * @param password 密码
   */
  async function loginAction(username, password) {
    try {
      const res = await login({ username, password })
      // 后端返回 ApiResponse 包装的数据，实际数据在 data 字段中
      const data = res.data || res

      console.log('登录响应数据:', data)
      console.log('用户信息:', data.userInfo)
      console.log('租户ID:', data.userInfo?.tenantId)

      // 保存令牌到状态和本地存储
      token.value = data.accessToken
      refreshToken.value = data.refreshToken
      localStorage.setItem('token', data.accessToken)
      localStorage.setItem('refreshToken', data.refreshToken)

      // 保存登录响应中的用户基本信息
      if (data.userInfo) {
        userInfo.value = data.userInfo
        // 同时保存 tenantId 到 localStorage，用于请求拦截器
        if (data.userInfo.tenantId) {
          localStorage.setItem('tenantId', data.userInfo.tenantId)
          console.log('已保存租户ID到 localStorage:', data.userInfo.tenantId)
        }
      }

      // 登录成功后立即获取用户权限和角色
      await getUserPermissionsAction()

      // 跳转到工作台首页
      router.push('/dashboard')
      return res
    } catch (error) {
      console.error('登录失败:', error)
      throw error
    }
  }

  /**
   * 获取当前登录用户的信息和权限
   * 从后端获取最新的用户信息、权限列表和角色列表
   */
  async function getUserInfoAction() {
    try {
      const res = await getUserInfo()
      const data = res.data || res
      userInfo.value = data.userInfo           // 用户基本信息
      permissions.value = data.permissions || [] // 权限编码列表
      roles.value = data.roles || []             // 角色编码列表
      return res
    } catch (error) {
      throw error
    }
  }

  /**
   * 获取当前登录用户的权限和角色
   * 从后端获取最新的权限列表和角色列表
   */
  async function getUserPermissionsAction() {
    try {
      const res = await getUserInfo()
      const data = res.data || res
      permissions.value = data.permissions || [] // 权限编码列表
      roles.value = data.roles || []             // 角色编码列表
      return res
    } catch (error) {
      throw error
    }
  }

  /**
   * 用户登出操作
   * 无论后端登出接口是否成功，都会清除本地认证信息并跳转到登录页
   */
  async function logoutAction() {
    try {
      await logout()
    } catch (error) {
      console.error('Logout error:', error)
    } finally {
      // 无论登出接口成功与否，都清除本地状态
      clearAuth()
      router.push('/login')
    }
  }

  /**
   * 清除所有认证相关信息
   * 清空内存中的状态和 localStorage 中的持久化数据
   */
  function clearAuth() {
    token.value = ''
    refreshToken.value = ''
    userInfo.value = null
    permissions.value = []
    roles.value = []
    localStorage.removeItem('token')
    localStorage.removeItem('refreshToken')
  }

  /**
   * 检查当前用户是否拥有指定权限
   * @param permission 权限编码（如 system:user:add）
   */
  function hasPermission(permission) {
    return permissions.value.includes('*') || permissions.value.includes(permission)
  }

  /**
   * 检查当前用户是否拥有指定角色
   * @param role 角色编码（如 admin）
   */
  function hasRole(role) {
    return roles.value.includes(role)
  }

  // 暴露状态和方法供组件使用
  return {
    token,
    refreshToken,
    userInfo,
    permissions,
    roles,
    isLoggedIn,
    loginAction,
    getUserInfoAction,
    logoutAction,
    clearAuth,
    hasPermission,
    hasRole
  }
})
