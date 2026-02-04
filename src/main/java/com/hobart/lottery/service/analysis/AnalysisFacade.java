package com.hobart.lottery.service.analysis;

import com.hobart.lottery.domain.model.NumberZone;
import com.hobart.lottery.dto.FrequencyDTO;
import com.hobart.lottery.dto.MissingDTO;
import com.hobart.lottery.dto.SameNumberDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

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
}
