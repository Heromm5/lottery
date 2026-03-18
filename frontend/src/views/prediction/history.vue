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
        <div class="filter-tabs" style="flex-shrink: 0">
          <el-radio-group v-model="filterStatus" @change="handleFilterChange" size="small">
            <el-radio-button value="all">全部</el-radio-button>
            <el-radio-button value="verified">已开奖</el-radio-button>
            <el-radio-button value="unverified">未开奖</el-radio-button>
          </el-radio-group>
        </div>

        <div class="table-wrapper">
        <el-table
          :data="historyList"
          v-loading="loading"
          class="history-table"
          style="width: 100%"
          :row-class-name="({ row }: { row: PredictionRecord }) => row.isFinal === 1 ? 'row--final' : ''"
        >
          <el-table-column prop="targetIssue" label="预测期号" min-width="110" align="center" />
          <el-table-column label="标记" width="56" align="center">
            <template #default="{ row }">
              <FinalMark :is-final="row.isFinal" />
            </template>
          </el-table-column>
          <el-table-column prop="methodName" label="预测方法" min-width="120" show-overflow-tooltip />
          <el-table-column label="预测号码" min-width="200" align="center">
            <template #default="{ row }">
              <BallDisplay :front-balls="row.frontBalls" :back-balls="row.backBalls" size="sm" />
            </template>
          </el-table-column>
          <el-table-column label="验证状态" min-width="100" align="center">
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
          <el-table-column label="命中情况" min-width="140" align="center">
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
          <el-table-column label="中奖等级" min-width="140" align="center" class-name="prize-column">
            <template #default="{ row }">
              <PrizeTag :prize="row.prizeLevel" />
            </template>
          </el-table-column>
          <el-table-column label="预测时间" min-width="160">
            <template #default="{ row }">
              {{ formatDateTime(row.createdAt) }}
            </template>
          </el-table-column>
        </el-table>
        </div>

        <div class="pagination-wrap" style="flex-shrink: 0">
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
import { BallDisplay, PrizeTag, FinalMark } from '@/components/common'
import { formatDateTime } from '@/composables/useDateTime'

const loading = ref(false)
const historyList = ref<PredictionRecord[]>([])
const page = ref(1)
const size = ref(50)
const total = ref(0)
const filterStatus = ref('all')

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
  min-height: 0;
  flex: 1;
}

.tech-card__body {
  display: flex;
  flex-direction: column;
  min-height: 0;
}

.tech-card {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-height: 0;
}

.page-header {
  h1 {
    font-size: 24px;
    font-weight: 600;
    margin: 0 0 8px;
    color: $text-primary;
  }

  p {
    color: $text-muted;
    margin: 0;
  }
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

.table-wrapper {
  flex: 1;
  min-height: 0;
  overflow: auto;
  width: 100%;
}

.history-table {
  width: 100%;
  min-width: 900px;
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
    padding: 10px 12px;
  }

  td.el-table__cell {
    border-bottom: 1px solid rgba($border-color, 0.4);
    padding: 10px 12px;
  }

  tr.row--final {
    --el-table-tr-bg-color: rgba($accent-orange, 0.08);
  }
  tr.row--final:hover > td {
    background: rgba($accent-orange, 0.12) !important;
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
