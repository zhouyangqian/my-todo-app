<!-- views/system/role-manage/index.vue - 角色管理页面（基于 user-service）
  提供角色的搜索、新增、编辑、删除、分配权限等功能
  包含搜索栏、数据表格、新增/编辑对话框、权限分配对话框
-->
<template>
  <div class="role-container">
    <!-- 搜索栏区域：支持按角色名称筛选 -->
    <el-card class="search-card">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="角色名称">
          <el-input v-model="searchForm.roleName" placeholder="请输入角色名称" clearable />
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

    <!-- 角色数据表格区域 -->
    <el-card class="table-card">
      <template #header>
        <div class="card-header">
          <span>角色列表</span>
          <el-button type="primary" @click="handleAdd">
            <el-icon><Plus /></el-icon>
            新增角色
          </el-button>
        </div>
      </template>

      <el-table :data="tableData" v-loading="loading" border stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="roleName" label="角色名称" width="150" />
        <el-table-column prop="roleCode" label="角色编码" width="150" />
        <el-table-column prop="description" label="描述" min-width="200" />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="180" />
        <el-table-column label="操作" fixed="right" width="250">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
            <el-button type="warning" link @click="handleAssignPermission(row)">
              分配权限
            </el-button>
            <el-button type="danger" link @click="handleDelete(row)">删除</el-button>
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

    <!-- 新增/编辑角色对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="80px"
      >
        <el-form-item label="角色名称" prop="roleName">
          <el-input v-model="formData.roleName" placeholder="请输入角色名称" />
        </el-form-item>
        <el-form-item label="角色编码" prop="roleCode">
          <el-input v-model="formData.roleCode" placeholder="请输入角色编码（字母和下划线）" />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input
            v-model="formData.description"
            type="textarea"
            :rows="3"
            placeholder="请输入描述"
          />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="formData.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitLoading">
          确定
        </el-button>
      </template>
    </el-dialog>

    <!-- 分配权限对话框：使用树形组件展示权限层级，支持勾选 -->
    <el-dialog
      v-model="permissionDialogVisible"
      title="分配权限"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-tree
        ref="treeRef"
        :data="permissionTree"
        :props="{ label: 'permissionName', children: 'children' }"
        show-checkbox
        node-key="id"
        default-expand-all
        :default-checked-keys="checkedPermissionIds"
      />
      <template #footer>
        <el-button @click="permissionDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSavePermission" :loading="permissionLoading">
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
// 角色管理页面逻辑：调用 user-service 提供的角色 API
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Plus } from '@element-plus/icons-vue'
import {
  getRolePage,
  createRole,
  updateRole,
  deleteRole
} from '@/api/user'
import {
  getPermissionTree,
  getRolePermissions,
  assignRolePermissions
} from '@/api/permission'

// ===== 搜索相关 =====
const searchForm = reactive({
  roleName: ''
})

// ===== 分页相关 =====
const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

// ===== 表格相关 =====
const tableData = ref([])
const loading = ref(false)

// ===== 对话框相关 =====
const dialogVisible = ref(false)
const dialogTitle = ref('新增角色')
const formRef = ref()
const submitLoading = ref(false)

const formData = reactive({
  id: undefined,
  roleName: '',
  roleCode: '',
  description: '',
  status: 1
})

const formRules = {
  roleName: [
    { required: true, message: '请输入角色名称', trigger: 'blur' }
  ],
  roleCode: [
    { required: true, message: '请输入角色编码', trigger: 'blur' },
    { pattern: /^[a-zA-Z_]+$/, message: '只能包含字母和下划线', trigger: 'blur' }
  ]
}

// ===== 权限分配相关 =====
const permissionDialogVisible = ref(false)
const permissionTree = ref([])
const checkedPermissionIds = ref([])
const currentRoleId = ref()
const treeRef = ref()
const permissionLoading = ref(false)

/**
 * 加载角色列表数据
 */
const loadData = async () => {
  loading.value = true
  try {
    const res = await getRolePage({
      page: pagination.page,
      size: pagination.size,
      roleName: searchForm.roleName || undefined
    })
    tableData.value = res.records || []
    pagination.total = res.total || 0
  } catch (error) {
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

/**
 * 加载完整的权限树数据
 */
const loadPermissionTree = async () => {
  try {
    const res = await getPermissionTree()
    permissionTree.value = res
  } catch (error) {
    ElMessage.error('加载权限树失败')
  }
}

/**
 * 搜索
 */
const handleSearch = () => {
  pagination.page = 1
  loadData()
}

/**
 * 重置搜索条件
 */
const handleReset = () => {
  searchForm.roleName = ''
  handleSearch()
}

/**
 * 新增角色
 */
const handleAdd = () => {
  dialogTitle.value = '新增角色'
  formData.id = undefined
  formData.roleName = ''
  formData.roleCode = ''
  formData.description = ''
  formData.status = 1
  dialogVisible.value = true
}

/**
 * 编辑角色
 * @param row 当前行数据
 */
const handleEdit = (row) => {
  dialogTitle.value = '编辑角色'
  formData.id = row.id
  formData.roleName = row.roleName
  formData.roleCode = row.roleCode
  formData.description = row.description
  formData.status = row.status
  dialogVisible.value = true
}

/**
 * 表单提交
 */
const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    submitLoading.value = true
    try {
      const submitData = {
        roleName: formData.roleName,
        roleCode: formData.roleCode,
        description: formData.description,
        status: formData.status
      }

      if (formData.id) {
        await updateRole(formData.id, submitData)
        ElMessage.success('更新成功')
      } else {
        await createRole(submitData)
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

/**
 * 删除角色
 * @param row 当前行数据
 */
const handleDelete = async (row) => {
  await ElMessageBox.confirm('确定要删除该角色吗？', '提示', {
    type: 'warning'
  })
  try {
    await deleteRole(row.id)
    ElMessage.success('删除成功')
    loadData()
  } catch (error) {
    ElMessage.error('删除失败')
  }
}

/**
 * 从权限树中收集所有非叶子节点的 ID（即有 children 的节点）
 * el-tree 的 setCheckedKeys 只能传叶子节点 ID，传父节点 ID 会导致所有子节点被勾选
 * @param nodes 权限树节点列表
 * @returns 非叶子节点 ID 集合
 */
const collectNonLeafIds = (nodes) => {
  const ids = new Set()
  const traverse = (list) => {
    for (const node of list) {
      if (node.children && node.children.length > 0) {
        ids.add(node.id)
        traverse(node.children)
      }
    }
  }
  traverse(nodes)
  return ids
}

/**
 * 打开权限分配对话框
 * @param row 当前行角色数据
 */
const handleAssignPermission = async (row) => {
  currentRoleId.value = row.id
  if (permissionTree.value.length === 0) {
    await loadPermissionTree()
  }
  permissionDialogVisible.value = true
  // 获取该角色已拥有的权限ID（仅自身直接分配的权限，不含继承）
  let permissionIds = []
  try {
    permissionIds = await getRolePermissions(row.id)
  } catch (error) {
    permissionIds = []
  }
  // 过滤掉父节点 ID，只保留叶子节点，避免 el-tree 自动勾选所有子节点
  const nonLeafIds = collectNonLeafIds(permissionTree.value)
  const leafIds = permissionIds.filter(id => !nonLeafIds.has(id))
  checkedPermissionIds.value = leafIds
  setTimeout(() => {
    if (treeRef.value) {
      treeRef.value.setCheckedKeys(leafIds)
    }
  }, 100)
}

/**
 * 保存权限分配
 */
const handleSavePermission = async () => {
  if (!currentRoleId.value || !treeRef.value) return
  permissionLoading.value = true
  try {
    const checkedKeys = treeRef.value.getCheckedKeys(false)
    const halfCheckedKeys = treeRef.value.getHalfCheckedKeys()
    const allKeys = [...checkedKeys, ...halfCheckedKeys]
    await assignRolePermissions(currentRoleId.value, allKeys)
    ElMessage.success('保存成功')
    permissionDialogVisible.value = false
  } catch (error) {
    ElMessage.error('保存失败')
  } finally {
    permissionLoading.value = false
  }
}

/**
 * 每页条数变化
 */
const handleSizeChange = (size) => {
  pagination.size = size
  loadData()
}

/**
 * 页码变化
 */
const handlePageChange = (page) => {
  pagination.page = page
  loadData()
}

onMounted(() => {
  loadData()
})
</script>

<style scoped lang="scss">
.role-container {
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
