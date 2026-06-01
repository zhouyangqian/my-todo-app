<template>
  <div>
    <el-card>
      <template #header><span>开放API配置</span></template>
      <el-alert title="开放API通过 X-API-Key 认证访问 /api/open/** 路径" type="info" :closable="false" show-icon style="margin-bottom: 16px" />
      <el-table :data="tableData" v-loading="loading" border stripe>
        <el-table-column prop="path" label="API路径" />
        <el-table-column prop="description" label="描述" />
        <el-table-column prop="method" label="方法" width="80" />
        <el-table-column prop="enabled" label="是否开放" width="80">
          <template #default="{ row }">
            <el-tag :type="row.enabled ? 'success' : 'info'">{{ row.enabled ? '是' : '否' }}</el-tag>
          </template>
        </el-table-column>
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
