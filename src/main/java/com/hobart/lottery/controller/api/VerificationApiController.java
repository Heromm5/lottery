package com.hobart.lottery.controller.api;

import com.hobart.lottery.common.result.PageResult;
import com.hobart.lottery.common.result.Result;
import com.hobart.lottery.dto.AccuracyStatsDTO;
import com.hobart.lottery.dto.BacktestResultDTO;
import com.hobart.lottery.dto.PredictionResultDTO;
import com.hobart.lottery.dto.VerificationHistoryDTO;
import com.hobart.lottery.service.BacktestService;
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
    private final BacktestService backtestService;

    /**
     * 获取准确率统计（默认按综合得分排序）
     */
    @GetMapping("/stats")
    public Result<List<AccuracyStatsDTO>> getAccuracyStats() {
        return Result.success(verificationService.getAllAccuracyStats());
    }

    /**
     * 获取准确率排行榜
     * @param sortBy 排序方式：composite(综合得分), hit(平均命中), prize(中奖率), high(高等奖)
     * @param ascending 是否升序（默认false降序）
     */
    @GetMapping("/stats/ranking")
    public Result<List<AccuracyStatsDTO>> getAccuracyRanking(
            @RequestParam(defaultValue = "composite") String sortBy,
            @RequestParam(defaultValue = "false") boolean ascending) {
        return Result.success(verificationService.getAllAccuracyStats(sortBy, ascending));
    }

    /**
     * 批量历史回测
     * @param method 预测方法（不传则测试所有方法）
     * @param issueCount 回测期数（默认50期）
     * @param predictionsPerIssue 每期预测注数（默认5注）
     */
    @PostMapping("/backtest")
    public Result<List<BacktestResultDTO>> runBacktest(
            @RequestParam(required = false) String method,
            @RequestParam(defaultValue = "50") int issueCount,
            @RequestParam(defaultValue = "5") int predictionsPerIssue) {
        try {
            List<BacktestResultDTO> results = backtestService.runBacktest(method, issueCount, predictionsPerIssue);
            return Result.success(results);
        } catch (Exception e) {
            return Result.fail("回测失败: " + e.getMessage());
        }
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
