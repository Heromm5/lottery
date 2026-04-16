package com.hobart.lottery.service.analysis;

import com.hobart.lottery.domain.model.NumberZone;
import com.hobart.lottery.dto.DigitFrequencyDTO;
import com.hobart.lottery.dto.FrequencyDTO;
import com.hobart.lottery.dto.MissingDTO;
import com.hobart.lottery.dto.SameNumberDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 分析服务门面
 * 统一入口，委托给各个专门的分析器
 */
@Service
@RequiredArgsConstructor
public class AnalysisFacade {
    
    private final FrequencyAnalyzer frequencyAnalyzer;
    private final MissingAnalyzer missingAnalyzer;
    private final TrendAnalyzer trendAnalyzer;
    private final StatisticsAnalyzer statisticsAnalyzer;
    
    // ==================== 频率分析 ====================
    
    public List<FrequencyDTO> calculateFrontFrequency() {
        return frequencyAnalyzer.calculateFrequency(NumberZone.FRONT, null);
    }
    
    public List<FrequencyDTO> calculateFrontFrequency(Integer recentCount) {
        return frequencyAnalyzer.calculateFrequency(NumberZone.FRONT, recentCount);
    }
    
    public List<FrequencyDTO> calculateBackFrequency() {
        return frequencyAnalyzer.calculateFrequency(NumberZone.BACK, null);
    }
    
    public List<FrequencyDTO> calculateBackFrequency(Integer recentCount) {
        return frequencyAnalyzer.calculateFrequency(NumberZone.BACK, recentCount);
    }
    
    // ==================== 遗漏分析 ====================
    
    public List<MissingDTO> calculateFrontMissing() {
        return missingAnalyzer.calculateMissing(NumberZone.FRONT);
    }
    
    public List<MissingDTO> calculateBackMissing() {
        return missingAnalyzer.calculateMissing(NumberZone.BACK);
    }
    
    // ==================== 热号冷号 ====================
    
    public List<Integer> getHotFrontNumbers(int count) {
        return frequencyAnalyzer.getHotNumbers(NumberZone.FRONT, count);
    }
    
    public List<Integer> getHotBackNumbers(int count) {
        return frequencyAnalyzer.getHotNumbers(NumberZone.BACK, count);
    }
    
    public List<Integer> getColdFrontNumbers(int count) {
        return frequencyAnalyzer.getColdNumbers(NumberZone.FRONT, count);
    }
    
    public List<Integer> getColdBackNumbers(int count) {
        return frequencyAnalyzer.getColdNumbers(NumberZone.BACK, count);
    }
    
    // ==================== 遗漏到期 ====================
    
    public List<Integer> getMissingDueFrontNumbers(int count) {
        return missingAnalyzer.getMissingDueNumbers(NumberZone.FRONT, count);
    }
    
    public List<Integer> getMissingDueBackNumbers(int count) {
        return missingAnalyzer.getMissingDueNumbers(NumberZone.BACK, count);
    }
    
    // ==================== 号码评分 ====================
    
    public Map<Integer, Double> getFrontNumberScores() {
        return frequencyAnalyzer.getNumberScores(NumberZone.FRONT, missingAnalyzer);
    }
    
    public Map<Integer, Double> getBackNumberScores() {
        return frequencyAnalyzer.getNumberScores(NumberZone.BACK, missingAnalyzer);
    }
    
    // ==================== 走势分析 ====================
    
    public List<Map<String, Object>> getTrendData(int limit) {
        return trendAnalyzer.getTrendData(limit);
    }
    
    public List<Map<String, Object>> getTrendData() {
        return trendAnalyzer.getTrendData();
    }
    
    // ==================== 统计分析 ====================
    
    public Map<String, Integer> getOddEvenStats() {
        return statisticsAnalyzer.getOddEvenStats();
    }
    
    public Map<String, Integer> getFrontSumStats() {
        return statisticsAnalyzer.getFrontSumStats();
    }
    
    public Map<Integer, Integer> getConsecutiveStats() {
        return statisticsAnalyzer.getConsecutiveStats();
    }
    
    public List<SameNumberDTO> findSameNumbers() {
        return statisticsAnalyzer.findSameNumbers();
    }
    
    // ==================== 直接访问分析器 ====================
    
    public FrequencyAnalyzer getFrequencyAnalyzer() {
        return frequencyAnalyzer;
    }
    
    public MissingAnalyzer getMissingAnalyzer() {
        return missingAnalyzer;
    }
    
    public TrendAnalyzer getTrendAnalyzer() {
        return trendAnalyzer;
    }
    
    public StatisticsAnalyzer getStatisticsAnalyzer() {
        return statisticsAnalyzer;
    }
    
    // ==================== 尾数分析 ====================
    
    /**
     * 获取前区尾数频率
     */
    public List<DigitFrequencyDTO> getFrontDigitFrequency() {
        return statisticsAnalyzer.getDigitFrequency(NumberZone.FRONT);
    }
    
    /**
     * 获取后区尾数频率
     */
    public List<DigitFrequencyDTO> getBackDigitFrequency() {
        return statisticsAnalyzer.getDigitFrequency(NumberZone.BACK);
    }
    
    /**
     * 获取尾数和值统计
     */
    public Map<String, Integer> getDigitSumStats() {
        return statisticsAnalyzer.getDigitSumStats();
    }
    
    // ==================== 区间分析 ====================
    
    /**
     * 获取区间分布统计
     */
    public Map<String, Map<String, Integer>> getZoneDistribution() {
        return statisticsAnalyzer.getZoneDistribution();
    }
    
    // ==================== 综合分析（供 AI 使用） ====================
    
    /**
     * 获取综合分析数据 - 供 AI 规则发现使用
     * 
     * @param periods 历史期数
     * @return 综合分析数据字符串
     */
    public String getComprehensiveAnalysis(int periods) {
        StringBuilder sb = new StringBuilder();
        
        // 频率分析
        sb.append("=== 号码频率分析 (最近").append(periods).append("期) ===\n");
        List<FrequencyDTO> frontFreq = calculateFrontFrequency(periods);
        List<FrequencyDTO> backFreq = calculateBackFrequency(periods);
        
        // 前区频率 Top10
        sb.append("前区频率 Top10: [");
        for (int i = 0; i < Math.min(10, frontFreq.size()); i++) {
            FrequencyDTO f = frontFreq.get(i);
            if (i > 0) sb.append(", ");
            sb.append(f.getNumber()).append(":").append(f.getCount());
        }
        sb.append("]\n");
        
        // 后区频率 Top10
        sb.append("后区频率 Top10: [");
        for (int i = 0; i < Math.min(10, backFreq.size()); i++) {
            FrequencyDTO f = backFreq.get(i);
            if (i > 0) sb.append(", ");
            sb.append(f.getNumber()).append(":").append(f.getCount());
        }
        sb.append("]\n");
        
        // 热号冷号
        sb.append("\n=== 热号冷号 ===\n");
        sb.append("前区热号: ").append(getHotFrontNumbers(10)).append("\n");
        sb.append("前区冷号: ").append(getColdFrontNumbers(10)).append("\n");
        sb.append("后区热号: ").append(getHotBackNumbers(6)).append("\n");
        sb.append("后区冷号: ").append(getColdBackNumbers(6)).append("\n");
        
        // 遗漏分析
        sb.append("\n=== 遗漏分析 ===\n");
        List<MissingDTO> frontMissing = calculateFrontMissing();
        List<MissingDTO> backMissing = calculateBackMissing();
        
        // 前区遗漏 Top10 - 排序
        List<MissingDTO> sortedFrontMissing = frontMissing.stream()
            .sorted(Comparator.comparing(MissingDTO::getCurrentMissing).reversed())
            .limit(10)
            .collect(Collectors.toList());
        sb.append("前区遗漏 Top10: [");
        for (int i = 0; i < sortedFrontMissing.size(); i++) {
            MissingDTO m = sortedFrontMissing.get(i);
            if (i > 0) sb.append(", ");
            sb.append(m.getNumber()).append(":").append(m.getCurrentMissing());
        }
        sb.append("]\n");
        
        // 后区遗漏 Top10 - 排序
        List<MissingDTO> sortedBackMissing = backMissing.stream()
            .sorted(Comparator.comparing(MissingDTO::getCurrentMissing).reversed())
            .limit(10)
            .collect(Collectors.toList());
        sb.append("后区遗漏 Top10: [");
        for (int i = 0; i < sortedBackMissing.size(); i++) {
            MissingDTO m = sortedBackMissing.get(i);
            if (i > 0) sb.append(", ");
            sb.append(m.getNumber()).append(":").append(m.getCurrentMissing());
        }
        sb.append("]\n");
        
        // 遗漏到期号码
        sb.append("前区遗漏到期: ").append(getMissingDueFrontNumbers(10)).append("\n");
        sb.append("后区遗漏到期: ").append(getMissingDueBackNumbers(6)).append("\n");
        
        // 统计特征
        sb.append("\n=== 统计特征 ===\n");
        sb.append("奇偶比: ").append(getOddEvenStats()).append("\n");
        sb.append("和值区间: ").append(getFrontSumStats()).append("\n");
        sb.append("连号统计: ").append(getConsecutiveStats()).append("\n");
        
        // 尾数分析
        sb.append("\n=== 尾数分析 ===\n");
        sb.append("前区尾数频率: ").append(getFrontDigitFrequency()).append("\n");
        sb.append("后区尾数频率: ").append(getBackDigitFrequency()).append("\n");
        sb.append("尾数和值: ").append(getDigitSumStats()).append("\n");
        
        // 区间分布
        sb.append("\n=== 区间分布 ===\n");
        sb.append("区间分布: ").append(getZoneDistribution()).append("\n");
        
        return sb.toString();
    }
}
