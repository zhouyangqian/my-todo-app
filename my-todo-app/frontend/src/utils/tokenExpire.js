/**
 * JWT Token 过期检查工具
 */

/**
 * 检查 token 是否已过期
 * @param {string} token - JWT token
 * @returns {boolean} 是否已过期
 */
export function isTokenExpired(token) {
  if (!token) return true
  try {
    const payload = JSON.parse(atob(token.split('.')[1]))
    return payload.exp * 1000 < Date.now()
  } catch {
    return true
  }
}

/**
 * 检查 token 是否即将过期（5分钟内）
 * @param {string} token - JWT token
 * @returns {boolean} 是否即将过期
 */
export function isTokenExpiringSoon(token) {
  if (!token) return true
  try {
    const payload = JSON.parse(atob(token.split('.')[1]))
    const expTime = payload.exp * 1000
    const fiveMinutes = 5 * 60 * 1000
    return expTime - Date.now() < fiveMinutes
  } catch {
    return true
  }
}

/**
 * 获取 token 剩余有效时间（毫秒）
 * @param {string} token - JWT token
 * @returns {number} 剩余毫秒数，过期返回 0
 */
export function getTokenRemainingTime(token) {
  if (!token) return 0
  try {
    const payload = JSON.parse(atob(token.split('.')[1]))
    const remaining = payload.exp * 1000 - Date.now()
    return remaining > 0 ? remaining : 0
  } catch {
    return 0
  }
}
