<!-- views/dashboard/index.vue - 工作台首页
  展示系统关键统计数据（商品总数、销售订单、采购订单、库存预警、金额统计）
  包含待办事项区域和快捷操作按钮
-->
<template>
  <div class="dashboard">
    <!-- 统计卡片区域：展示核心指标 -->
    <el-row :gutter="20">
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: #67c23a;">
              <el-icon size="28"><Goods /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.productCount ?? '-' }}</div>
              <div class="stat-label">商品总数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: #409eff;">
              <el-icon size="28"><ShoppingCart /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.salesOrderCount ?? '-' }}</div>
              <div class="stat-label">销售订单</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: #e6a23c;">
              <el-icon size="28"><Van /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.purchaseOrderCount ?? '-' }}</div>
              <div class="stat-label">采购订单</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: #f56c6c;">
              <el-icon size="28"><Warning /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.lowStockCount ?? '-' }}</div>
              <div class="stat-label">库存预警</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 金额统计区域 -->
    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="8">
        <el-card shadow="hover" class="amount-card">
          <div class="amount-content">
            <div class="amount-label">销售总金额</div>
            <div class="amount-value" style="color: #409eff;">
              ¥ {{ formatAmount(stats.salesOrderAmount) }}
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover" class="amount-card">
          <div class="amount-content">
            <div class="amount-label">采购总金额</div>
            <div class="amount-value" style="color: #e6a23c;">
              ¥ {{ formatAmount(stats.purchaseOrderAmount) }}
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover" class="amount-card">
          <div class="amount-content">
            <div class="amount-label">毛利润</div>
            <div class="amount-value" style="color: #67c23a;">
              ¥ {{ formatAmount(grossProfit) }}
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 待办事项 + 快捷操作 -->
    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="16">
        <el-card>
          <template #header>
            <span>待办事项</span>
          </template>
          <div class="todo-list">
            <div class="todo-item">
              <el-tag type="warning" size="large">待审核销售订单</el-tag>
              <span class="todo-count">{{ stats.pendingSalesOrders ?? 0 }} 笔</span>
            </div>
            <div class="todo-item">
              <el-tag type="warning" size="large">待审核采购订单</el-tag>
              <span class="todo-count">{{ stats.pendingPurchaseOrders ?? 0 }} 笔</span>
            </div>
            <div class="todo-item">
              <el-tag type="danger" size="large">库存预警</el-tag>
              <span class="todo-count">{{ stats.lowStockCount ?? 0 }} 项</span>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card>
          <template #header>
            <span>快捷操作</span>
          </template>
          <div class="quick-actions">
            <el-button type="primary" :icon="Plus" @click="$router.push('/erp/sales-order')">新建销售订单</el-button>
            <el-button type="success" :icon="Goods" @click="$router.push('/erp/purchase-order')">新建采购订单</el-button>
            <el-button type="warning" :icon="Box" @click="$router.push('/erp/product')">添加商品</el-button>
            <el-button type="info" :icon="DataAnalysis" @click="$router.push('/erp/inventory')">库存管理</el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { Plus, Goods, ShoppingCart, Van, Warning, Box, DataAnalysis } from '@element-plus/icons-vue'
import { getDashboardStats } from '@/api/erp'

const stats = ref({})

const grossProfit = computed(() => {
  const sales = Number(stats.value.salesOrderAmount) || 0
  const purchase = Number(stats.value.purchaseOrderAmount) || 0
  return sales - purchase
})

const formatAmount = (value) => {
  if (value === null || value === undefined) return '-'
  const num = Number(value)
  if (isNaN(num)) return '-'
  return num.toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

const loadStats = async () => {
  try {
    const res = await getDashboardStats()
    stats.value = res || {}
  } catch (error) {
    console.error('加载统计数据失败', error)
  }
}

onMounted(() => {
  loadStats()
})
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

  .amount-card {
    .amount-content {
      text-align: center;
      padding: 10px 0;

      .amount-label {
        font-size: 14px;
        color: #909399;
        margin-bottom: 8px;
      }

      .amount-value {
        font-size: 22px;
        font-weight: bold;
      }
    }
  }

  .todo-list {
    .todo-item {
      display: flex;
      align-items: center;
      justify-content: space-between;
      padding: 12px 0;
      border-bottom: 1px solid #ebeef5;

      &:last-child {
        border-bottom: none;
      }

      .todo-count {
        font-size: 16px;
        font-weight: bold;
        color: #303133;
      }
    }
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
