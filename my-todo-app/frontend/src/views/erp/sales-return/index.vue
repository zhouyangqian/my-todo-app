<!-- views/erp/sales-return/index.vue - 销售退货管理页面 -->
<template>
  <div class="sales-return-container">
    <el-card class="search-card">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="客户">
          <el-select v-model="searchForm.customerId" placeholder="请选择客户" clearable filterable style="width: 180px;">
            <el-option v-for="c in customerList" :key="c.id" :label="c.customerName" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.returnStatus" placeholder="请选择状态" clearable style="width: 120px;">
            <el-option label="草稿" :value="0" />
            <el-option label="待审核" :value="1" />
            <el-option label="已审核" :value="2" />
            <el-option label="已取消" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch"><el-icon><Search /></el-icon>搜索</el-button>
          <el-button @click="handleReset"><el-icon><Refresh /></el-icon>重置</el-button>
          <el-button type="success" @click="handleCreate"><el-icon><Plus /></el-icon>新建退货</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="table-card">
      <el-table :data="tableData" v-loading="loading" border stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="returnNo" label="退货单号" width="150" />
        <el-table-column prop="customerName" label="客户" width="150" />
        <el-table-column prop="warehouseName" label="仓库" width="120" />
        <el-table-column prop="totalAmount" label="退货金额" width="120" align="right">
          <template #default="{ row }">¥{{ row.totalAmount?.toFixed(2) || '0.00' }}</template>
        </el-table-column>
        <el-table-column prop="returnStatus" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.returnStatus)">{{ getStatusText(row.returnStatus) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="returnDate" label="退货日期" width="110" />
        <el-table-column prop="reason" label="退货原因" min-width="150" show-overflow-tooltip />
        <el-table-column label="操作" fixed="right" width="250">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleView(row)">查看</el-button>
            <el-button v-if="row.returnStatus === 0" type="success" link @click="handleSubmit(row)">提交</el-button>
            <el-button v-if="row.returnStatus === 1" type="success" link @click="handleApprove(row)">审核</el-button>
            <el-button v-if="row.returnStatus < 2" type="danger" link @click="handleCancel(row)">取消</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination v-model:current-page="pagination.page" v-model:page-size="pagination.size"
        :page-sizes="[10, 20, 50, 100]" :total="pagination.total"
        layout="total, sizes, prev, pager, next, jumper" @size-change="handleSizeChange"
        @current-change="handlePageChange" class="pagination" />
    </el-card>

    <!-- 新建退货对话框 -->
    <el-dialog v-model="dialogVisible" title="新建销售退货" width="900px" :close-on-click-modal="false">
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="100px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="客户" prop="customerId">
              <el-select v-model="formData.customerId" placeholder="请选择客户" filterable style="width: 100%;">
                <el-option v-for="c in customerList" :key="c.id" :label="c.customerName" :value="c.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="仓库" prop="warehouseId">
              <el-select v-model="formData.warehouseId" placeholder="请选择仓库" style="width: 100%;">
                <el-option v-for="w in warehouseList" :key="w.id" :label="w.warehouseName" :value="w.id" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="退货日期" prop="returnDate">
              <el-date-picker v-model="formData.returnDate" type="date" placeholder="选择日期" style="width: 100%;" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="退货原因">
              <el-input v-model="formData.reason" placeholder="请输入退货原因" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-divider content-position="left">退货明细</el-divider>
        <el-table :data="formData.items" border max-height="300">
          <el-table-column label="商品" width="200">
            <template #default="{ row }">
              <el-select v-model="row.productId" placeholder="选择商品" filterable @change="handleProductChange(row)">
                <el-option v-for="p in productList" :key="p.id" :label="`${p.productName} (${p.productCode})`" :value="p.id" />
              </el-select>
            </template>
          </el-table-column>
          <el-table-column label="数量" width="120">
            <template #default="{ row }">
              <el-input-number v-model="row.quantity" :min="0.01" :precision="2" style="width: 100%;" />
            </template>
          </el-table-column>
          <el-table-column label="单价" width="120">
            <template #default="{ row }">
              <el-input-number v-model="row.price" :min="0" :precision="2" style="width: 100%;" />
            </template>
          </el-table-column>
          <el-table-column label="金额" width="120">
            <template #default="{ row }">¥{{ ((row.quantity || 0) * (row.price || 0)).toFixed(2) }}</template>
          </el-table-column>
          <el-table-column label="操作" width="80">
            <template #default="{ $index }">
              <el-button type="danger" link @click="formData.items.splice($index, 1)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
        <el-button type="primary" link @click="formData.items.push({ productId: undefined, quantity: 1, price: 0 })" style="margin-top: 10px;">
          <el-icon><Plus /></el-icon>添加明细
        </el-button>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleFormSubmit" :loading="submitLoading">确定</el-button>
      </template>
    </el-dialog>

    <!-- 查看详情对话框 -->
    <el-dialog v-model="detailDialogVisible" title="退货单详情" width="800px">
      <el-descriptions :column="2" border style="margin-bottom: 20px;">
        <el-descriptions-item label="退货单号">{{ currentRow?.returnNo }}</el-descriptions-item>
        <el-descriptions-item label="客户">{{ currentRow?.customerName }}</el-descriptions-item>
        <el-descriptions-item label="仓库">{{ currentRow?.warehouseName }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getStatusType(currentRow?.returnStatus)">{{ getStatusText(currentRow?.returnStatus) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="退货金额">¥{{ currentRow?.totalAmount?.toFixed(2) }}</el-descriptions-item>
        <el-descriptions-item label="退货原因">{{ currentRow?.reason || '-' }}</el-descriptions-item>
      </el-descriptions>
      <el-table :data="currentRow?.items" border>
        <el-table-column prop="productName" label="商品名称" />
        <el-table-column prop="productCode" label="商品编码" width="120" />
        <el-table-column prop="quantity" label="数量" width="100" align="right" />
        <el-table-column prop="price" label="单价" width="100" align="right" />
        <el-table-column prop="amount" label="金额" width="100" align="right" />
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Plus } from '@element-plus/icons-vue'
import { getSalesReturnPage, getSalesReturnDetail, createSalesReturn, submitSalesReturn, approveSalesReturn, cancelSalesReturn, getCustomerPage, getWarehouses, getProductPage } from '@/api/erp'

const searchForm = reactive({ customerId: undefined, returnStatus: undefined })
const pagination = reactive({ page: 1, size: 10, total: 0 })
const tableData = ref([])
const loading = ref(false)
const customerList = ref([])
const warehouseList = ref([])
const productList = ref([])

const dialogVisible = ref(false)
const formRef = ref()
const submitLoading = ref(false)
const formData = reactive({ customerId: undefined, warehouseId: undefined, returnDate: null, reason: '', items: [] })
const formRules = {
  customerId: [{ required: true, message: '请选择客户', trigger: 'change' }],
  warehouseId: [{ required: true, message: '请选择仓库', trigger: 'change' }],
  returnDate: [{ required: true, message: '请选择退货日期', trigger: 'change' }]
}

const detailDialogVisible = ref(false)
const currentRow = ref(null)

const getStatusType = (status) => ({ 0: 'info', 1: 'warning', 2: 'success', 3: 'danger' }[status] || 'info')
const getStatusText = (status) => ({ 0: '草稿', 1: '待审核', 2: '已审核', 3: '已取消' }[status] || '未知')

const handleProductChange = (row) => {
  const product = productList.value.find(p => p.id === row.productId)
  if (product) row.price = product.salePrice || 0
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await getSalesReturnPage({ page: pagination.page, size: pagination.size, ...searchForm })
    tableData.value = res.records || []
    pagination.total = res.total || 0
  } catch { ElMessage.error('加载数据失败') } finally { loading.value = false }
}

const loadCustomers = async () => { try { customerList.value = (await getCustomerPage({ page: 1, size: 1000 })).records || [] } catch {} }
const loadWarehouses = async () => { try { warehouseList.value = await getWarehouses() || [] } catch {} }
const loadProducts = async () => { try { productList.value = (await getProductPage({ page: 1, size: 1000 })).records || [] } catch {} }

const handleSearch = () => { pagination.page = 1; loadData() }
const handleReset = () => { searchForm.customerId = undefined; searchForm.returnStatus = undefined; handleSearch() }

const handleCreate = () => {
  Object.assign(formData, { customerId: undefined, warehouseId: undefined, returnDate: null, reason: '', items: [] })
  dialogVisible.value = true
}

const handleFormSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    if (formData.items.length === 0) { ElMessage.warning('请添加退货明细'); return }
    submitLoading.value = true
    try {
      await createSalesReturn(formData)
      ElMessage.success('创建成功')
      dialogVisible.value = false
      loadData()
    } catch { ElMessage.error('创建失败') } finally { submitLoading.value = false }
  })
}

const handleView = async (row) => {
  try { currentRow.value = await getSalesReturnDetail(row.id); detailDialogVisible.value = true }
  catch { ElMessage.error('加载详情失败') }
}

const handleSubmit = async (row) => {
  try { await submitSalesReturn(row.id); ElMessage.success('提交成功'); loadData() }
  catch { ElMessage.error('提交失败') }
}

const handleApprove = async (row) => {
  try { await approveSalesReturn(row.id); ElMessage.success('审核成功'); loadData() }
  catch { ElMessage.error('审核失败') }
}

const handleCancel = async (row) => {
  await ElMessageBox.confirm('确认取消该退货单？', '提示', { type: 'warning' })
  try { await cancelSalesReturn(row.id); ElMessage.success('取消成功'); loadData() }
  catch (e) { if (e !== 'cancel') ElMessage.error('取消失败') }
}

const handleSizeChange = (size) => { pagination.size = size; loadData() }
const handlePageChange = (page) => { pagination.page = page; loadData() }

onMounted(() => { loadCustomers(); loadWarehouses(); loadProducts(); loadData() })
</script>

<style scoped lang="scss">
.sales-return-container { padding: 20px;
  .search-card { margin-bottom: 20px; .search-form { display: flex; flex-wrap: wrap; gap: 10px; } }
  .table-card { .pagination { margin-top: 20px; justify-content: flex-end; } }
}
</style>
