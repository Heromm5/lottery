package com.hobart.lottery.ai.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 异常告警DTO
 */
@Data
public class AnomalyAlert {

    /**
     * 告警类型
     */
    private String type;

    /**
     * 严重程度：LOW|MEDIUM|HIGH|CRITICAL
     */
    private String severity;

    /**
     * 告警描述
     */
    private String description;

    /**
     * 告警数据
     */
    private Map<String, Object> data = new HashMap<>();

    /**
     * 检测时间
     */
    private LocalDateTime detectedAt;

    /**
     * 是否已确认
     */
    private Boolean acknowledged;

    /**
     * 告警ID（持久化后有值）
     */
    private Long id;
}