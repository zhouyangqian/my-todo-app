<!-- views/system/profile/index.vue - 个人资料页面
  展示当前用户信息（用户名、邮箱、手机号、部门等）
  提供修改密码功能（旧密码、新密码、确认密码）
-->
<template>
  <div class="profile-container">
    <el-row :gutter="20">
      <!-- 用户信息卡片 -->
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header>
            <span>个人信息</span>
          </template>
          <el-descriptions :column="1" border v-loading="profileLoading">
            <el-descriptions-item label="用户名">{{ profile.userName }}</el-descriptions-item>
            <el-descriptions-item label="真实姓名">{{ profile.realName || '-' }}</el-descriptions-item>
            <el-descriptions-item label="邮箱">{{ profile.email || '-' }}</el-descriptions-item>
            <el-descriptions-item label="手机号">{{ profile.phone || '-' }}</el-descriptions-item>
            <el-descriptions-item label="租户ID">{{ profile.tenantId || '-' }}</el-descriptions-item>
            <el-descriptions-item label="角色">
              <el-tag v-for="role in (profile.roles || [])" :key="role" size="small" style="margin-right: 4px;">
                {{ role }}
              </el-tag>
              <span v-if="!profile.roles || profile.roles.length === 0">-</span>
            </el-descriptions-item>
            <el-descriptions-item label="注册时间">{{ profile.createdAt || '-' }}</el-descriptions-item>
          </el-descriptions>
        </el-card>
      </el-col>

      <!-- 修改密码卡片 -->
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header>
            <span>修改密码</span>
          </template>
          <el-form ref="pwdFormRef" :model="pwdForm" :rules="pwdRules" label-width="100px">
            <el-form-item label="旧密码" prop="oldPassword">
              <el-input v-model="pwdForm.oldPassword" type="password" show-password placeholder="请输入旧密码" />
            </el-form-item>
            <el-form-item label="新密码" prop="newPassword">
              <el-input v-model="pwdForm.newPassword" type="password" show-password placeholder="请输入新密码" />
            </el-form-item>
            <el-form-item label="确认密码" prop="confirmPassword">
              <el-input v-model="pwdForm.confirmPassword" type="password" show-password placeholder="请再次输入新密码" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="pwdLoading" @click="handleChangePassword">确认修改</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getProfile, changePassword } from '@/api/auth'
import { useUserStore } from '@/stores/user'
import router from '@/router'

const userStore = useUserStore()

// 个人信息
const profileLoading = ref(false)
const profile = ref({})

const loadProfile = async () => {
  profileLoading.value = true
  try {
    const data = await getProfile()
    profile.value = data || {}
  } catch (error) {
    console.error('获取用户信息失败:', error)
  } finally {
    profileLoading.value = false
  }
}

// 修改密码
const pwdFormRef = ref()
const pwdLoading = ref(false)
const pwdForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const validateConfirm = (rule, value, callback) => {
  if (value !== pwdForm.newPassword) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const pwdRules = {
  oldPassword: [
    { required: true, message: '请输入旧密码', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    { validator: validateConfirm, trigger: 'blur' }
  ]
}

const handleChangePassword = async () => {
  if (!pwdFormRef.value) return
  await pwdFormRef.value.validate(async (valid) => {
    if (valid) {
      pwdLoading.value = true
      try {
        await changePassword({
          oldPassword: pwdForm.oldPassword,
          newPassword: pwdForm.newPassword
        })
        ElMessage.success('密码修改成功，请重新登录')
        // 修改密码后需要重新登录
        await userStore.logoutAction()
        router.push('/login')
      } catch (error) {
        console.error('修改密码失败:', error)
      } finally {
        pwdLoading.value = false
      }
    }
  })
}

onMounted(() => {
  loadProfile()
})
</script>

<style lang="scss" scoped>
.profile-container {
  padding: 20px;
}
</style>
