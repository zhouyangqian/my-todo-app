<!-- views/erp/inventory-check/index.vue - 库存盘点管理页面 -->
<template>
  <div class="inventory-check-container">
    <el-card class="search-card">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="仓库">
          <el-select v-model="searchForm.warehouseId" placeholder="请选择仓库" clearable style="width: 180px;">
            <el-option v-for="w in warehouseList" :key="w.id" :label="w.warehouseName" :value="w.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.checkStatus" placeholder="请选择状态" clearable style="width: 120px;">
            <el-option label="草稿" :value="0" />
            <el-option label="盘点中" :value="1" />
            <el-option label="已完成" :value="2" />
            <el-option label="已取消" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch"><el-icon><Search /></el-icon>搜索</el-button>
          <el-button @click="handleReset"><el-icon><Refresh /></el-icon>重置</el-button>
          <el-button type="success" @click="handleCreate" v-if="userStore.hasPermission('erp:inventoryCheck:create')"><el-icon><Plus /></el-icon>新建盘点</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="table-card">
      <el-table :data="tableData" v-loading="loading" border stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="checkNo" label="盘点单号" width="150" />
        <el-table-column prop="warehouseName" label="仓库" width="120" />
        <el-table-column prop="checkType" label="盘点类型" width="100">
          <template #default="{ row }">
            {{ row.checkType === 1 ? '全盘' : '抽盘' }}
          </template>
        </el-table-column>
        <el-table-column prop="checkStatus" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.checkStatus)">{{ getStatusText(row.checkStatus) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="totalProfitQty" label="盘盈数量" width="100" align="right" />
        <el-table-column prop="totalLossQty" label="盘亏数量" width="100" align="right" />
        <el-table-column prop="checkDate" label="盘点日期" width="110" />
        <el-table-column label="操作" fixed="right" width="200">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleView(row)">查看</el-button>
            <el-button v-if="(row.checkStatus === 0 || row.checkStatus === 1) && userStore.hasPermission('erp:inventoryCheck:submit')" type="success" link @click="handleEdit(row)">录入结果</el-button>
            <el-button v-if="row.checkStatus < 2 && userStore.hasPermission('erp:inventoryCheck:cancel')" type="danger" link @click="handleCancel(row)">取消</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination v-model:current-page="pagination.page" v-model:page-size="pagination.size"
        :page-sizes="[10, 20, 50, 100]" :total="pagination.total"
        layout="total, sizes, prev, pager, next, jumper" @size-change="handleSizeChange"
        @current-change="handlePageChange" class="pagination" />
    </el-card>

    <!-- 新建盘点对话框 -->
    <el-dialog v-model="createDialogVisible" title="新建盘点单" width="500px" :close-on-click-modal="false">
      <el-form ref="createFormRef" :model="createForm" :rules="createRules" label-width="100px">
        <el-form-item label="仓库" prop="warehouseId">
          <el-select v-model="createForm.warehouseId" placeholder="请选择仓库" style="width: 100%;">
            <el-option v-for="w in warehouseList" :key="w.id" :label="w.warehouseName" :value="w.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="盘点类型" prop="checkType">
          <el-radio-group v-model="createForm.checkType">
            <el-radio :value="1">全盘</el-radio>
            <el-radio :value="2">抽盘</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="盘点日期" prop="checkDate">
          <el-date-picker v-model="createForm.checkDate" type="date" placeholder="选择日期" style="width: 100%;" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="createForm.remark" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleCreateSubmit" :loading="createLoading">确定</el-button>
      </template>
    </el-dialog>

    <!-- 录入盘点结果对话框 -->
    <el-dialog v-model="editDialogVisible" title="录入盘点结果" width="900px" :close-on-click-modal="false">
      <el-descriptions :column="2" border style="margin-bottom: 20px;">
        <el-descriptions-item label="盘点单号">{{ currentCheck?.checkNo }}</el-descriptions-item>
        <el-descriptions-item label="仓库">{{ currentCheck?.warehouseName }}</el-descriptions-item>
      </el-descriptions>
      <el-table :data="checkItems" border max-height="400">
        <el-table-column prop="productName" label="商品名称" />
        <el-table-column prop="productCode" label="商品编码" width="120" />
        <el-table-column prop="systemQuantity" label="系统数量" width="100" align="right" />
        <el-table-column label="实盘数量" width="140">
          <template #default="{ row }">
            <el-input-number v-model="row.actualQuantity" :min="0" :precision="2" size="small" style="width: 100%;" />
          </template>
        </el-table-column>
        <el-table-column label="差异数量" width="100" align="right">
          <template #default="{ row }">
            <span :style="{ color: getDiffColor(row) }">
              {{ row.actualQuantity != null ? (row.actualQuantity - row.systemQuantity).toFixed(2) : '-' }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="备注" width="150">
          <template #default="{ row }">
            <el-input v-model="row.remark" size="small" />
          </template>
        </el-table-column>
      </el-table>
      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleEditSubmit" :loading="editLoading">提交结果</el-button>
      </template>
    </el-dialog>

    <!-- 查看详情对话框 -->
    <el-dialog v-model="detailDialogVisible" title="盘点单详情" width="800px">
      <el-descriptions :column="2" border style="margin-bottom: 20px;">
        <el-descriptions-item label="盘点单号">{{ currentCheck?.checkNo }}</el-descriptions-item>
        <el-descriptions-item label="仓库">{{ currentCheck?.warehouseName }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getStatusType(currentCheck?.checkStatus)">{{ getStatusText(currentCheck?.checkStatus) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="盘点类型">{{ currentCheck?.checkType === 1 ? '全盘' : '抽盘' }}</el-descriptions-item>
        <el-descriptions-item label="盘盈数量">{{ currentCheck?.totalProfitQty }}</el-descriptions-item>
        <el-descriptions-item label="盘亏数量">{{ currentCheck?.totalLossQty }}</el-descriptions-item>
      </el-descriptions>
      <el-table :data="currentCheck?.items" border>
        <el-table-column prop="productName" label="商品名称" />
        <el-table-column prop="productCode" label="商品编码" width="120" />
        <el-table-column prop="systemQuantity" label="系统数量" width="100" align="right" />
        <el-table-column prop="actualQuantity" label="实盘数量" width="100" align="right" />
        <el-table-column prop="diffQuantity" label="差异数量" width="100" align="right">
          <template #default="{ row }">
            <span :style="{ color: row.diffQuantity > 0 ? '#67c23a' : row.diffQuantity < 0 ? '#f56c6c' : '' }">
              {{ row.diffQuantity }}
            </span>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Plus } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
import { getInventoryCheckPage, getInventoryCheckDetail, createInventoryCheck, submitInventoryCheck, cancelInventoryCheck, getWarehouses } from '@/api/erp'

const searchForm = reactive({ warehouseId: undefined, checkStatus: undefined })
const pagination = reactive({ page: 1, size: 10, total: 0 })
const tableData = ref([])
const loading = ref(false)
const warehouseList = ref([])

const createDialogVisible = ref(false)
const createFormRef = ref()
const createLoading = ref(false)
const createForm = reactive({ warehouseId: undefined, checkType: 1, checkDate: null, remark: '' })
const createRules = {
  warehouseId: [{ required: true, message: '请选择仓库', trigger: 'change' }],
  checkType: [{ required: true, message: '请选择盘点类型', trigger: 'change' }],
  checkDate: [{ required: true, message: '请选择盘点日期', trigger: 'change' }]
}

const editDialogVisible = ref(false)
const editLoading = ref(false)
const currentCheck = ref(null)
const checkItems = ref([])

const detailDialogVisible = ref(false)

const getStatusType = (status) => ({ 0: 'info', 1: 'warning', 2: 'success', 3: 'danger' }[status] || 'info')
const getStatusText = (status) => ({ 0: '草稿', 1: '盘点中', 2: '已完成', 3: '已取消' }[status] || '未知')
const getDiffColor = (row) => {
  if (row.actualQuantity == null) return ''
  const diff = row.actualQuantity - row.systemQuantity
  return diff > 0 ? '#67c23a' : diff < 0 ? '#f56c6c' : ''
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await getInventoryCheckPage({ page: pagination.page, size: pagination.size, ...searchForm })
    tableData.value = res.records || []
    pagination.total = res.total || 0
  } catch { ElMessage.error('加载数据失败') } finally { loading.value = false }
}

const loadWarehouses = async () => {
  try { warehouseList.value = await getWarehouses() || [] } catch { console.error('加载仓库失败') }
}

const handleSearch = () => { pagination.page = 1; loadData() }
const handleReset = () => { searchForm.warehouseId = undefined; searchForm.checkStatus = undefined; handleSearch() }

const handleCreate = () => {
  Object.assign(createForm, { warehouseId: undefined, checkType: 1, checkDate: null, remark: '' })
  createDialogVisible.value = true
}

const handleCreateSubmit = async () => {
  if (!createFormRef.value) return
  await createFormRef.value.validate(async (valid) => {
    if (!valid) return
    createLoading.value = true
    try {
      await createInventoryCheck(createForm)
      ElMessage.success('创建成功')
      createDialogVisible.value = false
      loadData()
    } catch { ElMessage.error('创建失败') } finally { createLoading.value = false }
  })
}

const handleView = async (row) => {
  try {
    currentCheck.value = await getInventoryCheckDetail(row.id)
    detailDialogVisible.value = true
  } catch { ElMessage.error('加载详情失败') }
}

const handleEdit = async (row) => {
  try {
    currentCheck.value = await getInventoryCheckDetail(row.id)
    checkItems.value = (currentCheck.value.items || []).map(item => ({
      ...item,
      actualQuantity: item.actualQuantity || item.systemQuantity
    }))
    editDialogVisible.value = true
  } catch { ElMessage.error('加载详情失败') }
}

const handleEditSubmit = async () => {
  editLoading.value = true
  try {
    await submitInventoryCheck(currentCheck.value.id, checkItems.value)
    ElMessage.success('提交成功')
    editDialogVisible.value = false
    loadData()
  } catch { ElMessage.error('提交失败') } finally { editLoading.value = false }
}

const handleCancel = async (row) => {
  await ElMessageBox.confirm('确认取消该盘点单？', '提示', { type: 'warning' })
  try {
    await cancelInventoryCheck(row.id)
    ElMessage.success('取消成功')
    loadData()
  } catch (e) { if (e !== 'cancel') ElMessage.error('取消失败') }
}

const handleSizeChange = (size) => { pagination.size = size; loadData() }
const handlePageChange = (page) => { pagination.page = page; loadData() }

onMounted(() => { loadWarehouses(); loadData() })
</script>

<style scoped lang="scss">
.inventory-check-container { padding: 20px;
  .search-card { margin-bottom: 20px; .search-form { display: flex; flex-wrap: wrap; gap: 10px; } }
  .table-card { .pagination { margin-top: 20px; justify-content: flex-end; } }
}
</style>
