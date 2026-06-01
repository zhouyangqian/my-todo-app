<!-- BasicLayout.vue - 应用主布局组件 -->
<template>
  <el-container class="basic-layout">
    <!-- 侧边栏导航区域 -->
    <el-aside :width="isCollapse ? '64px' : '210px'" class="sidebar">
      <div class="logo">
        <img src="@/assets/logo.svg" alt="Logo" class="logo-img" />
        <span v-show="!isCollapse" class="logo-text">My Todo App</span>
      </div>
      <!-- 侧边菜单（带滚动条） -->
      <el-scrollbar class="sidebar-scroll">
      <el-menu
        ref="menuRef"
        :default-active="activeMenu"
        :collapse="isCollapse"
        :collapse-transition="false"
        background-color="#304156"
        text-color="#bfcbd9"
        active-text-color="#409eff"
        router
      >
        <!-- 首页 -->
        <el-menu-item index="/dashboard">
          <el-icon><Odometer /></el-icon>
          <span>首页</span>
        </el-menu-item>

        <!-- 系统管理子菜单 -->
        <el-sub-menu index="system">
          <template #title>
            <el-icon><Setting /></el-icon>
            <span>系统管理</span>
          </template>
          <el-menu-item index="/system/user">
            <el-icon><User /></el-icon>
            <span>用户管理</span>
          </el-menu-item>
          <el-menu-item index="/system/role">
            <el-icon><UserFilled /></el-icon>
            <span>角色管理</span>
          </el-menu-item>
          <el-menu-item index="/system/permission">
            <el-icon><Lock /></el-icon>
            <span>权限管理</span>
          </el-menu-item>
          <el-menu-item index="/system/dept">
            <el-icon><OfficeBuilding /></el-icon>
            <span>部门管理</span>
          </el-menu-item>
          <el-menu-item index="/system/gateway">
            <el-icon><Monitor /></el-icon>
            <span>网关监控</span>
          </el-menu-item>
        </el-sub-menu>

        <!-- 字典管理子菜单 -->
        <el-sub-menu index="dict">
          <template #title>
            <el-icon><Collection /></el-icon>
            <span>字典管理</span>
          </template>
          <el-menu-item index="/dict/type">
            <el-icon><Files /></el-icon>
            <span>字典类型</span>
          </el-menu-item>
          <el-menu-item index="/dict/config">
            <el-icon><Tools /></el-icon>
            <span>系统配置</span>
          </el-menu-item>
          <el-menu-item index="/dict/parameter">
            <el-icon><Setting /></el-icon>
            <span>参数管理</span>
          </el-menu-item>
          <el-menu-item index="/dict/api-market">
            <el-icon><Connection /></el-icon>
            <span>API市场</span>
          </el-menu-item>
          <el-menu-item index="/dict/third-party">
            <el-icon><Link /></el-icon>
            <span>第三方API</span>
          </el-menu-item>
          <el-menu-item index="/dict/package">
            <el-icon><Box /></el-icon>
            <span>SaaS套餐</span>
          </el-menu-item>
          <el-menu-item index="/dict/activity">
            <el-icon><Present /></el-icon>
            <span>营销活动</span>
          </el-menu-item>
          <el-menu-item index="/dict/trace">
            <el-icon><View /></el-icon>
            <span>追踪管理</span>
          </el-menu-item>
          <el-menu-item index="/dict/codegen">
            <el-icon><DocumentCopy /></el-icon>
            <span>代码生成</span>
          </el-menu-item>
          <el-menu-item index="/dict/error-doc">
            <el-icon><Warning /></el-icon>
            <span>错误文档</span>
          </el-menu-item>
        </el-sub-menu>

        <!-- ERP子菜单 -->
        <el-sub-menu index="erp">
          <template #title>
            <el-icon><ShoppingCart /></el-icon>
            <span>进销存</span>
          </template>
          <!-- 商品管理 -->
          <el-sub-menu index="erp-product" @open="handleErpOpen">
            <template #title>
              <el-icon><Folder /></el-icon>
              <span>商品管理</span>
            </template>
            <el-menu-item index="/erp/product-category">商品分类</el-menu-item>
            <el-menu-item index="/erp/product">商品管理</el-menu-item>
            <el-menu-item index="/erp/product-price">商品价格</el-menu-item>
            <el-menu-item index="/erp/product-promotion">商品促销</el-menu-item>
          </el-sub-menu>
          <!-- 仓库库存 -->
          <el-sub-menu index="erp-inventory" @open="handleErpOpen">
            <template #title>
              <el-icon><Box /></el-icon>
              <span>仓库库存</span>
            </template>
            <el-menu-item index="/erp/warehouse">仓库管理</el-menu-item>
            <el-menu-item index="/erp/inventory">库存管理</el-menu-item>
            <el-menu-item index="/erp/inventory-flow">库存流水</el-menu-item>
            <el-menu-item index="/erp/inventory-check">库存盘点</el-menu-item>
            <el-menu-item index="/erp/inventory-alert">库存预警</el-menu-item>
          </el-sub-menu>
          <!-- 采购管理 -->
          <el-sub-menu index="erp-purchase" @open="handleErpOpen">
            <template #title>
              <el-icon><ShoppingCartFull /></el-icon>
              <span>采购管理</span>
            </template>
            <el-menu-item index="/erp/purchase-order">采购订单</el-menu-item>
            <el-menu-item index="/erp/purchase-return">采购退货</el-menu-item>
            <el-menu-item index="/erp/supplier">供应商管理</el-menu-item>
          </el-sub-menu>
          <!-- 销售管理 -->
          <el-sub-menu index="erp-sales" @open="handleErpOpen">
            <template #title>
              <el-icon><ShoppingCart /></el-icon>
              <span>销售管理</span>
            </template>
            <el-menu-item index="/erp/sales-order">销售订单</el-menu-item>
            <el-menu-item index="/erp/sales-return">销售退货</el-menu-item>
            <el-menu-item index="/erp/sales-shipment">销售出库</el-menu-item>
            <el-menu-item index="/erp/customer">客户管理</el-menu-item>
          </el-sub-menu>
          <!-- 报表配置 -->
          <el-sub-menu index="erp-report" @open="handleErpOpen">
            <template #title>
              <el-icon><DataAnalysis /></el-icon>
              <span>报表配置</span>
            </template>
            <el-menu-item index="/erp/report">报表统计</el-menu-item>
            <el-menu-item index="/erp/config">系统配置</el-menu-item>
          </el-sub-menu>
        </el-sub-menu>

        <!-- 财务子菜单 -->
        <el-sub-menu index="finance">
          <template #title>
            <el-icon><Money /></el-icon>
            <span>财务管理</span>
          </template>
          <el-menu-item index="/finance/receivable">
            <el-icon><CreditCard /></el-icon>
            <span>应收账款</span>
          </el-menu-item>
          <el-menu-item index="/finance/payable">
            <el-icon><Wallet /></el-icon>
            <span>应付账款</span>
          </el-menu-item>
          <el-menu-item index="/finance/record">
            <el-icon><Tickets /></el-icon>
            <span>收支记录</span>
          </el-menu-item>
          <el-menu-item index="/finance/bank-account">
            <el-icon><Postcard /></el-icon>
            <span>银行账户</span>
          </el-menu-item>
          <el-menu-item index="/finance/invoice">
            <el-icon><Document /></el-icon>
            <span>发票管理</span>
          </el-menu-item>
          <el-menu-item index="/finance/cost">
            <el-icon><DataAnalysis /></el-icon>
            <span>成本核算</span>
          </el-menu-item>
          <el-menu-item index="/finance/report">
            <el-icon><TrendCharts /></el-icon>
            <span>财务报表</span>
          </el-menu-item>
        </el-sub-menu>
      </el-menu>
      </el-scrollbar>
    </el-aside>

    <el-container class="main-container">
      <!-- 顶部栏 -->
      <el-header class="header">
        <div class="header-left">
          <!-- 折叠按钮 -->
          <el-icon class="collapse-btn" @click="toggleCollapse">
            <Fold v-if="!isCollapse" />
            <Expand v-else />
          </el-icon>
          <!-- 面包屑 -->
          <el-breadcrumb separator="/">
            <el-breadcrumb-item
              v-for="(item, index) in breadcrumbs"
              :key="item.path"
            >
              <span v-if="index === breadcrumbs.length - 1" class="breadcrumb-current">
                <el-icon v-if="index === 0" style="vertical-align: middle; margin-right: 4px;">
                  <HomeFilled />
                </el-icon>
                {{ item.title }}
              </span>
              <a v-else @click="handleBreadcrumbClick(item.path)" class="breadcrumb-link">
                <el-icon v-if="index === 0" style="vertical-align: middle; margin-right: 4px;">
                  <HomeFilled />
                </el-icon>
                {{ item.title }}
              </a>
            </el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <div class="header-right">
          <!-- 用户信息 -->
          <el-dropdown trigger="click">
            <span class="user-info">
              <el-avatar :size="32" :src="userStore.userInfo?.avatar">
                {{ userStore.userInfo?.realName?.charAt(0) || 'U' }}
              </el-avatar>
              <span class="username">{{ userStore.userInfo?.realName || userStore.userInfo?.userName }}</span>
              <el-icon><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="router.push('/system/profile')">个人中心</el-dropdown-item>
                <el-dropdown-item divided @click="handleLogout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <!-- 主内容区域 -->
      <el-main class="main">
        <router-view v-slot="{ Component }">
          <transition name="fade" mode="out-in">
            <keep-alive>
              <component :is="Component" />
            </keep-alive>
          </transition>
        </router-view>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import {
  Fold,
  Expand,
  ArrowDown,
  HomeFilled,
  Odometer,
  Setting,
  User,
  UserFilled,
  Lock,
  OfficeBuilding,
  Collection,
  Files,
  Tools,
  ShoppingCart,
  ShoppingCartFull,
  Goods,
  House,
  Box,
  List,
  Van,
  Avatar,
  Money,
  CreditCard,
  Wallet,
  Tickets,
  Postcard,
  Folder,
  Document,
  Warning,
  RefreshLeft,
  RefreshRight,
  PriceTag,
  DataAnalysis,
  Present,
  Monitor,
  TrendCharts,
  Connection,
  Link,
  DocumentCopy,
  View
} from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const isCollapse = ref(false)
const menuRef = ref()

const toggleCollapse = () => {
  isCollapse.value = !isCollapse.value
}

// 进销存子菜单手风琴效果：同一时刻只展开一个分组
const erpGroupKeys = ['erp-product', 'erp-inventory', 'erp-purchase', 'erp-sales', 'erp-report']
const handleErpOpen = (index) => {
  if (erpGroupKeys.includes(index)) {
    erpGroupKeys.filter(k => k !== index).forEach(k => menuRef.value?.close(k))
  }
}

const activeMenu = computed(() => route.path)

// 面包屑配置
const breadcrumbMap = {
  '/dashboard': '首页',
  '/system/user': '用户管理',
  '/system/role': '角色管理',
  '/system/permission': '权限管理',
  '/system/dept': '部门管理',
  '/system/gateway': '网关监控',
  '/dict/type': '字典类型',
  '/dict/config': '系统配置',
  '/dict/parameter': '参数管理',
  '/dict/api-market': 'API市场',
  '/erp/product': '商品管理',
  '/erp/product-price': '商品价格',
  '/erp/product-promotion': '商品促销',
  '/erp/warehouse': '仓库管理',
  '/erp/inventory': '库存管理',
  '/erp/inventory-flow': '库存流水',
  '/erp/inventory-check': '库存盘点',
  '/erp/inventory-alert': '库存预警',
  '/erp/supplier': '供应商管理',
  '/erp/customer': '客户管理',
  '/erp/purchase-order': '采购订单',
  '/erp/purchase-return': '采购退货',
  '/erp/sales-order': '销售订单',
  '/erp/sales-return': '销售退货',
  '/erp/sales-shipment': '销售出库',
  '/erp/report': '报表统计',
  '/erp/config': '系统配置',
  '/dict/parameter': '参数管理',
  '/dict/api-market': 'API市场',
  '/dict/third-party': '第三方API',
  '/dict/package': 'SaaS套餐',
  '/dict/activity': '营销活动',
  '/dict/trace': '追踪管理',
  '/dict/codegen': '代码生成',
  '/dict/error-doc': '错误文档',
  '/finance/receivable': '应收账款',
  '/finance/payable': '应付账款',
  '/finance/record': '收支记录',
  '/finance/bank-account': '银行账户',
  '/finance/invoice': '发票管理',
  '/finance/cost': '成本核算',
  '/finance/report': '财务报表',
  '/system/profile': '个人中心'
}

const breadcrumbs = computed(() => {
  const path = route.path
  const crumbs = []

  if (path !== '/dashboard') {
    crumbs.push({ path: '/dashboard', title: '首页' })
  }

  const title = breadcrumbMap[path] || route.meta?.title
  if (title) {
    crumbs.push({ path, title })
  }

  return crumbs.length > 0 ? crumbs : [{ path: '/dashboard', title: '首页' }]
})

const handleBreadcrumbClick = (path) => {
  if (path && path !== route.path) {
    router.push(path)
  }
}

const handleLogout = async () => {
  await userStore.logoutAction()
}
</script>

<style lang="scss" scoped>
.basic-layout {
  width: 100%;
  height: 100%;
}

.sidebar {
  background-color: #304156;
  transition: width 0.3s;
  overflow: hidden;
  display: flex;
  flex-direction: column;

  .sidebar-scroll {
    flex: 1;
    overflow: hidden;
  }

  .logo {
    height: 50px;
    display: flex;
    align-items: center;
    justify-content: center;
    background-color: #2b3a4d;

    .logo-img {
      width: 32px;
      height: 32px;
    }

    .logo-text {
      margin-left: 10px;
      color: #fff;
      font-size: 16px;
      font-weight: bold;
      white-space: nowrap;
    }
  }

  .el-menu {
    border-right: none;
  }
}

.main-container {
  display: flex;
  flex-direction: column;
  height: 100%;
}

.header {
  height: 50px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  background-color: #fff;
  border-bottom: 1px solid #dcdfe6;

  .header-left {
    display: flex;
    align-items: center;

    .collapse-btn {
      font-size: 20px;
      cursor: pointer;
      margin-right: 15px;

      &:hover {
        color: #409eff;
      }
    }

    :deep(.el-breadcrumb) {
      font-size: 14px;

      .el-breadcrumb__item {
        .breadcrumb-link {
          color: #606266;
          text-decoration: none;
          cursor: pointer;

          &:hover {
            color: #409eff;
          }
        }

        .breadcrumb-current {
          color: #909399;
          cursor: default;
        }
      }
    }
  }

  .header-right {
    .user-info {
      display: flex;
      align-items: center;
      cursor: pointer;

      .username {
        margin: 0 8px;
      }
    }
  }
}

.main {
  background-color: #f2f3f5;
  overflow: auto;
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
