<template>
  <div class="association-page">
    <div class="tech-card">
      <div class="tech-card__header">
        <div class="tech-card__title">
          <el-icon><Share /></el-icon>
          关联分析
        </div>
        <el-radio-group v-model="zone" size="small" @change="fetchData">
          <el-radio-button value="front">前区</el-radio-button>
          <el-radio-button value="back">后区</el-radio-button>
        </el-radio-group>
      </div>
      <div class="tech-card__body">
        <div ref="chartRef" class="chart-container"></div>
      </div>
    </div>

    <div class="tech-card">
      <div class="tech-card__header">
        <div class="tech-card__title">
          <el-icon><List /></el-icon>
          关联规则详情
        </div>
      </div>
      <div class="tech-card__body">
        <el-table :data="rules" class="association-table" v-loading="loading">
          <el-table-column prop="antecedent" label="关联号码" width="120">
            <template #default="{ row }">
              {{ row.antecedent.join(', ') }}
            </template>
          </el-table-column>
          <el-table-column prop="consequent" label="关联结果" width="120">
            <template #default="{ row }">
              {{ row.consequent.join(', ') }}
            </template>
          </el-table-column>
          <el-table-column prop="support" label="支持度" width="100" align="center">
            <template #default="{ row }">
              {{ (row.support * 100).toFixed(2) }}%
            </template>
          </el-table-column>
          <el-table-column prop="confidence" label="置信度" width="100" align="center">
            <template #default="{ row }">
              {{ (row.confidence * 100).toFixed(2) }}%
            </template>
          </el-table-column>
          <el-table-column prop="lift" label="提升度" width="100" align="center">
            <template #default="{ row }">
              <span :class="row.lift > 1 ? 'lift-up' : ''">{{ row.lift.toFixed(2) }}</span>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </div>

    <div class="analysis-tip">
      <div class="tip-header">
        <el-icon><InfoFilled /></el-icon>
        <span>说明</span>
      </div>
      <div class="tip-content">
        <p><strong>关联分析</strong>通过挖掘历史数据，发现号码之间的共现规律。例如：如果号码 A 和 B 经常一起出现，则它们之间存在关联规则。</p>
        <p><strong>指标说明：</strong>支持度 = 该规则出现的概率；置信度 = 前置号码出现时，结果号码出现的概率；提升度 = 该规则相比随机出现的倍数（>1 表示正相关）。</p>
        <p><strong>应用建议：</strong>选择提升度高的关联规则，当您选的号码包含前置号码时，可以重点关注结果号码。但需注意，历史规律不一定在未来继续有效。</p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, watch } from 'vue'
import * as echarts from 'echarts'
import { Share, List, InfoFilled } from '@element-plus/icons-vue'
import { analysisApi } from '@/api'
import type { AssociationRule } from '@/types'

const zone = ref<'front' | 'back'>('front')
const chartRef = ref<HTMLElement>()
const rules = ref<AssociationRule[]>([])
const loading = ref(false)
let chartInstance: echarts.ECharts | null = null

async function fetchData() {
  loading.value = true
  try {
    const result = await analysisApi.getAssociationRules(zone.value)
    rules.value = result || []
    renderChart(result || [])
  } catch (error) {
    console.error('获取关联数据失败:', error)
    rules.value = []
    renderChart([])
  } finally {
    loading.value = false
  }
}

function renderChart(data: AssociationRule[]) {
  if (!chartRef.value) return

  if (!chartInstance) {
    chartInstance = echarts.init(chartRef.value)
  }

  const topRules = data.slice(0, 20)

  const option: echarts.Option = {
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'shadow' },
      formatter: (params: any) => {
        const rule = topRules[params[0].dataIndex]
        if (!rule) return ''
        return `关联: ${rule.antecedent.join(', ')} → ${rule.consequent.join(', ')}<br/>
                支持度: ${(rule.support * 100).toFixed(2)}%<br/>
                置信度: ${(rule.confidence * 100).toFixed(2)}%<br/>
                提升度: ${rule.lift.toFixed(2)}`
      }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: topRules.map(r => `${r.antecedent.join('-')}→${r.consequent.join('-')}`),
      axisLabel: { color: '#fff', rotate: 45, fontSize: 10 },
      axisLine: { lineStyle: { color: '#667eea' } }
    },
    yAxis: {
      type: 'value',
      name: '提升度',
      nameTextStyle: { color: '#fff' },
      axisLabel: { color: '#fff' },
      splitLine: { lineStyle: { color: 'rgba(102,126,234,0.2)' } }
    },
    series: [{
      type: 'bar',
      data: topRules.map(r => ({
        value: r.lift,
        itemStyle: {
          color: r.lift > 1.5 ? '#ff4757' : r.lift > 1 ? '#ffa502' : '#00f5ff'
        }
      })),
      barWidth: '60%'
    }]
  }

  chartInstance.setOption(option)
}

watch(zone, fetchData)

onMounted(() => {
  fetchData()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  chartInstance?.dispose()
})

function handleResize() {
  chartInstance?.resize()
}
</script>

<style lang="scss" scoped>
.association-page {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.chart-container {
  height: 300px;
}

.association-table {
  --el-table-bg-color: transparent;
  --el-table-tr-bg-color: rgba($bg-dark-lighter, 0.6);
  --el-table-header-bg-color: rgba($bg-dark-lighter, 0.8);
  --el-table-row-hover-bg-color: rgba($primary-color, 0.15);
  --el-table-border-color: $border-color;
  --el-table-text-color: $text-primary;
  --el-table-header-text-color: $text-secondary;

  background: transparent;

  &::before {
    display: none;
  }

  th.el-table__cell {
    background: rgba($bg-dark-lighter, 0.9);
    border-bottom: 1px solid $border-color;
  }

  td.el-table__cell {
    border-bottom: 1px solid rgba($border-color, 0.4);
  }
}

.lift-up {
  color: $accent-green;
  font-weight: 600;
}

.analysis-tip {
  background: rgba($bg-dark-lighter, 0.6);
  border: 1px solid $border-color;
  border-radius: 8px;
  padding: 16px 20px;
  margin-top: 24px;

  .tip-header {
    display: flex;
    align-items: center;
    gap: 8px;
    color: $accent-cyan;
    font-size: 14px;
    font-weight: 600;
    margin-bottom: 12px;
  }

  .tip-content {
    p {
      margin: 0 0 8px;
      color: $text-secondary;
      font-size: 13px;
      line-height: 1.6;

      &:last-child {
        margin-bottom: 0;
      }
    }
  }
}
</style>
