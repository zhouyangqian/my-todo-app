<!-- views/system/data-rule/index.vue - 数据权限规则管理页面
  提供数据权限规则的按角色管理功能：
  - 角色下拉框选择
  - 规则列表表格（规则名、范围类型、目标表、部门字段、用户字段、操作）
  - 新增/编辑对话框
-->
<template>
  <div class="data-rule-container">
    <!-- 角色选择区域 -->
    <el-card class="search-card">
      <el-form :inline="true" class="search-form">
        <el-form-item label="选择角色">
          <el-select
            v-model="selectedRoleId"
            placeholder="请选择角色"
            clearable
            @change="handleRoleChange"
            style="width: 240px"
          >
            <el-option
              v-for="role in roleList"
              :key="role.id"
              :label="role.roleName"
              :value="role.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleAdd" :disabled="!selectedRoleId">
            <el-icon><Plus /></el-icon>
            新增规则
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 规则列表 -->
    <el-card class="table-card">
      <template #header>
        <span>数据权限规则列表</span>
      </template>

      <el-table :data="ruleList" v-loading="loading" border stripe>
        <el-table-column prop="ruleName" label="规则名称" width="160" />
        <el-table-column prop="scopeType" label="范围类型" width="140">
          <template #default="{ row }">
            <el-tag :type="scopeTagType(row.scopeType)">
              {{ scopeLabel(row.scopeType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="tableName" label="目标表名" width="180">
          <template #default="{ row }">
            {{ row.tableName || '所有表' }}
          </template>
        </el-table-column>
        <el-table-column prop="deptColumn" label="部门字段" width="120" />
        <el-table-column prop="userColumn" label="用户字段" width="120" />
        <el-table-column prop="sortOrder" label="排序" width="80" />
        <el-table-column label="操作" fixed="right" width="150">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
            <el-button type="danger" link @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 新增/编辑规则对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="520px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="90px"
      >
        <el-form-item label="规则名称" prop="ruleName">
          <el-input v-model="formData.ruleName" placeholder="请输入规则名称" />
        </el-form-item>
        <el-form-item label="范围类型" prop="scopeType">
          <el-select v-model="formData.scopeType" placeholder="请选择范围类型" style="width: 100%">
            <el-option label="全部数据" value="ALL" />
            <el-option label="本部门数据" value="DEPT" />
            <el-option label="本部门及子部门" value="DEPT_AND_SUB" />
            <el-option label="仅本人数据" value="SELF" />
            <el-option label="按项目过滤" value="PROJECT" />
          </el-select>
        </el-form-item>
        <el-form-item label="目标表名" prop="tableName">
          <el-input v-model="formData.tableName" placeholder="留空则适用于所有表" />
        </el-form-item>
        <el-form-item label="部门字段" prop="deptColumn">
          <el-input v-model="formData.deptColumn" placeholder="默认 dept_id" />
        </el-form-item>
        <el-form-item label="用户字段" prop="userColumn">
          <el-input v-model="formData.userColumn" placeholder="默认 created_by" />
        </el-form-item>
        <el-form-item label="排序" prop="sortOrder">
          <el-input-number v-model="formData.sortOrder" :min="0" :max="999" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitLoading">
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import {
  getRoleList,
  getDataRulesByRole,
  createDataRule,
  updateDataRule,
  deleteDataRule
} from '@/api/permission'

// ===== 角色选择 =====
const selectedRoleId = ref()
const roleList = ref([])

// ===== 规则列表 =====
const ruleList = ref([])
const loading = ref(false)

// ===== 对话框 =====
const dialogVisible = ref(false)
const dialogTitle = ref('新增规则')
const formRef = ref()
const submitLoading = ref(false)

const formData = reactive({
  id: undefined,
  ruleName: '',
  scopeType: 'DEPT',
  tableName: '',
  deptColumn: 'dept_id',
  userColumn: 'created_by',
  sortOrder: 0
})

const formRules = {
  ruleName: [
    { required: true, message: '请输入规则名称', trigger: 'blur' }
  ],
  scopeType: [
    { required: true, message: '请选择范围类型', trigger: 'change' }
  ]
}

/** 范围类型标签映射 */
const scopeLabel = (type) => {
  const map = {
    ALL: '全部数据',
    DEPT: '本部门',
    DEPT_AND_SUB: '本部门及子部门',
    SELF: '仅本人',
    PROJECT: '按项目'
  }
  return map[type] || type
}

/** 范围类型标签颜色映射 */
const scopeTagType = (type) => {
  const map = {
    ALL: '',
    DEPT: 'primary',
    DEPT_AND_SUB: 'warning',
    SELF: 'info',
    PROJECT: 'success'
  }
  return map[type] || ''
}

/**
 * 加载角色列表
 */
const loadRoleList = async () => {
  try {
    const res = await getRoleList({ page: 1, size: 200 })
    roleList.value = res.records || []
  } catch (error) {
    ElMessage.error('加载角色列表失败')
  }
}

/**
 * 加载指定角色的数据权限规则
 */
const loadRules = async () => {
  if (!selectedRoleId.value) {
    ruleList.value = []
    return
  }
  loading.value = true
  try {
    const res = await getDataRulesByRole(selectedRoleId.value)
    ruleList.value = res || []
  } catch (error) {
    ElMessage.error('加载规则失败')
  } finally {
    loading.value = false
  }
}

/**
 * 角色选择变化
 */
const handleRoleChange = () => {
  loadRules()
}

/**
 * 新增规则
 */
const handleAdd = () => {
  dialogTitle.value = '新增规则'
  formData.id = undefined
  formData.ruleName = ''
  formData.scopeType = 'DEPT'
  formData.tableName = ''
  formData.deptColumn = 'dept_id'
  formData.userColumn = 'created_by'
  formData.sortOrder = 0
  dialogVisible.value = true
}

/**
 * 编辑规则
 */
const handleEdit = (row) => {
  dialogTitle.value = '编辑规则'
  formData.id = row.id
  formData.ruleName = row.ruleName
  formData.scopeType = row.scopeType
  formData.tableName = row.tableName
  formData.deptColumn = row.deptColumn
  formData.userColumn = row.userColumn
  formData.sortOrder = row.sortOrder
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
      const submitData = {
        roleId: selectedRoleId.value,
        ruleName: formData.ruleName,
        scopeType: formData.scopeType,
        tableName: formData.tableName || null,
        deptColumn: formData.deptColumn || 'dept_id',
        userColumn: formData.userColumn || 'created_by',
        sortOrder: formData.sortOrder || 0
      }
      if (formData.id) {
        await updateDataRule(formData.id, submitData)
        ElMessage.success('更新成功')
      } else {
        await createDataRule(submitData)
        ElMessage.success('创建成功')
      }
      dialogVisible.value = false
      loadRules()
    } catch (error) {
      ElMessage.error('操作失败')
    } finally {
      submitLoading.value = false
    }
  })
}

/**
 * 删除规则
 */
const handleDelete = async (row) => {
  await ElMessageBox.confirm('确定要删除该规则吗？', '提示', {
    type: 'warning'
  })
  try {
    await deleteDataRule(row.id)
    ElMessage.success('删除成功')
    loadRules()
  } catch (error) {
    ElMessage.error('删除失败')
  }
}

onMounted(() => {
  loadRoleList()
})
</script>

<style scoped lang="scss">
.data-rule-container {
  padding: 20px;

  .search-card {
    margin-bottom: 20px;

    .search-form {
      display: flex;
      flex-wrap: wrap;
      gap: 10px;
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
