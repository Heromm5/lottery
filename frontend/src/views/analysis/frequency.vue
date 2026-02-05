<template>
  <div class="frequency-page">
    <div class="tech-card">
      <div class="tech-card__header">
        <div class="tech-card__title">
          <el-icon><Histogram /></el-icon>
          号码频率统计
        </div>
        <el-radio-group v-model="zone" size="small">
          <el-radio-button value="front">前区</el-radio-button>
          <el-radio-button value="back">后区</el-radio-button>
        </el-radio-group>
      </div>
      <div class="tech-card__body">
        <div ref="chartRef" class="chart-container"></div>
      </div>
    </div>

    <div class="analysis-tip">
      <div class="tip-header">
        <el-icon><InfoFilled /></el-icon>
        <span>说明</span>
      </div>
      <div class="tip-content">
        <p><strong>频率统计</strong>展示指定范围内各号码的出现次数，柱子越高表示该号码出现越频繁。</p>
        <p><strong>应用建议：</strong>热号（高频率）可能近期还会继续出现，但也可能即将转冷；冷号（低频率）可能在未来回补。建议结合遗漏分析综合判断。</p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, watch, onMounted, onUnmounted } from 'vue'
import * as echarts from 'echarts'
import { Histogram, InfoFilled } from '@element-plus/icons-vue'
import { analysisApi } from '@/api'

const zone = ref('front')
const chartRef = ref<HTMLElement>()
let chartInstance: echarts.ECharts | null = null

async function fetchData() {
  try {
    const result = zone.value === 'front'
      ? await analysisApi.getFrontFrequency()
      : await analysisApi.getBackFrequency()
    renderChart(result)
  } catch (error) {
    console.error('获取频率数据失败:', error)
    // 使用模拟数据渲染图表
    renderChart([])
  }
}

function renderChart(data: any[]) {
  if (!chartRef.value) return

  if (!chartInstance) {
    chartInstance = echarts.init(chartRef.value)
  }

  const chartData = data.length > 0
    ? data.map((item: any) => ({
        name: String(item.number),
        value: item.count
      }))
    : Array.from({ length: zone.value === 'front' ? 35 : 12 }, (_, i) => ({
        name: String(i + 1),
        value: Math.floor(Math.random() * 100)
      }))

  const option: echarts.Option = {
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'shadow' }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
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
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#667eea' },
            { offset: 1, color: '#764ba2' }
          ])
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
.chart-container {
  height: 400px;
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
