<script setup lang="ts">
import { computed } from 'vue'

const props = defineProps<{
  prize?: string | null
}>()

const WINNING_PRIZES = ['一等奖', '二等奖', '三等奖', '四等奖', '五等奖', '六等奖', '七等奖']

const isWin = computed(() => !!props.prize && WINNING_PRIZES.includes(props.prize))

const levelType = computed(() => {
  const map: Record<string, string> = {
    '一等奖': 'danger',
    '二等奖': 'warning',
    '三等奖': 'success',
    '四等奖': 'success',
    '五等奖': 'info',
    '六等奖': 'info',
    '七等奖': 'info'
  }
  return props.prize ? (map[props.prize] || 'info') : 'info'
})
</script>

<template>
  <span v-if="prize" :class="['prize-badge', { 'prize-badge--win': isWin }]" :data-level="levelType">
    {{ prize }}
  </span>
  <span v-else class="pending-text">--</span>
</template>

<style lang="scss" scoped>
@use '@/assets/styles/variables.scss' as *;

.prize-badge {
  display: inline-block;
  padding: 4px 10px;
  border-radius: $radius-sm;
  font-size: 12px;
  color: $text-secondary;
  background: rgba($bg-dark-lighter, 0.8);
  border: 1px solid $border-color;
  transition: $transition-fast;
}

.prize-badge--win {
  font-weight: 700;
  font-size: 13px;
  letter-spacing: 0.5px;
  border-width: 1.5px;
  box-shadow: $shadow-sm;

  &[data-level='danger'] {
    color: #fecaca;
    background: linear-gradient(135deg, rgba(239, 68, 68, 0.35), rgba(185, 28, 28, 0.25));
    border-color: rgba(239, 68, 68, 0.6);
    text-shadow: 0 0 12px rgba(239, 68, 68, 0.4);
  }

  &[data-level='warning'] {
    color: #fde68a;
    background: linear-gradient(135deg, rgba(245, 158, 11, 0.35), rgba(180, 83, 9, 0.25));
    border-color: rgba(245, 158, 11, 0.6);
    text-shadow: 0 0 10px rgba(245, 158, 11, 0.35);
  }

  &[data-level='success'] {
    color: #a7f3d0;
    background: linear-gradient(135deg, rgba(16, 185, 129, 0.35), rgba(5, 150, 105, 0.25));
    border-color: rgba(16, 185, 129, 0.6);
    text-shadow: 0 0 10px rgba(16, 185, 129, 0.35);
  }

  &[data-level='info'] {
    color: #bae6fd;
    background: linear-gradient(135deg, rgba(59, 130, 246, 0.3), rgba(29, 78, 216, 0.2));
    border-color: rgba(59, 130, 246, 0.5);
  }
}

.pending-text {
  color: $text-muted;
}
</style>
