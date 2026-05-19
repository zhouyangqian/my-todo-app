<!-- views/erp/inventory-alert/index.vue - 库存预警页面 -->
<template>
  <div class="inventory-alert-container">
    <el-card class="search-card">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="仓库">
          <el-select v-model="searchForm.warehouseId" placeholder="请选择仓库" clearable style="width: 180px;">
            <el-option v-for="w in warehouseList" :key="w.id" :label="w.warehouseName" :value="w.id" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch"><el-icon><Search /></el-icon>搜索</el-button>
          <el-button @click="handleReset"><el-icon><Refresh /></el-icon>重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="table-card">
      <el-table :data="tableData" v-loading="loading" border stripe>
        <el-table-column prop="productName" label="商品名称" min-width="150" />
        <el-table-column prop="productCode" label="商品编码" width="120" />
        <el-table-column prop="warehouseName" label="仓库" width="120" />
        <el-table-column prop="quantity" label="当前库存" width="100" align="right" />
        <el-table-column prop="stockMin" label="库存下限" width="100" align="right" />
        <el-table-column prop="stockMax" label="库存上限" width="100" align="right" />
        <el-table-column label="预警类型" width="120">
          <template #default="{ row }">
            <el-tag v-if="row.quantity < row.stockMin" type="danger">低库存</el-tag>
            <el-tag v-else-if="row.quantity > row.stockMax" type="warning">高库存</el-tag>
            <el-tag v-else type="info">正常</el-tag>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination v-model:current-page="pagination.page" v-model:page-size="pagination.size"
        :page-sizes="[10, 20, 50, 100]" :total="pagination.total"
        layout="total, sizes, prev, pager, next, jumper" @size-change="handleSizeChange"
        @current-change="handlePageChange" class="pagination" />
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, Refresh } from '@element-plus/icons-vue'
import { getAlertInventories, getWarehouses } from '@/api/erp'

const searchForm = reactive({ warehouseId: undefined })
const pagination = reactive({ page: 1, size: 10, total: 0 })
const tableData = ref([])
const loading = ref(false)
const warehouseList = ref([])

const loadData = async () => {
  loading.value = true
  try {
    const res = await getAlertInventories({ page: pagination.page, size: pagination.size, ...searchForm })
    tableData.value = res.records || []
    pagination.total = res.total || 0
  } catch { ElMessage.error('加载数据失败') } finally { loading.value = false }
}

const loadWarehouses = async () => {
  try { warehouseList.value = await getWarehouses() || [] } catch {}
}

const handleSearch = () => { pagination.page = 1; loadData() }
const handleReset = () => { searchForm.warehouseId = undefined; handleSearch() }
const handleSizeChange = (size) => { pagination.size = size; loadData() }
const handlePageChange = (page) => { pagination.page = page; loadData() }

onMounted(() => { loadWarehouses(); loadData() })
</script>

<style scoped lang="scss">
.inventory-alert-container { padding: 20px;
  .search-card { margin-bottom: 20px; .search-form { display: flex; flex-wrap: wrap; gap: 10px; } }
  .table-card { .pagination { margin-top: 20px; justify-content: flex-end; } }
}
</style>
