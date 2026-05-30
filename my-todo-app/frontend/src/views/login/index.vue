<!-- views/login/index.vue - 登录页面
  提供用户名密码输入表单，调用用户 Store 执行登录操作
  包含表单验证、验证码（登录失败3次后显示）、加载状态、回车提交等功能
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
        <el-form-item prop="userName">
          <el-input
            v-model="loginForm.userName"
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
        <!-- 验证码：登录失败次数 >= 3 时显示 -->
        <el-form-item v-if="showCaptcha" prop="captchaValue">
          <Captcha
            ref="captchaRef"
            @update:captcha-key="loginForm.captchaKey = $event"
            @update:captcha-code="loginForm.captchaValue = $event"
            @enter="handleLogin"
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
        <!-- 租户注册链接 -->
        <div class="register-link">
          <router-link to="/register">租户注册</router-link>
        </div>
      </el-form>
      <!-- 默认账号提示（开发环境使用） -->
      <div class="login-footer">
        <p>默认账号: admin / admin123</p>
      </div>
    </div>
  </div>
</template>

<script setup>
// 登录页面逻辑：表单数据绑定、验证规则、验证码、登录提交处理
import { ref, reactive, computed } from 'vue'
import { useUserStore } from '@/stores/user'
import Captcha from '@/components/Captcha.vue'

// 用户状态管理实例，用于调用登录方法
const userStore = useUserStore()

// 登录表单引用，用于触发表单验证
const loginFormRef = ref()
// 验证码组件引用
const captchaRef = ref()
// 登录按钮加载状态，防止重复提交
const loading = ref(false)
// 登录失败次数
const failCount = ref(0)

// 是否显示验证码（失败次数 >= 3 时显示）
const showCaptcha = computed(() => failCount.value >= 3)

// 登录表单数据（预填充默认账号，方便开发调试）
const loginForm = reactive({
  userName: 'admin',
  password: 'admin123',
  captchaKey: '',
  captchaValue: ''
})

// 表单验证规则（根据是否显示验证码动态决定）
const loginRules = computed(() => {
  const rules = {
    userName: [
      { required: true, message: '请输入用户名', trigger: 'blur' }
    ],
    password: [
      { required: true, message: '请输入密码', trigger: 'blur' },
      { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
    ]
  }
  // 验证码显示时才添加验证规则
  if (showCaptcha.value) {
    rules.captchaValue = [
      { required: true, message: '请输入验证码', trigger: 'blur' }
    ]
  }
  return rules
})

/**
 * 处理登录操作
 * 1. 验证表单字段是否合法
 * 2. 验证通过后调用 Store 的登录方法（附带验证码信息）
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
        await userStore.loginAction(loginForm.userName, loginForm.password, loginForm.captchaKey, loginForm.captchaValue)
      } catch (error) {
        console.error('Login failed:', error)
        // 登录失败，累加失败次数
        failCount.value++
        // 验证码已显示时，登录失败后刷新验证码
        if (showCaptcha.value && captchaRef.value) {
          captchaRef.value.refresh()
        }
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

    .register-link {
      text-align: right;
      margin-top: -10px;
      margin-bottom: 10px;

      a {
        color: #409eff;
        font-size: 13px;
        text-decoration: none;

        &:hover {
          text-decoration: underline;
        }
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
