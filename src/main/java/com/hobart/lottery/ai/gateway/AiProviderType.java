package com.hobart.lottery.ai.gateway;

/**
 * AI 服务商类型枚举
 */
public enum AiProviderType {
    /** Anthropic 官方 API */
    ANTHROPIC,
    
    /** OpenAI 兼容 API（如 Kimi） */
    OPENAI_COMPATIBLE,
    
    /** OpenAI 官方 API */
    OPENAI
}