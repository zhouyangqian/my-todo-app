<!-- views/erp/product-promotion/index.vue - 商品促销管理页面 -->
<template>
  <div class="promotion-container">
    <el-card class="search-card">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="促销名称">
          <el-input v-model="searchForm.promotionName" placeholder="请输入名称" clearable />
        </el-form-item>
        <el-form-item label="促销类型">
          <el-select v-model="searchForm.promotionType" placeholder="请选择" clearable style="width: 130px;">
            <el-option label="限时折扣" :value="1" />
            <el-option label="满减" :value="2" />
            <el-option label="买赠" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择" clearable style="width: 120px;">
            <el-option label="未开始" :value="0" />
            <el-option label="进行中" :value="1" />
            <el-option label="已结束" :value="2" />
            <el-option label="已停用" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch"><el-icon><Search /></el-icon>搜索</el-button>
          <el-button @click="handleReset"><el-icon><Refresh /></el-icon>重置</el-button>
          <el-button type="success" @click="handleCreate"><el-icon><Plus /></el-icon>新增促销</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="table-card">
      <el-table :data="tableData" v-loading="loading" border stripe>
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="promotionName" label="促销名称" min-width="160" show-overflow-tooltip />
        <el-table-column prop="promotionType" label="类型" width="100">
          <template #default="{ row }">
            <el-tag :type="getTypeTag(row.promotionType)">{{ getTypeText(row.promotionType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="促销内容" min-width="200">
          <template #default="{ row }">
            <span v-if="row.promotionType === 1">打 {{ row.discountRate }} 折</span>
            <span v-else-if="row.promotionType === 2">满 ¥{{ row.minAmount }} 减 ¥{{ row.reduceAmount }}</span>
            <span v-else-if="row.promotionType === 3">买赠: 赠{{ row.giftProductName || '商品' }} x{{ row.giftQuantity }}</span>
          </template>
        </el-table-column>
        <el-table-column label="适用商品" width="140">
          <template #default="{ row }">
            <span v-if="row.productId">{{ row.productName || 'ID:' + row.productId }}</span>
            <el-tag v-else type="info" size="small">全场活动</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="起止时间" width="200">
          <template #default="{ row }">
            <div>{{ row.startDate?.substring(0, 10) }}</div>
            <div style="color: #909399;">至 {{ row.endDate?.substring(0, 10) }}</div>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="getStatusTag(row.status)">{{ getStatusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" fixed="right" width="200">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
            <el-button v-if="row.status === 0 || row.status === 2" type="success" link @click="handleEnable(row)">启用</el-button>
            <el-button v-if="row.status === 1" type="warning" link @click="handleDisable(row)">停用</el-button>
            <el-button type="danger" link @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination v-model:current-page="pagination.page" v-model:page-size="pagination.size"
        :page-sizes="[10, 20, 50, 100]" :total="pagination.total"
        layout="total, sizes, prev, pager, next, jumper" @size-change="handleSizeChange"
        @current-change="handlePageChange" class="pagination" />
    </el-card>

    <!-- 新建/编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑促销' : '新增促销'" width="600px" :close-on-click-modal="false">
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="110px">
        <el-form-item label="促销名称" prop="promotionName">
          <el-input v-model="formData.promotionName" placeholder="如: 夏季大促8折优惠" />
        </el-form-item>
        <el-form-item label="促销类型" prop="promotionType">
          <el-select v-model="formData.promotionType" placeholder="请选择" style="width: 100%;">
            <el-option label="限时折扣" :value="1" />
            <el-option label="满减" :value="2" />
            <el-option label="买赠" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="适用商品">
          <el-select v-model="formData.productId" placeholder="不选则全场活动" clearable filterable style="width: 100%;">
            <el-option v-for="p in productList" :key="p.id" :label="`${p.productName} (${p.productCode})`" :value="p.id" />
          </el-select>
        </el-form-item>

        <!-- 限时折扣 -->
        <template v-if="formData.promotionType === 1">
          <el-form-item label="折扣率" prop="discountRate">
            <el-input-number v-model="formData.discountRate" :min="0.01" :max="0.99" :step="0.05" :precision="2" style="width: 200px;" />
            <span style="margin-left: 8px; color: #909399;">如0.80表示8折</span>
          </el-form-item>
        </template>

        <!-- 满减 -->
        <template v-if="formData.promotionType === 2">
          <el-form-item label="满减门槛" prop="minAmount">
            <el-input-number v-model="formData.minAmount" :min="0.01" :precision="2" style="width: 200px;" />
            <span style="margin-left: 8px; color: #909399;">元</span>
          </el-form-item>
          <el-form-item label="优惠金额" prop="reduceAmount">
            <el-input-number v-model="formData.reduceAmount" :min="0.01" :precision="2" style="width: 200px;" />
            <span style="margin-left: 8px; color: #909399;">元</span>
          </el-form-item>
        </template>

        <!-- 买赠 -->
        <template v-if="formData.promotionType === 3">
          <el-form-item label="赠品商品" prop="giftProductId">
            <el-select v-model="formData.giftProductId" placeholder="请选择赠品" clearable filterable style="width: 100%;">
              <el-option v-for="p in productList" :key="p.id" :label="`${p.productName} (${p.productCode})`" :value="p.id" />
            </el-select>
          </el-form-item>
          <el-form-item label="赠品数量" prop="giftQuantity">
            <el-input-number v-model="formData.giftQuantity" :min="1" style="width: 200px;" />
          </el-form-item>
        </template>

        <el-form-item label="起止时间" prop="dateRange">
          <el-date-picker v-model="formData.dateRange" type="datetimerange" range-separator="至"
            start-placeholder="开始时间" end-placeholder="结束时间" value-format="YYYY-MM-DD HH:mm:ss"
            style="width: 100%;" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="formData.remark" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitLoading">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Plus } from '@element-plus/icons-vue'
import {
  getProductPromotionPage, createProductPromotion, updateProductPromotion,
  deleteProductPromotion, enableProductPromotion, disableProductPromotion,
  getProductPage
} from '@/api/erp'

const searchForm = reactive({ promotionName: '', promotionType: undefined, status: undefined })
const pagination = reactive({ page: 1, size: 10, total: 0 })
const tableData = ref([])
const loading = ref(false)
const productList = ref([])

const dialogVisible = ref(false)
const formRef = ref()
const submitLoading = ref(false)
const isEdit = ref(false)
const currentId = ref(null)
const formData = reactive({
  promotionName: '', promotionType: 1, productId: null,
  discountRate: 0.8, minAmount: null, reduceAmount: null,
  giftProductId: null, giftQuantity: 1,
  dateRange: [], remark: ''
})
const formRules = {
  promotionName: [{ required: true, message: '请输入促销名称', trigger: 'blur' }],
  promotionType: [{ required: true, message: '请选择促销类型', trigger: 'change' }],
  dateRange: [{ required: true, message: '请选择起止时间', trigger: 'change' }]
}

const getTypeTag = (t) => ({ 1: 'danger', 2: 'warning', 3: 'success' }[t] || 'info')
const getTypeText = (t) => ({ 1: '限时折扣', 2: '满减', 3: '买赠' }[t] || '未知')
const getStatusTag = (s) => ({ 0: 'info', 1: 'success', 2: 'warning', 3: 'danger' }[s] || 'info')
const getStatusText = (s) => ({ 0: '未开始', 1: '进行中', 2: '已结束', 3: '已停用' }[s] || '未知')

const loadData = async () => {
  loading.value = true
  try {
    const res = await getProductPromotionPage({
      page: pagination.page, size: pagination.size, ...searchForm
    })
    tableData.value = res.records || []
    pagination.total = res.total || 0
  } catch { ElMessage.error('加载数据失败') } finally { loading.value = false }
}

const loadProducts = async () => {
  try {
    const res = await getProductPage({ page: 1, size: 999 })
    productList.value = res.records || []
  } catch {}
}

const handleSearch = () => { pagination.page = 1; loadData() }
const handleReset = () => {
  searchForm.promotionName = ''; searchForm.promotionType = undefined; searchForm.status = undefined
  handleSearch()
}

const handleCreate = () => {
  isEdit.value = false; currentId.value = null
  Object.assign(formData, {
    promotionName: '', promotionType: 1, productId: null,
    discountRate: 0.8, minAmount: null, reduceAmount: null,
    giftProductId: null, giftQuantity: 1, dateRange: [], remark: ''
  })
  dialogVisible.value = true
}

const handleEdit = (row) => {
  isEdit.value = true; currentId.value = row.id
  Object.assign(formData, {
    promotionName: row.promotionName, promotionType: row.promotionType,
    productId: row.productId, discountRate: row.discountRate,
    minAmount: row.minAmount, reduceAmount: row.reduceAmount,
    giftProductId: row.giftProductId, giftQuantity: row.giftQuantity,
    dateRange: row.startDate && row.endDate ? [row.startDate, row.endDate] : [],
    remark: row.remark
  })
  dialogVisible.value = true
}

const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    if (!formData.dateRange || formData.dateRange.length !== 2) {
      ElMessage.warning('请选择起止时间'); return
    }
    submitLoading.value = true
    try {
      const data = {
        promotionName: formData.promotionName,
        promotionType: formData.promotionType,
        productId: formData.productId || null,
        discountRate: formData.promotionType === 1 ? formData.discountRate : null,
        minAmount: formData.promotionType === 2 ? formData.minAmount : null,
        reduceAmount: formData.promotionType === 2 ? formData.reduceAmount : null,
        giftProductId: formData.promotionType === 3 ? formData.giftProductId : null,
        giftQuantity: formData.promotionType === 3 ? formData.giftQuantity : null,
        startDate: formData.dateRange[0],
        endDate: formData.dateRange[1],
        remark: formData.remark
      }
      if (isEdit.value) { await updateProductPromotion(currentId.value, data); ElMessage.success('更新成功') }
      else { await createProductPromotion(data); ElMessage.success('创建成功') }
      dialogVisible.value = false; loadData()
    } catch { ElMessage.error(isEdit.value ? '更新失败' : '创建失败') } finally { submitLoading.value = false }
  })
}

const handleDelete = async (row) => {
  await ElMessageBox.confirm('确认删除该促销活动？', '提示', { type: 'warning' })
  try { await deleteProductPromotion(row.id); ElMessage.success('删除成功'); loadData() }
  catch (e) { if (e !== 'cancel') ElMessage.error('删除失败') }
}

const handleEnable = async (row) => {
  try { await enableProductPromotion(row.id); ElMessage.success('已启用'); loadData() }
  catch { ElMessage.error('操作失败') }
}

const handleDisable = async (row) => {
  try { await disableProductPromotion(row.id); ElMessage.success('已停用'); loadData() }
  catch { ElMessage.error('操作失败') }
}

const handleSizeChange = (size) => { pagination.size = size; loadData() }
const handlePageChange = (page) => { pagination.page = page; loadData() }

onMounted(() => { loadData(); loadProducts() })
</script>

<style scoped lang="scss">
.promotion-container {
  padding: 20px;

  .search-card {
    margin-bottom: 20px;
    .search-form { display: flex; flex-wrap: wrap; gap: 10px; }
  }

  .table-card {
    .pagination { margin-top: 20px; justify-content: flex-end; }
  }
}
</style>
