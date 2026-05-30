<!-- components/Captcha.vue - 验证码组件
  调用后端接口获取验证码图片，点击图片可刷新
  通过 emit 将 captchaKey 和 captchaCode 传递给父组件
-->
<template>
  <div class="captcha-row">
    <el-input
      v-model="captchaCode"
      placeholder="验证码"
      prefix-icon="Key"
      size="large"
      class="captcha-input"
      @keyup.enter="$emit('enter')"
      @input="handleInput"
    />
    <img
      v-if="captchaImage"
      :src="captchaImage"
      alt="验证码"
      class="captcha-img"
      @click="refresh"
      title="点击刷新验证码"
    />
    <div v-else class="captcha-placeholder" @click="refresh">加载中...</div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getCaptcha } from '@/api/auth'

const emit = defineEmits(['update:captchaKey', 'update:captchaCode', 'enter'])

const captchaImage = ref('')
const captchaCode = ref('')
const captchaKey = ref('')

const refresh = async () => {
  try {
    const data = await getCaptcha()
    captchaImage.value = data.captchaImage
    captchaKey.value = data.captchaKey
    captchaCode.value = ''
    emit('update:captchaKey', data.captchaKey)
    emit('update:captchaCode', '')
  } catch (error) {
    console.error('获取验证码失败:', error)
  }
}

const handleInput = (value) => {
  emit('update:captchaCode', value)
}

onMounted(() => {
  refresh()
})

defineExpose({ refresh })
</script>

<style lang="scss" scoped>
.captcha-row {
  display: flex;
  align-items: center;
  width: 100%;
  gap: 10px;

  .captcha-input {
    flex: 1;
  }

  .captcha-img {
    height: 40px;
    cursor: pointer;
    border: 1px solid #dcdfe6;
    border-radius: 4px;
    flex-shrink: 0;
  }

  .captcha-placeholder {
    height: 40px;
    width: 120px;
    display: flex;
    align-items: center;
    justify-content: center;
    border: 1px solid #dcdfe6;
    border-radius: 4px;
    color: #909399;
    font-size: 12px;
    cursor: pointer;
    flex-shrink: 0;
  }
}
</style>
