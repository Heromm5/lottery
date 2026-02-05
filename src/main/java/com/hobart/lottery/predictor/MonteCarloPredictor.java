package com.hobart.lottery.predictor;

import com.hobart.lottery.domain.model.NumberZone;
import com.hobart.lottery.entity.LotteryResult;
import com.hobart.lottery.service.AnalysisService;
import com.hobart.lottery.service.LotteryService;

import java.util.*;

/**
 * 蒙特卡洛模拟预测器
 * 通过大量随机模拟估计概率分布
 * 
 * 方法：
 * 1. 直接抽样：从历史分布中直接抽样
 * 2. MCMC（Metropolis-Hastings）：从复杂分布中采样
 * 3. 重要性采样：使用提议分布优化采样效率
 */
public class MonteCarloPredictor extends BasePredictor {

    private final LotteryService lotteryService;
    
    // 模拟次数
    private static final int DIRECT_SAMPLES = 50000;
    private static final int MCMC_SAMPLES = 10000;
    private static final int IMPORTANCE_SAMPLES = 20000;
    
    // 历史数据量
    private static final int HISTORY_PERIODS = 300;

    public MonteCarloPredictor(AnalysisService analysisService, LotteryService lotteryService) {
        super(analysisService);
        this.lotteryService = lotteryService;
    }

    @Override
    public String getMethodName() {
        return "蒙特卡洛";
    }

    @Override
    public String getMethodCode() {
        return "MONTECARLO";
    }

    @Override
    public int[][] predict() {
        List<LotteryResult> results = lotteryService.getRecentResults(HISTORY_PERIODS);
        
        if (results.size() < 10) {
            return generateRandom();
        }
        
        // 方法1：直接抽样
        Map<Integer, Integer> directFreq = directSampling(results);
        
        // 方法2：MCMC抽样
        Map<Integer, Integer> mcmcFreq = metropolisHastingsSampling(results);
        
        // 方法3：重要性抽样
        Map<Integer, Integer> importanceFreq = importanceSampling(results);
        
        // 融合三种方法的结果
        Map<Integer, Integer> fusedFreq = fuseResults(directFreq, mcmcFreq, importanceFreq);
        
        // 选择频率最高的号码组合
        int[] front = selectByFrequency(fusedFreq, 5, 1, 35);
        int[] back = selectByFrequency(fusedFreq, 2, 1, 12);
        
        return new int[][]{front, back};
    }

    /**
     * 方法1：直接抽样
     * 从历史开奖结果中直接抽取号码组合
     */
    private Map<Integer, Integer> directSampling(List<LotteryResult> results) {
        Map<Integer, Integer> frequency = new HashMap<>();
        
        for (int i = 0; i < DIRECT_SAMPLES; i++) {
            // 随机选择一期历史数据
            int idx = random.nextInt(results.size());
            LotteryResult result = results.get(idx);
            
            // 记录前区号码
            for (int ball : result.getFrontBallArray()) {
                frequency.merge(ball, 1, Integer::sum);
            }
        }
        
        return frequency;
    }

    /**
     * 方法2：Metropolis-Hastings 算法
     * 从基于频率分布的复杂分布中采样
     */
    private Map<Integer, Integer> metropolisHastingsSampling(List<LotteryResult> results) {
        Map<Integer, Integer> frequency = new HashMap<>();
        
        // 计算历史频率分布（作为目标分布）
        Map<Integer, Double> targetDist = calculateFrequencyDistribution(results);
        
        // 初始化：使用均匀分布作为起始状态
        int[] current = generateRandomFront();
        double currentProb = calculateProbability(current, targetDist);
        
        for (int i = 0; i < MCMC_SAMPLES; i++) {
            // 提议：随机扰动当前解
            int[] proposal = proposeNewState(current);
            double proposalProb = calculateProbability(proposal, targetDist);
            
            // 接受概率
            double acceptance = proposalProb / currentProb;
            
            if (Math.random() < acceptance) {
                current = proposal;
                currentProb = proposalProb;
            }
            
            // 记录
            for (int ball : current) {
                frequency.merge(ball, 1, Integer::sum);
            }
        }
        
        return frequency;
    }

    /**
     * 获取抽样统计信息
     */
    private int[] proposeNewState(int[] current) {
        int[] next = current.clone();
        
        // 随机替换1-2个号码
        int changes = random.nextInt(3);  // 0, 1, or 2
        for (int i = 0; i < changes; i++) {
            int idx = random.nextInt(5);
            int newNum = random.nextInt(35) + 1;
            
            // 确保不重复
            Set<Integer> set = new HashSet<>();
            for (int num : next) {
                if (num != next[idx]) {
                    set.add(num);
                }
            }
            
            // 找到第一个不重复的新号码
            while (set.contains(newNum)) {
                newNum = random.nextInt(35) + 1;
            }
            next[idx] = newNum;
        }
        
        return next;
    }

    /**
     * 计算组合在目标分布下的概率
     */
    private double calculateProbability(int[] combo, Map<Integer, Double> targetDist) {
        double prob = 1.0;
        for (int ball : combo) {
            prob *= targetDist.getOrDefault(ball, 0.01);
        }
        return prob;
    }

    /**
     * 方法3：重要性采样
     * 使用均匀分布作为提议分布
     */
    private Map<Integer, Integer> importanceSampling(List<LotteryResult> results) {
        Map<Integer, Integer> frequency = new HashMap<>();
        
        // 计算历史频率分布（目标分布）
        Map<Integer, Double> targetDist = calculateFrequencyDistribution(results);
        
        // 提议分布：均匀分布
        double uniformProb = 1.0 / 35;
        
        // 重要性权重 = 目标概率 / 提议概率
        for (int i = 0; i < IMPORTANCE_SAMPLES; i++) {
            // 从提议分布（均匀分布）采样
            int[] sample = generateRandomFront();
            
            // 计算重要性权重
            double weight = 1.0;
            for (int ball : sample) {
                double targetProb = targetDist.getOrDefault(ball, uniformProb);
                weight *= targetProb / uniformProb;
            }
            
            // 归一化权重
            int numBalls = 5;
            weight = Math.pow(weight, 1.0 / numBalls);
            
            // 记录加权频率
            for (int ball : sample) {
                int weightedCount = (int) (weight * 1000);
                frequency.merge(ball, weightedCount, Integer::sum);
            }
        }
        
        return frequency;
    }

    /**
     * 计算频率分布
     */
    private Map<Integer, Double> calculateFrequencyDistribution(List<LotteryResult> results) {
        Map<Integer, Integer> count = new HashMap<>();
        int total = 0;
        
        for (LotteryResult result : results) {
            for (int ball : result.getFrontBallArray()) {
                count.merge(ball, 1, Integer::sum);
                total++;
            }
        }
        
        // 转换为概率分布
        Map<Integer, Double> distribution = new HashMap<>();
        for (Map.Entry<Integer, Integer> entry : count.entrySet()) {
            distribution.put(entry.getKey(), (double) entry.getValue() / total);
        }
        
        return distribution;
    }

    /**
     * 融合三种方法的结果
     */
    private Map<Integer, Integer> fuseResults(Map<Integer, Integer>... results) {
        Map<Integer, Integer> fused = new HashMap<>();
        
        for (Map<Integer, Integer> result : results) {
            for (Map.Entry<Integer, Integer> entry : result.entrySet()) {
                fused.merge(entry.getKey(), entry.getValue(), Integer::sum);
            }
        }
        
        return fused;
    }

    /**
     * 根据频率选择号码
     */
    private int[] selectByFrequency(Map<Integer, Integer> frequency, int need, int min, int max) {
        List<Map.Entry<Integer, Integer>> entries = new ArrayList<>(frequency.entrySet());
        
        // 过滤有效范围内的号码
        final int finalMin = min;
        final int finalMax = max;
        entries = entries.stream()
            .filter(e -> e.getKey() >= finalMin && e.getKey() <= finalMax)
            .collect(java.util.stream.Collectors.toList());
        
        // 按频率降序排序
        entries = entries.stream()
            .sorted((a, b) -> Integer.compare(b.getValue(), a.getValue()))
            .collect(java.util.stream.Collectors.toList());
        
        // 选择最高的need个
        Set<Integer> selected = new HashSet<>();
        int[] result = new int[need];
        int idx = 0;
        
        for (Map.Entry<Integer, Integer> entry : entries) {
            if (selected.size() >= need) break;
            
            int num = entry.getKey();
            if (!selected.contains(num)) {
                selected.add(num);
                result[idx++] = num;
            }
        }
        
        Arrays.sort(result);
        return result;
    }

    /**
     * 获取抽样统计信息
     */
    public Map<String, Object> getSamplingStats(List<LotteryResult> results) {
        Map<String, Object> stats = new HashMap<>();
        
        stats.put("directSamples", DIRECT_SAMPLES);
        stats.put("mcmcSamples", MCMC_SAMPLES);
        stats.put("importanceSamples", IMPORTANCE_SAMPLES);
        stats.put("historyPeriods", results.size());
        
        // 直接抽样分布
        Map<Integer, Integer> directFreq = directSampling(results);
        stats.put("directTop5", getTopNumbers(directFreq, 5));
        
        // MCMC分布
        Map<Integer, Integer> mcmcFreq = metropolisHastingsSampling(results);
        stats.put("mcmcTop5", getTopNumbers(mcmcFreq, 5));
        
        return stats;
    }

    /**
     * 获取频率最高的号码
     */
    private List<Integer> getTopNumbers(Map<Integer, Integer> frequency, int n) {
        return frequency.entrySet().stream()
            .sorted((a, b) -> Integer.compare(b.getValue(), a.getValue()))
            .limit(n)
            .map(Map.Entry::getKey)
            .collect(java.util.stream.Collectors.toList());
    }
}
