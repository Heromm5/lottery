<template>
  <div class="learning-page">
    <div class="page-header">
      <h1>模型学习</h1>
      <p>调整预测算法权重，优化预测效果</p>
    </div>

    <div class="tech-card">
      <div class="tech-card__header">
        <div class="tech-card__title">
          <el-icon><Setting /></el-icon>
          算法权重配置
        </div>
        <el-button type="primary" :icon="Refresh" @click="retrain">
          重新训练模型
        </el-button>
      </div>
      <div class="tech-card__body">
        <div class="weights-list">
          <div v-for="item in weights" :key="item.id" class="weight-item">
            <div class="weight-info">
              <h4>{{ item.methodName }}</h4>
              <p>{{ item.description || '暂无描述' }}</p>
            </div>
            <div class="weight-control">
              <el-slider
                v-model="item.weight"
                :min="0"
                :max="100"
                :step="5"
                show-input
                @change="updateWeight(item)"
              />
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Setting, Refresh } from '@element-plus/icons-vue'
import { learningApi } from '@/api'
import type { MethodWeight } from '@/api/modules/learning'

const weights = ref<MethodWeight[]>([])

async function fetchData() {
  try {
    const result = await learningApi.getWeights()
    weights.value = result
  } catch (error) {
    console.error('获取权重失败:', error)
  }
}

async function updateWeight(item: MethodWeight) {
  try {
    await learningApi.updateWeight(item.id, item.weight)
    ElMessage.success('权重已更新')
  } catch (error) {
    ElMessage.error('更新失败')
  }
}

async function retrain() {
  try {
    await learningApi.retrain()
    ElMessage.success('模型训练完成')
  } catch (error) {
    ElMessage.error('训练失败')
  }
}

onMounted(fetchData)
</script>

<style lang="scss" scoped>
@use '@/assets/styles/variables.scss' as *;

.learning-page {
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

.weights-list {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.weight-item {
  display: flex;
  align-items: center;
  gap: 32px;
  padding: 20px;
  background: rgba($primary-color, 0.05);
  border-radius: $radius-lg;
}

.weight-info {
  width: 200px;

  h4 {
    font-size: 16px;
    font-weight: 600;
    margin: 0 0 4px;
    color: $text-primary;
  }

  p {
    font-size: 13px;
    color: $text-muted;
    margin: 0;
  }
}

.weight-control {
  flex: 1;
}
</style>
