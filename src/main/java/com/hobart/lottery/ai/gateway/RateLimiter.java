package com.hobart.lottery.ai.gateway;

import java.time.Duration;
import java.time.Instant;
import java.util.Deque;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * 滑动窗口限流器
 * 使用 ConcurrentLinkedDeque 实现线程安全的滑动窗口算法
 */
public class RateLimiter {
    private final int maxRequests;
    private final Duration window;
    private final Deque<Instant> requests = new ConcurrentLinkedDeque<>();

    public RateLimiter(int maxRequests, Duration window) {
        this.maxRequests = maxRequests;
        this.window = window;
    }

    /**
     * 尝试获取一个请求许可
     * 
     * @return true 表示允许请求，false 表示被限流拒绝
     */
    public synchronized boolean tryAcquire() {
        Instant now = Instant.now();
        Instant cutoff = now.minus(window);
        
        // 移除过期的请求记录
        while (!requests.isEmpty() && requests.peekFirst().isBefore(cutoff)) {
            requests.pollFirst();
        }
        
        // 检查是否在限制内
        if (requests.size() < maxRequests) {
            requests.addLast(now);
            return true;
        }
        return false;
    }

    /**
     * 获取当前窗口内的请求数
     */
    public synchronized int getCurrentRequests() {
        Instant now = Instant.now();
        Instant cutoff = now.minus(window);
        
        // 移除过期的请求记录
        while (!requests.isEmpty() && requests.peekFirst().isBefore(cutoff)) {
            requests.pollFirst();
        }
        
        return requests.size();
    }
}
