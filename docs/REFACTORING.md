# 大乐透数据分析系统 - 前后端分离重构

## 项目概述

将原有的 Spring Boot + Thymeleaf 单体应用重构为前后端分离架构。

## 技术选型

| 层级 | 技术栈 |
|------|--------|
| 后端 | Spring Boot 2.7.18 + MyBatis-Plus + MySQL |
| 前端 | Vue 3 + TypeScript + Vite + Element Plus |
| 构建 | Maven (后端) + Vite (前端) |
| 可视化 | ECharts |

## 项目结构

```
lottery-java/
├── src/main/java/com/hobart/lottery/
│   ├── common/              # 公共模块
│   │   ├── result/         # 统一响应封装
│   │   └── exception/       # 异常处理
│   ├── controller/          # REST API控制器
│   ├── service/            # 服务层
│   ├── entity/            # 实体类
│   └── ...
├── frontend/               # Vue 3前端项目
│   ├── src/
│   │   ├── api/           # API接口封装
│   │   ├── views/         # 页面组件
│   │   ├── components/    # 公共组件
│   │   ├── stores/       # 状态管理
│   │   └── ...
│   └── ...
└── pom.xml
```

## 已完成工作

### 1. 前端工程搭建
- ✅ Vue 3 + TypeScript + Vite 项目初始化
- ✅ Element Plus 组件库集成
- ✅ Pinia 状态管理配置
- ✅ Vue Router 路由配置
- ✅ Axios 请求封装（统一错误处理、代理配置）
- ✅ ESLint + Prettier 代码规范

### 2. 前端页面开发
- ✅ **首页** - 科技感数据大屏风格，最新开奖、统计卡片
- ✅ **预测中心** - 智能预测、批量预测、历史记录
- ✅ **数据分析** - 频率统计、遗漏分析、走势分析、关联分析
- ✅ **验证中心** - 准确率统计
- ✅ **模型学习** - 权重配置
- ✅ **数据管理** - 开奖数据CRUD

### 3. 后端API规范化
- ✅ 统一响应包装类 `Result<T>`
- ✅ 响应状态码枚举 `ResultCode`
- ✅ 全局异常处理器 `GlobalExceptionHandler`
- ✅ 业务异常 `BusinessException`

### 4. 样式设计
- ✅ 科技感深色主题
- ✅ 渐变色彩方案
- ✅ 玻璃拟态卡片效果
- ✅ 响应式布局

## 启动方式

### 方式一：分别启动

**后端**
```bash
cd lottery-java
mvn spring-boot:run
```

**前端**
```bash
cd frontend
npm install
npm run dev
```

### 方式二：整体构建

```bash
cd lottery-java
mvn clean package
java -jar target/lottery-java-1.0-SNAPSHOT.jar
```

前端构建产物会自动复制到 `src/main/resources/static/` 目录。

## API接口规范

所有API统一返回格式：

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": { ... }
}
```

## 前端运行截图说明

首页采用科技感数据大屏设计：
- 深色背景 + 网格纹理
- 渐变色统计卡片
- 动态ECharts图表
- 导航侧边栏（可折叠）
- 响应式布局

## 待完善

1. 后端Controller层需要适配新的 `Result<T>` 返回格式
2. 前端依赖安装（如npm install失败）
3. 真实数据联调测试
4. 生产环境Nginx配置
