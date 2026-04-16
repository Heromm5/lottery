package com.hobart.lottery.ai.gateway;

/**
 * 熔断器打开异常
 * 当熔断器处于打开状态时抛出,拒绝请求通过
 */
public class CircuitOpenException extends RuntimeException {

    public CircuitOpenException(String message) {
        super(message);
    }

    public CircuitOpenException(String message, Throwable cause) {
        super(message, cause);
    }
}