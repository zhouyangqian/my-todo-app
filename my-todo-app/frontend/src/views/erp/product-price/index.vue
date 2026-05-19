<!-- views/erp/product-price/index.vue - 商品价格管理页面 -->
<template>
  <div class="product-price-container">
    <el-card class="search-card">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="商品">
          <el-select v-model="searchForm.productId" placeholder="请选择商品" clearable filterable style="width: 200px;">
            <el-option v-for="p in productList" :key="p.id" :label="`${p.productName} (${p.productCode})`" :value="p.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="价格类型">
          <el-select v-model="searchForm.priceType" placeholder="请选择类型" clearable style="width: 120px;">
            <el-option label="成本价" :value="1" />
            <el-option label="销售价" :value="2" />
            <el-option label="批发价" :value="3" />
            <el-option label="会员价" :value="4" />
            <el-option label="促销价" :value="5" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch"><el-icon><Search /></el-icon>搜索</el-button>
          <el-button @click="handleReset"><el-icon><Refresh /></el-icon>重置</el-button>
          <el-button type="success" @click="handleCreate"><el-icon><Plus /></el-icon>新增价格</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="table-card">
      <el-table :data="tableData" v-loading="loading" border stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="productName" label="商品名称" min-width="150" />
        <el-table-column prop="productCode" label="商品编码" width="120" />
        <el-table-column prop="priceType" label="价格类型" width="100">
          <template #default="{ row }">
            <el-tag :type="getPriceTypeTag(row.priceType)">{{ getPriceTypeText(row.priceType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="price" label="价格" width="120" align="right">
          <template #default="{ row }">¥{{ row.price?.toFixed(4) }}</template>
        </el-table-column>
        <el-table-column prop="minQuantity" label="最小数量" width="100" align="right" />
        <el-table-column prop="startDate" label="生效日期" width="110" />
        <el-table-column prop="endDate" label="失效日期" width="110" />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">{{ row.status === 1 ? '启用' : '停用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" fixed="right" width="150">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
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
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑价格' : '新增价格'" width="500px" :close-on-click-modal="false">
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="100px">
        <el-form-item label="商品" prop="productId">
          <el-select v-model="formData.productId" placeholder="请选择商品" filterable style="width: 100%;">
            <el-option v-for="p in productList" :key="p.id" :label="`${p.productName} (${p.productCode})`" :value="p.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="价格类型" prop="priceType">
          <el-select v-model="formData.priceType" placeholder="请选择类型" style="width: 100%;">
            <el-option label="成本价" :value="1" />
            <el-option label="销售价" :value="2" />
            <el-option label="批发价" :value="3" />
            <el-option label="会员价" :value="4" />
            <el-option label="促销价" :value="5" />
          </el-select>
        </el-form-item>
        <el-form-item label="价格" prop="price">
          <el-input-number v-model="formData.price" :min="0" :precision="4" style="width: 100%;" />
        </el-form-item>
        <el-form-item label="最小数量">
          <el-input-number v-model="formData.minQuantity" :min="1" :precision="2" style="width: 100%;" />
        </el-form-item>
        <el-form-item label="生效日期">
          <el-date-picker v-model="formData.startDate" type="date" placeholder="选择日期" style="width: 100%;" />
        </el-form-item>
        <el-form-item label="失效日期">
          <el-date-picker v-model="formData.endDate" type="date" placeholder="选择日期" style="width: 100%;" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="formData.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">停用</el-radio>
          </el-radio-group>
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
import { getProductPricePage, createProductPrice, updateProductPrice, deleteProductPrice, getProductPage } from '@/api/erp'

const searchForm = reactive({ productId: undefined, priceType: undefined })
const pagination = reactive({ page: 1, size: 10, total: 0 })
const tableData = ref([])
const loading = ref(false)
const productList = ref([])

const dialogVisible = ref(false)
const formRef = ref()
const submitLoading = ref(false)
const isEdit = ref(false)
const currentId = ref(null)
const formData = reactive({ productId: undefined, priceType: undefined, price: 0, minQuantity: 1, startDate: null, endDate: null, status: 1, remark: '' })
const formRules = {
  productId: [{ required: true, message: '请选择商品', trigger: 'change' }],
  priceType: [{ required: true, message: '请选择价格类型', trigger: 'change' }],
  price: [{ required: true, message: '请输入价格', trigger: 'blur' }]
}

const getPriceTypeTag = (type) => ({ 1: 'info', 2: 'success', 3: 'warning', 4: 'primary', 5: 'danger' }[type] || 'info')
const getPriceTypeText = (type) => ({ 1: '成本价', 2: '销售价', 3: '批发价', 4: '会员价', 5: '促销价' }[type] || '未知')

const loadData = async () => {
  loading.value = true
  try {
    const res = await getProductPricePage({ page: pagination.page, size: pagination.size, ...searchForm })
    tableData.value = res.records || []
    pagination.total = res.total || 0
  } catch { ElMessage.error('加载数据失败') } finally { loading.value = false }
}

const loadProducts = async () => { try { productList.value = (await getProductPage({ page: 1, size: 1000 })).records || [] } catch {} }

const handleSearch = () => { pagination.page = 1; loadData() }
const handleReset = () => { searchForm.productId = undefined; searchForm.priceType = undefined; handleSearch() }

const handleCreate = () => {
  isEdit.value = false; currentId.value = null
  Object.assign(formData, { productId: undefined, priceType: undefined, price: 0, minQuantity: 1, startDate: null, endDate: null, status: 1, remark: '' })
  dialogVisible.value = true
}

const handleEdit = (row) => {
  isEdit.value = true; currentId.value = row.id
  Object.assign(formData, { productId: row.productId, priceType: row.priceType, price: row.price, minQuantity: row.minQuantity, startDate: row.startDate, endDate: row.endDate, status: row.status, remark: row.remark })
  dialogVisible.value = true
}

const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    submitLoading.value = true
    try {
      if (isEdit.value) { await updateProductPrice(currentId.value, formData); ElMessage.success('更新成功') }
      else { await createProductPrice(formData); ElMessage.success('创建成功') }
      dialogVisible.value = false; loadData()
    } catch { ElMessage.error(isEdit.value ? '更新失败' : '创建失败') } finally { submitLoading.value = false }
  })
}

const handleDelete = async (row) => {
  await ElMessageBox.confirm('确认删除该价格记录？', '提示', { type: 'warning' })
  try { await deleteProductPrice(row.id); ElMessage.success('删除成功'); loadData() }
  catch (e) { if (e !== 'cancel') ElMessage.error('删除失败') }
}

const handleSizeChange = (size) => { pagination.size = size; loadData() }
const handlePageChange = (page) => { pagination.page = page; loadData() }

onMounted(() => { loadProducts(); loadData() })
</script>

<style scoped lang="scss">
.product-price-container { padding: 20px;
  .search-card { margin-bottom: 20px; .search-form { display: flex; flex-wrap: wrap; gap: 10px; } }
  .table-card { .pagination { margin-top: 20px; justify-content: flex-end; } }
}
</style>
