<!-- BasicLayout.vue - 应用主布局组件
  包含左侧导航菜单（可折叠）、顶部栏（折叠按钮+面包屑+用户下拉）、主内容区（带路由过渡动画和缓存）
-->
<template>
  <el-container class="basic-layout">
    <!-- 侧边栏导航区域 -->
    <el-aside :width="isCollapse ? '64px' : '210px'" class="sidebar">
      <!-- Logo 区域，折叠时只显示图标 -->
      <div class="logo">
        <img src="@/assets/logo.svg" alt="Logo" class="logo-img" />
        <span v-show="!isCollapse" class="logo-text">My Todo App</span>
      </div>
      <!-- 侧边菜单，使用 Element Plus 的 Menu 组件，开启 router 模式自动关联路由 -->
      <el-menu
        :default-active="activeMenu"
        :collapse="isCollapse"
        :collapse-transition="false"
        :unique-opened="true"
        background-color="#304156"
        text-color="#bfcbd9"
        active-text-color="#409eff"
        router
      >
        <template v-for="route in menuRoutes" :key="route.path">
          <!-- 有多个子菜单时渲染为子菜单（可展开/收起） -->
          <el-sub-menu v-if="route.children && route.children.length > 1" :index="route.path">
            <template #title>
              <el-icon><component :is="route.meta?.icon" /></el-icon>
              <span>{{ route.meta?.title }}</span>
            </template>
            <el-menu-item
              v-for="child in route.children"
              :key="child.path"
              :index="`${route.path}/${child.path}`"
            >
              <el-icon><component :is="child.meta?.icon" /></el-icon>
              <span>{{ child.meta?.title }}</span>
            </el-menu-item>
          </el-sub-menu>
          <!-- 只有一个或没有子菜单时直接渲染为菜单项 -->
          <el-menu-item v-else :index="route.redirect || route.path">
            <el-icon><component :is="route.meta?.icon" /></el-icon>
            <span>{{ route.meta?.title }}</span>
          </el-menu-item>
        </template>
      </el-menu>
    </el-aside>

    <el-container class="main-container">
      <!-- 顶部栏 -->
      <el-header class="header">
        <div class="header-left">
          <!-- 侧边栏折叠/展开切换按钮 -->
          <el-icon class="collapse-btn" @click="toggleCollapse">
            <Fold v-if="!isCollapse" />
            <Expand v-else />
          </el-icon>
          <!-- 面包屑导航，根据当前路由自动生成 -->
          <el-breadcrumb separator="/">
            <el-breadcrumb-item v-for="item in breadcrumbs" :key="item.path">
              {{ item.meta?.title }}
            </el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <div class="header-right">
          <!-- 用户信息下拉菜单 -->
          <el-dropdown trigger="click">
            <span class="user-info">
              <!-- 用户头像，无头像时显示姓名首字 -->
              <el-avatar :size="32" :src="userStore.userInfo?.avatar">
                {{ userStore.userInfo?.realName?.charAt(0) || 'U' }}
              </el-avatar>
              <span class="username">{{ userStore.userInfo?.realName || userStore.userInfo?.username }}</span>
              <el-icon><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item>个人中心</el-dropdown-item>
                <!-- 退出登录按钮 -->
                <el-dropdown-item divided @click="handleLogout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <!-- 主内容区域 -->
      <el-main class="main">
        <!-- 路由视图，带淡入淡出过渡动画和组件缓存 -->
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

<script setup lang="ts">
// 主布局组件逻辑：侧边栏折叠控制、菜单高亮、面包屑生成、用户下拉菜单
import { ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'

// 当前路由实例，用于获取路由信息
const route = useRoute()
// 路由器实例，用于获取路由配置
const router = useRouter()
// 用户状态管理，用于获取用户信息和执行登出操作
const userStore = useUserStore()

// 侧边栏折叠状态（true 为折叠，false 为展开）
const isCollapse = ref(false)

/**
 * 切换侧边栏折叠/展开状态
 */
const toggleCollapse = () => {
  isCollapse.value = !isCollapse.value
}

// 当前激活的菜单项路径（与当前路由路径保持一致，实现菜单高亮）
const activeMenu = computed(() => route.path)

// 面包屑数据，从当前路由的 matched 记录中提取有标题的项
const breadcrumbs = computed(() => {
  return route.matched.filter(item => item.meta?.title)
})

// 菜单路由数据，从路由配置中提取根路径 '/' 下的子路由（带标题的）用于渲染侧边菜单
const menuRoutes = computed(() => {
  const routes = router.options.routes.find(r => r.path === '/')?.children || []
  return routes.filter(r => r.meta?.title)
})

/**
 * 处理用户退出登录
 * 调用用户 Store 的登出方法，清除认证信息并跳转到登录页
 */
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

// 过渡动画
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
