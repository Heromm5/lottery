<template>
  <div class="history-page">
    <div class="page-header">
      <div class="header-content">
        <h1>预测历史</h1>
        <p>查看历史预测记录及验证结果</p>
      </div>
    </div>

    <div class="tech-card">
      <div class="tech-card__body">
        <!-- 筛选Tab -->
        <div class="filter-tabs">
          <el-radio-group v-model="filterStatus" @change="handleFilterChange" size="small">
            <el-radio-button value="all">全部</el-radio-button>
            <el-radio-button value="verified">已开奖</el-radio-button>
            <el-radio-button value="unverified">未开奖</el-radio-button>
          </el-radio-group>
        </div>

        <el-table
          :data="historyList"
          v-loading="loading"
          class="history-table"
          style="width: 100%"
        >
          <el-table-column prop="targetIssue" label="预测期号" width="120" align="center" />
          <el-table-column prop="methodName" label="预测方法" width="150" />
          <el-table-column label="预测号码" min-width="200">
            <template #default="{ row }">
              <div class="balls-cell">
                <span
                  v-for="n in row.frontBalls"
                  :key="n"
                  class="ball ball--front ball--sm"
                >{{ n }}</span>
                <span class="separator">+</span>
                <span
                  v-for="n in row.backBalls"
                  :key="n"
                  class="ball ball--back ball--sm"
                >{{ n }}</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="验证状态" width="120" align="center">
            <template #default="{ row }">
              <el-tag
                :type="row.isVerified === 1 ? 'success' : 'info'"
                effect="dark"
                size="small"
              >
                {{ row.isVerified === 1 ? '已验证' : '未验证' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="命中情况" width="150" align="center">
            <template #default="{ row }">
              <template v-if="row.isVerified === 1">
                <span class="hit-text">
                  前区 {{ row.frontHitCount }}/5
                </span>
                <span class="hit-text back">
                  后区 {{ row.backHitCount }}/2
                </span>
              </template>
              <span v-else class="pending-text">--</span>
            </template>
          </el-table-column>
          <el-table-column prop="prizeLevel" label="中奖等级" width="120" align="center">
            <template #default="{ row }">
              <el-tag
                v-if="row.prizeLevel"
                :type="getPrizeType(row.prizeLevel)"
                effect="dark"
                size="small"
              >
                {{ row.prizeLevel }}
              </el-tag>
              <span v-else class="pending-text">--</span>
            </template>
          </el-table-column>
          <el-table-column prop="createdAt" label="预测时间" width="180" />
        </el-table>

        <div class="pagination-wrap">
          <el-pagination
            v-model:current-page="page"
            v-model:page-size="size"
            :page-sizes="[10, 20, 50]"
            :total="total"
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
import { predictionApi } from '@/api'
import type { PredictionRecord } from '@/types'

const loading = ref(false)
const historyList = ref<PredictionRecord[]>([])
const page = ref(1)
const size = ref(20)
const total = ref(0)
const filterStatus = ref('all') // 筛选状态: all-全部, verified-已开奖, unverified-未开奖

async function fetchHistory() {
  loading.value = true
  try {
    const result = await predictionApi.getList(page.value, size.value, filterStatus.value)
    historyList.value = result.records
    total.value = result.total
  } catch (error) {
    console.error('获取历史记录失败:', error)
  } finally {
    loading.value = false
  }
}

function handleFilterChange() {
  page.value = 1
  fetchHistory()
}

function getPrizeType(prize: string): string {
  const map: Record<string, string> = {
    '一等奖': 'danger',
    '二等奖': 'warning',
    '三等奖': 'success',
    '四等奖': 'success',
    '五等奖': 'info'
  }
  return map[prize] || 'info'
}

onMounted(() => {
  fetchHistory()
})
</script>

<style lang="scss" scoped>
@use '@/assets/styles/variables.scss' as *;

.history-page {
  display: flex;
  flex-direction: column;
  gap: 24px;
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

.balls-cell {
  display: flex;
  align-items: center;
  gap: 8px;
}

.separator {
  color: $text-muted;
  margin: 0 4px;
}

.hit-text {
  font-family: $font-mono;
  color: $accent-green;

  &.back {
    color: $accent-cyan;
    margin-left: 8px;
  }
}

.pending-text {
  color: $text-muted;
}

.filter-tabs {
  margin-bottom: 16px;
  display: flex;
  justify-content: flex-start;
}

.history-table {
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
    padding: 10px 0;
  }

  td.el-table__cell {
    border-bottom: 1px solid rgba($border-color, 0.4);
    padding: 8px 0;
  }
}

.pagination-wrap {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
  padding-top: 16px;
  border-top: 1px solid $border-color;
}
</style>
