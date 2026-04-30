<!-- BasicLayout.vue - 应用主布局组件 -->
<template>
  <el-container class="basic-layout">
    <!-- 侧边栏导航区域 -->
    <el-aside :width="isCollapse ? '64px' : '210px'" class="sidebar">
      <div class="logo">
        <img src="@/assets/logo.svg" alt="Logo" class="logo-img" />
        <span v-show="!isCollapse" class="logo-text">My Todo App</span>
      </div>
      <!-- 侧边菜单 -->
      <el-menu
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
        </el-sub-menu>

        <!-- ERP子菜单 -->
        <el-sub-menu index="erp">
          <template #title>
            <el-icon><ShoppingCart /></el-icon>
            <span>进销存</span>
          </template>
          <el-menu-item index="/erp/product">
            <el-icon><Goods /></el-icon>
            <span>商品管理</span>
          </el-menu-item>
          <el-menu-item index="/erp/warehouse">
            <el-icon><House /></el-icon>
            <span>仓库管理</span>
          </el-menu-item>
          <el-menu-item index="/erp/inventory">
            <el-icon><Box /></el-icon>
            <span>库存管理</span>
          </el-menu-item>
          <el-menu-item index="/erp/supplier">
            <el-icon><Van /></el-icon>
            <span>供应商管理</span>
          </el-menu-item>
          <el-menu-item index="/erp/customer">
            <el-icon><Avatar /></el-icon>
            <span>客户管理</span>
          </el-menu-item>
          <el-menu-item index="/erp/sales-order">
            <el-icon><ShoppingCart /></el-icon>
            <span>销售订单</span>
          </el-menu-item>
          <el-menu-item index="/erp/sales-shipment">
            <el-icon><Van /></el-icon>
            <span>销售出库</span>
          </el-menu-item>
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
        </el-sub-menu>
      </el-menu>
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
              <span class="username">{{ userStore.userInfo?.realName || userStore.userInfo?.username }}</span>
              <el-icon><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item>个人中心</el-dropdown-item>
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
  Goods,
  House,
  Box,
  Van,
  Avatar,
  Money,
  CreditCard,
  Wallet,
  Tickets,
  Postcard
} from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const isCollapse = ref(false)

const toggleCollapse = () => {
  isCollapse.value = !isCollapse.value
}

const activeMenu = computed(() => route.path)

// 面包屑配置
const breadcrumbMap = {
  '/dashboard': '首页',
  '/system/user': '用户管理',
  '/system/role': '角色管理',
  '/system/permission': '权限管理',
  '/system/dept': '部门管理',
  '/dict/type': '字典类型',
  '/dict/config': '系统配置',
  '/erp/product': '商品管理',
  '/erp/warehouse': '仓库管理',
  '/erp/inventory': '库存管理',
  '/erp/supplier': '供应商管理',
  '/erp/customer': '客户管理',
  '/erp/sales-order': '销售订单',
  '/erp/sales-shipment': '销售出库',
  '/finance/receivable': '应收账款',
  '/finance/payable': '应付账款',
  '/finance/record': '收支记录',
  '/finance/bank-account': '银行账户'
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
