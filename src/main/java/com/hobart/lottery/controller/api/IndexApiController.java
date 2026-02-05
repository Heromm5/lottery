package com.hobart.lottery.controller.api;

import com.hobart.lottery.common.result.Result;
import com.hobart.lottery.dto.AccuracyStatsDTO;
import com.hobart.lottery.entity.LotteryResult;
import com.hobart.lottery.service.AnalysisService;
import com.hobart.lottery.service.LotteryService;
import com.hobart.lottery.service.VerificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 首页 API 控制器
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class IndexApiController {

    private final LotteryService lotteryService;
    private final AnalysisService analysisService;
    private final VerificationService verificationService;

    /**
     * 获取首页数据
     */
    @GetMapping("/index")
    public Result<Map<String, Object>> getIndexData() {
        Map<String, Object> data = new HashMap<>();

        // 最新开奖结果
        data.put("latest", lotteryService.getLatestResult());

        // 最近10期开奖
        data.put("recentResults", lotteryService.getRecentResults(10));

        // 统计数据
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalCount", lotteryService.count());
        stats.put("nextIssue", lotteryService.generateNextIssue());
        stats.put("methodCount", 5);
        // 准确率统计数量
        try {
            List<AccuracyStatsDTO> accuracyStats = verificationService.getAllAccuracyStats();
            stats.put("statsCount", accuracyStats.size());
        } catch (Exception e) {
            stats.put("statsCount", 0);
        }
        data.put("stats", stats);

        // 奇偶比统计
        data.put("oddEvenStats", analysisService.getOddEvenStats());

        return Result.success(data);
    }
}
