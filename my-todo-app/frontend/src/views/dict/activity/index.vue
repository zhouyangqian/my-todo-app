<!-- views/dict/activity/index.vue - 营销活动管理页面 -->
<template>
  <div class="activity-container">
    <!-- 搜索区域 -->
    <el-card class="search-card">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="活动名称">
          <el-input v-model="searchForm.activityName" placeholder="请输入活动名称" clearable />
        </el-form-item>
        <el-form-item label="活动状态">
          <el-select v-model="searchForm.status" placeholder="请选择" clearable style="width: 120px">
            <el-option label="草稿" value="DRAFT" />
            <el-option label="进行中" value="ACTIVE" />
            <el-option label="已结束" value="ENDED" />
          </el-select>
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

    <!-- 活动列表 -->
    <el-card class="table-card">
      <template #header>
        <div class="card-header">
          <span>营销活动列表</span>
          <el-button type="primary" @click="handleAdd">
            <el-icon><Plus /></el-icon>
            新增活动
          </el-button>
        </div>
      </template>

      <el-table :data="tableData" v-loading="loading" border stripe>
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="activityName" label="活动名称" width="160" />
        <el-table-column prop="activityType" label="活动类型" width="110">
          <template #default="{ row }">
            <el-tag size="small">{{ row.activityType }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="startTime" label="开始时间" width="160" />
        <el-table-column prop="endTime" label="结束时间" width="160" />
        <el-table-column prop="discount" label="折扣/优惠" width="100" />
        <el-table-column prop="status" label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.status === 'ACTIVE' ? 'success' : row.status === 'DRAFT' ? 'info' : 'warning'" size="small">
              {{ row.status === 'ACTIVE' ? '进行中' : row.status === 'DRAFT' ? '草稿' : '已结束' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="描述" min-width="120" show-overflow-tooltip />
        <el-table-column label="操作" fixed="right" width="250">
          <template #default="{ row }">
            <el-button
              v-if="row.status === 'DRAFT'"
              type="success"
              link
              @click="handleActivate(row)"
            >
              上架
            </el-button>
            <el-button type="warning" link @click="handleEdit(row)">编辑</el-button>
            <el-button type="danger" link @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

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
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="活动名称" prop="activityName">
          <el-input v-model="form.activityName" placeholder="请输入活动名称" />
        </el-form-item>
        <el-form-item label="活动类型" prop="activityType">
          <el-select v-model="form.activityType" style="width: 100%">
            <el-option label="满减" value="FULL_REDUCTION" />
            <el-option label="折扣" value="DISCOUNT" />
            <el-option label="赠品" value="GIFT" />
            <el-option label="优惠券" value="COUPON" />
          </el-select>
        </el-form-item>
        <el-form-item label="开始时间" prop="startTime">
          <el-date-picker
            v-model="form.startTime"
            type="datetime"
            placeholder="请选择开始时间"
            style="width: 100%"
            value-format="YYYY-MM-DD HH:mm:ss"
          />
        </el-form-item>
        <el-form-item label="结束时间" prop="endTime">
          <el-date-picker
            v-model="form.endTime"
            type="datetime"
            placeholder="请选择结束时间"
            style="width: 100%"
            value-format="YYYY-MM-DD HH:mm:ss"
          />
        </el-form-item>
        <el-form-item label="折扣/优惠" prop="discount">
          <el-input v-model="form.discount" placeholder="如: 满100减20 / 8折" />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="请输入活动描述" />
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
import {
  getActivities,
  createActivity,
  updateActivity,
  deleteActivity,
  activateActivity
} from '@/api/dict'

// ==================== 列表相关 ====================
const searchForm = reactive({ activityName: '', status: '' })
const pagination = reactive({ page: 1, size: 10, total: 0 })
const tableData = ref([])
const loading = ref(false)

const loadList = async () => {
  loading.value = true
  try {
    const res = await getActivities({
      page: pagination.page,
      size: pagination.size,
      ...searchForm
    })
    tableData.value = res.records || []
    pagination.total = res.total || 0
  } catch (error) {
    ElMessage.error('加载活动列表失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.page = 1
  loadList()
}

const handleReset = () => {
  searchForm.activityName = ''
  searchForm.status = ''
  handleSearch()
}

const handleSizeChange = (size) => {
  pagination.size = size
  loadList()
}

const handlePageChange = (page) => {
  pagination.page = page
  loadList()
}

// ==================== 新增/编辑对话框 ====================
const dialogVisible = ref(false)
const dialogTitle = ref('新增活动')
const formRef = ref()
const submitLoading = ref(false)

const form = reactive({
  id: null,
  activityName: '',
  activityType: 'FULL_REDUCTION',
  startTime: '',
  endTime: '',
  discount: '',
  description: ''
})

const rules = {
  activityName: [{ required: true, message: '请输入活动名称', trigger: 'blur' }],
  activityType: [{ required: true, message: '请选择活动类型', trigger: 'change' }],
  startTime: [{ required: true, message: '请选择开始时间', trigger: 'change' }],
  endTime: [{ required: true, message: '请选择结束时间', trigger: 'change' }]
}

const handleAdd = () => {
  dialogTitle.value = '新增活动'
  Object.assign(form, {
    id: null, activityName: '', activityType: 'FULL_REDUCTION',
    startTime: '', endTime: '', discount: '', description: ''
  })
  dialogVisible.value = true
}

const handleEdit = (row) => {
  dialogTitle.value = '编辑活动'
  Object.assign(form, row)
  form.id = row.id
  dialogVisible.value = true
}

const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    submitLoading.value = true
    try {
      if (form.id) {
        await updateActivity(form.id, { ...form })
        ElMessage.success('更新成功')
      } else {
        await createActivity({ ...form })
        ElMessage.success('创建成功')
      }
      dialogVisible.value = false
      loadList()
    } catch (error) {
      ElMessage.error('操作失败')
    } finally {
      submitLoading.value = false
    }
  })
}

const handleDelete = async (row) => {
  await ElMessageBox.confirm(`确定要删除活动"${row.activityName}"吗？`, '提示', { type: 'warning' })
  try {
    await deleteActivity(row.id)
    ElMessage.success('删除成功')
    loadList()
  } catch (error) {
    ElMessage.error('删除失败')
  }
}

const handleActivate = async (row) => {
  await ElMessageBox.confirm(`确定要上架活动"${row.activityName}"吗？`, '提示', { type: 'info' })
  try {
    await activateActivity(row.id)
    ElMessage.success('上架成功')
    loadList()
  } catch (error) {
    ElMessage.error('上架失败')
  }
}

// ==================== 初始化 ====================
onMounted(() => {
  loadList()
})
</script>

<style scoped lang="scss">
.activity-container {
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
