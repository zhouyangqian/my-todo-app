<!-- views/erp/config/index.vue - 系统配置管理页面 -->
<template>
  <div class="config-container">
    <el-card class="search-card">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="配置类型">
          <el-select v-model="searchForm.configType" placeholder="请选择类型" clearable style="width: 150px;">
            <el-option label="审批规则" value="APPROVAL" />
            <el-option label="编号规则" value="NUMBER" />
            <el-option label="业务参数" value="BIZ" />
          </el-select>
        </el-form-item>
        <el-form-item label="配置名称">
          <el-input v-model="searchForm.configName" placeholder="请输入名称" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch"><el-icon><Search /></el-icon>搜索</el-button>
          <el-button @click="handleReset"><el-icon><Refresh /></el-icon>重置</el-button>
          <el-button type="success" @click="handleCreate" v-if="userStore.hasPermission('erp:config:create')"><el-icon><Plus /></el-icon>新增配置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 快捷配置标签页 -->
    <el-card class="quick-config-card">
      <el-tabs v-model="activeQuickTab" @tab-change="handleQuickTabChange">
        <el-tab-pane label="审批规则" name="APPROVAL" />
        <el-tab-pane label="编号规则" name="NUMBER" />
        <el-tab-pane label="业务参数" name="BIZ" />
      </el-tabs>
      <el-form label-width="200px" class="quick-form">
        <template v-if="activeQuickTab === 'APPROVAL'">
          <el-form-item label="采购订单是否需要审核">
            <el-switch v-model="quickConfigs.purchaseOrderApproval" active-value="1" inactive-value="0" />
          </el-form-item>
          <el-form-item label="销售订单是否需要审核">
            <el-switch v-model="quickConfigs.salesOrderApproval" active-value="1" inactive-value="0" />
          </el-form-item>
          <el-form-item label="采购退货是否需要审核">
            <el-switch v-model="quickConfigs.purchaseReturnApproval" active-value="1" inactive-value="0" />
          </el-form-item>
          <el-form-item label="销售退货是否需要审核">
            <el-switch v-model="quickConfigs.salesReturnApproval" active-value="1" inactive-value="0" />
          </el-form-item>
          <el-form-item label="销售出库是否需要审核">
            <el-switch v-model="quickConfigs.salesShipmentApproval" active-value="1" inactive-value="0" />
          </el-form-item>
        </template>
        <template v-if="activeQuickTab === 'NUMBER'">
          <el-form-item label="采购订单编号前缀">
            <el-input v-model="quickConfigs.purchaseOrderPrefix" placeholder="如: PO" />
          </el-form-item>
          <el-form-item label="销售订单编号前缀">
            <el-input v-model="quickConfigs.salesOrderPrefix" placeholder="如: SO" />
          </el-form-item>
          <el-form-item label="采购退货编号前缀">
            <el-input v-model="quickConfigs.purchaseReturnPrefix" placeholder="如: PR" />
          </el-form-item>
          <el-form-item label="销售退货编号前缀">
            <el-input v-model="quickConfigs.salesReturnPrefix" placeholder="如: SR" />
          </el-form-item>
          <el-form-item label="盘点单编号前缀">
            <el-input v-model="quickConfigs.inventoryCheckPrefix" placeholder="如: CK" />
          </el-form-item>
        </template>
        <template v-if="activeQuickTab === 'BIZ'">
          <el-form-item label="默认仓库">
            <el-select v-model="quickConfigs.defaultWarehouseId" placeholder="请选择" clearable style="width: 200px;">
              <el-option v-for="w in warehouseList" :key="w.id" :label="w.warehouseName" :value="String(w.id)" />
            </el-select>
          </el-form-item>
          <el-form-item label="库存预警检查间隔(小时)">
            <el-input-number v-model="quickConfigs.alertCheckInterval" :min="1" :max="24" />
          </el-form-item>
          <el-form-item label="允许负库存出库">
            <el-switch v-model="quickConfigs.allowNegativeStock" active-value="1" inactive-value="0" />
          </el-form-item>
        </template>
        <el-form-item>
          <el-button type="primary" @click="handleQuickSave" :loading="quickLoading">保存配置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 配置列表 -->
    <el-card class="table-card">
      <el-table :data="tableData" v-loading="loading" border stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="configKey" label="配置键" width="200" />
        <el-table-column prop="configName" label="配置名称" width="180" />
        <el-table-column prop="configValue" label="配置值" min-width="200" show-overflow-tooltip />
        <el-table-column prop="configType" label="类型" width="100">
          <template #default="{ row }">
            <el-tag :type="getConfigTypeTag(row.configType)">{{ getConfigTypeText(row.configType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" width="150" show-overflow-tooltip />
        <el-table-column label="操作" fixed="right" width="150">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleEdit(row)" v-if="userStore.hasPermission('erp:config:update')">编辑</el-button>
            <el-button type="danger" link @click="handleDelete(row)" v-if="userStore.hasPermission('erp:config:delete')">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination v-model:current-page="pagination.page" v-model:page-size="pagination.size"
        :page-sizes="[10, 20, 50, 100]" :total="pagination.total"
        layout="total, sizes, prev, pager, next, jumper" @size-change="handleSizeChange"
        @current-change="handlePageChange" class="pagination" />
    </el-card>

    <!-- 新建/编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑配置' : '新增配置'" width="500px" :close-on-click-modal="false">
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="100px">
        <el-form-item label="配置键" prop="configKey">
          <el-input v-model="formData.configKey" placeholder="如: purchaseOrderApproval" :disabled="isEdit" />
        </el-form-item>
        <el-form-item label="配置名称" prop="configName">
          <el-input v-model="formData.configName" placeholder="请输入配置名称" />
        </el-form-item>
        <el-form-item label="配置类型" prop="configType">
          <el-select v-model="formData.configType" placeholder="请选择类型" style="width: 100%;">
            <el-option label="审批规则" value="APPROVAL" />
            <el-option label="编号规则" value="NUMBER" />
            <el-option label="业务参数" value="BIZ" />
          </el-select>
        </el-form-item>
        <el-form-item label="配置值" prop="configValue">
          <el-input v-model="formData.configValue" placeholder="请输入配置值" />
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
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
import { getConfigPage, getConfigsByType, createConfig, updateConfig, deleteConfig, batchUpdateConfigs, getWarehouses } from '@/api/erp'

const searchForm = reactive({ configType: undefined, configName: '' })
const pagination = reactive({ page: 1, size: 10, total: 0 })
const tableData = ref([])
const loading = ref(false)
const warehouseList = ref([])

const activeQuickTab = ref('APPROVAL')
const quickConfigs = reactive({})
const quickLoading = ref(false)

const dialogVisible = ref(false)
const formRef = ref()
const submitLoading = ref(false)
const isEdit = ref(false)
const currentId = ref(null)
const formData = reactive({ configKey: '', configName: '', configType: '', configValue: '', remark: '' })
const formRules = {
  configKey: [{ required: true, message: '请输入配置键', trigger: 'blur' }],
  configName: [{ required: true, message: '请输入配置名称', trigger: 'blur' }],
  configType: [{ required: true, message: '请选择配置类型', trigger: 'change' }],
  configValue: [{ required: true, message: '请输入配置值', trigger: 'blur' }]
}

const getConfigTypeTag = (type) => ({ APPROVAL: 'success', NUMBER: 'primary', BIZ: 'warning' }[type] || 'info')
const getConfigTypeText = (type) => ({ APPROVAL: '审批规则', NUMBER: '编号规则', BIZ: '业务参数' }[type] || '未知')

const loadData = async () => {
  loading.value = true
  try {
    const res = await getConfigPage({ page: pagination.page, size: pagination.size, ...searchForm })
    tableData.value = res.records || []
    pagination.total = res.total || 0
  } catch { ElMessage.error('加载数据失败') } finally { loading.value = false }
}

const loadQuickConfigs = async () => {
  try {
    const configs = await getConfigsByType(activeQuickTab.value)
    Object.keys(quickConfigs).forEach(key => delete quickConfigs[key])
    configs.forEach(c => { quickConfigs[c.configKey] = c.configValue })
  } catch {}
}

const loadWarehouses = async () => {
  try { warehouseList.value = await getWarehouses() || [] } catch {}
}

const handleSearch = () => { pagination.page = 1; loadData() }
const handleReset = () => { searchForm.configType = undefined; searchForm.configName = ''; handleSearch() }

const handleQuickTabChange = () => { loadQuickConfigs() }

const handleQuickSave = async () => {
  quickLoading.value = true
  try {
    await batchUpdateConfigs(activeQuickTab.value, quickConfigs)
    ElMessage.success('保存成功')
    loadData()
  } catch { ElMessage.error('保存失败') } finally { quickLoading.value = false }
}

const handleCreate = () => {
  isEdit.value = false; currentId.value = null
  Object.assign(formData, { configKey: '', configName: '', configType: '', configValue: '', remark: '' })
  dialogVisible.value = true
}

const handleEdit = (row) => {
  isEdit.value = true; currentId.value = row.id
  Object.assign(formData, { configKey: row.configKey, configName: row.configName, configType: row.configType, configValue: row.configValue, remark: row.remark })
  dialogVisible.value = true
}

const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    submitLoading.value = true
    try {
      if (isEdit.value) { await updateConfig(currentId.value, formData); ElMessage.success('更新成功') }
      else { await createConfig(formData); ElMessage.success('创建成功') }
      dialogVisible.value = false; loadData(); loadQuickConfigs()
    } catch { ElMessage.error(isEdit.value ? '更新失败' : '创建失败') } finally { submitLoading.value = false }
  })
}

const handleDelete = async (row) => {
  await ElMessageBox.confirm('确认删除该配置？', '提示', { type: 'warning' })
  try { await deleteConfig(row.id); ElMessage.success('删除成功'); loadData(); loadQuickConfigs() }
  catch (e) { if (e !== 'cancel') ElMessage.error('删除失败') }
}

const handleSizeChange = (size) => { pagination.size = size; loadData() }
const handlePageChange = (page) => { pagination.page = page; loadData() }

onMounted(() => { loadWarehouses(); loadData(); loadQuickConfigs() })
</script>

<style scoped lang="scss">
.config-container {
  padding: 20px;

  .search-card {
    margin-bottom: 20px;
    .search-form { display: flex; flex-wrap: wrap; gap: 10px; }
  }

  .quick-config-card {
    margin-bottom: 20px;
    .quick-form { max-width: 600px; margin-top: 10px; }
  }

  .table-card {
    .pagination { margin-top: 20px; justify-content: flex-end; }
  }
}
</style>
