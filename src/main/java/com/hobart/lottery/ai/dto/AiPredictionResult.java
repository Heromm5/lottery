package com.hobart.lottery.ai.dto;

import lombok.Data;

import java.util.List;

/**
 * AI 预测结果
 */
@Data
public class AiPredictionResult {

    /**
     * 前区号码（5个）
     */
    private List<String> frontBalls;

    /**
     * 后区号码（2个）
     */
    private List<String> backBalls;

    /**
     * 置信度 0-1
     */
    private Double confidence;

    /**
     * 使用的AI模型
     */
    private String aiModel;

    /**
     * AI推理过程
     */
    private String reasoning;
}
