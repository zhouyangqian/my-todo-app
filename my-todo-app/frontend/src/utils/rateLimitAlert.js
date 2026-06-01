/**
 * 限流告警工具
 */
import { ElNotification } from 'element-plus'

/**
 * 显示限流告警
 * @param {Object} response - HTTP 响应对象
 */
export function showRateLimitAlert(response) {
  const retryAfter = response?.headers?.['retry-after'] || '60'

  ElNotification({
    title: '请求频率过高',
    message: `您的操作过于频繁，请在 ${retryAfter} 秒后重试。`,
    type: 'warning',
    duration: 5000
  })
}

/**
 * 处理 429 限流响应
 * @param {Object} error - Axios 错误对象
 * @returns {boolean} 是否为限流错误
 */
export function handleRateLimitError(error) {
  if (error?.response?.status === 429) {
    showRateLimitAlert(error.response)
    return true
  }
  return false
}
