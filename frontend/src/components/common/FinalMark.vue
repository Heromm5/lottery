<script setup lang="ts">
import { Star } from '@element-plus/icons-vue'

defineProps<{
  isFinal?: number | null
}>()
</script>

<template>
  <el-tooltip v-if="isFinal === 1" content="最终预测" placement="top">
    <span class="final-mark"><el-icon><Star /></el-icon></span>
  </el-tooltip>
  <span v-else class="pending-text">--</span>
</template>

<style lang="scss" scoped>
@use '@/assets/styles/variables.scss' as *;

.final-mark {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  color: $accent-orange;
  font-size: 14px;
  cursor: default;
}

.pending-text {
  color: $text-muted;
}
</style>
