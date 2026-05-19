<!-- views/erp/inventory-flow/index.vue - 库存流水页面 -->
<template>
  <div class="inventory-flow-container">
    <!-- 搜索栏区域 -->
    <el-card class="search-card">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="仓库">
          <el-select v-model="searchForm.warehouseId" placeholder="请选择仓库" clearable style="width: 180px;">
            <el-option
              v-for="warehouse in warehouseList"
              :key="warehouse.id"
              :label="warehouse.warehouseName"
              :value="warehouse.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="业务类型">
          <el-select v-model="searchForm.bizType" placeholder="请选择类型" clearable style="width: 150px;">
            <el-option label="采购入库" :value="1" />
            <el-option label="销售出库" :value="2" />
            <el-option label="调拨入库" :value="3" />
            <el-option label="调拨出库" :value="4" />
            <el-option label="盘盈" :value="5" />
            <el-option label="盘亏" :value="6" />
            <el-option label="退货入库" :value="7" />
            <el-option label="退货出库" :value="8" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>
            搜索
          </el-button>
          <el-button @click="handleReset">
            <el-icon><Refresh /></el-icon>
            重置
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 数据表格区域 -->
    <el-card class="table-card">
      <template #header>
        <div class="card-header">
          <span>库存流水</span>
        </div>
      </template>

      <el-table :data="tableData" v-loading="loading" border stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="warehouseId" label="仓库ID" width="100" />
        <el-table-column prop="productId" label="商品ID" width="100" />
        <el-table-column prop="bizType" label="业务类型" width="120">
          <template #default="{ row }">
            <el-tag :type="getBizTypeTag(row.bizType)">{{ getBizTypeText(row.bizType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="bizNo" label="业务单号" width="180" />
        <el-table-column prop="quantity" label="变动数量" width="120" align="right">
          <template #default="{ row }">
            <span :style="{ color: row.quantity > 0 ? '#67c23a' : '#f56c6c' }">
              {{ row.quantity > 0 ? '+' : '' }}{{ row.quantity }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="beforeQuantity" label="变动前数量" width="120" align="right" />
        <el-table-column prop="afterQuantity" label="变动后数量" width="120" align="right" />
        <el-table-column prop="costPrice" label="成本价" width="100" align="right">
          <template #default="{ row }">
            {{ row.costPrice ? '¥' + row.costPrice.toFixed(2) : '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="batchNo" label="批次号" width="120" />
        <el-table-column prop="operatorId" label="操作人" width="100" />
        <el-table-column prop="createdAt" label="操作时间" width="180" />
      </el-table>

      <!-- 分页组件 -->
      <el-pagination
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.size"
        :page-sizes="[10, 20, 50, 100]"
        :total="pagination.total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handlePageChange"
        class="pagination"
      />
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, Refresh } from '@element-plus/icons-vue'
import { getInventoryFlowPage, getWarehouses } from '@/api/erp'

const searchForm = reactive({
  warehouseId: undefined,
  bizType: undefined
})

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

const tableData = ref([])
const loading = ref(false)
const warehouseList = ref([])

const getBizTypeText = (type) => {
  const map = {
    1: '采购入库',
    2: '销售出库',
    3: '调拨入库',
    4: '调拨出库',
    5: '盘盈',
    6: '盘亏',
    7: '退货入库',
    8: '退货出库'
  }
  return map[type] || '未知'
}

const getBizTypeTag = (type) => {
  const map = {
    1: 'success',
    2: 'danger',
    3: 'success',
    4: 'danger',
    5: 'warning',
    6: 'warning',
    7: 'success',
    8: 'danger'
  }
  return map[type] || 'info'
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await getInventoryFlowPage({
      page: pagination.page,
      size: pagination.size,
      ...searchForm
    })
    tableData.value = res.records || []
    pagination.total = res.total || 0
  } catch (error) {
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

const loadWarehouses = async () => {
  try {
    const res = await getWarehouses()
    warehouseList.value = res || []
  } catch (error) {
    console.error('加载仓库列表失败', error)
  }
}

const handleSearch = () => {
  pagination.page = 1
  loadData()
}

const handleReset = () => {
  searchForm.warehouseId = undefined
  searchForm.bizType = undefined
  handleSearch()
}

const handleSizeChange = (size) => {
  pagination.size = size
  loadData()
}

const handlePageChange = (page) => {
  pagination.page = page
  loadData()
}

onMounted(() => {
  loadWarehouses()
  loadData()
})
</script>

<style scoped lang="scss">
.inventory-flow-container {
  padding: 20px;

  .search-card {
    margin-bottom: 20px;

    .search-form {
      display: flex;
      flex-wrap: wrap;
      gap: 10px;
    }
  }

  .table-card {
    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
    }

    .pagination {
      margin-top: 20px;
      justify-content: flex-end;
    }
  }
}
</style>
