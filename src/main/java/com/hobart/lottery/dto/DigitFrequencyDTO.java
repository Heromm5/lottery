package com.hobart.lottery.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 尾数频率DTO
 * 用于展示0-9尾数的出现频率
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DigitFrequencyDTO {
    
    /**
     * 尾数（0-9）
     */
    private int digit;
    
    /**
     * 出现次数
     */
    private int count;
    
    /**
     * 出现频率百分比
     */
    private double frequency;
    
    /**
     * 上次出现期数（遗漏值）
     */
    private int missing;
}
