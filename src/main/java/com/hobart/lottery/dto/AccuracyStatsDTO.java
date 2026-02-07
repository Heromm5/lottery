package com.hobart.lottery.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 准确率统计DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccuracyStatsDTO {
    
    /**
     * 预测方法
     */
    private String predictMethod;
    
    /**
     * 预测方法名称
     */
    private String methodName;
    
    /**
     * 总预测次数
     */
    private Integer totalPredictions;
    
    /**
     * 前区平均命中数
     */
    private BigDecimal frontAvgHit;
    
    /**
     * 后区平均命中数
     */
    private BigDecimal backAvgHit;
    
    /**
     * 各等级中奖次数（一等奖~七等奖）
     */
    private Integer prizeCount1;
    private Integer prizeCount2;
    private Integer prizeCount3;
    private Integer prizeCount4;
    private Integer prizeCount5;
    private Integer prizeCount6;
    private Integer prizeCount7;
    
    /**
     * 总中奖次数
     */
    private Integer totalPrizeCount;
    
    /**
     * 中奖率
     */
    private Double prizeRate;
    
    /**
     * 前区命中率（命中数/5）
     */
    private Double frontHitRate;
    
    /**
     * 后区命中率（命中数/2）
     */
    private Double backHitRate;
    
    /**
     * 综合得分（用于排行）
     */
    private Double compositeScore;
    
    /**
     * 高等奖次数（一二三等）
     */
    private Integer highPrizeCount;
    
    /**
     * 排名
     */
    private Integer rank;
}
