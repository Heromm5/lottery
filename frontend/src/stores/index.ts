import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useAppStore = defineStore('app', () => {
  // 状态
  const sidebarCollapsed = ref(false)
  const theme = ref<'light' | 'dark'>('dark')
  const loading = ref(false)

  // 计算属性
  const isDark = computed(() => theme.value === 'dark')

  // 方法
  function toggleSidebar() {
    sidebarCollapsed.value = !sidebarCollapsed.value
  }

  function toggleTheme() {
    theme.value = theme.value === 'dark' ? 'light' : 'dark'
  }

  function setLoading(value: boolean) {
    loading.value = value
  }

  return {
    sidebarCollapsed,
    theme,
    loading,
    isDark,
    toggleSidebar,
    toggleTheme,
    setLoading
  }
})
