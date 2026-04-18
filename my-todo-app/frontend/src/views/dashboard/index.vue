<!-- views/dashboard/index.vue - 工作台首页
  展示系统关键统计数据（用户总数、商品总数、订单总数、销售额）
  包含销售趋势图表占位区和快捷操作按钮
-->
<template>
  <div class="dashboard">
    <!-- 统计卡片区域：展示四个核心指标 -->
    <el-row :gutter="20">
      <!-- 用户总数统计卡片 -->
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: #409eff;">
              <el-icon size="28"><User /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">1,234</div>
              <div class="stat-label">用户总数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <!-- 商品总数统计卡片 -->
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: #67c23a;">
              <el-icon size="28"><Goods /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">5,678</div>
              <div class="stat-label">商品总数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <!-- 订单总数统计卡片 -->
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: #e6a23c;">
              <el-icon size="28"><ShoppingCart /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">890</div>
              <div class="stat-label">订单总数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <!-- 销售额统计卡片 -->
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: #f56c6c;">
              <el-icon size="28"><Money /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">¥ 123,456</div>
              <div class="stat-label">销售额</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 下方区域：销售趋势图表 + 快捷操作 -->
    <el-row :gutter="20" style="margin-top: 20px;">
      <!-- 销售趋势图表（占位，待集成图表库） -->
      <el-col :span="16">
        <el-card>
          <template #header>
            <span>销售趋势</span>
          </template>
          <div class="chart-placeholder">
            <el-empty description="图表组件开发中..." />
          </div>
        </el-card>
      </el-col>
      <!-- 快捷操作按钮区 -->
      <el-col :span="8">
        <el-card>
          <template #header>
            <span>快捷操作</span>
          </template>
          <div class="quick-actions">
            <el-button type="primary" :icon="Plus">新建订单</el-button>
            <el-button type="success" :icon="Goods">添加商品</el-button>
            <el-button type="warning" :icon="User">添加用户</el-button>
            <el-button type="info" :icon="Document">导出报表</el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
// 工作台首页逻辑：目前为静态展示页面，导入图标组件
import { Plus, Goods, User, Document } from '@element-plus/icons-vue'
</script>

<style lang="scss" scoped>
.dashboard {
  padding: 20px;

  .stat-card {
    .stat-content {
      display: flex;
      align-items: center;

      .stat-icon {
        width: 60px;
        height: 60px;
        border-radius: 8px;
        display: flex;
        align-items: center;
        justify-content: center;
        color: #fff;
      }

      .stat-info {
        margin-left: 20px;

        .stat-value {
          font-size: 24px;
          font-weight: bold;
          color: #303133;
        }

        .stat-label {
          font-size: 14px;
          color: #909399;
          margin-top: 5px;
        }
      }
    }
  }

  .chart-placeholder {
    height: 300px;
    display: flex;
    align-items: center;
    justify-content: center;
  }

  .quick-actions {
    display: flex;
    flex-direction: column;
    gap: 12px;

    .el-button {
      width: 100%;
    }
  }
}
</style>
