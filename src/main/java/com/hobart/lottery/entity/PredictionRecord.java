package com.hobart.lottery.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 预测记录实体
 */
@Data
@TableName("prediction_records")
public class PredictionRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 预测目标期号
     */
    private String targetIssue;

    /**
     * 预测方法
     */
    private String predictMethod;

    /**
     * 预测前区号码字符串
     */
    private String frontBalls;

    /**
     * 预测后区号码字符串
     */
    private String backBalls;

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
     * 是否已验证 0-未验证 1-已验证
     */
    private Integer isVerified;

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

    private LocalDateTime createdAt;
    private LocalDateTime verifiedAt;

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

    /**
     * 设置前区号码
     */
    public void setFrontBallArray(int[] balls) {
        if (balls != null && balls.length == 5) {
            this.frontBall1 = balls[0];
            this.frontBall2 = balls[1];
            this.frontBall3 = balls[2];
            this.frontBall4 = balls[3];
            this.frontBall5 = balls[4];
            this.frontBalls = balls[0] + "," + balls[1] + "," + balls[2] + "," + balls[3] + "," + balls[4];
        }
    }

    /**
     * 设置后区号码
     */
    public void setBackBallArray(int[] balls) {
        if (balls != null && balls.length == 2) {
            this.backBall1 = balls[0];
            this.backBall2 = balls[1];
            this.backBalls = balls[0] + "," + balls[1];
        }
    }
}
