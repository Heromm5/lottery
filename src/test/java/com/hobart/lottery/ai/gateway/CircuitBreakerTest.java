package com.hobart.lottery.ai.gateway;

import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 熔断器测试类
 */
class CircuitBreakerTest {

    @Test
    void shouldAllowRequestsWhenClosed() {
        // 测试：关闭状态下应该允许所有请求
        CircuitBreaker breaker = new CircuitBreaker(3, Duration.ofMinutes(1));
        assertTrue(breaker.isCallPermitted(), "关闭状态下应该允许请求");
        assertTrue(breaker.isCallPermitted(), "关闭状态下应该允许请求");
        assertTrue(breaker.isCallPermitted(), "关闭状态下应该允许请求");
    }

    @Test
    void shouldOpenAfterFailureThreshold() {
        // 测试：达到失败阈值后应该打开熔断器
        CircuitBreaker breaker = new CircuitBreaker(2, Duration.ofMinutes(1));
        breaker.recordFailure();
        breaker.recordFailure();
        assertFalse(breaker.isCallPermitted(), "达到失败阈值后应该拒绝请求");
    }

    @Test
    void shouldRemainOpenUntilTimeout() throws InterruptedException {
        // 测试：打开后应该保持打开状态直到超时
        CircuitBreaker breaker = new CircuitBreaker(1, Duration.ofMillis(100));
        breaker.recordFailure();
        assertFalse(breaker.isCallPermitted(), "打开后应该拒绝请求");

        // 等待超时前应该仍然拒绝
        Thread.sleep(50);
        assertFalse(breaker.isCallPermitted(), "超时前应该仍然拒绝请求");
    }

    @Test
    void shouldTransitionToHalfOpenAfterTimeout() throws InterruptedException {
        // 测试：超时后应该转换到半开状态
        CircuitBreaker breaker = new CircuitBreaker(1, Duration.ofMillis(100));
        breaker.recordFailure();
        assertFalse(breaker.isCallPermitted(), "打开后应该拒绝请求");

        // 等待超时后应该转换到半开状态，允许请求
        Thread.sleep(150);
        assertTrue(breaker.isCallPermitted(), "超时后应该允许请求（半开状态）");
    }

    @Test
    void shouldCloseAfterSuccessInHalfOpen() throws InterruptedException {
        // 测试：半开状态下成功后应该关闭熔断器
        CircuitBreaker breaker = new CircuitBreaker(1, Duration.ofMillis(100));
        breaker.recordFailure();

        // 等待超时转换到半开状态
        Thread.sleep(150);
        assertTrue(breaker.isCallPermitted(), "半开状态应该允许请求");

        // 记录成功应该关闭熔断器
        breaker.recordSuccess();
        assertTrue(breaker.isCallPermitted(), "成功后应该关闭并允许请求");
    }

    @Test
    void shouldOpenAgainAfterFailureInHalfOpen() throws InterruptedException {
        // 测试：半开状态下失败应该重新打开熔断器
        CircuitBreaker breaker = new CircuitBreaker(1, Duration.ofMillis(100));
        breaker.recordFailure();

        // 等待超时转换到半开状态
        Thread.sleep(150);
        assertTrue(breaker.isCallPermitted(), "半开状态应该允许请求");

        // 半开状态下失败应该重新打开熔断器
        breaker.recordFailure();
        assertFalse(breaker.isCallPermitted(), "半开状态下失败应该重新打开");
    }

    @Test
    void shouldResetFailureCountAfterSuccess() {
        // 测试：成功后应该重置失败计数
        CircuitBreaker breaker = new CircuitBreaker(3, Duration.ofMinutes(1));

        // 记录2次���败
        breaker.recordFailure();
        breaker.recordFailure();
        assertTrue(breaker.isCallPermitted(), "未达到阈值应该允许请求");

        // 再记录1次成功，应该重置失败计数
        breaker.recordSuccess();

        // 再来2次失败不应该打开
        breaker.recordFailure();
        breaker.recordFailure();
        assertTrue(breaker.isCallPermitted(), "成功后重置失败计数，3次失败才应该打开");
    }
}