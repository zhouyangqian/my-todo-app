<template>
  <div>
    <el-card>
      <template #header><span>API Key 管理</span></template>
      <el-table :data="tableData" v-loading="loading" border stripe>
        <el-table-column prop="appName" label="应用名称" />
        <el-table-column prop="apiKey" label="API Key">
          <template #default="{ row }">
            <span>{{ row.apiKey ? row.apiKey.substring(0, 8) + '****' : '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? '有效' : '已禁用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="170" />
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getExternalApis } from '@/api/externalApi'

const loading = ref(false)
const tableData = ref([])

const loadData = async () => {
  loading.value = true
  try {
    const res = await getExternalApis()
    tableData.value = res.data || res || []
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

onMounted(() => loadData())
</script>
