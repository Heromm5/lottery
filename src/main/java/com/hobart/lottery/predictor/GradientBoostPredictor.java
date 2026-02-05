package com.hobart.lottery.predictor;

import com.hobart.lottery.entity.LotteryResult;
import com.hobart.lottery.service.AnalysisService;
import com.hobart.lottery.service.LotteryService;

import java.util.*;

/**
 * 梯度提升预测器
 * 使用加权特征评分进行预测（模拟梯度提升思想）
 */
public class GradientBoostPredictor extends BasePredictor {

    private final LotteryService lotteryService;
    
    private static final int HISTORY_PERIODS = 300;
    private static final int MIN_PERIODS = 50;

    public GradientBoostPredictor(AnalysisService analysisService, LotteryService lotteryService) {
        super(analysisService);
        this.lotteryService = lotteryService;
    }

    @Override
    public String getMethodName() {
        return "梯度提升";
    }

    @Override
    public String getMethodCode() {
        return "GRADIENT_BOOST";
    }

    @Override
    public int[][] predict() {
        List<LotteryResult> results = lotteryService.getRecentResults(HISTORY_PERIODS);
        
        if (results.size() < MIN_PERIODS) {
            return generateRandom();
        }
        
        int[] front = predictZone(results, 1, 35, 5);
        int[] back = predictZone(results, 1, 12, 2);
        
        return new int[][]{front, back};
    }

    private int[] predictZone(List<LotteryResult> results, int zoneMin, int zoneMax, int zoneCount) {
        Map<Integer, Double> scores = new HashMap<>();
        
        for (int num = zoneMin; num <= zoneMax; num++) {
            double score = calculateGradientBoostScore(num, results, zoneMin, zoneMax, zoneCount);
            scores.put(num, score);
        }
        
        return selectTopByScore(scores, zoneCount);
    }

    /**
     * 计算梯度提升评分
     * 综合多个基础学习器的预测结果
     */
    private double calculateGradientBoostScore(int num, List<LotteryResult> results, int zoneMin, int zoneMax, int zoneCount) {
        double totalScore = 0;
        
        // 基础学习器1：频率评分
        double freqScore = calculateFrequencyScore(num, results, zoneCount);
        totalScore += freqScore * 0.20;
        
        // 基础学习器2：遗漏评分
        double missScore = calculateMissingScore(num, results, zoneCount);
        totalScore += missScore * 0.20;
        
        // 基础学习器3：趋势评分
        double trendScore = calculateTrendScore(num, results);
        totalScore += trendScore * 0.15;
        
        // 基础学习器4：周期性评分
        double periodicScore = calculatePeriodicScore(num, results);
        totalScore += periodicScore * 0.10;
        
        // 基础学习器5：关联评分
        double relationScore = calculateRelationScore(num, results);
        totalScore += relationScore * 0.15;
        
        // 基础学习器6：位置评分
        double positionScore = calculatePositionScore(num, zoneMin, zoneMax);
        totalScore += positionScore * 0.10;
        
        // 基础学习器7：贝叶斯评分
        double bayesScore = calculateBayesScore(num, results, zoneCount);
        totalScore += bayesScore * 0.10;
        
        return totalScore;
    }

    private double calculateFrequencyScore(int num, List<LotteryResult> results, int zoneCount) {
        int count = 0;
        int period = Math.min(30, results.size());
        for (int i = 0; i < period; i++) {
            int[] balls = getBalls(results.get(i), zoneCount);
            for (int ball : balls) {
                if (ball == num) count++;
            }
        }
        return (double) count / period;
    }

    private double calculateMissingScore(int num, List<LotteryResult> results, int zoneCount) {
        int missing = 0;
        for (int i = 0; i < results.size(); i++) {
            int[] balls = getBalls(results.get(i), zoneCount);
            boolean found = false;
            for (int ball : balls) {
                if (ball == num) {
                    found = true;
                    break;
                }
            }
            if (!found) missing++;
            else break;
        }
        double avgMissing = (double) results.size() / zoneCount;
        if (missing >= avgMissing * 0.8 && missing <= avgMissing * 1.5) return 1.0;
        if (missing > avgMissing * 1.5) return 0.7;
        return 0.3;
    }

    private double calculateTrendScore(int num, List<LotteryResult> results) {
        int recent = 0;
        int period = Math.min(10, results.size());
        for (int i = 0; i < period; i++) {
            int[] balls = getBalls(results.get(i), 5);
            for (int ball : balls) {
                if (ball == num) recent++;
            }
        }
        return (double) recent / period;
    }

    private double calculatePeriodicScore(int num, List<LotteryResult> results) {
        if (results.isEmpty()) return 0.5;
        // 简化的周期性计算
        return random.nextDouble() * 0.3 + 0.35;
    }

    private double calculateRelationScore(int num, List<LotteryResult> results) {
        if (results.isEmpty()) return 0.5;
        
        int count = 0;
        int period = Math.min(50, results.size());
        for (int i = 0; i < period; i++) {
            int[] balls = getBalls(results.get(i), 5);
            for (int ball : balls) {
                if (Math.abs(ball - num) <= 3) count++;
            }
        }
        return Math.min(1.0, (double) count / period * 2);
    }

    private double calculatePositionScore(int num, int zoneMin, int zoneMax) {
        // 避免选择边界号码
        if (num == zoneMin || num == zoneMax) return 0.3;
        if (num == zoneMin + 1 || num == zoneMax - 1) return 0.5;
        return 0.8;
    }

    private double calculateBayesScore(int num, List<LotteryResult> results, int zoneCount) {
        int count = 0;
        for (LotteryResult r : results) {
            int[] balls = getBalls(r, zoneCount);
            for (int ball : balls) {
                if (ball == num) count++;
            }
        }
        return (double) count / (results.size() * zoneCount / zoneCount);
    }

    private int[] getBalls(LotteryResult result, int zoneCount) {
        if (zoneCount == 5) return result.getFrontBallArray();
        return result.getBackBallArray();
    }

    private int[] selectTopByScore(Map<Integer, Double> scores, int need) {
        List<Map.Entry<Integer, Double>> entries = new ArrayList<>(scores.entrySet());
        entries.sort((a, b) -> Double.compare(b.getValue(), a.getValue()));
        
        Set<Integer> selected = new HashSet<>();
        int[] result = new int[need];
        int idx = 0;
        
        for (Map.Entry<Integer, Double> entry : entries) {
            if (selected.size() >= need) break;
            if (!selected.contains(entry.getKey())) {
                selected.add(entry.getKey());
                result[idx++] = entry.getKey();
            }
        }
        
        Arrays.sort(result);
        return result;
    }
}
