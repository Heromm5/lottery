package com.hobart.lottery.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 预测方法权重实体
 * 用于持续学习机制，记录各预测方法的历史表现和当前权重
 */
@Data
@TableName("prediction_method_weight")
public class MethodWeight {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 方法代码：HOT, MISSING, BALANCED, ML, ADAPTIVE
     */
    private String methodCode;
    
    /**
     * 方法名称
     */
    private String methodName;
    
    /**
     * 当前权重（0-1，所有方法权重之和为1）
     */
    private BigDecimal weight;
    
    /**
     * 总预测次数
     */
    private Integer totalPredictions;
    
    /**
     * 命中次数（前区>=3 或 后区>=1）
     */
    private Integer totalHits;
    
    /**
     * 平滑后的命中率（EMA）
     */
    private BigDecimal hitRate;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
    
    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
    
    /**
     * 计算实际命中率
     */
    public double getActualHitRate() {
        if (totalPredictions == null || totalPredictions == 0) {
            return 0.0;
        }
        return (double) totalHits / totalPredictions;
    }
}
