<!-- views/system/menu/index.vue - 菜单管理页面
  提供菜单的树形展示、新增、编辑、删除、角色分配等功能
-->
<template>
  <div class="menu-container">
    <!-- 操作栏 -->
    <el-card class="search-card">
      <div class="card-header">
        <span>菜单管理</span>
        <div>
          <el-button type="primary" @click="handleAdd(null)">
            <el-icon><Plus /></el-icon>
            新增目录
          </el-button>
        </div>
      </div>
    </el-card>

    <!-- 菜单树形表格 -->
    <el-card class="table-card">
      <el-table
        :data="menuTree"
        v-loading="loading"
        row-key="id"
        border
        :tree-props="{ children: 'children', hasChildren: 'hasChildren' }"
        default-expand-all
      >
        <el-table-column prop="menuName" label="菜单名称" min-width="180" />
        <el-table-column prop="menuType" label="类型" width="100" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.menuType === 1" type="">目录</el-tag>
            <el-tag v-else-if="row.menuType === 2" type="success">菜单</el-tag>
            <el-tag v-else-if="row.menuType === 3" type="warning">按钮</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="path" label="路由路径" width="160" />
        <el-table-column prop="component" label="组件路径" width="200" />
        <el-table-column prop="permissionCode" label="权限编码" width="180" />
        <el-table-column prop="sortOrder" label="排序" width="80" align="center" />
        <el-table-column prop="visible" label="可见" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.visible === 1 ? 'success' : 'info'">
              {{ row.visible === 1 ? '显示' : '隐藏' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" fixed="right" width="240">
          <template #default="{ row }">
            <el-button v-if="row.menuType !== 3" type="primary" link @click="handleAdd(row)">
              新增子菜单
            </el-button>
            <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
            <el-button type="danger" link @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 新增/编辑菜单对话框 -->
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
        label-width="100px"
      >
        <el-form-item label="上级菜单">
          <el-input :value="parentMenuName" disabled placeholder="无上级菜单" />
        </el-form-item>
        <el-form-item label="菜单类型" prop="menuType">
          <el-radio-group v-model="formData.menuType" @change="handleTypeChange">
            <el-radio :value="1">目录</el-radio>
            <el-radio :value="2">菜单</el-radio>
            <el-radio :value="3">按钮</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="菜单名称" prop="menuName">
          <el-input v-model="formData.menuName" placeholder="请输入菜单名称" />
        </el-form-item>
        <el-form-item v-if="formData.menuType !== 3" label="路由路径" prop="path">
          <el-input v-model="formData.path" placeholder="请输入路由路径，如 /system/user" />
        </el-form-item>
        <el-form-item v-if="formData.menuType === 2" label="组件路径" prop="component">
          <el-input v-model="formData.component" placeholder="请输入组件路径，如 system/user/index" />
        </el-form-item>
        <el-form-item v-if="formData.menuType === 3" label="权限编码" prop="permissionCode">
          <el-input v-model="formData.permissionCode" placeholder="请输入权限编码，如 system:user:create" />
        </el-form-item>
        <el-form-item v-if="formData.menuType !== 3" label="图标">
          <el-input v-model="formData.icon" placeholder="请输入图标名称" />
        </el-form-item>
        <el-form-item label="排序" prop="sortOrder">
          <el-input-number v-model="formData.sortOrder" :min="0" :max="999" />
        </el-form-item>
        <el-form-item v-if="formData.menuType !== 3" label="是否可见">
          <el-radio-group v-model="formData.visible">
            <el-radio :value="1">显示</el-radio>
            <el-radio :value="0">隐藏</el-radio>
          </el-radio-group>
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
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { getMenuTree, createMenu, updateMenu, deleteMenu } from '@/api/permission'

// ===== 数据相关 =====
const menuTree = ref([])
const loading = ref(false)

// ===== 对话框相关 =====
const dialogVisible = ref(false)
const dialogTitle = ref('新增菜单')
const formRef = ref()
const submitLoading = ref(false)
const parentRow = ref(null)

const formData = reactive({
  id: undefined,
  parentId: null,
  menuName: '',
  menuType: 1,
  path: '',
  component: '',
  permissionCode: '',
  icon: '',
  sortOrder: 0,
  visible: 1
})

const formRules = {
  menuName: [{ required: true, message: '请输入菜单名称', trigger: 'blur' }],
  menuType: [{ required: true, message: '请选择菜单类型', trigger: 'change' }]
}

const parentMenuName = computed(() => {
  if (!parentRow.value) return ''
  return parentRow.value.menuName
})

/**
 * 加载菜单树
 */
const loadData = async () => {
  loading.value = true
  try {
    const res = await getMenuTree()
    menuTree.value = res || []
  } catch (error) {
    ElMessage.error('加载菜单树失败')
  } finally {
    loading.value = false
  }
}

/**
 * 菜单类型切换时清空不相关字段
 */
const handleTypeChange = () => {
  if (formData.menuType === 3) {
    formData.path = ''
    formData.component = ''
    formData.icon = ''
    formData.visible = 1
  } else if (formData.menuType === 1) {
    formData.component = ''
    formData.permissionCode = ''
  }
}

/**
 * 新增菜单
 * @param row 父菜单行数据，null表示顶级
 */
const handleAdd = (row) => {
  parentRow.value = row
  dialogTitle.value = row ? '新增子菜单' : '新增菜单'
  formData.id = undefined
  formData.parentId = row ? row.id : null
  formData.menuName = ''
  formData.menuType = row ? (row.menuType === 1 ? 2 : 3) : 1
  formData.path = ''
  formData.component = ''
  formData.permissionCode = ''
  formData.icon = ''
  formData.sortOrder = 0
  formData.visible = 1
  dialogVisible.value = true
}

/**
 * 编辑菜单
 */
const handleEdit = (row) => {
  parentRow.value = null
  dialogTitle.value = '编辑菜单'
  formData.id = row.id
  formData.parentId = row.parentId
  formData.menuName = row.menuName
  formData.menuType = row.menuType
  formData.path = row.path || ''
  formData.component = row.component || ''
  formData.permissionCode = row.permissionCode || ''
  formData.icon = row.icon || ''
  formData.sortOrder = row.sortOrder || 0
  formData.visible = row.visible
  dialogVisible.value = true
}

/**
 * 提交表单
 */
const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    submitLoading.value = true
    try {
      const data = {
        parentId: formData.parentId,
        menuName: formData.menuName,
        menuType: formData.menuType,
        path: formData.path || undefined,
        component: formData.component || undefined,
        permissionCode: formData.permissionCode || undefined,
        icon: formData.icon || undefined,
        sortOrder: formData.sortOrder,
        visible: formData.visible
      }

      if (formData.id) {
        await updateMenu(formData.id, data)
        ElMessage.success('更新成功')
      } else {
        await createMenu(data)
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
 * 删除菜单
 */
const handleDelete = async (row) => {
  await ElMessageBox.confirm(
    `确定要删除菜单「${row.menuName}」吗？`,
    '提示',
    { type: 'warning' }
  )
  try {
    await deleteMenu(row.id)
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
.menu-container {
  padding: 20px;

  .search-card {
    margin-bottom: 20px;

    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
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
