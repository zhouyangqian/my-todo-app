<!-- views/error/404.vue - 404 页面未找到错误页
  当用户访问不存在的路由路径时显示此页面
  提供"返回首页"按钮跳转到工作台
-->
<template>
  <div class="error-page">
    <div class="error-content">
      <h1>404</h1>
      <p>抱歉，您访问的页面不存在</p>
      <!-- 返回首页按钮 -->
      <el-button type="primary" @click="goHome">返回首页</el-button>
    </div>
  </div>
</template>

<script setup>
// 404 页面逻辑：提供返回首页的导航功能
import { useRouter } from 'vue-router'

const router = useRouter()

/**
 * 跳转回工作台首页
 */
const goHome = () => {
  router.push('/dashboard')
}
</script>

<style scoped>
.error-page {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f7fa;
}

.error-content {
  text-align: center;

  h1 {
    font-size: 120px;
    color: #409eff;
    margin-bottom: 20px;
  }

  p {
    font-size: 18px;
    color: #606266;
    margin-bottom: 30px;
  }
}
</style>
