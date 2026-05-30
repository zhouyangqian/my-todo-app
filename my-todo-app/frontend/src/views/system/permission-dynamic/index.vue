<!-- views/system/permission-dynamic/index.vue - 动态权限配置页面 -->
<template>
  <div class="permission-dynamic-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>动态权限配置</span>
          <div>
            <el-button @click="handleClearCache" :loading="cacheLoading">
              清除缓存
            </el-button>
            <el-button type="primary" @click="handleAdd(null)">
              <el-icon><Plus /></el-icon>
              新增权限
            </el-button>
          </div>
        </div>
      </template>

      <el-tree
        ref="treeRef"
        :data="permissionTree"
        node-key="id"
        :props="treeProps"
        default-expand-all
        :expand-on-click-node="false"
        v-loading="loading"
      >
        <template #default="{ node, data }">
          <div class="tree-node">
            <span class="node-label">
              <el-tag size="small" type="info" class="code-tag">{{ data.code }}</el-tag>
              <span class="node-name">{{ data.name }}</span>
              <span v-if="data.description" class="node-desc">{{ data.description }}</span>
            </span>
            <span class="node-actions">
              <el-button type="primary" link size="small" @click.stop="handleAdd(data)">
                添加子权限
              </el-button>
              <el-button type="warning" link size="small" @click.stop="handleEdit(data)">
                编辑
              </el-button>
              <el-button type="danger" link size="small" @click.stop="handleDelete(data)">
                删除
              </el-button>
            </span>
          </div>
        </template>
      </el-tree>
    </el-card>

    <!-- 新增/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="100px">
        <el-form-item label="上级权限">
          <el-tree-select
            v-model="formData.parentId"
            :data="permissionTree"
            :props="{ label: 'name', value: 'id', children: 'children' }"
            placeholder="选择上级权限（不选则为顶级）"
            clearable
            check-strictly
            style="width: 100%;"
          />
        </el-form-item>
        <el-form-item label="权限编码" prop="code">
          <el-input v-model="formData.code" placeholder="如 system:user:create" :disabled="isEdit" />
        </el-form-item>
        <el-form-item label="权限名称" prop="name">
          <el-input v-model="formData.name" placeholder="请输入权限名称" />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="formData.description" type="textarea" :rows="3" placeholder="请输入权限描述" />
        </el-form-item>
        <el-form-item label="权限类型" prop="permissionType">
          <el-radio-group v-model="formData.permissionType">
            <el-radio :value="1">菜单</el-radio>
            <el-radio :value="2">按钮</el-radio>
            <el-radio :value="3">API</el-radio>
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
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import {
  getDynamicPermissionTree,
  createDynamicPermission,
  updateDynamicPermission,
  deleteDynamicPermission,
  clearPermissionCache
} from '@/api/permission'

const treeRef = ref()
const loading = ref(false)
const cacheLoading = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref('新增权限')
const isEdit = ref(false)
const formRef = ref()
const submitLoading = ref(false)
const permissionTree = ref([])

const treeProps = {
  label: 'name',
  children: 'children'
}

const formData = reactive({
  id: null,
  parentId: null,
  code: '',
  name: '',
  description: '',
  permissionType: 2
})

const formRules = {
  code: [{ required: true, message: '请输入权限编码', trigger: 'blur' }],
  name: [{ required: true, message: '请输入权限名称', trigger: 'blur' }]
}

/** 加载权限树数据 */
const loadData = async () => {
  loading.value = true
  try {
    const res = await getDynamicPermissionTree()
    permissionTree.value = res || []
  } catch (error) {
    ElMessage.error('加载权限树失败')
  } finally {
    loading.value = false
  }
}

/** 新增权限 */
const handleAdd = (parentNode) => {
  isEdit.value = false
  dialogTitle.value = '新增权限'
  Object.assign(formData, {
    id: null,
    parentId: parentNode?.id || null,
    code: '',
    name: '',
    description: '',
    permissionType: 2
  })
  dialogVisible.value = true
}

/** 编辑权限 */
const handleEdit = (row) => {
  isEdit.value = true
  dialogTitle.value = '编辑权限'
  Object.assign(formData, {
    id: row.id,
    parentId: row.parentId,
    code: row.code,
    name: row.name,
    description: row.description
  })
  dialogVisible.value = true
}

/** 提交表单 */
const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    submitLoading.value = true
    try {
      if (isEdit.value) {
        await updateDynamicPermission(formData.id, {
          name: formData.name,
          description: formData.description
        })
        ElMessage.success('更新成功')
      } else {
        await createDynamicPermission({
          code: formData.code,
          name: formData.name,
          description: formData.description,
          parentId: formData.parentId,
          permissionType: formData.permissionType
        })
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

/** 删除权限 */
const handleDelete = async (row) => {
  if (row.children && row.children.length > 0) {
    ElMessage.warning('该权限下有子权限，不能删除')
    return
  }
  await ElMessageBox.confirm('确定要删除该权限吗？', '提示', { type: 'warning' })
  try {
    await deleteDynamicPermission(row.id)
    ElMessage.success('删除成功')
    loadData()
  } catch (error) {
    ElMessage.error('删除失败')
  }
}

/** 清除缓存 */
const handleClearCache = async () => {
  cacheLoading.value = true
  try {
    await clearPermissionCache()
    ElMessage.success('缓存已清除')
    loadData()
  } catch (error) {
    ElMessage.error('清除缓存失败')
  } finally {
    cacheLoading.value = false
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped lang="scss">
.permission-dynamic-container {
  padding: 20px;

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .tree-node {
    display: flex;
    justify-content: space-between;
    align-items: center;
    flex: 1;
    padding-right: 8px;

    .node-label {
      display: flex;
      align-items: center;
      gap: 8px;

      .code-tag {
        font-size: 12px;
      }

      .node-name {
        font-weight: 500;
      }

      .node-desc {
        color: #999;
        font-size: 12px;
      }
    }

    .node-actions {
      display: none;
    }
  }

  .el-tree-node:hover .node-actions {
    display: inline-flex;
  }
}
</style>
