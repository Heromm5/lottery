package com.hobart.lottery.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 号码遗漏统计DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MissingDTO {
    
    /**
     * 号码
     */
    private Integer number;
    
    /**
     * 当前遗漏期数
     */
    private Integer currentMissing;
    
    /**
     * 平均遗漏期数
     */
    private Double avgMissing;
    
    /**
     * 最大遗漏期数
     */
    private Integer maxMissing;
    
    /**
     * 类型：front-前区，back-后区
     */
    private String type;
}
