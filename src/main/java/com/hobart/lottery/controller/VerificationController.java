package com.hobart.lottery.controller;

import com.hobart.lottery.dto.AccuracyStatsDTO;
import com.hobart.lottery.dto.PredictionResultDTO;
import com.hobart.lottery.entity.LotteryResult;
import com.hobart.lottery.service.LotteryService;
import com.hobart.lottery.service.VerificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 验证控制器
 */
@Controller
@RequestMapping("/verification")
@RequiredArgsConstructor
public class VerificationController {

    private final VerificationService verificationService;
    private final LotteryService lotteryService;

    /**
     * 验证页面
     */
    @GetMapping
    public String verify(Model model) {
        // 获取未验证的期号列表
        List<String> unverifiedIssues = verificationService.getUnverifiedIssues();
        model.addAttribute("unverifiedIssues", unverifiedIssues);
        
        // 获取最新开奖结果
        LotteryResult latest = lotteryService.getLatestResult();
        model.addAttribute("latestResult", latest);
        
        return "verification/verify";
    }

    /**
     * 执行验证
     */
    @PostMapping("/execute")
    @ResponseBody
    public Map<String, Object> executeVerification(@RequestParam String issue) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 检查是否有开奖结果
            if (!verificationService.hasDrawResult(issue)) {
                result.put("success", false);
                result.put("message", "期号 " + issue + " 还没有开奖结果，无法验证");
                return result;
            }
            
            List<PredictionResultDTO> verified = verificationService.verifyPredictions(issue);
            result.put("success", true);
            result.put("data", verified);
            result.put("message", "成功验证 " + verified.size() + " 条预测记录");
            
            // 统计结果
            long prizeCount = verified.stream()
                .filter(p -> p.getPrizeLevel() != null && !p.getPrizeLevel().equals("未中奖"))
                .count();
            result.put("prizeCount", prizeCount);
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "验证失败: " + e.getMessage());
        }
        
        return result;
    }

    /**
     * 准确率统计页面
     */
    @GetMapping("/stats")
    public String stats(Model model) {
        List<AccuracyStatsDTO> stats = verificationService.getAllAccuracyStats();
        model.addAttribute("stats", stats);
        return "verification/stats";
    }

    /**
     * 获取准确率统计API
     */
    @GetMapping("/api/stats")
    @ResponseBody
    public List<AccuracyStatsDTO> getStats() {
        return verificationService.getAllAccuracyStats();
    }

    /**
     * 检查期号是否有开奖结果
     */
    @GetMapping("/api/checkIssue")
    @ResponseBody
    public Map<String, Object> checkIssue(@RequestParam String issue) {
        Map<String, Object> result = new HashMap<>();
        boolean hasResult = verificationService.hasDrawResult(issue);
        result.put("hasResult", hasResult);
        
        if (hasResult) {
            LotteryResult lr = lotteryService.getByIssue(issue);
            result.put("drawResult", lr);
        }
        
        return result;
    }
}
