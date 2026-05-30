<!-- views/system/permission-template/index.vue - 权限模板管理页面 -->
<template>
  <div class="template-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>权限模板管理</span>
          <div>
            <el-button type="primary" @click="handleAdd">新增模板</el-button>
            <el-button @click="loadData">刷新</el-button>
          </div>
        </div>
      </template>

      <!-- 表格 -->
      <el-table :data="tableData" v-loading="loading" border stripe style="width: 100%;">
        <el-table-column prop="templateName" label="模板名称" width="160" />
        <el-table-column prop="templateCode" label="模板编码" width="160" />
        <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip />
        <el-table-column prop="isSystem" label="类型" width="120">
          <template #default="{ row }">
            <el-tag :type="row.isSystem === 1 ? 'warning' : 'success'">
              {{ row.isSystem === 1 ? '系统预设' : '自定义' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="180">
          <template #default="{ row }">
            {{ formatTime(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" fixed="right" width="260">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleDetail(row)">
              详情
            </el-button>
            <el-button
              v-if="row.isSystem !== 1"
              type="warning"
              link
              size="small"
              @click="handleEdit(row)"
            >
              编辑
            </el-button>
            <el-button type="success" link size="small" @click="handleApply(row)">
              应用到角色
            </el-button>
            <el-button
              v-if="row.isSystem !== 1"
              type="danger"
              link
              size="small"
              @click="handleDelete(row)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="loadData"
          @current-change="loadData"
        />
      </div>
    </el-card>

    <!-- 新增/编辑模板对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="600px"
      destroy-on-close
      :close-on-click-modal="false"
    >
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="100px">
        <el-form-item label="模板名称" prop="templateName">
          <el-input v-model="formData.templateName" placeholder="请输入模板名称" />
        </el-form-item>
        <el-form-item label="模板编码" prop="templateCode">
          <el-input v-model="formData.templateCode" placeholder="请输入模板编码（字母和下划线）" />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input
            v-model="formData.description"
            type="textarea"
            :rows="3"
            placeholder="请输入描述"
            maxlength="256"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="formData.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="权限选择">
          <el-tree
            ref="treeRef"
            :data="permissionTree"
            :props="{ label: 'permissionName', children: 'children' }"
            show-checkbox
            node-key="id"
            default-expand-all
            :default-checked-keys="formData.checkedPermissionIds"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 详情对话框 -->
    <el-dialog v-model="detailVisible" title="模板详情" width="500px" destroy-on-close>
      <el-descriptions :column="1" border>
        <el-descriptions-item label="模板名称">{{ detailData.templateName }}</el-descriptions-item>
        <el-descriptions-item label="模板编码">{{ detailData.templateCode }}</el-descriptions-item>
        <el-descriptions-item label="描述">{{ detailData.description || '-' }}</el-descriptions-item>
        <el-descriptions-item label="类型">
          <el-tag :type="detailData.isSystem === 1 ? 'warning' : 'success'">
            {{ detailData.isSystem === 1 ? '系统预设' : '自定义' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="detailData.status === 1 ? 'success' : 'danger'">
            {{ detailData.status === 1 ? '启用' : '禁用' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="权限ID列表">
          {{ parsePermissionIds(detailData.permissionIds) }}
        </el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ formatTime(detailData.createdAt) }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>

    <!-- 应用到角色对话框 -->
    <el-dialog v-model="applyDialogVisible" title="应用到角色" width="400px" destroy-on-close>
      <el-form ref="applyFormRef" :model="applyForm" :rules="applyRules" label-width="80px">
        <el-form-item label="角色ID" prop="roleId">
          <el-input-number v-model="applyForm.roleId" :min="1" placeholder="请输入角色ID" style="width: 100%;" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="applyDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="applyLoading" @click="handleApplySubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getTemplatePage,
  getTemplateDetail,
  createTemplate,
  updateTemplate,
  deleteTemplate,
  applyTemplateToRole,
  getPermissionTree
} from '@/api/permission'

const loading = ref(false)
const submitLoading = ref(false)
const applyLoading = ref(false)
const tableData = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 新增/编辑对话框
const dialogVisible = ref(false)
const dialogTitle = ref('新增模板')
const formRef = ref(null)
const treeRef = ref(null)
const permissionTree = ref([])
const isEdit = ref(false)

const formData = ref({
  id: null,
  templateName: '',
  templateCode: '',
  description: '',
  status: 1,
  checkedPermissionIds: []
})

const formRules = {
  templateName: [{ required: true, message: '请输入模板名称', trigger: 'blur' }],
  templateCode: [
    { required: true, message: '请输入模板编码', trigger: 'blur' },
    { pattern: /^[A-Za-z_]+$/, message: '只能包含字母和下划线', trigger: 'blur' }
  ]
}

// 详情对话框
const detailVisible = ref(false)
const detailData = ref({})

// 应用到角色对话框
const applyDialogVisible = ref(false)
const applyFormRef = ref(null)
const applyForm = ref({ templateId: null, roleId: null })
const applyRules = {
  roleId: [{ required: true, message: '请输入角色ID', trigger: 'blur' }]
}

/** 加载模板列表 */
const loadData = async () => {
  loading.value = true
  try {
    const res = await getTemplatePage({
      page: currentPage.value,
      size: pageSize.value
    })
    tableData.value = res?.records || []
    total.value = res?.total || 0
  } catch (error) {
    ElMessage.error('加载模板列表失败')
  } finally {
    loading.value = false
  }
}

/** 加载权限树 */
const loadPermissionTree = async () => {
  if (permissionTree.value.length > 0) return
  try {
    const res = await getPermissionTree()
    permissionTree.value = res || []
  } catch (error) {
    ElMessage.error('加载权限树失败')
  }
}

/** 新增模板 */
const handleAdd = async () => {
  isEdit.value = false
  dialogTitle.value = '新增模板'
  formData.value = {
    id: null,
    templateName: '',
    templateCode: '',
    description: '',
    status: 1,
    checkedPermissionIds: []
  }
  await loadPermissionTree()
  dialogVisible.value = true
}

/** 编辑模板 */
const handleEdit = async (row) => {
  isEdit.value = true
  dialogTitle.value = '编辑模板'
  await loadPermissionTree()

  // 获取详情以获取 permissionIds
  try {
    const detail = await getTemplateDetail(row.id)
    formData.value = {
      id: row.id,
      templateName: row.templateName,
      templateCode: row.templateCode,
      description: row.description,
      status: row.status,
      checkedPermissionIds: parsePermissionIds(detail?.permissionIds)
    }
  } catch (error) {
    formData.value = {
      id: row.id,
      templateName: row.templateName,
      templateCode: row.templateCode,
      description: row.description,
      status: row.status,
      checkedPermissionIds: []
    }
  }
  dialogVisible.value = true
}

/** 提交表单 */
const handleSubmit = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  submitLoading.value = true
  try {
    // 获取选中的权限ID
    const checkedKeys = treeRef.value ? treeRef.value.getCheckedKeys(false) : []
    const halfCheckedKeys = treeRef.value ? treeRef.value.getHalfCheckedKeys() : []
    const allKeys = [...checkedKeys, ...halfCheckedKeys]

    const submitData = {
      templateName: formData.value.templateName,
      templateCode: formData.value.templateCode,
      description: formData.value.description,
      status: formData.value.status,
      permissionIds: allKeys
    }

    if (isEdit.value) {
      await updateTemplate(formData.value.id, submitData)
      ElMessage.success('更新成功')
    } else {
      await createTemplate(submitData)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    loadData()
  } catch (error) {
    ElMessage.error(error?.message || '操作失败')
  } finally {
    submitLoading.value = false
  }
}

/** 查看详情 */
const handleDetail = async (row) => {
  try {
    const res = await getTemplateDetail(row.id)
    detailData.value = res || {}
    detailVisible.value = true
  } catch (error) {
    ElMessage.error('获取模板详情失败')
  }
}

/** 删除模板 */
const handleDelete = async (row) => {
  await ElMessageBox.confirm('确定要删除该模板吗？', '提示', { type: 'warning' })
  try {
    await deleteTemplate(row.id)
    ElMessage.success('删除成功')
    loadData()
  } catch (error) {
    ElMessage.error(error?.message || '删除失败')
  }
}

/** 打开应用到角色对话框 */
const handleApply = (row) => {
  applyForm.value = { templateId: row.id, roleId: null }
  applyDialogVisible.value = true
}

/** 提交应用到角色 */
const handleApplySubmit = async () => {
  const valid = await applyFormRef.value.validate().catch(() => false)
  if (!valid) return

  applyLoading.value = true
  try {
    await applyTemplateToRole(applyForm.value)
    ElMessage.success('模板已应用到角色')
    applyDialogVisible.value = false
  } catch (error) {
    ElMessage.error(error?.message || '应用到角色失败')
  } finally {
    applyLoading.value = false
  }
}

/** 解析 permissionIds JSON 字符串为数组展示 */
const parsePermissionIds = (json) => {
  if (!json) return '-'
  try {
    const ids = JSON.parse(json)
    if (Array.isArray(ids) && ids.length > 0) {
      return ids.join(', ')
    }
    return '-'
  } catch {
    return json
  }
}

/** 格式化时间 */
const formatTime = (time) => {
  if (!time) return '-'
  if (typeof time === 'string') return time.replace('T', ' ')
  return time
}

onMounted(() => {
  loadData()
})
</script>

<style scoped lang="scss">
.template-container {
  padding: 20px;

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .pagination-wrapper {
    display: flex;
    justify-content: flex-end;
    margin-top: 16px;
  }
}
</style>
