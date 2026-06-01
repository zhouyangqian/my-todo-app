<template>
  <div>
    <el-card>
      <template #header><span>限流监控</span></template>
      <el-descriptions :column="3" border>
        <el-descriptions-item label="IP限流">100次/60秒</el-descriptions-item>
        <el-descriptions-item label="用户限流">200次/60秒</el-descriptions-item>
        <el-descriptions-item label="租户限流">500次/60秒</el-descriptions-item>
      </el-descriptions>
      <el-table :data="tableData" v-loading="loading" border stripe style="margin-top: 16px">
        <el-table-column prop="dimension" label="限流维度" />
        <el-table-column prop="currentCount" label="当前请求数" width="120" />
        <el-table-column prop="limitCount" label="限制数量" width="120" />
        <el-table-column prop="remaining" label="剩余配额" width="120" />
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getRateLimitConfig } from '@/api/rateLimit'

const loading = ref(false)
const tableData = ref([])

const loadData = async () => {
  loading.value = true
  try {
    const res = await getRateLimitConfig()
    tableData.value = res.data || res || []
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

onMounted(() => loadData())
</script>
