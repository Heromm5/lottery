<template>
  <div class="trend-page">
    <div class="tech-card">
      <div class="tech-card__header">
        <div class="tech-card__title">
          <el-icon><TrendCharts /></el-icon>
          走势分析
        </div>
        <div class="header-actions">
          <span class="label">查看期数</span>
          <el-select v-model="size" size="small" style="width: 100px">
            <el-option :value="30" label="近30期" />
            <el-option :value="50" label="近50期" />
            <el-option :value="100" label="近100期" />
          </el-select>
        </div>
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
        <p><strong>走势分析</strong>展示最近多期开奖号码的分布情况，可以直观看出号码的变化趋势。</p>
        <p><strong>图表解读：</strong>每个点代表一个号码在当期的位置，上方为前区（01-35），下方为后区（01-12）。</p>
        <p><strong>应用建议：</strong>观察号码是否在某区域聚集或分散，判断是否会出现"回归均值"现象。连续多期未出现的区域可能即将回补。</p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, watch, onMounted, onUnmounted } from 'vue'
import * as echarts from 'echarts'
import { TrendCharts, InfoFilled } from '@element-plus/icons-vue'
import { analysisApi } from '@/api'

const size = ref(30)
const chartRef = ref<HTMLElement>()
let chartInstance: echarts.ECharts | null = null

async function fetchData() {
  try {
    const result = await analysisApi.getTrend(size.value)
    renderChart(result)
  } catch (error) {
    console.error('获取走势数据失败:', error)
    renderChart([])
  }
}

function renderChart(data: any[]) {
  if (!chartRef.value) return

  if (!chartInstance) {
    chartInstance = echarts.init(chartRef.value)
  }

  // 模拟走势数据
  const issues = data.length > 0
    ? data.map((item: any) => item.issue)
    : Array.from({ length: size.value }, (_, i) => `2024${String(i + 1).padStart(3, '0')}`)

  // 后端返回 frontBalls 和 backBalls，分开处理
  const frontNumbers = data.length > 0
    ? data.map((item: any) => item.frontBalls || [])
    : Array.from({ length: size.value }, () =>
        Array.from({ length: 5 }, () => Math.floor(Math.random() * 35) + 1)
      ).reverse()

  const backNumbers = data.length > 0
    ? data.map((item: any) => item.backBalls || [])
    : Array.from({ length: size.value }, () =>
        Array.from({ length: 2 }, () => Math.floor(Math.random() * 12) + 1)
      ).reverse()

  const option: echarts.Option = {
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'cross' },
      formatter: (params: any) => {
        const idx = params[0].dataIndex
        const issue = issues[idx]
        const front = frontNumbers[idx] || []
        const back = backNumbers[idx] || []
        return `期号: ${issue}<br/>前区: ${front.join(', ')}<br/>后区: ${back.join(', ')}`
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
      data: issues,
      axisLabel: { color: '#fff', rotate: 45 },
      axisLine: { lineStyle: { color: '#667eea' } }
    },
    yAxis: {
      type: 'value',
      min: 0,
      max: 35,
      axisLabel: { color: '#fff' },
      splitLine: { lineStyle: { color: 'rgba(102,126,234,0.2)' } }
    },
    series: [
      {
        name: '前区',
        type: 'scatter',
        symbolSize: 8,
        data: frontNumbers.map(nums => nums[0]),
        lineStyle: { color: '#667eea' },
        itemStyle: { color: '#667eea' }
      },
      {
        name: '前区2',
        type: 'scatter',
        symbolSize: 8,
        data: frontNumbers.map(nums => nums[1]),
        itemStyle: { color: '#667eea', opacity: 0.8 }
      },
      {
        name: '前区3',
        type: 'scatter',
        symbolSize: 8,
        data: frontNumbers.map(nums => nums[2]),
        itemStyle: { color: '#667eea', opacity: 0.6 }
      },
      {
        name: '前区4',
        type: 'scatter',
        symbolSize: 8,
        data: frontNumbers.map(nums => nums[3]),
        itemStyle: { color: '#667eea', opacity: 0.4 }
      },
      {
        name: '前区5',
        type: 'scatter',
        symbolSize: 8,
        data: frontNumbers.map(nums => nums[4]),
        itemStyle: { color: '#667eea', opacity: 0.2 }
      },
      {
        name: '后区1',
        type: 'scatter',
        symbolSize: 10,
        data: backNumbers.map(nums => nums[0]),
        lineStyle: { color: '#f5576c' },
        itemStyle: { color: '#f5576c' }
      },
      {
        name: '后区2',
        type: 'scatter',
        symbolSize: 10,
        data: backNumbers.map(nums => nums[1]),
        itemStyle: { color: '#f5576c', opacity: 0.8 }
      }
    ]
  }

  chartInstance.setOption(option)
}

watch(size, fetchData)

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
.header-actions {
  display: flex;
  align-items: center;
  gap: 12px;

  .label {
    color: $text-muted;
    font-size: 14px;
  }
}

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
