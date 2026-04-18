<!-- views/login/index.vue - 登录页面
  提供用户名密码输入表单，调用用户 Store 执行登录操作
  包含表单验证、加载状态、回车提交等功能
-->
<template>
  <div class="login-container">
    <div class="login-box">
      <!-- 登录页标题区域 -->
      <div class="login-header">
        <h2>My Todo App</h2>
        <p>企业级管理系统</p>
      </div>
      <!-- 登录表单，包含用户名和密码字段 -->
      <el-form ref="loginFormRef" :model="loginForm" :rules="loginRules" class="login-form">
        <!-- 用户名输入框 -->
        <el-form-item prop="username">
          <el-input
            v-model="loginForm.username"
            placeholder="用户名"
            prefix-icon="User"
            size="large"
          />
        </el-form-item>
        <!-- 密码输入框，支持显示/隐藏密码和回车提交 -->
        <el-form-item prop="password">
          <el-input
            v-model="loginForm.password"
            type="password"
            placeholder="密码"
            prefix-icon="Lock"
            size="large"
            show-password
            @keyup.enter="handleLogin"
          />
        </el-form-item>
        <!-- 登录按钮 -->
        <el-form-item>
          <el-button
            type="primary"
            size="large"
            :loading="loading"
            class="login-btn"
            @click="handleLogin"
          >
            登录
          </el-button>
        </el-form-item>
      </el-form>
      <!-- 默认账号提示（开发环境使用） -->
      <div class="login-footer">
        <p>默认账号: admin / admin123</p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
// 登录页面逻辑：表单数据绑定、验证规则、登录提交处理
import { ref, reactive } from 'vue'
import type { FormInstance, FormRules } from 'element-plus'
import { useUserStore } from '@/stores/user'

// 用户状态管理实例，用于调用登录方法
const userStore = useUserStore()

// 登录表单引用，用于触发表单验证
const loginFormRef = ref<FormInstance>()
// 登录按钮加载状态，防止重复提交
const loading = ref(false)

// 登录表单数据（预填充默认账号，方便开发调试）
const loginForm = reactive({
  username: 'admin',
  password: 'admin123'
})

// 表单验证规则
const loginRules: FormRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ]
}

/**
 * 处理登录操作
 * 1. 验证表单字段是否合法
 * 2. 验证通过后调用 Store 的登录方法
 * 3. 登录成功后 Store 会自动跳转到工作台
 */
const handleLogin = async () => {
  if (!loginFormRef.value) return

  // 先进行前端表单验证，验证通过后再发起登录请求
  await loginFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        // 调用用户 Store 的登录动作（包含接口请求、令牌保存、跳转等）
        await userStore.loginAction(loginForm.username, loginForm.password)
      } catch (error) {
        console.error('Login failed:', error)
      } finally {
        loading.value = false
      }
    }
  })
}
</script>

<style lang="scss" scoped>
.login-container {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);

  .login-box {
    width: 400px;
    padding: 40px;
    background: #fff;
    border-radius: 8px;
    box-shadow: 0 10px 40px rgba(0, 0, 0, 0.2);

    .login-header {
      text-align: center;
      margin-bottom: 30px;

      h2 {
        font-size: 28px;
        color: #303133;
        margin-bottom: 10px;
      }

      p {
        color: #909399;
        font-size: 14px;
      }
    }

    .login-form {
      .login-btn {
        width: 100%;
      }
    }

    .login-footer {
      text-align: center;
      margin-top: 20px;
      color: #909399;
      font-size: 12px;
    }
  }
}
</style>
