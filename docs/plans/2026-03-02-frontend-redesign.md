# 前端全面重构实施计划

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** 将13个前端页面从科技感风格重构为简约专业深色风格，统一设计语言，提升用户体验

**Architecture:** 
- 保持现有组件结构不变
- 统一使用新的样式变量系统
- 简化视觉效果，去除渐变和光晕
- 采用更大圆角、更多留白、清晰层次

**Tech Stack:** Vue 3 + Element Plus + ECharts + SCSS

---

## 重构原则

1. **保持功能不变** - 只改样式，不改功能逻辑
2. **统一设计语言** - 所有页面使用一致的色彩、间距、圆角
3. **渐进式重构** - 每个页面独立重构，互不影响
4. **验证构建** - 每重构一个页面，验证构建通过

---

## 样式变量（已更新）

```scss
// 核心变量
$bg-dark: #0f0f1a;
$bg-dark-lighter: #1a1a2e;
$bg-card: #16213e;

$primary-color: #4a90d9;
$accent-cyan: #00d4aa;
$text-primary: #ffffff;
$text-secondary: #a0aec0;
$text-muted: #64748b;

$border-color: #2a3a5a;
$radius-lg: 14px;
```

---

## 任务清单

### Phase 1: 首页重构

#### Task 1: 首页 (home/index.vue)

**Files:**
- Modify: `frontend/src/views/home/index.vue`

**Changes:**
- 移除渐变标题文字，使用纯白色
- 简化统计卡片样式，去除渐变背景
- 简化快速入口卡片，减少阴影
- 更新图表配色为更柔和的色调

**Step 1: 编辑首页样式**

```vue
<style lang="scss" scoped>
// 移除 .gradient-text 渐变效果
.welcome-title {
  color: $text-primary;
}

// 简化统计卡片
.stat-card {
  background: $bg-card;
  border: 1px solid $border-color;
  &:hover {
    border-color: $primary-color;
  }
}

// 简化图表配色
color: ['#4a90d9', '#00d4aa', '#8b5cf6', '#f59e0b', '#ef4444']
```

**Step 2: 构建验证**

Run: `cd frontend && npm run build`
Expected: SUCCESS

---

### Phase 2: 预测模块重构

#### Task 2: 预测主页 (prediction/index.vue)

**Files:**
- Modify: `frontend/src/views/prediction/index.vue`

**Changes:**
- 简化号码球样式
- 统一按钮和输入框样式
- 优化布局间距

#### Task 3: 预测历史 (prediction/history.vue)

**Files:**
- Modify: `frontend/src/views/prediction/history.vue`

---

### Phase 3: 数据分析模块重构

#### Task 4: 分析主页 (analysis/index.vue)

**Files:**
- Modify: `frontend/src/views/analysis/index.vue`

#### Task 5: 频率分析 (analysis/frequency.vue)

**Files:**
- Modify: `frontend/src/views/analysis/frequency.vue`
- 简化图表配色

#### Task 6: 遗漏分析 (analysis/missing.vue)

**Files:**
- Modify: `frontend/src/views/analysis/missing.vue`

#### Task 7: 走势分析 (analysis/trend.vue)

**Files:**
- Modify: `frontend/src/views/analysis/trend.vue`

#### Task 8: 关联分析 (analysis/association.vue)

**Files:**
- Modify: `frontend/src/views/analysis/association.vue`

#### Task 9: 尾数分析 (analysis/digit.vue)

**Files:**
- Modify: `frontend/src/views/analysis/digit.vue`

#### Task 10: 区间分析 (analysis/zone.vue)

**Files:**
- Modify: `frontend/src/views/analysis/zone.vue`

---

### Phase 4: 验证模块重构

#### Task 11: 验证中心 (verification/index.vue)

**Files:**
- Modify: `frontend/src/views/verification/index.vue`
- 简化表格样式
- 统一卡片样式

---

### Phase 5: 学习模块重构

#### Task 12: 模型学习 (learning/index.vue)

**Files:**
- Modify: `frontend/src/views/learning/index.vue`

---

### Phase 6: 数据管理模块重构

#### Task 13: 数据管理 (lottery/index.vue)

**Files:**
- Modify: `frontend/src/views/lottery/index.vue`

---

## 通用样式更新指南

每个页面需要检查以下样式：

### 1. 卡片样式
```scss
.tech-card {
  background: $bg-card;
  border: 1px solid $border-color;
  border-radius: $radius-lg;
  // 移除 backdrop-filter
  // 移除 hover 时的 glow 效果
}
```

### 2. 图表配色
```scss
// 使用更柔和的配色方案
color: [
  '#4a90d9',  // 品牌蓝
  '#00d4aa',  // 青色
  '#8b5cf6',  // 紫色
  '#f59e0b',  // 橙色
  '#ef4444',  // 红色
  '#10b981',  // 绿色
]
```

### 3. 按钮样式
```scss
.el-button--primary {
  background: $primary-color;  // 纯色，非渐变
  border: none;
}
```

### 4. 文字样式
```scss
// 标题使用纯白色
h1, h2, h3 {
  color: $text-primary;
}

// 描述使用灰色
p, .desc {
  color: $text-secondary;
}
```

---

## 构建验证

每完成一个页面，运行构建验证：

```bash
cd frontend && npm run build
```

确保无错误后继续下一个任务。

---

## 完成标准

- [ ] 所有13个页面重构完成
- [ ] 构建通过无错误
- [ ] 设计风格统一
- [ ] 页面功能正常
