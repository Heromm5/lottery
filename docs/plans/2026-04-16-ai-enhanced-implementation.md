# AI 增强功能实施计划

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** 在现有大乐透预测系统上增加云端 AI 增强功能，包括 AI 预测、自动化分析、异常检测和报告生成。

**Architecture:** 方案1 - 云端 AI 增强架构。新增 AI Service Layer 调用第三方 API（Claude/Kimi/GPT-4o），通过 AI Gateway 统一管理限流/缓存/熔断，现有统计方法作为降级方案。

**Tech Stack:** Spring Boot 2.7.18, Java 17, Spring AI (或 WebClient), Caffeine Cache, Sentinel/Guava RateLimiter, Vue 3

---

## 阶段一：基础架构搭建

### Task 1: 创建 AI 模块包结构

**Files:**
- Create: `src/main/java/com/hobart/lottery/ai/AiApplication.java`

**Step 1: 创建包结构**
创建 `com.hobart.lottery.ai` 包及其子包：
- `gateway/` - AI 网关（限流/缓存/熔断）
- `service/` - AI 服务（预测/置信度/模式分析）
- `automation/` - 自动化服务（规律发现/异常检测/报告生成）
- `config/` - AI 配置类
- `dto/` - AI 相关 DTO

**Step 2: 提交**
```bash
git add src/main/java/com/hobart/lottery/ai/
git commit -m "feat(ai): 创建AI模块基础包结构"
```

---

### Task 2: 实现 AI 配置管理

**Files:**
- Create: `src/main/java/com/hobart/lottery/ai/config/AiProperties.java`
- Create: `src/main/resources/ai-config.properties`
- Modify: `src/main/resources/application.yml` (添加 ai 前缀配置引用)

**Step 1: 创建配置属性类**
```java
@ConfigurationProperties(prefix = "ai")
public class AiProperties {
    private ClaudeConfig claude = new ClaudeConfig();
    private KimiConfig kimi = new KimiConfig();
    private Gpt4oConfig gpt4o = new Gpt4oConfig();
    private RateLimitConfig rateLimit = new RateLimitConfig();
    private CacheConfig cache = new CacheConfig();

    // getters and setters
}
```

**Step 2: 创建配置文件**
```properties
# AI Provider Configuration
ai.claude.api-key=${CLAUDE_API_KEY:}
ai.claude.base-url=https://api.anthropic.com
ai.claude.model=claude-sonnet-4-6
ai.claude.timeout=30000

ai.kimi.api-key=${KIMI_API_KEY:}
ai.kimi.base-url=https://api.moonshot.ai/v1
ai.kimi.model=kimi-k2.5

ai.gpt4o.api-key=${OPENAI_API_KEY:}
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

**Step 3: 验证配置加载**
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=local -Dai.claude.api-key=test
# 确认无配置加载错误
```

**Step 4: 提交**
```bash
git add src/main/java/com/hobart/lottery/ai/config/
git add src/main/resources/ai-config.properties
git add src/main/resources/application.yml
git commit -m "feat(ai): 添加AI配置管理"
```

---

### Task 3: 实现 AI Gateway - 限流器

**Files:**
- Create: `src/main/java/com/hobart/lottery/ai/gateway/RateLimiter.java`

**Step 1: 编写限流器单元测试**
```java
@Test
void shouldAllowRequestsWithinLimit() {
    RateLimiter limiter = new RateLimiter(10, Duration.ofMinutes(1));
    for (int i = 0; i < 10; i++) {
        assertTrue(limiter.tryAcquire());
    }
}

@Test
void shouldBlockRequestsOverLimit() {
    RateLimiter limiter = new RateLimiter(2, Duration.ofMinutes(1));
    limiter.tryAcquire();
    limiter.tryAcquire();
    assertFalse(limiter.tryAcquire());
}

@Test
void shouldResetAfterWindow() throws InterruptedException {
    RateLimiter limiter = new RateLimiter(1, Duration.ofMillis(100));
    limiter.tryAcquire();
    assertFalse(limiter.tryAcquire());
    Thread.sleep(150);
    assertTrue(limiter.tryAcquire());
}
```

**Step 2: 运行测试**
```bash
mvn test -Dtest=RateLimiterTest -q
# 预期: FAIL - class not found
```

**Step 3: 实现限流器**
```java
public class RateLimiter {
    private final int maxRequests;
    private final Duration window;
    private final Deque<Instant> requests = new ConcurrentLinkedDeque<>();

    public RateLimiter(int maxRequests, Duration window) {
        this.maxRequests = maxRequests;
        this.window = window;
    }

    public synchronized boolean tryAcquire() {
        Instant now = Instant.now();
        Instant cutoff = now.minus(window);
        while (!requests.isEmpty() && requests.peekFirst().isBefore(cutoff)) {
            requests.pollFirst();
        }
        if (requests.size() < maxRequests) {
            requests.addLast(now);
            return true;
        }
        return false;
    }
}
```

**Step 4: 运行测试**
```bash
mvn test -Dtest=RateLimiterTest -q
# 预期: PASS
```

**Step 5: 提交**
```bash
git add src/main/java/com/hobart/lottery/ai/gateway/RateLimiter.java
git add src/test/java/com/hobart/lottery/ai/gateway/RateLimiterTest.java
git commit -m "feat(ai): 实现限流器"
```

---

### Task 4: 实现 AI Gateway - 响应缓存

**Files:**
- Create: `src/main/java/com/hobart/lottery/ai/gateway/ResponseCache.java`
- Create: `src/test/java/com/hobart/lottery/ai/gateway/ResponseCacheTest.java`

**Step 1: 编写缓存测试**
```java
@Test
void shouldReturnCachedResponse() {
    ResponseCache cache = new ResponseCache(100, Duration.ofMinutes(10));
    String key = "predict:100:30";
    String expected = "cached_result";

    cache.put(key, expected);
    Optional<String> result = cache.get(key);

    assertTrue(result.isPresent());
    assertEquals(expected, result.get());
}

@Test
void shouldReturnEmptyForMiss() {
    ResponseCache cache = new ResponseCache(100, Duration.ofMinutes(10));
    Optional<String> result = cache.get("nonexistent");
    assertFalse(result.isPresent());
}

@Test
void shouldEvictOldEntries() {
    ResponseCache cache = new ResponseCache(2, Duration.ofMinutes(10));
    cache.put("key1", "value1");
    cache.put("key2", "value2");
    cache.put("key3", "value3"); // 应该驱逐 key1

    assertFalse(cache.get("key1").isPresent());
    assertTrue(cache.get("key2").isPresent());
    assertTrue(cache.get("key3").isPresent());
}
```

**Step 2: 运行测试**
```bash
mvn test -Dtest=ResponseCacheTest -q
# 预期: FAIL - class not found
```

**Step 3: 实现缓存**
```java
public class ResponseCache {
    private final int maxSize;
    private final Duration expireAfterWrite;
    private final ConcurrentLinkedHashMap<String, CacheEntry> cache;

    public ResponseCache(int maxSize, Duration expireAfterWrite) {
        this.maxSize = maxSize;
        this.expireAfterWrite = expireAfterWrite;
        this.cache = new ConcurrentLinkedHashMap<>(maxSize);
    }

    public void put(String key, String value) {
        if (cache.size() >= maxSize) {
            evictExpired();
            if (cache.size() >= maxSize) {
                cache.pollFirstEntry();
            }
        }
        cache.put(key, new CacheEntry(value, Instant.now().plus(expireAfterWrite)));
    }

    public Optional<String> get(String key) {
        CacheEntry entry = cache.get(key);
        if (entry == null) return Optional.empty();
        if (entry.expiresAt().isBefore(Instant.now())) {
            cache.remove(key);
            return Optional.empty();
        }
        return Optional.of(entry.value());
    }

    private void evictExpired() {
        Instant now = Instant.now();
        cache.entrySet().removeIf(e -> e.getValue().expiresAt().isBefore(now));
    }

    private record CacheEntry(String value, Instant expiresAt) {}
}
```

**Step 4: 运行测试**
```bash
mvn test -Dtest=ResponseCacheTest -q
# 预期: PASS
```

**Step 5: 提交**
```bash
git add src/main/java/com/hobart/lottery/ai/gateway/ResponseCache.java
git add src/test/java/com/hobart/lottery/ai/gateway/ResponseCacheTest.java
git commit -m "feat(ai): 实现响应缓存"
```

---

### Task 5: 实现 AI Gateway - 熔断器

**Files:**
- Create: `src/main/java/com/hobart/lottery/ai/gateway/CircuitBreaker.java`
- Create: `src/test/java/com/hobart/lottery/ai/gateway/CircuitBreakerTest.java`

**Step 1: 编写熔断器测试**
```java
@Test
void shouldAllowRequestsWhenClosed() {
    CircuitBreaker breaker = new CircuitBreaker(3, Duration.ofMinutes(1));
    assertTrue(breaker.isCallPermitted());
    assertTrue(breaker.isCallPermitted());
    assertTrue(breaker.isCallPermitted());
}

@Test
void shouldOpenAfterFailureThreshold() {
    CircuitBreaker breaker = new CircuitBreaker(2, Duration.ofMinutes(1));
    breaker.recordFailure();
    breaker.recordFailure();
    assertFalse(breaker.isCallPermitted());
}

@Test
void shouldTransitionToHalfOpenAfterTimeout() throws InterruptedException {
    CircuitBreaker breaker = new CircuitBreaker(1, Duration.ofMillis(100));
    breaker.recordFailure();
    assertFalse(breaker.isCallPermitted());

    Thread.sleep(150);
    assertTrue(breaker.isCallPermitted()); // Half-open
}

@Test
void shouldCloseAfterSuccessInHalfOpen() {
    CircuitBreaker breaker = new CircuitBreaker(1, Duration.ofMillis(100));
    breaker.recordFailure();
    breaker.recordFailure(); // Open

    // Simulate timeout transition to half-open
    breaker.transitionToHalfOpen();

    assertTrue(breaker.isCallPermitted());
    breaker.recordSuccess();
    // Now should be closed again
}
```

**Step 2: 运行测试**
```bash
mvn test -Dtest=CircuitBreakerTest -q
# 预期: FAIL - class not found
```

**Step 3: 实现熔断器**
```java
public class CircuitBreaker {
    private final int failureThreshold;
    private final Duration timeout;
    private State state = State.CLOSED;
    private int failureCount = 0;
    private Instant lastFailureTime;

    public CircuitBreaker(int failureThreshold, Duration timeout) {
        this.failureThreshold = failureThreshold;
        this.timeout = timeout;
    }

    public boolean isCallPermitted() {
        return switch (state) {
            case CLOSED -> true;
            case OPEN -> {
                if (lastFailureTime.plus(timeout).isBefore(Instant.now())) {
                    state = State.HALF_OPEN;
                    yield true;
                }
                yield false;
            }
            case HALF_OPEN -> true;
        };
    }

    public void recordFailure() {
        lastFailureTime = Instant.now();
        failureCount++;
        if (failureCount >= failureThreshold) {
            state = State.OPEN;
        }
    }

    public void recordSuccess() {
        if (state == State.HALF_OPEN) {
            state = State.CLOSED;
            failureCount = 0;
        }
    }

    public void transitionToHalfOpen() {
        state = State.HALF_OPEN;
    }

    private enum State { CLOSED, OPEN, HALF_OPEN }
}
```

**Step 4: 运行测试**
```bash
mvn test -Dtest=CircuitBreakerTest -q
# 预期: PASS
```

**Step 5: 提交**
```bash
git add src/main/java/com/hobart/lottery/ai/gateway/CircuitBreaker.java
git add src/test/java/com/hobart/lottery/ai/gateway/CircuitBreakerTest.java
git commit -m "feat(ai): 实现熔断器"
```

---

### Task 6: 实现 AI Gateway 主类

**Files:**
- Create: `src/main/java/com/hobart/lottery/ai/gateway/AIGateway.java`
- Create: `src/main/java/com/hobart/lottery/ai/gateway/AiProvider.java` (枚举)
- Create: `src/test/java/com/hobart/lottery/ai/gateway/AIGatewayTest.java`

**Step 1: 创建 AI Provider 枚举**
```java
public enum AiProvider {
    CLAUDE("claude", AiProviderType.ANTHROPIC),
    KIMI("kimi", AiProviderType.OPENAI_COMPATIBLE),
    GPT4O("gpt4o", AiProviderType.OPENAI);

    private final String code;
    private final AiProviderType type;

    AiProvider(String code, AiProviderType type) {
        this.code = code;
        this.type = type;
    }

    public String getCode() { return code; }
    public AiProviderType getType() { return type; }
}
```

**Step 2: 创建 AI Gateway 主类**
```java
@Service
public class AIGateway {
    private final RateLimiter rateLimiter;
    private final ResponseCache responseCache;
    private final Map<AiProvider, CircuitBreaker> breakers;
    private final Map<AiProvider, WebClient> clients;

    public AIGateway(AiProperties properties) {
        this.rateLimiter = new RateLimiter(
            properties.getRateLimit().getRequestsPerMinute(),
            Duration.ofMinutes(1)
        );
        this.responseCache = new ResponseCache(
            properties.getCache().getMaxSize(),
            Duration.ofMinutes(properties.getCache().getExpireAfterWriteMinutes())
        );
        this.breakers = Map.of(
            AiProvider.CLAUDE, new CircuitBreaker(5, Duration.ofMinutes(1)),
            AiProvider.KIMI, new CircuitBreaker(5, Duration.ofMinutes(1)),
            AiProvider.GPT4O, new CircuitBreaker(5, Duration.ofMinutes(1))
        );
        this.clients = Map.of(
            AiProvider.CLAUDE, createClaudeClient(properties.getClaude()),
            AiProvider.KIMI, createKimiClient(properties.getKimi()),
            AiProvider.GPT4O, createGpt4oClient(properties.getGpt4o())
        );
    }

    public Optional<String> call(AiProvider provider, String cacheKey, Supplier<String> apiCall) {
        if (!rateLimiter.tryAcquire()) {
            throw new RateLimitException("Rate limit exceeded");
        }

        if (!breakers.get(provider).isCallPermitted()) {
            throw new CircuitOpenException("Circuit breaker open for " + provider);
        }

        return responseCache.get(cacheKey)
            .or(() -> {
                try {
                    String result = apiCall.get();
                    responseCache.put(cacheKey, result);
                    breakers.get(provider).recordSuccess();
                    return Optional.of(result);
                } catch (Exception e) {
                    breakers.get(provider).recordFailure();
                    throw e;
                }
            });
    }

    private WebClient createClaudeClient(AiProperties.ClaudeConfig config) {
        return WebClient.builder()
            .baseUrl(config.getBaseUrl())
            .defaultHeader("x-api-key", config.getApiKey())
            .defaultHeader("anthropic-version", "2023-06-01")
            .build();
    }
    // ... 其他客户端创建
}
```

**Step 3: 编写网关测试**
```java
@Test
void shouldReturnCachedResult() {
    AIGateway gateway = new AIGateway(testProperties());
    String cacheKey = "test:key";
    String cachedValue = "cached";

    responseCache.put(cacheKey, cachedValue);
    String result = gateway.call(AiProvider.CLAUDE, cacheKey, () -> "fresh").get();

    assertEquals(cachedValue, result);
}
```

**Step 4: 验证编译**
```bash
mvn compile -q
# 预期: 无编译错误
```

**Step 5: 提交**
```bash
git add src/main/java/com/hobart/lottery/ai/gateway/
git add src/test/java/com/hobart/lottery/ai/gateway/
git commit -m "feat(ai): 实现AI网关主类"
```

---

## 阶段二：AI 预测服务

### Task 7: 创建 AI 预测 DTO

**Files:**
- Create: `src/main/java/com/hobart/lottery/ai/dto/AiPredictionRequest.java`
- Create: `src/main/java/com/hobart/lottery/ai/dto/AiPredictionResult.java`
- Create: `src/main/java/com/hobart/lottery/ai/dto/AiConfidenceScore.java`

**Step 1: 创建请求 DTO**
```java
public class AiPredictionRequest {
    private Integer count; // 1-50
    private String method; // DEEP_AI
    private Integer historyPeriods; // 历史期数
    private String targetIssue; // 可选，目标期号

    // getters and setters
}
```

**Step 2: 创建结果 DTO**
```java
public class AiPredictionResult {
    private List<String> frontBalls; // 前区号码
    private List<String> backBalls; // 后区号码
    private Double confidence; // 置信度 0-1
    private String aiModel; // 使用的AI模型
    private String reasoning; // AI推理过程

    // getters and setters
}
```

**Step 3: 提交**
```bash
git add src/main/java/com/hobart/lottery/ai/dto/
git commit -m "feat(ai): 添加AI预测DTO"
```

---

### Task 8: 实现 Deep Learning Predictor

**Files:**
- Create: `src/main/java/com/hobart/lottery/ai/service/DeepLearningPredictor.java`
- Create: `src/test/java/com/hobart/lottery/ai/service/DeepLearningPredictorTest.java`

**Step 1: 编写测试**
```java
@Test
void shouldGeneratePredictions() {
    DeepLearningPredictor predictor = new DeepLearningPredictor(gateway, analysisFacade);
    AiPredictionRequest request = new AiPredictionRequest();
    request.setCount(5);
    request.setHistoryPeriods(100);

    List<AiPredictionResult> results = predictor.predict(request);

    assertNotNull(results);
    assertEquals(5, results.size());
}

@Test
void shouldRespectCountLimit() {
    DeepLearningPredictor predictor = new DeepLearningPredictor(gateway, analysisFacade);
    AiPredictionRequest request = new AiPredictionRequest();
    request.setCount(100); // 超过限制

    assertThrows(IllegalArgumentException.class, () -> predictor.predict(request));
}
```

**Step 2: 运行测试**
```bash
mvn test -Dtest=DeepLearningPredictorTest -q
# 预期: FAIL - class not found
```

**Step 3: 实现预测器**
```java
@Service
public class DeepLearningPredictor {
    private static final int MAX_COUNT = 50;
    private final AIGateway gateway;
    private final AnalysisFacade analysisFacade;

    public DeepLearningPredictor(AIGateway gateway, AnalysisFacade analysisFacade) {
        this.gateway = gateway;
        this.analysisFacade = analysisFacade;
    }

    public List<AiPredictionResult> predict(AiPredictionRequest request) {
        validateRequest(request);

        String cacheKey = buildCacheKey(request);
        String aiResponse = gateway.call(AiProvider.CLAUDE, cacheKey,
            () -> callAiApi(request)).orElseThrow();

        return parseAiResponse(aiResponse);
    }

    private void validateRequest(AiPredictionRequest request) {
        if (request.getCount() == null || request.getCount() < 1 || request.getCount() > MAX_COUNT) {
            throw new IllegalArgumentException("count must be between 1 and " + MAX_COUNT);
        }
    }

    private String callAiApi(AiPredictionRequest request) {
        String prompt = buildPrompt(request);
        // 调用 Claude API 的实现
        return webClient.post()
            .uri("/v1/messages")
            .bodyValue(Map.of(
                "model", "claude-sonnet-4-6",
                "max_tokens", 1024,
                "messages", List.of(Map.of("role", "user", "content", prompt))
            ))
            .retrieve()
            .bodyToString();
    }

    private String buildPrompt(AiPredictionRequest request) {
        // 构建发送给AI的提示词
        String historyData = analysisFacade.getRecentHistory(request.getHistoryPeriods());
        return String.format("""
            基于以下大乐透历史数据，预测下一期号码：
            %s

            请生成%d注预测号码，每注包含5个前区号码(1-35)和2个后区号码(1-12)。
            返回格式为JSON数组，每注格式：{"front":[...],"back":[...]}
            """, historyData, request.getCount());
    }

    private List<AiPredictionResult> parseAiResponse(String response) {
        // 解析AI返回的JSON响应
        // 转换为 AiPredictionResult 列表
    }
}
```

**Step 4: 运行测试**
```bash
mvn test -Dtest=DeepLearningPredictorTest -q
# 预期: PASS (with mocks)
```

**Step 5: 提交**
```bash
git add src/main/java/com/hobart/lottery/ai/service/DeepLearningPredictor.java
git add src/test/java/com/hobart/lottery/ai/service/DeepLearningPredictorTest.java
git commit -m "feat(ai): 实现DeepLearningPredictor"
```

---

### Task 9: 创建 AI Controller

**Files:**
- Create: `src/main/java/com/hobart/lottery/controller/api/AiApiController.java`

**Step 1: 创建 Controller**
```java
@RestController
@RequestMapping("/api/ai")
public class AiApiController {
    private final DeepLearningPredictor deepLearningPredictor;

    public AiApiController(DeepLearningPredictor deepLearningPredictor) {
        this.deepLearningPredictor = deepLearningPredictor;
    }

    @PostMapping("/predict")
    public Result<List<AiPredictionResult>> predict(@RequestBody AiPredictionRequest request) {
        try {
            List<AiPredictionResult> results = deepLearningPredictor.predict(request);
            return Result.success(results);
        } catch (RateLimitException e) {
            return Result.error("服务繁忙，请稍后再试");
        } catch (CircuitOpenException e) {
            return Result.error("AI服务暂时不可用，已降级到传统预测");
        }
    }
}
```

**Step 2: 验证编译**
```bash
mvn compile -q
# 预期: 无编译错误
```

**Step 3: 提交**
```bash
git add src/main/java/com/hobart/lottery/controller/api/AiApiController.java
git commit -m "feat(ai): 添加AI预测Controller"
```

---

## 阶段三：自动化分析服务

### Task 10: 实现规则发现服务

**Files:**
- Create: `src/main/java/com/hobart/lottery/ai/automation/RuleDiscovery.java`
- Create: `src/main/java/com/hobart/lottery/ai/dto/DiscoveredPattern.java`
- Create: `src/test/java/com/hobart/lottery/ai/automation/RuleDiscoveryTest.java`

**Step 1: 创建 Pattern DTO**
```java
public class DiscoveredPattern {
    private String patternType; // frequency|missing|trend|association
    private String description;
    private Double confidence; // 0-1
    private Map<String, Object> evidence;
    private LocalDateTime discoveredAt;

    // getters and setters
}
```

**Step 2: 实现 RuleDiscovery**
```java
@Service
public class RuleDiscovery {
    private final AIGateway gateway;
    private final AnalysisFacade analysisFacade;

    public RuleDiscovery(AIGateway gateway, AnalysisFacade analysisFacade) {
        this.gateway = gateway;
        this.analysisFacade = analysisFacade;
    }

    public List<DiscoveredPattern> discoverPatterns(int periods) {
        String analysisData = analysisFacade.getComprehensiveAnalysis(periods);
        String cacheKey = "patterns:" + periods;

        String aiResponse = gateway.call(AiProvider.CLAUDE, cacheKey,
            () -> callAiForPatterns(analysisData)).orElseThrow();

        return parsePatterns(aiResponse);
    }

    private String callAiForPatterns(String analysisData) {
        String prompt = String.format("""
            分析以下大乐透数据，找出其中的规律和模式：

            %s

            请识别并返回：
            1. 号码频率规律（如某些号码出现频率异常高/低）
            2. 遗漏回补规律（如某些号码长期未出现后回补）
            3. 号码关联规律（如某些号码倾向于一起出现）
            4. 周期性规律（如某些模式定期重复）

            返回JSON格式：[{"type":"...","description":"...","confidence":0.95,"evidence":{}}]
            """, analysisData);

        // 调用 AI API
    }
}
```

**Step 3: 提交**
```bash
git add src/main/java/com/hobart/lottery/ai/automation/RuleDiscovery.java
git add src/main/java/com/hobart/lottery/ai/dto/DiscoveredPattern.java
git add src/test/java/com/hobart/lottery/ai/automation/RuleDiscoveryTest.java
git commit -m "feat(ai): 实现规则发现服务"
```

---

### Task 11: 实现异常检测服务

**Files:**
- Create: `src/main/java/com/hobart/lottery/ai/automation/AnomalyDetector.java`
- Create: `src/main/java/com/hobart/lottery/ai/dto/AnomalyAlert.java`
- Create: `src/main/java/com/hobart/lottery/entity/AnomalyAlertEntity.java`
- Create: `src/main/java/com/hobart/lottery/mapper/AnomalyAlertMapper.java`
- Create: `src/test/java/com/hobart/lottery/ai/automation/AnomalyDetectorTest.java`

**Step 1: 创建实体和 Mapper**
```java
@Entity
@Table(name = "anomaly_alert")
public class AnomalyAlertEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String alertType;
    private String severity; // LOW|MEDIUM|HIGH|CRITICAL
    private String description;
    private String detectedData; // JSON
    private Boolean acknowledged;
    private LocalDateTime acknowledgedAt;
    private LocalDateTime detectedAt;

    // getters and setters
}
```

```java
@Mapper
public interface AnomalyAlertMapper extends BaseMapper<AnomalyAlertEntity> {
}
```

**Step 2: 实现 AnomalyDetector**
```java
@Service
public class AnomalyDetector {
    private final AIGateway gateway;
    private final AnalysisFacade analysisFacade;
    private final AnomalyAlertMapper alertMapper;

    public AnomalyDetector(AIGateway gateway, AnalysisFacade analysisFacade,
                          AnomalyAlertMapper alertMapper) {
        this.gateway = gateway;
        this.analysisFacade = analysisFacade;
        this.alertMapper = alertMapper;
    }

    public List<AnomalyAlert> detectAnomalies(int periods) {
        String currentData = analysisFacade.getStatistics(periods);
        String cacheKey = "anomaly:" + periods;

        String aiResponse = gateway.call(AiProvider.GPT4O, cacheKey,
            () -> callAiForAnomalies(currentData)).orElseThrow();

        List<AnomalyAlert> alerts = parseAlerts(aiResponse);
        alerts.forEach(this::saveAlert);
        return alerts;
    }

    private void saveAlert(AnomalyAlert alert) {
        AnomalyAlertEntity entity = new AnomalyAlertEntity();
        entity.setAlertType(alert.getType());
        entity.setSeverity(alert.getSeverity());
        entity.setDescription(alert.getDescription());
        entity.setDetectedData(JSON.toJSONString(alert.getData()));
        entity.setAcknowledged(false);
        entity.setDetectedAt(LocalDateTime.now());
        alertMapper.insert(entity);
    }
}
```

**Step 3: 提交**
```bash
git add src/main/java/com/hobart/lottery/ai/automation/AnomalyDetector.java
git add src/main/java/com/hobart/lottery/ai/dto/AnomalyAlert.java
git add src/main/java/com/hobart/lottery/entity/AnomalyAlertEntity.java
git add src/main/java/com/hobart/lottery/mapper/AnomalyAlertMapper.java
git add sql/
git commit -m "feat(ai): 实现异常检测服务和实体"
```

---

### Task 12: 实现报告生成服务

**Files:**
- Create: `src/main/java/com/hobart/lottery/ai/automation/ReportGenerator.java`
- Create: `src/main/java/com/hobart/lottery/ai/dto/AiAnalysisReport.java`
- Create: `src/main/java/com/hobart/lottery/entity/AiReportEntity.java`
- Create: `src/main/java/com/hobart/lottery/mapper/AiReportMapper.java`
- Create: `src/test/java/com/hobart/lottery/ai/automation/ReportGeneratorTest.java`

**Step 1: 创建实体和 Mapper**
```java
@Entity
@Table(name = "ai_analysis_report")
public class AiReportEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String reportType; // daily|weekly|issue
    private String summary;
    private String content; // JSON
    private String insights; // JSON
    private LocalDateTime createdAt;
    // getters and setters
}
```

```java
@Mapper
public interface AiReportMapper extends BaseMapper<AiReportEntity> {
}
```

**Step 2: 实现 ReportGenerator**
```java
@Service
public class ReportGenerator {
    private final AIGateway gateway;
    private final AnalysisFacade analysisFacade;
    private final AiReportMapper reportMapper;

    public ReportGenerator(AIGateway gateway, AnalysisFacade analysisFacade,
                          AiReportMapper reportMapper) {
        this.gateway = gateway;
        this.analysisFacade = analysisFacade;
        this.reportMapper = reportMapper;
    }

    public AiAnalysisReport generateReport(String reportType, Integer startIssue, Integer endIssue) {
        String analysisData = analysisFacade.getDataForReport(startIssue, endIssue);
        String cacheKey = "report:" + reportType + ":" + startIssue + ":" + endIssue;

        String aiResponse = gateway.call(AiProvider.KIMI, cacheKey,
            () -> callAiForReport(reportType, analysisData)).orElseThrow();

        return parseAndSaveReport(reportType, aiResponse);
    }

    private AiAnalysisReport parseAndSaveReport(String reportType, String aiResponse) {
        AiAnalysisReport report = parseReport(aiResponse);

        AiReportEntity entity = new AiReportEntity();
        entity.setReportType(reportType);
        entity.setSummary(report.getSummary());
        entity.setContent(JSON.toJSONString(report.getContent()));
        entity.setInsights(JSON.toJSONString(report.getInsights()));
        entity.setCreatedAt(LocalDateTime.now());
        reportMapper.insert(entity);

        return report;
    }
}
```

**Step 3: 提交**
```bash
git add src/main/java/com/hobart/lottery/ai/automation/ReportGenerator.java
git add src/main/java/com/hobart/lottery/ai/dto/AiAnalysisReport.java
git add src/main/java/com/hobart/lottery/entity/AiReportEntity.java
git add src/main/java/com/hobart/lottery/mapper/AiReportMapper.java
git add sql/
git commit -m "feat(ai): 实现报告生成服务和实体"
```

---

### Task 13: 添加定时任务调度

**Files:**
- Create: `src/main/java/com/hobart/lottery/ai/config/AiSchedulingConfig.java`
- Modify: `src/main/java/com/hobart/lottery/ai/automation/AnomalyDetector.java` (添加定时方法)

**Step 1: 创建调度配置**
```java
@Configuration
@EnableScheduling
public class AiSchedulingConfig {
}
```

**Step 2: 在 AnomalyDetector 中添加定时方法**
```java
@Scheduled(cron = "0 0 8,20 * * ?") // 每天早8点和晚8点
public void scheduledAnomalyDetection() {
    try {
        List<AnomalyAlert> alerts = detectAnomalies(30);
        if (!alerts.isEmpty()) {
            notifyUser(alerts);
        }
    } catch (Exception e) {
        log.error("Scheduled anomaly detection failed", e);
    }
}

private void notifyUser(List<AnomalyAlert> alerts) {
    // 发送通知（邮件/消息等）
}
```

**Step 3: 提交**
```bash
git add src/main/java/com/hobart/lottery/ai/config/AiSchedulingConfig.java
git add src/main/java/com/hobart/lottery/ai/automation/AnomalyDetector.java
git commit -m "feat(ai): 添加定时异常检测"
```

---

## 阶段四：API 接口扩展

### Task 14: 扩展 AI Controller 接口

**Files:**
- Modify: `src/main/java/com/hobart/lottery/controller/api/AiApiController.java`

**Step 1: 添加分析接口**
```java
@PostMapping("/analyze-patterns")
public Result<List<DiscoveredPattern>> analyzePatterns(@RequestBody AnalyzePatternsRequest request) {
    try {
        List<DiscoveredPattern> patterns = ruleDiscovery.discoverPatterns(
            request.getPeriods() != null ? request.getPeriods() : 100
        );
        return Result.success(patterns);
    } catch (Exception e) {
        log.error("Pattern analysis failed", e);
        return Result.error("模式分析失败: " + e.getMessage());
    }
}

@PostMapping("/report")
public Result<AiAnalysisReport> generateReport(@RequestBody GenerateReportRequest request) {
    try {
        AiAnalysisReport report = reportGenerator.generateReport(
            request.getReportType(),
            request.getStartIssue(),
            request.getEndIssue()
        );
        return Result.success(report);
    } catch (Exception e) {
        log.error("Report generation failed", e);
        return Result.error("报告生成失败: " + e.getMessage());
    }
}

@GetMapping("/anomaly-alerts")
public Result<List<AnomalyAlert>> getAnomalyAlerts(
    @RequestParam(required = false) String severity,
    @RequestParam(required = false) Boolean acknowledged
) {
    List<AnomalyAlert> alerts = anomalyDetector.getAlerts(severity, acknowledged);
    return Result.success(alerts);
}
```

**Step 2: 验证编译**
```bash
mvn compile -q
# 预期: 无编译错误
```

**Step 3: 提交**
```bash
git add src/main/java/com/hobart/lottery/controller/api/AiApiController.java
git commit -m "feat(ai): 扩展AI Controller接口"
```

---

### Task 15: 添加数据库迁移脚本

**Files:**
- Create: `sql/migration_ai_tables.sql`

**Step 1: 创建迁移脚本**
```sql
-- AI 分析报告表
CREATE TABLE IF NOT EXISTS ai_analysis_report (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    report_type VARCHAR(20) NOT NULL COMMENT 'daily|weekly|issue',
    summary TEXT COMMENT '报告摘要',
    content JSON COMMENT '报告完整内容',
    insights JSON COMMENT 'AI洞察',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_report_type (report_type),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 异常报警表
CREATE TABLE IF NOT EXISTS anomaly_alert (
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 号码规律表
CREATE TABLE IF NOT EXISTS number_pattern (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    pattern_type VARCHAR(30) NOT NULL COMMENT 'frequency|missing|trend|association',
    pattern_desc TEXT COMMENT '规律描述',
    confidence DECIMAL(5,4) COMMENT '置信度0-1',
    evidence JSON COMMENT '支持证据',
    discovered_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_pattern_type (pattern_type),
    INDEX idx_confidence (confidence)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- PredictionRecord 扩展
ALTER TABLE prediction_records
ADD COLUMN IF NOT EXISTS ai_model VARCHAR(50) COMMENT '使用的AI模型',
ADD COLUMN IF NOT EXISTS ai_confidence DECIMAL(5,4) COMMENT 'AI置信度',
ADD COLUMN IF NOT EXISTS ai_reasoning TEXT COMMENT 'AI推理过程';
```

**Step 2: 提交**
```bash
git add sql/migration_ai_tables.sql
git commit -m "feat(ai): 添加AI功能数据库迁移脚本"
```

---

## 阶段五：前端集成

### Task 16: 创建前端 AI API 模块

**Files:**
- Create: `frontend/src/api/modules/ai.ts`

**Step 1: 创建 AI API 模块**
```typescript
import service from '../axios'

export const aiApi = {
  predict: (data: { count: number; historyPeriods?: number; method?: string }) =>
    service.post('/api/ai/predict', data),

  analyzePatterns: (data: { periods?: number; analysisType?: string }) =>
    service.post('/api/ai/analyze-patterns', data),

  generateReport: (data: { reportType: string; startIssue?: number; endIssue?: number }) =>
    service.post('/api/ai/report', data),

  getAnomalyAlerts: (params?: { severity?: string; acknowledged?: boolean }) =>
    service.get('/api/ai/anomaly-alerts', { params }),
}
```

**Step 2: 提交**
```bash
git add frontend/src/api/modules/ai.ts
git commit -m "feat(ai): 添加前端AI API模块"
```

---

### Task 17: 创建 AI 预测 Tab 组件

**Files:**
- Create: `frontend/src/views/prediction/AiPredictTab.vue`

**Step 1: 创建组件**
```vue
<template>
  <div class="ai-predict-tab">
    <el-card class="mb-4">
      <template #header>
        <span>AI 智能预测</span>
      </template>
      <el-form :model="form" label-width="120px">
        <el-form-item label="预测注数">
          <el-input-number v-model="form.count" :min="1" :max="50" />
        </el-form-item>
        <el-form-item label="历史期数">
          <el-select v-model="form.historyPeriods" placeholder="选择分析期数">
            <el-option label="最近30期" :value="30" />
            <el-option label="最近50期" :value="50" />
            <el-option label="最近100期" :value="100" />
            <el-option label="最近200期" :value="200" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" @click="handlePredict">
            开始预测
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card v-if="results.length > 0">
      <template #header>
        <span>预测结果</span>
      </template>
      <div class="results">
        <div v-for="(result, index) in results" :key="index" class="result-item">
          <BallDisplay :front-balls="result.frontBalls" :back-balls="result.backBalls" />
          <div class="confidence">
            <span>置信度: {{ (result.confidence * 100).toFixed(1) }}%</span>
            <el-tag size="small">{{ result.aiModel }}</el-tag>
          </div>
          <el-collapse>
            <el-collapse-item title="AI推理过程">
              <pre>{{ result.reasoning }}</pre>
            </el-collapse-item>
          </el-collapse>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { aiApi } from '@/api/modules/ai'
import BallDisplay from '@/components/common/BallDisplay.vue'
import { ElMessage } from 'element-plus'

const loading = ref(false)
const results = ref([])

const form = reactive({
  count: 5,
  historyPeriods: 100,
})

async function handlePredict() {
  loading.value = true
  try {
    const response = await aiApi.predict(form)
    results.value = response.data.data
    ElMessage.success('预测完成')
  } catch (error) {
    ElMessage.error('预测失败: ' + error.message)
  } finally {
    loading.value = false
  }
}
</script>
```

**Step 2: 修改 Prediction 页面集成 Tab**
Modify: `frontend/src/views/prediction/index.vue`
```vue
<template>
  <!-- 添加 AI 预测 Tab -->
  <el-tabs v-model="activeTab">
    <el-tab-pane label="算法随机" name="algorithm">
      <!-- 原有内容 -->
    </el-tab-pane>
    <el-tab-pane label="定胆生成" name="pinned">
      <!-- 原有内容 -->
    </el-tab-pane>
    <el-tab-pane label="AI预测" name="ai">
      <AiPredictTab />
    </el-tab-pane>
  </el-tabs>
</template>

<script setup lang="ts">
import AiPredictTab from './AiPredictTab.vue'
const activeTab = ref('algorithm')
</script>
```

**Step 3: 提交**
```bash
git add frontend/src/views/prediction/AiPredictTab.vue
git add frontend/src/views/prediction/index.vue
git add frontend/src/api/modules/ai.ts
git commit -m "feat(ai): 添加前端AI预测Tab组件"
```

---

### Task 18: 创建异常监控面板

**Files:**
- Create: `frontend/src/views/ai/AnomalyMonitor.vue`

**Step 1: 创建组件**
```vue
<template>
  <div class="anomaly-monitor">
    <el-card>
      <template #header>
        <div class="header">
          <span>异常监控</span>
          <el-button @click="loadAlerts">刷新</el-button>
        </div>
      </template>

      <el-table :data="alerts" stripe>
        <el-table-column prop="type" label="类型" width="120" />
        <el-table-column prop="severity" label="严重程度" width="100">
          <template #default="{ row }">
            <el-tag :type="getSeverityType(row.severity)">{{ row.severity }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="描述" />
        <el-table-column prop="detectedAt" label="检测时间" width="180">
          <template #default="{ row }">
            {{ formatDate(row.detectedAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100">
          <template #default="{ row }">
            <el-button
              v-if="!row.acknowledged"
              type="primary"
              size="small"
              @click="acknowledge(row.id)"
            >
              确认
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { aiApi } from '@/api/modules/ai'

const alerts = ref([])

async function loadAlerts() {
  const response = await aiApi.getAnomalyAlerts()
  alerts.value = response.data.data
}

async function acknowledge(id: number) {
  // 调用确认接口
  await loadAlerts()
}

function getSeverityType(severity: string) {
  return severity === 'CRITICAL' ? 'danger' : severity === 'HIGH' ? 'warning' : 'info'
}

function formatDate(date: string) {
  return new Date(date).toLocaleString('zh-CN')
}

onMounted(loadAlerts)
</script>
```

**Step 2: 添加路由**
Modify: `frontend/src/router/index.ts`
```typescript
{
  path: '/ai/anomaly-monitor',
  component: () => import('@/views/ai/AnomalyMonitor.vue'),
}
```

**Step 3: 提交**
```bash
git add frontend/src/views/ai/AnomalyMonitor.vue
git add frontend/src/router/index.ts
git commit -m "feat(ai): 添加异常监控面板"
```

---

## 验收标准

1. **AI Gateway** - 限流、缓存、熔断功能正常
2. **AI 预测接口** - `/api/ai/predict` 返回预测结果和置信度
3. **模式分析接口** - `/api/ai/analyze-patterns` 返回发现的规律
4. **异常检测** - 定时任务正常执行，报警数据入库
5. **报告生成** - `/api/ai/report` 生成分析报告
6. **前端** - AI 预测 Tab 和异常监控面板正常工作
7. **所有单元测试通过**

---

**Plan created**: 2026-04-16
**Estimated tasks**: 18 tasks
**Estimated time**: 2-3 weeks (phase by phase)