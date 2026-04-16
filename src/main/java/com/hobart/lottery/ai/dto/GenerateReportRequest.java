package com.hobart.lottery.ai.dto;

import lombok.Data;

/**
 * 报告生成请求体
 */
@Data
public class GenerateReportRequest {

    /**
     * 报告类型：daily|weekly|issue
     */
    private String reportType;

    /**
     * 起始期号（issue类型使用）
     */
    private Integer startIssue;

    /**
     * 结束期号（issue类型使用）
     */
    private Integer endIssue;
}