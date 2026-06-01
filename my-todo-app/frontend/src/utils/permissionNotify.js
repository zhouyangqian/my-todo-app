/**
 * 权限变更通知工具
 * 基于SSE监听权限更新事件
 */
import { ElNotification } from 'element-plus'
import { useUserStore } from '@/stores/user'

let permissionChangeListener = null

/**
 * 设置权限变更监听
 * @param {EventSource} eventSource - SSE连接实例
 */
export function setupPermissionNotify(eventSource) {
  if (!eventSource) return

  // 监听权限变更事件
  permissionChangeListener = (event) => {
    try {
      const data = JSON.parse(event.data)
      const userStore = useUserStore()

      switch (data.type) {
        case 'PERMISSION_UPDATED':
          ElNotification({
            title: '权限变更通知',
            message: '您的权限已更新，部分功能可能已变更',
            type: 'info',
            duration: 5000
          })
          // 刷新权限
          if (userStore.refreshPermissions) {
            userStore.refreshPermissions()
          }
          break
        case 'ROLE_UPDATED':
          ElNotification({
            title: '角色变更通知',
            message: '您的角色配置已更新',
            type: 'info',
            duration: 5000
          })
          break
        default:
          break
      }
    } catch (error) {
      console.error('处理权限变更通知失败:', error)
    }
  }

  eventSource.addEventListener('permission', permissionChangeListener)
}

/**
 * 移除权限变更监听
 * @param {EventSource} eventSource - SSE连接实例
 */
export function removePermissionNotify(eventSource) {
  if (eventSource && permissionChangeListener) {
    eventSource.removeEventListener('permission', permissionChangeListener)
    permissionChangeListener = null
  }
}
