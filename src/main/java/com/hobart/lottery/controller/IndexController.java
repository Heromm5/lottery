package com.hobart.lottery.controller;

import com.hobart.lottery.dto.AccuracyStatsDTO;
import com.hobart.lottery.entity.LotteryResult;
import com.hobart.lottery.service.AnalysisService;
import com.hobart.lottery.service.LotteryService;
import com.hobart.lottery.service.VerificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.Map;

/**
 * 首页控制器
 */
@Controller
@RequiredArgsConstructor
public class IndexController {

    private final LotteryService lotteryService;
    private final AnalysisService analysisService;
    private final VerificationService verificationService;

    @GetMapping("/")
    public String index(Model model) {
        // 最新开奖结果
        LotteryResult latest = lotteryService.getLatestResult();
        model.addAttribute("latest", latest);

        // 最近10期开奖
        List<LotteryResult> recentResults = lotteryService.getRecentResults(10);
        model.addAttribute("recentResults", recentResults);

        // 统计数据
        long totalCount = lotteryService.count();
        model.addAttribute("totalCount", totalCount);

        // 奇偶比统计
        Map<String, Integer> oddEvenStats = analysisService.getOddEvenStats();
        model.addAttribute("oddEvenStats", oddEvenStats);

        // 和值分布
        Map<String, Integer> sumStats = analysisService.getFrontSumStats();
        model.addAttribute("sumStats", sumStats);

        // 准确率统计
        List<AccuracyStatsDTO> accuracyStats = verificationService.getAllAccuracyStats();
        model.addAttribute("accuracyStats", accuracyStats);

        // 下一期期号
        String nextIssue = lotteryService.generateNextIssue();
        model.addAttribute("nextIssue", nextIssue);

        return "index";
    }
}
