package com.hobart.lottery.controller;

import com.hobart.lottery.domain.model.AssociationRule;
import com.hobart.lottery.domain.model.NumberZone;
import com.hobart.lottery.dto.FrequencyDTO;
import com.hobart.lottery.dto.MissingDTO;
import com.hobart.lottery.dto.SameNumberDTO;
import com.hobart.lottery.service.AnalysisService;
import com.hobart.lottery.service.analysis.AssociationAnalyzer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 数据分析控制器
 */
@Controller
@RequestMapping("/analysis")
@RequiredArgsConstructor
public class AnalysisController {

    private final AnalysisService analysisService;
    private final AssociationAnalyzer associationAnalyzer;

    /**
     * 频率分析页面
     */
    @GetMapping("/frequency")
    public String frequency(Model model, @RequestParam(defaultValue = "0") Integer recentCount) {
        Integer count = recentCount > 0 ? recentCount : null;
        
        List<FrequencyDTO> frontFrequency = analysisService.calculateFrontFrequency(count);
        List<FrequencyDTO> backFrequency = analysisService.calculateBackFrequency(count);
        
        model.addAttribute("frontFrequency", frontFrequency);
        model.addAttribute("backFrequency", backFrequency);
        model.addAttribute("recentCount", recentCount);
        
        return "analysis/frequency";
    }

    /**
     * 频率数据API
     */
    @GetMapping("/api/frequency")
    @ResponseBody
    public Map<String, Object> getFrequencyData(@RequestParam(defaultValue = "0") Integer recentCount) {
        Integer count = recentCount > 0 ? recentCount : null;
        
        Map<String, Object> result = new HashMap<>();
        result.put("front", analysisService.calculateFrontFrequency(count));
        result.put("back", analysisService.calculateBackFrequency(count));
        
        return result;
    }

    /**
     * 遗漏分析页面
     */
    @GetMapping("/missing")
    public String missing(Model model) {
        List<MissingDTO> frontMissing = analysisService.calculateFrontMissing();
        List<MissingDTO> backMissing = analysisService.calculateBackMissing();
        
        model.addAttribute("frontMissing", frontMissing);
        model.addAttribute("backMissing", backMissing);
        
        return "analysis/missing";
    }

    /**
     * 遗漏数据API
     */
    @GetMapping("/api/missing")
    @ResponseBody
    public Map<String, Object> getMissingData() {
        Map<String, Object> result = new HashMap<>();
        result.put("front", analysisService.calculateFrontMissing());
        result.put("back", analysisService.calculateBackMissing());
        
        return result;
    }

    /**
     * 走势图页面
     */
    @GetMapping("/trend")
    public String trend(Model model, @RequestParam(defaultValue = "30") Integer limit) {
        List<Map<String, Object>> trendData = analysisService.getTrendData(limit);
        model.addAttribute("trendData", trendData);
        model.addAttribute("limit", limit);
        
        return "analysis/trend";
    }

    /**
     * 走势数据API
     */
    @GetMapping("/api/trend")
    @ResponseBody
    public List<Map<String, Object>> getTrendData(@RequestParam(defaultValue = "30") Integer limit) {
        return analysisService.getTrendData(limit);
    }

    /**
     * 综合统计页面
     */
    @GetMapping("/stats")
    public String stats(Model model) {
        model.addAttribute("oddEvenStats", analysisService.getOddEvenStats());
        model.addAttribute("sumStats", analysisService.getFrontSumStats());
        model.addAttribute("consecutiveStats", analysisService.getConsecutiveStats());
        
        // 热号冷号
        model.addAttribute("hotFront", analysisService.getHotFrontNumbers(10));
        model.addAttribute("coldFront", analysisService.getColdFrontNumbers(10));
        model.addAttribute("hotBack", analysisService.getHotBackNumbers(5));
        model.addAttribute("coldBack", analysisService.getColdBackNumbers(5));
        
        return "analysis/stats";
    }

    /**
     * 同号统计页面
     * 展示历史中奖号码完全一致的情况
     */
    @GetMapping("/same")
    public String same(Model model) {
        List<SameNumberDTO> sameNumbers = analysisService.findSameNumbers();
        model.addAttribute("sameNumbers", sameNumbers);
        model.addAttribute("totalCount", sameNumbers.size());
        
        return "analysis/same";
    }

    /**
     * 同号统计数据API
     */
    @GetMapping("/api/same")
    @ResponseBody
    public List<SameNumberDTO> getSameNumberData() {
        return analysisService.findSameNumbers();
    }

    /**
     * 关联分析页面
     */
    @GetMapping("/association")
    public String association(Model model, @RequestParam(defaultValue = "front") String zone) {
        NumberZone numberZone = "back".equalsIgnoreCase(zone) ? NumberZone.BACK : NumberZone.FRONT;
        List<AssociationRule> rules = associationAnalyzer.mineAssociations(numberZone);
        
        model.addAttribute("rules", rules);
        model.addAttribute("zone", zone);
        model.addAttribute("zoneName", numberZone.getDisplayName());
        
        return "analysis/association";
    }

    /**
     * 关联分析数据API
     */
    @GetMapping("/api/association")
    @ResponseBody
    public Map<String, Object> getAssociationData(
            @RequestParam(defaultValue = "front") String zone,
            @RequestParam(defaultValue = "50") Integer topN) {
        NumberZone numberZone = "back".equalsIgnoreCase(zone) ? NumberZone.BACK : NumberZone.FRONT;
        return associationAnalyzer.getAssociationNetwork(numberZone, topN);
    }

    /**
     * 获取与指定号码关联的号码
     */
    @GetMapping("/api/related")
    @ResponseBody
    public List<Integer> getRelatedNumbers(
            @RequestParam Integer number,
            @RequestParam(defaultValue = "front") String zone,
            @RequestParam(defaultValue = "5") Integer topN) {
        NumberZone numberZone = "back".equalsIgnoreCase(zone) ? NumberZone.BACK : NumberZone.FRONT;
        return associationAnalyzer.getRelatedNumbers(number, numberZone, topN);
    }

    /**
     * 连续期关联分析数据API
     */
    @GetMapping("/api/sequential")
    @ResponseBody
    public List<AssociationRule> getSequentialAssociations(
            @RequestParam(defaultValue = "front") String zone) {
        NumberZone numberZone = "back".equalsIgnoreCase(zone) ? NumberZone.BACK : NumberZone.FRONT;
        return associationAnalyzer.mineSequentialAssociations(numberZone);
    }
}
