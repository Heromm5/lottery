# AI 增强架构设计方案

**日期**: 2026-04-16
**项目**: lottery-java 大乐透数据分析与预测系统
**方案**: 方案1 - 云端 AI 增强架构

---

## 1. 整体架构

```
┌─────────────────────────────────────────────────────────────┐
│ 前端 (Vue 3)                                                │
│ 智能预测 │ AI分析助手 │ 自动化报告 │ 异常检测报警          │
└─────────────────────────┬───────────────────────────────────┘
                          │ HTTP REST
┌─────────────────────────▼───────────────────────────────────┐
│ 后端 (Spring Boot)                                          │
│ ┌─────────────┐ ┌─────────────┐ ┌─────────────────────┐     │
│ │ 现有Service │ │ AIService   │ │ AutomationService   │     │
│ │ (保持不变)  │ │ (新增)      │ │ (新增)              │     │
│ └─────────────┘ └──────┬──────┘ └──────────┬──────────┘     │
│                        │                   │                 │
│ ┌──────────────────────▼───────────────────▼─────────────┐  │
│ │ AI Gateway (统一入口/限流/缓存)                         │  │
│ └───────────────────────┬─────────────────────────────────┘  │
└──────────────────────────┼───────────────────────────────────┘
                           │ API调用
┌──────────────────────────▼───────────────────────────────────┐
│ 第三方 AI API                                                │
│ Claude API │ Kimi API │ GPT-4o │ Gemini                      │
└─────────────────────────────────────────────────────────────┘
```

### 1.1 核心组件说明

| 组件 | 职责 | 技术选型 |
|------|------|----------|
| AI Service | AI 预测增强、置信度评分、模式分析 | Spring Bean + AI SDK |
| Automation Service | 自动化规律挖掘、异常检测、报告生成 | Spring @Scheduled + AI SDK |
| AI Gateway | 统一路由、限流、缓存、熔断、多模型切换 | 限流器(Guava/Sentinel) + Caffeine |
| 第三方 AI API | Claude/Kimi/GPT-4o/Gemini | REST API |

---

## 2. 核心模块设计

### 2.1 AI Service（预测增强）

| 模块 | 功能 | API调用方式 |
|------|------|------------|
| `DeepLearningPredictor` | 发送历史数据到 AI，分析号码规律，生成预测 | Claude/GPT-4o |
| `ConfidenceScorer` | AI 评估每个预测号码的置信度 | 流式返回 |
| `PatternAnalyzer` | AI 识别历史数据中的周期规律 | Kimi（长上下文） |

**新增类**:
- `src/main/java/com/hobart/lottery/ai/service/AIService.java` - AI 服务主类
- `src/main/java/com/hobart/lottery/ai/service/DeepLearningPredictor.java` - AI 预测实现
- `src/main/java/com/hobart/lottery/ai/service/ConfidenceScorer.java` - 置信度评分
- `src/main/java/com/hobart/lottery/ai/service/PatternAnalyzer.java` - 模式分析

### 2.2 Automation Service（自动化分析）

| 模块 | 功能 | API调用方式 |
|------|------|------------|
| `RuleDiscovery` | AI 自动发现号码相关性、异常模式 | Claude |
| `AnomalyDetector` | 监控数据流，检测偏离正常的模式 | GPT-4o |
| `ReportGenerator` | AI 自动生成数据分析报告 | Kimi |

**新增类**:
- `src/main/java/com/hobart/lottery/ai/automation/RuleDiscovery.java` - 规律发现
- `src/main/java/com/hobart/lottery/ai/automation/AnomalyDetector.java` - 异常检测
- `src/main/java/com/hobart/lottery/ai/automation/ReportGenerator.java` - 报告生成

### 2.3 AI Gateway（统一管理）

**功能**:
- **限流**: 防止 API 调用超出配额（滑动窗口算法）
- **缓存**: 相同查询返回缓存结果（Caffeine）
- **熔断**: API 失败时降级到现有算法
- **多模型**: 支持 Claude/Kimi/GPT-4o 切换

**新增类**:
- `src/main/java/com/hobart/lottery/ai/gateway/AIGateway.java` - AI 网关主类
- `src/main/java/com/hobart/lottery/ai/gateway/RateLimiter.java` - 限流器
- `src/main/java/com/hobart/lottery/ai/gateway/ResponseCache.java` - 响应缓存
- `src/main/java/com/hobart/lottery/ai/gateway/CircuitBreaker.java` - 熔断器

### 2.4 配置管理

**新增配置文件**:
- `src/main/resources/ai-config.properties` - AI 服务配置

```properties
# AI Provider Configuration
ai.claude.api-key=${CLAUDE_API_KEY}
ai.claude.base-url=https://api.anthropic.com
ai.claude.model=claude-sonnet-4-6
ai.claude.timeout=30000

ai.kimi.api-key=${KIMI_API_KEY}
ai.kimi.base-url=https://api.moonshot.ai/v1
ai.kimi.model=kimi-k2.5

ai.gpt4o.api-key=${OPENAI_API_KEY}
ai.gpt4o.base-url=https://api.openai.com/v1
ai.gpt4o.model=gpt-4o

# Rate Limiting
ai.rate-limit.requests-per-minute=60
ai.rate-limit.burst-size=10

# Cache Configuration
ai.cache.enabled=true
ai.cache.max-size=1000
ai.cache.expire-after-write-minutes=30
```

---

## 3. API 设计

### 3.1 新增 AI 相关接口

```java
// AI 增强预测
POST /api/ai/predict
- 输入: { method: "DEEP_AI", count: 5, historyPeriods: 100 }
- 输出: { predictions: [{numbers, confidence, aiModel, reasoning}], cost, cached}

// AI 模式分析
POST /api/ai/analyze-patterns
- 输入: { analysisType: "frequency|missing|trend|association", periods: 100 }
- 输出: { patterns: [], anomalies: [], insights: [] }

// AI 报告生成
POST /api/ai/report
- 输入: { reportType: "daily|weekly|issue", issueRange: {start, end} }
- 输出: { summary, chartsData, recommendations, generatedAt }

// 异常报警查询
GET /api/ai/anomaly-alerts
- 输出: [{ type, severity, description, detectedAt, acknowledged }]

// AI 助手对话 (可选)
POST /api/ai/chat
- 输入: { message, context }
- 输出: { reply, suggestions }
```

### 3.2 Controller 新增

```java
// 新增控制器
src/main/java/com/hobart/lottery/controller/api/AiApiController.java
```

---

## 4. 数据流设计

### 4.1 AI 预测流程

```
用户请求
    ↓
PredictionController
    ↓
AI Service.validateRequest()
    ↓
AI Gateway.route() [限流/缓存]
    ↓
AI API.call() [Claude/Kimi]
    ↓
ResponseCache.cache()
    ↓
返回结果 + 存储 PredictionRecord (generation_mode=DEEP_AI)
```

### 4.2 自动化分析流程

```
定时任务 (Cron) / 用户触发
    ↓
AutomationService.discover()
    ↓
AI Gateway.route()
    ↓
AI API.analyzePatterns()
    ↓
AnomalyDetector.detect()
    ↓
存储 AnalysisReport / AnomalyAlert
    ↓
通知用户 (可选: 消息推送/邮件)
```

### 4.3 报告生成流程

```
用户请求
    ↓
ReportGenerator.generate()
    ↓
历史数据分析 (从现有 AnalysisService 获取)
    ↓
AI API 生成报告内容
    ↓
Markdown/JSON 报告
    ↓
存储 + 返回
```

---

## 5. 数据库扩展

### 5.1 新增表

```sql
-- AI 分析报告表
CREATE TABLE ai_analysis_report (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    report_type VARCHAR(20) NOT NULL COMMENT 'daily|weekly|issue',
    summary TEXT COMMENT '报告摘要',
    content JSON COMMENT '报告完整内容',
    insights JSON COMMENT 'AI 洞察',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_report_type (report_type),
    INDEX idx_created_at (created_at)
);

-- 异常报警表
CREATE TABLE anomaly_alert (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    alert_type VARCHAR(50) NOT NULL COMMENT 'anomaly type',
    severity VARCHAR(10) NOT NULL COMMENT 'LOW|MEDIUM|HIGH|CRITICAL',
    description TEXT,
    detected_data JSON COMMENT '检测到的异常数据',
    acknowledged BOOLEAN DEFAULT FALSE,
    acknowledged_at TIMESTAMP NULL,
    detected_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_alert_type (alert_type),
    INDEX idx_severity (severity),
    INDEX idx_detected_at (detected_at)
);

-- 号码规律表
CREATE TABLE number_pattern (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    pattern_type VARCHAR(30) NOT NULL COMMENT 'frequency|missing|trend|association',
    pattern_desc TEXT COMMENT '规律描述',
    confidence DECIMAL(5,4) COMMENT '置信度 0-1',
    evidence JSON COMMENT '支持证据',
    discovered_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_pattern_type (pattern_type),
    INDEX idx_confidence (confidence)
);
```

### 5.2 PredictionRecord 扩展

```sql
ALTER TABLE prediction_records
ADD COLUMN ai_model VARCHAR(50) COMMENT '使用的AI模型',
ADD COLUMN ai_confidence DECIMAL(5,4) COMMENT 'AI置信度',
ADD COLUMN ai_reasoning TEXT COMMENT 'AI推理过程';
```

---

## 6. 错误处理与降级策略

| 场景 | 处理策略 |
|------|----------|
| AI API 超时 | 降级到现有统计方法 + 标记来源 |
| AI API 配额超限 | 排队 + 限流 + 前端提示"服务繁忙" |
| AI API 返回格式错误 | 重试3次 + 降级 + 记录日志 |
| AI 分析结果异常 | 置信度过滤 + 人工复核标记 |
| 网络故障 | 本地缓存返回 + 标记"可能过期" |

### 6.1 降级实现

```java
// AI Gateway 降级逻辑
public PredictionResult predictWithFallback(PredictionRequest request) {
    try {
        return aiGateway.call(request);
    } catch (RateLimitException e) {
        log.warn("AI rate limit hit, falling back to traditional method");
        return traditionalPredictor.predict(request);
    } catch (ApiTimeoutException e) {
        log.warn("AI API timeout, falling back");
        return traditionalPredictor.predict(request);
    } catch (Exception e) {
        log.error("AI prediction failed", e);
        return traditionalPredictor.predict(request);
    }
}
```

---

## 7. 前端扩展

### 7.1 新增页面/组件

| 页面/组件 | 路径 | 功能 |
|-----------|------|------|
| AI预测Tab | `views/prediction/ai-tab.vue` | AI增强预测交互 |
| AI分析助手 | `views/ai/assistant.vue` | 对话式AI分析 |
| 异常监控面板 | `views/ai/anomaly-monitor.vue` | 异常报警展示 |
| AI报告查看 | `views/ai/report-viewer.vue` | 报告查看/下载 |

### 7.2 新增 API 模块

```typescript
// frontend/src/api/modules/ai.ts
export const aiApi = {
  predict: (data) => service.post('/api/ai/predict', data),
  analyzePatterns: (data) => service.post('/api/ai/analyze-patterns', data),
  generateReport: (data) => service.post('/api/ai/report', data),
  getAnomalyAlerts: () => service.get('/api/ai/anomaly-alerts'),
  chat: (data) => service.post('/api/ai/chat', data),
}
```

---

## 8. 测试策略

### 8.1 测试类型

| 测试类型 | 覆盖内容 | 测试框架 |
|----------|----------|----------|
| 单元测试 | AI Gateway 限流/缓存/路由逻辑 | JUnit 5 + Mockito |
| 集成测试 | AI Service 与 Mock API 对接 | Spring Boot Test |
| 降级测试 | 模拟 API 失败，验证降级逻辑 | JUnit 5 |
| 对比测试 | AI 预测 vs 现有算法准确率对比 | 自定义测试类 |

### 8.2 测试文件

```
src/test/java/com/hobart/lottery/ai/
├── gateway/
│   ├── AIGatewayTest.java
│   ├── RateLimiterTest.java
│   └── CircuitBreakerTest.java
├── service/
│   ├── AIServiceTest.java
│   └── PatternAnalyzerTest.java
└── automation/
    ├── AnomalyDetectorTest.java
    └── ReportGeneratorTest.java
```

---

## 9. 实施阶段

### 阶段一：基础架构 (1-2周)
1. 创建 AI 模块基础包结构
2. 实现 AI Gateway（限流、缓存、熔断）
3. 配置第三方 API 集成
4. 基础单元测试

### 阶段二：AI 预测服务 (1-2周)
1. 实现 DeepLearningPredictor
2. 实现 ConfidenceScorer
3. 新增 /api/ai/predict 接口
4. 前端 AI 预测 Tab

### 阶段三：自动化分析 (1-2周)
1. 实现 RuleDiscovery
2. 实现 AnomalyDetector
3. 实现 ReportGenerator
4. 定时任务配置
5. 前端异常监控面板

### 阶段四：AI 助手 (可选，0.5周)
1. 实现 Chat 接口
2. 前端 AI 助手页面
3. 完整集成测试

---

## 10. 风险与缓解

| 风险 | 影响 | 缓解措施 |
|------|------|----------|
| API 成本超预期 | 运营成本增加 | 严格限流 + 缓存 + 按需调用 |
| API 响应不稳定 | 用户体验下降 | 降级策略 + 熔断器 |
| 数据隐私 | 合规风险 | 本地处理敏感数据，API 只传必要信息 |
| 模型幻觉 | 错误指导 | 置信度过滤 + 人工复核标记 |

---

**设计文档版本**: v1.0
**最后更新**: 2026-04-16
**状态**: 已批准