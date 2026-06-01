<template>
  <div>
    <el-card>
      <template #header><span>限流配置</span></template>
      <el-alert title="限流规则通过 application.yml 配置，此处仅做展示" type="info" :closable="false" show-icon style="margin-bottom: 16px" />
      <el-table :data="tableData" v-loading="loading" border stripe>
        <el-table-column prop="dimension" label="维度" />
        <el-table-column prop="limit" label="限制数量" />
        <el-table-column prop="period" label="时间窗口(秒)" />
      </el-table>
    </el-card>
  </div>
</template>
<script setup>
import { ref, onMounted } from 'vue'
import { getRateLimitConfig } from '@/api/rateLimit'
const loading = ref(false)
const tableData = ref([
  { dimension: 'IP', limit: 100, period: 60 },
  { dimension: '用户', limit: 200, period: 60 },
  { dimension: '租户', limit: 500, period: 60 }
])
onMounted(async () => {
  loading.value = true
  try {
    const res = await getRateLimitConfig()
    if (res.data || res) tableData.value = res.data || res
  } catch (e) { console.error(e) }
  finally { loading.value = false }
})
</script>
