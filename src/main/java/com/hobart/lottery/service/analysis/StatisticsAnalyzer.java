package com.hobart.lottery.service.analysis;

import com.hobart.lottery.dto.SameNumberDTO;
import com.hobart.lottery.entity.LotteryResult;
import com.hobart.lottery.service.LotteryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 统计分析服务
 * 提供奇偶比、和值分布、连号统计等
 */
@Service
@RequiredArgsConstructor
public class StatisticsAnalyzer {
    
    private final LotteryService lotteryService;
    
    /**
     * 获取奇偶比统计（前区）
     * 
     * @return 奇偶比 -> 次数
     */
    public Map<String, Integer> getOddEvenStats() {
        List<LotteryResult> results = lotteryService.getAllResults();
        Map<String, Integer> stats = new LinkedHashMap<>();
        
        // 初始化所有可能的奇偶比
        for (int odd = 0; odd <= 5; odd++) {
            String key = odd + ":" + (5 - odd);
            stats.put(key, 0);
        }
        
        for (LotteryResult result : results) {
            int oddCount = result.getOddCountFront();
            String key = oddCount + ":" + (5 - oddCount);
            stats.merge(key, 1, Integer::sum);
        }
        
        return stats;
    }
    
    /**
     * 获取和值分布统计（前区）
     * 
     * @return 和值区间 -> 次数
     */
    public Map<String, Integer> getFrontSumStats() {
        List<LotteryResult> results = lotteryService.getAllResults();
        Map<String, Integer> stats = new LinkedHashMap<>();
        
        // 按区间统计
        String[] ranges = {"30-60", "61-90", "91-120", "121-150", "151+"};
        for (String range : ranges) {
            stats.put(range, 0);
        }
        
        for (LotteryResult result : results) {
            int sum = result.getFrontSum();
            if (sum <= 60) stats.merge("30-60", 1, Integer::sum);
            else if (sum <= 90) stats.merge("61-90", 1, Integer::sum);
            else if (sum <= 120) stats.merge("91-120", 1, Integer::sum);
            else if (sum <= 150) stats.merge("121-150", 1, Integer::sum);
            else stats.merge("151+", 1, Integer::sum);
        }
        
        return stats;
    }
    
    /**
     * 获取连号统计（前区）
     * 
     * @return 连号数 -> 次数
     */
    public Map<Integer, Integer> getConsecutiveStats() {
        List<LotteryResult> results = lotteryService.getAllResults();
        Map<Integer, Integer> stats = new LinkedHashMap<>();
        
        // 初始化（0-4个连号）
        for (int i = 0; i <= 4; i++) {
            stats.put(i, 0);
        }
        
        for (LotteryResult result : results) {
            int count = result.getConsecutiveCountFront();
            stats.merge(count, 1, Integer::sum);
        }
        
        return stats;
    }
    
    /**
     * 查找历史中奖号码完全一致的情况
     * 统计前区5个号码 + 后区2个号码完全相同的组合
     * 
     * @return 相同号码列表
     */
    public List<SameNumberDTO> findSameNumbers() {
        List<LotteryResult> results = lotteryService.getAllResults();
        
        // 使用 Map 按号码组合分组
        Map<String, List<LotteryResult>> groupedResults = new LinkedHashMap<>();
        
        for (LotteryResult result : results) {
            String key = result.getFrontBalls() + "|" + result.getBackBalls();
            groupedResults.computeIfAbsent(key, k -> new ArrayList<>()).add(result);
        }
        
        // 筛选出出现次数 >= 2 的组合
        List<SameNumberDTO> sameNumberList = new ArrayList<>();
        
        for (Map.Entry<String, List<LotteryResult>> entry : groupedResults.entrySet()) {
            List<LotteryResult> group = entry.getValue();
            if (group.size() >= 2) {
                String[] parts = entry.getKey().split("\\|");
                String frontBalls = parts[0];
                String backBalls = parts.length > 1 ? parts[1] : "";
                
                List<String> issues = group.stream()
                    .map(LotteryResult::getIssue)
                    .collect(Collectors.toList());
                
                List<LocalDate> dates = group.stream()
                    .map(LotteryResult::getDrawDate)
                    .collect(Collectors.toList());
                
                sameNumberList.add(new SameNumberDTO(
                    frontBalls,
                    backBalls,
                    group.size(),
                    issues,
                    dates
                ));
            }
        }
        
        // 按出现次数降序排列
        sameNumberList.sort(Comparator.comparing(SameNumberDTO::getCount).reversed());
        
        return sameNumberList;
    }
    
    /**
     * 获取区间分布统计（前区按 1-7, 8-14, 15-21, 22-28, 29-35 分区）
     * 
     * @return 区间分布
     */
    public Map<String, Map<String, Integer>> getZoneDistribution() {
        List<LotteryResult> results = lotteryService.getAllResults();
        
        String[] zones = {"1-7", "8-14", "15-21", "22-28", "29-35"};
        Map<String, Map<String, Integer>> distribution = new LinkedHashMap<>();
        
        // 初始化：统计每个区间出现 0-5 个号码的次数
        for (String zone : zones) {
            Map<String, Integer> counts = new LinkedHashMap<>();
            for (int i = 0; i <= 5; i++) {
                counts.put(String.valueOf(i), 0);
            }
            distribution.put(zone, counts);
        }
        
        for (LotteryResult result : results) {
            int[] balls = result.getFrontBallArray();
            int[] zoneCounts = new int[5]; // 5 个区间
            
            for (int ball : balls) {
                int zoneIndex = (ball - 1) / 7;
                if (zoneIndex > 4) zoneIndex = 4;
                zoneCounts[zoneIndex]++;
            }
            
            for (int i = 0; i < zones.length; i++) {
                String countKey = String.valueOf(zoneCounts[i]);
                distribution.get(zones[i]).merge(countKey, 1, Integer::sum);
            }
        }
        
        return distribution;
    }
}
