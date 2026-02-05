import { createRouter, createWebHistory, RouteRecordRaw } from 'vue-router'

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    name: 'Home',
    component: () => import('@/views/home/index.vue'),
    meta: { title: '首页', icon: 'DataLine' }
  },
  {
    path: '/prediction',
    name: 'Prediction',
    component: () => import('@/views/prediction/index.vue'),
    meta: { title: '智能预测', icon: 'MagicStick' }
  },
  {
    path: '/prediction/history',
    name: 'PredictionHistory',
    component: () => import('@/views/prediction/history.vue'),
    meta: { title: '预测历史', icon: 'Clock' }
  },
  {
    path: '/analysis',
    name: 'Analysis',
    component: () => import('@/views/analysis/index.vue'),
    meta: { title: '数据分析', icon: 'TrendCharts' }
  },
  {
    path: '/analysis/frequency',
    name: 'Frequency',
    component: () => import('@/views/analysis/frequency.vue'),
    meta: { title: '频率统计', icon: 'Histogram' }
  },
  {
    path: '/analysis/missing',
    name: 'Missing',
    component: () => import('@/views/analysis/missing.vue'),
    meta: { title: '遗漏分析', icon: 'Minus' }
  },
  {
    path: '/analysis/trend',
    name: 'Trend',
    component: () => import('@/views/analysis/trend.vue'),
    meta: { title: '走势分析', icon: 'Line' }
  },
  {
    path: '/analysis/association',
    name: 'Association',
    component: () => import('@/views/analysis/association.vue'),
    meta: { title: '关联分析', icon: 'Connection' }
  },
  {
    path: '/verification',
    name: 'Verification',
    component: () => import('@/views/verification/index.vue'),
    meta: { title: '验证中心', icon: 'Check' }
  },
  {
    path: '/learning',
    name: 'Learning',
    component: () => import('@/views/learning/index.vue'),
    meta: { title: '模型学习', icon: 'Setting' }
  },
  {
    path: '/lottery',
    name: 'Lottery',
    component: () => import('@/views/lottery/index.vue'),
    meta: { title: '数据管理', icon: 'Folder' }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, _from, next) => {
  document.title = `${to.meta.title || ''} - 大乐透数据分析与预测系统`
  next()
})

export default router
