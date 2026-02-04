package com.hobart.lottery.predictor;

import com.hobart.lottery.service.AnalysisService;
import lombok.Getter;

import java.util.*;

/**
 * 预测器基类
 */
@Getter
public abstract class BasePredictor {

    protected final AnalysisService analysisService;
    protected final Random random = new Random();

    public BasePredictor(AnalysisService analysisService) {
        this.analysisService = analysisService;
    }

    /**
     * 获取预测方法名称
     */
    public abstract String getMethodName();

    /**
     * 获取预测方法代码
     */
    public abstract String getMethodCode();

    /**
     * 生成一注预测号码
     * @return int[2][] - [0]为前区5个号码，[1]为后区2个号码
     */
    public abstract int[][] predict();

    /**
     * 生成多注预测号码
     */
    public List<int[][]> predictMultiple(int count) {
        Set<String> generated = new HashSet<>();
        List<int[][]> results = new ArrayList<>();
        
        int maxAttempts = count * 10;
        int attempts = 0;
        
        while (results.size() < count && attempts < maxAttempts) {
            int[][] prediction = predict();
            String key = arrayToKey(prediction);
            
            if (!generated.contains(key)) {
                generated.add(key);
                results.add(prediction);
            }
            attempts++;
        }
        
        // 如果生成不够，随机补充
        while (results.size() < count) {
            int[][] prediction = generateRandom();
            String key = arrayToKey(prediction);
            if (!generated.contains(key)) {
                generated.add(key);
                results.add(prediction);
            }
        }
        
        return results;
    }

    /**
     * 生成纯随机号码
     */
    protected int[][] generateRandom() {
        int[] front = generateRandomFront();
        int[] back = generateRandomBack();
        return new int[][]{front, back};
    }

    /**
     * 生成随机前区号码
     */
    protected int[] generateRandomFront() {
        Set<Integer> numbers = new TreeSet<>();
        while (numbers.size() < 5) {
            numbers.add(random.nextInt(35) + 1);
        }
        return numbers.stream().mapToInt(Integer::intValue).toArray();
    }

    /**
     * 生成随机后区号码
     */
    protected int[] generateRandomBack() {
        Set<Integer> numbers = new TreeSet<>();
        while (numbers.size() < 2) {
            numbers.add(random.nextInt(12) + 1);
        }
        return numbers.stream().mapToInt(Integer::intValue).toArray();
    }

    /**
     * 从候选号码中随机选择指定数量
     */
    protected int[] selectFromCandidates(List<Integer> candidates, int count, int min, int max) {
        Set<Integer> selected = new TreeSet<>();
        List<Integer> pool = new ArrayList<>(candidates);
        
        // 先从候选中选
        Collections.shuffle(pool);
        for (int num : pool) {
            if (selected.size() >= count) break;
            selected.add(num);
        }
        
        // 不够则随机补充
        while (selected.size() < count) {
            int num = random.nextInt(max - min + 1) + min;
            selected.add(num);
        }
        
        return selected.stream().mapToInt(Integer::intValue).toArray();
    }

    /**
     * 基于权重随机选择
     */
    protected int[] selectByWeight(Map<Integer, Double> scores, int count, int min, int max) {
        // 计算权重总和
        double totalWeight = scores.values().stream().mapToDouble(Double::doubleValue).sum();
        
        Set<Integer> selected = new TreeSet<>();
        int maxAttempts = count * 50;
        int attempts = 0;
        
        while (selected.size() < count && attempts < maxAttempts) {
            double r = random.nextDouble() * totalWeight;
            double cumulative = 0;
            
            for (Map.Entry<Integer, Double> entry : scores.entrySet()) {
                cumulative += entry.getValue();
                if (r <= cumulative && !selected.contains(entry.getKey())) {
                    selected.add(entry.getKey());
                    break;
                }
            }
            attempts++;
        }
        
        // 不够则随机补充
        while (selected.size() < count) {
            int num = random.nextInt(max - min + 1) + min;
            selected.add(num);
        }
        
        return selected.stream().mapToInt(Integer::intValue).toArray();
    }

    /**
     * 将预测结果转为字符串key用于去重
     */
    private String arrayToKey(int[][] prediction) {
        return Arrays.toString(prediction[0]) + "-" + Arrays.toString(prediction[1]);
    }
}
