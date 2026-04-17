package com.hobart.lottery.ai.gateway;

/**
 * AI 服务商枚举
 * 支持 NVIDIA (GLM-5)、Claude、Kimi、GPT-4o 等 AI 服务商
 */
public enum AiProvider {
/** NVIDIA GLM-5 (主要使用) */
NVIDIA("nvidia", AiProviderType.OPENAI_COMPATIBLE),

/** Anthropic Claude */
CLAUDE("claude", AiProviderType.ANTHROPIC),

/** 月之暗面 Kimi */
KIMI("kimi", AiProviderType.OPENAI_COMPATIBLE),

/** OpenAI GPT-4o */
GPT4O("gpt4o", AiProviderType.OPENAI);

    private final String code;
    private final AiProviderType type;

    AiProvider(String code, AiProviderType type) {
        this.code = code;
        this.type = type;
    }

    /**
     * 获取服务商代码
     */
    public String getCode() {
        return code;
    }

    /**
     * 获取服务商类型
     */
    public AiProviderType getType() {
        return type;
    }
}