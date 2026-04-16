package com.hobart.lottery.ai.gateway;

import com.hobart.lottery.ai.config.AiProperties;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * AI 网关主类
 * 统一管理限流、缓存、熔断逻辑,提供对 Claude/Kimi/GPT-4o 的稳定调用
 */
@Service
public class AIGateway {

    private final RateLimiter rateLimiter;
    private final ResponseCache responseCache;
    private final Map<AiProvider, CircuitBreaker> breakers;

    public AIGateway(AiProperties properties) {
        // 初始化限流器
        this.rateLimiter = new RateLimiter(
            properties.getRateLimit().getRequestsPerMinute(),
            Duration.ofMinutes(1)
        );

        // 初始化响应缓存
        this.responseCache = new ResponseCache(
            properties.getCache().getMaxSize(),
            Duration.ofMinutes(properties.getCache().getExpireAfterWriteMinutes())
        );

        // 初始化每个提供商的熔断器
        this.breakers = new HashMap<>();
        for (AiProvider provider : AiProvider.values()) {
            breakers.put(provider, new CircuitBreaker(5, Duration.ofMinutes(1)));
        }
    }

    /**
     * 调用 AI 服务
     * 
     * @param provider AI 服务商
     * @param cacheKey 缓存键
     * @param apiCall API 调用函数
     * @return 响应结果(如果存在)
     */
    public Optional<String> call(AiProvider provider, String cacheKey, Supplier<String> apiCall) {
        // 1. 检查限流器
        if (!rateLimiter.tryAcquire()) {
            throw new RateLimitException("Rate limit exceeded for AI gateway");
        }

        // 2. 检查熔断器
        CircuitBreaker breaker = breakers.get(provider);
        if (!breaker.isCallPermitted()) {
            throw new CircuitOpenException("Circuit breaker open for " + provider);
        }

        // 3. 检查缓存
        Optional<String> cached = responseCache.get(cacheKey);
        if (cached.isPresent()) {
            return cached;
        }

        // 4. 调用 API
        try {
            String result = apiCall.get();
            // 缓存结果
            responseCache.put(cacheKey, result);
            // 记录成功
            breaker.recordSuccess();
            return Optional.of(result);
        } catch (Exception e) {
            // 记录失败
            breaker.recordFailure();
            throw e;
        }
    }

    /**
     * 获取指定提供商的熔断器
     */
    public CircuitBreaker getBreaker(AiProvider provider) {
        return breakers.get(provider);
    }

    /**
     * 获取限流器实例
     */
    public RateLimiter getRateLimiter() {
        return rateLimiter;
    }

    /**
     * 获取缓存实例
     */
    public ResponseCache getResponseCache() {
        return responseCache;
    }
}