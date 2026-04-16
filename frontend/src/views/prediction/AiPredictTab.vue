<template>
  <div class="ai-predict-tab">
    <div class="prediction-cards">
      <div class="tech-card prediction-card">
        <div class="tech-card__header">
          <div class="tech-card__title">
            <el-icon><MagicStick /></el-icon>
            AI 智能预测
          </div>
        </div>
        <div class="tech-card__body">
          <p class="ai-description">
            基于云端 AI 大模型分析历史数据，生成智能预测号码
          </p>
          <div class="generate-config">
            <div class="config-row">
              <span class="config-label">预测注数</span>
              <el-input-number v-model="form.count" :min="1" :max="50" size="small" />
            </div>
            <div class="config-row">
              <span class="config-label">历史期数</span>
              <el-select v-model="form.historyPeriods" placeholder="选择分析期数" size="small" style="width: 160px">
                <el-option label="最近 30 期" :value="30" />
                <el-option label="最近 50 期" :value="50" />
                <el-option label="最近 100 期" :value="100" />
                <el-option label="最近 200 期" :value="200" />
              </el-select>
            </div>
            <div class="config-row">
              <span class="config-label">AI 模型</span>
              <div class="model-tags">
                <el-tag
                  v-for="model in aiModels"
                  :key="model.code"
                  :type="selectedModel === model.code ? 'primary' : 'info'"
                  effect="dark"
                  class="model-tag"
                  :class="{ active: selectedModel === model.code }"
                  @click="selectedModel = model.code"
                >
                  {{ model.name }}
                </el-tag>
              </div>
            </div>
          </div>

          <el-button
            type="primary"
            size="large"
            :icon="Lightning"
            :loading="loading"
            class="generate-btn"
            @click="handlePredict"
          >
            {{ loading ? 'AI 预测中...' : '开始预测' }}
          </el-button>
        </div>
      </div>

      <div class="tech-card prediction-card">
        <div class="tech-card__header">
          <div class="tech-card__title">
            <el-icon><Star /></el-icon>
            AI 预测结果
          </div>
          <span class="result-count">{{ results.length }} 注</span>
        </div>
        <div class="tech-card__body">
          <div v-if="results.length > 0" class="results-list">
            <div
              v-for="(result, index) in results"
              :key="index"
              class="result-item"
            >
              <div class="result-header">
                <span class="result-num">{{ index + 1 }}</span>
                <div class="result-balls">
                  <BallDisplay
                    :front-balls="result.frontBalls"
                    :back-balls="result.backBalls"
                  />
                </div>
              </div>
              <div class="result-meta">
                <div class="confidence-bar">
                  <span class="confidence-label">置信度</span>
                  <el-progress
                    :percentage="Math.round(result.confidence * 100)"
                    :stroke-width="6"
                    :show-text="false"
                    class="confidence-progress"
                  />
                  <span class="confidence-value">{{ (result.confidence * 100).toFixed(1) }}%</span>
                </div>
                <el-tag size="small" type="info">{{ result.method || selectedModel }}</el-tag>
              </div>
              <el-collapse v-if="result.reasoning" class="reasoning-collapse">
                <el-collapse-item title="AI 推理过程">
                  <pre class="reasoning-text">{{ result.reasoning }}</pre>
                </el-collapse-item>
              </el-collapse>
            </div>
          </div>
          <div v-else class="empty-state">
            <el-icon size="40"><MagicStick /></el-icon>
            <p>点击上方按钮启动 AI 预测</p>
          </div>
        </div>
      </div>
    </div>

    <div class="tech-card info-card">
      <div class="tech-card__body">
        <div class="info-content">
          <el-icon><InfoFilled /></el-icon>
          <span>AI 预测基于大语言模型分析历史号码规律，生成统计意义上可能出现的号码组合。预测结果仅供参考，请理性投注。</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { MagicStick, Star, Lightning, InfoFilled } from '@element-plus/icons-vue'
import { aiApi, type AiPredictResponse } from '@/api/modules/ai'
import BallDisplay from '@/components/common/BallDisplay.vue'

interface PredictionResult {
  frontBalls: number[]
  backBalls: number[]
  confidence: number
  method: string
  reasoning?: string
}

const loading = ref(false)
const results = ref<PredictionResult[]>([])

const form = reactive({
  count: 5,
  historyPeriods: 100,
})

const selectedModel = ref('claude')

const aiModels = [
  { code: 'claude', name: 'Claude' },
  { code: 'kimi', name: 'Kimi' },
  { code: 'gpt4o', name: 'GPT-4o' },
]

async function handlePredict() {
  loading.value = true
  results.value = []

  try {
    const response = await aiApi.predict({
      count: form.count,
      historyPeriods: form.historyPeriods,
      method: selectedModel.value,
    })

    if (response.data && response.data.predictions) {
      results.value = response.data.predictions.map(p => ({
        frontBalls: p.frontBalls,
        backBalls: p.backBalls,
        confidence: p.confidence,
        method: p.method || selectedModel.value,
        reasoning: undefined,
      }))
      ElMessage.success(`AI 预测完成，已生成 ${results.value.length} 注`)
    }
  } catch (error) {
    console.error('AI 预测失败:', error)
    ElMessage.error('预测失败，请稍后重试')
  } finally {
    loading.value = false
  }
}
</script>

<style lang="scss" scoped>
@use '@/assets/styles/variables.scss' as *;

.ai-predict-tab {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.prediction-cards {
  display: grid;
  grid-template-columns: 360px 1fr;
  gap: 16px;
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

.ai-description {
  font-size: 13px;
  color: $text-muted;
  margin: 0 0 16px;
  line-height: 1.5;
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

.model-tags {
  display: flex;
  gap: 8px;
}

.model-tag {
  cursor: pointer;
  transition: all 0.2s;
  border: 1px solid $border-color !important;
  opacity: 0.7;

  &.active {
    opacity: 1;
    border-color: $primary-color !important;
    box-shadow: 0 0 8px rgba($primary-color, 0.4);
  }

  &:hover {
    opacity: 0.9;
  }
}

.generate-btn {
  width: 100%;
  margin-top: 8px;
}

.results-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.result-item {
  padding: 12px;
  background: rgba($primary-color, 0.05);
  border: 1px solid rgba($primary-color, 0.15);
  border-radius: 8px;
}

.result-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 10px;
}

.result-num {
  width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, $primary-color, #6366f1);
  border-radius: 50%;
  font-size: 12px;
  font-weight: 600;
  color: white;
  flex-shrink: 0;
}

.result-balls {
  flex: 1;
}

.result-meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding-top: 8px;
  border-top: 1px solid rgba($border-color, 0.5);
}

.confidence-bar {
  display: flex;
  align-items: center;
  gap: 8px;
  flex: 1;
}

.confidence-label {
  font-size: 11px;
  color: $text-muted;
  white-space: nowrap;
}

.confidence-progress {
  flex: 1;
  max-width: 120px;

  :deep(.el-progress-bar__outer) {
    background: rgba($text-muted, 0.15);
  }
}

.confidence-value {
  font-size: 12px;
  font-weight: 600;
  color: $accent-cyan;
  min-width: 40px;
  text-align: right;
}

.reasoning-collapse {
  margin-top: 10px;
  border: none !important;

  :deep(.el-collapse-item__header) {
    font-size: 12px;
    color: $text-muted;
    background: transparent;
    border-bottom: 1px solid rgba($border-color, 0.3);
  }

  :deep(.el-collapse-item__wrap) {
    background: transparent;
    border: none;
  }

  :deep(.el-collapse-item__content) {
    padding: 8px 0;
  }
}

.reasoning-text {
  font-family: $font-mono;
  font-size: 11px;
  color: $text-secondary;
  margin: 0;
  white-space: pre-wrap;
  word-break: break-word;
  line-height: 1.6;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px;
  gap: 12px;
  color: $text-muted;

  p {
    font-size: 13px;
    margin: 0;
  }
}

.info-card {
  .tech-card__body {
    padding: 12px 16px;
  }
}

.info-content {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 12px;
  color: $text-muted;

  .el-icon {
    color: $accent-cyan;
    flex-shrink: 0;
  }
}

@media (max-width: $lg) {
  .prediction-cards {
    grid-template-columns: 1fr;
  }
}
</style>