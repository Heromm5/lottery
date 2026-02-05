package com.hobart.lottery.controller.api;

import com.hobart.lottery.common.result.PageResult;
import com.hobart.lottery.common.result.Result;
import com.hobart.lottery.dto.AccuracyStatsDTO;
import com.hobart.lottery.dto.PredictionResultDTO;
import com.hobart.lottery.dto.VerificationHistoryDTO;
import com.hobart.lottery.service.VerificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 验证 API 控制器
 */
@RestController
@RequestMapping("/api/verification")
@RequiredArgsConstructor
public class VerificationApiController {

    private final VerificationService verificationService;

    /**
     * 获取准确率统计
     */
    @GetMapping("/stats")
    public Result<List<AccuracyStatsDTO>> getAccuracyStats() {
        return Result.success(verificationService.getAllAccuracyStats());
    }

    /**
     * 获取验证历史
     */
    @GetMapping("/history")
    public Result<PageResult<VerificationHistoryDTO>> getHistory(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        List<VerificationHistoryDTO> records = verificationService.getVerificationHistory(page, size);
        int total = verificationService.countVerificationHistory();
        PageResult<VerificationHistoryDTO> result = new PageResult<>(records, total, size, page);
        return Result.success(result);
    }

    /**
     * 获取未验证的期号列表
     */
    @GetMapping("/unverified/issues")
    public Result<List<String>> getUnverifiedIssues() {
        return Result.success(verificationService.getUnverifiedIssues());
    }

    /**
     * 触发验证
     */
    @PostMapping("/verify/{issue}")
    public Result<List<PredictionResultDTO>> verify(@PathVariable String issue) {
        try {
            List<PredictionResultDTO> results = verificationService.verifyPredictions(issue);
            return Result.success(results);
        } catch (RuntimeException e) {
            return Result.fail("验证失败: " + e.getMessage());
        }
    }

    /**
     * 检查某期是否有开奖结果
     */
    @GetMapping("/check/{issue}")
    public Result<Boolean> checkDrawResult(@PathVariable String issue) {
        return Result.success(verificationService.hasDrawResult(issue));
    }
}
