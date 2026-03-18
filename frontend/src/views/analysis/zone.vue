<template>
  <div class="zone-page">
    <!-- 区间分布统计卡片 -->
    <div class="tech-card">
      <div class="tech-card__header">
        <div class="tech-card__title">
          <el-icon><Grid /></el-icon>
          号码区间分布
        </div>
      </div>
      <div class="tech-card__body">
        <div ref="chartRef" class="chart-container"></div>
        
        <!-- 区间详情表格 -->
        <div class="zone-table-wrap">
          <el-table :data="zoneTableData" class="zone-table" stripe>
            <el-table-column prop="zone" label="区间" align="center" />
            <el-table-column prop="label" label="号码范围" align="center" />
            <el-table-column prop="avg" label="平均出现" align="center" />
            <el-table-column prop="max" label="最多出现" align="center" />
            <el-table-column prop="min" label="最少出现" align="center" />
          </el-table>
        </div>
      </div>
    </div>

    <!-- 说明卡片 -->
    <div class="info-card">
      <div class="info-card__title">
        <el-icon><InfoFilled /></el-icon>
        说明
      </div>
      <div class="info-card__content">
        <p><strong>区间分析</strong>将前区号码 1-35 分为 5 个区间：1-7、8-14、15-21、22-28、29-35。</p>
        <p>通过分析每个区间在每期开奖中出现的号码数量，可以发现某些区间在某段时间内活跃或冷清。</p>
        <p><strong>应用建议：</strong>选择号码时注意号码在各个区间的分布，避免过度集中或过度分散。</p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, computed } from 'vue'
import * as echarts from 'echarts'
import { Grid, InfoFilled } from '@element-plus/icons-vue'
import { analysisApi } from '@/api'

const chartRef = ref<HTMLElement>()
let chartInstance: echarts.ECharts | null = null
const zoneData = ref<Record<string, Record<string, number>>>({})

interface ZoneTableRow {
  zone: string
  label: string
  avg: string
  max: string
  min: string
}

const zoneLabels: Record<string, string> = {
  '1-7': '1-7',
  '8-14': '8-14',
  '15-21': '15-21',
  '22-28': '22-28',
  '29-35': '29-35'
}

const zoneTableData = computed((): ZoneTableRow[] => {
  const data = zoneData.value
  if (!data || Object.keys(data).length === 0) {
    return []
  }
  
  const result: ZoneTableRow[] = []
  const zones = ['1-7', '8-14', '15-21', '22-28', '29-35']
  
  for (const zone of zones) {
    const counts = data[zone]
    if (!counts) continue
    
    const values = Object.values(counts)
    const numValues = values.filter(v => typeof v === 'number')
    const max = Math.max(...numValues)
    const min = Math.min(...numValues)
    const avg = numValues.length > 0 
      ? (numValues.reduce((a, b) => a + b, 0) / numValues.length).toFixed(2)
      : '0'
    
    result.push({
      zone,
      label: zoneLabels[zone] || zone,
      avg,
      max: String(max),
      min: String(min)
    })
  }
  
  return result
})

async function fetchData() {
  try {
    const result = await analysisApi.getZoneDistribution()
    zoneData.value = result || {}
    renderChart()
  } catch (error) {
    console.error('获取区间数据失败:', error)
  }
}

function renderChart() {
  if (!chartRef.value) return

  if (!chartInstance) {
    chartInstance = echarts.init(chartRef.value)
  }

  const data = zoneData.value
  if (!data || Object.keys(data).length === 0) {
    // 渲染空状态
    chartInstance.setOption({
      title: {
        text: '暂无数据',
        left: 'center',
        top: 'center',
        textStyle: { color: '#64748b' }
      }
    })
    return
  }

  const zones = Object.keys(data)
  const seriesData = zones.map(zone => {
    const counts = data[zone]
    const values = Object.values(counts).filter(v => typeof v === 'number')
    return {
      name: zone,
      type: 'bar',
      data: values,
      label: { show: false }
    }
  })

  // 生成 X 轴期数标签
  const maxPeriods = Math.max(...zones.map(z => Object.keys(data[z] || {}).length))
  const periodLabels = Array.from({ length: Math.min(maxPeriods, 20) }, (_, i) => `期${i + 1}`)

  const option: echarts.Option = {
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'shadow' }
    },
    legend: {
      data: zones,
      textStyle: { color: '#a0aec0' },
      top: 0
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: periodLabels,
      axisLabel: { color: '#a0aec0' },
      axisLine: { lineStyle: { color: '#2a3a5a' } }
    },
    yAxis: {
      type: 'value',
      name: '出现次数',
      axisLabel: { color: '#a0aec0' },
      splitLine: { lineStyle: { color: '#2a3a5a' } }
    },
    series: seriesData
  }

  chartInstance.setOption(option)
}

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
  height: 350px;
}

.zone-table-wrap {
  margin-top: 20px;
}

.zone-table {
  --el-table-bg-color: transparent;
  --el-table-tr-bg-color: rgba(26, 26, 46, 0.6);
  --el-table-header-bg-color: rgba(26, 26, 46, 0.9);
  --el-table-border-color: #2a3a5a;
  --el-table-text-color: #a0aec0;
  --el-table-header-text-color: #ffffff;
  
  &::before {
    display: none;
  }
}

.info-card {
  background: #16213e;
  border: 1px solid #2a3a5a;
  border-radius: 14px;
  padding: 16px 20px;

  &__title {
    display: flex;
    align-items: center;
    gap: 8px;
    color: #00d4aa;
    font-size: 14px;
    font-weight: 600;
    margin-bottom: 12px;
  }

  &__content {
    p {
      margin: 0 0 8px;
      color: #a0aec0;
      font-size: 13px;
      line-height: 1.6;

      &:last-child {
        margin-bottom: 0;
      }
    }
  }
}
</style>
