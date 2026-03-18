package com.hobart.lottery.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 预测历史列表项 DTO（仅包含展示所需字段，避免实体序列化问题）
 */
@Data
public class PredictionRecordListDTO {

    private Long id;
    private String targetIssue;
    private String predictMethod;
    private String methodName;
    private String frontBalls;
    private String backBalls;
    private Integer isVerified;
    /** 是否当次最终预测（生成时选中的 Top N 注） */
    private Integer isFinal;
    private Integer frontHitCount;
    private Integer backHitCount;
    private String prizeLevel;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private LocalDateTime verifiedAt;
}
