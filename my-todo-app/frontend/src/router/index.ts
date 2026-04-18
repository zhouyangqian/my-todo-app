// router/index.ts - 路由配置与导航守卫
// 定义应用的所有页面路由，包括登录页、布局容器页、系统管理、字典管理、进销存、财务管理等模块
// 配置路由守卫实现登录鉴权和页面标题自动更新

import { createRouter, createWebHistory, RouteRecordRaw } from 'vue-router'
import NProgress from 'nprogress'
import { useUserStore } from '@/stores/user'

// 路由配置表 - 定义所有页面路由的路径、组件和元信息
const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/index.vue'),  // 登录页（懒加载）
    meta: { title: '登录', requiresAuth: false }         // 不需要登录即可访问
  },
  {
    path: '/',
    component: () => import('@/layouts/BasicLayout.vue'),  // 主布局容器（侧边栏+头部+内容区）
    redirect: '/dashboard',                                // 默认重定向到工作台
    meta: { requiresAuth: true },                          // 需要登录才能访问
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/dashboard/index.vue'),
        meta: { title: '工作台', icon: 'Odometer' }
      },
      // 系统管理模块
      {
        path: 'system',
        name: 'System',
        redirect: '/system/user',
        meta: { title: '系统管理', icon: 'Setting' },
        children: [
          {
            path: 'user',
            name: 'User',
            component: () => import('@/views/system/user/index.vue'),
            meta: { title: '用户管理', icon: 'User' }
          },
          {
            path: 'role',
            name: 'Role',
            component: () => import('@/views/system/role/index.vue'),
            meta: { title: '角色管理', icon: 'UserFilled' }
          },
          {
            path: 'permission',
            name: 'Permission',
            component: () => import('@/views/system/permission/index.vue'),
            meta: { title: '权限管理', icon: 'Lock' }
          },
          {
            path: 'dept',
            name: 'Department',
            component: () => import('@/views/system/dept/index.vue'),
            meta: { title: '部门管理', icon: 'OfficeBuilding' }
          }
        ]
      },
      // 字典管理模块
      {
        path: 'dict',
        name: 'Dict',
        redirect: '/dict/type',
        meta: { title: '字典管理', icon: 'Collection' },
        children: [
          {
            path: 'type',
            name: 'DictType',
            component: () => import('@/views/dict/type/index.vue'),
            meta: { title: '字典类型', icon: 'Files' }
          },
          {
            path: 'config',
            name: 'SystemConfig',
            component: () => import('@/views/dict/config/index.vue'),
            meta: { title: '系统配置', icon: 'Tools' }
          }
        ]
      },
      // 进销存（ERP）管理模块
      {
        path: 'erp',
        name: 'ERP',
        redirect: '/erp/product',
        meta: { title: '进销存', icon: 'ShoppingCart' },
        children: [
          {
            path: 'product',
            name: 'Product',
            component: () => import('@/views/erp/product/index.vue'),
            meta: { title: '商品管理', icon: 'Goods' }
          },
          {
            path: 'warehouse',
            name: 'Warehouse',
            component: () => import('@/views/erp/warehouse/index.vue'),
            meta: { title: '仓库管理', icon: 'House' }
          },
          {
            path: 'inventory',
            name: 'Inventory',
            component: () => import('@/views/erp/inventory/index.vue'),
            meta: { title: '库存管理', icon: 'Box' }
          },
          {
            path: 'supplier',
            name: 'Supplier',
            component: () => import('@/views/erp/supplier/index.vue'),
            meta: { title: '供应商管理', icon: 'Van' }
          },
          {
            path: 'customer',
            name: 'Customer',
            component: () => import('@/views/erp/customer/index.vue'),
            meta: { title: '客户管理', icon: 'Avatar' }
          }
        ]
      },
      // 财务管理模块
      {
        path: 'finance',
        name: 'Finance',
        redirect: '/finance/receivable',
        meta: { title: '财务管理', icon: 'Money' },
        children: [
          {
            path: 'receivable',
            name: 'Receivable',
            component: () => import('@/views/finance/receivable/index.vue'),
            meta: { title: '应收账款', icon: 'CreditCard' }
          },
          {
            path: 'payable',
            name: 'Payable',
            component: () => import('@/views/finance/payable/index.vue'),
            meta: { title: '应付账款', icon: 'Wallet' }
          },
          {
            path: 'record',
            name: 'PaymentRecord',
            component: () => import('@/views/finance/record/index.vue'),
            meta: { title: '收支记录', icon: 'Tickets' }
          },
          {
            path: 'bank-account',
            name: 'BankAccount',
            component: () => import('@/views/finance/bank-account/index.vue'),
            meta: { title: '银行账户', icon: 'Postcard' }
          }
        ]
      }
    ]
  },
  // 404 兜底路由 - 匹配所有未定义的路径
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/error/404.vue'),
    meta: { title: '404' }
  }
]

// 创建路由实例，使用 HTML5 History 模式（URL 无 # 号）
const router = createRouter({
  history: createWebHistory(),
  routes
})

// 全局前置守卫 - 在每次路由跳转前执行
router.beforeEach((to, from, next) => {
  // 启动顶部进度条动画
  NProgress.start()

  // 动态设置浏览器标签页标题
  document.title = `${to.meta.title || '首页'} - My Todo App`

  // 获取当前用户的登录令牌
  const userStore = useUserStore()
  const token = userStore.token

  // 鉴权逻辑：
  // 1. 需要登录的页面但未登录 -> 重定向到登录页
  // 2. 已登录用户访问登录页 -> 重定向到工作台
  // 3. 其他情况正常放行
  if (to.meta.requiresAuth !== false && !token) {
    next('/login')
  } else if (to.path === '/login' && token) {
    next('/dashboard')
  } else {
    next()
  }
})

// 全局后置钩子 - 路由跳转完成后执行
router.afterEach(() => {
  // 结束顶部进度条动画
  NProgress.done()
})

export default router
