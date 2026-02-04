package com.hobart.lottery.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

/**
 * 同号统计DTO
 * 用于展示历史中奖号码完全一致的情况
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SameNumberDTO {

    /**
     * 前区号码 (如 "3,6,17,21,33")
     */
    private String frontBalls;

    /**
     * 后区号码 (如 "5,11")
     */
    private String backBalls;

    /**
     * 出现次数
     */
    private Integer count;

    /**
     * 期号列表
     */
    private List<String> issues;

    /**
     * 开奖日期列表
     */
    private List<LocalDate> dates;
}
