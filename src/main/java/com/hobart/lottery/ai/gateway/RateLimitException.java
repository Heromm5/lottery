package com.hobart.lottery.ai.gateway;

/**
 * 限流异常
 * 当请求速率超过配置的限流阈值时抛出
 */
public class RateLimitException extends RuntimeException {

    public RateLimitException(String message) {
        super(message);
    }

    public RateLimitException(String message, Throwable cause) {
        super(message, cause);
    }
}