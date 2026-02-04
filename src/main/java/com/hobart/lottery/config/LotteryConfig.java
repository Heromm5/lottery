package com.hobart.lottery.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 彩票系统配置
 * 将原来硬编码的魔法数字提取为可配置参数
 */
@Configuration
@ConfigurationProperties(prefix = "lottery")
@Data
public class LotteryConfig {
    
    /** 前区配置 */
    private ZoneConfig front = new ZoneConfig(1, 35, 5);
    
    /** 后区配置 */
    private ZoneConfig back = new ZoneConfig(1, 12, 2);
    
    /** 分析参数配置 */
    private AnalysisConfig analysis = new AnalysisConfig();
    
    /** 学习参数配置 */
    private LearningConfig learning = new LearningConfig();
    
    /**
     * 号码区域配置
     */
    @Data
    public static class ZoneConfig {
        /** 最小号码 */
        private int min;
        /** 最大号码 */
        private int max;
        /** 选号个数 */
        private int count;
        
        public ZoneConfig() {}
        
        public ZoneConfig(int min, int max, int count) {
            this.min = min;
            this.max = max;
            this.count = count;
        }
    }
    
    /**
     * 分析参数配置
     */
    @Data
    public static class AnalysisConfig {
        /** 热号冷号统计期数 */
        private int hotColdPeriod = 30;
        
        /** 遗漏分析期数 */
        private int missingPeriod = 500;
        
        /** 走势图默认期数 */
        private int trendDefaultLimit = 30;
        
        /** 关联分析期数 */
        private int associationPeriod = 200;
        
        /** 关联规则最小支持度 */
        private double minSupport = 0.02;
        
        /** 关联规则最小置信度 */
        private double minConfidence = 0.3;
    }
    
    /**
     * 学习参数配置
     */
    @Data
    public static class LearningConfig {
        /** EMA 平滑因子（0-1，越大新数据权重越高） */
        private double emaAlpha = 0.1;
        
        /** 命中判定：前区最少命中数 */
        private int frontHitThreshold = 3;
        
        /** 命中判定：后区最少命中数 */
        private int backHitThreshold = 1;
        
        /** 初始权重（均等分配） */
        private double initialWeight = 0.2;
    }
}
