<template>
  <div class="analysis-page">
    <div class="page-header">
      <div class="header-content">
        <h1>数据分析</h1>
        <p>深入分析历史数据，发现号码规律</p>
      </div>
      <div class="header-tabs">
        <el-radio-group v-model="activeTab">
          <el-radio-button value="frequency">频率统计</el-radio-button>
          <el-radio-button value="missing">遗漏分析</el-radio-button>
          <el-radio-button value="trend">走势分析</el-radio-button>
          <el-radio-button value="association">关联分析</el-radio-button>
        </el-radio-group>
      </div>
    </div>

    <component :is="currentComponent" />
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import Frequency from './frequency.vue'
import Missing from './missing.vue'
import Trend from './trend.vue'
import Association from './association.vue'

const activeTab = ref('frequency')

const currentComponent = computed(() => {
  const map: Record<string, any> = {
    frequency: Frequency,
    missing: Missing,
    trend: Trend,
    association: Association
  }
  return map[activeTab.value] || Frequency
})
</script>

<style lang="scss" scoped>
@use '@/assets/styles/variables.scss' as *;

.analysis-page {
  display: flex;
  flex-direction: column;
  gap: 24px;
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
  }
}
</style>
