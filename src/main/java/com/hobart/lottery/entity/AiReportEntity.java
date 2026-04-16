package com.hobart.lottery.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * AI分析报告实体
 */
@Data
@TableName("ai_analysis_report")
public class AiReportEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 报告类型：daily|weekly|issue
     */
    @TableField("report_type")
    private String reportType;

    /**
     * 报告摘要
     */
    private String summary;

    /**
     * 报告内容（JSON格式）
     */
    @TableField(typeHandler = com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler.class)
    private String content;

    /**
     * AI洞察（JSON格式）
     */
    @TableField(typeHandler = com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler.class)
    private String insights;

    /**
     * 创建时间
     */
    @TableField("created_at")
    private LocalDateTime createdAt;
}