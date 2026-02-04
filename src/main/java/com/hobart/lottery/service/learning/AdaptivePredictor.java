package com.hobart.lottery.service.learning;

import com.hobart.lottery.domain.model.NumberZone;
import com.hobart.lottery.service.analysis.FrequencyAnalyzer;
import com.hobart.lottery.service.analysis.MissingAnalyzer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 自适应预测器
 * 根据各方法的历史表现权重，加权融合生成预测
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AdaptivePredictor {
    
    private final WeightAdjuster weightAdjuster;
    private final FrequencyAnalyzer frequencyAnalyzer;
    private final MissingAnalyzer missingAnalyzer;
    
    private final Random random = new Random();
    
    /**
     * 使用自适应权重生成预测
     * 
     * @return int[2][] - [0]为前区5个号码，[1]为后区2个号码
     */
    public int[][] predict() {
        Map<String, Double> weights = weightAdjuster.getMethodWeights();
        
        // 获取各方法的号码评分
        Map<Integer, Double> frontScores = calculateWeightedScores(NumberZone.FRONT, weights);
        Map<Integer, Double> backScores = calculateWeightedScores(NumberZone.BACK, weights);
        
        // 根据加权分数选择号码
        int[] front = selectByWeightedScore(frontScores, 5);
        int[] back = selectByWeightedScore(backScores, 2);
        
        log.info("自适应预测生成: 前区{}, 后区{}", Arrays.toString(front), Arrays.toString(back));
        
        return new int[][] { front, back };
    }
    
    /**
     * 生成多注预测
     */
    public List<int[][]> predictMultiple(int count) {
        Set<String> generated = new HashSet<>();
        List<int[][]> results = new ArrayList<>();
        
        int maxAttempts = count * 10;
        int attempts = 0;
        
        while (results.size() < count && attempts < maxAttempts) {
            int[][] prediction = predict();
            String key = Arrays.toString(prediction[0]) + "-" + Arrays.toString(prediction[1]);
            
            if (!generated.contains(key)) {
                generated.add(key);
                results.add(prediction);
            }
            attempts++;
        }
        
        return results;
    }
    
    /**
     * 计算加权综合评分
     */
    private Map<Integer, Double> calculateWeightedScores(NumberZone zone, Map<String, Double> weights) {
        Map<Integer, Double> finalScores = new HashMap<>();
        
        // 初始化
        for (int i = zone.getMin(); i <= zone.getMax(); i++) {
            finalScores.put(i, 0.0);
        }
        
        // HOT 方法：热号得高分
        double hotWeight = weights.getOrDefault("HOT", 0.2);
        List<Integer> hotNumbers = frequencyAnalyzer.getHotNumbers(zone, zone.getCount() * 2);
        for (int i = 0; i < hotNumbers.size(); i++) {
            int num = hotNumbers.get(i);
            double score = (hotNumbers.size() - i) * 10.0; // 越热分越高
            finalScores.merge(num, score * hotWeight, Double::sum);
        }
        
        // MISSING 方法：高遗漏得高分
        double missingWeight = weights.getOrDefault("MISSING", 0.2);
        List<Integer> missingNumbers = missingAnalyzer.getHighMissingNumbers(zone, zone.getCount() * 2);
        for (int i = 0; i < missingNumbers.size(); i++) {
            int num = missingNumbers.get(i);
            double score = (missingNumbers.size() - i) * 10.0;
            finalScores.merge(num, score * missingWeight, Double::sum);
        }
        
        // BALANCED 方法：遗漏到期号码得高分
        double balancedWeight = weights.getOrDefault("BALANCED", 0.2);
        List<Integer> dueNumbers = missingAnalyzer.getMissingDueNumbers(zone, zone.getCount() * 2);
        for (int i = 0; i < dueNumbers.size(); i++) {
            int num = dueNumbers.get(i);
            double score = (dueNumbers.size() - i) * 10.0;
            finalScores.merge(num, score * balancedWeight, Double::sum);
        }
        
        // ADAPTIVE 方法：使用综合评分（自身权重，用于自我参考）
        double adaptiveWeight = weights.getOrDefault("ADAPTIVE", 0.2);
        Map<Integer, Double> adaptiveScores = frequencyAnalyzer.getNumberScores(zone, missingAnalyzer);
        for (Map.Entry<Integer, Double> entry : adaptiveScores.entrySet()) {
            finalScores.merge(entry.getKey(), entry.getValue() * adaptiveWeight / 10, Double::sum);
        }
        
        // ML 方法权重暂时用随机因子模拟
        double mlWeight = weights.getOrDefault("ML", 0.2);
        for (int i = zone.getMin(); i <= zone.getMax(); i++) {
            double randomFactor = random.nextDouble() * 5;
            finalScores.merge(i, randomFactor * mlWeight, Double::sum);
        }
        
        return finalScores;
    }
    
    /**
     * 根据加权分数选择号码
     */
    private int[] selectByWeightedScore(Map<Integer, Double> scores, int count) {
        // 将分数转换为权重（确保非负）
        double minScore = scores.values().stream().mapToDouble(Double::doubleValue).min().orElse(0);
        Map<Integer, Double> adjustedScores = new HashMap<>();
        for (Map.Entry<Integer, Double> entry : scores.entrySet()) {
            adjustedScores.put(entry.getKey(), entry.getValue() - minScore + 1);
        }
        
        double totalWeight = adjustedScores.values().stream().mapToDouble(Double::doubleValue).sum();
        
        Set<Integer> selected = new TreeSet<>();
        int maxAttempts = count * 50;
        int attempts = 0;
        
        while (selected.size() < count && attempts < maxAttempts) {
            double r = random.nextDouble() * totalWeight;
            double cumulative = 0;
            
            for (Map.Entry<Integer, Double> entry : adjustedScores.entrySet()) {
                cumulative += entry.getValue();
                if (r <= cumulative && !selected.contains(entry.getKey())) {
                    selected.add(entry.getKey());
                    break;
                }
            }
            attempts++;
        }
        
        // 如果选择不够，随机补充
        List<Integer> remaining = new ArrayList<>();
        for (int num : scores.keySet()) {
            if (!selected.contains(num)) {
                remaining.add(num);
            }
        }
        Collections.shuffle(remaining);
        
        int idx = 0;
        while (selected.size() < count && idx < remaining.size()) {
            selected.add(remaining.get(idx++));
        }
        
        return selected.stream().mapToInt(Integer::intValue).toArray();
    }
    
    /**
     * 获取当前各方法权重信息（用于展示）
     */
    public Map<String, Double> getCurrentWeights() {
        return weightAdjuster.getMethodWeights();
    }
}
