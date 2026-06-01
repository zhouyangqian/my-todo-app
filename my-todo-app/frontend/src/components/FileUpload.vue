<template>
  <el-upload
    :action="uploadAction"
    :headers="uploadHeaders"
    :accept="accept"
    :limit="limit"
    :on-exceed="handleExceed"
    :on-success="handleSuccess"
    :on-error="handleError"
    :before-upload="handleBeforeUpload"
    :file-list="fileList"
    :auto-upload="autoUpload"
  >
    <el-button type="primary">{{ buttonText }}</el-button>
    <template #tip>
      <div class="el-upload__tip" v-if="tip">{{ tip }}</div>
    </template>
  </el-upload>
</template>

<script setup>
import { ref, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'

const props = defineProps({
  action: { type: String, required: true },
  accept: { type: String, default: '.xlsx,.xls,.csv' },
  limit: { type: Number, default: 1 },
  maxSize: { type: Number, default: 10 },
  buttonText: { type: String, default: '选择文件' },
  tip: { type: String, default: '' },
  autoUpload: { type: Boolean, default: true }
})
const emit = defineEmits(['success', 'error'])

const userStore = useUserStore()
const fileList = ref([])
const uploadAction = computed(() => `/api${props.action}`)
const uploadHeaders = computed(() => ({
  Authorization: `Bearer ${userStore.token}`
}))

const handleBeforeUpload = (file) => {
  const isLtMaxSize = file.size / 1024 / 1024 < props.maxSize
  if (!isLtMaxSize) {
    ElMessage.error(`文件大小不能超过 ${props.maxSize}MB`)
    return false
  }
  return true
}

const handleExceed = () => {
  ElMessage.warning(`最多上传 ${props.limit} 个文件`)
}

const handleSuccess = (response, file) => {
  ElMessage.success('上传成功')
  emit('success', response, file)
}

const handleError = (error) => {
  ElMessage.error('上传失败')
  emit('error', error)
}
</script>
