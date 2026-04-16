package com.hobart.lottery.ai.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * AI 配置属性类
 * 支持 Claude、Kimi、GPT-4o 三大 AI 服务商的配置
 */
@Configuration
@ConfigurationProperties(prefix = "ai")
@Data
public class AiProperties {

    /** Claude 配置 */
    private ClaudeConfig claude = new ClaudeConfig();

    /** Kimi 配置 */
    private KimiConfig kimi = new KimiConfig();

    /** GPT-4o 配置 */
    private Gpt4oConfig gpt4o = new Gpt4oConfig();

    /** 限流配置 */
    private RateLimitConfig rateLimit = new RateLimitConfig();

    /** 缓存配置 */
    private CacheConfig cache = new CacheConfig();

    /**
     * Claude API 配置
     */
    @Data
    public static class ClaudeConfig {
        /** API 密钥 */
        private String apiKey = "";

        /** API 基础 URL */
        private String baseUrl = "https://api.anthropic.com";

        /** 模型名称 */
        private String model = "claude-sonnet-4-6";

        /** 超时时间（毫秒） */
        private int timeout = 30000;
    }

    /**
     * Kimi API 配置
     */
    @Data
    public static class KimiConfig {
        /** API 密钥 */
        private String apiKey = "";

        /** API 基础 URL */
        private String baseUrl = "https://api.moonshot.ai/v1";

        /** 模型名称 */
        private String model = "kimi-k2.5";

        /** 超时时间（毫秒） */
        private int timeout = 30000;
    }

    /**
     * GPT-4o API 配置
     */
    @Data
    public static class Gpt4oConfig {
        /** API 密钥 */
        private String apiKey = "";

        /** API 基础 URL */
        private String baseUrl = "https://api.openai.com/v1";

        /** 模型名称 */
        private String model = "gpt-4o";

        /** 超时时间（毫秒） */
        private int timeout = 30000;
    }

    /**
     * 限流配置
     */
    @Data
    public static class RateLimitConfig {
        /** 每分钟最大请求数 */
        private int requestsPerMinute = 60;

        /** 突发容量 */
        private int burstSize = 10;
    }

    /**
     * 缓存配置
     */
    @Data
    public static class CacheConfig {
        /** 是否启用缓存 */
        private boolean enabled = true;

        /** 最大缓存条目数 */
        private int maxSize = 1000;

        /** 写入后过期时间（分钟） */
        private int expireAfterWriteMinutes = 30;
    }
}