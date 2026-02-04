package com.hobart.lottery.service.analysis;

import com.hobart.lottery.config.LotteryConfig;
import com.hobart.lottery.entity.LotteryResult;
import com.hobart.lottery.service.LotteryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 走势分析服务
 * 提供走势图数据
 */
@Service
@RequiredArgsConstructor
public class TrendAnalyzer {
    
    private final LotteryService lotteryService;
    private final LotteryConfig config;
    
    /**
     * 获取走势数据
     * 
     * @param limit 期数
     * @return 走势数据列表（按时间正序）
     */
    public List<Map<String, Object>> getTrendData(int limit) {
        List<LotteryResult> results = lotteryService.getRecentResults(limit);
        Collections.reverse(results); // 按时间正序
        
        List<Map<String, Object>> trendData = new ArrayList<>();
        for (LotteryResult result : results) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("issue", result.getIssue());
            item.put("drawDate", result.getDrawDate().toString());
            item.put("frontBalls", result.getFrontBallArray());
            item.put("backBalls", result.getBackBallArray());
            item.put("frontSum", result.getFrontSum());
            item.put("backSum", result.getBackSum());
            item.put("oddCountFront", result.getOddCountFront());
            item.put("consecutiveCountFront", result.getConsecutiveCountFront());
            trendData.add(item);
        }
        
        return trendData;
    }
    
    /**
     * 获取默认期数的走势数据
     */
    public List<Map<String, Object>> getTrendData() {
        return getTrendData(config.getAnalysis().getTrendDefaultLimit());
    }
    
    /**
     * 获取号码走势（指定号码在最近若干期的出现情况）
     * 
     * @param number 号码
     * @param isFront 是否前区
     * @param limit 期数
     * @return 出现情况列表（true=出现，false=未出现）
     */
    public List<Boolean> getNumberTrend(int number, boolean isFront, int limit) {
        List<LotteryResult> results = lotteryService.getRecentResults(limit);
        List<Boolean> trend = new ArrayList<>();
        
        for (LotteryResult result : results) {
            int[] balls = isFront ? result.getFrontBallArray() : result.getBackBallArray();
            boolean found = false;
            for (int ball : balls) {
                if (ball == number) {
                    found = true;
                    break;
                }
            }
            trend.add(found);
        }
        
        Collections.reverse(trend); // 按时间正序
        return trend;
    }
    
    /**
     * 获取和值走势
     * 
     * @param isFront 是否前区
     * @param limit 期数
     * @return 和值列表（按时间正序）
     */
    public List<Integer> getSumTrend(boolean isFront, int limit) {
        List<LotteryResult> results = lotteryService.getRecentResults(limit);
        List<Integer> sums = new ArrayList<>();
        
        for (LotteryResult result : results) {
            sums.add(isFront ? result.getFrontSum() : result.getBackSum());
        }
        
        Collections.reverse(sums);
        return sums;
    }
}
