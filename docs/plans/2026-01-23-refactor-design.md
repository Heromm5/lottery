# 大乐透分析系统重构设计方案

**日期**: 2026-01-23  
**目标**: 渐进式架构改进 + 持续学习机制

---

## 1. 重构目标

1. **服务层拆分** - 将 498 行的 AnalysisService 拆分为职责单一的小服务
2. **消除重复代码** - 通过 NumberZone 枚举统一处理前区/后区逻辑
3. **配置化** - 将魔法数字提取到配置文件
4. **持续学习** - 根据预测准确率动态调整方法权重
5. **关联分析** - 新增号码关联规则挖掘
6. **前端增强** - Thymeleaf + AJAX + ECharts 交互式图表

---

## 2. 整体架构

```
com.hobart.lottery
├── config/                    # 配置
│   ├── LotteryConfig.java     # 号码范围、期数等配置
│   └── MybatisPlusConfig.java # 数据库配置
│
├── domain/                    # 领域层（新增）
│   ├── model/                 # 领域模型
│   │   ├── NumberZone.java    # 号码区域（前区/后区）枚举
│   │   └── AssociationRule.java # 关联规则值对象
│   └── strategy/              # 策略
│       └── WeightStrategy.java # 权重策略接口
│
├── service/                   # 服务层（重构）
│   ├── analysis/              # 分析服务拆分
│   │   ├── FrequencyAnalyzer.java    # 频率分析
│   │   ├── MissingAnalyzer.java      # 遗漏分析
│   │   ├── TrendAnalyzer.java        # 走势分析
│   │   ├── StatisticsAnalyzer.java   # 统计分析
│   │   ├── AssociationAnalyzer.java  # 关联规则（新增）
│   │   └── AnalysisFacade.java       # 分析门面
│   │
│   ├── prediction/            # 预测服务
│   │   ├── PredictionService.java    # 原预测服务
│   │   └── AdaptivePredictor.java    # 自适应预测器（新增）
│   │
│   └── learning/              # 持续学习（新增）
│       ├── AccuracyTracker.java      # 准确率追踪
│       └── WeightAdjuster.java       # 权重调整器
│
├── entity/                    # 实体
│   ├── LotteryResult.java
│   ├── PredictionRecord.java
│   ├── PredictionAccuracy.java
│   └── MethodWeight.java      # 方法权重（新增）
│
├── predictor/                 # 预测器
│   ├── BasePredictor.java
│   ├── HotNumberPredictor.java
│   ├── MissingPredictor.java
│   ├── BalancedPredictor.java
│   ├── MLPredictor.java
│   └── CombinedPredictor.java
│
├── controller/                # 控制器
├── mapper/                    # 数据访问
└── dto/                       # 数据传输对象
```

---

## 3. 核心设计

### 3.1 NumberZone 枚举（消除重复代码）

```java
public enum NumberZone {
    FRONT(1, 35, 5, "前区"),
    BACK(1, 12, 2, "后区");
    
    private final int min, max, count;
    private final String name;
    
    public int[] getBalls(LotteryResult result) {
        return this == FRONT ? result.getFrontBallArray() : result.getBackBallArray();
    }
}
```

### 3.2 持续学习机制

**数据表：**
```sql
CREATE TABLE prediction_method_weight (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    method_code VARCHAR(32) NOT NULL,
    weight DECIMAL(5,4) DEFAULT 0.2000,
    total_predictions INT DEFAULT 0,
    total_hits INT DEFAULT 0,
    hit_rate DECIMAL(5,4) DEFAULT 0,
    updated_at TIMESTAMP,
    UNIQUE KEY uk_method (method_code)
);
```

**权重调整算法：EMA（指数移动平均）**
- 平滑因子 α = 0.1
- 新命中率 = α × 本次结果 + (1-α) × 历史命中率
- 权重 = 归一化后的命中率

### 3.3 关联规则分析

- **支持度阈值**: 0.02（至少 2% 期数同时出现）
- **置信度阈值**: 0.3（条件概率至少 30%）
- **分析维度**: 前区内关联、后区内关联、跨区关联

### 3.4 配置化

```yaml
lottery:
  front:
    min: 1
    max: 35
    count: 5
  back:
    min: 1
    max: 12
    count: 2
  analysis:
    hot-cold-period: 30
    missing-period: 500
    trend-default-limit: 30
    association-period: 200
    min-support: 0.02
    min-confidence: 0.3
```

---

## 4. 实施计划

| 阶段 | 内容 | 文件 |
|------|------|------|
| **Phase 1** | 基础设施 | NumberZone, LotteryConfig, MethodWeight 实体和表 |
| **Phase 2** | 服务拆分 | FrequencyAnalyzer, MissingAnalyzer, TrendAnalyzer, StatisticsAnalyzer, AnalysisFacade |
| **Phase 3** | 持续学习 | WeightAdjuster, AdaptivePredictor, 权重调整集成 |
| **Phase 4** | 关联分析 | AssociationAnalyzer, AssociationRule, 关联分析 API |
| **Phase 5** | 前端增强 | ECharts 图表集成 |

---

## 5. 预期效果

| 指标 | 重构前 | 重构后 |
|------|--------|--------|
| AnalysisService 代码量 | 498 行 | ~50 行（门面） |
| 重复代码 | 大量前/后区重复 | 通过 NumberZone 消除 |
| 可配置性 | 硬编码 | application.yml 配置 |
| 预测方式 | 静态权重 | 动态自适应权重 |
| 分析维度 | 频率/遗漏/走势 | + 关联规则 |
| 前端交互 | 服务端渲染 | + ECharts 图表 |
