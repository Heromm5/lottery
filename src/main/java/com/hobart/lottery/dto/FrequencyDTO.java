package com.hobart.lottery.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 号码频率统计DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FrequencyDTO {
    
    /**
     * 号码
     */
    private Integer number;
    
    /**
     * 出现次数
     */
    private Integer count;
    
    /**
     * 出现频率（百分比）
     */
    private Double frequency;
    
    /**
     * 类型：front-前区，back-后区
     */
    private String type;
}
