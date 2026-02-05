package com.hobart.lottery.controller.api;

import com.hobart.lottery.common.result.Result;
import com.hobart.lottery.domain.model.AssociationRule;
import com.hobart.lottery.domain.model.NumberZone;
import com.hobart.lottery.dto.AccuracyStatsDTO;
import com.hobart.lottery.dto.FrequencyDTO;
import com.hobart.lottery.dto.MissingDTO;
import com.hobart.lottery.service.AnalysisService;
import com.hobart.lottery.service.analysis.AssociationAnalyzer;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 数据分析 API 控制器
 */
@RestController
@RequestMapping("/api/analysis")
@RequiredArgsConstructor
public class AnalysisApiController {

    private final AnalysisService analysisService;
    private final AssociationAnalyzer associationAnalyzer;

    /**
     * 前区频率统计
     */
    @GetMapping("/frequency/front")
    public Result<List<FrequencyDTO>> getFrontFrequency(@RequestParam(required = false) Integer recentCount) {
        if (recentCount != null) {
            return Result.success(analysisService.calculateFrontFrequency(recentCount));
        }
        return Result.success(analysisService.calculateFrontFrequency());
    }

    /**
     * 后区频率统计
     */
    @GetMapping("/frequency/back")
    public Result<List<FrequencyDTO>> getBackFrequency(@RequestParam(required = false) Integer recentCount) {
        if (recentCount != null) {
            return Result.success(analysisService.calculateBackFrequency(recentCount));
        }
        return Result.success(analysisService.calculateBackFrequency());
    }

    /**
     * 前区遗漏分析
     */
    @GetMapping("/missing/front")
    public Result<List<MissingDTO>> getFrontMissing() {
        return Result.success(analysisService.calculateFrontMissing());
    }

    /**
     * 后区遗漏分析
     */
    @GetMapping("/missing/back")
    public Result<List<MissingDTO>> getBackMissing() {
        return Result.success(analysisService.calculateBackMissing());
    }

    /**
     * 走势分析
     */
    @GetMapping("/trend")
    public Result<List<Map<String, Object>>> getTrend(@RequestParam(defaultValue = "30") int size) {
        return Result.success(analysisService.getTrendData(size));
    }

    /**
     * 奇偶比统计
     */
    @GetMapping("/stats/odd-even")
    public Result<Map<String, Integer>> getOddEvenStats() {
        return Result.success(analysisService.getOddEvenStats());
    }

    /**
     * 和值分布统计
     */
    @GetMapping("/stats/sum")
    public Result<Map<String, Integer>> getSumStats() {
        return Result.success(analysisService.getFrontSumStats());
    }

    /**
     * 连号统计
     */
    @GetMapping("/stats/consecutive")
    public Result<Map<Integer, Integer>> getConsecutiveStats() {
        return Result.success(analysisService.getConsecutiveStats());
    }

    /**
     * 热号推荐
     */
    @GetMapping("/hot")
    public Result<Map<String, List<Integer>>> getHotNumbers(
            @RequestParam(defaultValue = "10") int count) {
        Map<String, List<Integer>> hot = new HashMap<>();
        hot.put("front", analysisService.getHotFrontNumbers(count));
        hot.put("back", analysisService.getHotBackNumbers(count));
        return Result.success(hot);
    }

    /**
     * 冷号推荐
     */
    @GetMapping("/cold")
    public Result<Map<String, List<Integer>>> getColdNumbers(
            @RequestParam(defaultValue = "10") int count) {
        Map<String, List<Integer>> cold = new HashMap<>();
        cold.put("front", analysisService.getColdFrontNumbers(count));
        cold.put("back", analysisService.getColdBackNumbers(count));
        return Result.success(cold);
    }

    /**
     * 遗漏到期号码
     */
    @GetMapping("/missing-due")
    public Result<Map<String, List<Integer>>> getMissingDue(
            @RequestParam(defaultValue = "10") int count) {
        Map<String, List<Integer>> missingDue = new HashMap<>();
        missingDue.put("front", analysisService.getMissingDueFrontNumbers(count));
        missingDue.put("back", analysisService.getMissingDueBackNumbers(count));
        return Result.success(missingDue);
    }

    /**
     * 关联规则列表
     */
    @GetMapping("/association")
    public Result<List<AssociationRule>> getAssociationRules(
            @RequestParam(defaultValue = "front") String zone) {
        NumberZone numberZone = "back".equalsIgnoreCase(zone) ? NumberZone.BACK : NumberZone.FRONT;
        return Result.success(associationAnalyzer.mineAssociations(numberZone));
    }

    /**
     * 获取关联网络数据
     */
    @GetMapping("/association/network")
    public Result<Map<String, Object>> getAssociationNetwork(
            @RequestParam(defaultValue = "front") String zone,
            @RequestParam(defaultValue = "50") int topN) {
        NumberZone numberZone = "back".equalsIgnoreCase(zone) ? NumberZone.BACK : NumberZone.FRONT;
        return Result.success(associationAnalyzer.getAssociationNetwork(numberZone, topN));
    }
}
