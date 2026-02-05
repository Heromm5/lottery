package com.hobart.lottery.predictor;

import com.hobart.lottery.domain.model.NumberZone;
import com.hobart.lottery.service.AnalysisService;
import com.hobart.lottery.service.LotteryService;

import java.util.*;

/**
 * 集成预测器
 * 融合多种预测方法的结果
 * 
 * 方法：
 * 1. 加权投票：根据各方法的历史准确率分配权重
 * 2. 概率融合：将各方法的概率预测加权平均
 */
public class EnsemblePredictor extends BasePredictor {

    private final LotteryService lotteryService;
    
    // 包含的预测器
    private List<BasePredictor> predictors;
    
    // 各预测器的权重
    private double[] weights;
    
    // 默认权重
    private static final double[] DEFAULT_WEIGHTS = {
        0.15,  // 热号优先
        0.15,  // 遗漏回补
        0.15,  // 冷热均衡
        0.20,  // 机器学习
        0.10,  // 贝叶斯
        0.125, // 马尔可夫
        0.125  // 蒙特卡洛
    };

    public EnsemblePredictor(AnalysisService analysisService, LotteryService lotteryService) {
        super(analysisService);
        this.lotteryService = lotteryService;
        
        // 初始化预测器列表
        this.predictors = new ArrayList<>();
        this.predictors.add(new HotNumberPredictor(analysisService));
        this.predictors.add(new MissingPredictor(analysisService));
        this.predictors.add(new BalancedPredictor(analysisService));
        this.predictors.add(new MLPredictor(analysisService, lotteryService));
        this.predictors.add(new BayesianPredictor(analysisService, lotteryService));
        this.predictors.add(new MarkovPredictor(analysisService, lotteryService));
        this.predictors.add(new MonteCarloPredictor(analysisService, lotteryService));
        
        this.weights = DEFAULT_WEIGHTS.clone();
    }

    @Override
    public String getMethodName() {
        return "集成预测";
    }

    @Override
    public String getMethodCode() {
        return "ENSEMBLE";
    }

    @Override
    public int[][] predict() {
        // 获取各方法的概率预测
        List<Map<Integer, Double>> allProbabilities = new ArrayList<>();
        
        for (BasePredictor predictor : predictors) {
            int[][] prediction = predictor.predict();
            Map<Integer, Double> probs = convertToProbabilities(prediction);
            allProbabilities.add(probs);
        }
        
        // 融合概率
        Map<Integer, Double> fusedProbs = fuseProbabilities(allProbabilities);
        
        // 选择概率最高的组合
        int[] front = selectByProbability(fusedProbs, 5, 1, 35);
        int[] back = selectByProbability(fusedProbs, 2, 1, 12);
        
        return new int[][]{front, back};
    }

    /**
     * 融合概率预测
     * 加权平均各方法的概率
     */
    private Map<Integer, Double> fuseProbabilities(List<Map<Integer, Double>> allProbs) {
        Map<Integer, Double> fused = new HashMap<>();
        
        // 初始化
        for (int i = 1; i <= 35; i++) {
            fused.put(i, 0.0);
        }
        
        // 加权融合
        for (int p = 0; p < allProbs.size(); p++) {
            Map<Integer, Double> probs = allProbs.get(p);
            double weight = weights[p];
            
            for (Map.Entry<Integer, Double> entry : probs.entrySet()) {
                int num = entry.getKey();
                double prob = entry.getValue();
                fused.merge(num, prob * weight, Double::sum);
            }
        }
        
        return fused;
    }

    /**
     * 将预测结果转换为概率分布
     */
    private Map<Integer, Double> convertToProbabilities(int[][] prediction) {
        Map<Integer, Double> probs = new HashMap<>();
        
        // 预测的号码获得更高的概率
        for (int ball : prediction[0]) {
            probs.put(ball, probs.getOrDefault(ball, 0.0) + 0.5);
        }
        
        // 归一化
        double total = probs.values().stream().mapToDouble(Double::doubleValue).sum();
        if (total > 0) {
            probs.replaceAll((k, v) -> v / total);
        }
        
        return probs;
    }

    /**
     * 根据概率选择号码
     */
    private int[] selectByProbability(Map<Integer, Double> probs, int need, int min, int max) {
        // 创建候选列表
        List<Map.Entry<Integer, Double>> candidates = new ArrayList<>();
        for (int i = min; i <= max; i++) {
            candidates.add(new AbstractMap.SimpleEntry<>(i, probs.getOrDefault(i, 0.0)));
        }
        
        // 按概率降序排序
        candidates.sort((a, b) -> Double.compare(b.getValue(), a.getValue()));
        
        // 选择最高的need个
        Set<Integer> selected = new HashSet<>();
        int[] result = new int[need];
        int idx = 0;
        
        for (Map.Entry<Integer, Double> entry : candidates) {
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
     * 根据历史准确率调整权重
     */
    public void adjustWeightsByAccuracy(Map<String, Double> accuracies) {
        String[] methodCodes = {"HOT", "MISSING", "BALANCED", "ML", "BAYESIAN", "MARKOV", "MONTECARLO"};
        
        double totalAccuracy = 0;
        double[] newWeights = new double[weights.length];
        
        for (int i = 0; i < methodCodes.length; i++) {
            double accuracy = accuracies.getOrDefault(methodCodes[i], 0.0);
            newWeights[i] = accuracy * DEFAULT_WEIGHTS[i];
            totalAccuracy += newWeights[i];
        }
        
        // 归一化
        if (totalAccuracy > 0) {
            for (int i = 0; i < weights.length; i++) {
                weights[i] = newWeights[i] / totalAccuracy;
            }
        } else {
            weights = DEFAULT_WEIGHTS.clone();
        }
    }

    /**
     * 获取当前权重
     */
    public Map<String, Double> getCurrentWeights() {
        Map<String, Double> weightMap = new HashMap<>();
        String[] names = {"热号优先", "遗漏回补", "冷热均衡", "机器学习", "贝叶斯", "马尔可夫", "蒙特卡洛"};
        
        for (int i = 0; i < names.length; i++) {
            weightMap.put(names[i], weights[i]);
        }
        
        return weightMap;
    }

    /**
     * 获取各预测器的预测结果
     */
    public List<int[][]> getAllPredictions() {
        List<int[][]> results = new ArrayList<>();
        
        for (BasePredictor predictor : predictors) {
            results.add(predictor.predict());
        }
        
        return results;
    }

    /**
     * 获取预测结果对比
     */
    public Map<String, Object> getPredictionComparison() {
        Map<String, Object> comparison = new HashMap<>();
        
        List<int[][]> predictions = getAllPredictions();
        String[] names = {"热号优先", "遗漏回补", "冷热均衡", "机器学习", "贝叶斯", "马尔可夫", "蒙特卡洛"};
        
        for (int i = 0; i < names.length; i++) {
            Map<String, Object> pred = new HashMap<>();
            pred.put("front", Arrays.toString(predictions.get(i)[0]));
            pred.put("back", Arrays.toString(predictions.get(i)[1]));
            comparison.put(names[i], pred);
        }
        
        // 集成预测
        int[][] ensembleResult = predict();
        Map<String, String> ensembleFront = new HashMap<>();
        ensembleFront.put("front", Arrays.toString(ensembleResult[0]));
        ensembleFront.put("back", Arrays.toString(ensembleResult[1]));
        comparison.put("集成预测", ensembleFront);
        
        return comparison;
    }
}
