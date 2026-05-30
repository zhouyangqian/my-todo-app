<!-- views/finance/invoice/index.vue - 发票管理页面 -->
<template>
  <div class="invoice-container">
    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stat-row">
      <el-col :span="8">
        <el-card shadow="hover">
          <div class="stat-item">
            <div class="stat-label">开票总金额</div>
            <div class="stat-value amount">¥ {{ statistics.totalAmount?.toFixed(2) || '0.00' }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover">
          <div class="stat-item">
            <div class="stat-label">税额合计</div>
            <div class="stat-value tax">¥ {{ statistics.totalTax?.toFixed(2) || '0.00' }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover">
          <div class="stat-item">
            <div class="stat-label">发票总数</div>
            <div class="stat-value count">{{ statistics.totalCount || 0 }}</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 搜索栏区域 -->
    <el-card class="search-card">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="发票号">
          <el-input v-model="searchForm.invoiceNo" placeholder="请输入发票号" clearable />
        </el-form-item>
        <el-form-item label="发票类型">
          <el-select v-model="searchForm.invoiceType" placeholder="请选择类型" clearable>
            <el-option label="增值税普通发票" value="NORMAL" />
            <el-option label="增值税专用发票" value="SPECIAL" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable>
            <el-option label="草稿" value="DRAFT" />
            <el-option label="已开具" value="ISSUED" />
            <el-option label="已取消" value="CANCELLED" />
          </el-select>
        </el-form-item>
        <el-form-item label="日期范围">
          <el-date-picker
            v-model="searchForm.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
          />
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
          <span>发票列表</span>
          <el-button type="primary" @click="handleAdd" v-if="userStore.hasPermission('finance:invoice:create')">
            <el-icon><Plus /></el-icon>
            新增发票
          </el-button>
        </div>
      </template>

      <el-table :data="tableData" v-loading="loading" border stripe>
        <el-table-column prop="invoiceNo" label="发票号" width="180" />
        <el-table-column prop="invoiceType" label="发票类型" width="150">
          <template #default="{ row }">
            <el-tag :type="row.invoiceType === 'SPECIAL' ? 'warning' : 'info'">
              {{ row.invoiceType === 'SPECIAL' ? '增值税专用发票' : '增值税普通发票' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="relatedOrderNo" label="关联单据" width="180" />
        <el-table-column prop="customerName" label="客户/供应商" width="150" />
        <el-table-column prop="amount" label="金额" width="120">
          <template #default="{ row }">
            ¥ {{ row.amount?.toFixed(2) || '0.00' }}
          </template>
        </el-table-column>
        <el-table-column prop="taxAmount" label="税额" width="120">
          <template #default="{ row }">
            ¥ {{ row.taxAmount?.toFixed(2) || '0.00' }}
          </template>
        </el-table-column>
        <el-table-column prop="totalAmount" label="总金额" width="120">
          <template #default="{ row }">
            <span style="font-weight: bold;">¥ {{ row.totalAmount?.toFixed(2) || '0.00' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">{{ getStatusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="invoiceDate" label="开票日期" width="120" />
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" fixed="right" width="200">
          <template #default="{ row }">
            <el-button type="info" link @click="handleView(row)">详情</el-button>
            <el-button type="primary" link @click="handleEdit(row)" :disabled="row.status !== 'DRAFT'" v-if="userStore.hasPermission('finance:invoice:update')">
              编辑
            </el-button>
            <el-button type="danger" link @click="handleVoid(row)" :disabled="row.status === 'CANCELLED'" v-if="userStore.hasPermission('finance:invoice:void')">
              作废
            </el-button>
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

    <!-- 新增/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="700px"
      :close-on-click-modal="false"
    >
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="100px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="发票类型" prop="invoiceType">
              <el-select v-model="formData.invoiceType" placeholder="请选择发票类型" style="width: 100%;">
                <el-option label="增值税普通发票" value="NORMAL" />
                <el-option label="增值税专用发票" value="SPECIAL" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="税率" prop="taxRate">
              <el-input-number v-model="formData.taxRate" :min="0" :max="100" :precision="2" :step="1" style="width: 100%;" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="客户/供应商" prop="customerName">
              <el-input v-model="formData.customerName" placeholder="请输入客户/供应商名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="关联账单ID" prop="relatedBillId">
              <el-input v-model="formData.relatedBillId" placeholder="请输入关联账单ID" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="开票日期" prop="invoiceDate">
          <el-date-picker
            v-model="formData.invoiceDate"
            type="date"
            placeholder="选择开票日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            style="width: 100%;"
          />
        </el-form-item>

        <!-- 明细行 -->
        <el-divider content-position="left">发票明细</el-divider>
        <el-table :data="formData.items" border style="margin-bottom: 10px;">
          <el-table-column label="商品名称" min-width="150">
            <template #default="{ row }">
              <el-input v-model="row.productName" placeholder="商品名称" size="small" />
            </template>
          </el-table-column>
          <el-table-column label="数量" width="100">
            <template #default="{ row }">
              <el-input-number v-model="row.quantity" :min="1" :precision="0" size="small" style="width: 100%;" />
            </template>
          </el-table-column>
          <el-table-column label="单价" width="130">
            <template #default="{ row }">
              <el-input-number v-model="row.unitPrice" :min="0" :precision="2" size="small" style="width: 100%;" />
            </template>
          </el-table-column>
          <el-table-column label="税额" width="130">
            <template #default="{ row }">
              <el-input-number v-model="row.taxAmount" :min="0" :precision="2" size="small" style="width: 100%;" />
            </template>
          </el-table-column>
          <el-table-column label="小计" width="120">
            <template #default="{ row }">
              ¥ {{ ((row.quantity || 0) * (row.unitPrice || 0)).toFixed(2) }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="70" align="center">
            <template #default="{ $index }">
              <el-button type="danger" link size="small" @click="removeItem($index)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
        <el-button type="primary" link @click="addItem">
          <el-icon><Plus /></el-icon> 添加明细行
        </el-button>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitLoading">确定</el-button>
      </template>
    </el-dialog>

    <!-- 详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="发票详情"
      width="700px"
    >
      <el-descriptions :column="2" border>
        <el-descriptions-item label="发票号">{{ detailData.invoiceNo }}</el-descriptions-item>
        <el-descriptions-item label="发票类型">
          {{ detailData.invoiceType === 'SPECIAL' ? '增值税专用发票' : '增值税普通发票' }}
        </el-descriptions-item>
        <el-descriptions-item label="客户/供应商">{{ detailData.customerName }}</el-descriptions-item>
        <el-descriptions-item label="关联单据">{{ detailData.relatedOrderNo || '-' }}</el-descriptions-item>
        <el-descriptions-item label="金额">¥ {{ detailData.amount?.toFixed(2) || '0.00' }}</el-descriptions-item>
        <el-descriptions-item label="税额">¥ {{ detailData.taxAmount?.toFixed(2) || '0.00' }}</el-descriptions-item>
        <el-descriptions-item label="总金额">
          <span style="font-weight: bold; color: #409eff;">¥ {{ detailData.totalAmount?.toFixed(2) || '0.00' }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getStatusType(detailData.status)">{{ getStatusText(detailData.status) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="开票日期">{{ detailData.invoiceDate || '-' }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ detailData.createTime || '-' }}</el-descriptions-item>
      </el-descriptions>
      <div v-if="detailData.items && detailData.items.length > 0" style="margin-top: 20px;">
        <el-divider content-position="left">发票明细</el-divider>
        <el-table :data="detailData.items" border>
          <el-table-column prop="productName" label="商品名称" />
          <el-table-column prop="quantity" label="数量" width="80" />
          <el-table-column prop="unitPrice" label="单价" width="120">
            <template #default="{ row }">¥ {{ row.unitPrice?.toFixed(2) || '0.00' }}</template>
          </el-table-column>
          <el-table-column prop="taxAmount" label="税额" width="120">
            <template #default="{ row }">¥ {{ row.taxAmount?.toFixed(2) || '0.00' }}</template>
          </el-table-column>
          <el-table-column label="小计" width="120">
            <template #default="{ row }">¥ {{ ((row.quantity || 0) * (row.unitPrice || 0)).toFixed(2) }}</template>
          </el-table-column>
        </el-table>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Plus } from '@element-plus/icons-vue'
import {
  getInvoicePage,
  getInvoice,
  createInvoice,
  updateInvoice,
  voidInvoice,
  getInvoiceStatistics
} from '@/api/finance'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()

// ===== 统计数据 =====
const statistics = reactive({
  totalAmount: 0,
  totalTax: 0,
  totalCount: 0
})

// ===== 搜索相关 =====
const searchForm = reactive({
  invoiceNo: '',
  invoiceType: undefined,
  status: undefined,
  dateRange: null
})

// ===== 分页相关 =====
const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

// ===== 表格相关 =====
const tableData = ref([])
const loading = ref(false)

// ===== 对话框相关 =====
const dialogVisible = ref(false)
const dialogTitle = ref('新增发票')
const isEdit = ref(false)
const editId = ref(null)
const formRef = ref()
const submitLoading = ref(false)

// ===== 详情对话框 =====
const detailDialogVisible = ref(false)
const detailData = reactive({})

// ===== 表单数据 =====
const getDefaultFormData = () => ({
  invoiceType: 'NORMAL',
  customerName: '',
  relatedBillId: '',
  taxRate: 13,
  invoiceDate: '',
  items: [{ productName: '', quantity: 1, unitPrice: 0, taxAmount: 0 }]
})
const formData = reactive(getDefaultFormData())

const formRules = {
  invoiceType: [{ required: true, message: '请选择发票类型', trigger: 'change' }],
  customerName: [{ required: true, message: '请输入客户/供应商名称', trigger: 'blur' }],
  taxRate: [{ required: true, message: '请输入税率', trigger: 'blur' }],
  invoiceDate: [{ required: true, message: '请选择开票日期', trigger: 'change' }]
}

// ===== 状态辅助函数 =====
const getStatusType = (status) => {
  const types = { DRAFT: 'info', ISSUED: 'success', CANCELLED: 'danger' }
  return types[status] || 'info'
}

const getStatusText = (status) => {
  const texts = { DRAFT: '草稿', ISSUED: '已开具', CANCELLED: '已取消' }
  return texts[status] || '未知'
}

// ===== 明细行操作 =====
const addItem = () => {
  formData.items.push({ productName: '', quantity: 1, unitPrice: 0, taxAmount: 0 })
}

const removeItem = (index) => {
  if (formData.items.length <= 1) {
    ElMessage.warning('至少保留一条明细')
    return
  }
  formData.items.splice(index, 1)
}

// ===== 数据加载 =====
const loadStatistics = async () => {
  try {
    const res = await getInvoiceStatistics()
    if (res) {
      statistics.totalAmount = res.totalAmount || 0
      statistics.totalTax = res.totalTax || 0
      statistics.totalCount = res.totalCount || 0
    }
  } catch {
    // 统计加载失败不影响主流程
  }
}

const loadData = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.page,
      size: pagination.size,
      invoiceNo: searchForm.invoiceNo || undefined,
      invoiceType: searchForm.invoiceType || undefined,
      status: searchForm.status || undefined
    }
    if (searchForm.dateRange && searchForm.dateRange.length === 2) {
      params.startDate = searchForm.dateRange[0]
      params.endDate = searchForm.dateRange[1]
    }
    const res = await getInvoicePage(params)
    tableData.value = res.records || []
    pagination.total = res.total || 0
  } catch (error) {
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

// ===== 搜索相关 =====
const handleSearch = () => {
  pagination.page = 1
  loadData()
}

const handleReset = () => {
  searchForm.invoiceNo = ''
  searchForm.invoiceType = undefined
  searchForm.status = undefined
  searchForm.dateRange = null
  handleSearch()
}

// ===== 表单操作 =====
const handleAdd = () => {
  dialogTitle.value = '新增发票'
  isEdit.value = false
  editId.value = null
  Object.assign(formData, getDefaultFormData())
  dialogVisible.value = true
}

const handleEdit = async (row) => {
  dialogTitle.value = '编辑发票'
  isEdit.value = true
  editId.value = row.id
  try {
    const res = await getInvoice(row.id)
    Object.assign(formData, {
      invoiceType: res.invoiceType || 'NORMAL',
      customerName: res.customerName || '',
      relatedBillId: res.relatedBillId || '',
      taxRate: res.taxRate ?? 13,
      invoiceDate: res.invoiceDate || '',
      items: res.items && res.items.length > 0 ? res.items : [{ productName: '', quantity: 1, unitPrice: 0, taxAmount: 0 }]
    })
    dialogVisible.value = true
  } catch {
    ElMessage.error('获取发票详情失败')
  }
}

const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    submitLoading.value = true
    try {
      if (isEdit.value) {
        await updateInvoice(editId.value, formData)
        ElMessage.success('更新成功')
      } else {
        await createInvoice(formData)
        ElMessage.success('创建成功')
      }
      dialogVisible.value = false
      loadData()
      loadStatistics()
    } catch (error) {
      ElMessage.error(isEdit.value ? '更新失败' : '创建失败')
    } finally {
      submitLoading.value = false
    }
  })
}

const handleView = async (row) => {
  try {
    const res = await getInvoice(row.id)
    Object.assign(detailData, res)
    detailDialogVisible.value = true
  } catch {
    ElMessage.error('获取发票详情失败')
  }
}

const handleVoid = async (row) => {
  await ElMessageBox.confirm('确定要作废该发票吗？作废后不可恢复。', '作废确认', { type: 'warning' })
  try {
    await voidInvoice(row.id)
    ElMessage.success('发票已作废')
    loadData()
    loadStatistics()
  } catch {
    ElMessage.error('作废失败')
  }
}

// ===== 分页相关 =====
const handleSizeChange = (size) => {
  pagination.size = size
  loadData()
}

const handlePageChange = (page) => {
  pagination.page = page
  loadData()
}

onMounted(() => {
  loadData()
  loadStatistics()
})
</script>

<style scoped lang="scss">
.invoice-container {
  padding: 20px;

  .stat-row {
    margin-bottom: 20px;

    .stat-item {
      text-align: center;
      padding: 10px 0;

      .stat-label {
        font-size: 14px;
        color: #909399;
        margin-bottom: 8px;
      }

      .stat-value {
        font-size: 24px;
        font-weight: bold;

        &.amount { color: #409eff; }
        &.tax { color: #e6a23c; }
        &.count { color: #67c23a; }
      }
    }
  }

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
