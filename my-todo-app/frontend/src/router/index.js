// router/index.js - 路由配置与导航守卫
// 定义应用的所有页面路由，包括登录页、布局容器页、系统管理、字典管理、进销存、财务管理等模块
// 配置路由守卫实现登录鉴权和页面标题自动更新

import { createRouter, createWebHistory } from 'vue-router'
import NProgress from 'nprogress'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'

/**
 * 检查 JWT Token 是否已过期
 * 通过解码 Token 的 payload 部分，检查 exp 字段是否小于当前时间
 * @param {string} token JWT Token
 * @returns {boolean} true 表示已过期
 */
function isTokenExpired(token) {
  if (!token) return true
  try {
    const payload = JSON.parse(atob(token.split('.')[1]))
    return payload.exp * 1000 < Date.now()
  } catch {
    return true
  }
}

// 路由配置表 - 扁平化结构，避免嵌套路由的复杂性
const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/index.vue'),
    meta: { title: '登录', requiresAuth: false }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/register/index.vue'),
    meta: { title: '租户注册', requiresAuth: false }
  },
  {
    path: '/',
    component: () => import('@/layouts/BasicLayout.vue'),
    redirect: '/dashboard',
    meta: { requiresAuth: true },
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/dashboard/index.vue'),
        meta: { title: '工作台', icon: 'Odometer' }
      },
      // 系统管理
      {
        path: 'system/user',
        name: 'User',
        component: () => import('@/views/system/user/index.vue'),
        meta: { title: '用户管理', icon: 'User' }
      },
      {
        path: 'system/role',
        name: 'Role',
        component: () => import('@/views/system/role-manage/index.vue'),
        meta: { title: '角色管理', icon: 'UserFilled' }
      },
      {
        path: 'system/permission',
        name: 'Permission',
        component: () => import('@/views/system/permission/index.vue'),
        meta: { title: '权限管理', icon: 'Lock' }
      },
      {
        path: 'system/dept',
        name: 'Department',
        component: () => import('@/views/system/dept/index.vue'),
        meta: { title: '部门管理', icon: 'OfficeBuilding' }
      },
      {
        path: 'system/gateway',
        name: 'GatewayMonitor',
        component: () => import('@/views/system/gateway/index.vue'),
        meta: { title: '网关监控', icon: 'Monitor' }
      },
      {
        path: 'system/cache',
        name: 'CacheManage',
        component: () => import('@/views/system/cache/index.vue'),
        meta: { title: '缓存管理', icon: 'Coin' }
      },
      {
        path: 'system/canary',
        name: 'CanaryRelease',
        component: () => import('@/views/system/canary/index.vue'),
        meta: { title: '金丝雀发布', icon: 'Promotion' }
      },
      {
        path: 'system/monitor',
        name: 'RealtimeMonitor',
        component: () => import('@/views/system/monitor/index.vue'),
        meta: { title: '实时监控', icon: 'DataAnalysis' }
      },
      {
        path: 'system/permission-dynamic',
        name: 'PermissionDynamic',
        component: () => import('@/views/system/permission-dynamic/index.vue'),
        meta: { title: '动态权限', icon: 'Setting' }
      },
      {
        path: 'system/session',
        name: 'SessionManage',
        component: () => import('@/views/system/session/index.vue'),
        meta: { title: '会话管理', icon: 'Connection' }
      },
      {
        path: 'system/blacklist',
        name: 'Blacklist',
        component: () => import('@/views/system/blacklist/index.vue'),
        meta: { title: '黑名单管理', icon: 'Warning' }
      },
      {
        path: 'system/permission-template',
        name: 'PermissionTemplate',
        component: () => import('@/views/system/permission-template/index.vue'),
        meta: { title: '权限模板', icon: 'Document' }
      },
      {
        path: 'system/data-rule',
        name: 'DataRule',
        component: () => import('@/views/system/data-rule/index.vue'),
        meta: { title: '数据权限', icon: 'Filter' }
      },
      {
        path: 'system/role-inheritance',
        name: 'RoleInheritance',
        component: () => import('@/views/system/role-inheritance/index.vue'),
        meta: { title: '角色继承', icon: 'Share' }
      },
      {
        path: 'system/menu',
        name: 'MenuManage',
        component: () => import('@/views/system/menu/index.vue'),
        meta: { title: '菜单管理', icon: 'Menu' }
      },
      {
        path: 'system/tenant',
        name: 'TenantManage',
        component: () => import('@/views/system/tenant/index.vue'),
        meta: { title: '租户管理', icon: 'OfficeBuilding' }
      },
      {
        path: 'system/profile',
        name: 'Profile',
        component: () => import('@/views/system/profile/index.vue'),
        meta: { title: '个人中心', icon: 'User', requiresAuth: true }
      },
      // 字典管理
      {
        path: 'dict/type',
        name: 'DictType',
        component: () => import('@/views/dict/type/index.vue'),
        meta: { title: '字典类型', icon: 'Files' }
      },
      {
        path: 'dict/config',
        name: 'SystemConfig',
        component: () => import('@/views/dict/config/index.vue'),
        meta: { title: '系统配置', icon: 'Tools' }
      },
      {
        path: 'dict/parameter',
        name: 'ParameterManage',
        component: () => import('@/views/dict/parameter/index.vue'),
        meta: { title: '参数管理', icon: 'Setting' }
      },
      {
        path: 'dict/api-market',
        name: 'ApiMarket',
        component: () => import('@/views/dict/api-market/index.vue'),
        meta: { title: 'API市场', icon: 'Connection' }
      },
      {
        path: 'dict/third-party',
        name: 'ThirdPartyApi',
        component: () => import('@/views/dict/third-party/index.vue'),
        meta: { title: '第三方API', icon: 'Link' }
      },
      {
        path: 'dict/package',
        name: 'SaaSBookmark',
        component: () => import('@/views/dict/package/index.vue'),
        meta: { title: 'SaaS套餐', icon: 'Box' }
      },
      {
        path: 'dict/activity',
        name: 'ActivityManage',
        component: () => import('@/views/dict/activity/index.vue'),
        meta: { title: '营销活动', icon: 'Present' }
      },
      {
        path: 'dict/trace',
        name: 'TraceManage',
        component: () => import('@/views/dict/trace/index.vue'),
        meta: { title: '追踪管理', icon: 'View' }
      },
      {
        path: 'dict/codegen',
        name: 'CodeGeneration',
        component: () => import('@/views/dict/codegen/index.vue'),
        meta: { title: '代码生成', icon: 'DocumentCopy' }
      },
      {
        path: 'dict/error-doc',
        name: 'ErrorDoc',
        component: () => import('@/views/dict/error-doc/index.vue'),
        meta: { title: '错误文档', icon: 'Warning' }
      },
      // ERP
      {
        path: 'erp/product-category',
        name: 'ProductCategory',
        component: () => import('@/views/erp/product-category/index.vue'),
        meta: { title: '商品分类', icon: 'Folder' }
      },
      {
        path: 'erp/product',
        name: 'Product',
        component: () => import('@/views/erp/product/index.vue'),
        meta: { title: '商品管理', icon: 'Goods' }
      },
      {
        path: 'erp/product-price',
        name: 'ProductPrice',
        component: () => import('@/views/erp/product-price/index.vue'),
        meta: { title: '商品价格', icon: 'PriceTag' }
      },
      {
        path: 'erp/product-promotion',
        name: 'ProductPromotion',
        component: () => import('@/views/erp/product-promotion/index.vue'),
        meta: { title: '商品促销', icon: 'Present' }
      },
      {
        path: 'erp/warehouse',
        name: 'Warehouse',
        component: () => import('@/views/erp/warehouse/index.vue'),
        meta: { title: '仓库管理', icon: 'House' }
      },
      {
        path: 'erp/inventory',
        name: 'Inventory',
        component: () => import('@/views/erp/inventory/index.vue'),
        meta: { title: '库存管理', icon: 'Box' }
      },
      {
        path: 'erp/inventory-flow',
        name: 'InventoryFlow',
        component: () => import('@/views/erp/inventory-flow/index.vue'),
        meta: { title: '库存流水', icon: 'List' }
      },
      {
        path: 'erp/inventory-check',
        name: 'InventoryCheck',
        component: () => import('@/views/erp/inventory-check/index.vue'),
        meta: { title: '库存盘点', icon: 'Document' }
      },
      {
        path: 'erp/inventory-alert',
        name: 'InventoryAlert',
        component: () => import('@/views/erp/inventory-alert/index.vue'),
        meta: { title: '库存预警', icon: 'Warning' }
      },
      {
        path: 'erp/supplier',
        name: 'Supplier',
        component: () => import('@/views/erp/supplier/index.vue'),
        meta: { title: '供应商管理', icon: 'Van' }
      },
      {
        path: 'erp/customer',
        name: 'Customer',
        component: () => import('@/views/erp/customer/index.vue'),
        meta: { title: '客户管理', icon: 'Avatar' }
      },
      {
        path: 'erp/purchase-order',
        name: 'PurchaseOrder',
        component: () => import('@/views/erp/purchase-order/index.vue'),
        meta: { title: '采购订单', icon: 'ShoppingCartFull' }
      },
      {
        path: 'erp/purchase-return',
        name: 'PurchaseReturn',
        component: () => import('@/views/erp/purchase-return/index.vue'),
        meta: { title: '采购退货', icon: 'RefreshLeft' }
      },
      {
        path: 'erp/sales-order',
        name: 'SalesOrder',
        component: () => import('@/views/erp/sales-order/index.vue'),
        meta: { title: '销售订单', icon: 'ShoppingCart' }
      },
      {
        path: 'erp/sales-return',
        name: 'SalesReturn',
        component: () => import('@/views/erp/sales-return/index.vue'),
        meta: { title: '销售退货', icon: 'RefreshRight' }
      },
      {
        path: 'erp/sales-shipment',
        name: 'SalesShipment',
        component: () => import('@/views/erp/sales-shipment/index.vue'),
        meta: { title: '销售出库', icon: 'Van' }
      },
      {
        path: 'erp/report',
        name: 'ErpReport',
        component: () => import('@/views/erp/report/index.vue'),
        meta: { title: '报表统计', icon: 'DataAnalysis' }
      },
      {
        path: 'erp/config',
        name: 'ErpConfig',
        component: () => import('@/views/erp/config/index.vue'),
        meta: { title: '系统配置', icon: 'Setting' }
      },
      // 财务管理
      {
        path: 'finance/receivable',
        name: 'Receivable',
        component: () => import('@/views/finance/receivable/index.vue'),
        meta: { title: '应收账款', icon: 'CreditCard' }
      },
      {
        path: 'finance/payable',
        name: 'Payable',
        component: () => import('@/views/finance/payable/index.vue'),
        meta: { title: '应付账款', icon: 'Wallet' }
      },
      {
        path: 'finance/record',
        name: 'PaymentRecord',
        component: () => import('@/views/finance/record/index.vue'),
        meta: { title: '收支记录', icon: 'Tickets' }
      },
      {
        path: 'finance/bank-account',
        name: 'BankAccount',
        component: () => import('@/views/finance/bank-account/index.vue'),
        meta: { title: '银行账户', icon: 'Postcard' }
      },
      {
        path: 'finance/invoice',
        name: 'FinanceInvoice',
        component: () => import('@/views/finance/invoice/index.vue'),
        meta: { title: '发票管理', icon: 'Document' }
      },
      {
        path: 'finance/cost',
        name: 'FinanceCost',
        component: () => import('@/views/finance/cost/index.vue'),
        meta: { title: '成本核算', icon: 'DataAnalysis' }
      },
      {
        path: 'finance/report',
        name: 'FinanceReport',
        component: () => import('@/views/finance/report/index.vue'),
        meta: { title: '财务报表', icon: 'TrendCharts' }
      },
      {
        path: 'finance/bank-reconciliation',
        name: 'BankReconciliation',
        component: () => import('@/views/finance/bank-reconciliation/index.vue'),
        meta: { title: '银行对账', icon: 'Connection' }
      },
      {
        path: 'finance/budget',
        name: 'Budget',
        component: () => import('@/views/finance/budget/index.vue'),
        meta: { title: '预算管理', icon: 'Money' }
      }
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/error/404.vue'),
    meta: { title: '404' }
  }
]

// 创建路由实例
const router = createRouter({
  history: createWebHistory(),
  routes
})

// 全局前置守卫
router.beforeEach(async (to, from, next) => {
  NProgress.start()
  document.title = `${to.meta.title || '首页'} - My Todo App`

  const userStore = useUserStore()
  const token = userStore.token

  // Token 过期检查：主动在前端检测 Token 是否已过期，避免发送无效请求
  if (token && isTokenExpired(token)) {
    ElMessage.warning('登录已过期，请重新登录')
    userStore.clearAuth()
    next('/login')
    NProgress.done()
    return
  }

  if (to.meta.requiresAuth !== false && !token) {
    next('/login')
  } else if (to.path === '/login' && token) {
    next('/dashboard')
  } else {
    // token 存在但权限为空（页面刷新或权限加载失败），自动重新获取
    if (token && userStore.permissions.length === 0) {
      try {
        await userStore.refreshPermissions()
      } catch (error) {
        // 权限获取失败（token 过期等），跳转登录页
        console.error('刷新权限失败:', error)
        userStore.clearAuth()
        next('/login')
        return
      }
    }
    next()
  }
})

router.afterEach(() => {
  NProgress.done()
})

export default router
