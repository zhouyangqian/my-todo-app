<!-- views/dict/type/index.vue - 字典类型管理页面 -->
<template>
  <div class="dict-type-container">
    <!-- 搜索栏区域 -->
    <el-card class="search-card">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="字典名称">
          <el-input v-model="searchForm.dictName" placeholder="请输入字典名称" clearable />
        </el-form-item>
        <el-form-item label="字典类型">
          <el-input v-model="searchForm.dictCode" placeholder="请输入字典类型" clearable />
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
          <span>字典类型列表</span>
          <el-button v-if="userStore.hasPermission('dict:type:create')" type="primary" @click="handleAdd">
            <el-icon><Plus /></el-icon>
            新增字典
          </el-button>
        </div>
      </template>

      <el-table :data="tableData" v-loading="loading" border stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="dictName" label="字典名称" width="150" />
        <el-table-column prop="dictCode" label="字典类型" width="150" />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" min-width="200" />
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" fixed="right" width="250">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleViewItems(row)">
              字典项
            </el-button>
            <el-button v-if="userStore.hasPermission('dict:type:update')" type="warning" link @click="handleEdit(row)">
              编辑
            </el-button>
            <el-button v-if="userStore.hasPermission('dict:type:delete')" type="danger" link @click="handleDelete(row)">
              删除
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
      width="600px"
      :close-on-click-modal="false"
    >
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="100px">
        <el-form-item label="字典名称" prop="dictName">
          <el-input v-model="formData.dictName" placeholder="请输入字典名称" />
        </el-form-item>
        <el-form-item label="字典类型" prop="dictCode">
          <el-input v-model="formData.dictCode" placeholder="请输入字典类型（英文）" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="formData.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="formData.remark" type="textarea" :rows="3" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitLoading">确定</el-button>
      </template>
    </el-dialog>

    <!-- 字典项对话框 -->
    <el-dialog
      v-model="itemsDialogVisible"
      title="字典项管理"
      width="800px"
    >
      <div class="items-header">
        <el-button v-if="userStore.hasPermission('dict:type:create')" type="primary" size="small" @click="handleAddItem">
          <el-icon><Plus /></el-icon>
          新增字典项
        </el-button>
      </div>
      <el-table :data="dictItems" border stripe>
        <el-table-column prop="label" label="字典标签" width="150" />
        <el-table-column prop="value" label="字典值" width="150" />
        <el-table-column prop="sort" label="排序" width="80" />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" min-width="150" />
        <el-table-column label="操作" width="150">
          <template #default="{ row }">
            <el-button v-if="userStore.hasPermission('dict:type:update')" type="primary" link @click="handleEditItem(row)">编辑</el-button>
            <el-button v-if="userStore.hasPermission('dict:type:delete')" type="danger" link @click="handleDeleteItem(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>

    <!-- 字典项编辑对话框 -->
    <el-dialog
      v-model="itemDialogVisible"
      :title="itemDialogTitle"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form ref="itemFormRef" :model="itemForm" :rules="itemRules" label-width="100px">
        <el-form-item label="字典标签" prop="label">
          <el-input v-model="itemForm.label" placeholder="请输入字典标签" />
        </el-form-item>
        <el-form-item label="字典值" prop="value">
          <el-input v-model="itemForm.value" placeholder="请输入字典值" />
        </el-form-item>
        <el-form-item label="排序" prop="sort">
          <el-input-number v-model="itemForm.sort" :min="0" style="width: 100%;" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="itemForm.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="itemForm.remark" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="itemDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmitItem" :loading="itemSubmitLoading">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Plus } from '@element-plus/icons-vue'
import { getDictTypePage, createDictType, updateDictType, deleteDictType } from '@/api/dict'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()

const searchForm = reactive({
  dictName: '',
  dictCode: ''
})

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

const tableData = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref('新增字典类型')
const formRef = ref()
const submitLoading = ref(false)

const itemsDialogVisible = ref(false)
const itemDialogVisible = ref(false)
const itemDialogTitle = ref('新增字典项')
const itemFormRef = ref()
const itemSubmitLoading = ref(false)
const currentDictType = ref(null)
const dictItems = ref([])

const formData = reactive({
  dictName: '',
  dictCode: '',
  status: 1,
  remark: ''
})

const itemForm = reactive({
  label: '',
  value: '',
  sort: 0,
  status: 1,
  remark: ''
})

const formRules = {
  dictName: [{ required: true, message: '请输入字典名称', trigger: 'blur' }],
  dictCode: [{ required: true, message: '请输入字典类型', trigger: 'blur' }]
}

const itemRules = {
  label: [{ required: true, message: '请输入字典标签', trigger: 'blur' }],
  value: [{ required: true, message: '请输入字典值', trigger: 'blur' }]
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await getDictTypePage({
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

const handleSearch = () => {
  pagination.page = 1
  loadData()
}

const handleReset = () => {
  searchForm.dictName = ''
  searchForm.dictCode = ''
  handleSearch()
}

const handleAdd = () => {
  dialogTitle.value = '新增字典类型'
  Object.assign(formData, {
    dictName: '',
    dictCode: '',
    status: 1,
    remark: ''
  })
  dialogVisible.value = true
}

const handleEdit = (row) => {
  dialogTitle.value = '编辑字典类型'
  Object.assign(formData, row)
  formData.id = String(row.id)  // 确保id是字符串类型
  dialogVisible.value = true
}

const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    submitLoading.value = true
    try {
      if (formData.id) {
        await updateDictType(formData.id, formData)
        ElMessage.success('更新成功')
      } else {
        await createDictType(formData)
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

const handleDelete = async (row) => {
  await ElMessageBox.confirm('确定要删除该字典类型吗?', '提示', { type: 'warning' })
  try {
    await deleteDictType(row.id)
    ElMessage.success('删除成功')
    loadData()
  } catch (error) {
    ElMessage.error('删除失败')
  }
}

const handleViewItems = (row) => {
  currentDictType.value = row
  dictItems.value = row.items || []
  itemsDialogVisible.value = true
}

const handleAddItem = () => {
  itemDialogTitle.value = '新增字典项'
  Object.assign(itemForm, {
    label: '',
    value: '',
    sort: 0,
    status: 1,
    remark: ''
  })
  itemDialogVisible.value = true
}

const handleEditItem = (row) => {
  itemDialogTitle.value = '编辑字典项'
  Object.assign(itemForm, row)
  itemForm.id = String(row.id)  // 确保id是字符串类型
  itemDialogVisible.value = true
}

const handleSubmitItem = async () => {
  if (!itemFormRef.value) return
  await itemFormRef.value.validate(async (valid) => {
    if (!valid) return
    itemSubmitLoading.value = true
    try {
      ElMessage.success(itemForm.id ? '更新成功' : '创建成功')
      itemDialogVisible.value = false
      loadData()
    } catch (error) {
      ElMessage.error('操作失败')
    } finally {
      itemSubmitLoading.value = false
    }
  })
}

const handleDeleteItem = async (row) => {
  await ElMessageBox.confirm('确定要删除该字典项吗?', '提示', { type: 'warning' })
  try {
    ElMessage.success('删除成功')
  } catch (error) {
    ElMessage.error('删除失败')
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
  loadData()
})
</script>

<style scoped lang="scss">
.dict-type-container {
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

  .items-header {
    margin-bottom: 10px;
  }
}
</style>
