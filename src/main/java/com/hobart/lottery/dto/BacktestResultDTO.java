package com.hobart.lottery.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 回测结果DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BacktestResultDTO {
    
    /**
     * 回测方法
     */
    private String method;
    
    /**
     * 方法名称
     */
    private String methodName;
    
    /**
     * 回测期数
     */
    private Integer totalIssues;
    
    /**
     * 总预测次数（每期每种方法可能有多注）
     */
    private Integer totalPredictions;
    
    /**
     * 平均前区命中数
     */
    private BigDecimal avgFrontHit;
    
    /**
     * 平均后区命中数
     */
    private BigDecimal avgBackHit;
    
    /**
     * 前区命中率
     */
    private Double frontHitRate;
    
    /**
     * 后区命中率
     */
    private Double backHitRate;
    
    /**
     * 各等级中奖次数
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
     * 高等奖次数
     */
    private Integer highPrizeCount;
    
    /**
     * 理论投入金额（每注2元）
     */
    private Integer totalCost;
    
    /**
     * 理论奖金（按官方奖金计算）
     */
    private Long totalPrizeMoney;
    
    /**
     * 盈亏（负数表示亏损）
     */
    private Long profitLoss;
    
    /**
     * 投资回报率（%）
     */
    private Double roi;
    
    /**
     * 最佳表现期号
     */
    private String bestIssue;
    
    /**
     * 最佳表现（中奖等级）
     */
    private String bestPrize;
    
    /**
     * 回测详情（每期结果）
     */
    private List<BacktestDetailDTO> details;
    
    /**
     * 回测总结评价
     */
    private String evaluation;
    
    /**
     * 回测明细DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BacktestDetailDTO {
        /**
         * 期号
         */
        private String issue;
        
        /**
         * 预测号码
         */
        private String prediction;
        
        /**
         * 实际开奖号码
         */
        private String actualResult;
        
        /**
         * 前区命中数
         */
        private Integer frontHit;
        
        /**
         * 后区命中数
         */
        private Integer backHit;
        
        /**
         * 中奖等级
         */
        private String prizeLevel;
        
        /**
         * 理论奖金
         */
        private Long prizeMoney;
    }
}
