package com.hobart.lottery.ai.gateway;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 熔断器实现
 * 三种状态: CLOSED（关闭）, OPEN（打开）, HALF_OPEN（半开）
 * 
 * 状态转换:
 * - CLOSED -> OPEN: 失败次数达到阈值
 * - OPEN -> HALF_OPEN: 超时后
 * - HALF_OPEN -> CLOSED: 半开状态下成功后
 * - HALF_OPEN -> OPEN: 半开状态下失败后
 */
public class CircuitBreaker {

    /**
     * 熔断器状态枚举
     */
    public enum State {
        /** 关闭状态，允许请求通过 */
        CLOSED,
        /** 打开状态，拒绝请求通过 */
        OPEN,
        /** 半开状态，允许有限的请求通过探测 */
        HALF_OPEN
    }

    private final int failureThreshold;
    private final Duration timeout;
    private final AtomicInteger failureCount = new AtomicInteger(0);

    /** 当前状态 */
    private volatile State state = State.CLOSED;

    /** 上次状态转换的时间戳 */
    private volatile Instant lastStateChangeTime;

    public CircuitBreaker(int failureThreshold, Duration timeout) {
        if (failureThreshold <= 0) {
            throw new IllegalArgumentException("failureThreshold must be positive");
        }
        this.failureThreshold = failureThreshold;
        this.timeout = timeout;
    }

    /**
     * 检查是否允许请求通过
     * 
     * @return true 表示允许请求,false表示拒绝
     */
    public synchronized boolean isCallPermitted() {
        Instant now = Instant.now();

        // 如果是 OPEN 状态,检查是否超时应该转换到 HALF_OPEN
        if (state == State.OPEN) {
            if (lastStateChangeTime != null && 
                ChronoUnit.MILLIS.between(lastStateChangeTime, now) >= timeout.toMillis()) {
                // 超时后转换到半开状态
                state = State.HALF_OPEN;
                lastStateChangeTime = now;
            } else {
                return false;
            }
        }

        // CLOSED 和 HALF_OPEN 状态都允许请求通过
        return state == State.CLOSED || state == State.HALF_OPEN;
    }

    /**
     * 记录一次失败
     * 在 CLOSED 状态下,失败次数达到阈值时打开熔断器
     * 在 HALF_OPEN 状态下,一次失败就重新打开熔断器
     */
    public synchronized void recordFailure() {
        Instant now = Instant.now();

        if (state == State.CLOSED) {
            int failures = failureCount.incrementAndGet();
            if (failures >= failureThreshold) {
                // 达到阈值,打开熔断器
                state = State.OPEN;
                lastStateChangeTime = now;
            }
        } else if (state == State.HALF_OPEN) {
            // 半开状态下,一次失败就重新打开
            state = State.OPEN;
            lastStateChangeTime = now;
        }
    }

    /**
     * 记录一次成功
     * 仅在 HALF_OPEN 状态下有效,成功后关闭熔断器
     * 在 CLOSED 状态下也会重置失败计数
     */
    public synchronized void recordSuccess() {
        if (state == State.HALF_OPEN) {
            // 半开状态下成功后,关闭熔断器
            state = State.CLOSED;
            lastStateChangeTime = Instant.now();
        }

        // 重置失败计数
        failureCount.set(0);
    }

    /**
     * 获取当前状态
     */
    public State getState() {
        return state;
    }

    /**
     * 获取当前失败计数
     */
    public int getFailureCount() {
        return failureCount.get();
    }

    /**
     * 手动转换到半开状态（用于测试）
     */
    public void transitionToHalfOpen() {
        this.state = State.HALF_OPEN;
        this.lastStateChangeTime = Instant.now();
    }
}