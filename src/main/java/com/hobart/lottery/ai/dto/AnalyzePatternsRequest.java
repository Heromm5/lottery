package com.hobart.lottery.ai.dto;

import lombok.Data;

/**
 * 模式分析请求体
 */
@Data
public class AnalyzePatternsRequest {

    /**
     * 分析历史期数，默认100
     */
    private Integer periods;

    /**
     * 分析类型：frequency|missing|trend|association|all
     */
    private String analysisType;
}