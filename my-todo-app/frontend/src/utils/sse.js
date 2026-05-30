// utils/sse.js - SSE（Server-Sent Events）客户端
// 管理与后端的 SSE 长连接，接收实时踢出通知等事件

import { ElMessage } from 'element-plus'
import router from '@/router'
import { useUserStore } from '@/stores/user'

let eventSource = null
let reconnectTimer = null
let reconnectAttempts = 0

/** 最大重连次数 */
const MAX_RECONNECT_ATTEMPTS = 5
/** 重连基础延迟（毫秒） */
const BASE_RECONNECT_DELAY = 3000

/**
 * 建立 SSE 连接
 * <p>
 * 通过 EventSource 连接后端 SSE 端点，监听踢出和心跳事件。
 * Token 通过 query 参数传递（EventSource 不支持自定义请求头）。
 * </p>
 *
 * @param token JWT 访问令牌
 */
export function connectSSE(token) {
  // 先断开已有连接
  disconnectSSE()

  if (!token) {
    console.warn('SSE 连接失败：缺少 Token')
    return
  }

  const url = `/api/auth/sse/connect?token=${encodeURIComponent(token)}`
  console.log('建立 SSE 连接...')

  eventSource = new EventSource(url)

  // 监听踢出事件
  eventSource.addEventListener('kick', (event) => {
    console.warn('收到踢出通知:', event.data)
    ElMessage({
      message: event.data || '您已被迫下线',
      type: 'warning',
      duration: 5000,
      showClose: true
    })
    // 断开 SSE 连接
    disconnectSSE()
    // 清除本地认证信息并跳转到登录页
    const userStore = useUserStore()
    userStore.clearAuth()
    router.push('/login')
  })

  // 监听心跳事件
  eventSource.addEventListener('heartbeat', () => {
    // 收到心跳，重置重连计数
    reconnectAttempts = 0
  })

  // 连接打开
  eventSource.onopen = () => {
    console.log('SSE 连接已建立')
    reconnectAttempts = 0
  }

  // 连接错误，自动重连
  eventSource.onerror = () => {
    console.warn('SSE 连接错误')

    if (eventSource) {
      eventSource.close()
      eventSource = null
    }

    // 非登录状态下不重连
    const savedToken = localStorage.getItem('token')
    if (!savedToken) {
      return
    }

    // 指数退避重连
    if (reconnectAttempts < MAX_RECONNECT_ATTEMPTS) {
      const delay = BASE_RECONNECT_DELAY * Math.pow(2, reconnectAttempts)
      reconnectAttempts++
      console.log(`SSE 将在 ${delay}ms 后重连（第 ${reconnectAttempts} 次）`)

      reconnectTimer = setTimeout(() => {
        connectSSE(savedToken)
      }, delay)
    } else {
      console.warn('SSE 重连次数已达上限，停止重连')
    }
  }
}

/**
 * 断开 SSE 连接
 */
export function disconnectSSE() {
  if (reconnectTimer) {
    clearTimeout(reconnectTimer)
    reconnectTimer = null
  }

  if (eventSource) {
    eventSource.close()
    eventSource = null
    console.log('SSE 连接已断开')
  }

  reconnectAttempts = 0
}
