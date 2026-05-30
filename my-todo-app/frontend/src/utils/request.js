// utils/request.js - HTTP 请求封装模块
// 基于 axios 封装统一的请求方法，包含请求/响应拦截器、Token 自动注入、错误处理、Token 自动刷新等

import axios from 'axios'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import router from '@/router'

// 创建 axios 实例，配置基础参数
const service = axios.create({
  baseURL: '/api',          // API 基础路径，所有请求会自动添加此前缀
  timeout: 30000,           // 请求超时时间：30秒
  headers: {
    'Content-Type': 'application/json'  // 默认请求体格式为 JSON
  }
})

// Token 刷新状态管理（防止并发请求同时触发刷新）
let isRefreshing = false
let refreshSubscribers = []

function onRefreshed(newToken) {
  refreshSubscribers.forEach(cb => cb(newToken))
  refreshSubscribers = []
}

function addRefreshSubscriber(cb) {
  refreshSubscribers.push(cb)
}

// 请求拦截器 - 在每个请求发送前执行，用于注入认证信息
service.interceptors.request.use(
  (config) => {
    // 从用户状态管理中获取当前令牌
    const userStore = useUserStore()
    const token = userStore.token

    // 如果存在令牌，在请求头中添加 Authorization 字段（Bearer 认证方式）
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`
    }

    // 添加租户ID到请求头，用于后端多租户数据隔离
    // 优先使用 userInfo 中的 tenantId，否则使用 localStorage 中的值，最后使用默认值 1
    let tenantId = userStore.userInfo?.tenantId
    if (!tenantId) {
      const savedTenantId = localStorage.getItem('tenantId')
      tenantId = savedTenantId ? parseInt(savedTenantId) : 1
    }
    config.headers['X-Tenant-Id'] = tenantId

    // 添加用户ID到请求头，用于后端记录创建人/更新人
    // 注意：后端返回的字段名是 userId，不是 id
    if (userStore.userInfo?.userId) {
      config.headers['X-User-Id'] = userStore.userInfo.userId
    }

    // 添加用户名到请求头，用于后端获取当前用户信息
    if (userStore.userInfo?.userName) {
      config.headers['X-Username'] = userStore.userInfo.userName
    }

    return config
  },
  (error) => {
    console.error('Request error:', error)
    return Promise.reject(error)
  }
)

// 响应拦截器 - 在收到响应后执行，用于统一处理业务错误和 HTTP 错误
service.interceptors.response.use(
  (response) => {
    const res = response.data

    // 如果是文件下载请求（blob 类型），直接返回原始响应对象
    if (response.config.responseType === 'blob') {
      return response
    }

    // 业务状态码判断（后端返回的 code 不为 200 时表示业务异常）
    if (res.code !== 200) {
      // 使用 grouped 模式避免重复消息
      ElMessage({
        message: res.message || '请求失败',
        type: 'error',
        duration: 3000,
        showClose: true,
        grouping: true
      })

      // 401 未授权 - 尝试刷新 Token
      if (res.code === 401) {
        const config = response.config
        const userStore = useUserStore()
        const storedRefreshToken = userStore.refreshToken

        if (!storedRefreshToken) {
          userStore.clearAuth()
          router.push('/login')
          return Promise.reject(new Error(res.message || 'Error'))
        }

        if (!isRefreshing) {
          isRefreshing = true
          // 使用原始 axios 实例发送刷新请求，避免拦截器循环
          axios.post('/api/auth/refresh', { refreshToken: storedRefreshToken })
            .then(res => {
              const data = res.data?.data || res.data
              if (data?.accessToken) {
                userStore.token = data.accessToken
                userStore.refreshToken = data.refreshToken
                localStorage.setItem('token', data.accessToken)
                localStorage.setItem('refreshToken', data.refreshToken)
                onRefreshed(data.accessToken)
              } else {
                throw new Error('刷新令牌响应无效')
              }
            })
            .catch(() => {
              // 刷新失败，清除认证信息并跳转登录页
              userStore.clearAuth()
              router.push('/login')
              refreshSubscribers = []
            })
            .finally(() => {
              isRefreshing = false
            })
        }

        // 将当前请求加入等待队列，刷新成功后自动重试
        return new Promise((resolve) => {
          addRefreshSubscriber((newToken) => {
            config.headers['Authorization'] = `Bearer ${newToken}`
            resolve(service(config))
          })
        })
      }

      return Promise.reject(new Error(res.message || 'Error'))
    }

    // 正常响应，返回业务数据
    return res.data
  },
  (error) => {
    console.error('Response error:', error)

    // 根据 HTTP 状态码显示不同的错误提示
    if (error.response) {
      const status = error.response.status

      // 401 - 尝试刷新 Token 后重试
      if (status === 401) {
        const config = error.config
        const userStore = useUserStore()
        const storedRefreshToken = userStore.refreshToken

        if (!storedRefreshToken) {
          userStore.clearAuth()
          router.push('/login')
          return Promise.reject(error)
        }

        if (!isRefreshing) {
          isRefreshing = true
          return axios.post('/api/auth/refresh', { refreshToken: storedRefreshToken })
            .then(res => {
              const data = res.data?.data || res.data
              if (data?.accessToken) {
                userStore.token = data.accessToken
                userStore.refreshToken = data.refreshToken
                localStorage.setItem('token', data.accessToken)
                localStorage.setItem('refreshToken', data.refreshToken)
                onRefreshed(data.accessToken)
                // 重试原请求
                config.headers['Authorization'] = `Bearer ${data.accessToken}`
                return service(config)
              } else {
                throw new Error('刷新令牌响应无效')
              }
            })
            .catch(() => {
              userStore.clearAuth()
              router.push('/login')
              refreshSubscribers = []
              return Promise.reject(error)
            })
            .finally(() => {
              isRefreshing = false
            })
        }

        // 正在刷新中，加入等待队列
        return new Promise((resolve) => {
          addRefreshSubscriber((newToken) => {
            config.headers['Authorization'] = `Bearer ${newToken}`
            resolve(service(config))
          })
        })
      }

      let errorMsg = ''
      switch (status) {
        case 403:
          errorMsg = '没有权限访问'
          break
        case 404:
          errorMsg = '请求的资源不存在'
          break
        case 500:
          errorMsg = '服务器内部错误'
          break
        default:
          errorMsg = error.message || '请求失败'
      }

      ElMessage({
        message: errorMsg,
        type: 'error',
        duration: 3000,
        showClose: true,
        grouping: true
      })
    } else {
      // 无响应对象，通常是网络连接问题
      ElMessage({
        message: '网络错误，请检查网络连接',
        type: 'error',
        duration: 3000,
        showClose: true,
        grouping: true
      })
    }

    return Promise.reject(error)
  }
)

/**
 * GET 请求封装
 * @param url 请求路径
 * @param params URL 查询参数
 * @param config 额外的 axios 配置
 */
export function get(url, params, config) {
  return service.get(url, { params, ...config })
}

/**
 * POST 请求封装（通常用于创建资源）
 * @param url 请求路径
 * @param data 请求体数据
 * @param config 额外的 axios 配置
 */
export function post(url, data, config) {
  return service.post(url, data, config)
}

/**
 * PUT 请求封装（通常用于更新资源）
 * @param url 请求路径
 * @param data 请求体数据
 * @param config 额外的 axios 配置
 */
export function put(url, data, config) {
  return service.put(url, data, config)
}

/**
 * DELETE 请求封装（用于删除资源）
 * @param url 请求路径
 * @param params URL 查询参数
 * @param config 额外的 axios 配置
 */
export function del(url, params, config) {
  return service.delete(url, { params, ...config })
}

// 导出 axios 实例，供特殊场景直接使用
export default service
