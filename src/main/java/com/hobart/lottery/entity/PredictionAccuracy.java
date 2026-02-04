package com.hobart.lottery.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 预测准确率统计实体
 */
@Data
@TableName("prediction_accuracy")
public class PredictionAccuracy {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 预测方法
     */
    private String predictMethod;

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
     * 一等奖次数
     */
    @TableField("prize_count_1")
    private Integer prizeCount1;

    /**
     * 二等奖次数
     */
    @TableField("prize_count_2")
    private Integer prizeCount2;

    /**
     * 三等奖次数
     */
    @TableField("prize_count_3")
    private Integer prizeCount3;

    /**
     * 四等奖次数
     */
    @TableField("prize_count_4")
    private Integer prizeCount4;

    /**
     * 五等奖次数
     */
    @TableField("prize_count_5")
    private Integer prizeCount5;

    /**
     * 六等奖次数
     */
    @TableField("prize_count_6")
    private Integer prizeCount6;

    /**
     * 七等奖次数
     */
    @TableField("prize_count_7")
    private Integer prizeCount7;

    private LocalDateTime updatedAt;
}
