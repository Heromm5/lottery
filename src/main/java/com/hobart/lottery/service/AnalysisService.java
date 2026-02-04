package com.hobart.lottery.service;

import com.hobart.lottery.dto.FrequencyDTO;
import com.hobart.lottery.dto.MissingDTO;
import com.hobart.lottery.dto.SameNumberDTO;
import com.hobart.lottery.service.analysis.AnalysisFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * 数据分析服务（重构后）
 * 
 * 委托给 AnalysisFacade，保持向后兼容
 * 新代码建议直接使用 AnalysisFacade 或各个专门的 Analyzer
 */
@Service
@RequiredArgsConstructor
public class AnalysisService {

    private final AnalysisFacade analysisFacade;

    // 前区号码范围 1-35（保留常量以保持兼容）
    public static final int FRONT_MIN = 1;
    public static final int FRONT_MAX = 35;
    // 后区号码范围 1-12
    public static final int BACK_MIN = 1;
    public static final int BACK_MAX = 12;

    /**
     * 计算前区号码频率
     */
    public List<FrequencyDTO> calculateFrontFrequency() {
        return analysisFacade.calculateFrontFrequency();
    }

    /**
     * 计算前区号码频率（指定期数）
     */
    public List<FrequencyDTO> calculateFrontFrequency(Integer recentCount) {
        return analysisFacade.calculateFrontFrequency(recentCount);
    }

    /**
     * 计算后区号码频率
     */
    public List<FrequencyDTO> calculateBackFrequency() {
        return analysisFacade.calculateBackFrequency();
    }

    /**
     * 计算后区号码频率（指定期数）
     */
    public List<FrequencyDTO> calculateBackFrequency(Integer recentCount) {
        return analysisFacade.calculateBackFrequency(recentCount);
    }

    /**
     * 计算前区号码遗漏
     */
    public List<MissingDTO> calculateFrontMissing() {
        return analysisFacade.calculateFrontMissing();
    }

    /**
     * 计算后区号码遗漏
     */
    public List<MissingDTO> calculateBackMissing() {
        return analysisFacade.calculateBackMissing();
    }

    /**
     * 获取热号（近30期出现频率最高的号码）
     */
    public List<Integer> getHotFrontNumbers(int count) {
        return analysisFacade.getHotFrontNumbers(count);
    }

    /**
     * 获取后区热号
     */
    public List<Integer> getHotBackNumbers(int count) {
        return analysisFacade.getHotBackNumbers(count);
    }

    /**
     * 获取冷号（近30期出现频率最低的号码）
     */
    public List<Integer> getColdFrontNumbers(int count) {
        return analysisFacade.getColdFrontNumbers(count);
    }

    /**
     * 获取后区冷号
     */
    public List<Integer> getColdBackNumbers(int count) {
        return analysisFacade.getColdBackNumbers(count);
    }

    /**
     * 获取遗漏值接近平均遗漏的号码（前区）
     */
    public List<Integer> getMissingDueFrontNumbers(int count) {
        return analysisFacade.getMissingDueFrontNumbers(count);
    }

    /**
     * 获取遗漏值接近平均遗漏的号码（后区）
     */
    public List<Integer> getMissingDueBackNumbers(int count) {
        return analysisFacade.getMissingDueBackNumbers(count);
    }

    /**
     * 获取奇偶比统计
     */
    public Map<String, Integer> getOddEvenStats() {
        return analysisFacade.getOddEvenStats();
    }

    /**
     * 获取和值分布统计（前区）
     */
    public Map<String, Integer> getFrontSumStats() {
        return analysisFacade.getFrontSumStats();
    }

    /**
     * 获取连号统计（前区）
     */
    public Map<Integer, Integer> getConsecutiveStats() {
        return analysisFacade.getConsecutiveStats();
    }

    /**
     * 获取走势数据
     */
    public List<Map<String, Object>> getTrendData(int limit) {
        return analysisFacade.getTrendData(limit);
    }

    /**
     * 获取号码综合评分（用于预测）
     */
    public Map<Integer, Double> getFrontNumberScores() {
        return analysisFacade.getFrontNumberScores();
    }

    /**
     * 获取后区号码综合评分
     */
    public Map<Integer, Double> getBackNumberScores() {
        return analysisFacade.getBackNumberScores();
    }

    /**
     * 查找历史中奖号码完全一致的情况
     */
    public List<SameNumberDTO> findSameNumbers() {
        return analysisFacade.findSameNumbers();
    }
}
