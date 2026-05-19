// router/index.js - 路由配置与导航守卫
// 定义应用的所有页面路由，包括登录页、布局容器页、系统管理、字典管理、进销存、财务管理等模块
// 配置路由守卫实现登录鉴权和页面标题自动更新

import { createRouter, createWebHistory } from 'vue-router'
import NProgress from 'nprogress'
import { useUserStore } from '@/stores/user'

// 路由配置表 - 扁平化结构，避免嵌套路由的复杂性
const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/index.vue'),
    meta: { title: '登录', requiresAuth: false }
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
        component: () => import('@/views/system/role/index.vue'),
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
router.beforeEach((to, from, next) => {
  NProgress.start()
  document.title = `${to.meta.title || '首页'} - My Todo App`

  const userStore = useUserStore()
  const token = userStore.token

  if (to.meta.requiresAuth !== false && !token) {
    next('/login')
  } else if (to.path === '/login' && token) {
    next('/dashboard')
  } else {
    next()
  }
})

router.afterEach(() => {
  NProgress.done()
})

export default router
