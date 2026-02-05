package com.hobart.lottery.service.analysis;

import com.hobart.lottery.config.LotteryConfig;
import com.hobart.lottery.domain.model.NumberZone;
import com.hobart.lottery.dto.FrequencyDTO;
import com.hobart.lottery.dto.MissingDTO;
import com.hobart.lottery.entity.LotteryResult;
import com.hobart.lottery.service.LotteryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 频率分析服务
 * 统计号码出现频率，支持热号、冷号分析
 */
@Service
@RequiredArgsConstructor
public class FrequencyAnalyzer {
    
    private final LotteryService lotteryService;
    private final LotteryConfig config;
    
    /**
     * 计算指定区域的号码频率
     * 
     * @param zone 号码区域（前区/后区）
     * @param recentCount 最近期数，null 表示全部
     * @return 频率列表
     */
    public List<FrequencyDTO> calculateFrequency(NumberZone zone, Integer recentCount) {
        List<LotteryResult> results = recentCount != null 
            ? lotteryService.getRecentResults(recentCount) 
            : lotteryService.getAllResults();
        
        // 初始化计数 Map
        Map<Integer, Integer> countMap = new LinkedHashMap<>();
        for (int i = zone.getMin(); i <= zone.getMax(); i++) {
            countMap.put(i, 0);
        }
        
        // 统计频率
        for (LotteryResult result : results) {
            int[] balls = zone.getBalls(result);
            for (int ball : balls) {
                countMap.merge(ball, 1, Integer::sum);
            }
        }
        
        // 转换为 DTO
        int totalCount = results.size();
        return countMap.entrySet().stream()
            .map(e -> new FrequencyDTO(
                e.getKey(),
                e.getValue(),
                totalCount > 0 ? (e.getValue() * 100.0 / totalCount) : 0,
                zone.getCode()
            ))
            .sorted(Comparator.comparing(FrequencyDTO::getNumber))
            .collect(Collectors.toList());
    }
    
    /**
     * 获取热号（出现频率最高的号码）
     * 
     * @param zone 号码区域
     * @param count 获取数量
     * @return 热号列表（已排序）
     */
    public List<Integer> getHotNumbers(NumberZone zone, int count) {
        int period = config.getAnalysis().getHotColdPeriod();
        List<FrequencyDTO> frequencies = calculateFrequency(zone, period);
        
        return frequencies.stream()
            .sorted(Comparator.comparing(FrequencyDTO::getCount).reversed())
            .limit(count)
            .map(FrequencyDTO::getNumber)
            .sorted()
            .collect(Collectors.toList());
    }
    
    /**
     * 获取冷号（出现频率最低的号码）
     * 
     * @param zone 号码区域
     * @param count 获取数量
     * @return 冷号列表（已排序）
     */
    public List<Integer> getColdNumbers(NumberZone zone, int count) {
        int period = config.getAnalysis().getHotColdPeriod();
        List<FrequencyDTO> frequencies = calculateFrequency(zone, period);
        
        return frequencies.stream()
            .sorted(Comparator.comparing(FrequencyDTO::getCount))
            .limit(count)
            .map(FrequencyDTO::getNumber)
            .sorted()
            .collect(Collectors.toList());
    }
    
    /**
     * 获取号码综合评分（用于预测）
     * 结合频率和遗漏计算综合得分
     * 
     * @param zone 号码区域
     * @param missingAnalyzer 遗漏分析器（用于获取遗漏数据）
     * @return 号码 -> 评分 映射
     */
    public Map<Integer, Double> getNumberScores(NumberZone zone, MissingAnalyzer missingAnalyzer) {
        int period = config.getAnalysis().getHotColdPeriod();
        List<FrequencyDTO> frequencies = calculateFrequency(zone, period);
        List<MissingDTO> missings = missingAnalyzer.calculateMissing(zone);
        
        Map<Integer, Double> scores = new HashMap<>();
        
        // 频率得分归一化
        double maxFreq = frequencies.stream().mapToDouble(FrequencyDTO::getFrequency).max().orElse(1);
        double minFreq = frequencies.stream().mapToDouble(FrequencyDTO::getFrequency).min().orElse(0);
        
        for (int num = zone.getMin(); num <= zone.getMax(); num++) {
            final int n = num;
            FrequencyDTO freq = frequencies.stream().filter(f -> f.getNumber() == n).findFirst().orElse(null);
            MissingDTO miss = missings.stream().filter(m -> m.getNumber() == n).findFirst().orElse(null);
            
            double freqScore = 0, missScore = 0;
            
            if (freq != null) {
                freqScore = (freq.getFrequency() - minFreq) / (maxFreq - minFreq + 0.001) * 100;
            }
            
            if (miss != null && miss.getAvgMissing() > 0) {
                // 遗漏接近平均值得分高
                double ratio = miss.getCurrentMissing() / miss.getAvgMissing();
                if (ratio >= 0.8 && ratio <= 1.5) {
                    missScore = 100 - Math.abs(ratio - 1) * 50;
                } else if (ratio > 1.5) {
                    missScore = 80; // 遗漏过大也给较高分
                }
            }
            
            // 综合评分
            scores.put(num, freqScore * 0.5 + missScore * 0.5);
        }
        
        return scores;
    }
}
