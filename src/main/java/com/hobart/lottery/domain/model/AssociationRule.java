package com.hobart.lottery.domain.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

/**
 * 关联规则值对象
 * 表示号码之间的关联关系
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AssociationRule {
    
    /**
     * 前件（如果出现这些号码）
     */
    private Set<Integer> antecedent;
    
    /**
     * 后件（则这些号码也容易出现）
     */
    private Set<Integer> consequent;
    
    /**
     * 支持度（同时出现的频率）
     * 范围：0-1
     */
    private double support;
    
    /**
     * 置信度（条件概率）
     * 范围：0-1
     * 含义：在前件出现的情况下，后件出现的概率
     */
    private double confidence;
    
    /**
     * 提升度
     * >1 表示正相关，<1 表示负相关，=1 表示无关
     */
    private double lift;
    
    /**
     * 区域类型
     * FRONT: 前区内关联
     * BACK: 后区内关联
     * CROSS: 跨区关联
     */
    private String zoneType;
    
    /**
     * 判断是否为强关联规则
     * 支持度 >= 0.02 且 置信度 >= 0.3 且 提升度 > 1
     */
    public boolean isStrong(double minSupport, double minConfidence) {
        return support >= minSupport && confidence >= minConfidence && lift > 1;
    }
    
    /**
     * 获取规则描述
     */
    public String getDescription() {
        return String.format("%s -> %s (支持度:%.2f%%, 置信度:%.2f%%, 提升度:%.2f)",
            formatNumbers(antecedent),
            formatNumbers(consequent),
            support * 100,
            confidence * 100,
            lift);
    }
    
    private String formatNumbers(Set<Integer> numbers) {
        if (numbers == null || numbers.isEmpty()) {
            return "[]";
        }
        StringBuilder sb = new StringBuilder("[");
        for (Integer num : numbers) {
            if (sb.length() > 1) sb.append(", ");
            sb.append(String.format("%02d", num));
        }
        sb.append("]");
        return sb.toString();
    }
}
