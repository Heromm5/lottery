<template>
  <div class="digit-page">
    <!-- 尾数频率统计卡片 -->
    <div class="tech-card">
      <div class="tech-card__header">
        <div class="tech-card__title">
          <el-icon><DataLine /></el-icon>
          尾数频率统计
        </div>
        <el-radio-group v-model="zone" size="small">
          <el-radio-button value="front">前区</el-radio-button>
          <el-radio-button value="back">后区</el-radio-button>
        </el-radio-group>
      </div>
      <div class="tech-card__body">
        <div ref="chartRef" class="chart-container"></div>
        
        <!-- 尾数详情卡片 -->
        <div class="digit-cards">
          <div 
            v-for="item in digitData" 
            :key="item.digit" 
            class="digit-card"
            :class="{ 'digit-card--hot': item.frequency > 12 }"
          >
            <div class="digit-card__value">{{ item.digit }}</div>
            <div class="digit-card__count">{{ item.count }}次</div>
            <div class="digit-card__freq">{{ item.frequency.toFixed(1) }}%</div>
            <div class="digit-card__missing">遗漏 {{ item.missing }}期</div>
          </div>
        </div>
      </div>
    </div>

    <!-- 尾数和值统计卡片 -->
    <div class="tech-card">
      <div class="tech-card__header">
        <div class="tech-card__title">
          <el-icon><TrendCharts /></el-icon>
          尾数和值分布
        </div>
      </div>
      <div class="tech-card__body">
        <div ref="sumChartRef" class="chart-container"></div>
      </div>
    </div>

    <!-- 说明卡片 -->
    <div class="info-card">
      <div class="info-card__title">
        <el-icon><InfoFilled /></el-icon>
        说明
      </div>
      <div class="info-card__content">
        <p><strong>尾数</strong>指号码的个位数，例如 05、15、25、35 的尾数都是 5。</p>
        <p><strong>尾数和值</strong>指前区 5 个号码的尾数之和，范围 0-45。</p>
        <p>通过尾数分析可以发现某些尾数的出现规律，帮助选号。</p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, watch, onMounted, onUnmounted } from 'vue'
import * as echarts from 'echarts'
import { DataLine, TrendCharts, InfoFilled } from '@element-plus/icons-vue'
import { analysisApi, type DigitFrequencyDTO } from '@/api'

const zone = ref<'front' | 'back'>('front')
const chartRef = ref<HTMLElement>()
const sumChartRef = ref<HTMLElement>()
const digitData = ref<DigitFrequencyDTO[]>([])
let chartInstance: echarts.ECharts | null = null
let sumChartInstance: echarts.ECharts | null = null

async function fetchDigitData() {
  try {
    const result = await analysisApi.getDigitFrequency(zone.value)
    digitData.value = result || []
    renderChart()
    renderSumChart()
  } catch (error) {
    console.error('获取尾数数据失败:', error)
    digitData.value = []
  }
}

async function fetchDigitSumData() {
  try {
    const result = await analysisApi.getDigitSumStats()
    renderSumChart(result)
  } catch (error) {
    console.error('获取尾数和值数据失败:', error)
  }
}

function renderChart() {
  if (!chartRef.value) return

  if (!chartInstance) {
    chartInstance = echarts.init(chartRef.value)
  }

  const data = digitData.value.length > 0 ? digitData.value : 
    Array.from({ length: 10 }, (_, i) => ({ digit: i, count: 0, frequency: 0, missing: 0 }))

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
      data: data.map(item => `尾${item.digit}`),
      axisLabel: { color: '#a0aec0' },
      axisLine: { lineStyle: { color: '#2a3a5a' } }
    },
    yAxis: {
      type: 'value',
      axisLabel: { color: '#a0aec0' },
      splitLine: { lineStyle: { color: '#2a3a5a' } }
    },
    series: [{
      type: 'bar',
      data: data.map(item => ({
        value: item.count,
        itemStyle: {
          color: item.frequency > 12 ? '#ef4444' : '#4a90d9'
        }
      })),
      barWidth: '50%',
      label: {
        show: true,
        position: 'top',
        color: '#a0aec0',
        formatter: '{c}'
      }
    }]
  }

  chartInstance.setOption(option)
}

function renderSumChart(sumData?: Record<string, number>) {
  if (!sumChartRef.value) return

  if (!sumChartInstance) {
    sumChartInstance = echarts.init(sumChartRef.value)
  }

  const data = sumData || {}
  const ranges = Object.keys(data).length > 0 ? Object.keys(data) : ['0-9', '10-19', '20-29', '30-39', '40-45']
  const values = Object.keys(data).length > 0 ? Object.values(data) : [0, 0, 0, 0, 0]

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
      data: ranges,
      axisLabel: { color: '#a0aec0' },
      axisLine: { lineStyle: { color: '#2a3a5a' } }
    },
    yAxis: {
      type: 'value',
      axisLabel: { color: '#a0aec0' },
      splitLine: { lineStyle: { color: '#2a3a5a' } }
    },
    series: [{
      type: 'bar',
      data: values,
      barWidth: '50%',
      itemStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: '#4a90d9' },
          { offset: 1, color: '#6ba3e0' }
        ])
      }
    }]
  }

  sumChartInstance.setOption(option)
}

watch(zone, fetchDigitData)

onMounted(() => {
  fetchDigitData()
  fetchDigitSumData()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  chartInstance?.dispose()
  sumChartInstance?.dispose()
})

function handleResize() {
  chartInstance?.resize()
  sumChartInstance?.resize()
}
</script>

<style lang="scss" scoped>
.chart-container {
  height: 300px;
}

.digit-cards {
  display: grid;
  grid-template-columns: repeat(10, 1fr);
  gap: 12px;
  margin-top: 20px;
}

.digit-card {
  background: rgba(26, 26, 46, 0.8);
  border: 1px solid #2a3a5a;
  border-radius: 10px;
  padding: 12px 8px;
  text-align: center;
  transition: all 0.25s ease;

  &:hover {
    border-color: #4a90d9;
    transform: translateY(-2px);
  }

  &--hot {
    border-color: #ef4444;
    background: rgba(239, 68, 68, 0.1);
  }

  &__value {
    font-size: 20px;
    font-weight: 700;
    color: #4a90d9;
    margin-bottom: 4px;
  }

  &__count {
    font-size: 12px;
    color: #a0aec0;
    margin-bottom: 2px;
  }

  &__freq {
    font-size: 14px;
    font-weight: 600;
    color: #00d4aa;
    margin-bottom: 2px;
  }

  &__missing {
    font-size: 11px;
    color: #64748b;
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

@media (max-width: 768px) {
  .digit-cards {
    grid-template-columns: repeat(5, 1fr);
  }
}
</style>
