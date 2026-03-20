package com.hobart.lottery.dto;

import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import java.util.List;

/**
 * 定胆生成预测请求体
 */
@Data
public class GeneratePinnedRequest {

    @Min(value = 1, message = "生成注数至少为 1")
    @Max(value = 50, message = "生成注数最多为 50")
    private int count = 5;

    @NotBlank(message = "预测方法不能为空")
    private String method;

    private String targetIssue;

    /** 前区胆码，可空或空列表（须与后区至少一侧非空，由业务校验） */
    private List<Integer> lockedFront;

    /** 后区胆码 */
    private List<Integer> lockedBack;
}
