<template>
  <div>
    <el-card>
      <template #header><span>路由详情</span></template>
      <el-descriptions :column="2" border v-loading="loading">
        <el-descriptions-item label="路由ID">{{ route.id }}</el-descriptions-item>
        <el-descriptions-item label="路由URI">{{ route.uri }}</el-descriptions-item>
        <el-descriptions-item label="谓词">{{ route.predicates }}</el-descriptions-item>
        <el-descriptions-item label="过滤器">{{ route.filters }}</el-descriptions-item>
        <el-descriptions-item label="排序">{{ route.order }}</el-descriptions-item>
      </el-descriptions>
    </el-card>
  </div>
</template>
<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { get } from '@/utils/request'
const loading = ref(false)
const route = ref({})
const currentRoute = useRoute()
const loadData = async () => {
  loading.value = true
  try {
    const res = await get('/gateway/routes')
    const routes = res.data || res || []
    const id = currentRoute.params.id
    route.value = routes.find(r => r.routeId === id) || {}
  } catch (e) { console.error(e) }
  finally { loading.value = false }
}
onMounted(() => loadData())
</script>
