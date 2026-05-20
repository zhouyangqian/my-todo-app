<!-- views/dict/config/index.vue - 系统配置管理页面 -->
<template>
  <div class="config-container">
    <!-- 搜索栏区域 -->
    <el-card class="search-card">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="配置名称">
          <el-input v-model="searchForm.configName" placeholder="请输入配置名称" clearable />
        </el-form-item>
        <el-form-item label="配置键">
          <el-input v-model="searchForm.configKey" placeholder="请输入配置键" clearable />
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
          <span>系统配置列表</span>
          <el-button v-if="userStore.hasPermission('dict:config:create')" type="primary" @click="handleAdd">
            <el-icon><Plus /></el-icon>
            新增配置
          </el-button>
        </div>
      </template>

      <el-table :data="tableData" v-loading="loading" border stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="configName" label="配置名称" width="150" />
        <el-table-column prop="configKey" label="配置键" width="180" />
        <el-table-column prop="configValue" label="配置值" min-width="200" show-overflow-tooltip />
        <el-table-column prop="configType" label="配置类型" width="100">
          <template #default="{ row }">
            <el-tag>{{ getConfigTypeText(row.configType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="isSystem" label="系统配置" width="100">
          <template #default="{ row }">
            <el-tag :type="row.isSystem === 1 ? 'danger' : 'info'" size="small">
              {{ row.isSystem === 1 ? '是' : '否' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" min-width="150" show-overflow-tooltip />
        <el-table-column label="操作" fixed="right" width="180">
          <template #default="{ row }">
            <el-button v-if="userStore.hasPermission('dict:config:update')" type="primary" link @click="handleEdit(row)" :disabled="row.isSystem === 1">
              编辑
            </el-button>
            <el-button v-if="userStore.hasPermission('dict:config:delete')" type="danger" link @click="handleDelete(row)" :disabled="row.isSystem === 1">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 新增/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="600px"
      :close-on-click-modal="false"
    >
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="100px">
        <el-form-item label="配置名称" prop="configName">
          <el-input v-model="formData.configName" placeholder="请输入配置名称" />
        </el-form-item>
        <el-form-item label="配置键" prop="configKey">
          <el-input v-model="formData.configKey" placeholder="请输入配置键（英文）" />
        </el-form-item>
        <el-form-item label="配置键值" prop="configValue">
          <el-input
            v-if="formData.configType === 'string'"
            v-model="formData.configValue"
            type="textarea"
            :rows="3"
            placeholder="请输入配置值"
          />
          <el-input-number
            v-else-if="formData.configType === 'number'"
            v-model="formData.configValue"
            style="width: 100%;"
          />
          <el-input
            v-else-if="formData.configType === 'boolean'"
            v-model="formData.configValue"
            placeholder="true 或 false"
          />
          <el-input
            v-else
            v-model="formData.configValue"
            type="textarea"
            :rows="5"
            placeholder="请输入 JSON 格式的配置值"
          />
        </el-form-item>
        <el-form-item label="配置类型" prop="configType">
          <el-select v-model="formData.configType" placeholder="请选择配置类型" style="width: 100%;">
            <el-option label="字符串" value="string" />
            <el-option label="数字" value="number" />
            <el-option label="布尔值" value="boolean" />
            <el-option label="JSON" value="json" />
          </el-select>
        </el-form-item>
        <el-form-item label="系统配置" prop="isSystem">
          <el-radio-group v-model="formData.isSystem">
            <el-radio :value="1">是</el-radio>
            <el-radio :value="0">否</el-radio>
          </el-radio-group>
          <div style="color: #909399; font-size: 12px; margin-top: 5px;">
            系统配置不可删除，请谨慎设置
          </div>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="formData.remark" type="textarea" :rows="2" placeholder="请输入备注" />
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
import { getConfigList, createConfig, updateConfig, deleteConfig } from '@/api/dict'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()

const searchForm = reactive({
  configName: '',
  configKey: ''
})

const tableData = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref('新增系统配置')
const formRef = ref()
const submitLoading = ref(false)

const formData = reactive({
  configName: '',
  configKey: '',
  configValue: '',
  configType: 'string',
  isSystem: 0,
  remark: ''
})

const formRules = {
  configName: [{ required: true, message: '请输入配置名称', trigger: 'blur' }],
  configKey: [{ required: true, message: '请输入配置键', trigger: 'blur' }],
  configValue: [{ required: true, message: '请输入配置值', trigger: 'blur' }],
  configType: [{ required: true, message: '请选择配置类型', trigger: 'change' }]
}

const getConfigTypeText = (type) => {
  const texts = { string: '字符串', number: '数字', boolean: '布尔值', json: 'JSON' }
  return texts[type] || type
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await getConfigList(searchForm)
    tableData.value = res || []
  } catch (error) {
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  loadData()
}

const handleReset = () => {
  searchForm.configName = ''
  searchForm.configKey = ''
  handleSearch()
}

const handleAdd = () => {
  dialogTitle.value = '新增系统配置'
  Object.assign(formData, {
    configName: '',
    configKey: '',
    configValue: '',
    configType: 'string',
    isSystem: 0,
    remark: ''
  })
  dialogVisible.value = true
}

const handleEdit = (row) => {
  dialogTitle.value = '编辑系统配置'
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
        await updateConfig(formData.id, formData)
        ElMessage.success('更新成功')
      } else {
        await createConfig(formData)
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
  if (row.isSystem === 1) {
    ElMessage.warning('系统配置不能删除')
    return
  }
  await ElMessageBox.confirm('确定要删除该配置吗?', '提示', { type: 'warning' })
  try {
    await deleteConfig(row.id)
    ElMessage.success('删除成功')
    loadData()
  } catch (error) {
    ElMessage.error('删除失败')
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped lang="scss">
.config-container {
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
  }
}
</style>
