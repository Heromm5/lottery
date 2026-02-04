package com.hobart.lottery.controller;

import com.hobart.lottery.dto.PredictionResultDTO;
import com.hobart.lottery.service.LotteryService;
import com.hobart.lottery.service.PredictionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 预测控制器
 */
@Controller
@RequestMapping("/prediction")
@RequiredArgsConstructor
public class PredictionController {

    private final PredictionService predictionService;
    private final LotteryService lotteryService;

    /**
     * 预测页面
     */
    @GetMapping
    public String predict(Model model) {
        model.addAttribute("methods", predictionService.getAllMethods());
        model.addAttribute("nextIssue", lotteryService.generateNextIssue());
        return "prediction/predict";
    }

    /**
     * 生成预测：全部生成（每种方法 count 注），并自动为每种方法推荐一注最可能预测。
     */
    @PostMapping("/generate")
    @ResponseBody
    public Map<String, Object> generate(
            @RequestParam(defaultValue = "5") Integer count,
            @RequestParam(required = false) String targetIssue) {

        Map<String, Object> result = new HashMap<>();

        try {
            Map<String, Object> data = predictionService.generateAllThenRecommend(count, targetIssue);
            @SuppressWarnings("unchecked")
            List<PredictionResultDTO> allPredictions = (List<PredictionResultDTO>) data.get("allPredictions");
            @SuppressWarnings("unchecked")
            List<PredictionResultDTO> recommendations = (List<PredictionResultDTO>) data.get("recommendations");

            result.put("success", true);
            result.put("allPredictions", allPredictions);
            result.put("recommendations", recommendations);
            result.put("message", "成功生成 " + allPredictions.size() + " 注预测，并为 " + recommendations.size() + " 种方法各推荐一注");
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "生成失败: " + e.getMessage());
        }

        return result;
    }

    /**
     * 预测历史页面
     */
    @GetMapping("/history")
    public String history(Model model, @RequestParam(required = false) String issue) {
        if (issue != null && !issue.isEmpty()) {
            model.addAttribute("predictions", predictionService.getPredictionsByIssue(issue));
            model.addAttribute("selectedIssue", issue);
        } else {
            model.addAttribute("predictions", predictionService.getRecentPredictions(50));
        }
        return "prediction/history";
    }

    /**
     * 获取某期预测记录API
     */
    @GetMapping("/api/byIssue")
    @ResponseBody
    public List<PredictionResultDTO> getByIssue(@RequestParam String issue) {
        return predictionService.getPredictionsByIssue(issue);
    }

    /**
     * 获取最近预测记录API
     */
    @GetMapping("/api/recent")
    @ResponseBody
    public List<PredictionResultDTO> getRecent(@RequestParam(defaultValue = "20") Integer limit) {
        return predictionService.getRecentPredictions(limit);
    }
}
