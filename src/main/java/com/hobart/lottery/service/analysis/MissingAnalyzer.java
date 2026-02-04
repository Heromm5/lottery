package com.hobart.lottery.service.analysis;

import com.hobart.lottery.config.LotteryConfig;
import com.hobart.lottery.domain.model.NumberZone;
import com.hobart.lottery.dto.MissingDTO;
import com.hobart.lottery.entity.LotteryResult;
import com.hobart.lottery.service.LotteryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * 遗漏分析服务
 * 计算号码的当前遗漏、平均遗漏、最大遗漏
 */
@Service
@RequiredArgsConstructor
public class MissingAnalyzer {
    
    private final LotteryService lotteryService;
    private final LotteryConfig config;
    
    /**
     * 计算指定区域的号码遗漏
     * 
     * @param zone 号码区域（前区/后区）
     * @return 遗漏列表
     */
    public List<MissingDTO> calculateMissing(NumberZone zone) {
        int period = config.getAnalysis().getMissingPeriod();
        List<LotteryResult> results = lotteryService.getRecentResults(period);
        // 结果按时间倒序，最新的在前
        
        List<MissingDTO> missingList = new ArrayList<>();
        
        for (int num = zone.getMin(); num <= zone.getMax(); num++) {
            final int currentNum = num;
            int currentMissing = 0;
            List<Integer> missingIntervals = new ArrayList<>();
            int lastAppearIndex = -1;
            
            for (int i = 0; i < results.size(); i++) {
                int[] balls = zone.getBalls(results.get(i));
                boolean found = IntStream.of(balls).anyMatch(b -> b == currentNum);
                
                if (found) {
                    if (lastAppearIndex == -1) {
                        currentMissing = i;
                    }
                    if (lastAppearIndex >= 0) {
                        missingIntervals.add(i - lastAppearIndex - 1);
                    }
                    lastAppearIndex = i;
                }
            }
            
            // 如果从未出现过
            if (lastAppearIndex == -1) {
                currentMissing = results.size();
            }
            
            double avgMissing = missingIntervals.isEmpty() ? 0 : 
                missingIntervals.stream().mapToInt(Integer::intValue).average().orElse(0);
            int maxMissing = missingIntervals.isEmpty() ? currentMissing : 
                Math.max(currentMissing, missingIntervals.stream().mapToInt(Integer::intValue).max().orElse(0));
            
            missingList.add(new MissingDTO(num, currentMissing, avgMissing, maxMissing, zone.getCode()));
        }
        
        return missingList;
    }
    
    /**
     * 获取遗漏值接近平均遗漏的号码（即将出现的号码）
     * 
     * @param zone 号码区域
     * @param count 获取数量
     * @return 号码列表（已排序）
     */
    public List<Integer> getMissingDueNumbers(NumberZone zone, int count) {
        List<MissingDTO> missings = calculateMissing(zone);
        
        return missings.stream()
            .filter(m -> m.getCurrentMissing() >= m.getAvgMissing() * 0.8)
            .sorted(Comparator.comparing(m -> Math.abs(m.getCurrentMissing() - m.getAvgMissing())))
            .limit(count)
            .map(MissingDTO::getNumber)
            .sorted()
            .collect(Collectors.toList());
    }
    
    /**
     * 获取高遗漏号码（遗漏值最大的号码）
     * 
     * @param zone 号码区域
     * @param count 获取数量
     * @return 号码列表
     */
    public List<Integer> getHighMissingNumbers(NumberZone zone, int count) {
        List<MissingDTO> missings = calculateMissing(zone);
        
        return missings.stream()
            .sorted(Comparator.comparing(MissingDTO::getCurrentMissing).reversed())
            .limit(count)
            .map(MissingDTO::getNumber)
            .sorted()
            .collect(Collectors.toList());
    }
}
