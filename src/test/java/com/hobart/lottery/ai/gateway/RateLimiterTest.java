package com.hobart.lottery.ai.gateway;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 限流器测试类
 */
class RateLimiterTest {

    @Test
    void shouldAllowRequestsWithinLimit() {
        // 测试：10次请求在限制10次以内应该全部允许
        RateLimiter limiter = new RateLimiter(10, Duration.ofMinutes(1));
        for (int i = 0; i < 10; i++) {
            assertTrue(limiter.tryAcquire(), "第 " + (i + 1) + " 次请求应该被允许");
        }
    }

    @Test
    void shouldBlockRequestsOverLimit() {
        // 测试：限制2次，第3次请求应该被拒绝
        RateLimiter limiter = new RateLimiter(2, Duration.ofMinutes(1));
        assertTrue(limiter.tryAcquire(), "第1次请求应该被允许");
        assertTrue(limiter.tryAcquire(), "第2次请求应该被允许");
        assertFalse(limiter.tryAcquire(), "第3次请求应该被拒绝");
    }

    @Test
    void shouldResetAfterWindow() throws InterruptedException {
        // 测试：100ms窗口限制1次，等待150ms后应该重置
        RateLimiter limiter = new RateLimiter(1, Duration.ofMillis(100));
        assertTrue(limiter.tryAcquire(), "第1次请求应该被允许");
        assertFalse(limiter.tryAcquire(), "第2次请求应该被拒绝");
        
        // 等待窗口过期
        Thread.sleep(150);
        assertTrue(limiter.tryAcquire(), "等待窗口过期后请求应该被允许");
    }

    @Test
    void shouldBeThreadSafe() throws InterruptedException {
        // 测试：并发访问的线程安全性
        RateLimiter limiter = new RateLimiter(100, Duration.ofSeconds(1));
        int threadCount = 10;
        int requestsPerThread = 20;
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(threadCount);
        AtomicInteger allowedCount = new AtomicInteger(0);
        
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    startLatch.await();
                    for (int j = 0; j < requestsPerThread; j++) {
                        if (limiter.tryAcquire()) {
                            allowedCount.incrementAndGet();
                        }
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    endLatch.countDown();
                }
            });
        }
        
        // 启动所有线程
        startLatch.countDown();
        endLatch.await();
        executor.shutdown();
        
        // 验证允许的请求数不超过限制
        assertTrue(allowedCount.get() <= 100, "允许的请求数不应超过限制: " + allowedCount.get());
    }
}
