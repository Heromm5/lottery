# 大乐透数据分析系统 - 前端

## 技术栈

- **Vue 3** - 渐进式JavaScript框架
- **TypeScript** - 类型安全的JavaScript超集
- **Vite** - 下一代前端构建工具
- **Element Plus** - Vue 3 组件库
- **Pinia** - Vue状态管理
- **Axios** - HTTP客户端
- **ECharts** - 数据可视化库
- **SCSS** - CSS预处理器

## 项目结构

```
frontend/
├── public/              # 静态资源
├── src/
│   ├── api/            # API接口封装
│   │   ├── axios.ts    # Axios封装
│   │   ├── index.ts    # 导出入口
│   │   └── modules/    # 业务API模块
│   │       ├── analysis.ts
│   │       ├── lottery.ts
│   │       ├── prediction.ts
│   │       ├── verification.ts
│   │       └── learning.ts
│   ├── assets/         # 资源文件
│   │   └── styles/    # 样式文件
│   │       ├── index.scss
│   │       └── variables.scss
│   ├── components/     # 公共组件
│   │   └── Layout/    # 布局组件
│   │       └── MainLayout.vue
│   ├── router/         # 路由配置
│   │   └── index.ts
│   ├── stores/         # Pinia状态管理
│   │   └── index.ts
│   ├── types/          # TypeScript类型定义
│   │   └── index.ts
│   ├── views/          # 页面组件
│   │   ├── home/       # 首页
│   │   ├── prediction/ # 预测中心
│   │   ├── analysis/   # 数据分析
│   │   ├── verification/# 验证中心
│   │   ├── learning/   # 模型学习
│   │   └── lottery/    # 数据管理
│   ├── App.vue         # 根组件
│   ├── main.ts         # 入口文件
│   └── vite-env.d.ts   # Vite类型声明
├── index.html          # HTML模板
├── vite.config.ts      # Vite配置
├── tsconfig.json       # TypeScript配置
├── .eslintrc.cjs       # ESLint配置
├── .prettierrc         # Prettier配置
└── package.json        # 项目配置
```

## 开发

### 安装依赖

```bash
npm install
```

### 启动开发服务器

```bash
npm run dev
```

开发服务器将在 `http://localhost:3000` 启动

### 构建生产版本

```bash
npm run build
```

构建产物将输出到 `../src/main/resources/static/` 目录

### 代码检查

```bash
# 代码检查
npm run lint

# 代码格式化
npm run format
```

## 功能模块

1. **首页** - 最新开奖、统计卡片、快速入口
2. **智能预测** - 单期预测、批量预测、算法选择
3. **数据分析** - 频率统计、遗漏分析、走势分析、关联分析
4. **验证中心** - 准确率统计
5. **模型学习** - 权重配置、模型训练
6. **数据管理** - 开奖数据CRUD

## 设计风格

采用**科技感数据大屏**风格：
- 深色主题背景
- 霓虹光效渐变
- 玻璃拟态卡片
- ECharts动态图表
- 响应式布局
