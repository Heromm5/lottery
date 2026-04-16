package com.hobart.lottery.ai.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * AI分析报告DTO
 */
@Data
public class AiAnalysisReport {

    /**
     * 报告类型：daily|weekly|issue
     */
    private String reportType;

    /**
     * 报告摘要
     */
    private String summary;

    /**
     * 报告内容（JSON格式）
     */
    private Map<String, Object> content = new HashMap<>();

    /**
     * AI洞察（JSON格式）
     */
    private Map<String, Object> insights = new HashMap<>();

    /**
     * 生成时间
     */
    private LocalDateTime generatedAt;
}