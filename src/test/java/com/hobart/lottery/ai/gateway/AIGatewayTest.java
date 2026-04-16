package com.hobart.lottery.ai.gateway;

import com.hobart.lottery.ai.config.AiProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * AI 网关测试类
 */
class AIGatewayTest {

    private AiProperties properties;
    private AIGateway gateway;

    @BeforeEach
    void setUp() {
        properties = createTestProperties();
        gateway = new AIGateway(properties);
    }

    @Test
    void shouldReturnCachedResult() {
        // 测试：缓存命中时应返回缓存结果
        String cacheKey = "test:cache:key";
        String cachedValue = "cached_response";

        // 直接向缓存中放入数据
        gateway.getResponseCache().put(cacheKey, cachedValue);

        // 调用网关，传入一个会返回不同值的 apiCall
        Optional<String> result = gateway.call(AiProvider.CLAUDE, cacheKey, () -> "fresh_response");

        assertTrue(result.isPresent());
        assertEquals(cachedValue, result.get(), "应返回缓存的结果而不是fresh_response");
    }

    @Test
    void shouldCallApiOnCacheMiss() {
        // 测试：缓存未命中时应调用 API 并缓存结果
        String cacheKey = "test:cache:miss";
        String expectedApiResult = "api_response";

        // 不预先放入缓存，调用网关
        Optional<String> result = gateway.call(AiProvider.CLAUDE, cacheKey, () -> expectedApiResult);

        assertTrue(result.isPresent());
        assertEquals(expectedApiResult, result.get(), "应返回 API 调用结果");

        // 验证结果已被缓存
        Optional<String> cached = gateway.getResponseCache().get(cacheKey);
        assertTrue(cached.isPresent());
        assertEquals(expectedApiResult, cached.get());
    }

    @Test
    void shouldThrowRateLimitExceptionWhenLimited() {
        // 测试：限流器超出限制时应抛出异常
        // 先耗尽限流配额
        for (int i = 0; i < properties.getRateLimit().getRequestsPerMinute(); i++) {
            gateway.getRateLimiter().tryAcquire();
        }

        // 再次调用应该抛出 RateLimitException
        assertThrows(RateLimitException.class, () -> {
            gateway.call(AiProvider.CLAUDE, "test:key", () -> "response");
        });
    }

    @Test
    void shouldThrowCircuitOpenExceptionWhenBreakerOpen() {
        // 测试：熔断器打开时应抛出异常
        String cacheKey = "test:circuit:key";

        // 手动打开熔断器
        CircuitBreaker breaker = gateway.getBreaker(AiProvider.CLAUDE);
        // 触发熔断：连续失败5次
        for (int i = 0; i < 5; i++) {
            breaker.recordFailure();
        }

        // 熔断器已打开，调用应抛出 CircuitOpenException
        assertThrows(CircuitOpenException.class, () -> {
            gateway.call(AiProvider.CLAUDE, cacheKey, () -> "response");
        });
    }

    @Test
    void shouldRecordSuccessOnApiCall() {
        // 测试：API 调用成功应记录成功
        String cacheKey = "test:success:key";

        // 调用 API（缓存未命中）
        gateway.call(AiProvider.KIMI, cacheKey, () -> "success_response");

        // 验证熔断器记录了成功
        CircuitBreaker breaker = gateway.getBreaker(AiProvider.KIMI);
        assertEquals(0, breaker.getFailureCount(), "失败计数应为0");
    }

    @Test
    void shouldRecordFailureOnApiException() {
        // 测试：API 调用失败应记录失败
        String cacheKey = "test:failure:key";

        // 调用 API 并抛出异常
        assertThrows(RuntimeException.class, () -> {
            gateway.call(AiProvider.GPT4O, cacheKey, () -> {
                throw new RuntimeException("API error");
            });
        });

        // 验证熔断器记录了失败
        CircuitBreaker breaker = gateway.getBreaker(AiProvider.GPT4O);
        assertTrue(breaker.getFailureCount() > 0, "失败计数应大于0");
    }

    private AiProperties createTestProperties() {
        AiProperties props = new AiProperties();

        // Claude 配置
        AiProperties.ClaudeConfig claude = new AiProperties.ClaudeConfig();
        claude.setApiKey("test-key");
        claude.setBaseUrl("https://api.anthropic.com");
        claude.setModel("claude-sonnet-4-6");
        claude.setTimeout(30000);
        props.setClaude(claude);

        // Kimi 配置
        AiProperties.KimiConfig kimi = new AiProperties.KimiConfig();
        kimi.setApiKey("test-key");
        kimi.setBaseUrl("https://api.moonshot.ai/v1");
        kimi.setModel("kimi-k2.5");
        kimi.setTimeout(30000);
        props.setKimi(kimi);

        // GPT-4o 配置
        AiProperties.Gpt4oConfig gpt4o = new AiProperties.Gpt4oConfig();
        gpt4o.setApiKey("test-key");
        gpt4o.setBaseUrl("https://api.openai.com/v1");
        gpt4o.setModel("gpt-4o");
        gpt4o.setTimeout(30000);
        props.setGpt4o(gpt4o);

        // 限流配置
        AiProperties.RateLimitConfig rateLimit = new AiProperties.RateLimitConfig();
        rateLimit.setRequestsPerMinute(60);
        rateLimit.setBurstSize(10);
        props.setRateLimit(rateLimit);

        // 缓存配置
        AiProperties.CacheConfig cache = new AiProperties.CacheConfig();
        cache.setEnabled(true);
        cache.setMaxSize(1000);
        cache.setExpireAfterWriteMinutes(30);
        props.setCache(cache);

        return props;
    }
}