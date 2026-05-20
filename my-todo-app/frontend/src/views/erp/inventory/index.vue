<!-- views/erp/inventory/index.vue - 库存管理页面 -->
<template>
  <div class="inventory-container">
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
        <el-form-item label="商品名称">
          <el-input v-model="searchForm.productName" placeholder="请输入商品名称" clearable />
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
          <span>库存列表</span>
          <el-button type="warning" plain @click="handleShowAlert">
            <el-icon><Warning /></el-icon>
            库存预警
          </el-button>
        </div>
      </template>

      <el-table :data="tableData" v-loading="loading" border stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="warehouseName" label="仓库" width="150" />
        <el-table-column prop="productName" label="商品名称" width="180" />
        <el-table-column prop="productSku" label="商品编码" width="120" />
        <el-table-column prop="quantity" label="库存数量" width="100" align="right">
          <template #default="{ row }">
            <span :style="{ color: row.quantity <= row.minStock ? '#f56c6c' : '' }">
              {{ row.quantity }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="minStock" label="最低库存" width="100" align="right" />
        <el-table-column prop="maxStock" label="最高库存" width="100" align="right" />
        <el-table-column prop="costPrice" label="成本价" width="100" align="right">
          <template #default="{ row }">
            {{ row.costPrice ? '¥' + row.costPrice.toFixed(2) : '-' }}
          </template>
        </el-table-column>
        <el-table-column label="库存金额" width="120" align="right">
          <template #default="{ row }">
            {{ row.quantity && row.costPrice ? '¥' + (row.quantity * row.costPrice).toFixed(2) : '-' }}
          </template>
        </el-table-column>
        <el-table-column label="操作" fixed="right" width="260">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleInbound(row)" v-if="userStore.hasPermission('erp:inventory:inbound')">入库</el-button>
            <el-button type="warning" link @click="handleOutbound(row)" v-if="userStore.hasPermission('erp:inventory:outbound')">出库</el-button>
            <el-button type="info" link @click="handleViewLog(row)">日志</el-button>
          </template>
        </el-table-column>
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

    <!-- 入库对话框 -->
    <el-dialog
      v-model="inboundDialogVisible"
      title="库存入库"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form ref="inboundFormRef" :model="inboundForm" :rules="inboundRules" label-width="100px">
        <el-form-item label="商品">
          <el-input :value="`${currentRow?.productName || ''} (${currentRow?.productSku || ''})`" disabled />
        </el-form-item>
        <el-form-item label="当前库存">
          <el-input :value="currentRow?.quantity || 0" disabled />
        </el-form-item>
        <el-form-item label="入库数量" prop="quantity">
          <el-input-number v-model="inboundForm.quantity" :min="0.01" :precision="2" style="width: 100%;" />
        </el-form-item>
        <el-form-item label="成本价" prop="costPrice">
          <el-input-number v-model="inboundForm.costPrice" :min="0" :precision="2" style="width: 100%;" />
        </el-form-item>
        <el-form-item label="批次号" prop="batchNo">
          <el-input v-model="inboundForm.batchNo" placeholder="请输入批次号（可选）" />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="inboundForm.remark" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="inboundDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleInboundSubmit" :loading="inboundLoading">确定</el-button>
      </template>
    </el-dialog>

    <!-- 出库对话框 -->
    <el-dialog
      v-model="outboundDialogVisible"
      title="库存出库"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form ref="outboundFormRef" :model="outboundForm" :rules="outboundRules" label-width="100px">
        <el-form-item label="商品">
          <el-input :value="`${currentRow?.productName || ''} (${currentRow?.productSku || ''})`" disabled />
        </el-form-item>
        <el-form-item label="当前库存">
          <el-input :value="currentRow?.quantity || 0" disabled />
        </el-form-item>
        <el-form-item label="出库数量" prop="quantity">
          <el-input-number v-model="outboundForm.quantity" :min="0.01" :max="currentRow?.quantity || 0" :precision="2" style="width: 100%;" />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="outboundForm.remark" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="outboundDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleOutboundSubmit" :loading="outboundLoading">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Warning } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { getInventoryPage, inbound, outbound, getWarehouses } from '@/api/erp'

const router = useRouter()
const userStore = useUserStore()

const searchForm = reactive({
  warehouseId: undefined,
  productName: ''
})

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

const tableData = ref([])
const loading = ref(false)
const warehouseList = ref([])

// 入库相关
const inboundDialogVisible = ref(false)
const inboundFormRef = ref()
const inboundLoading = ref(false)
const currentRow = ref(null)
const inboundForm = reactive({
  quantity: 1,
  costPrice: 0,
  batchNo: '',
  remark: ''
})
const inboundRules = {
  quantity: [{ required: true, message: '请输入入库数量', trigger: 'blur' }]
}

// 出库相关
const outboundDialogVisible = ref(false)
const outboundFormRef = ref()
const outboundLoading = ref(false)
const outboundForm = reactive({
  quantity: 1,
  remark: ''
})
const outboundRules = {
  quantity: [{ required: true, message: '请输入出库数量', trigger: 'blur' }]
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await getInventoryPage({
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
  searchForm.productName = ''
  handleSearch()
}

const handleInbound = (row) => {
  currentRow.value = row
  Object.assign(inboundForm, {
    quantity: 1,
    costPrice: row.costPrice || 0,
    batchNo: '',
    remark: ''
  })
  inboundDialogVisible.value = true
}

const handleInboundSubmit = async () => {
  if (!inboundFormRef.value) return
  await inboundFormRef.value.validate(async (valid) => {
    if (!valid) return
    inboundLoading.value = true
    try {
      await inbound({
        warehouseId: currentRow.value.warehouseId,
        productId: currentRow.value.productId,
        quantity: inboundForm.quantity,
        costPrice: inboundForm.costPrice,
        batchNo: inboundForm.batchNo,
        bizType: 1,
        bizNo: 'IN' + Date.now(),
        remark: inboundForm.remark
      })
      ElMessage.success('入库成功')
      inboundDialogVisible.value = false
      loadData()
    } catch (error) {
      ElMessage.error('入库失败')
    } finally {
      inboundLoading.value = false
    }
  })
}

const handleOutbound = (row) => {
  currentRow.value = row
  Object.assign(outboundForm, {
    quantity: Math.min(1, row.quantity || 0),
    remark: ''
  })
  outboundDialogVisible.value = true
}

const handleOutboundSubmit = async () => {
  if (!outboundFormRef.value) return
  await outboundFormRef.value.validate(async (valid) => {
    if (!valid) return
    if (outboundForm.quantity > currentRow.value.quantity) {
      ElMessage.warning('出库数量不能超过当前库存')
      return
    }
    outboundLoading.value = true
    try {
      await outbound({
        warehouseId: currentRow.value.warehouseId,
        productId: currentRow.value.productId,
        quantity: outboundForm.quantity,
        bizType: 1,
        bizNo: 'OUT' + Date.now(),
        remark: outboundForm.remark
      })
      ElMessage.success('出库成功')
      outboundDialogVisible.value = false
      loadData()
    } catch (error) {
      ElMessage.error('出库失败')
    } finally {
      outboundLoading.value = false
    }
  })
}

const handleViewLog = (row) => {
  router.push({ path: '/erp/inventory-flow', query: { warehouseId: row.warehouseId, productId: row.productId } })
}

const handleShowAlert = () => {
  ElMessage.info('库存预警功能开发中')
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
.inventory-container {
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
