/**
 * 全局登出工具 - 清除所有认证状态并跳转到登录页
 */
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/stores/user'

/**
 * 执行全局登出
 * @param {string} reason - 登出原因
 * @param {boolean} showMessage - 是否显示提示消息
 */
export async function globalLogout(reason = '登录已过期，请重新登录', showMessage = true) {
  try {
    const userStore = useUserStore()

    // 断开 SSE 连接
    if (userStore.disconnectSSE) {
      userStore.disconnectSSE()
    }

    // 清除认证状态
    userStore.clearAuth()

    // 清除 localStorage 中的所有认证相关数据
    localStorage.removeItem('token')
    localStorage.removeItem('refreshToken')
    localStorage.removeItem('userInfo')

    if (showMessage) {
      ElMessage.warning(reason)
    }

    // 跳转到登录页
    window.location.href = '/login'
  } catch (error) {
    console.error('全局登出失败:', error)
    // 强制跳转
    window.location.href = '/login'
  }
}

/**
 * 被踢下线通知登出
 * @param {string} message - 踢下线消息
 */
export async function kickoutLogout(message = '您的账号已在其他设备登录，请重新登录') {
  try {
    await ElMessageBox.alert(message, '下线通知', {
      confirmButtonText: '确定',
      type: 'warning'
    })
  } catch {
    // 用户关闭对话框
  }
  await globalLogout(message, false)
}
