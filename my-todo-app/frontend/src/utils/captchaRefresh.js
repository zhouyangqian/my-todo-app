/**
 * 验证码自动刷新工具
 */

let refreshTimer = null

/**
 * 启动验证码自动刷新
 * @param {Function} refreshFn - 刷新回调函数
 * @param {number} interval - 刷新间隔（毫秒），默认 120000 (2分钟)
 */
export function startCaptchaAutoRefresh(refreshFn, interval = 120000) {
  stopCaptchaAutoRefresh()
  if (typeof refreshFn === 'function') {
    refreshTimer = setInterval(() => {
      refreshFn()
    }, interval)
  }
}

/**
 * 停止验证码自动刷新
 */
export function stopCaptchaAutoRefresh() {
  if (refreshTimer) {
    clearInterval(refreshTimer)
    refreshTimer = null
  }
}

/**
 * 验证失败后刷新验证码
 * @param {Function} refreshFn - 刷新回调函数
 * @param {number} delay - 延迟刷新时间（毫秒），默认 500
 */
export function refreshOnFail(refreshFn, delay = 500) {
  if (typeof refreshFn === 'function') {
    setTimeout(() => {
      refreshFn()
    }, delay)
  }
}
