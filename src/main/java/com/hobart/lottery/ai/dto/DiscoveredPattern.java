package com.hobart.lottery.ai.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 发现规律 DTO
 * 用于存储 AI 发现的数据规律和模式
 */
@Data
public class DiscoveredPattern {
    
    /**
     * 规律类型: frequency(频率), missing(遗漏), trend(趋势), association(关联)
     */
    private String patternType;
    
    /**
     * 规律描述
     */
    private String description;
    
    /**
     * 置信度 0-1
     */
    private Double confidence;
    
    /**
     * 支持证据
     */
    private Map<String, Object> evidence;
    
    /**
     * 发现时间
     */
    private LocalDateTime discoveredAt;
}