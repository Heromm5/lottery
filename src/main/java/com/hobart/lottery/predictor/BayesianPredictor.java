package com.hobart.lottery.predictor;

import com.hobart.lottery.domain.model.NumberZone;
import com.hobart.lottery.entity.LotteryResult;
import com.hobart.lottery.service.AnalysisService;
import com.hobart.lottery.service.LotteryService;

import java.util.*;
import java.util.stream.Collectors;
public class BayesianPredictor extends BasePredictor {

    private final LotteryService lotteryService;
    
    // 先验概率（基于理论概率 + 历史频率混合）
    private static final double THEORETICAL_PRIOR_FRONT = 1.0 / 35;
    private static final double THEORETICAL_PRIOR_BACK = 1.0 / 12;
    
    public BayesianPredictor(AnalysisService analysisService, LotteryService lotteryService) {
        super(analysisService);
        this.lotteryService = lotteryService;
    }

    @Override
    public String getMethodName() {
        return "贝叶斯";
    }

    @Override
    public String getMethodCode() {
        return "BAYESIAN";
    }

    @Override
    public int[][] predict() {
        List<LotteryResult> results = lotteryService.getRecentResults(200);
        
        // 计算前后区概率
        Map<Integer, Double> frontProbs = calculatePosteriorProb(results, NumberZone.FRONT);
        Map<Integer, Double> backProbs = calculatePosteriorProb(results, NumberZone.BACK);
        
        // 基于后验概率采样
        int[] front = sampleByProbability(frontProbs, 5, NumberZone.FRONT);
        int[] back = sampleByProbability(backProbs, 2, NumberZone.BACK);
        
        return new int[][]{front, back};
    }

    /**
     * 计算后验概率
     * 后验概率 = 似然 × 先验 / 归一化常数
     */
    private Map<Integer, Double> calculatePosteriorProb(List<LotteryResult> results, NumberZone zone) {
        Map<Integer, Double> posterior = new HashMap<>();
        double totalEvidence = 0;
        
        int min = zone.getMin();
        int max = zone.getMax();
        int count = zone.getCount();
        
        // 历史统计
        Map<Integer, Integer> appearCount = countAppearances(results, zone);
        double theoreticalPrior = zone == NumberZone.FRONT ? THEORETICAL_PRIOR_FRONT : THEORETICAL_PRIOR_BACK;
        
        for (int num = min; num <= max; num++) {
            // 1. 似然：历史数据中该号码出现的频率
            double likelihood = calculateLikelihood(num, appearCount, results.size(), zone);
            
            // 2. 先验：混合先验（理论先验 + 历史频率先验）
            double prior = calculatePrior(num, appearCount, results.size(), theoreticalPrior);
            
            // 3. 后验概率（未归一化）
            double posteriorUnnormalized = likelihood * prior;
            
            posterior.put(num, posteriorUnnormalized);
            totalEvidence += posteriorUnnormalized;
        }
        
        // 归一化
        final double finalTotal = totalEvidence;
        posterior.forEach((k, v) -> posterior.put(k, v / finalTotal));
        
        return posterior;
    }

    /**
     * 计算似然 P(历史数据|出现)
     * 使用多项分布似然
     */
    private double calculateLikelihood(int num, Map<Integer, Integer> appearCount, 
                                       int totalPeriods, NumberZone zone) {
        int count = appearCount.getOrDefault(num, 0);
        int totalNumbers = zone.getCount() * totalPeriods;
        
        // 使用Beta分布作为先验（贝叶斯方法）
        double alpha = count + 1;  // 成功次数 + 1（拉普拉斯平滑）
        double beta = totalNumbers - count + (zone.getCount() - 1);  // 失败次数 + 1
        
        // Beta分布的众数作为似然估计
        if (alpha > 1 && beta > 1) {
            return (alpha - 1) / (alpha + beta - 2);
        } else if (alpha > 1) {
            return 1.0;
        } else {
            return 0.5;
        }
    }

    /**
     * 计算混合先验
     * 先验 = w1 * 理论先验 + w2 * 历史先验
     */
    private double calculatePrior(int num, Map<Integer, Integer> appearCount, 
                                  int totalPeriods, double theoreticalPrior) {
        // 历史频率作为先验
        double historicalPrior = (double) appearCount.getOrDefault(num, 0) / totalPeriods;
        
        // 混合权重（使用经验权重）
        double wHistorical = 0.7;
        double wTheoretical = 0.3;
        
        return wHistorical * historicalPrior + wTheoretical * theoreticalPrior;
    }

    /**
     * 统计号码出现次数
     */
    private Map<Integer, Integer> countAppearances(List<LotteryResult> results, NumberZone zone) {
        Map<Integer, Integer> count = new HashMap<>();
        
        for (LotteryResult result : results) {
            int[] balls = zone == NumberZone.FRONT 
                ? result.getFrontBallArray() 
                : result.getBackBallArray();
            
            for (int ball : balls) {
                count.merge(ball, 1, Integer::sum);
            }
        }
        
        return count;
    }

    /**
     * 基于概率采样（保证不重复）
     */
    private int[] sampleByProbability(Map<Integer, Double> probs, int need, NumberZone zone) {
        List<Map.Entry<Integer, Double>> entries = new ArrayList<>(probs.entrySet());
        
        // 按概率降序排序
        entries.sort((a, b) -> Double.compare(b.getValue(), a.getValue()));
        
        // 选择概率最高的need个（去重）
        Set<Integer> selected = new HashSet<>();
        int[] result = new int[need];
        int idx = 0;
        
        for (Map.Entry<Integer, Double> entry : entries) {
            if (selected.size() >= need) break;
            
            int num = entry.getKey();
            // 验证号码在有效范围内
            if (num >= zone.getMin() && num <= zone.getMax() && !selected.contains(num)) {
                selected.add(num);
                result[idx++] = num;
            }
        }
        
        // 如果选不够，从剩余中随机补充
        if (selected.size() < need) {
            List<Integer> remaining = entries.stream()
                .map(Map.Entry::getKey)
                .filter(n -> !selected.contains(n))
                .filter(n -> n >= zone.getMin() && n <= zone.getMax())
                .collect(Collectors.toList());
            
            Collections.shuffle(remaining);
            for (int num : remaining) {
                if (selected.size() >= need) break;
                selected.add(num);
                result[idx++] = num;
            }
        }
        
        Arrays.sort(result);
        return result;
    }
}
