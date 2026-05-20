<!-- views/erp/product-category/index.vue - 商品分类管理页面 -->
<template>
  <div class="product-category-container">
    <!-- 搜索栏区域 -->
    <el-card class="search-card">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="分类名称">
          <el-input v-model="searchForm.categoryName" placeholder="请输入分类名称" clearable />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable style="width: 120px;">
            <el-option label="启用" :value="1" />
            <el-option label="停用" :value="0" />
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
          <el-button type="success" @click="handleCreate" v-if="userStore.hasPermission('erp:productCategory:create')">
            <el-icon><Plus /></el-icon>
            新建分类
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 数据表格区域 -->
    <el-card class="table-card">
      <el-table :data="tableData" v-loading="loading" border stripe row-key="id">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="categoryCode" label="分类编码" width="120" />
        <el-table-column prop="categoryName" label="分类名称" width="180" />
        <el-table-column prop="parentId" label="父分类" width="120">
          <template #default="{ row }">
            {{ getParentName(row.parentId) }}
          </template>
        </el-table-column>
        <el-table-column prop="sortOrder" label="排序" width="80" align="center" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" min-width="150" show-overflow-tooltip />
        <el-table-column label="操作" fixed="right" width="180">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleEdit(row)" v-if="userStore.hasPermission('erp:productCategory:update')">编辑</el-button>
            <el-button type="danger" link @click="handleDelete(row)" v-if="userStore.hasPermission('erp:productCategory:delete')">删除</el-button>
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

    <!-- 创建/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑分类' : '新建分类'"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="100px">
        <el-form-item label="分类编码" prop="categoryCode">
          <el-input v-model="formData.categoryCode" placeholder="请输入分类编码" :disabled="isEdit" />
        </el-form-item>
        <el-form-item label="分类名称" prop="categoryName">
          <el-input v-model="formData.categoryName" placeholder="请输入分类名称" />
        </el-form-item>
        <el-form-item label="父分类">
          <el-select v-model="formData.parentId" placeholder="请选择父分类（不选则为顶级分类）" clearable style="width: 100%;">
            <el-option label="顶级分类" :value="0" />
            <el-option
              v-for="cat in allCategories"
              :key="cat.id"
              :label="cat.categoryName"
              :value="cat.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="排序号">
          <el-input-number v-model="formData.sortOrder" :min="0" :max="999" style="width: 100%;" />
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
import {
  getProductCategoryPage,
  getAllProductCategories,
  createProductCategory,
  updateProductCategory,
  deleteProductCategory
} from '@/api/erp'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()

const searchForm = reactive({
  categoryName: '',
  status: undefined
})

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

const tableData = ref([])
const loading = ref(false)
const allCategories = ref([])

// 表单相关
const dialogVisible = ref(false)
const formRef = ref()
const submitLoading = ref(false)
const isEdit = ref(false)
const currentId = ref(null)

const formData = reactive({
  categoryCode: '',
  categoryName: '',
  parentId: 0,
  sortOrder: 0,
  status: 1,
  remark: ''
})

const formRules = {
  categoryCode: [
    { required: true, message: '请输入分类编码', trigger: 'blur' },
    { pattern: /^[A-Za-z0-9_-]+$/, message: '编码只能包含字母、数字、下划线和横线', trigger: 'blur' }
  ],
  categoryName: [
    { required: true, message: '请输入分类名称', trigger: 'blur' }
  ]
}

const getParentName = (parentId) => {
  if (!parentId || parentId === 0) return '顶级分类'
  const parent = allCategories.value.find(c => c.id === parentId)
  return parent ? parent.categoryName : '-'
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await getProductCategoryPage({
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

const loadAllCategories = async () => {
  try {
    const res = await getAllProductCategories()
    allCategories.value = res || []
  } catch (error) {
    console.error('加载分类列表失败', error)
  }
}

const handleSearch = () => {
  pagination.page = 1
  loadData()
}

const handleReset = () => {
  searchForm.categoryName = ''
  searchForm.status = undefined
  handleSearch()
}

const handleCreate = () => {
  isEdit.value = false
  currentId.value = null
  Object.assign(formData, {
    categoryCode: '',
    categoryName: '',
    parentId: 0,
    sortOrder: 0,
    status: 1,
    remark: ''
  })
  dialogVisible.value = true
}

const handleEdit = (row) => {
  isEdit.value = true
  currentId.value = row.id
  Object.assign(formData, {
    categoryCode: row.categoryCode,
    categoryName: row.categoryName,
    parentId: row.parentId || 0,
    sortOrder: row.sortOrder || 0,
    status: row.status,
    remark: row.remark || ''
  })
  dialogVisible.value = true
}

const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    submitLoading.value = true
    try {
      if (isEdit.value) {
        await updateProductCategory(currentId.value, formData)
        ElMessage.success('更新成功')
      } else {
        await createProductCategory(formData)
        ElMessage.success('创建成功')
      }
      dialogVisible.value = false
      loadData()
      loadAllCategories()
    } catch (error) {
      ElMessage.error(isEdit.value ? '更新失败' : '创建失败')
    } finally {
      submitLoading.value = false
    }
  })
}

const handleDelete = async (row) => {
  await ElMessageBox.confirm(`确认删除分类"${row.categoryName}"？`, '提示', { type: 'warning' })
  try {
    await deleteProductCategory(row.id)
    ElMessage.success('删除成功')
    loadData()
    loadAllCategories()
  } catch (error) {
    if (error !== 'cancel') ElMessage.error('删除失败')
  }
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
  loadAllCategories()
  loadData()
})
</script>

<style scoped lang="scss">
.product-category-container {
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
    .pagination {
      margin-top: 20px;
      justify-content: flex-end;
    }
  }
}
</style>
