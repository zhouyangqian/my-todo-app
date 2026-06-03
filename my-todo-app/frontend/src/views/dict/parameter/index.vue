<!-- views/dict/parameter/index.vue - 参数管理页面 -->
<template>
  <div class="parameter-container">
    <el-row :gutter="20">
      <!-- 左侧: 参数分类树 -->
      <el-col :span="6">
        <el-card class="category-card">
          <template #header>
            <div class="card-header">
              <span>参数分类</span>
              <el-button type="primary" size="small" @click="handleAddCategory">
                <el-icon><Plus /></el-icon>
                新增
              </el-button>
            </div>
          </template>
          <el-input
            v-model="categoryFilter"
            placeholder="搜索分类"
            clearable
            class="category-filter"
          />
          <el-tree
            ref="categoryTreeRef"
            :data="categoryTree"
            :props="{ label: 'categoryName', children: 'children' }"
            :filter-node-method="filterCategoryNode"
            node-key="id"
            highlight-current
            default-expand-all
            @node-click="handleCategoryClick"
          >
            <template #default="{ node, data }">
              <span class="tree-node">
                <span>{{ node.label }}</span>
                <span class="tree-node-actions">
                  <el-button type="primary" link size="small" @click.stop="handleEditCategory(data)">
                    <el-icon><Edit /></el-icon>
                  </el-button>
                  <el-button type="danger" link size="small" @click.stop="handleDeleteCategory(data)">
                    <el-icon><Delete /></el-icon>
                  </el-button>
                </span>
              </span>
            </template>
          </el-tree>
        </el-card>
      </el-col>

      <!-- 右侧: 参数字典表格 + 参数项 -->
      <el-col :span="18">
        <!-- 参数字典搜索 -->
        <el-card class="search-card">
          <el-form :inline="true" :model="dictSearchForm" class="search-form">
            <el-form-item label="参数名称">
              <el-input v-model="dictSearchForm.paramName" placeholder="请输入参数名称" clearable />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleDictSearch">
                <el-icon><Search /></el-icon>
                搜索
              </el-button>
              <el-button @click="handleDictReset">
                <el-icon><Refresh /></el-icon>
                重置
              </el-button>
            </el-form-item>
          </el-form>
        </el-card>

        <!-- 参数字典表格 -->
        <el-card class="table-card">
          <template #header>
            <div class="card-header">
              <span>参数字典列表{{ currentCategory ? ` - ${currentCategory.categoryName}` : '' }}</span>
              <el-button type="primary" @click="handleAddDictionary" :disabled="!currentCategory">
                <el-icon><Plus /></el-icon>
                新增参数
              </el-button>
            </div>
          </template>

          <el-table :data="dictTableData" v-loading="dictLoading" border stripe>
            <el-table-column prop="id" label="ID" width="70" />
            <el-table-column prop="paramName" label="参数名称" width="140" />
            <el-table-column prop="paramCode" label="参数编码" width="140" />
            <el-table-column prop="paramValue" label="参数值" min-width="180" show-overflow-tooltip />
            <el-table-column prop="valueType" label="值类型" width="100">
              <template #default="{ row }">
                <el-tag size="small">{{ row.valueType }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="80">
              <template #default="{ row }">
                <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
                  {{ row.status === 1 ? '启用' : '禁用' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="description" label="描述" min-width="120" show-overflow-tooltip />
            <el-table-column label="操作" fixed="right" width="240">
              <template #default="{ row }">
                <el-button type="primary" link @click="handleViewItems(row)">参数项</el-button>
                <el-button type="warning" link @click="handleEditDictionary(row)">编辑</el-button>
                <el-button type="danger" link @click="handleDeleteDictionary(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>

          <el-pagination
            v-model:current-page="dictPagination.page"
            v-model:page-size="dictPagination.size"
            :page-sizes="[10, 20, 50, 100]"
            :total="dictPagination.total"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="handleDictSizeChange"
            @current-change="handleDictPageChange"
            class="pagination"
          />
        </el-card>

        <!-- 参数项区域 -->
        <el-card v-if="currentDictionary" class="items-card">
          <template #header>
            <div class="card-header">
              <span>参数项 - {{ currentDictionary.paramName }}</span>
              <div>
                <el-button type="primary" size="small" @click="handleAddItem">
                  <el-icon><Plus /></el-icon>
                  新增项
                </el-button>
                <el-button type="success" size="small" @click="handleBatchSave">
                  批量保存
                </el-button>
              </div>
            </div>
          </template>

          <el-table :data="paramItems" border stripe>
            <el-table-column prop="id" label="ID" width="70" />
            <el-table-column prop="itemLabel" label="项标签" width="150">
              <template #default="{ row }">
                <el-input v-if="row._editing" v-model="row.itemLabel" size="small" />
                <span v-else>{{ row.itemLabel }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="itemValue" label="项值" width="150">
              <template #default="{ row }">
                <el-input v-if="row._editing" v-model="row.itemValue" size="small" />
                <span v-else>{{ row.itemValue }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="sortOrder" label="排序" width="100">
              <template #default="{ row }">
                <el-input-number v-if="row._editing" v-model="row.sortOrder" :min="0" size="small" style="width: 80px" />
                <span v-else>{{ row.sortOrder }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="100">
              <template #default="{ row }">
                <el-switch
                  v-if="row._editing"
                  v-model="row.status"
                  :active-value="1"
                  :inactive-value="0"
                  size="small"
                />
                <el-tag v-else :type="row.status === 1 ? 'success' : 'info'" size="small">
                  {{ row.status === 1 ? '启用' : '禁用' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="180">
              <template #default="{ row, $index }">
                <template v-if="row._editing">
                  <el-button type="success" link size="small" @click="handleSaveItem(row)">保存</el-button>
                  <el-button link size="small" @click="handleCancelItem(row, $index)">取消</el-button>
                </template>
                <template v-else>
                  <el-button type="primary" link size="small" @click="row._editing = true">编辑</el-button>
                  <el-button type="danger" link size="small" @click="handleDeleteItem(row, $index)">删除</el-button>
                </template>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>

    <!-- 分类新增/编辑对话框 -->
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
        <el-form-item label="分类类型" prop="categoryType">
          <el-select v-model="categoryForm.categoryType" style="width: 100%">
            <el-option label="系统参数" value="SYSTEM" />
            <el-option label="业务参数" value="BUSINESS" />
          </el-select>
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="categoryForm.description" type="textarea" :rows="2" placeholder="请输入描述" />
        </el-form-item>
        <el-form-item label="排序" prop="sortOrder">
          <el-input-number v-model="categoryForm.sortOrder" :min="0" style="width: 100%" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="categoryForm.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="categoryDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleCategorySubmit" :loading="categorySubmitLoading">确定</el-button>
      </template>
    </el-dialog>

    <!-- 参数字典新增/编辑对话框 -->
    <el-dialog
      v-model="dictDialogVisible"
      :title="dictDialogTitle"
      width="600px"
      :close-on-click-modal="false"
    >
      <el-form ref="dictFormRef" :model="dictForm" :rules="dictRules" label-width="100px">
        <el-form-item label="参数名称" prop="paramName">
          <el-input v-model="dictForm.paramName" placeholder="请输入参数名称" />
        </el-form-item>
        <el-form-item label="参数值" prop="paramValue">
          <el-input
            v-model="dictForm.paramValue"
            type="textarea"
            :rows="3"
            placeholder="请输入参数值（支持JSON）"
          />
        </el-form-item>
        <el-form-item label="值类型" prop="valueType">
          <el-select v-model="dictForm.valueType" style="width: 100%">
            <el-option label="字符串" value="STRING" />
            <el-option label="数字" value="NUMBER" />
            <el-option label="布尔" value="BOOLEAN" />
            <el-option label="JSON" value="JSON" />
          </el-select>
        </el-form-item>
        <el-form-item label="验证规则" prop="validationRule">
          <el-input v-model="dictForm.validationRule" placeholder="正则表达式或范围（可选）" />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="dictForm.description" type="textarea" :rows="2" placeholder="请输入描述" />
        </el-form-item>
        <el-form-item label="排序" prop="sortOrder">
          <el-input-number v-model="dictForm.sortOrder" :min="0" style="width: 100%" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="dictForm.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dictDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleDictSubmit" :loading="dictSubmitLoading">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Plus, Edit, Delete } from '@element-plus/icons-vue'
import {
  getParameterCategoryPage,
  getAllParameterCategories,
  createParameterCategory,
  updateParameterCategory,
  deleteParameterCategory,
  getParameterDictionaryPage,
  createParameterDictionary,
  updateParameterDictionary,
  deleteParameterDictionary,
  getParameterItems,
  createParameterItem,
  updateParameterItem,
  deleteParameterItem,
  batchSaveParameterItems
} from '@/api/dict'

// ==================== 分类相关 ====================
const categoryTreeRef = ref()
const categoryFilter = ref('')
const categoryTree = ref([])
const currentCategory = ref(null)

// 过滤分类树
const filterCategoryNode = (value, data) => {
  if (!value) return true
  return data.categoryName.includes(value)
}
watch(categoryFilter, (val) => {
  categoryTreeRef.value?.filter(val)
})

// 加载分类列表（作为平铺树节点）
const loadCategories = async () => {
  try {
    const res = await getAllParameterCategories()
    categoryTree.value = (res || []).map(item => ({ ...item, children: [] }))
  } catch (error) {
    ElMessage.error('加载分类失败')
  }
}

const handleCategoryClick = (data) => {
  currentCategory.value = data
  currentDictionary.value = null
  loadDictionaries()
}

// ==================== 分类对话框 ====================
const categoryDialogVisible = ref(false)
const categoryDialogTitle = ref('新增参数分类')
const categoryFormRef = ref()
const categorySubmitLoading = ref(false)

const categoryForm = reactive({
  id: null,
  categoryName: '',
  categoryType: 'SYSTEM',
  description: '',
  sortOrder: 0,
  status: 1
})

const categoryRules = {
  categoryName: [{ required: true, message: '请输入分类名称', trigger: 'blur' }],
  categoryType: [{ required: true, message: '请选择分类类型', trigger: 'change' }]
}

const handleAddCategory = () => {
  categoryDialogTitle.value = '新增参数分类'
  Object.assign(categoryForm, {
    id: null,
    categoryName: '',
    categoryType: 'SYSTEM',
    description: '',
    sortOrder: 0,
    status: 1
  })
  categoryDialogVisible.value = true
}

const handleEditCategory = (data) => {
  categoryDialogTitle.value = '编辑参数分类'
  Object.assign(categoryForm, data)
  categoryForm.id = data.id
  categoryDialogVisible.value = true
}

const handleCategorySubmit = async () => {
  if (!categoryFormRef.value) return
  await categoryFormRef.value.validate(async (valid) => {
    if (!valid) return
    categorySubmitLoading.value = true
    try {
      if (categoryForm.id) {
        await updateParameterCategory(categoryForm.id, { ...categoryForm })
        ElMessage.success('更新成功')
      } else {
        await createParameterCategory({ ...categoryForm })
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

const handleDeleteCategory = async (data) => {
  await ElMessageBox.confirm(`确定要删除分类"${data.categoryName}"吗？`, '提示', { type: 'warning' })
  try {
    await deleteParameterCategory(data.id)
    ElMessage.success('删除成功')
    if (currentCategory.value?.id === data.id) {
      currentCategory.value = null
    }
    loadCategories()
  } catch (error) {
    ElMessage.error('删除失败')
  }
}

// ==================== 参数字典相关 ====================
const dictSearchForm = reactive({ paramName: '' })
const dictPagination = reactive({ page: 1, size: 10, total: 0 })
const dictTableData = ref([])
const dictLoading = ref(false)

const loadDictionaries = async () => {
  if (!currentCategory.value) return
  dictLoading.value = true
  try {
    const res = await getParameterDictionaryPage({
      page: dictPagination.page,
      size: dictPagination.size,
      categoryId: currentCategory.value.id,
      ...dictSearchForm
    })
    dictTableData.value = res.records || []
    dictPagination.total = res.total || 0
  } catch (error) {
    ElMessage.error('加载参数字典失败')
  } finally {
    dictLoading.value = false
  }
}

const handleDictSearch = () => {
  dictPagination.page = 1
  loadDictionaries()
}

const handleDictReset = () => {
  dictSearchForm.paramName = ''
  handleDictSearch()
}

const handleDictSizeChange = (size) => {
  dictPagination.size = size
  loadDictionaries()
}

const handleDictPageChange = (page) => {
  dictPagination.page = page
  loadDictionaries()
}

// ==================== 参数字典对话框 ====================
const dictDialogVisible = ref(false)
const dictDialogTitle = ref('新增参数字典')
const dictFormRef = ref()
const dictSubmitLoading = ref(false)

const dictForm = reactive({
  id: null,
  categoryId: null,
  paramName: '',
  paramValue: '',
  valueType: 'STRING',
  description: '',
  validationRule: '',
  sortOrder: 0,
  status: 1,
  version: 0
})

const dictRules = {
  paramName: [{ required: true, message: '请输入参数名称', trigger: 'blur' }],
  valueType: [{ required: true, message: '请选择值类型', trigger: 'change' }]
}

const handleAddDictionary = () => {
  dictDialogTitle.value = '新增参数字典'
  Object.assign(dictForm, {
    id: null,
    categoryId: currentCategory.value?.id,
    paramName: '',
    paramValue: '',
    valueType: 'STRING',
    description: '',
    validationRule: '',
    sortOrder: 0,
    status: 1,
    version: 0
  })
  dictDialogVisible.value = true
}

const handleEditDictionary = (row) => {
  dictDialogTitle.value = '编辑参数字典'
  Object.assign(dictForm, row)
  dictForm.id = row.id
  dictDialogVisible.value = true
}

const handleDictSubmit = async () => {
  if (!dictFormRef.value) return
  await dictFormRef.value.validate(async (valid) => {
    if (!valid) return
    dictSubmitLoading.value = true
    try {
      if (dictForm.id) {
        await updateParameterDictionary(dictForm.id, { ...dictForm })
        ElMessage.success('更新成功')
      } else {
        await createParameterDictionary({ ...dictForm })
        ElMessage.success('创建成功')
      }
      dictDialogVisible.value = false
      loadDictionaries()
    } catch (error) {
      ElMessage.error('操作失败')
    } finally {
      dictSubmitLoading.value = false
    }
  })
}

const handleDeleteDictionary = async (row) => {
  await ElMessageBox.confirm(`确定要删除参数"${row.paramName}"吗？`, '提示', { type: 'warning' })
  try {
    await deleteParameterDictionary(row.id)
    ElMessage.success('删除成功')
    if (currentDictionary.value?.id === row.id) {
      currentDictionary.value = null
    }
    loadDictionaries()
  } catch (error) {
    ElMessage.error('删除失败')
  }
}

// ==================== 参数项相关 ====================
const currentDictionary = ref(null)
const paramItems = ref([])

const handleViewItems = async (row) => {
  currentDictionary.value = row
  try {
    const items = await getParameterItems(row.id)
    paramItems.value = (items || []).map(item => ({ ...item, _editing: false }))
  } catch (error) {
    ElMessage.error('加载参数项失败')
  }
}

const handleAddItem = () => {
  paramItems.value.push({
    id: null,
    dictionaryId: currentDictionary.value.id,
    itemLabel: '',
    itemValue: '',
    sortOrder: paramItems.value.length,
    status: 1,
    _editing: true
  })
}

const handleSaveItem = async (row) => {
  if (!row.itemLabel || !row.itemValue) {
    ElMessage.warning('项标签和项值不能为空')
    return
  }
  try {
    if (row.id) {
      await updateParameterItem(row.id, {
        itemLabel: row.itemLabel,
        itemValue: row.itemValue,
        sortOrder: row.sortOrder,
        status: row.status,
        dictionaryId: row.dictionaryId
      })
      ElMessage.success('更新成功')
    } else {
      await createParameterItem({
        itemLabel: row.itemLabel,
        itemValue: row.itemValue,
        sortOrder: row.sortOrder,
        status: row.status,
        dictionaryId: row.dictionaryId
      })
      ElMessage.success('创建成功')
    }
    handleViewItems(currentDictionary.value)
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const handleCancelItem = (row, index) => {
  if (!row.id) {
    paramItems.value.splice(index, 1)
  } else {
    row._editing = false
  }
}

const handleDeleteItem = async (row, index) => {
  if (!row.id) {
    paramItems.value.splice(index, 1)
    return
  }
  await ElMessageBox.confirm('确定要删除该参数项吗？', '提示', { type: 'warning' })
  try {
    await deleteParameterItem(row.id)
    ElMessage.success('删除成功')
    handleViewItems(currentDictionary.value)
  } catch (error) {
    ElMessage.error('删除失败')
  }
}

const handleBatchSave = async () => {
  const items = paramItems.value.map(item => ({
    itemLabel: item.itemLabel,
    itemValue: item.itemValue,
    sortOrder: item.sortOrder,
    status: item.status
  }))
  if (items.length === 0) {
    ElMessage.warning('暂无参数项可保存')
    return
  }
  try {
    await batchSaveParameterItems(currentDictionary.value.id, items)
    ElMessage.success('批量保存成功')
    handleViewItems(currentDictionary.value)
  } catch (error) {
    ElMessage.error('批量保存失败')
  }
}

// ==================== 初始化 ====================
onMounted(() => {
  loadCategories()
})
</script>

<style scoped lang="scss">
.parameter-container {
  padding: 20px;

  .category-card {
    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
    }

    .category-filter {
      margin-bottom: 12px;
    }

    .tree-node {
      flex: 1;
      display: flex;
      align-items: center;
      justify-content: space-between;
      font-size: 14px;

      .tree-node-actions {
        display: none;
      }

      &:hover .tree-node-actions {
        display: inline-block;
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
    margin-bottom: 20px;

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

  .items-card {
    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
    }
  }
}
</style>
