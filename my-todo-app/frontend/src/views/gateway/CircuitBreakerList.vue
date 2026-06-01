<template>
  <div>
    <el-card>
      <template #header><span>熔断器状态</span></template>
      <el-table :data="tableData" v-loading="loading" border stripe>
        <el-table-column prop="serviceName" label="服务" />
        <el-table-column prop="state" label="状态" width="120">
          <template #default="{ row }">
            <el-tag :type="row.state === 'CLOSED' ? 'success' : row.state === 'OPEN' ? 'danger' : 'warning'">{{ row.state }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="failureCount" label="失败次数" width="100" />
      </el-table>
    </el-card>
  </div>
</template>
<script setup>
import { ref, onMounted } from 'vue'
import { getCircuitBreakerStatus } from '@/api/circuitBreaker'
const loading = ref(false)
const tableData = ref([])
const loadData = async () => {
  loading.value = true
  try {
    const res = await getCircuitBreakerStatus()
    const data = res.data || res || {}
    tableData.value = Object.entries(data).map(([k, v]) => ({ serviceName: k, ...v }))
  } catch (e) { console.error(e) }
  finally { loading.value = false }
}
onMounted(() => loadData())
</script>
