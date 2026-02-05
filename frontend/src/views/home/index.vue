<template>
  <div class="home-page">
    <!-- 欢迎区域 -->
    <section class="welcome-section">
      <div class="welcome-content">
        <h1 class="welcome-title">
          <span class="gradient-text">大乐透数据分析与预测系统</span>
        </h1>
        <p class="welcome-subtitle">基于数据驱动的智能预测分析平台</p>
      </div>
    </section>

    <!-- 最新开奖 -->
    <section class="latest-draw">
      <div class="tech-card">
        <div class="tech-card__header">
          <div class="tech-card__title">
            <el-icon><Trophy /></el-icon>
            最新开奖结果
          </div>
          <el-tag type="success" effect="dark">第 {{ latest?.issue || '--' }} 期</el-tag>
        </div>
        <div class="tech-card__body">
          <div v-if="latest" class="draw-result">
            <div class="draw-balls">
              <div class="balls-section">
                <span class="zone-label">前区</span>
                <div class="balls-row">
                  <span
                    v-for="ball in frontBalls"
                    :key="ball"
                    class="ball ball--front ball--lg"
                  >{{ ball }}</span>
                </div>
              </div>
              <span class="zone-separator">+</span>
              <div class="balls-section">
                <span class="zone-label">后区</span>
                <div class="balls-row">
                  <span
                    v-for="ball in backBalls"
                    :key="ball"
                    class="ball ball--back ball--lg"
                  >{{ ball }}</span>
                </div>
              </div>
            </div>
            <div class="draw-info">
              <div class="info-item">
                <span class="info-label">开奖日期</span>
                <span class="info-value">{{ latest.drawDate }}</span>
              </div>
              <div class="info-item">
                <span class="info-label">前区和值</span>
                <span class="info-value stat-number stat-number--small">{{ latest.frontSum }}</span>
              </div>
              <div class="info-item">
                <span class="info-label">奇偶比</span>
                <span class="info-value">{{ latest.oddCountFront }}:{{ 5 - latest.oddCountFront }}</span>
              </div>
              <div class="info-item">
                <span class="info-label">连号数</span>
                <span class="info-value">{{ latest.consecutiveCountFront }}</span>
              </div>
            </div>
          </div>
          <div v-else class="empty-state">
            <el-icon size="48" color="#666"><Warning /></el-icon>
            <p>暂无开奖数据</p>
          </div>
        </div>
      </div>
    </section>

    <!-- 统计数据 -->
    <section class="stats-section">
      <div class="stats-grid">
        <div class="stat-card">
          <div class="stat-icon">
            <el-icon><Document /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-number">{{ stats.totalCount || 0 }}</div>
            <div class="stat-label">历史数据</div>
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-icon accent">
            <el-icon><Right /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-number">{{ stats.nextIssue || '--' }}</div>
            <div class="stat-label">下期预测</div>
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-icon purple">
            <el-icon><Cpu /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-number">{{ stats.methodCount || 5 }}</div>
            <div class="stat-label">预测算法</div>
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-icon green">
            <el-icon><DataAnalysis /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-number">{{ stats.statsCount || 0 }}</div>
            <div class="stat-label">已统计方法</div>
          </div>
        </div>
      </div>
    </section>

    <!-- 快速入口 -->
    <section class="quick-actions">
      <div class="action-grid">
        <div class="action-card" @click="$router.push('/prediction')">
          <div class="action-icon">
            <el-icon><MagicStick /></el-icon>
          </div>
          <h3>智能预测</h3>
          <p>多算法融合生成预测号码</p>
          <el-button type="primary" plain>立即预测</el-button>
        </div>
        <div class="action-card" @click="$router.push('/analysis')">
          <div class="action-icon">
            <el-icon><TrendCharts /></el-icon>
          </div>
          <h3>数据分析</h3>
          <p>频率、遗漏、走势深度分析</p>
          <el-button type="success" plain>查看分析</el-button>
        </div>
        <div class="action-card" @click="$router.push('/verification')">
          <div class="action-icon">
            <el-icon><CircleCheck /></el-icon>
          </div>
          <h3>结果验证</h3>
          <p>验证预测准确率统计</p>
          <el-button type="warning" plain>开始验证</el-button>
        </div>
        <div class="action-card" @click="$router.push('/lottery')">
          <div class="action-icon">
            <el-icon><FolderOpened /></el-icon>
          </div>
          <h3>数据管理</h3>
          <p>开奖数据录入与管理</p>
          <el-button type="info" plain>管理数据</el-button>
        </div>
      </div>
    </section>

    <!-- 近期开奖 + 图表 -->
    <section class="charts-section">
      <div class="charts-grid">
        <div class="tech-card">
          <div class="tech-card__header">
            <div class="tech-card__title">
              <el-icon><List /></el-icon>
              近期开奖
            </div>
          </div>
          <div class="tech-card__body">
            <div class="recent-table">
              <div class="table-header">
                <span>期号</span>
                <span>前区号码</span>
                <span>后区</span>
              </div>
              <div class="table-body">
                <div v-for="item in recentResults" :key="item.issue" class="table-row">
                  <span class="issue">{{ item.issue }}</span>
                  <div class="balls-mini">
                    <span
                      v-for="n in [item.frontBall1, item.frontBall2, item.frontBall3, item.frontBall4, item.frontBall5]"
                      :key="n"
                      class="ball ball--front ball--sm"
                    >{{ n }}</span>
                  </div>
                  <div class="balls-mini">
                    <span
                      v-for="n in [item.backBall1, item.backBall2]"
                      :key="n"
                      class="ball ball--back ball--sm"
                    >{{ n }}</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <div class="tech-card">
          <div class="tech-card__header">
            <div class="tech-card__title">
              <el-icon><PieChart /></el-icon>
              奇偶比分布
            </div>
          </div>
          <div class="tech-card__body">
            <div ref="chartRef" class="chart-container"></div>
          </div>
        </div>
      </div>
    </section>

    <!-- 预测准确率 -->
    <section v-if="accuracyStats.length" class="accuracy-section">
      <div class="tech-card">
        <div class="tech-card__header">
          <div class="tech-card__title">
            <el-icon><DataBoard /></el-icon>
            预测准确率统计
          </div>
        </div>
        <div class="tech-card__body">
          <el-table :data="accuracyStats" class="accuracy-table" style="width: 100%">
            <el-table-column prop="methodName" label="预测方法" width="180" />
            <el-table-column prop="totalPredictions" label="预测次数" width="100" align="center" />
            <el-table-column prop="frontAvgHit" label="前区命中" width="100" align="center">
              <template #default="{ row }">
                <span class="hit-rate">{{ row.frontAvgHit.toFixed(2) }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="backAvgHit" label="后区命中" width="100" align="center">
              <template #default="{ row }">
                <span class="hit-rate">{{ row.backAvgHit.toFixed(2) }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="totalPrizeCount" label="中奖次数" width="100" align="center" />
          </el-table>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import * as echarts from 'echarts'
import {
  Trophy, Warning, Document, Right, Cpu, DataAnalysis,
  MagicStick, TrendCharts, CircleCheck, FolderOpened,
  List, PieChart, DataBoard
} from '@element-plus/icons-vue'
import { lotteryApi, analysisApi } from '@/api'
import type { LotteryResult, AccuracyStats as AccuracyStatsType } from '@/types'

const latest = ref<LotteryResult | null>(null)
const recentResults = ref<LotteryResult[]>([])
const accuracyStats = ref<AccuracyStatsType[]>([])
const stats = ref({
  totalCount: 0,
  nextIssue: '--',
  methodCount: 10,
  statsCount: 0
})

const chartRef = ref<HTMLElement>()
let chartInstance: echarts.ECharts | null = null

const frontBalls = computed(() => {
  if (!latest.value) return []
  return [
    latest.value.frontBall1,
    latest.value.frontBall2,
    latest.value.frontBall3,
    latest.value.frontBall4,
    latest.value.frontBall5
  ]
})

const backBalls = computed(() => {
  if (!latest.value) return []
  return [latest.value.backBall1, latest.value.backBall2]
})

async function fetchData() {
  try {
    const [latestRes, recentRes, statsRes, oddEvenStats, accuracyRes] = await Promise.all([
      lotteryApi.getLatest().catch(() => null),
      lotteryApi.getRecent(10).catch(() => null),
      lotteryApi.getStats().catch(() => null),
      analysisApi.getOddEvenStats().catch(() => null),
      analysisApi.getSumStats().catch(() => null)
    ])

    if (latestRes) latest.value = latestRes
    if (recentRes) recentResults.value = recentRes
    if (statsRes) stats.value = statsRes as any

    // 准确率数据从验证接口获取
    try {
      const { verificationApi } = await import('@/api')
      const accuracyRes = await verificationApi.getAccuracyStats()
      accuracyStats.value = accuracyRes
    } catch {
      // 静默处理
    }

    // 渲染图表
    if (oddEvenStats) {
      renderChart(oddEvenStats)
    }
  } catch (error) {
    console.error('获取数据失败:', error)
  }
}

function renderChart(data: Record<string, number>) {
  if (!chartRef.value) return

  chartInstance = echarts.init(chartRef.value)

  const chartData = Object.entries(data).map(([name, value]) => ({
    name: `奇偶比 ${name}`,
    value
  }))

  const option: echarts.Option = {
    tooltip: {
      trigger: 'item',
      formatter: '{b}: {c}期 ({d}%)'
    },
    legend: {
      orient: 'vertical',
      right: 10,
      top: 'center',
      textStyle: { color: '#fff' }
    },
    series: [{
      type: 'pie',
      radius: ['40%', '70%'],
      center: ['40%', '50%'],
      avoidLabelOverlap: false,
      itemStyle: {
        borderRadius: 8,
        borderColor: '#111128',
        borderWidth: 2
      },
      label: { show: false },
      emphasis: {
        label: {
          show: true,
          fontSize: 14,
          fontWeight: 'bold'
        }
      },
      data: chartData,
      color: ['#667eea', '#f4a261', '#2a9d8f', '#457b9d', '#1d3557', '#264653']
    }]
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
@use '@/assets/styles/variables.scss' as *;

.home-page {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

// 欢迎区域
.welcome-section {
  text-align: center;
  padding: 32px 0;
}

.welcome-title {
  font-size: 32px;
  font-weight: 700;
  margin: 0 0 12px;
}

.gradient-text {
  background: linear-gradient(135deg, $primary-color, $accent-cyan, $accent-purple);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.welcome-subtitle {
  font-size: 16px;
  color: $text-muted;
  margin: 0;
}

// 最新开奖
.draw-result {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.draw-balls {
  display: flex;
  flex-direction: row;
  align-items: center;
  justify-content: center;
  gap: 16px;
}

.balls-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}

.zone-separator {
  font-size: 28px;
  font-weight: 700;
  color: $text-muted;
  margin-top: 20px;
}

.zone-label {
  font-size: 14px;
  color: $text-muted;
  text-transform: uppercase;
}

.balls-row {
  display: flex;
  gap: 12px;
}

.draw-info {
  display: flex;
  justify-content: center;
  gap: 48px;
  padding-top: 16px;
  border-top: 1px solid $border-color;
}

.info-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
}

.info-label {
  font-size: 12px;
  color: $text-muted;
}

.info-value {
  font-size: 16px;
  font-weight: 600;
  color: $text-primary;
}

// 统计卡片
.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
}

.stat-card {
  background: $bg-card;
  border: 1px solid $border-color;
  border-radius: $radius-lg;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  transition: all $transition-normal;

  &:hover {
    border-color: $primary-color;
    transform: translateY(-2px);
  }
}

.stat-icon {
  width: 56px;
  height: 56px;
  border-radius: $radius-lg;
  background: linear-gradient(135deg, rgba($primary-color, 0.2), rgba($primary-light, 0.2));
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  color: $primary-color;

  &.accent {
    background: linear-gradient(135deg, rgba($accent-cyan, 0.2), rgba($accent-green, 0.2));
    color: $accent-cyan;
  }

  &.purple {
    background: linear-gradient(135deg, rgba($accent-purple, 0.2), rgba($primary-light, 0.2));
    color: $accent-purple;
  }

  &.green {
    background: linear-gradient(135deg, rgba($accent-green, 0.2), rgba($primary-color, 0.2));
    color: $accent-green;
  }
}

.stat-content {
  flex: 1;
}

.stat-label {
  font-size: 14px;
  color: $text-muted;
  margin-top: 4px;
}

// 快速入口
.action-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
}

.action-card {
  background: $bg-card;
  border: 1px solid $border-color;
  border-radius: $radius-lg;
  padding: 24px;
  text-align: center;
  cursor: pointer;
  transition: all $transition-normal;

  &:hover {
    border-color: $primary-color;
    transform: translateY(-4px);
    box-shadow: $shadow-glow;
  }

  h3 {
    font-size: 18px;
    font-weight: 600;
    margin: 16px 0 8px;
    color: $text-primary;
  }

  p {
    font-size: 14px;
    color: $text-muted;
    margin-bottom: 16px;
  }
}

.action-icon {
  width: 64px;
  height: 64px;
  border-radius: 50%;
  background: linear-gradient(135deg, rgba($primary-color, 0.2), rgba($primary-light, 0.2));
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto;
  font-size: 28px;
  color: $primary-color;
}

// 图表区域
.charts-section {
  .charts-grid {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 20px;
  }
}

.chart-container {
  height: 300px;
}

// 近期开奖表格
.recent-table {
  background: rgba($bg-dark-lighter, 0.4);
  border-radius: $radius-md;
  padding: 16px;

  .table-header {
    display: grid;
    grid-template-columns: 100px 1fr 100px;
    gap: 16px;
    padding: 12px 0;
    border-bottom: 1px solid $border-color;
    font-size: 14px;
    color: $text-muted;
  }

  .table-body {
    .table-row {
      display: grid;
      grid-template-columns: 100px 1fr 100px;
      gap: 16px;
      padding: 12px 0;
      border-bottom: 1px solid rgba($border-color, 0.5);
      align-items: center;

      &:last-child {
        border-bottom: none;
      }
    }

    .issue {
      font-family: $font-mono;
      font-size: 14px;
      color: $text-secondary;
    }
  }
}

.balls-mini {
  display: flex;
  gap: 4px;
}

// 准确率
.accuracy-table {
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

.hit-rate {
  font-family: $font-mono;
  color: $accent-cyan;
}

// 响应式
@media (max-width: $lg) {
  .stats-grid,
  .action-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .charts-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: $md) {
  .stats-grid,
  .action-grid {
    grid-template-columns: 1fr;
  }

  .draw-info {
    flex-wrap: wrap;
    gap: 24px;
  }
}
</style>
