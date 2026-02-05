<template>
  <div class="missing-page">
    <div class="missing-grid">
      <div class="tech-card">
        <div class="tech-card__header">
          <div class="tech-card__title">
            <el-icon><Minus /></el-icon>
            前区遗漏分析
          </div>
        </div>
        <div class="tech-card__body">
          <div ref="frontChartRef" class="chart-container"></div>
        </div>
      </div>

      <div class="tech-card">
        <div class="tech-card__header">
          <div class="tech-card__title">
            <el-icon><Minus /></el-icon>
            后区遗漏分析
          </div>
        </div>
        <div class="tech-card__body">
          <div ref="backChartRef" class="chart-container"></div>
        </div>
      </div>
    </div>

    <div class="analysis-tip">
      <div class="tip-header">
        <el-icon><InfoFilled /></el-icon>
        <span>说明</span>
      </div>
      <div class="tip-content">
        <p><strong>遗漏分析</strong>展示各号码距离上次出现的期数，柱子越高表示该号码已经遗漏越久。</p>
        <p><strong>指标说明：</strong>当前遗漏 = 距上次出现已隔期数；平均遗漏 = 历史平均遗漏期数；最大遗漏 = 历史最长遗漏期数。</p>
        <p><strong>应用建议：</strong>遗漏值接近或超过平均遗漏的号码称为"回补候选"，可以考虑在选号时重点关注。</p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import * as echarts from 'echarts'
import { Minus, InfoFilled } from '@element-plus/icons-vue'
import { analysisApi } from '@/api'

const frontChartRef = ref<HTMLElement>()
const backChartRef = ref<HTMLElement>()
let frontChartInstance: echarts.ECharts | null = null
let backChartInstance: echarts.ECharts | null = null

async function fetchData() {
  try {
    const [frontResult, backResult] = await Promise.all([
      analysisApi.getFrontMissing(),
      analysisApi.getBackMissing()
    ])
    renderFrontChart(frontResult)
    renderBackChart(backResult)
  } catch (error) {
    console.error('获取遗漏数据失败:', error)
    renderFrontChart([])
    renderBackChart([])
  }
}

function renderFrontChart(data: any[]) {
  if (!frontChartRef.value) return
  if (!frontChartInstance) {
    frontChartInstance = echarts.init(frontChartRef.value)
  }

  const chartData = data.length > 0
    ? data.map((item: any) => ({
        name: String(item.number),
        value: item.currentMissing,
        maxMissing: item.maxMissing
      }))
    : Array.from({ length: 35 }, (_, i) => ({
        name: String(i + 1),
        value: Math.floor(Math.random() * 50),
        maxMissing: Math.floor(Math.random() * 100)
      }))

  const option: echarts.Option = {
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'shadow' },
      formatter: (params: any) => {
        const d = chartData.find((item: any) => item.name === params[0].name)
        return `号码 ${params[0].name}<br/>当前遗漏: ${params[0].value}<br/>最大遗漏: ${d?.maxMissing || 0}`
      }
    },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: {
      type: 'category',
      data: chartData.map((item: any) => item.name),
      axisLabel: { color: '#fff', interval: 4 },
      axisLine: { lineStyle: { color: '#667eea' } }
    },
    yAxis: {
      type: 'value',
      axisLabel: { color: '#fff' },
      splitLine: { lineStyle: { color: 'rgba(102,126,234,0.2)' } }
    },
    series: [{
      type: 'bar',
      data: chartData.map((item: any) => ({
        value: item.value,
        itemStyle: { color: item.value > 30 ? '#ff4757' : '#00f5ff' }
      })),
      barWidth: '60%'
    }]
  }
  frontChartInstance.setOption(option)
}

function renderBackChart(data: any[]) {
  if (!backChartRef.value) return
  if (!backChartInstance) {
    backChartInstance = echarts.init(backChartRef.value)
  }

  const chartData = data.length > 0
    ? data.map((item: any) => ({
        name: String(item.number),
        value: item.currentMissing,
        maxMissing: item.maxMissing
      }))
    : Array.from({ length: 12 }, (_, i) => ({
        name: String(i + 1),
        value: Math.floor(Math.random() * 50),
        maxMissing: Math.floor(Math.random() * 100)
      }))

  const option: echarts.Option = {
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'shadow' },
      formatter: (params: any) => {
        const d = chartData.find((item: any) => item.name === params[0].name)
        return `号码 ${params[0].name}<br/>当前遗漏: ${params[0].value}<br/>最大遗漏: ${d?.maxMissing || 0}`
      }
    },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: {
      type: 'category',
      data: chartData.map((item: any) => item.name),
      axisLabel: { color: '#fff' },
      axisLine: { lineStyle: { color: '#667eea' } }
    },
    yAxis: {
      type: 'value',
      axisLabel: { color: '#fff' },
      splitLine: { lineStyle: { color: 'rgba(102,126,234,0.2)' } }
    },
    series: [{
      type: 'bar',
      data: chartData.map((item: any) => ({
        value: item.value,
        itemStyle: { color: item.value > 30 ? '#ff4757' : '#00f5ff' }
      })),
      barWidth: '60%'
    }]
  }
  backChartInstance.setOption(option)
}

onMounted(() => {
  fetchData()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  frontChartInstance?.dispose()
  backChartInstance?.dispose()
})

function handleResize() {
  frontChartInstance?.resize()
  backChartInstance?.resize()
}
</script>

<style lang="scss" scoped>
.missing-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 24px;
}

.chart-container {
  height: 300px;
}

.analysis-tip {
  background: rgba($bg-dark-lighter, 0.6);
  border: 1px solid $border-color;
  border-radius: 8px;
  padding: 16px 20px;

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
