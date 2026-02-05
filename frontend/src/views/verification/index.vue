<template>
  <div class="verification-page">
    <div class="page-header">
      <h1>验证中心</h1>
      <p>查看预测验证结果及准确率统计</p>
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

    <!-- 准确率统计卡片 -->
    <div class="tech-card">
      <div class="tech-card__header">
        <div class="tech-card__title">
          <el-icon><DataAnalysis /></el-icon>
          预测准确率统计
        </div>
      </div>
      <div class="tech-card__body">
        <el-table :data="accuracyStats" class="verify-table" style="width: 100%" :default-expand-all="false">
          <el-table-column prop="methodName" label="预测方法" min-width="120" />
          <el-table-column prop="totalPredictions" label="预测次数" width="100" align="center" />
          <el-table-column prop="frontAvgHit" label="前区命中" width="100" align="center">
            <template #default="{ row }">
              <span class="hit-rate">{{ row.frontAvgHit?.toFixed(2) || '0.00' }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="backAvgHit" label="后区命中" width="100" align="center">
            <template #default="{ row }">
              <span class="hit-rate">{{ row.backAvgHit?.toFixed(2) || '0.00' }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="totalPrizeCount" label="中奖次数" width="100" align="center" />
          <el-table-column prop="prizeRate" label="中奖率" width="120" align="center">
            <template #default="{ row }">
              <span class="prize-rate">{{ (row.prizeRate || 0).toFixed(1) }}%</span>
            </template>
          </el-table-column>
        </el-table>
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
import { DataAnalysis, List, Refresh, RefreshRight } from '@element-plus/icons-vue'
import { verificationApi, type VerificationHistoryRecord } from '@/api'
import type { AccuracyStats } from '@/types'
import { PREDICTION_METHODS, getMethodDisplayName } from '@/types'

const accuracyStats = ref<AccuracyStats[]>([])
const historyList = ref<VerificationHistoryRecord[]>([])
const historyLoading = ref(false)
const historyPage = ref(1)
const historySize = ref(20)
const historyTotal = ref(0)

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
  color: $accent-green;
  font-weight: 600;
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
</style>
