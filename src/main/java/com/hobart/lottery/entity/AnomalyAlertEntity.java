package com.hobart.lottery.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 异常告警实体
 */
@Data
@TableName("anomaly_alert")
public class AnomalyAlertEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 告警类型
     */
    @TableField("alert_type")
    private String alertType;

    /**
     * 严重程度：LOW|MEDIUM|HIGH|CRITICAL
     */
    private String severity;

    /**
     * 告警描述
     */
    private String description;

    /**
     * 检测数据（JSON格式）
     */
    @TableField("detected_data")
    private String detectedData;

    /**
     * 是否已确认
     */
    private Boolean acknowledged;

    /**
     * 确认时间
     */
    @TableField("acknowledged_at")
    private LocalDateTime acknowledgedAt;

    /**
     * 检测时间
     */
    @TableField("detected_at")
    private LocalDateTime detectedAt;

    /**
     * 告警严重程度枚举
     */
    public static final String SEVERITY_LOW = "LOW";
    public static final String SEVERITY_MEDIUM = "MEDIUM";
    public static final String SEVERITY_HIGH = "HIGH";
    public static final String SEVERITY_CRITICAL = "CRITICAL";
}