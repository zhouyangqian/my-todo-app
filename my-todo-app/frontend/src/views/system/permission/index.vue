<!-- views/system/permission/index.vue - 权限管理页面 -->
<template>
  <div class="permission-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>权限管理</span>
          <el-button v-if="userStore.hasPermission('system:permission:create')" type="primary" @click="handleAdd(null)">
            <el-icon><Plus /></el-icon>
            新增权限
          </el-button>
        </div>
      </template>

      <el-table
        :data="permissionTree"
        row-key="id"
        :tree-props="{ children: 'children', hasChildren: 'hasChildren' }"
        border
        stripe
        v-loading="loading"
        default-expand-all
      >
        <el-table-column prop="permissionName" label="权限名称" width="200" />
        <el-table-column prop="permissionCode" label="权限编码" width="200" />
        <el-table-column prop="permissionType" label="权限类型" width="100">
          <template #default="{ row }">
            <el-tag :type="getTypeColor(row.permissionType)">
              {{ getTypeText(row.permissionType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="resourcePath" label="路由/接口" min-width="200" />
        <el-table-column prop="icon" label="图标" width="100" />
        <el-table-column prop="sort" label="排序" width="80" />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" fixed="right" width="220">
          <template #default="{ row }">
            <el-button v-if="userStore.hasPermission('system:permission:create')" type="primary" link @click="handleAdd(row)">
              添加子权限
            </el-button>
            <el-button v-if="userStore.hasPermission('system:permission:update')" type="warning" link @click="handleEdit(row)">
              编辑
            </el-button>
            <el-button v-if="userStore.hasPermission('system:permission:delete')" type="danger" link @click="handleDelete(row)">
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
        <el-form-item label="上级权限" prop="parentId">
          <el-tree-select
            v-model="formData.parentId"
            :data="permissionTree"
            :props="{ label: 'name', value: 'id' }"
            placeholder="选择上级权限（不选则为顶级）"
            clearable
            check-strictly
            style="width: 100%;"
          />
        </el-form-item>
        <el-form-item label="权限类型" prop="type">
          <el-radio-group v-model="formData.type">
            <el-radio :value="1">菜单</el-radio>
            <el-radio :value="2">按钮</el-radio>
            <el-radio :value="3">接口</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="权限名称" prop="name">
          <el-input v-model="formData.name" placeholder="请输入权限名称" />
        </el-form-item>
        <el-form-item label="权限编码" prop="code">
          <el-input v-model="formData.code" placeholder="如：system:user:add" />
        </el-form-item>
        <el-form-item label="路由/接口" prop="path">
          <el-input v-model="formData.path" placeholder="请输入路由路径或接口地址" />
        </el-form-item>
        <el-form-item label="图标" prop="icon">
          <el-input v-model="formData.icon" placeholder="Element Plus 图标名称" />
        </el-form-item>
        <el-form-item label="排序" prop="sort">
          <el-input-number v-model="formData.sort" :min="0" style="width: 100%;" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="formData.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
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
import { useUserStore } from '@/stores/user'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { getPermissionTree, createPermission, updatePermission, deletePermission } from '@/api/permission'

const userStore = useUserStore()

const permissionTree = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref('新增权限')
const formRef = ref()
const submitLoading = ref(false)

const formData = reactive({
  parentId: null,
  type: 1,
  name: '',
  code: '',
  path: '',
  icon: '',
  sort: 0,
  status: 1,
  remark: ''
})

const formRules = {
  name: [{ required: true, message: '请输入权限名称', trigger: 'blur' }],
  code: [{ required: true, message: '请输入权限编码', trigger: 'blur' }],
  type: [{ required: true, message: '请选择权限类型', trigger: 'change' }]
}

const getTypeText = (type) => {
  const texts = { 1: '菜单', 2: '按钮', 3: '接口' }
  return texts[type] || '未知'
}

const getTypeColor = (type) => {
  const colors = { 1: 'primary', 2: 'success', 3: 'warning' }
  return colors[type] || 'info'
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await getPermissionTree()
    permissionTree.value = res || []
  } catch (error) {
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

const handleAdd = (row) => {
  dialogTitle.value = '新增权限'
  Object.assign(formData, {
    parentId: row?.id || null,
    type: 1,
    name: '',
    code: '',
    path: '',
    icon: '',
    sort: 0,
    status: 1,
    remark: ''
  })
  dialogVisible.value = true
}

const handleEdit = (row) => {
  dialogTitle.value = '编辑权限'
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
      // 字段映射：前端字段 -> 后端字段
      const submitData = {
        permissionCode: formData.code,           // code -> permissionCode
        permissionName: formData.name,           // name -> permissionName
        permissionType: formData.type,           // type -> permissionType
        resourcePath: formData.path,             // path -> resourcePath
        menuPath: formData.menuPath || '',
        component: formData.component || '',
        httpMethod: formData.method || '',
        icon: formData.icon,
        sort: formData.sort,
        status: formData.status,
        description: formData.remark             // remark -> description
      }

      if (formData.id) {
        await updatePermission(formData.id, submitData)
        ElMessage.success('更新成功')
      } else {
        await createPermission(submitData)
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
  if (row.children && row.children.length > 0) {
    ElMessage.warning('该权限下有子权限，不能删除')
    return
  }
  await ElMessageBox.confirm('确定要删除该权限吗?', '提示', { type: 'warning' })
  try {
    await deletePermission(row.id)
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
.permission-container {
  padding: 20px;

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }
}
</style>
