package com.hobart.lottery.ai.dto;

import lombok.Data;

/**
 * AI 置信度评分
 */
@Data
public class AiConfidenceScore {

    /**
     * 号码
     */
    private String ballNumber;

    /**
     * 区域：FRONT（前区）或 BACK（后区）
     */
    private String zone;

    /**
     * 置信度 0-1
     */
    private Double confidence;

    /**
     * 置信度理由
     */
    private String reason;
}
