<template>
  <div>
    <el-card>
      <template #header><span>第三方接口管理</span></template>
      <el-table :data="tableData" v-loading="loading" border stripe>
        <el-table-column prop="name" label="接口名称" />
        <el-table-column prop="url" label="接口地址" />
        <el-table-column prop="method" label="请求方式" width="100" />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">{{ row.status === 1 ? '启用' : '禁用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="lastCheckTime" label="最后检查" width="170" />
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
