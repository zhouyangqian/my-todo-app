<!-- views/dict/error-doc/index.vue - 错误文档管理页面 -->
<template>
  <div class="error-doc-container">
    <el-row :gutter="20">
      <!-- 左侧: 错误分类 -->
      <el-col :span="10">
        <el-card class="category-card">
          <template #header>
            <div class="card-header">
              <span>错误分类</span>
              <el-button type="primary" size="small" @click="handleAddCategory">
                <el-icon><Plus /></el-icon>
                新增
              </el-button>
            </div>
          </template>

          <el-table :data="categoryData" v-loading="categoryLoading" border stripe highlight-current-row @current-change="handleCategorySelect">
            <el-table-column prop="id" label="ID" width="60" />
            <el-table-column prop="categoryName" label="分类名称" min-width="120" />
            <el-table-column prop="categoryCode" label="分类编码" width="120" />
            <el-table-column prop="errorCount" label="错误数" width="70" />
            <el-table-column label="操作" fixed="right" width="120">
              <template #default="{ row }">
                <el-button type="warning" link size="small" @click.stop="handleEditCategory(row)">编辑</el-button>
                <el-button type="danger" link size="small" @click.stop="handleDeleteCategory(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>

      <!-- 右侧: 解决方案列表 -->
      <el-col :span="14">
        <el-card class="solution-card">
          <template #header>
            <div class="card-header">
              <span>解决方案{{ currentCategory ? ` - ${currentCategory.categoryName}` : '' }}</span>
              <el-button type="primary" size="small" @click="handleAddSolution" :disabled="!currentCategory">
                <el-icon><Plus /></el-icon>
                新增
              </el-button>
            </div>
          </template>

          <!-- 搜索 -->
          <el-form :inline="true" :model="solutionSearch" class="search-form">
            <el-form-item label="错误码">
              <el-input v-model="solutionSearch.errorCode" placeholder="请输入错误码" clearable size="small" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" size="small" @click="loadSolutions">
                <el-icon><Search /></el-icon>
                搜索
              </el-button>
              <el-button size="small" @click="handleSearchError">
                全局搜索
              </el-button>
            </el-form-item>
          </el-form>

          <el-table :data="solutionData" v-loading="solutionLoading" border stripe>
            <el-table-column prop="id" label="ID" width="60" />
            <el-table-column prop="errorCode" label="错误码" width="100" />
            <el-table-column prop="errorTitle" label="错误标题" width="140" show-overflow-tooltip />
            <el-table-column prop="solution" label="解决方案" min-width="200" show-overflow-tooltip />
            <el-table-column prop="severity" label="严重程度" width="90">
              <template #default="{ row }">
                <el-tag :type="row.severity === 'HIGH' ? 'danger' : row.severity === 'MEDIUM' ? 'warning' : 'info'" size="small">
                  {{ row.severity === 'HIGH' ? '高' : row.severity === 'MEDIUM' ? '中' : '低' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" fixed="right" width="80">
              <template #default="{ row }">
                <el-button type="danger" link size="small" @click="handleDeleteSolution(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>

    <!-- 错误分类对话框 -->
    <el-dialog
      v-model="categoryDialogVisible"
      :title="categoryDialogTitle"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form ref="categoryFormRef" :model="categoryForm" :rules="categoryRules" label-width="100px">
        <el-form-item label="分类名称" prop="categoryName">
          <el-input v-model="categoryForm.categoryName" placeholder="请输入分类名称" />
        </el-form-item>
        <el-form-item label="分类编码" prop="categoryCode">
          <el-input v-model="categoryForm.categoryCode" placeholder="请输入分类编码" />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="categoryForm.description" type="textarea" :rows="2" placeholder="请输入描述" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="categoryDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleCategorySubmit" :loading="categorySubmitLoading">确定</el-button>
      </template>
    </el-dialog>

    <!-- 解决方案对话框 -->
    <el-dialog
      v-model="solutionDialogVisible"
      :title="solutionDialogTitle"
      width="600px"
      :close-on-click-modal="false"
    >
      <el-form ref="solutionFormRef" :model="solutionForm" :rules="solutionRules" label-width="100px">
        <el-form-item label="错误码" prop="errorCode">
          <el-input v-model="solutionForm.errorCode" placeholder="请输入错误码" />
        </el-form-item>
        <el-form-item label="错误标题" prop="errorTitle">
          <el-input v-model="solutionForm.errorTitle" placeholder="请输入错误标题" />
        </el-form-item>
        <el-form-item label="严重程度" prop="severity">
          <el-select v-model="solutionForm.severity" style="width: 100%">
            <el-option label="高" value="HIGH" />
            <el-option label="中" value="MEDIUM" />
            <el-option label="低" value="LOW" />
          </el-select>
        </el-form-item>
        <el-form-item label="错误描述" prop="errorDescription">
          <el-input v-model="solutionForm.errorDescription" type="textarea" :rows="3" placeholder="请输入错误描述" />
        </el-form-item>
        <el-form-item label="解决方案" prop="solution">
          <el-input v-model="solutionForm.solution" type="textarea" :rows="4" placeholder="请输入解决方案" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="solutionDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSolutionSubmit" :loading="solutionSubmitLoading">确定</el-button>
      </template>
    </el-dialog>

    <!-- 全局搜索对话框 -->
    <el-dialog
      v-model="searchDialogVisible"
      title="全局错误搜索"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form :inline="true" class="search-form">
        <el-form-item>
          <el-input v-model="globalSearch.keyword" placeholder="输入错误码或关键词" clearable style="width: 300px" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleGlobalSearch">搜索</el-button>
        </el-form-item>
      </el-form>
      <el-table :data="searchResult" v-loading="searchLoading" border stripe max-height="350">
        <el-table-column prop="errorCode" label="错误码" width="100" />
        <el-table-column prop="errorTitle" label="标题" min-width="140" show-overflow-tooltip />
        <el-table-column prop="solution" label="解决方案" min-width="200" show-overflow-tooltip />
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Plus } from '@element-plus/icons-vue'
import {
  getErrorCategories,
  createErrorCategory,
  updateErrorCategory,
  deleteErrorCategory,
  getErrorSolutions,
  createErrorSolution,
  searchError
} from '@/api/dict'

// ==================== 错误分类相关 ====================
const categoryData = ref([])
const categoryLoading = ref(false)
const currentCategory = ref(null)

const loadCategories = async () => {
  categoryLoading.value = true
  try {
    const res = await getErrorCategories({})
    categoryData.value = res || []
  } catch (error) {
    ElMessage.error('加载错误分类失败')
  } finally {
    categoryLoading.value = false
  }
}

const handleCategorySelect = (row) => {
  currentCategory.value = row
  loadSolutions()
}

// ==================== 分类对话框 ====================
const categoryDialogVisible = ref(false)
const categoryDialogTitle = ref('新增错误分类')
const categoryFormRef = ref()
const categorySubmitLoading = ref(false)

const categoryForm = reactive({
  id: null,
  categoryName: '',
  categoryCode: '',
  description: ''
})

const categoryRules = {
  categoryName: [{ required: true, message: '请输入分类名称', trigger: 'blur' }],
  categoryCode: [{ required: true, message: '请输入分类编码', trigger: 'blur' }]
}

const handleAddCategory = () => {
  categoryDialogTitle.value = '新增错误分类'
  Object.assign(categoryForm, { id: null, categoryName: '', categoryCode: '', description: '' })
  categoryDialogVisible.value = true
}

const handleEditCategory = (row) => {
  categoryDialogTitle.value = '编辑错误分类'
  Object.assign(categoryForm, row)
  categoryForm.id = row.id
  categoryDialogVisible.value = true
}

const handleCategorySubmit = async () => {
  if (!categoryFormRef.value) return
  await categoryFormRef.value.validate(async (valid) => {
    if (!valid) return
    categorySubmitLoading.value = true
    try {
      if (categoryForm.id) {
        await updateErrorCategory(categoryForm.id, { ...categoryForm })
        ElMessage.success('更新成功')
      } else {
        await createErrorCategory({ ...categoryForm })
        ElMessage.success('创建成功')
      }
      categoryDialogVisible.value = false
      loadCategories()
    } catch (error) {
      ElMessage.error('操作失败')
    } finally {
      categorySubmitLoading.value = false
    }
  })
}

const handleDeleteCategory = async (row) => {
  await ElMessageBox.confirm(`确定要删除分类"${row.categoryName}"吗？`, '提示', { type: 'warning' })
  try {
    await deleteErrorCategory(row.id)
    ElMessage.success('删除成功')
    if (currentCategory.value?.id === row.id) {
      currentCategory.value = null
    }
    loadCategories()
  } catch (error) {
    ElMessage.error('删除失败')
  }
}

// ==================== 解决方案相关 ====================
const solutionSearch = reactive({ errorCode: '' })
const solutionData = ref([])
const solutionLoading = ref(false)

const loadSolutions = async () => {
  if (!currentCategory.value) return
  solutionLoading.value = true
  try {
    const res = await getErrorSolutions({
      categoryId: currentCategory.value.id,
      ...solutionSearch
    })
    solutionData.value = res || []
  } catch (error) {
    ElMessage.error('加载解决方案失败')
  } finally {
    solutionLoading.value = false
  }
}

// ==================== 解决方案对话框 ====================
const solutionDialogVisible = ref(false)
const solutionDialogTitle = ref('新增解决方案')
const solutionFormRef = ref()
const solutionSubmitLoading = ref(false)

const solutionForm = reactive({
  id: null,
  categoryId: null,
  errorCode: '',
  errorTitle: '',
  severity: 'MEDIUM',
  errorDescription: '',
  solution: ''
})

const solutionRules = {
  errorCode: [{ required: true, message: '请输入错误码', trigger: 'blur' }],
  errorTitle: [{ required: true, message: '请输入错误标题', trigger: 'blur' }],
  solution: [{ required: true, message: '请输入解决方案', trigger: 'blur' }]
}

const handleAddSolution = () => {
  solutionDialogTitle.value = '新增解决方案'
  Object.assign(solutionForm, {
    id: null, categoryId: currentCategory.value?.id,
    errorCode: '', errorTitle: '', severity: 'MEDIUM',
    errorDescription: '', solution: ''
  })
  solutionDialogVisible.value = true
}

const handleSolutionSubmit = async () => {
  if (!solutionFormRef.value) return
  await solutionFormRef.value.validate(async (valid) => {
    if (!valid) return
    solutionSubmitLoading.value = true
    try {
      await createErrorSolution({ ...solutionForm })
      ElMessage.success('创建成功')
      solutionDialogVisible.value = false
      loadSolutions()
    } catch (error) {
      ElMessage.error('创建失败')
    } finally {
      solutionSubmitLoading.value = false
    }
  })
}

const handleDeleteSolution = async (row) => {
  await ElMessageBox.confirm(`确定要删除错误"${row.errorCode}"的解决方案吗？`, '提示', { type: 'warning' })
  try {
    await deleteErrorCategory(row.id)
    ElMessage.success('删除成功')
    loadSolutions()
  } catch (error) {
    ElMessage.error('删除失败')
  }
}

// ==================== 全局搜索 ====================
const searchDialogVisible = ref(false)
const searchResult = ref([])
const searchLoading = ref(false)
const globalSearch = reactive({ keyword: '' })

const handleSearchError = () => {
  globalSearch.keyword = ''
  searchResult.value = []
  searchDialogVisible.value = true
}

const handleGlobalSearch = async () => {
  if (!globalSearch.keyword) {
    ElMessage.warning('请输入搜索关键词')
    return
  }
  searchLoading.value = true
  try {
    const res = await searchError({ keyword: globalSearch.keyword })
    searchResult.value = res || []
  } catch (error) {
    ElMessage.error('搜索失败')
  } finally {
    searchLoading.value = false
  }
}

// ==================== 初始化 ====================
onMounted(() => {
  loadCategories()
})
</script>

<style scoped lang="scss">
.error-doc-container {
  padding: 20px;

  .category-card,
  .solution-card {
    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
    }

    .search-form {
      margin-bottom: 12px;
    }
  }
}
</style>
