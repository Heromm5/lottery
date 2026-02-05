package com.hobart.lottery.domain.model;

import com.hobart.lottery.entity.LotteryResult;
import lombok.Getter;

/**
 * 号码区域枚举
 * 统一处理前区和后区的逻辑，消除重复代码
 */
@Getter
public enum NumberZone {
    
    FRONT(1, 35, 5, "前区", "front"),
    BACK(1, 12, 2, "后区", "back");
    
    /** 最小号码 */
    public final int min;
    /** 最大号码 */
    public final int max;
    /** 选号个数 */
    public final int count;
    /** 显示名称 */
    public final String displayName;
    /** 代码标识 */
    public final String code;
    
    NumberZone(int min, int max, int count, String displayName, String code) {
        this.min = min;
        this.max = max;
        this.count = count;
        this.displayName = displayName;
        this.code = code;
    }
    
    /**
     * 从开奖结果中获取该区域的号码数组
     */
    public int[] getBalls(LotteryResult result) {
        return this == FRONT ? result.getFrontBallArray() : result.getBackBallArray();
    }
    
    /**
     * 获取该区域的和值
     */
    public int getSum(LotteryResult result) {
        return this == FRONT ? result.getFrontSum() : result.getBackSum();
    }
    
    /**
     * 获取该区域的奇数个数
     */
    public int getOddCount(LotteryResult result) {
        return this == FRONT ? result.getOddCountFront() : result.getOddCountBack();
    }
    
    /**
     * 获取号码总数（用于遍历）
     */
    public int getNumberCount() {
        return max - min + 1;
    }
    
    /**
     * 根据代码获取枚举
     */
    public static NumberZone fromCode(String code) {
        for (NumberZone zone : values()) {
            if (zone.code.equalsIgnoreCase(code)) {
                return zone;
            }
        }
        throw new IllegalArgumentException("Unknown zone code: " + code);
    }
}
