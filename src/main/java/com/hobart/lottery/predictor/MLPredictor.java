package com.hobart.lottery.predictor;

import com.hobart.lottery.domain.model.NumberZone;
import com.hobart.lottery.entity.LotteryResult;
import com.hobart.lottery.service.AnalysisService;
import com.hobart.lottery.service.LotteryService;

import java.util.*;

/**
 * 机器学习预测器
 * 基于历史数据特征进行简单的模式识别预测
 */
public class MLPredictor extends BasePredictor {

    private final LotteryService lotteryService;

    public MLPredictor(AnalysisService analysisService, LotteryService lotteryService) {
        super(analysisService);
        this.lotteryService = lotteryService;
    }

    @Override
    public String getMethodName() {
        return "机器学习";
    }

    @Override
    public String getMethodCode() {
        return "ML";
    }

    @Override
    public int[][] predict() {
        List<LotteryResult> results = lotteryService.getRecentResults(100);
        
        // 基于历史模式的特征分析
        Map<Integer, Double> frontScores = calculateMLScores(results, NumberZone.FRONT);
        Map<Integer, Double> backScores = calculateMLScores(results, NumberZone.BACK);
        
        // 基于权重随机选择
        int[] front = selectByWeight(frontScores, 5, 
            NumberZone.FRONT.getMin(), NumberZone.FRONT.getMax());
        int[] back = selectByWeight(backScores, 2, 
            NumberZone.BACK.getMin(), NumberZone.BACK.getMax());
        
        return new int[][]{front, back};
    }

    /**
     * 计算ML评分
     * 综合考虑：频率、遗漏、趋势、相邻号码关联性
     */
    private Map<Integer, Double> calculateMLScores(List<LotteryResult> results, NumberZone zone) {
        int min = zone.getMin();
        int max = zone.getMax();
        
        Map<Integer, Double> scores = new HashMap<>();
        
        // 初始化
        for (int i = min; i <= max; i++) {
            scores.put(i, 50.0);
        }
        
        if (results.isEmpty()) return scores;
        
        // 1. 频率特征 (近30期)
        Map<Integer, Integer> freqCount = new HashMap<>();
        int freqPeriod = Math.min(30, results.size());
        for (int i = 0; i < freqPeriod; i++) {
            int[] balls = zone.getBalls(results.get(i));
            for (int ball : balls) {
                freqCount.merge(ball, 1, Integer::sum);
            }
        }
        
        // 2. 遗漏特征
        Map<Integer, Integer> missingMap = new HashMap<>();
        for (int num = min; num <= max; num++) {
            final int currentNum = num;
            for (int i = 0; i < results.size(); i++) {
                int[] balls = zone.getBalls(results.get(i));
                boolean found = Arrays.stream(balls).anyMatch(b -> b == currentNum);
                if (found) {
                    missingMap.put(num, i);
                    break;
                }
            }
            missingMap.putIfAbsent(num, results.size());
        }
        
        // 3. 趋势特征 (最近5期的走势)
        Map<Integer, Double> trend = new HashMap<>();
        for (int num = min; num <= max; num++) {
            final int currentNum = num;
            int count = 0;
            int recentPeriod = Math.min(5, results.size());
            for (int i = 0; i < recentPeriod; i++) {
                int[] balls = zone.getBalls(results.get(i));
                if (Arrays.stream(balls).anyMatch(b -> b == currentNum)) {
                    count++;
                }
            }
            trend.put(num, count * 1.0 / recentPeriod);
        }
        
        // 4. 相邻号码关联性 (最近一期的号码±3范围有加成)
        Set<Integer> recentNeighbors = new HashSet<>();
        if (!results.isEmpty()) {
            int[] lastBalls = zone.getBalls(results.get(0));
            for (int ball : lastBalls) {
                for (int delta = -3; delta <= 3; delta++) {
                    int neighbor = ball + delta;
                    if (neighbor >= min && neighbor <= max) {
                        recentNeighbors.add(neighbor);
                    }
                }
            }
        }
        
        // 综合计算评分
        double avgMiss = 30.0 / zone.getCount(); // 理论平均遗漏
        for (int num = min; num <= max; num++) {
            double score = 50.0;
            
            // 频率分数 (出现次数越多分越高)
            int freq = freqCount.getOrDefault(num, 0);
            score += freq * 3;
            
            // 遗漏分数 (遗漏适中得高分)
            int miss = missingMap.getOrDefault(num, 0);
            if (miss >= avgMiss * 0.8 && miss <= avgMiss * 1.5) {
                score += 20;
            } else if (miss > avgMiss * 1.5) {
                score += 15; // 遗漏过大也有一定加分
            }
            
            // 趋势分数
            double trendScore = trend.getOrDefault(num, 0.0);
            score += trendScore * 15;
            
            // 相邻号码加成
            if (recentNeighbors.contains(num)) {
                score += 10;
            }
            
            // 添加随机扰动
            score += random.nextDouble() * 10;
            
            scores.put(num, Math.max(score, 1));
        }
        
        return scores;
    }
}
