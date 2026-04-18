// utils/request.ts - HTTP 请求封装模块
// 基于 axios 封装统一的请求方法，包含请求/响应拦截器、Token 自动注入、错误处理等

import axios, { AxiosInstance, AxiosRequestConfig, AxiosResponse, InternalAxiosRequestConfig } from 'axios'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import router from '@/router'

// 创建 axios 实例，配置基础参数
const service: AxiosInstance = axios.create({
  baseURL: '/api',          // API 基础路径，所有请求会自动添加此前缀
  timeout: 30000,           // 请求超时时间：30秒
  headers: {
    'Content-Type': 'application/json'  // 默认请求体格式为 JSON
  }
})

// 请求拦截器 - 在每个请求发送前执行，用于注入认证信息
service.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    // 从用户状态管理中获取当前令牌
    const userStore = useUserStore()
    const token = userStore.token

    // 如果存在令牌，在请求头中添加 Authorization 字段（Bearer 认证方式）
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`
    }

    // 添加租户ID到请求头，用于后端多租户数据隔离
    const tenantId = userStore.userInfo?.tenantId || 1
    config.headers['X-Tenant-Id'] = tenantId

    return config
  },
  (error) => {
    console.error('Request error:', error)
    return Promise.reject(error)
  }
)

// 响应拦截器 - 在收到响应后执行，用于统一处理业务错误和 HTTP 错误
service.interceptors.response.use(
  (response: AxiosResponse) => {
    const res = response.data

    // 如果是文件下载请求（blob 类型），直接返回原始响应对象
    if (response.config.responseType === 'blob') {
      return response
    }

    // 业务状态码判断（后端返回的 code 不为 200 时表示业务异常）
    if (res.code !== 200) {
      ElMessage.error(res.message || '请求失败')

      // 401 未授权 - 令牌过期或无效，清除认证信息并跳转到登录页
      if (res.code === 401) {
        const userStore = useUserStore()
        userStore.clearAuth()
        router.push('/login')
      }

      return Promise.reject(new Error(res.message || 'Error'))
    }

    // 正常响应，返回业务数据
    return res
  },
  (error) => {
    console.error('Response error:', error)

    // 根据 HTTP 状态码显示不同的错误提示
    if (error.response) {
      const status = error.response.status

      switch (status) {
        case 401:
          // 未授权 - 令牌失效，需要重新登录
          ElMessage.error('登录已过期，请重新登录')
          const userStore = useUserStore()
          userStore.clearAuth()
          router.push('/login')
          break
        case 403:
          // 禁止访问 - 当前用户无权限
          ElMessage.error('没有权限访问')
          break
        case 404:
          // 资源不存在
          ElMessage.error('请求的资源不存在')
          break
        case 500:
          // 服务器内部错误
          ElMessage.error('服务器内部错误')
          break
        default:
          ElMessage.error(error.message || '请求失败')
      }
    } else {
      // 无响应对象，通常是网络连接问题
      ElMessage.error('网络错误，请检查网络连接')
    }

    return Promise.reject(error)
  }
)

// 后端统一响应数据结构类型
export interface ApiResponse<T = any> {
  code: number             // 业务状态码（200 表示成功）
  message: string          // 响应消息
  data: T                  // 业务数据（泛型）
  timestamp: number        // 服务器时间戳
}

/**
 * GET 请求封装
 * @param url 请求路径
 * @param params URL 查询参数
 * @param config 额外的 axios 配置
 */
export function get<T = any>(url: string, params?: any, config?: AxiosRequestConfig): Promise<ApiResponse<T>> {
  return service.get(url, { params, ...config })
}

/**
 * POST 请求封装（通常用于创建资源）
 * @param url 请求路径
 * @param data 请求体数据
 * @param config 额外的 axios 配置
 */
export function post<T = any>(url: string, data?: any, config?: AxiosRequestConfig): Promise<ApiResponse<T>> {
  return service.post(url, data, config)
}

/**
 * PUT 请求封装（通常用于更新资源）
 * @param url 请求路径
 * @param data 请求体数据
 * @param config 额外的 axios 配置
 */
export function put<T = any>(url: string, data?: any, config?: AxiosRequestConfig): Promise<ApiResponse<T>> {
  return service.put(url, data, config)
}

/**
 * DELETE 请求封装（用于删除资源）
 * @param url 请求路径
 * @param params URL 查询参数
 * @param config 额外的 axios 配置
 */
export function del<T = any>(url: string, params?: any, config?: AxiosRequestConfig): Promise<ApiResponse<T>> {
  return service.delete(url, { params, ...config })
}

// 导出 axios 实例，供特殊场景直接使用
export default service
