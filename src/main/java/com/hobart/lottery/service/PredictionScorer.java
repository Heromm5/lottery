package com.hobart.lottery.service;

import com.hobart.lottery.entity.LotteryResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 预测评分器
 * 基于历史命中相似度为预测号码评分，选出最可能的一注
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PredictionScorer {

    private final LotteryService lotteryService;

    /**
     * 从多注预测中选择最优的一注
     * 基于与历史高命中模式的相似度评分
     *
     * @param predictions 预测列表，每个元素为 int[2][]，[0]前区 [1]后区
     * @return 最优预测的索引
     */
    public int selectBestPrediction(List<int[][]> predictions) {
        if (predictions == null || predictions.isEmpty()) {
            return 0;
        }
        if (predictions.size() == 1) {
            return 0;
        }

        // 获取历史数据用于分析
        List<LotteryResult> history = lotteryService.getRecentResults(100);
        if (history.isEmpty()) {
            return 0;
        }

        // 计算历史模式特征
        HistoryPattern pattern = analyzeHistoryPattern(history);

        // 为每注预测评分
        double maxScore = Double.MIN_VALUE;
        int bestIndex = 0;

        for (int i = 0; i < predictions.size(); i++) {
            double score = scorePrediction(predictions.get(i), pattern, history);
            log.debug("预测 {} 评分: {}", i + 1, score);
            if (score > maxScore) {
                maxScore = score;
                bestIndex = i;
            }
        }

        log.info("选择第 {} 注作为最优预测，评分: {}", bestIndex + 1, maxScore);
        return bestIndex;
    }

    /**
     * 分析历史模式特征
     */
    private HistoryPattern analyzeHistoryPattern(List<LotteryResult> history) {
        HistoryPattern pattern = new HistoryPattern();

        // 统计前区号码出现频率
        Map<Integer, Integer> frontFreq = new HashMap<>();
        Map<Integer, Integer> backFreq = new HashMap<>();

        // 统计和值分布
        Map<Integer, Integer> sumDist = new HashMap<>();

        // 统计奇偶比分布
        Map<String, Integer> oddEvenDist = new HashMap<>();

        // 统计连号出现频率
        int consecutiveCount = 0;

        for (LotteryResult result : history) {
            int[] front = result.getFrontBallArray();
            int[] back = result.getBackBallArray();

            // 前区频率
            for (int num : front) {
                frontFreq.merge(num, 1, Integer::sum);
            }

            // 后区频率
            for (int num : back) {
                backFreq.merge(num, 1, Integer::sum);
            }

            // 和值
            int sum = Arrays.stream(front).sum();
            int sumRange = sum / 20; // 分成几个区间
            sumDist.merge(sumRange, 1, Integer::sum);

            // 奇偶比
            int oddCount = (int) Arrays.stream(front).filter(n -> n % 2 == 1).count();
            String oddEvenKey = oddCount + ":" + (5 - oddCount);
            oddEvenDist.merge(oddEvenKey, 1, Integer::sum);

            // 连号
            if (hasConsecutive(front)) {
                consecutiveCount++;
            }
        }

        pattern.frontFrequency = frontFreq;
        pattern.backFrequency = backFreq;
        pattern.sumDistribution = sumDist;
        pattern.oddEvenDistribution = oddEvenDist;
        pattern.consecutiveRate = (double) consecutiveCount / history.size();

        // 计算最常见的奇偶比
        pattern.mostCommonOddEven = oddEvenDist.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("3:2");

        // 计算最常见的和值区间
        pattern.mostCommonSumRange = sumDist.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(4); // 80-100

        return pattern;
    }

    /**
     * 为单注预测评分
     */
    private double scorePrediction(int[][] prediction, HistoryPattern pattern, List<LotteryResult> history) {
        int[] front = prediction[0];
        int[] back = prediction[1];

        double score = 0;

        // 1. 号码热度得分（与历史频率的匹配度）- 权重 30%
        double hotScore = calculateHotScore(front, back, pattern);
        score += hotScore * 0.30;

        // 2. 和值合理性得分 - 权重 20%
        double sumScore = calculateSumScore(front, pattern);
        score += sumScore * 0.20;

        // 3. 奇偶比合理性得分 - 权重 20%
        double oddEvenScore = calculateOddEvenScore(front, pattern);
        score += oddEvenScore * 0.20;

        // 4. 号码分布均匀度得分 - 权重 15%
        double distributionScore = calculateDistributionScore(front);
        score += distributionScore * 0.15;

        // 5. 与近期号码的关联性得分 - 权重 15%
        double correlationScore = calculateCorrelationScore(front, back, history);
        score += correlationScore * 0.15;

        return score;
    }

    /**
     * 计算号码热度得分
     */
    private double calculateHotScore(int[] front, int[] back, HistoryPattern pattern) {
        double frontScore = 0;
        for (int num : front) {
            frontScore += pattern.frontFrequency.getOrDefault(num, 0);
        }
        // 归一化到 0-100
        double maxFrontFreq = pattern.frontFrequency.values().stream()
                .mapToInt(Integer::intValue).max().orElse(1) * 5;
        frontScore = (frontScore / maxFrontFreq) * 100;

        double backScore = 0;
        for (int num : back) {
            backScore += pattern.backFrequency.getOrDefault(num, 0);
        }
        double maxBackFreq = pattern.backFrequency.values().stream()
                .mapToInt(Integer::intValue).max().orElse(1) * 2;
        backScore = (backScore / maxBackFreq) * 100;

        return (frontScore * 0.7 + backScore * 0.3);
    }

    /**
     * 计算和值合理性得分
     */
    private double calculateSumScore(int[] front, HistoryPattern pattern) {
        int sum = Arrays.stream(front).sum();
        int sumRange = sum / 20;

        // 如果落在最常见区间，得高分
        if (sumRange == pattern.mostCommonSumRange) {
            return 100;
        }

        // 根据与最常见区间的距离扣分
        int distance = Math.abs(sumRange - pattern.mostCommonSumRange);
        return Math.max(0, 100 - distance * 20);
    }

    /**
     * 计算奇偶比合理性得分
     */
    private double calculateOddEvenScore(int[] front, HistoryPattern pattern) {
        int oddCount = (int) Arrays.stream(front).filter(n -> n % 2 == 1).count();
        String oddEvenKey = oddCount + ":" + (5 - oddCount);

        // 最常见的奇偶比组合：3:2 和 2:3
        if (oddEvenKey.equals("3:2") || oddEvenKey.equals("2:3")) {
            return 100;
        }
        if (oddEvenKey.equals("4:1") || oddEvenKey.equals("1:4")) {
            return 60;
        }
        // 全奇或全偶得低分
        return 20;
    }

    /**
     * 计算号码分布均匀度得分
     */
    private double calculateDistributionScore(int[] front) {
        // 将1-35分为5个区间，每区间7个号码
        int[] zones = new int[5];
        for (int num : front) {
            int zone = (num - 1) / 7;
            if (zone >= 5) zone = 4;
            zones[zone]++;
        }

        // 统计覆盖的区间数
        int coveredZones = (int) Arrays.stream(zones).filter(z -> z > 0).count();

        // 覆盖越多区间越好
        return coveredZones * 20;
    }

    /**
     * 计算与近期号码的关联性得分
     */
    private double calculateCorrelationScore(int[] front, int[] back, List<LotteryResult> history) {
        if (history.isEmpty()) return 50;

        // 与最近一期的号码有1-2个重叠是正常的
        LotteryResult latest = history.get(0);
        int[] latestFront = latest.getFrontBallArray();
        int[] latestBack = latest.getBackBallArray();

        Set<Integer> latestFrontSet = new HashSet<>();
        for (int n : latestFront) latestFrontSet.add(n);

        Set<Integer> latestBackSet = new HashSet<>();
        for (int n : latestBack) latestBackSet.add(n);

        int frontOverlap = (int) Arrays.stream(front).filter(latestFrontSet::contains).count();
        int backOverlap = (int) Arrays.stream(back).filter(latestBackSet::contains).count();

        // 1-2个重叠最佳
        double score = 0;
        if (frontOverlap == 1 || frontOverlap == 2) {
            score += 60;
        } else if (frontOverlap == 0) {
            score += 40;
        } else {
            score += 20;
        }

        if (backOverlap == 1) {
            score += 40;
        } else {
            score += 20;
        }

        return score;
    }

    /**
     * 检查是否有连号
     */
    private boolean hasConsecutive(int[] nums) {
        int[] sorted = Arrays.copyOf(nums, nums.length);
        Arrays.sort(sorted);
        for (int i = 0; i < sorted.length - 1; i++) {
            if (sorted[i + 1] - sorted[i] == 1) {
                return true;
            }
        }
        return false;
    }

    /**
     * 历史模式特征
     */
    private static class HistoryPattern {
        Map<Integer, Integer> frontFrequency;
        Map<Integer, Integer> backFrequency;
        Map<Integer, Integer> sumDistribution;
        Map<String, Integer> oddEvenDistribution;
        double consecutiveRate;
        String mostCommonOddEven;
        int mostCommonSumRange;
    }
}
