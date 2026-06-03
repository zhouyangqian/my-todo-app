<!-- views/register/index.vue - 租户注册页面
  提供租户信息和管理员账户注册表单
-->
<template>
  <div class="register-container">
    <el-card class="register-card">
      <template #header><span>租户注册</span></template>
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="100px">
        <!-- 租户信息 -->
        <el-divider>租户信息</el-divider>
        <el-form-item label="租户名称" prop="tenantName">
          <el-input v-model="formData.tenantName" placeholder="请输入租户名称" />
        </el-form-item>
        <el-form-item label="联系人" prop="contactName">
          <el-input v-model="formData.contactName" placeholder="请输入联系人" />
        </el-form-item>
        <el-form-item label="联系邮箱" prop="contactEmail">
          <el-input v-model="formData.contactEmail" placeholder="请输入联系邮箱" />
        </el-form-item>
        <el-form-item label="联系电话" prop="contactPhone">
          <el-input v-model="formData.contactPhone" placeholder="请输入联系电话" />
        </el-form-item>
        <!-- 按钮 -->
        <el-form-item>
          <el-button type="primary" @click="handleRegister" :loading="loading">注册</el-button>
          <el-button @click="$router.push('/login')">返回登录</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { registerTenant } from '@/api/auth'

const router = useRouter()
const formRef = ref()
const loading = ref(false)

// 表单数据
const formData = reactive({
  tenantName: '',
  contactName: '',
  contactEmail: '',
  contactPhone: ''
})

// 表单验证规则
const formRules = {
  tenantName: [
    { required: true, message: '请输入租户名称', trigger: 'blur' },
    { min: 2, max: 128, message: '租户名称长度在2-128位之间', trigger: 'blur' }
  ],
  contactEmail: [
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ]
}

/**
 * 处理注册操作
 */
const handleRegister = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        const result = await registerTenant(formData)
        const { tenantCode, adminUsername, adminPassword } = result || {}
        ElMessage.success({
          message: `注册成功！租户编码: ${tenantCode || '-'}，管理员: ${adminUsername || 'admin'}，密码: ${adminPassword || '***'}，请妥善保存`,
          duration: 0,
          showClose: true
        })
        setTimeout(() => router.push('/login'), 3000)
      } catch (error) {
        console.error('注册失败:', error)
      } finally {
        loading.value = false
      }
    }
  })
}
</script>

<style scoped lang="scss">
.register-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: #f0f2f5;

  .register-card {
    width: 600px;

    :deep(.el-card__header) {
      text-align: center;
      font-size: 18px;
      font-weight: bold;
    }
  }
}
</style>
