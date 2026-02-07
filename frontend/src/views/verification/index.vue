<template>
  <div class="verification-page">
    <div class="page-header">
      <h1>验证中心</h1>
      <p>查看预测验证结果、准确率排行榜及历史回测</p>
    </div>

    <!-- 手动验证卡片 -->
    <div class="tech-card">
      <div class="tech-card__header">
        <div class="tech-card__title">
          <el-icon><RefreshRight /></el-icon>
          手动验证
        </div>
      </div>
      <div class="tech-card__body">
        <div class="verify-actions">
          <el-select v-model="selectedIssue" placeholder="选择要验证的期号" clearable filterable style="width: 200px">
            <el-option
              v-for="issue in unverifiedIssues"
              :key="issue"
              :label="issue"
              :value="issue"
            />
          </el-select>
          <el-button
            type="primary"
            :loading="verifying"
            :disabled="!selectedIssue"
            @click="handleVerify"
          >
            开始验证
          </el-button>
          <el-button @click="fetchUnverifiedIssues">
            刷新期号列表
          </el-button>
        </div>
        <div class="verify-hint" v-if="unverifiedIssues.length === 0">
          暂无待验证的期号（所有预测已验证完成）
        </div>
      </div>
    </div>

    <!-- 准确率排行榜卡片 -->
    <div class="tech-card">
      <div class="tech-card__header">
        <div class="tech-card__title">
          <el-icon><Trophy /></el-icon>
          算法排行榜
        </div>
        <div class="ranking-controls">
          <el-radio-group v-model="sortBy" size="small" @change="fetchRanking">
            <el-radio-button value="composite">综合得分</el-radio-button>
            <el-radio-button value="hit">平均命中</el-radio-button>
            <el-radio-button value="prize">中奖率</el-radio-button>
            <el-radio-button value="high">高等奖</el-radio-button>
          </el-radio-group>
          <el-button type="primary" size="small" :icon="Refresh" @click="fetchRanking" :loading="rankingLoading">
            刷新
          </el-button>
        </div>
      </div>
      <div class="tech-card__body">
        <el-table :data="rankingStats" class="verify-table" style="width: 100%" v-loading="rankingLoading">
          <el-table-column label="排名" width="60" align="center">
            <template #default="{ row, $index }">
              <div class="rank-cell">
                <span v-if="row.rank === 1" class="rank-badge rank-1">{{ row.rank }}</span>
                <span v-else-if="row.rank === 2" class="rank-badge rank-2">{{ row.rank }}</span>
                <span v-else-if="row.rank === 3" class="rank-badge rank-3">{{ row.rank }}</span>
                <span v-else class="rank-badge">{{ row.rank }}</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="methodName" label="预测方法" width="90" />
          <el-table-column prop="totalPredictions" label="预测次数" width="80" align="center" />
          <el-table-column label="前区命中" width="80" align="center">
            <template #default="{ row }">
              <span class="hit-rate">{{ (row.frontHitRate || 0).toFixed(1) }}%</span>
            </template>
          </el-table-column>
          <el-table-column label="后区命中" width="80" align="center">
            <template #default="{ row }">
              <span class="hit-rate">{{ (row.backHitRate || 0).toFixed(1) }}%</span>
            </template>
          </el-table-column>
          <el-table-column prop="totalPrizeCount" label="中奖次数" width="80" align="center" />
          <el-table-column label="中奖率" width="75" align="center">
            <template #default="{ row }">
              <span :class="['prize-rate', { 'high-rate': row.prizeRate >= 8, 'medium-rate': row.prizeRate >= 5 && row.prizeRate < 8 }]">
                {{ (row.prizeRate || 0).toFixed(1) }}%
              </span>
            </template>
          </el-table-column>
          <el-table-column label="高等奖" width="70" align="center">
            <template #default="{ row }">
              <span class="high-prize">{{ row.highPrizeCount || 0 }}</span>
            </template>
          </el-table-column>
          <el-table-column label="综合得分" width="85" align="center">
            <template #default="{ row }">
              <span class="composite-score">{{ (row.compositeScore || 0).toFixed(1) }}</span>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </div>

    <!-- 历史回测卡片 -->
    <div class="tech-card">
      <div class="tech-card__header">
        <div class="tech-card__title">
          <el-icon><Histogram /></el-icon>
          历史回测
        </div>
        <div class="backtest-controls">
          <el-select v-model="backtestMethod" placeholder="选择算法" clearable style="width: 150px">
            <el-option
              v-for="method in PREDICTION_METHODS"
              :key="method.code"
              :label="method.displayName"
              :value="method.code"
            />
          </el-select>
          <el-input-number v-model="backtestIssueCount" :min="10" :max="200" :step="10" style="width: 120px" />
          <span class="control-label">期</span>
          <el-input-number v-model="backtestPredictions" :min="1" :max="20" :step="1" style="width: 100px" />
          <span class="control-label">注/期</span>
          <el-button type="primary" :loading="backtestLoading" @click="runBacktest" :disabled="backtestLoading">
            开始回测
          </el-button>
        </div>
      </div>
      <div class="tech-card__body">
        <!-- 回测结果概览 -->
        <div v-if="backtestResults.length > 0" class="backtest-overview">
          <el-row :gutter="16">
            <el-col :span="6" v-for="(result, index) in backtestResults.slice(0, 4)" :key="result.method">
              <div class="backtest-card" :class="{ 'backtest-best': index === 0 }">
                <div class="backtest-rank">#{{ index + 1 }}</div>
                <div class="backtest-method">{{ result.methodName }}</div>
                <div class="backtest-roi" :class="result.roi >= 0 ? 'roi-positive' : 'roi-negative'">
                  {{ result.roi >= 0 ? '+' : '' }}{{ result.roi.toFixed(1) }}%
                </div>
                <div class="backtest-label">ROI</div>
                <div class="backtest-stats">
                  <span>中奖率: {{ result.prizeRate.toFixed(1) }}%</span>
                  <span>高等奖: {{ result.highPrizeCount }}</span>
                </div>
              </div>
            </el-col>
          </el-row>
        </div>

        <!-- 回测详细结果 -->
        <el-collapse v-if="backtestResults.length > 0" v-model="activeBacktest">
          <el-collapse-item
            v-for="result in backtestResults"
            :key="result.method"
            :name="result.method"
          >
            <template #title>
              <div class="backtest-title">
                <span class="backtest-title-method">{{ result.methodName }}</span>
                <el-tag :type="result.roi >= 0 ? 'success' : 'danger'" size="small">
                  ROI {{ result.roi >= 0 ? '+' : '' }}{{ result.roi.toFixed(1) }}%
                </el-tag>
                <span class="backtest-title-prize">中奖率 {{ result.prizeRate.toFixed(1) }}%</span>
              </div>
            </template>
            
            <div class="backtest-detail">
              <el-alert :type="result.roi >= 0 ? 'success' : 'info'" :closable="false">
                {{ result.evaluation }}
              </el-alert>
              
              <el-descriptions :column="4" border class="backtest-desc">
                <el-descriptions-item label="回测期数">{{ result.totalIssues }} 期</el-descriptions-item>
                <el-descriptions-item label="总预测">{{ result.totalPredictions }} 注</el-descriptions-item>
                <el-descriptions-item label="总投入">{{ result.totalCost }} 元</el-descriptions-item>
                <el-descriptions-item label="理论奖金">{{ result.totalPrizeMoney.toLocaleString() }} 元</el-descriptions-item>
                <el-descriptions-item label="盈亏">
                  <span :class="result.profitLoss >= 0 ? 'profit' : 'loss'">
                    {{ result.profitLoss >= 0 ? '+' : '' }}{{ result.profitLoss.toLocaleString() }} 元
                  </span>
                </el-descriptions-item>
                <el-descriptions-item label="平均前区命中">{{ result.avgFrontHit.toFixed(2) }}</el-descriptions-item>
                <el-descriptions-item label="平均后区命中">{{ result.avgBackHit.toFixed(2) }}</el-descriptions-item>
                <el-descriptions-item label="最佳表现">
                  {{ result.bestIssue ? result.bestIssue + ' ' + result.bestPrize : '无' }}
                </el-descriptions-item>
              </el-descriptions>

              <div class="prize-distribution">
                <div class="prize-item">
                  <span class="prize-label">一等奖</span>
                  <span class="prize-count">{{ result.prizeCount1 }}</span>
                </div>
                <div class="prize-item">
                  <span class="prize-label">二等奖</span>
                  <span class="prize-count">{{ result.prizeCount2 }}</span>
                </div>
                <div class="prize-item">
                  <span class="prize-label">三等奖</span>
                  <span class="prize-count">{{ result.prizeCount3 }}</span>
                </div>
                <div class="prize-item">
                  <span class="prize-label">四等奖</span>
                  <span class="prize-count">{{ result.prizeCount4 }}</span>
                </div>
                <div class="prize-item">
                  <span class="prize-label">五等奖</span>
                  <span class="prize-count">{{ result.prizeCount5 }}</span>
                </div>
                <div class="prize-item">
                  <span class="prize-label">六等奖</span>
                  <span class="prize-count">{{ result.prizeCount6 }}</span>
                </div>
                <div class="prize-item">
                  <span class="prize-label">七等奖</span>
                  <span class="prize-count">{{ result.prizeCount7 }}</span>
                </div>
              </div>

              <!-- 中奖详情 -->
              <el-table v-if="result.details && result.details.length > 0" :data="result.details" size="small" class="backtest-detail-table">
                <el-table-column prop="issue" label="期号" width="100" />
                <el-table-column prop="prediction" label="预测号码" />
                <el-table-column prop="actualResult" label="开奖号码" />
                <el-table-column label="命中" width="80" align="center">
                  <template #default="{ row }">
                    {{ row.frontHit }}/{{ row.backHit }}
                  </template>
                </el-table-column>
                <el-table-column prop="prizeLevel" label="奖项" width="100" align="center">
                  <template #default="{ row }">
                    <el-tag v-if="row.prizeLevel !== '未中奖'" type="success" size="small">{{ row.prizeLevel }}</el-tag>
                    <span v-else>-</span>
                  </template>
                </el-table-column>
                <el-table-column prop="prizeMoney" label="奖金" width="120" align="right">
                  <template #default="{ row }">
                    {{ row.prizeMoney.toLocaleString() }} 元
                  </template>
                </el-table-column>
              </el-table>
            </div>
          </el-collapse-item>
        </el-collapse>

        <el-empty v-else description="点击开始回测按钮进行历史数据回测" />
      </div>
    </div>

    <!-- 验证历史记录 -->
    <div class="tech-card">
      <div class="tech-card__header">
        <div class="tech-card__title">
          <el-icon><List /></el-icon>
          验证历史记录
        </div>
        <el-button type="primary" size="small" :icon="Refresh" @click="fetchHistory">
          刷新
        </el-button>
      </div>
      <div class="tech-card__body">
        <el-table :data="historyList" class="verify-table" style="width: 100%" v-loading="historyLoading">
          <el-table-column prop="targetIssue" label="期号" align="center" />
          <el-table-column prop="methodName" label="方法" />
          <el-table-column label="预测号码" align="center">
            <template #default="{ row }">
              <div class="balls-compare">
                <div class="balls-cell balls-cell--front">
                  <span
                    v-for="n in parseBalls(row.frontBallsStr)"
                    :key="'pred-front-' + n"
                    class="ball ball--front ball--xs"
                    :class="{ 'ball--hit': isHit(row.actualFrontBallsStr, n) }"
                  >{{ n }}</span>
                </div>
                <span class="balls-separator">+</span>
                <div class="balls-cell balls-cell--back">
                  <span
                    v-for="n in parseBalls(row.backBallsStr)"
                    :key="'pred-back-' + n"
                    class="ball ball--back ball--xs"
                    :class="{ 'ball--hit': isHit(row.actualBackBallsStr, n) }"
                  >{{ n }}</span>
                </div>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="开奖号码" align="center">
            <template #default="{ row }">
              <div class="balls-compare">
                <div class="balls-cell balls-cell--front">
                  <span
                    v-for="n in parseBalls(row.actualFrontBallsStr)"
                    :key="'actual-front-' + n"
                    class="ball ball--front ball--xs"
                  >{{ n }}</span>
                </div>
                <span class="balls-separator">+</span>
                <div class="balls-cell balls-cell--back">
                  <span
                    v-for="n in parseBalls(row.actualBackBallsStr)"
                    :key="'actual-back-' + n"
                    class="ball ball--back ball--xs"
                  >{{ n }}</span>
                </div>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="命中" align="center">
            <template #default="{ row }">
              <span class="hit-count">
                {{ row.frontHitCount || 0 }}/{{ row.backHitCount || 0 }}
              </span>
            </template>
          </el-table-column>
          <el-table-column prop="prizeLevel" label="中奖" align="center">
            <template #default="{ row }">
              <el-tag
                v-if="row.prizeLevel && row.prizeLevel !== '未中奖'"
                type="success"
                size="small"
                effect="dark"
              >
                {{ row.prizeLevel }}
              </el-tag>
              <span v-else class="no-prize">未中奖</span>
            </template>
          </el-table-column>
          <el-table-column prop="verifiedAt" label="验证时间" />
        </el-table>

        <div class="pagination-wrap">
          <el-pagination
            v-model:current-page="historyPage"
            v-model:page-size="historySize"
            :page-sizes="[10, 20, 50]"
            :total="historyTotal"
            layout="total, sizes, prev, pager, next"
            @size-change="fetchHistory"
            @current-change="fetchHistory"
          />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { DataAnalysis, List, Refresh, RefreshRight, Trophy, Histogram } from '@element-plus/icons-vue'
import { verificationApi, type VerificationHistoryRecord } from '@/api'
import type { AccuracyStats, BacktestResult } from '@/types'
import { PREDICTION_METHODS, getMethodDisplayName } from '@/types'

const accuracyStats = ref<AccuracyStats[]>([])
const historyList = ref<VerificationHistoryRecord[]>([])
const historyLoading = ref(false)
const historyPage = ref(1)
const historySize = ref(20)
const historyTotal = ref(0)

// 排行榜相关
const rankingStats = ref<AccuracyStats[]>([])
const rankingLoading = ref(false)
const sortBy = ref('composite')

// 回测相关
const backtestResults = ref<BacktestResult[]>([])
const backtestLoading = ref(false)
const backtestMethod = ref('')
const backtestIssueCount = ref(50)
const backtestPredictions = ref(5)
const activeBacktest = ref<string[]>([])

// 手动验证相关
const unverifiedIssues = ref<string[]>([])
const selectedIssue = ref('')
const verifying = ref(false)

function parseBalls(ballsStr: string): number[] {
  if (!ballsStr || ballsStr === '-') return []
  return ballsStr.split(',').map(Number).filter(n => !isNaN(n))
}

function isHit(actualBallsStr: string, ball: number): boolean {
  if (!actualBallsStr || actualBallsStr === '-') return false
  const actualBalls = parseBalls(actualBallsStr)
  return actualBalls.includes(ball)
}

async function fetchStats() {
  try {
    const result = await verificationApi.getAccuracyStats()
    // axios拦截器已返回data字段
    accuracyStats.value = Array.isArray(result) ? result : []
  } catch (error) {
    console.error('获取准确率数据失败:', error)
  }
}

// 获取排行榜
async function fetchRanking() {
  rankingLoading.value = true
  try {
    const result = await verificationApi.getAccuracyRanking(sortBy.value, false)
    rankingStats.value = Array.isArray(result) ? result : []
  } catch (error) {
    console.error('获取排行榜失败:', error)
    ElMessage.error('获取排行榜失败')
  } finally {
    rankingLoading.value = false
  }
}

// 运行回测
async function runBacktest() {
  backtestLoading.value = true
  backtestResults.value = []
  try {
    const result = await verificationApi.runBacktest(
      backtestMethod.value || undefined,
      backtestIssueCount.value,
      backtestPredictions.value
    )
    backtestResults.value = Array.isArray(result) ? result : []
    if (backtestResults.value.length > 0) {
      ElMessage.success(`回测完成！共测试 ${backtestResults.value.length} 种算法`)
      // 默认展开第一个
      if (backtestResults.value[0]) {
        activeBacktest.value = [backtestResults.value[0].method]
      }
    } else {
      ElMessage.warning('回测结果为空')
    }
  } catch (error: any) {
    console.error('回测失败:', error)
    ElMessage.error(error?.message || '回测失败')
  } finally {
    backtestLoading.value = false
  }
}

// 获取未验证的期号列表
async function fetchUnverifiedIssues() {
  try {
    const result = await verificationApi.getUnverifiedIssues()
    unverifiedIssues.value = Array.isArray(result) ? result : []
  } catch (error) {
    console.error('获取未验证期号失败:', error)
  }
}

// 执行验证
async function handleVerify() {
  if (!selectedIssue.value) return

  verifying.value = true
  try {
    const result = await verificationApi.triggerVerify(selectedIssue.value)
    ElMessage.success(`验证完成！共验证 ${result?.length || 0} 条预测记录`)
    selectedIssue.value = ''
    // 刷新数据
    await Promise.all([
      fetchStats(),
      fetchHistory(),
      fetchUnverifiedIssues()
    ])
  } catch (error: any) {
    ElMessage.error(error?.message || '验证失败')
  } finally {
    verifying.value = false
  }
}

async function fetchHistory() {
  historyLoading.value = true
  try {
    const result = await verificationApi.getHistory(historyPage.value, historySize.value)
    // axios拦截器已返回data字段，result本身就是PageResult对象
    if (result && result.records) {
      historyList.value = result.records
      historyTotal.value = result.total || 0
    } else {
      historyList.value = []
      historyTotal.value = 0
    }
  } catch (error) {
    console.error('获取验证历史失败:', error)
    historyList.value = []
  } finally {
    historyLoading.value = false
  }
}

onMounted(() => {
  fetchStats()
  fetchHistory()
  fetchUnverifiedIssues()
  fetchRanking()
})
</script>

<style lang="scss" scoped>
@use '@/assets/styles/variables.scss' as *;

.verification-page {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.verify-actions {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.verify-hint {
  margin-top: 12px;
  color: $text-muted;
  font-size: 13px;
}

.page-header {
  h1 {
    font-size: 24px;
    font-weight: 700;
    margin: 0 0 8px;
    background: linear-gradient(90deg, $primary-color, $accent-cyan);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
  }

  p {
    color: $text-muted;
    margin: 0;
  }
}

.hit-rate {
  font-family: $font-mono;
  color: $accent-cyan;
  font-weight: 600;
}

.prize-rate {
  font-family: $font-mono;
  color: $text-secondary;
  font-weight: 600;

  &.high-rate {
    color: $accent-green;
    text-shadow: 0 0 10px rgba($accent-green, 0.5);
  }

  &.medium-rate {
    color: $accent-orange;
  }
}

.no-prize {
  color: $text-muted;
  font-size: 12px;
}

.hit-count {
  font-family: $font-mono;
  font-size: 12px;
  color: $text-secondary;
}

// Dark table styles
.verify-table {
  --el-table-bg-color: transparent;
  --el-table-tr-bg-color: rgba($bg-dark-lighter, 0.6);
  --el-table-header-bg-color: rgba($bg-dark-lighter, 0.8);
  --el-table-row-hover-bg-color: rgba($primary-color, 0.15);
  --el-table-border-color: $border-color;
  --el-table-text-color: $text-primary;
  --el-table-header-text-color: $text-secondary;
  --el-table-cell-height: 40px;

  background: transparent;
  font-size: 12px;

  &::before {
    display: none;
  }

  th.el-table__cell {
    background: rgba($bg-dark-lighter, 0.9);
    border-bottom: 1px solid $border-color;
    padding: 6px 0;
  }

  td.el-table__cell {
    border-bottom: 1px solid rgba($border-color, 0.4);
    padding: 4px 0;
  }

  tr:hover > td.el-table__cell {
    background: rgba($primary-color, 0.15) !important;
  }
}

// 号码对比样式
.balls-compare {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 2px;
}

.balls-separator {
  color: $text-muted;
  font-weight: 600;
  margin: 0 1px;
  font-size: 10px;
}

.balls-cell {
  display: flex;
  gap: 1px;
  align-items: center;

  &--front {
    gap: 2px;
  }

  &--back {
    gap: 1px;
  }
}

// 命中样式
.ball--hit {
  background: $accent-green !important;
  box-shadow: 0 0 8px $accent-green !important;
  color: $bg-dark !important;
}

// 超小球号样式
.ball--xs {
  width: 24px !important;
  height: 24px !important;
  font-size: 12px !important;
}

.pagination-wrap {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}

// 排行榜样式
.ranking-controls {
  display: flex;
  align-items: center;
  gap: 12px;
}

.rank-cell {
  display: flex;
  justify-content: center;
}

.rank-badge {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 28px;
  height: 28px;
  border-radius: 50%;
  background: rgba($text-muted, 0.2);
  color: $text-secondary;
  font-weight: 700;
  font-size: 14px;

  &.rank-1 {
    background: linear-gradient(135deg, #ffd700, #ffaa00);
    color: #333;
    box-shadow: 0 0 12px rgba(255, 215, 0, 0.5);
  }

  &.rank-2 {
    background: linear-gradient(135deg, #c0c0c0, #a0a0a0);
    color: #333;
    box-shadow: 0 0 12px rgba(192, 192, 192, 0.5);
  }

  &.rank-3 {
    background: linear-gradient(135deg, #cd7f32, #b87333);
    color: #fff;
    box-shadow: 0 0 12px rgba(205, 127, 50, 0.5);
  }
}

.high-prize {
  font-family: $font-mono;
  color: $accent-orange;
  font-weight: 600;
}

.composite-score {
  font-family: $font-mono;
  color: $primary-color;
  font-weight: 700;
}

// 回测样式
.backtest-controls {
  display: flex;
  align-items: center;
  gap: 8px;

  .control-label {
    color: $text-muted;
    font-size: 13px;
    margin-right: 8px;
  }
}

.backtest-overview {
  margin-bottom: 20px;
}

.backtest-card {
  background: rgba($bg-dark, 0.8);
  border: 1px solid $border-color;
  border-radius: 8px;
  padding: 16px;
  text-align: center;
  transition: all 0.3s ease;

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 4px 20px rgba($primary-color, 0.2);
  }

  &.backtest-best {
    border-color: $accent-green;
    background: rgba($accent-green, 0.15);
  }
}

.backtest-rank {
  font-size: 12px;
  color: $text-muted;
  margin-bottom: 4px;
}

.backtest-method {
  font-size: 14px;
  font-weight: 600;
  color: $text-primary;
  margin-bottom: 8px;
}

.backtest-roi {
  font-size: 24px;
  font-weight: 700;
  font-family: $font-mono;
  margin-bottom: 4px;

  &.roi-positive {
    color: $accent-green;
  }

  &.roi-negative {
    color: $accent-red;
  }
}

.backtest-label {
  font-size: 12px;
  color: $text-muted;
  margin-bottom: 8px;
}

.backtest-stats {
  display: flex;
  justify-content: center;
  gap: 12px;
  font-size: 12px;
  color: $text-secondary;
}

.backtest-title {
  display: flex;
  align-items: center;
  gap: 12px;
  flex: 1;

  .backtest-title-method {
    font-weight: 600;
    min-width: 100px;
  }

  .backtest-title-prize {
    color: $text-muted;
    font-size: 13px;
  }
}

.backtest-detail {
  padding: 16px;
  background: rgba($bg-dark, 0.8);
  border-radius: 8px;
  border: 1px solid $border-color;
}

.backtest-desc {
  margin-top: 16px;
  margin-bottom: 16px;
}

.prize-distribution {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
  flex-wrap: wrap;
}

.prize-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 8px 12px;
  background: rgba($bg-dark-lighter, 0.6);
  border-radius: 6px;
  min-width: 60px;

  .prize-label {
    font-size: 11px;
    color: $text-muted;
    margin-bottom: 4px;
  }

  .prize-count {
    font-size: 16px;
    font-weight: 700;
    font-family: $font-mono;
    color: $accent-green;
  }
}

.backtest-detail-table {
  margin-top: 16px;

  --el-table-bg-color: transparent;
  --el-table-tr-bg-color: rgba($bg-dark, 0.6);
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
    color: $text-secondary;
  }

  td.el-table__cell {
    background: rgba($bg-dark, 0.6);
    border-bottom: 1px solid rgba($border-color, 0.4);
    color: $text-primary;
  }
}

.profit {
  color: $accent-green;
  font-weight: 600;
}

.loss {
  color: $accent-red;
  font-weight: 600;
}
</style>
