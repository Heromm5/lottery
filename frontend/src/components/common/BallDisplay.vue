<script setup lang="ts">
import { computed } from 'vue'
import { parseBalls } from '@/composables/useBalls'

const props = withDefaults(defineProps<{
  frontBalls: string | number[] | null
  backBalls: string | number[] | null
  size?: 'xs' | 'sm' | 'md' | 'lg'
  hitFront?: number[]
  hitBack?: number[]
}>(), {
  size: 'sm',
  hitFront: () => [],
  hitBack: () => []
})

const front = computed(() => parseBalls(props.frontBalls))
const back = computed(() => parseBalls(props.backBalls))

const sizeClass = computed(() => props.size === 'md' ? '' : `ball--${props.size}`)

function isFrontHit(n: number): boolean {
  return props.hitFront.length > 0 && props.hitFront.includes(n)
}

function isBackHit(n: number): boolean {
  return props.hitBack.length > 0 && props.hitBack.includes(n)
}
</script>

<template>
  <div class="ball-display">
    <span
      v-for="(n, idx) in front"
      :key="'f-' + idx"
      class="ball ball--front"
      :class="[sizeClass, { 'ball--hit': isFrontHit(n) }]"
    >{{ n }}</span>
    <span v-if="back.length" class="separator">+</span>
    <span
      v-for="(n, idx) in back"
      :key="'b-' + idx"
      class="ball ball--back"
      :class="[sizeClass, { 'ball--hit': isBackHit(n) }]"
    >{{ n }}</span>
  </div>
</template>

<style lang="scss" scoped>
@use '@/assets/styles/variables.scss' as *;

.ball-display {
  display: inline-flex;
  align-items: center;
  gap: 3px;
  flex-wrap: wrap;
}

.separator {
  color: $text-muted;
  font-size: 12px;
  margin: 0 2px;
}

.ball--hit {
  box-shadow: 0 0 0 2px $accent-green, 0 0 8px rgba($accent-green, 0.5);
}
</style>
