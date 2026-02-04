package com.hobart.lottery.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 大乐透开奖结果实体
 */
@Data
@TableName("lottery_results")
public class LotteryResult {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 期号
     */
    private String issue;

    /**
     * 开奖日期
     */
    private LocalDate drawDate;

    /**
     * 前区号码1-5
     */
    private Integer frontBall1;
    private Integer frontBall2;
    private Integer frontBall3;
    private Integer frontBall4;
    private Integer frontBall5;

    /**
     * 后区号码1-2
     */
    private Integer backBall1;
    private Integer backBall2;

    /**
     * 前区号码字符串
     */
    private String frontBalls;

    /**
     * 后区号码字符串
     */
    private String backBalls;

    /**
     * 前区和值
     */
    private Integer frontSum;

    /**
     * 后区和值
     */
    private Integer backSum;

    /**
     * 前区奇数个数
     */
    private Integer oddCountFront;

    /**
     * 后区奇数个数
     */
    private Integer oddCountBack;

    /**
     * AC值
     */
    private Integer acValue;

    /**
     * 前区连号数
     */
    private Integer consecutiveCountFront;

    /**
     * 后区连号数
     */
    private Integer consecutiveCountBack;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * 获取前区号码数组
     */
    public int[] getFrontBallArray() {
        return new int[]{frontBall1, frontBall2, frontBall3, frontBall4, frontBall5};
    }

    /**
     * 获取后区号码数组
     */
    public int[] getBackBallArray() {
        return new int[]{backBall1, backBall2};
    }
}
