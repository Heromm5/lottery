package com.hobart.lottery.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 验证历史记录DTO - 包含预测和实际开奖结果
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VerificationHistoryDTO {

    /**
     * 预测记录ID
     */
    private Long id;

    /**
     * 目标期号
     */
    private String targetIssue;

    /**
     * 预测方法代码
     */
    private String predictMethod;

    /**
     * 预测方法名称
     */
    private String methodName;

    /**
     * 预测前区号码字符串
     */
    private String frontBallsStr;

    /**
     * 预测后区号码字符串
     */
    private String backBallsStr;

    /**
     * 实际开奖前区号码字符串
     */
    private String actualFrontBallsStr;

    /**
     * 实际开奖后区号码字符串
     */
    private String actualBackBallsStr;

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

    /**
     * 预测创建时间
     */
    private String createdAt;

    /**
     * 验证时间
     */
    private String verifiedAt;
}
