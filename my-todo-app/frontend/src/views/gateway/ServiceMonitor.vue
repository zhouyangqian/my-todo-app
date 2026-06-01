<template>
  <div>
    <el-card>
      <template #header><span>服务健康监控</span></template>
      <el-table :data="tableData" v-loading="loading" border stripe>
        <el-table-column prop="serviceName" label="服务名称" />
        <el-table-column prop="host" label="地址" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 'UP' ? 'success' : 'danger'">{{ row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="lastCheckTime" label="最后检查时间" width="170" />
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getServiceStatuses } from '@/api/health'

const loading = ref(false)
const tableData = ref([])

const loadData = async () => {
  loading.value = true
  try {
    const res = await getServiceStatuses()
    tableData.value = res.data || res || []
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

onMounted(() => loadData())
</script>
