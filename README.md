# 大乐透数据分析与预测系统

基于 Spring Boot + Vue 3 的大乐透历史数据分析与智能预测系统，包含 10 种预测算法、持续学习权重调整、历史回测验证等功能。

## 技术栈

| 层级 | 技术 |
|------|------|
| 后端 | Spring Boot 2.7.18, Java 17, MyBatis-Plus 3.5.5 |
| 前端 | Vue 3, TypeScript, Vite, Element Plus, ECharts |
| 数据库 | MySQL 8.0 |
| 缓存 | Caffeine |

## 环境要求

- **JDK 17+**
- **Node.js 18+** (前端开发)
- **MySQL 8.0+**
- **Maven 3.8+**

## 快速开始

### 1. 初始化数据库

```sql
CREATE DATABASE lottery DEFAULT CHARACTER SET utf8mb4;
```

执行 `src/main/resources/db/schema.sql` 创建表结构。

**从旧版库升级**：若 `prediction_records` 尚无定胆相关列，可执行 [`sql/migration_prediction_records_generation_mode.sql`](sql/migration_prediction_records_generation_mode.sql)（与 `schema.sql` 中定义一致）。已手动执行过相同 `ALTER` 的环境无需重复执行。

定胆落库字段说明：

- `generation_mode`：`RANDOM`（默认，算法随机生成） / `PINNED`（定胆生成）
- `locked_front_balls` / `locked_back_balls`：当次胆码，逗号分隔；非定胆为 `NULL`

### 2. 配置数据库连接

复制 `src/main/resources/application-local.yml`，填写实际数据库地址和密码，然后以 `local` profile 启动：

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

或设置环境变量：

```bash
export DB_URL=jdbc:mysql://localhost:3306/lottery?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai
export DB_USERNAME=root
export DB_PASSWORD=your_password
mvn spring-boot:run
```

### 3. 前端开发

```bash
cd frontend
npm install
npm run dev
```

前端开发服务器运行在 `http://localhost:3000`，API 请求代理到后端 `http://localhost:9060`。

### 4. 构建部署

前端构建输出自动到 `src/main/resources/static/`：

```bash
cd frontend && npm run build
cd .. && mvn package -DskipTests
java -jar target/lottery-java-1.0-SNAPSHOT.jar
```

### 5. Docker 一键启动

```bash
docker compose up -d
```

访问 `http://localhost:9060`。

## 项目结构

```
lottery-java/
├── src/main/java/com/hobart/lottery/
│   ├── controller/api/     # REST API 控制器
│   ├── service/            # 业务逻辑层
│   │   ├── analysis/       # 数据分析（频率、遗漏、走势、关联）
│   │   └── learning/       # 持续学习（权重自适应）
│   ├── predictor/          # 10 种预测算法（Spring Bean 自动注册）
│   ├── entity/             # 数据库实体
│   ├── mapper/             # MyBatis-Plus Mapper
│   ├── dto/                # 数据传输对象
│   ├── domain/model/       # 领域模型（枚举、值对象）
│   ├── config/             # 配置类
│   └── common/             # 通用工具（Result、异常处理）
├── src/main/resources/
│   ├── db/                 # 数据库 DDL + 迁移脚本
│   └── application.yml     # 应用配置
├── frontend/               # Vue 3 前端
│   ├── src/
│   │   ├── views/          # 页面视图
│   │   ├── components/common/  # 公共组件（BallDisplay、PrizeTag、FinalMark）
│   │   ├── composables/    # 组合式函数（useBalls、useDateTime）
│   │   ├── api/            # API 接口定义
│   │   └── types/          # TypeScript 类型定义
│   └── vite.config.ts
├── Dockerfile
├── docker-compose.yml
└── pom.xml
```

## 定胆预测

- 前端「智能预测」页含 **算法随机** / **定胆生成** 两个 Tab；胆码模式下每注 **必含** 所选前区/后区号码，其余由对应算法补全。
- 接口：`POST /api/prediction/generate-pinned`，JSON 体字段：`count`（1–50）、`method`（算法代码）、`targetIssue`（可选）、`lockedFront` / `lockedBack`（可选，整数数组）。
- 落库：`generation_mode=PINNED`，`locked_front_balls`、`locked_back_balls` 存当次胆码（逗号分隔）。

## 预测算法

| 算法 | 代码 | 说明 |
|------|------|------|
| 热号优先 | HOT | 基于近期出现频率较高的号码 |
| 遗漏回补 | MISSING | 根据历史遗漏值预测回补概率 |
| 冷热均衡 | BALANCED | 综合热号和冷号追求均衡 |
| 机器学习 | ML | 机器学习算法挖掘潜在规律 |
| 自适应 | ADAPTIVE | 自动选择最佳预测策略 |
| 贝叶斯 | BAYESIAN | 基于贝叶斯概率论 |
| 马尔可夫 | MARKOV | 马尔可夫链转移概率 |
| 蒙特卡洛 | MONTECARLO | 蒙特卡洛随机模拟 |
| 梯度提升 | GRADIENT_BOOST | 梯度提升决策树 |
| 集成预测 | ENSEMBLE | 多种方法加权投票融合 |
