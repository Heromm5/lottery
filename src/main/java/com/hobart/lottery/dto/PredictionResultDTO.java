package com.hobart.lottery.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 预测结果DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PredictionResultDTO {
    
    /**
     * 预测ID
     */
    private Long id;
    
    /**
     * 目标期号
     */
    private String targetIssue;
    
    /**
     * 预测方法
     */
    private String predictMethod;
    
    /**
     * 预测方法名称
     */
    private String methodName;
    
    /**
     * 前区号码数组
     */
    private int[] frontBalls;
    
    /**
     * 后区号码数组
     */
    private int[] backBalls;
    
    /**
     * 前区号码字符串
     */
    private String frontBallsStr;
    
    /**
     * 后区号码字符串
     */
    private String backBallsStr;
    
    /**
     * 是否已验证
     */
    private Boolean verified;
    
    /**
     * 前区命中数
     */
    private Integer frontHitCount;
    
    /**
     * 后区命中数
     */
    private Integer backHitCount;
    
    /**
     * 中奖等级
     */
    private String prizeLevel;
    
    public static String getMethodDisplayName(String method) {
        return switch (method) {
            case "HOT" -> "热号优先";
            case "MISSING" -> "遗漏回补";
            case "BALANCED" -> "冷热均衡";
            case "ML" -> "机器学习";
            case "ADAPTIVE" -> "自适应预测";
            default -> method;
        };
    }
}
