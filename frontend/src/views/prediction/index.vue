<template>
  <div class="prediction-page">
    <div class="page-header">
      <div class="header-content">
        <h1>智能预测</h1>
        <p>多算法融合智能分析，生成下期预测号码</p>
      </div>
      <div class="header-actions">
        <el-button :icon="Clock" @click="$router.push('/prediction/history')">
          预测历史
        </el-button>
      </div>
    </div>

    <div class="prediction-cards">
      <!-- 生成配置区域 -->
      <div class="tech-card prediction-card">
        <div class="tech-card__header">
          <div class="tech-card__title">
            <el-icon><MagicStick /></el-icon>
            生成预测
          </div>
        </div>
        <div class="tech-card__body">
          <div class="generate-config">
            <div class="config-row">
              <span class="config-label">选择算法</span>
              <div class="method-tags">
                <el-tag
                  v-for="algo in algorithms"
                  :key="algo.code"
                  :type="selectedMethods.includes(algo.code) ? 'primary' : 'info'"
                  effect="dark"
                  class="method-tag"
                  :class="{ active: selectedMethods.includes(algo.code) }"
                  @click="toggleMethod(algo.code)"
                >
                  <span class="tag-text">{{ algo.name }}</span>
                  <span v-if="selectedMethods.includes(algo.code)" class="tag-check">✓</span>
                </el-tag>
              </div>
            </div>
            <div class="config-row-inline">
              <div class="config-item">
                <span class="config-label">每种方法生成</span>
                <el-input-number v-model="betsPerMethod" :min="1" :max="10" size="small" />
                <span class="config-unit">注</span>
              </div>
              <div class="config-item">
                <span class="config-label">最终选取</span>
                <el-input-number v-model="targetCount" :min="1" :max="20" size="small" />
                <span class="config-unit">注</span>
              </div>
            </div>
          </div>

          <el-button
            type="primary"
            size="large"
            :icon="Lightning"
            :loading="generating"
            :disabled="selectedMethods.length === 0"
            class="generate-btn"
            @click="generatePredictions"
          >
            {{ generating ? '生成中...' : '生成预测号码' }}
          </el-button>

          <!-- 生成结果 -->
          <div v-if="generatedResults.length > 0" class="generated-section">
            <div class="section-header">
              <span class="section-title">
                <el-icon><DocumentCopy /></el-icon>
                已生成 {{ generatedResults.length }} 注
              </span>
              <div class="section-actions">
                <el-button size="small" text @click="selectTop">选择前 {{ targetCount }} 注</el-button>
                <el-button size="small" text @click="clearSelection">清空</el-button>
              </div>
            </div>
            <div class="results-grid">
              <div
                v-for="(item, index) in generatedResults"
                :key="index"
                class="result-chip"
                :class="{ selected: selectedResults.includes(index) }"
                @click="toggleSelection(index)"
              >
                <el-checkbox :model-value="selectedResults.includes(index)" @click.stop />
                <span class="chip-method">{{ item.methodName }}</span>
                <span class="chip-balls">
                  <span class="ball-text front">{{ item.frontBalls.slice(0, 3).join(' ') }}</span>
                  <span class="ball-text back">{{ item.backBalls.join(' ') }}</span>
                </span>
              </div>
            </div>
            <div class="selection-bar">
              <span class="selection-info">
                已选择 <strong>{{ selectedResults.length }}</strong> / {{ targetCount }} 注
              </span>
              <el-button
                type="primary"
                size="small"
                :disabled="selectedResults.length !== targetCount"
                @click="confirmSelection"
              >
                确认选择
              </el-button>
            </div>
          </div>
        </div>
      </div>

      <!-- 最终预测 -->
      <div class="tech-card prediction-card">
        <div class="tech-card__header">
          <div class="tech-card__title">
            <el-icon><Star /></el-icon>
            最终预测
          </div>
          <span class="result-count">{{ finalResults.length }} 注</span>
        </div>
        <div class="tech-card__body">
          <div v-if="finalResults.length > 0" class="final-list">
            <div
              v-for="(item, index) in finalResults"
              :key="index"
              class="final-item"
            >
              <span class="final-num">{{ index + 1 }}</span>
              <div class="final-balls">
                <span class="ball-inline front">{{ item.frontBalls.join(' ') }}</span>
                <span class="plus">+</span>
                <span class="ball-inline back">{{ item.backBalls.join(' ') }}</span>
              </div>
              <span class="final-method">{{ item.methodName }}</span>
            </div>
          </div>
          <div v-else class="final-empty">
            <el-icon size="32"><MagicStick /></el-icon>
            <p>选择 {{ targetCount }} 注作为最终预测</p>
          </div>
          <div v-if="finalResults.length > 0" class="final-actions">
            <el-button size="small" @click="regenerateFromSelection">
              <el-icon><Refresh /></el-icon>
              重新选择
            </el-button>
          </div>
        </div>
      </div>
    </div>

    <!-- 说明 -->
    <div class="tech-card info-card">
      <div class="tech-card__body">
        <div class="info-tags">
          <div v-for="algo in algorithms.slice(0, 4)" :key="algo.code" class="info-tag">
            <span class="info-name">{{ algo.name }}</span>
            <span class="info-desc">{{ algo.description }}</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import {
  MagicStick, Refresh, Star, Lightning,
  DocumentCopy, Clock
} from '@element-plus/icons-vue'
import { predictionApi } from '@/api'
import type { PredictionResult } from '@/types'

const generating = ref(false)
const selectedMethods = ref<string[]>(['HOT', 'MISSING', 'BALANCED', 'ML', 'ADAPTIVE', 'BAYESIAN', 'MARKOV', 'MONTECARLO', 'GRADIENT_BOOST', 'ENSEMBLE'])
const betsPerMethod = ref(5)
const targetCount = ref(5)
const generatedResults = ref<PredictionResult[]>([])
const selectedResults = ref<number[]>([])
const finalResults = ref<PredictionResult[]>([])

const algorithms = [
  { code: 'HOT', name: '热号', description: '基于近期出现频率最高的号码' },
  { code: 'MISSING', name: '遗漏', description: '分析遗漏周期，捕捉回补机会' },
  { code: 'BALANCED', name: '均衡', description: '平衡冷热号码的组合策略' },
  { code: 'ML', name: '机器学习', description: '基于历史数据的模式识别预测' },
  { code: 'ADAPTIVE', name: '自适应', description: '根据历史表现动态调整权重' },
  { code: 'BAYESIAN', name: '贝叶斯', description: '基于概率论的贝叶斯分析方法' },
  { code: 'MARKOV', name: '马尔可夫', description: '基于状态转移的概率预测' },
  { code: 'MONTECARLO', name: '蒙特卡洛', description: '通过随机模拟生成预测' },
  { code: 'GRADIENT_BOOST', name: '梯度提升', description: '基于梯度提升树的预测方法' },
  { code: 'ENSEMBLE', name: '集成', description: '融合多种算法的综合预测' }
]

function toggleMethod(code: string) {
  const idx = selectedMethods.value.indexOf(code)
  if (idx >= 0) {
    if (selectedMethods.value.length > 1) {
      selectedMethods.value.splice(idx, 1)
    }
  } else {
    selectedMethods.value.push(code)
  }
  if (selectedMethods.value.length === 0) {
    ElMessage.warning('请至少选择一个算法')
    selectedMethods.value = ['HOT']
  }
}

async function generatePredictions() {
  if (selectedMethods.value.length === 0) {
    ElMessage.warning('请至少选择一个算法')
    return
  }

  generating.value = true
  generatedResults.value = []
  selectedResults.value = []
  finalResults.value = []

  try {
    const allResults: PredictionResult[] = []

    for (const method of selectedMethods.value) {
      try {
        const results = await predictionApi.generate(betsPerMethod.value, method)
        if (results && results.length > 0) {
          allResults.push(...results)
        }
      } catch (e) {
        console.warn(`方法 ${method} 生成失败:`, e)
      }
    }

    if (allResults.length === 0) {
      ElMessage.error('没有生成任何预测结果')
      return
    }

    // 调用评分API获取带分数的预测结果
    const scoredResults = await predictionApi.score(allResults)

    // 按分数降序排序，选择前N注概率最高的
    const sortedResults = scoredResults.sort((a, b) => {
      const scoreA = a.score || 0
      const scoreB = b.score || 0
      return scoreB - scoreA
    })

    generatedResults.value = sortedResults

    // 选择分数最高的N注
    selectedResults.value = Array.from({ length: Math.min(targetCount.value, sortedResults.length) }, (_, i) => i)
    finalResults.value = selectedResults.value.map(i => sortedResults[i])

    ElMessage.success(`生成 ${allResults.length} 注预测，已选择概率最高的 ${finalResults.value.length} 注`)
  } catch (error) {
    ElMessage.error('预测生成失败')
    console.error(error)
  } finally {
    generating.value = false
  }
}

function toggleSelection(index: number) {
  const idx = selectedResults.value.indexOf(index)
  if (idx >= 0) {
    selectedResults.value.splice(idx, 1)
  } else {
    if (selectedResults.value.length < targetCount.value) {
      selectedResults.value.push(index)
    } else {
      ElMessage.warning(`最多选择 ${targetCount.value} 注`)
    }
  }
  finalResults.value = selectedResults.value.map(i => generatedResults.value[i])
}

function selectTop() {
  // 假设 generatedResults 已经按分数降序排序，直接选择前N注
  const count = Math.min(targetCount.value, generatedResults.value.length)
  selectedResults.value = Array.from({ length: count }, (_, i) => i)
  finalResults.value = selectedResults.value.map(i => generatedResults.value[i])
}

function clearSelection() {
  selectedResults.value = []
  finalResults.value = []
}

function confirmSelection() {
  if (selectedResults.value.length !== targetCount.value) {
    ElMessage.warning(`请选择恰好 ${targetCount.value} 注`)
    return
  }
  finalResults.value = selectedResults.value.map(i => generatedResults.value[i])
}

function regenerateFromSelection() {
  finalResults.value = []
  selectedResults.value = []
}
</script>

<style lang="scss" scoped>
@use '@/assets/styles/variables.scss' as *;

.prediction-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;

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
    font-size: 14px;
  }
}

.prediction-cards {
  display: grid;
  grid-template-columns: 1fr 340px;
  gap: 16px;
}

.generate-config {
  .config-row {
    margin-bottom: 16px;
  }

  .config-label {
    display: block;
    font-size: 13px;
    color: $text-muted;
    margin-bottom: 8px;
  }
}

.method-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.method-tag {
  cursor: pointer;
  transition: all 0.2s;
  border: 1px solid $border-color !important;
  opacity: 0.6;

  &.active {
    opacity: 1;
    border-color: $primary-color !important;
    box-shadow: 0 0 8px rgba($primary-color, 0.4);
  }

  &:hover {
    opacity: 0.9;
  }

  .tag-text {
    margin-right: 4px;
  }

  .tag-check {
    color: $accent-cyan;
    font-weight: bold;
  }
}

.config-row-inline {
  display: flex;
  gap: 24px;
  margin-bottom: 16px;
}

.config-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.config-unit {
  font-size: 13px;
  color: $text-muted;
}

.generate-btn {
  width: 100%;
  margin-bottom: 16px;
}

.generated-section {
  border-top: 1px solid $border-color;
  padding-top: 16px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  font-weight: 600;
  color: $text-primary;
}

.section-actions {
  display: flex;
  gap: 4px;
}

.results-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
  gap: 6px;
  max-height: 240px;
  overflow-y: auto;
  padding: 2px;
}

.result-chip {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 6px 8px;
  background: $bg-dark-lighter;
  border: 1px solid $border-color;
  border-radius: 4px;
  cursor: pointer;
  font-size: 11px;
  transition: all 0.15s;

  &:hover {
    border-color: rgba($primary-color, 0.5);
  }

  &.selected {
    border-color: $primary-color;
    background: rgba($primary-color, 0.1);
  }

  .chip-method {
    color: $text-muted;
    white-space: nowrap;
    max-width: 50px;
    overflow: hidden;
    text-overflow: ellipsis;
  }

  .chip-balls {
    display: flex;
    align-items: center;
    gap: 4px;
    flex: 1;
    min-width: 0;
  }
}

.ball-text {
  font-family: $font-mono;
  font-size: 10px;

  &.front {
    color: #667eea;
  }

  &.back {
    color: #f5576c;
  }
}

.selection-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid $border-color;
}

.selection-info {
  font-size: 13px;
  color: $text-secondary;

  strong {
    color: $primary-color;
    font-size: 16px;
  }
}

.prediction-card {
  .tech-card__header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }
}

.result-count {
  font-size: 12px;
  color: $primary-color;
  background: rgba($primary-color, 0.1);
  padding: 2px 8px;
  border-radius: 10px;
}

.final-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.final-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 10px;
  background: rgba($primary-color, 0.08);
  border: 1px solid rgba($primary-color, 0.2);
  border-radius: 6px;

  .final-num {
    width: 20px;
    height: 20px;
    display: flex;
    align-items: center;
    justify-content: center;
    background: $primary-color;
    border-radius: 50%;
    font-size: 11px;
    font-weight: 600;
    color: white;
    flex-shrink: 0;
  }

  .final-balls {
    display: flex;
    align-items: center;
    gap: 6px;
    flex: 1;
    justify-content: center;
  }

  .final-method {
    font-size: 11px;
    color: $text-muted;
    white-space: nowrap;
    max-width: 50px;
    overflow: hidden;
    text-overflow: ellipsis;
  }
}

.ball-inline {
  font-family: $font-mono;
  font-size: 12px;
  font-weight: 600;
  letter-spacing: 1px;

  &.front {
    color: #667eea;
  }

  &.back {
    color: #f5576c;
  }
}

.plus {
  color: $text-muted;
  font-size: 10px;
}

.final-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 24px;
  gap: 8px;
  color: $text-muted;

  p {
    font-size: 12px;
    margin: 0;
    text-align: center;
  }
}

.final-actions {
  display: flex;
  gap: 8px;
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid $border-color;
}

.info-card {
  .tech-card__body {
    padding: 12px 16px;
  }
}

.info-tags {
  display: flex;
  gap: 16px;
  flex-wrap: wrap;
}

.info-tag {
  display: flex;
  flex-direction: column;
  gap: 2px;

  .info-name {
    font-size: 13px;
    font-weight: 600;
    color: $accent-cyan;
  }

  .info-desc {
    font-size: 11px;
    color: $text-muted;
  }
}

@media (max-width: $lg) {
  .prediction-cards {
    grid-template-columns: 1fr;
  }
}
</style>
