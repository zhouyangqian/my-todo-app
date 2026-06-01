<!-- views/user/UserImport.vue - 用户导入导出页面
  支持下载模板、上传文件导入、查看导入结果、导出用户列表
-->
<template>
  <div class="user-import-container">
    <!-- 操作区域 -->
    <el-card class="action-card">
      <template #header>
        <div class="card-header">
          <span>用户导入导出</span>
        </div>
      </template>

      <el-row :gutter="40">
        <!-- 导入区域 -->
        <el-col :span="12">
          <h3 class="section-title">导入用户</h3>
          <el-upload
            ref="uploadRef"
            :auto-upload="false"
            :limit="1"
            accept=".xlsx,.xls"
            :on-change="handleFileChange"
            :on-exceed="() => ElMessage.warning('只能上传一个文件')"
            drag
          >
            <el-icon style="font-size: 40px; color: #c0c4cc;"><Upload /></el-icon>
            <div style="margin-top: 10px;">将Excel文件拖到此处，或<em>点击上传</em></div>
            <template #tip>
              <div class="upload-tip">仅支持 .xlsx / .xls 格式，单次最多导入1000条数据</div>
            </template>
          </el-upload>

          <div class="action-buttons">
            <el-button @click="handleDownloadTemplate">下载导入模板</el-button>
            <el-button type="primary" @click="handleImport" :loading="importLoading" :disabled="!importFile">
              开始导入
            </el-button>
          </div>

          <!-- 导入进度 -->
          <el-progress
            v-if="importLoading"
            :percentage="importProgress"
            :stroke-width="10"
            style="margin-top: 16px;"
          />

          <!-- 导入结果 -->
          <div v-if="importResult" style="margin-top: 16px;">
            <el-alert
              :title="`导入完成：成功 ${importResult.successCount} 条，失败 ${importResult.failCount} 条，共 ${importResult.totalCount} 条`"
              :type="importResult.failCount > 0 ? 'warning' : 'success'"
              show-icon
              :closable="false"
            />
            <el-table
              v-if="importResult.failures && importResult.failures.length > 0"
              :data="importResult.failures"
              border
              stripe
              style="margin-top: 10px; max-height: 200px; overflow-y: auto;"
            >
              <el-table-column prop="rowNum" label="行号" width="80" />
              <el-table-column prop="userName" label="用户名" width="120" />
              <el-table-column prop="reason" label="失败原因" />
            </el-table>
          </div>
        </el-col>

        <!-- 导出区域 -->
        <el-col :span="12">
          <h3 class="section-title">导出用户</h3>
          <el-form :model="exportForm" label-width="80px">
            <el-form-item label="用户名">
              <el-input v-model="exportForm.userName" placeholder="留空则导出全部" clearable />
            </el-form-item>
            <el-form-item label="姓名">
              <el-input v-model="exportForm.realName" placeholder="留空则导出全部" clearable />
            </el-form-item>
            <el-form-item label="状态">
              <el-select v-model="exportForm.status" placeholder="全部" clearable>
                <el-option label="启用" :value="1" />
                <el-option label="禁用" :value="0" />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button type="warning" @click="handleExport" :loading="exportLoading">
                导出Excel
              </el-button>
            </el-form-item>
          </el-form>
        </el-col>
      </el-row>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { Upload } from '@element-plus/icons-vue'
import { downloadImportTemplate, importUsers, exportUsers } from '@/api/user'

const uploadRef = ref()
const importFile = ref(null)
const importLoading = ref(false)
const importProgress = ref(0)
const importResult = ref(null)
const exportLoading = ref(false)

const exportForm = reactive({
  userName: '',
  realName: '',
  status: undefined
})

const handleFileChange = (file) => {
  importFile.value = file.raw
}

const handleDownloadTemplate = async () => {
  try {
    const response = await downloadImportTemplate()
    const blob = new Blob([response.data], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' })
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = 'user_import_template.xlsx'
    link.click()
    window.URL.revokeObjectURL(url)
    ElMessage.success('模板下载成功')
  } catch (error) {
    ElMessage.error('模板下载失败')
  }
}

const handleImport = async () => {
  if (!importFile.value) {
    ElMessage.warning('请选择要导入的文件')
    return
  }
  importLoading.value = true
  importProgress.value = 0
  importResult.value = null

  // 模拟进度
  const progressTimer = setInterval(() => {
    if (importProgress.value < 90) {
      importProgress.value += 10
    }
  }, 200)

  try {
    const result = await importUsers(importFile.value)
    importProgress.value = 100
    importResult.value = result
    if (result.failCount === 0) {
      ElMessage.success(`导入成功：${result.successCount}条`)
    } else {
      ElMessage.warning(`导入完成：成功${result.successCount}条，失败${result.failCount}条`)
    }
  } catch (error) {
    ElMessage.error('导入失败')
  } finally {
    clearInterval(progressTimer)
    importLoading.value = false
  }
}

const handleExport = async () => {
  exportLoading.value = true
  try {
    const response = await exportUsers({
      userName: exportForm.userName || undefined,
      realName: exportForm.realName || undefined,
      status: exportForm.status
    })
    const blob = new Blob([response.data], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' })
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = 'users_export.xlsx'
    link.click()
    window.URL.revokeObjectURL(url)
    ElMessage.success('导出成功')
  } catch (error) {
    ElMessage.error('导出失败')
  } finally {
    exportLoading.value = false
  }
}
</script>

<style scoped lang="scss">
.user-import-container {
  padding: 20px;

  .action-card {
    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
    }
  }

  .section-title {
    font-size: 16px;
    font-weight: 600;
    margin-bottom: 20px;
    color: #303133;
  }

  .upload-tip {
    font-size: 12px;
    color: #909399;
    margin-top: 4px;
  }

  .action-buttons {
    margin-top: 16px;
    display: flex;
    gap: 12px;
  }
}
</style>
