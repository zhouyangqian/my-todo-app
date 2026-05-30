// router/dynamicRoutes.js - 动态路由加载器
// 根据后端返回的菜单权限动态生成 Vue Router 路由配置
// 后端菜单数据通过 /permissions/menus/user-menus 接口获取

import { getUserMenus } from '@/api/permission'

/**
 * 视图组件路径映射表
 * 将后端菜单的 path 字段映射到实际的 Vue 组件文件路径
 * key 为菜单 path（去除前导斜杠），value 为组件导入函数
 */
const viewModules = import.meta.glob('../views/**/*.vue')

/**
 * 将后端菜单数据转换为 Vue Router 路由配置
 * @param {Array} menus 后端返回的菜单列表（扁平或树形结构）
 * @returns {Array} Vue Router 路由配置数组
 */
export function buildRoutesFromMenus(menus) {
  if (!menus || menus.length === 0) return []

  const routes = []

  menus.forEach(menu => {
    if (menu.type === 2) {
      // type=2 为按钮级别权限，不生成路由
      return
    }

    const route = {
      path: menu.path,
      name: menu.name || menu.code,
      meta: {
        title: menu.title || menu.name,
        icon: menu.icon || '',
        permission: menu.permission || '',
        hidden: menu.hidden || false
      }
    }

    // 解析组件路径
    if (menu.component) {
      const componentPath = `../views/${menu.component}.vue`
      if (viewModules[componentPath]) {
        route.component = viewModules[componentPath]
      } else {
        // 组件不存在时使用占位组件
        route.component = () => import('../views/error/404.vue')
        console.warn(`[动态路由] 组件未找到: ${componentPath}, 菜单: ${menu.title}`)
      }
    } else if (menu.children && menu.children.length > 0) {
      // 有子菜单但无自身组件，仅作为布局容器
      route.children = buildRoutesFromMenus(menu.children)
    }

    // 处理子路由
    if (menu.children && menu.children.length > 0 && route.component) {
      route.children = buildRoutesFromMenus(menu.children)
    }

    routes.push(route)
  })

  return routes
}

/**
 * 从后端加载用户菜单并转换为动态路由
 * @returns {Promise<Array>} Vue Router 路由配置数组
 */
export async function loadDynamicRoutes() {
  try {
    const res = await getUserMenus()
    const menus = res.data || res || []
    return buildRoutesFromMenus(menus)
  } catch (error) {
    console.error('[动态路由] 加载菜单失败:', error)
    return []
  }
}
