<!-- views/erp/product/index.vue - 商品管理页面
  提供商品的搜索、新增、编辑、删除等功能
  包含搜索栏（按名称、分类、状态筛选）、数据表格（带分页）、新增/编辑对话框
-->
<template>
  <div class="product-container">
    <!-- 搜索栏区域：支持按商品名称、SKU编码、状态筛选 -->
    <el-card class="search-card">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="商品名称">
          <el-input v-model="searchForm.name" placeholder="请输入商品名称" clearable />
        </el-form-item>
        <el-form-item label="商品编码">
          <el-input v-model="searchForm.sku" placeholder="请输入商品编码" clearable />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable>
            <el-option label="启用" :value="1" />
            <el-option label="禁用" :value="0" />
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

    <!-- 数据表格 -->
    <el-card class="table-card">
      <template #header>
        <div class="card-header">
          <span>商品列表</span>
          <el-button type="primary" @click="handleAdd">
            <el-icon><Plus /></el-icon>
            新增商品
          </el-button>
        </div>
      </template>

      <el-table :data="tableData" v-loading="loading" border stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="sku" label="商品编码" width="120" />
        <el-table-column prop="name" label="商品名称" width="200" />
        <el-table-column prop="brand" label="品牌" width="100" />
        <el-table-column prop="model" label="型号" width="100" />
        <el-table-column prop="unit" label="单位" width="60" />
        <el-table-column prop="costPrice" label="成本价" width="100">
          <template #default="{ row }">
            ¥{{ row.costPrice?.toFixed(2) || '0.00' }}
          </template>
        </el-table-column>
        <el-table-column prop="salePrice" label="销售价" width="100">
          <template #default="{ row }">
            ¥{{ row.salePrice?.toFixed(2) || '0.00' }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="180" />
        <el-table-column label="操作" fixed="right" width="150">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
            <el-button type="danger" link @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
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
      width="600px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="80px"
      >
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="商品编码" prop="sku">
              <el-input v-model="formData.sku" placeholder="请输入商品编码" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="商品名称" prop="name">
              <el-input v-model="formData.name" placeholder="请输入商品名称" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="品牌" prop="brand">
              <el-input v-model="formData.brand" placeholder="请输入品牌" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="型号" prop="model">
              <el-input v-model="formData.model" placeholder="请输入型号" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="单位" prop="unit">
              <el-select v-model="formData.unit" placeholder="请选择单位" style="width: 100%">
                <el-option label="个" value="个" />
                <el-option label="件" value="件" />
                <el-option label="台" value="台" />
                <el-option label="套" value="套" />
                <el-option label="箱" value="箱" />
                <el-option label="公斤" value="kg" />
                <el-option label="米" value="m" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态" prop="status">
              <el-radio-group v-model="formData.status">
                <el-radio :value="1">启用</el-radio>
                <el-radio :value="0">禁用</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="成本价" prop="costPrice">
              <el-input-number
                v-model="formData.costPrice"
                :precision="2"
                :min="0"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="销售价" prop="salePrice">
              <el-input-number
                v-model="formData.salePrice"
                :precision="2"
                :min="0"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="描述" prop="description">
          <el-input
            v-model="formData.description"
            type="textarea"
            :rows="3"
            placeholder="请输入商品描述"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitLoading">
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { Search, Refresh, Plus } from '@element-plus/icons-vue'
import { getProductPage, getProduct, createProduct, updateProduct, deleteProduct, type Product } from '@/api/erp'

// 搜索表单
const searchForm = reactive({
  name: '',
  sku: '',
  status: undefined as number | undefined
})

// 分页
const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

// 表格数据
const tableData = ref<Product[]>([])
const loading = ref(false)

// 对话框
const dialogVisible = ref(false)
const dialogTitle = ref('新增商品')
const formRef = ref<FormInstance>()
const submitLoading = ref(false)

// 表单数据
const formData = reactive({
  id: undefined as number | undefined,
  sku: '',
  name: '',
  brand: '',
  model: '',
  unit: '',
  costPrice: 0,
  salePrice: 0,
  status: 1,
  description: ''
})

// 表单验证规则
const formRules: FormRules = {
  sku: [
    { required: true, message: '请输入商品编码', trigger: 'blur' }
  ],
  name: [
    { required: true, message: '请输入商品名称', trigger: 'blur' }
  ],
  unit: [
    { required: true, message: '请选择单位', trigger: 'change' }
  ]
}

// 加载数据
const loadData = async () => {
  loading.value = true
  try {
    const res = await getProductPage({
      page: pagination.page,
      size: pagination.size,
      ...searchForm
    })
    tableData.value = res.records
    pagination.total = res.total
  } catch (error) {
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  pagination.page = 1
  loadData()
}

// 重置
const handleReset = () => {
  searchForm.name = ''
  searchForm.sku = ''
  searchForm.status = undefined
  handleSearch()
}

// 新增
const handleAdd = () => {
  dialogTitle.value = '新增商品'
  formData.id = undefined
  formData.sku = ''
  formData.name = ''
  formData.brand = ''
  formData.model = ''
  formData.unit = ''
  formData.costPrice = 0
  formData.salePrice = 0
  formData.status = 1
  formData.description = ''
  dialogVisible.value = true
}

// 编辑
const handleEdit = async (row: Product) => {
  dialogTitle.value = '编辑商品'
  try {
    const data = await getProduct(row.id)
    Object.assign(formData, data)
    dialogVisible.value = true
  } catch (error) {
    ElMessage.error('获取数据失败')
  }
}

// 提交
const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    submitLoading.value = true
    try {
      if (formData.id) {
        await updateProduct(formData.id, formData)
        ElMessage.success('更新成功')
      } else {
        await createProduct(formData)
        ElMessage.success('创建成功')
      }
      dialogVisible.value = false
      loadData()
    } catch (error) {
      ElMessage.error('操作失败')
    } finally {
      submitLoading.value = false
    }
  })
}

// 删除
const handleDelete = async (row: Product) => {
  await ElMessageBox.confirm('确定要删除该商品吗?', '提示', {
    type: 'warning'
  })
  try {
    await deleteProduct(row.id)
    ElMessage.success('删除成功')
    loadData()
  } catch (error) {
    ElMessage.error('删除失败')
  }
}

// 分页变化
const handleSizeChange = (size: number) => {
  pagination.size = size
  loadData()
}

const handlePageChange = (page: number) => {
  pagination.page = page
  loadData()
}

// 初始化
onMounted(() => {
  loadData()
})
</script>

<style scoped lang="scss">
.product-container {
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
