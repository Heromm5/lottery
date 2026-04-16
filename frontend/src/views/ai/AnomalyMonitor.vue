<template>
  <div class="anomaly-monitor">
    <div class="page-header">
      <h1>异常监控</h1>
      <p>AI 驱动的异常检测告警面板，监控数据异常和预测偏差</p>
    </div>

    <!-- 异常告警卡片 -->
    <div class="tech-card">
      <div class="tech-card__header">
        <div class="tech-card__title">
          <el-icon><Warning /></el-icon>
          异常告警
        </div>
        <div class="header-actions">
          <el-select v-model="filterSeverity" placeholder="筛选严重程度" clearable style="width: 140px" @change="loadAlerts">
            <el-option label="全部" value="" />
            <el-option label="低" value="low" />
            <el-option label="中" value="medium" />
            <el-option label="高" value="high" />
            <el-option label="严重" value="critical" />
          </el-select>
          <el-button type="primary" :icon="Refresh" @click="loadAlerts" :loading="loading">
            刷新
          </el-button>
        </div>
      </div>
      <div class="tech-card__body">
        <el-table
          :data="alerts"
          class="anomaly-table"
          style="width: 100%"
          v-loading="loading"
          :row-class-name="getRowClassName"
        >
          <el-table-column prop="type" label="类型" width="140">
            <template #default="{ row }">
              <div class="type-cell">
                <el-icon><component :is="getTypeIcon(row.type)" /></el-icon>
                <span>{{ formatType(row.type) }}</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="severity" label="严重程度" width="100" align="center">
            <template #default="{ row }">
              <el-tag :type="getSeverityType(row.severity)" size="small" effect="dark">
                {{ formatSeverity(row.severity) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="message" label="描述" min-width="280">
            <template #default="{ row }">
              <div class="message-cell">{{ row.message }}</div>
            </template>
          </el-table-column>
          <el-table-column prop="detectedAt" label="检测时间" width="180">
            <template #default="{ row }">
              <span class="time-cell">{{ formatDate(row.detectedAt) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="状态" width="100" align="center">
            <template #default="{ row }">
              <el-tag v-if="row.acknowledged" type="success" size="small">
                已确认
              </el-tag>
              <el-tag v-else type="info" size="small">
                待处理
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="120" align="center">
            <template #default="{ row }">
              <el-button
                v-if="!row.acknowledged"
                type="primary"
                size="small"
                :loading="acknowledgingId === row.id"
                @click="handleAcknowledge(row.id)"
              >
                确认
              </el-button>
              <span v-else class="ack-time">
                {{ formatDate(row.acknowledgedAt) }}
              </span>
            </template>
          </el-table-column>
        </el-table>

        <el-empty v-if="!loading && alerts.length === 0" description="暂无异常告警" />

        <div v-if="alerts.length > 0" class="pagination-wrap">
          <el-pagination
            v-model:current-page="currentPage"
            v-model:page-size="pageSize"
            :page-sizes="[10, 20, 50]"
            :total="total"
            layout="total, sizes, prev, pager, next"
            @size-change="loadAlerts"
            @current-change="loadAlerts"
          />
        </div>
      </div>
    </div>

    <!-- 统计概览卡片 -->
    <div class="tech-card">
      <div class="tech-card__header">
        <div class="tech-card__title">
          <el-icon><DataAnalysis /></el-icon>
          告警统计
        </div>
      </div>
      <div class="tech-card__body">
        <el-row :gutter="20">
          <el-col :span="6">
            <div class="stat-card stat-critical">
              <div class="stat-icon">
                <el-icon><WarningFilled /></el-icon>
              </div>
              <div class="stat-content">
                <div class="stat-value">{{ stats.critical }}</div>
                <div class="stat-label">严重</div>
              </div>
            </div>
          </el-col>
          <el-col :span="6">
            <div class="stat-card stat-high">
              <div class="stat-icon">
                <el-icon><Warning /></el-icon>
              </div>
              <div class="stat-content">
                <div class="stat-value">{{ stats.high }}</div>
                <div class="stat-label">高</div>
              </div>
            </div>
          </el-col>
          <el-col :span="6">
            <div class="stat-card stat-medium">
              <div class="stat-icon">
                <el-icon><InfoFilled /></el-icon>
              </div>
              <div class="stat-content">
                <div class="stat-value">{{ stats.medium }}</div>
                <div class="stat-label">中</div>
              </div>
            </div>
          </el-col>
          <el-col :span="6">
            <div class="stat-card stat-low">
              <div class="stat-icon">
                <el-icon><CircleCheck /></el-icon>
              </div>
              <div class="stat-content">
                <div class="stat-value">{{ stats.low }}</div>
                <div class="stat-label">低</div>
              </div>
            </div>
          </el-col>
        </el-row>
      </div>
    </div>

    <!-- 说明卡片 -->
    <div class="analysis-tip">
      <div class="tip-header">
        <el-icon><InfoFilled /></el-icon>
        <span>说明</span>
      </div>
      <div class="tip-content">
        <p><strong>异常监控</strong>功能由 AI 自动检测数据异常、预测偏差和模型异常。</p>
        <p><strong>严重程度：</strong></p>
        <ul>
          <li><span class="severity-tag severity-critical">严重</span> - 需要立即处理，可能影响系统运行</li>
          <li><span class="severity-tag severity-high">高</span> - 需要尽快处理，影响预测准确性</li>
          <li><span class="severity-tag severity-medium">中</span> - 需要关注，可能影响用户体验</li>
          <li><span class="severity-tag severity-low">低</span> - 轻微异常，可后续处理</li>
        </ul>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import {
  Warning,
  WarningFilled,
  InfoFilled,
  Refresh,
  DataAnalysis,
  CircleCheck,
  TrendCharts,
  Connection,
  Timer,
  MagicStick
} from '@element-plus/icons-vue'
import { aiApi, type AiAnomalyAlert } from '@/api/modules/ai'

const alerts = ref<AiAnomalyAlert[]>([])
const loading = ref(false)
const acknowledgingId = ref<string | null>(null)

// 分页
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 筛选
const filterSeverity = ref('')

// 统计数据
const stats = computed(() => {
  const result = { critical: 0, high: 0, medium: 0, low: 0 }
  alerts.value.forEach(alert => {
    if (alert.severity === 'critical') result.critical++
    else if (alert.severity === 'high') result.high++
    else if (alert.severity === 'medium') result.medium++
    else if (alert.severity === 'low') result.low++
  })
  return result
})

async function loadAlerts() {
  loading.value = true
  try {
    const result = await aiApi.getAnomalyAlerts({
      severity: filterSeverity.value || undefined,
      acknowledged: undefined
    })
    // axios 拦截器已返回 data 字段
    alerts.value = Array.isArray(result) ? result : []
    total.value = alerts.value.length
  } catch (error) {
    console.error('获取异常告警失败:', error)
    alerts.value = []
  } finally {
    loading.value = false
  }
}

async function handleAcknowledge(id: string) {
  acknowledgingId.value = id
  try {
    await aiApi.acknowledgeAlert(id)
    ElMessage.success('告警已确认')
    await loadAlerts()
  } catch (error: any) {
    console.error('确认告警失败:', error)
    ElMessage.error(error?.message || '确认失败')
  } finally {
    acknowledgingId.value = null
  }
}

function getSeverityType(severity: string): 'danger' | 'warning' | 'info' | 'success' {
  switch (severity) {
    case 'critical':
      return 'danger'
    case 'high':
      return 'warning'
    case 'medium':
      return 'info'
    case 'low':
      return 'success'
    default:
      return 'info'
  }
}

function formatSeverity(severity: string): string {
  const map: Record<string, string> = {
    critical: '严重',
    high: '高',
    medium: '中',
    low: '低'
  }
  return map[severity] || severity
}

function formatType(type: string): string {
  const map: Record<string, string> = {
    data_anomaly: '数据异常',
    prediction_deviation: '预测偏差',
    model_drift: '模型漂移',
    pattern_break: '模式断裂',
    outlier: '离群点',
    frequency_spike: '频率异常',
    missing_data: '数据缺失'
  }
  return map[type] || type
}

function getTypeIcon(type: string) {
  const iconMap: Record<string, any> = {
    data_anomaly: TrendCharts,
    prediction_deviation: MagicStick,
    model_drift: Connection,
    pattern_break: Warning,
    outlier: WarningFilled,
    frequency_spike: Timer,
    missing_data: InfoFilled
  }
  return iconMap[type] || Warning
}

function formatDate(dateStr: string): string {
  if (!dateStr) return '-'
  const date = new Date(dateStr)
  const now = new Date()
  const diff = now.getTime() - date.getTime()

  // 小于1小时显示相对时间
  if (diff < 3600000) {
    const minutes = Math.floor(diff / 60000)
    if (minutes < 1) return '刚刚'
    return `${minutes}分钟前`
  }

  // 小于24小时显示小时
  if (diff < 86400000) {
    const hours = Math.floor(diff / 3600000)
    return `${hours}小时前`
  }

  // 超过24小时显示具体时间
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

function getRowClassName({ row }: { row: AiAnomalyAlert }) {
  if (row.severity === 'critical') return 'row-critical'
  if (row.severity === 'high') return 'row-high'
  return ''
}

onMounted(() => {
  loadAlerts()
})
</script>

<style lang="scss" scoped>
@use '@/assets/styles/variables.scss' as *;

.anomaly-monitor {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.page-header {
  h1 {
    font-size: 24px;
    font-weight: 600;
    margin: 0 0 8px;
    color: $text-primary;
  }

  p {
    color: $text-muted;
    margin: 0;
  }
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

// Table styles
.anomaly-table {
  --el-table-bg-color: transparent;
  --el-table-tr-bg-color: rgba($bg-dark-lighter, 0.6);
  --el-table-header-bg-color: rgba($bg-dark-lighter, 0.8);
  --el-table-row-hover-bg-color: rgba($primary-color, 0.15);
  --el-table-border-color: $border-color;
  --el-table-text-color: $text-primary;
  --el-table-header-text-color: $text-secondary;
  --el-table-cell-height: 48px;

  background: transparent;
  font-size: 13px;

  &::before {
    display: none;
  }

  th.el-table__cell {
    background: rgba($bg-dark-lighter, 0.9);
    border-bottom: 1px solid $border-color;
    padding: 8px 0;
    font-weight: 600;
  }

  td.el-table__cell {
    border-bottom: 1px solid rgba($border-color, 0.4);
    padding: 6px 0;
  }

  tr:hover > td.el-table__cell {
    background: rgba($primary-color, 0.15) !important;
  }

  tr.row-critical > td.el-table__cell {
    background: rgba($accent-red, 0.1);
  }

  tr.row-high > td.el-table__cell {
    background: rgba($accent-orange, 0.08);
  }
}

.type-cell {
  display: flex;
  align-items: center;
  gap: 8px;

  .el-icon {
    color: $primary-color;
  }
}

.message-cell {
  color: $text-primary;
  line-height: 1.5;
}

.time-cell {
  color: $text-secondary;
  font-size: 12px;
}

.ack-time {
  font-size: 11px;
  color: $text-muted;
}

.pagination-wrap {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}

// Stats cards
.stat-card {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px;
  background: rgba($bg-dark, 0.8);
  border: 1px solid $border-color;
  border-radius: $radius-md;
  transition: all 0.3s ease;

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 4px 16px rgba(0, 0, 0, 0.3);
  }

  .stat-icon {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 48px;
    height: 48px;
    border-radius: 50%;
    font-size: 20px;
  }

  .stat-content {
    flex: 1;
  }

  .stat-value {
    font-size: 28px;
    font-weight: 700;
    font-family: $font-mono;
    line-height: 1.2;
  }

  .stat-label {
    font-size: 13px;
    color: $text-muted;
    margin-top: 4px;
  }

  &.stat-critical {
    border-color: rgba($accent-red, 0.4);
    .stat-icon {
      background: rgba($accent-red, 0.2);
      color: $accent-red;
    }
    .stat-value {
      color: $accent-red;
    }
  }

  &.stat-high {
    border-color: rgba($accent-orange, 0.4);
    .stat-icon {
      background: rgba($accent-orange, 0.2);
      color: $accent-orange;
    }
    .stat-value {
      color: $accent-orange;
    }
  }

  &.stat-medium {
    border-color: rgba($primary-color, 0.4);
    .stat-icon {
      background: rgba($primary-color, 0.2);
      color: $primary-color;
    }
    .stat-value {
      color: $primary-color;
    }
  }

  &.stat-low {
    border-color: rgba($accent-green, 0.4);
    .stat-icon {
      background: rgba($accent-green, 0.2);
      color: $accent-green;
    }
    .stat-value {
      color: $accent-green;
    }
  }
}

// Analysis tip
.analysis-tip {
  background: rgba($bg-dark-lighter, 0.6);
  border: 1px solid $border-color;
  border-radius: $radius-md;
  overflow: hidden;

  .tip-header {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 12px 16px;
    background: rgba($bg-dark, 0.8);
    border-bottom: 1px solid $border-color;
    color: $text-secondary;
    font-weight: 600;
  }

  .tip-content {
    padding: 16px;
    color: $text-secondary;
    font-size: 13px;
    line-height: 1.8;

    p {
      margin: 0 0 12px;
    }

    ul {
      margin: 8px 0;
      padding-left: 20px;
    }

    li {
      margin-bottom: 6px;
    }
  }
}

.severity-tag {
  display: inline-block;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;

  &.severity-critical {
    background: rgba($accent-red, 0.2);
    color: $accent-red;
  }

  &.severity-high {
    background: rgba($accent-orange, 0.2);
    color: $accent-orange;
  }

  &.severity-medium {
    background: rgba($primary-color, 0.2);
    color: $primary-color;
  }

  &.severity-low {
    background: rgba($accent-green, 0.2);
    color: $accent-green;
  }
}
</style>