<template>
  <div class="lottery-page">
    <div class="page-header">
      <h1>数据管理</h1>
      <p>开奖数据录入与管理</p>
      <el-button type="primary" :icon="Plus" @click="openAddDialog">
        添加开奖数据
      </el-button>
    </div>

    <div class="tech-card">
      <div class="tech-card__body">
        <el-table :data="lotteryList" v-loading="loading" class="lottery-table" style="width: 100%">
          <el-table-column prop="issue" label="期号" align="center" />
          <el-table-column prop="drawDate" label="开奖日期" align="center" />
          <el-table-column label="前区号码" align="center">
            <template #default="{ row }">
              <div class="balls-cell balls-cell--front">
                <span
                  v-for="n in [row.frontBall1, row.frontBall2, row.frontBall3, row.frontBall4, row.frontBall5]"
                  :key="n"
                  class="ball ball--front"
                >{{ n }}</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="后区号码" align="center">
            <template #default="{ row }">
              <div class="balls-cell balls-cell--back">
                <span
                  v-for="n in [row.backBall1, row.backBall2]"
                  :key="n"
                  class="ball ball--back"
                >{{ n }}</span>
              </div>
            </template>
          </el-table-column>
        </el-table>

        <div class="pagination-wrap">
          <el-pagination
            v-model:current-page="page"
            v-model:page-size="size"
            :page-sizes="[10, 20, 50]"
            :total="total"
            layout="total, sizes, prev, pager, next"
            @size-change="fetchList"
            @current-change="fetchList"
          />
        </div>
      </div>
    </div>

    <el-dialog v-model="showAddDialog" title="添加开奖数据" width="560px" center>
      <el-form :model="form" label-width="80px">
        <el-form-item label="期号">
          <el-input v-model="form.issue" placeholder="请输入期号" />
        </el-form-item>
        <el-form-item label="开奖日期">
          <el-date-picker
            v-model="form.drawDate"
            type="date"
            placeholder="选择日期"
            value-format="yyyy-MM-dd"
            style="width: 100%"
          />
        </el-form-item>

        <!-- 前区号码选择 -->
        <el-form-item label="前区号码">
          <div class="ball-selector">
            <div class="ball-row ball-row--front">
              <span
                v-for="n in 35"
                :key="'front-' + n"
                class="ball ball--selector ball--front"
                :class="{ 'ball--selected': frontSelected.includes(n) }"
                @click="toggleFrontBall(n)"
              >{{ n }}</span>
            </div>
            <div class="ball-hint">已选 {{ frontSelected.length }} 个（需选5个）</div>
          </div>
        </el-form-item>

        <!-- 后区号码选择 -->
        <el-form-item label="后区号码">
          <div class="ball-selector">
            <div class="ball-row ball-row--back">
              <span
                v-for="n in 12"
                :key="'back-' + n"
                class="ball ball--selector ball--back"
                :class="{ 'ball--selected': backSelected.includes(n) }"
                @click="toggleBackBall(n)"
              >{{ n }}</span>
            </div>
            <div class="ball-hint">已选 {{ backSelected.length }} 个（需选2个）</div>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAddDialog = false">取消</el-button>
        <el-button type="primary" :loading="submitting" :disabled="!canSubmit" @click="submitAdd">
          确认添加
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { lotteryApi } from '@/api'
import type { LotteryResult } from '@/types'

const loading = ref(false)
const submitting = ref(false)
const showAddDialog = ref(false)
const lotteryList = ref<LotteryResult[]>([])
const page = ref(1)
const size = ref(20)
const total = ref(0)

// 前区后区选中的号码
const frontSelected = ref<number[]>([])
const backSelected = ref<number[]>([])

// 表单
const form = reactive({
  issue: '',
  drawDate: '',
  frontBall1: 0, frontBall2: 0, frontBall3: 0, frontBall4: 0, frontBall5: 0,
  backBall1: 0, backBall2: 0
})

// 是否可以提交
const canSubmit = computed(() => {
  return form.issue &&
    form.drawDate &&
    frontSelected.value.length === 5 &&
    backSelected.value.length === 2
})

// 切换前区号码
function toggleFrontBall(n: number) {
  const idx = frontSelected.value.indexOf(n)
  if (idx > -1) {
    frontSelected.value.splice(idx, 1)
  } else if (frontSelected.value.length < 5) {
    frontSelected.value.push(n)
  }
  frontSelected.value.sort((a, b) => a - b)
  updateFrontBalls()
}

// 切换后区号码
function toggleBackBall(n: number) {
  const idx = backSelected.value.indexOf(n)
  if (idx > -1) {
    backSelected.value.splice(idx, 1)
  } else if (backSelected.value.length < 2) {
    backSelected.value.push(n)
  }
  backSelected.value.sort((a, b) => a - b)
  updateBackBalls()
}

// 更新前区球号到表单
function updateFrontBalls() {
  for (let i = 0; i < 5; i++) {
    (form as any)[`frontBall${i + 1}`] = frontSelected.value[i] || 0
  }
}

// 更新后区球号到表单
function updateBackBalls() {
  for (let i = 0; i < 2; i++) {
    (form as any)[`backBall${i + 1}`] = backSelected.value[i] || 0
  }
}

// 重置表单
function resetForm() {
  form.issue = ''
  form.drawDate = ''
  frontSelected.value = []
  backSelected.value = []
  updateFrontBalls()
  updateBackBalls()
}

// 打开添加对话框时重置表单
function openAddDialog() {
  resetForm()
  showAddDialog.value = true
}

async function fetchList() {
  loading.value = true
  try {
    const result = await lotteryApi.getList(page.value, size.value)
    // 后端已按期号倒序返回
    lotteryList.value = result.records || []
    total.value = result.total || 0
  } catch (error) {
    console.error('获取列表失败:', error)
  } finally {
    loading.value = false
  }
}

async function submitAdd() {
  submitting.value = true
  try {
    await lotteryApi.add({
      issue: form.issue,
      drawDate: form.drawDate,
      frontBall1: form.frontBall1,
      frontBall2: form.frontBall2,
      frontBall3: form.frontBall3,
      frontBall4: form.frontBall4,
      frontBall5: form.frontBall5,
      backBall1: form.backBall1,
      backBall2: form.backBall2
    })
    ElMessage.success('添加成功')
    showAddDialog.value = false
    fetchList()
  } catch (error) {
    ElMessage.error('添加失败')
  } finally {
    submitting.value = false
  }
}

onMounted(fetchList)
</script>

<style lang="scss" scoped>
@use '@/assets/styles/variables.scss' as *;

.lottery-page {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.page-header {
  display: flex;
  align-items: center;
  gap: 16px;

  h1 {
    font-size: 24px;
    font-weight: 700;
    margin: 0;
    background: linear-gradient(90deg, $primary-color, $accent-cyan);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
  }

  p {
    color: $text-muted;
    margin: 0;
    flex: 1;
  }
}

.balls-cell {
  display: inline-flex;
  gap: 3px;
  align-items: center;
  justify-content: center;
  min-width: 100%;
}

.balls-cell--front {
  gap: 4px;
}

.balls-cell--back {
  gap: 3px;
}

.date-cell {
  font-family: $font-mono;
  color: $text-primary;
}

.pagination-wrap {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}

.ball-inputs {
  display: flex;
  gap: 8px;
}

// 号码选择器样式
.ball-selector {
  width: 100%;
}

.ball-row {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-bottom: 8px;

  &--front {
    gap: 5px;
  }

  &--back {
    gap: 4px;
  }
}

.ball-hint {
  font-size: 12px;
  color: $text-muted;
  margin-top: 4px;
}

// 对话框内白色输入框样式
.el-dialog {
  .el-input__wrapper {
    background-color: #ffffff !important;
    box-shadow: 0 0 0 1px #dcdfe6 inset !important;
  }

  .el-input__inner {
    color: #333333 !important;
    background-color: #ffffff !important;
  }

  .el-input__placeholder {
    color: #999999 !important;
  }

  .el-date-editor {
    .el-input__wrapper {
      background-color: #ffffff !important;
      box-shadow: 0 0 0 1px #dcdfe6 inset !important;
    }

    .el-input__inner {
      color: #333333 !important;
      background-color: #ffffff !important;
    }
  }

  .el-form-item__label {
    color: #ffffff !important;
  }
}

.ball--selector {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s;
  font-size: 12px;
  font-weight: 600;
  background: #1a1a2e;
  color: rgba(255, 255, 255, 0.9);
  border: 2px solid #2a2a4a;

  &:hover {
    background: #252545;
    border-color: #3a3a6a;
  }

  &.ball--front.ball--selected {
    background: linear-gradient(135deg, $primary-color, $primary-light);
    border-color: $primary-color;
    box-shadow: 0 0 10px rgba(102, 126, 234, 0.5);
    color: white;
  }

  &.ball--back.ball--selected {
    background: linear-gradient(135deg, #f5576c, #f093fb);
    border-color: #f5576c;
    box-shadow: 0 0 10px rgba(245, 87, 108, 0.5);
    color: white;
  }
}

// Dark table styles
.lottery-table {
  --el-table-bg-color: transparent;
  --el-table-tr-bg-color: rgba($bg-dark-lighter, 0.6);
  --el-table-header-bg-color: rgba($bg-dark-lighter, 0.8);
  --el-table-row-hover-bg-color: rgba($primary-color, 0.15);
  --el-table-border-color: $border-color;
  --el-table-text-color: $text-primary;
  --el-table-header-text-color: $text-secondary;
  --el-table-cell-height: 36px;

  background: transparent;
  font-size: 12px;

  &::before {
    display: none;
  }

  th.el-table__cell {
    background: rgba($bg-dark-lighter, 0.9);
    border-bottom: 1px solid $border-color;
    padding: 6px 8px;
  }

  td.el-table__cell {
    border-bottom: 1px solid rgba($border-color, 0.4);
    padding: 4px 8px;
  }
}

// 表格单元格内容对齐
.date-cell {
  display: inline-block;
  width: 100%;
  text-align: center;
}

// 表格内的球号样式 - 放大
.lottery-table .ball {
  width: 30px;
  height: 30px;
  font-size: 14px;
}
</style>
