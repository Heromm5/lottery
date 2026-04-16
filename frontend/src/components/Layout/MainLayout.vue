<template>
  <div class="app-layout">
    <!-- 顶部导航栏 -->
    <header class="app-header">
      <div class="header-left">
        <!-- Logo -->
        <router-link to="/" class="logo">
          <svg viewBox="0 0 100 100" class="logo-icon">
            <circle cx="50" cy="50" r="45" fill="#4a90d9" />
            <circle cx="30" cy="40" r="8" fill="#fff" opacity="0.9" />
            <circle cx="50" cy="35" r="8" fill="#fff" opacity="0.9" />
            <circle cx="70" cy="40" r="8" fill="#fff" opacity="0.9" />
            <circle cx="38" cy="55" r="8" fill="#fff" opacity="0.9" />
            <circle cx="62" cy="55" r="8" fill="#fff" opacity="0.9" />
            <circle cx="50" cy="68" r="8" fill="#fbbf24" />
            <circle cx="65" cy="68" r="8" fill="#fbbf24" />
          </svg>
          <span class="logo-text">大乐透分析</span>
        </router-link>

        <!-- 主导航 -->
        <nav class="main-nav">
          <router-link
            v-for="item in menuItems"
            :key="item.path"
            :to="item.path"
            class="nav-item"
            :class="{ active: isActive(item.path) }"
          >
            <el-icon><component :is="item.icon" /></el-icon>
            <span>{{ item.name }}</span>
            <el-icon v-if="item.children" class="nav-arrow">
              <ArrowDown />
            </el-icon>
            <!-- 下拉菜单 -->
            <div v-if="item.children" class="nav-dropdown">
              <router-link
                v-for="child in item.children"
                :key="child.path"
                :to="child.path"
                class="dropdown-item"
                :class="{ active: route.path === child.path }"
              >
                <el-icon><component :is="child.icon" /></el-icon>
                <span>{{ child.name }}</span>
              </router-link>
            </div>
          </router-link>
        </nav>
      </div>

      <div class="header-right">
        <el-button :icon="Refresh" circle size="small" @click="refresh" />
        <el-button :icon="Bell" circle size="small" />
        <div class="user-info">
          <el-avatar :size="28" src="https://api.dicebear.com/7.x/avataaars/svg?seed=admin" />
        </div>
      </div>
    </header>

    <!-- 面包屑 + 主内容区 -->
    <div class="app-body">
      <!-- 面包屑导航 -->
      <div class="breadcrumb-wrap">
        <el-breadcrumb separator="/">
          <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
          <el-breadcrumb-item v-if="currentParent">
            {{ currentParent }}
          </el-breadcrumb-item>
          <el-breadcrumb-item v-if="currentPageTitle !== '首页'">
            {{ currentPageTitle }}
          </el-breadcrumb-item>
        </el-breadcrumb>
      </div>

      <!-- 主内容区 -->
      <main class="app-content">
        <router-view v-slot="{ Component }">
          <transition name="fade-slide" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </main>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  Refresh,
  Bell,
  DataLine,
  MagicStick,
  TrendCharts,
  Setting,
  Check,
  Folder,
  ArrowDown,
  Histogram,
  Minus,
  DataAnalysis,
  Connection,
  Warning
} from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()

interface NavItem {
  name: string
  path: string
  icon: any
  children?: NavItem[]
}

const menuItems: NavItem[] = [
  { name: '首页', path: '/', icon: DataLine },
  { name: '智能预测', path: '/prediction', icon: MagicStick },
  { 
    name: '数据分析', 
    path: '/analysis', 
    icon: TrendCharts,
    children: [
      { name: '频率统计', path: '/analysis/frequency', icon: Histogram },
      { name: '遗漏分析', path: '/analysis/missing', icon: Minus },
      { name: '走势分析', path: '/analysis/trend', icon: DataAnalysis },
      { name: '关联分析', path: '/analysis/association', icon: Connection }
    ]
  },
  { name: '验证中心', path: '/verification', icon: Check },
  { name: '模型学习', path: '/learning', icon: Setting },
  { name: '数据管理', path: '/lottery', icon: Folder },
  { 
    name: 'AI 增强', 
    path: '/ai', 
    icon: MagicStick,
    children: [
      { name: '异常监控', path: '/ai/anomaly-monitor', icon: Warning }
    ]
  }
]

const currentPageTitle = computed(() => {
  return route.meta?.title as string || ''
})

const currentParent = computed(() => {
  const path = route.path
  if (path.startsWith('/analysis')) return '数据分析'
  if (path.startsWith('/prediction')) return '智能预测'
  if (path.startsWith('/ai')) return 'AI 增强'
  return ''
})

function isActive(path: string): boolean {
  if (path === '/') return route.path === '/'
  return route.path.startsWith(path)
}

function refresh() {
  router.replace({ path: '/redirect' + route.fullPath })
}
</script>

<style lang="scss" scoped>
@use '@/assets/styles/variables.scss' as *;

.app-layout {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  background: transparent;
}

// 顶部导航
.app-header {
  height: 60px;
  background: $bg-card;
  border-bottom: 1px solid $border-color;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  position: sticky;
  top: 0;
  z-index: 100;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 32px;
}

.logo {
  display: flex;
  align-items: center;
  gap: 10px;
  text-decoration: none;
}

.logo-icon {
  width: 32px;
  height: 32px;
}

.logo-text {
  font-size: 16px;
  font-weight: 600;
  color: $text-primary;
}

// 主导航
.main-nav {
  display: flex;
  align-items: center;
  gap: 4px;
}

.nav-item {
  position: relative;
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  border-radius: $radius-md;
  color: $text-secondary;
  text-decoration: none;
  font-size: 14px;
  transition: all $transition-fast;

  &:hover {
    background: rgba($primary-color, 0.1);
    color: $text-primary;

    .nav-dropdown {
      opacity: 1;
      visibility: visible;
      transform: translateY(0);
    }
  }

  &.active {
    background: rgba($primary-color, 0.15);
    color: $primary-color;
  }

  .nav-arrow {
    font-size: 12px;
    margin-left: 2px;
  }
}

// 下拉菜单
.nav-dropdown {
  position: absolute;
  top: 100%;
  left: 0;
  min-width: 160px;
  background: $bg-card;
  border: 1px solid $border-color;
  border-radius: $radius-md;
  box-shadow: $shadow-md;
  padding: 6px;
  opacity: 0;
  visibility: hidden;
  transform: translateY(-8px);
  transition: all $transition-fast;
  z-index: 200;
}

.dropdown-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 14px;
  border-radius: $radius-sm;
  color: $text-secondary;
  text-decoration: none;
  font-size: 13px;
  transition: all $transition-fast;

  &:hover {
    background: rgba($primary-color, 0.1);
    color: $text-primary;
  }

  &.active {
    background: rgba($primary-color, 0.15);
    color: $primary-color;
  }
}

.header-right {
  display: flex;
  align-items: center;
  gap: 8px;
}

// 内容区域
.app-body {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.breadcrumb-wrap {
  padding: 12px 24px;
  background: rgba($bg-dark-lighter, 0.5);
  border-bottom: 1px solid rgba($border-color, 0.5);

  :deep(.el-breadcrumb__item) {
    .el-breadcrumb__inner {
      color: $text-muted;
      font-size: 13px;

      &.is-link:hover {
        color: $primary-color;
      }
    }

    &:last-child .el-breadcrumb__inner {
      color: $text-secondary;
    }
  }

  :deep(.el-breadcrumb__separator) {
    color: $text-muted;
  }
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
  transform: translateY(10px);
}

.fade-slide-leave-to {
  opacity: 0;
  transform: translateY(-10px);
}

// 响应式
@media (max-width: $md) {
  .logo-text {
    display: none;
  }

  .nav-item span:not(.el-icon) {
    display: none;
  }

  .nav-item {
    padding: 8px 12px;
  }
}
</style>
