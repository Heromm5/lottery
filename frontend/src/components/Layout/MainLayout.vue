<template>
  <div class="app-layout">
    <!-- 侧边栏 -->
    <aside class="app-sidebar" :class="{ collapsed: isCollapsed }">
      <div class="sidebar-header">
        <div class="logo">
          <svg viewBox="0 0 100 100" class="logo-icon">
            <defs>
              <linearGradient id="logoGrad" x1="0%" y1="0%" x2="100%" y2="100%">
                <stop offset="0%" style="stop-color: #667eea" />
                <stop offset="100%" style="stop-color: #764ba2" />
              </linearGradient>
            </defs>
            <circle cx="50" cy="50" r="45" fill="url(#logoGrad)" />
            <circle cx="30" cy="40" r="8" fill="#fff" opacity="0.9" />
            <circle cx="50" cy="35" r="8" fill="#fff" opacity="0.9" />
            <circle cx="70" cy="40" r="8" fill="#fff" opacity="0.9" />
            <circle cx="38" cy="55" r="8" fill="#fff" opacity="0.9" />
            <circle cx="62" cy="55" r="8" fill="#fff" opacity="0.9" />
            <circle cx="50" cy="68" r="8" fill="#fbbf24" />
            <circle cx="65" cy="68" r="8" fill="#fbbf24" />
          </svg>
          <span v-if="!isCollapsed" class="logo-text">大乐透分析</span>
        </div>
      </div>

      <nav class="sidebar-nav">
        <router-link
          v-for="item in menuItems"
          :key="item.path"
          :to="item.path"
          class="nav-item"
          :class="{ active: isActive(item.path) }"
        >
          <el-icon class="nav-icon"><component :is="item.icon" /></el-icon>
          <span v-if="!isCollapsed" class="nav-text">{{ item.name }}</span>
        </router-link>
      </nav>

      <div class="sidebar-footer">
        <button class="collapse-btn" @click="toggleCollapse">
          <el-icon>
            <Fold v-if="!isCollapsed" />
            <Expand v-else />
          </el-icon>
        </button>
      </div>
    </aside>

    <!-- 主内容区 -->
    <main class="app-main" :class="{ expanded: isCollapsed }">
      <header class="app-header">
        <div class="header-left">
          <h1 class="page-title">{{ currentPageTitle }}</h1>
        </div>
        <div class="header-right">
          <el-button :icon="Refresh" circle @click="refresh" />
          <el-button :icon="Bell" circle />
          <div class="user-info">
            <el-avatar :size="32" src="https://api.dicebear.com/7.x/avataaars/svg?seed=admin" />
          </div>
        </div>
      </header>

      <div class="app-content">
        <router-view v-slot="{ Component }">
          <transition name="fade-slide" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </div>
    </main>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  Fold,
  Expand,
  Refresh,
  Bell,
  DataLine,
  MagicStick,
  TrendCharts,
  Setting,
  Check,
  Folder
} from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()

const isCollapsed = ref(false)

const menuItems = [
  { name: '首页', path: '/', icon: DataLine },
  { name: '智能预测', path: '/prediction', icon: MagicStick },
  { name: '数据分析', path: '/analysis', icon: TrendCharts },
  { name: '验证中心', path: '/verification', icon: Check },
  { name: '模型学习', path: '/learning', icon: Setting },
  { name: '数据管理', path: '/lottery', icon: Folder }
]

const currentPageTitle = computed(() => {
  const item = menuItems.find(m => m.path === route.path)
  if (item) return item.name
  const subMenus: Record<string, string> = {
    '/prediction/history': '预测历史',
    '/analysis/frequency': '频率统计',
    '/analysis/missing': '遗漏分析',
    '/analysis/trend': '走势分析',
    '/analysis/association': '关联分析'
  }
  return subMenus[route.path] || route.meta?.title || ''
})

function isActive(path: string): boolean {
  if (path === '/') return route.path === '/'
  return route.path.startsWith(path)
}

function toggleCollapse() {
  isCollapsed.value = !isCollapsed.value
}

function refresh() {
  router.replace({ path: '/redirect' + route.fullPath })
}
</script>

<style lang="scss" scoped>
@use '@/assets/styles/variables.scss' as *;

.app-layout {
  display: flex;
  min-height: 100vh;
  background: transparent;
}

// 侧边栏
.app-sidebar {
  width: 220px;
  background: $bg-card;
  border-right: 1px solid $border-color;
  display: flex;
  flex-direction: column;
  transition: width $transition-normal;
  backdrop-filter: blur(10px);

  &.collapsed {
    width: 64px;

    .logo-text,
    .nav-text {
      display: none;
    }
  }
}

.sidebar-header {
  padding: 20px;
  border-bottom: 1px solid $border-color;
}

.logo {
  display: flex;
  align-items: center;
  gap: 12px;
}

.logo-icon {
  width: 36px;
  height: 36px;
  flex-shrink: 0;
}

.logo-text {
  font-size: 18px;
  font-weight: 700;
  background: linear-gradient(90deg, $primary-color, $accent-cyan);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

// 导航
.sidebar-nav {
  flex: 1;
  padding: 16px 12px;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  border-radius: $radius-md;
  color: $text-secondary;
  text-decoration: none;
  transition: all $transition-fast;

  &:hover {
    background: rgba($primary-color, 0.1);
    color: $primary-color;
  }

  &.active {
    background: linear-gradient(135deg, rgba($primary-color, 0.2), rgba($primary-light, 0.2));
    color: $primary-color;
    border-left: 3px solid $primary-color;
  }
}

.nav-icon {
  font-size: 20px;
  flex-shrink: 0;
}

.nav-text {
  font-size: 14px;
  font-weight: 500;
}

.sidebar-footer {
  padding: 16px;
  border-top: 1px solid $border-color;
}

.collapse-btn {
  width: 100%;
  padding: 8px;
  background: transparent;
  border: 1px solid $border-color;
  border-radius: $radius-md;
  color: $text-muted;
  cursor: pointer;
  transition: all $transition-fast;

  &:hover {
    background: rgba($primary-color, 0.1);
    color: $primary-color;
    border-color: $primary-color;
  }
}

// 主内容区
.app-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
  transition: margin-left $transition-normal;

  &.expanded {
    margin-left: 0;
  }
}

.app-header {
  height: 64px;
  background: $bg-card;
  border-bottom: 1px solid $border-color;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  backdrop-filter: blur(10px);
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.page-title {
  font-size: 20px;
  font-weight: 600;
  color: $text-primary;
  margin: 0;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.user-info {
  margin-left: 8px;
}

.app-content {
  flex: 1;
  padding: 24px;
  overflow-y: auto;
}

// 过渡动画
.fade-slide-enter-active,
.fade-slide-leave-active {
  transition: all 0.3s ease;
}

.fade-slide-enter-from {
  opacity: 0;
  transform: translateY(20px);
}

.fade-slide-leave-to {
  opacity: 0;
  transform: translateY(-20px);
}

// 响应式
@media (max-width: $md) {
  .app-sidebar {
    position: fixed;
    left: 0;
    top: 0;
    bottom: 0;
    z-index: 1000;
    transform: translateX(-100%);

    &.collapsed {
      transform: translateX(-100%);
    }
  }
}
</style>
