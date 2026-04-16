package com.hobart.lottery.ai.dto;

import lombok.Data;

/**
 * AI 预测请求体
 */
@Data
public class AiPredictionRequest {

    /**
     * 预测注数，1-50
     */
    private Integer count;

    /**
     * 预测方法，如 DEEP_AI
     */
    private String method;

    /**
     * 历史期数
     */
    private Integer historyPeriods;

    /**
     * 目标期号（可选）
     */
    private String targetIssue;
}
